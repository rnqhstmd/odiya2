import SwiftUI

struct LoginView: View {

    @EnvironmentObject var router: RootRouter
    @StateObject private var viewModel = LoginViewModel()

    var body: some View {
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

            if viewModel.isLoading {
                ProgressView()
            }

            if let error = viewModel.errorMessage {
                Text(error)
                    .font(.footnote)
                    .foregroundStyle(.red)
                    .multilineTextAlignment(.center)
            }
        }
        .padding(.horizontal, 24)
        .padding(.bottom, 48)
        .onAppear {
            viewModel.onLoginSuccess = { router.navigateToMain() }
        }
    }
}

#Preview {
    LoginView()
        .environmentObject(RootRouter())
}
