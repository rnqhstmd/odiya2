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

    init(repository: FriendRepository = FriendRepositoryImpl()) {
        self.repository = repository
    }

    func configure(myUserId: Int64?) {
        self.myUserId = myUserId
    }

    // MARK: - Scanning

    func startScanning() async {
        // Check camera permission
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            setupCaptureSession()
        case .notDetermined:
            let granted = await AVCaptureDevice.requestAccess(for: .video)
            if granted {
                setupCaptureSession()
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
        guard captureSession.isRunning else { return }
        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            self?.captureSession.stopRunning()
        }
    }

    // MARK: - Private

    private func setupCaptureSession() {
        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            guard let self,
                  let device = AVCaptureDevice.default(for: .video),
                  let input = try? AVCaptureDeviceInput(device: device) else {
                Task { @MainActor [weak self] in
                    self?.alertMessage = "카메라를 사용할 수 없습니다."
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
            self.captureSession.startRunning()
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
            isProcessing = false
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
            isProcessing = false
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

            guard let scannedUserId = self.parseQRCode(stringValue) else {
                self.alertMessage = "유효하지 않은 QR 코드입니다."
                self.shouldDismissOnAlert = false
                self.showAlert = true
                self.isProcessing = false
                return
            }

            self.stopScanning()
            self.handleScannedUserId(scannedUserId)
        }
    }
}
