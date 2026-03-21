import Foundation

enum AppEnvironment: String {
    case local
    case dev
    case prd

    var baseURL: String {
        switch self {
        case .local:
            return "http://localhost:8080"
        case .dev:
            return "https://dev-api.odiya.com"
        case .prd:
            return "https://api.odiya.com"
        }
    }

    var kakaoAppKey: String {
        switch self {
        case .local, .dev:
            return "ece6b8787b49709a2c667866573791a7"
        case .prd:
            return "YOUR_KAKAO_PRD_APP_KEY"
        }
    }

    static var current: AppEnvironment {
        #if DEBUG
        return .local
        #else
        return .prd
        #endif
    }
}
