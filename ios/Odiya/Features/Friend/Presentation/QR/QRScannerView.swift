import SwiftUI
import AVFoundation

struct QRScannerView: View {

    @StateObject private var viewModel = QRScannerViewModel()
    @Environment(\.dismiss) private var dismiss

    var myUserId: Int64?

    var body: some View {
        NavigationStack {
            ZStack {
                // Camera preview
                CameraPreview(session: viewModel.captureSession)
                    .ignoresSafeArea()

                // Scan guide overlay
                scanGuideOverlay
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button {
                        dismiss()
                    } label: {
                        Image(systemName: "xmark")
                            .font(.body.weight(.semibold))
                            .foregroundStyle(.white)
                            .padding(8)
                            .background(.ultraThinMaterial)
                            .clipShape(Circle())
                    }
                }
            }
            .task {
                viewModel.configure(myUserId: myUserId)
                await viewModel.startScanning()
            }
            .onDisappear {
                viewModel.stopScanning()
            }
            .alert("알림", isPresented: $viewModel.showAlert) {
                Button("확인", role: .cancel) {
                    if viewModel.shouldDismissOnAlert {
                        dismiss()
                    } else {
                        viewModel.resumeScanning()
                    }
                }
            } message: {
                Text(viewModel.alertMessage)
            }
            .alert("카메라 권한 필요", isPresented: $viewModel.showPermissionAlert) {
                Button("설정으로 이동") {
                    if let url = URL(string: UIApplication.openSettingsURLString) {
                        UIApplication.shared.open(url)
                    }
                }
                Button("취소", role: .cancel) {
                    dismiss()
                }
            } message: {
                Text("QR 코드를 스캔하려면 카메라 권한이 필요합니다.\n설정에서 카메라 접근을 허용해주세요.")
            }
        }
    }

    // MARK: - Scan Guide Overlay

    private var scanGuideOverlay: some View {
        GeometryReader { geometry in
            let size = min(geometry.size.width, geometry.size.height) * 0.65
            let rect = CGRect(
                x: (geometry.size.width - size) / 2,
                y: (geometry.size.height - size) / 2 - 40,
                width: size,
                height: size
            )

            ZStack {
                // Dimmed background
                Color.black.opacity(0.5)
                    .ignoresSafeArea()

                // Clear center cutout
                Rectangle()
                    .frame(width: rect.width, height: rect.height)
                    .position(x: rect.midX, y: rect.midY)
                    .blendMode(.destinationOut)
            }
            .compositingGroup()

            // Corner brackets
            ScanCornerBrackets(rect: rect)

            // Guide text
            VStack {
                Spacer()
                Text("QR 코드를 사각형 안에 맞춰주세요")
                    .font(.subheadline)
                    .foregroundStyle(.white)
                    .padding(.horizontal, 24)
                    .padding(.vertical, 12)
                    .background(.ultraThinMaterial)
                    .clipShape(Capsule())
                    .padding(.bottom, 80)
            }
            .frame(maxWidth: .infinity)
        }
    }
}

// MARK: - Camera Preview (UIViewRepresentable)

struct CameraPreview: UIViewRepresentable {

    let session: AVCaptureSession

    func makeUIView(context: Context) -> UIView {
        let view = UIView(frame: .zero)
        let previewLayer = AVCaptureVideoPreviewLayer(session: session)
        previewLayer.videoGravity = .resizeAspectFill
        view.layer.addSublayer(previewLayer)
        context.coordinator.previewLayer = previewLayer
        return view
    }

    func updateUIView(_ uiView: UIView, context: Context) {
        DispatchQueue.main.async {
            context.coordinator.previewLayer?.frame = uiView.bounds
        }
    }

    func makeCoordinator() -> Coordinator { Coordinator() }

    final class Coordinator {
        var previewLayer: AVCaptureVideoPreviewLayer?
    }
}

// MARK: - Scan Corner Brackets

private struct ScanCornerBrackets: View {

    let rect: CGRect
    private let lineLength: CGFloat = 24
    private let lineWidth: CGFloat = 3

    var body: some View {
        ZStack {
            // Top-left
            cornerBracket(x: rect.minX, y: rect.minY, dx: 1, dy: 1)
            // Top-right
            cornerBracket(x: rect.maxX, y: rect.minY, dx: -1, dy: 1)
            // Bottom-left
            cornerBracket(x: rect.minX, y: rect.maxY, dx: 1, dy: -1)
            // Bottom-right
            cornerBracket(x: rect.maxX, y: rect.maxY, dx: -1, dy: -1)
        }
    }

    private func cornerBracket(x: CGFloat, y: CGFloat, dx: CGFloat, dy: CGFloat) -> some View {
        Path { path in
            path.move(to: CGPoint(x: x, y: y + dy * lineLength))
            path.addLine(to: CGPoint(x: x, y: y))
            path.addLine(to: CGPoint(x: x + dx * lineLength, y: y))
        }
        .stroke(Color.white, style: StrokeStyle(lineWidth: lineWidth, lineCap: .round, lineJoin: .round))
    }
}
