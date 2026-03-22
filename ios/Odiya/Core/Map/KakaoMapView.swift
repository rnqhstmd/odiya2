import SwiftUI
import KakaoMapsSDK

// MARK: - KakaoMapContainerView (Public API)

/// 카카오맵 지도를 표시하는 SwiftUI 컴포넌트.
/// 인증 실패 시 자동으로 fallback 뷰를 표시한다.
struct KakaoMapContainerView: View {

    let latitude: Double?
    let longitude: Double?
    let height: CGFloat
    let markerTitle: String?
    let isScrollEnabled: Bool

    init(
        latitude: Double? = nil,
        longitude: Double? = nil,
        height: CGFloat = 200,
        markerTitle: String? = nil,
        isScrollEnabled: Bool = false
    ) {
        self.latitude = latitude
        self.longitude = longitude
        self.height = height
        self.markerTitle = markerTitle
        self.isScrollEnabled = isScrollEnabled
    }

    @State private var showFallback = false

    var body: some View {
        Group {
            if showFallback || !hasValidCoordinates {
                MapFallbackView(placeName: markerTitle, height: height)
            } else {
                KakaoMapView(
                    latitude: latitude,
                    longitude: longitude,
                    markerTitle: markerTitle,
                    isScrollEnabled: isScrollEnabled,
                    onAuthFailure: { showFallback = true }
                )
                .frame(height: height)
            }
        }
    }

    /// 좌표가 0,0이면 유효하지 않은 좌표로 판단
    private var hasValidCoordinates: Bool {
        guard let lat = latitude, let lon = longitude else { return false }
        return !(lat == 0 && lon == 0)
    }
}

// MARK: - KakaoMapView (UIViewControllerRepresentable)

struct KakaoMapView: UIViewControllerRepresentable {

    let latitude: Double?
    let longitude: Double?
    let markerTitle: String?
    let isScrollEnabled: Bool
    let onAuthFailure: () -> Void

    func makeUIViewController(context: Context) -> KakaoMapViewController {
        let vc = KakaoMapViewController()
        vc.latitude = latitude
        vc.longitude = longitude
        vc.markerTitle = markerTitle
        vc.isScrollEnabled = isScrollEnabled
        vc.onAuthFailure = onAuthFailure
        return vc
    }

    func updateUIViewController(_ vc: KakaoMapViewController, context: Context) {
        let newLat = latitude ?? KakaoMapViewController.defaultLatitude
        let newLon = longitude ?? KakaoMapViewController.defaultLongitude
        let oldLat = vc.latitude ?? KakaoMapViewController.defaultLatitude
        let oldLon = vc.longitude ?? KakaoMapViewController.defaultLongitude

        if abs(newLat - oldLat) > 0.00001 || abs(newLon - oldLon) > 0.00001 || vc.markerTitle != markerTitle {
            vc.updatePosition(latitude: newLat, longitude: newLon, title: markerTitle)
        }
    }

    static func dismantleUIViewController(_ vc: KakaoMapViewController, coordinator: ()) {
        vc.cleanup()
    }
}

// MARK: - KakaoMapViewController

class KakaoMapViewController: UIViewController, MapControllerDelegate {

    // MARK: - 기본 좌표 (서울 시청)
    static let defaultLatitude: Double = 37.5665
    static let defaultLongitude: Double = 126.9780

    // MARK: - 설정
    var latitude: Double?
    var longitude: Double?
    var markerTitle: String?
    var isScrollEnabled: Bool = false
    var onAuthFailure: (() -> Void)?

    // MARK: - 내부 상태
    private var mapContainer: KMViewContainer?
    private var mapController: KMController?
    private var authSucceeded = false
    private var authRetryCount = 0
    private let maxAuthRetry = 1

    // MARK: - Lifecycle

    override func loadView() {
        let container = KMViewContainer()
        self.view = container
        self.mapContainer = container
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        mapController = KMController(viewContainer: mapContainer!)
        mapController?.delegate = self
        mapController?.prepareEngine()
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        if authSucceeded {
            mapController?.activateEngine()
        }
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        mapController?.pauseEngine()
    }

    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        mapController?.resetEngine()
    }

    func cleanup() {
        mapController?.pauseEngine()
        mapController?.resetEngine()
    }

    // MARK: - MapControllerDelegate

    func authenticationSucceeded() {
        authSucceeded = true
        mapController?.activateEngine()
    }

    func authenticationFailed(_ errorCode: Int, desc: String) {
        if authRetryCount < maxAuthRetry {
            authRetryCount += 1
            mapController?.prepareEngine()
        } else {
            DispatchQueue.main.async { [weak self] in
                self?.onAuthFailure?()
            }
        }
    }

    func addViews() {
        let lon = longitude ?? Self.defaultLongitude
        let lat = latitude ?? Self.defaultLatitude

        // MapPoint(longitude:, latitude:) — 경도, 위도 순서
        let position = MapPoint(longitude: lon, latitude: lat)
        let info = MapviewInfo(
            viewName: "mapview",
            viewInfoName: "map",
            defaultPosition: position,
            defaultLevel: 15
        )
        mapController?.addView(info)
    }

    func addViewSucceeded(_ viewName: String, viewInfoName: String) {
        if markerTitle != nil {
            addMarker()
        }

        if !isScrollEnabled {
            disableGestures(viewName: viewName)
        }
    }

    func addViewFailed(_ viewName: String, viewInfoName: String) {
        // 지도 뷰 추가 실패 — fallback 트리거
        DispatchQueue.main.async { [weak self] in
            self?.onAuthFailure?()
        }
    }

    // MARK: - 마커

    private func addMarker() {
        guard let mapView = mapController?.getView("mapview") as? KakaoMap else { return }
        let manager = mapView.getLabelManager()

        let lon = longitude ?? Self.defaultLongitude
        let lat = latitude ?? Self.defaultLatitude

        let layerOption = LabelLayerOptions(
            layerID: "markerLayer",
            competitionType: .none,
            competitionUnit: .symbolFirst,
            orderType: .rank,
            zOrder: 10001
        )
        let layer = manager.addLabelLayer(option: layerOption)

        let iconStyle = PoiIconStyle(
            symbol: UIImage(systemName: "mappin.circle.fill")?
                .withTintColor(.systemPurple, renderingMode: .alwaysOriginal)
        )
        if manager.getPoiStyle(styleID: "defaultMarker") == nil {
            let poiStyle = PoiStyle(styleID: "defaultMarker", styles: [
                PerLevelPoiStyle(iconStyle: iconStyle, level: 0)
            ])
            manager.addPoiStyle(poiStyle)
        }

        let options = PoiOptions(styleID: "defaultMarker")
        let point = MapPoint(longitude: lon, latitude: lat)
        if let poi = layer?.addPoi(option: options, at: point) {
            poi.show()
        }
    }

    // MARK: - 제스처 비활성화

    private func disableGestures(viewName: String) {
        guard let mapView = mapController?.getView(viewName) as? KakaoMap else { return }
        mapView.setGestureEnable(type: .pan, enable: false)
        mapView.setGestureEnable(type: .rotate, enable: false)
        mapView.setGestureEnable(type: .tilt, enable: false)
        // 줌은 허용
    }

    // MARK: - 좌표 업데이트

    func updatePosition(latitude: Double, longitude: Double, title: String?) {
        self.latitude = latitude
        self.longitude = longitude
        self.markerTitle = title

        guard authSucceeded,
              let mapView = mapController?.getView("mapview") as? KakaoMap else { return }

        let newPoint = MapPoint(longitude: longitude, latitude: latitude)
        mapView.moveCamera(
            CameraUpdate.make(target: newPoint, mapView: mapView)
        )

        // 기존 마커 제거 후 재추가
        let manager = mapView.getLabelManager()
        manager.removeLabelLayer(layerID: "markerLayer")
        if title != nil {
            addMarker()
        }
    }
}
