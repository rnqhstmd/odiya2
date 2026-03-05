import SwiftUI

@MainActor
final class RootRouter: ObservableObject {

    enum Route {
        case splash
        case login
        case main
    }

    @Published var currentRoute: Route = .splash

    private let tokenManager: TokenManager

    init(tokenManager: TokenManager = .shared) {
        self.tokenManager = tokenManager
    }

    func checkAuthState() {
        Task {
            let isLoggedIn = await tokenManager.isLoggedIn
            currentRoute = isLoggedIn ? .main : .login
        }
    }

    func navigateToLogin() {
        currentRoute = .login
    }

    func navigateToMain() {
        currentRoute = .main
    }
}

struct RootRouterView: View {

    @StateObject private var router = RootRouter()

    var body: some View {
        Group {
            switch router.currentRoute {
            case .splash:
                ProgressView()
                    .task { router.checkAuthState() }

            case .login:
                LoginView()

            case .main:
                MainTabView()
            }
        }
        .environmentObject(router)
    }
}
