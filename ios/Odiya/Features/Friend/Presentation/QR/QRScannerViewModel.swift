import AVFoundation
import SwiftUI

@MainActor
final class QRScannerViewModel: NSObject, ObservableObject {

    @Published var showAlert = false
    @Published var alertMessage = ""
    @Published var showPermissionAlert = false
    @Published var shouldDismissOnAlert = false
    @Published var isProcessing = false

    let captureSession = AVCaptureSession()

    // MARK: - Dependencies

    private let repository: FriendRepository
    private var myUserId: Int64?
    private var isConfigured = false
    // 캡처 세션 관련 호출(configure/startRunning/stopRunning)을 순차 실행하여 race 방지.
    private let sessionQueue = DispatchQueue(label: "com.odiya.qrscanner.session")

    init(repository: FriendRepository = FriendRepositoryImpl()) {
        self.repository = repository
    }

    func configure(myUserId: Int64?) {
        self.myUserId = myUserId
    }

    // MARK: - Scanning

    func startScanning() async {
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            configureAndStart()
        case .notDetermined:
            let granted = await AVCaptureDevice.requestAccess(for: .video)
            if granted {
                configureAndStart()
            } else {
                showPermissionAlert = true
            }
        case .denied, .restricted:
            showPermissionAlert = true
        @unknown default:
            showPermissionAlert = true
        }
    }

    func stopScanning() {
        sessionQueue.async { [weak self] in
            guard let self, self.captureSession.isRunning else { return }
            self.captureSession.stopRunning()
        }
    }

    /// 복구 가능한 알림(자기 QR, 유효하지 않은 QR, 친구 요청 실패)을 닫은 뒤 다시 스캔을 재개한다.
    func resumeScanning() {
        isProcessing = false
        sessionQueue.async { [weak self] in
            guard let self, !self.captureSession.isRunning else { return }
            self.captureSession.startRunning()
        }
    }

    // MARK: - Private

    /// MainActor에서 호출. 최초 1회만 session 구성하고, 이후에는 startRunning만 호출.
    private func configureAndStart() {
        let needsConfiguration = !isConfigured
        if needsConfiguration {
            // 구성 시작 전에 플래그를 선점하여 중복 구성 방지 (실패 시 rollback).
            isConfigured = true
        }

        sessionQueue.async { [weak self] in
            guard let self else { return }

            if needsConfiguration {
                guard let device = AVCaptureDevice.default(for: .video),
                      let input = try? AVCaptureDeviceInput(device: device) else {
                    Task { @MainActor [weak self] in
                        self?.isConfigured = false
                        self?.alertMessage = "카메라를 사용할 수 없습니다."
                        self?.shouldDismissOnAlert = true
                        self?.showAlert = true
                    }
                    return
                }

                let metadataOutput = AVCaptureMetadataOutput()

                self.captureSession.beginConfiguration()

                if self.captureSession.canAddInput(input) {
                    self.captureSession.addInput(input)
                }

                if self.captureSession.canAddOutput(metadataOutput) {
                    self.captureSession.addOutput(metadataOutput)
                    metadataOutput.setMetadataObjectsDelegate(self, queue: .main)
                    metadataOutput.metadataObjectTypes = [.qr]
                }

                self.captureSession.commitConfiguration()
            }

            if !self.captureSession.isRunning {
                self.captureSession.startRunning()
            }
        }
    }

    private func parseQRCode(_ value: String) -> Int64? {
        guard let url = URLComponents(string: value),
              url.scheme == "odiya",
              url.host == "friend",
              let userIdString = url.queryItems?.first(where: { $0.name == "userId" })?.value,
              let userId = Int64(userIdString) else {
            return nil
        }
        return userId
    }

    private func handleScannedUserId(_ scannedUserId: Int64) {
        // Self check
        if let myUserId, myUserId == scannedUserId {
            alertMessage = "자기 자신에게는 친구 요청을 보낼 수 없습니다."
            shouldDismissOnAlert = false
            showAlert = true
            return
        }

        Task {
            do {
                try await repository.sendFriendRequest(targetUserId: scannedUserId)
                alertMessage = "친구 요청을 보냈습니다."
                shouldDismissOnAlert = true
                showAlert = true
            } catch let error as APIError {
                alertMessage = error.userMessage
                shouldDismissOnAlert = false
                showAlert = true
            } catch {
                alertMessage = "네트워크 오류가 발생했습니다. 다시 시도해주세요."
                shouldDismissOnAlert = false
                showAlert = true
            }
        }
    }

}

// MARK: - AVCaptureMetadataOutputObjectsDelegate

extension QRScannerViewModel: AVCaptureMetadataOutputObjectsDelegate {

    nonisolated func metadataOutput(
        _ output: AVCaptureMetadataOutput,
        didOutput metadataObjects: [AVMetadataObject],
        from connection: AVCaptureConnection
    ) {
        guard let metadataObject = metadataObjects.first as? AVMetadataMachineReadableCodeObject,
              metadataObject.type == .qr,
              let stringValue = metadataObject.stringValue else {
            return
        }

        Task { @MainActor [weak self] in
            guard let self, !self.isProcessing else { return }
            self.isProcessing = true
            // 알림/처리 도중 동일 QR의 delegate 재진입을 막기 위해 즉시 세션 정지.
            self.stopScanning()

            guard let scannedUserId = self.parseQRCode(stringValue) else {
                self.alertMessage = "유효하지 않은 QR 코드입니다."
                self.shouldDismissOnAlert = false
                self.showAlert = true
                return
            }

            self.handleScannedUserId(scannedUserId)
        }
    }
}
