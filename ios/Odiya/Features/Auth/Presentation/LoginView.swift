import SwiftUI

struct LoginView: View {

    @StateObject private var viewModel = LoginViewModel()

    var body: some View {
        ZStack {
            // 배경 그라데이션
            LinearGradient(
                colors: [
                    Color(hex: 0xF8F3FC),
                    Color(hex: 0xE8D5F5),
                    Color(hex: 0xF0E6F7)
                ],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            VStack(spacing: 32) {
                Spacer()

                // 앱 로고 영역
                VStack(spacing: 12) {
                    Image(systemName: "leaf.circle.fill")
                        .resizable()
                        .frame(width: 80, height: 80)
                        .foregroundStyle(OdiyaColors.primary)

                    Text("오디야")
                        .font(.largeTitle)
                        .fontWeight(.bold)
                        .foregroundStyle(OdiyaColors.odiya900)
                }

                Spacer()

                VStack(spacing: 12) {
                    // 카카오 로그인 버튼
                    Button(action: viewModel.loginWithKakao) {
                        HStack(spacing: 8) {
                            Image(systemName: "message.fill")
                            Text("카카오로 시작하기")
                                .fontWeight(.semibold)
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 52)
                        .background(OdiyaColors.kakaoYellow)
                        .foregroundStyle(.black)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                    }
                    .disabled(viewModel.isLoading)

                    // Apple 로그인 버튼
                    Button(action: viewModel.loginWithApple) {
                        HStack(spacing: 8) {
                            Image(systemName: "apple.logo")
                            Text("Apple로 시작하기")
                                .fontWeight(.semibold)
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 52)
                        .background(.black)
                        .foregroundStyle(.white)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                    }
                    .disabled(viewModel.isLoading)
                }

                if viewModel.isLoading {
                    ProgressView()
                }

                if let error = viewModel.errorMessage {
                    Text(error)
                        .font(.footnote)
                        .foregroundStyle(.red)
                        .multilineTextAlignment(.center)
                }

                // 이용약관 안내
                Text("시작하면 이용약관 및 개인정보 처리방침에 동의하는 것으로 간주됩니다.")
                    .font(.caption2)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 48)
        }
    }
}

#Preview {
    LoginView()
}
