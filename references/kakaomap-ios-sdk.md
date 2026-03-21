# 카카오맵 iOS SDK v2

> 공식 문서: https://apis.map.kakao.com/ios_v2/
> SPM: https://github.com/kakao-mapsSDK/KakaoMapsSDK-SPM

> 구 SDK는 2024년 8월 31일 이후 완전 종료. **반드시 v2 SDK 사용**.

## 구현 체크리스트

### SDK 설치 및 초기화
- [ ] SPM 패키지: `https://github.com/kakao-mapsSDK/KakaoMapsSDK-SPM`
- [ ] `SDKInitializer.InitSDK("YOUR_KAKAO_APP_KEY")` — 앱 시작 시 호출
- [ ] `KMViewContainer`를 Storyboard Custom Class로 설정하거나 코드로 생성

### 엔진 라이프사이클
- [ ] `viewDidLoad` → `mapController?.prepareEngine()` (인증 시작)
- [ ] `viewWillAppear` → `mapController?.activateEngine()` (인증 성공 후)
- [ ] `viewWillDisappear` → `mapController?.pauseEngine()`
- [ ] `viewDidDisappear` → `mapController?.resetEngine()`
- [ ] `authenticationSucceeded()` 콜백에서 `_auth = true` 설정
- [ ] `authenticationFailed()` 콜백에서 `prepareEngine()` 재시도

### 지도 뷰 생성
- [ ] `MapviewInfo`에 `defaultPosition` (MapPoint: longitude, latitude), `defaultLevel` 설정
- [ ] `mapController?.addView(mapviewInfo)` — `addViews()` 콜백 내에서 호출
- [ ] 좌표 순서: `MapPoint(longitude:, latitude:)` — **경도, 위도** 순서

### 마커 (Poi)
- [ ] `LabelManager` → `addLabelLayer()` → `PoiStyle` 정의 → `addPoi()` → `show()`
- [ ] 마커 이미지: `PoiIconStyle(symbol: UIImage)`
- [ ] 마커 탭 이벤트 처리

### 폴리라인 (RouteLine)
- [ ] `ShapeManager` → `addRouteLayer()` → `RouteStyleSet` 정의 → `addRoute()` → `show()`
- [ ] `RouteSegment`에 `MapPoint` 배열 전달
- [ ] `PerLevelRouteStyle`로 너비, 색상, 외곽선 설정

### 에러 코드
- [ ] 401: 인증 실패 (앱 키 확인)
- [ ] 429: 쿼터 초과
- [ ] 499: 네트워크 오류 → 재시도 필요

### 쿼터
- [ ] 무료 일일 한도: 300,000건/일
- [ ] 월 공유 한도: 3,000,000건/월

### 코드 참조 — 기본 지도 뷰

```swift
class MapViewController: UIViewController, MapControllerDelegate {
    var mapContainer: KMViewContainer?
    var mapController: KMController?
    var _auth = false

    override func viewDidLoad() {
        super.viewDidLoad()
        mapContainer = self.view as? KMViewContainer
        mapController = KMController(viewContainer: mapContainer!)
        mapController?.delegate = self
        mapController?.prepareEngine()
    }

    func authenticationSucceeded() { _auth = true }
    func authenticationFailed(_ errorCode: Int, desc: String) {
        mapController?.prepareEngine()
    }

    func addViews() {
        let info = MapviewInfo(
            viewName: "mapview", viewInfoName: "map",
            defaultPosition: MapPoint(longitude: 127.108678, latitude: 37.402001),
            defaultLevel: 7
        )
        mapController?.addView(info)
    }
}
```
