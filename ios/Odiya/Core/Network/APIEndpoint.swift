import Foundation

enum HTTPMethod: String {
    case get = "GET"
    case post = "POST"
    case patch = "PATCH"
    case delete = "DELETE"
}

enum APIEndpoint {
    // Auth
    case kakaoLogin
    case refreshToken
    case logout

    // User
    case getMyProfile
    case updateNickname
    case updateProfileImage
    case deleteAccount

    // Calendar
    case getCalendarData(year: Int, month: Int)

    var path: String {
        switch self {
        case .kakaoLogin:       return "/api/v1/auth/kakao/login"
        case .refreshToken:     return "/api/v1/auth/token/refresh"
        case .logout:           return "/api/v1/auth/logout"
        case .getMyProfile:     return "/api/v1/users/me"
        case .updateNickname:   return "/api/v1/users/me/nickname"
        case .updateProfileImage: return "/api/v1/users/me/profile-image"
        case .deleteAccount:    return "/api/v1/users/me"
        case .getCalendarData(let year, let month):
            return "/api/v1/appointments/calendar?year=\(year)&month=\(month)"
        }
    }

    var method: HTTPMethod {
        switch self {
        case .kakaoLogin, .refreshToken, .logout:
            return .post
        case .getMyProfile, .getCalendarData:
            return .get
        case .updateNickname, .updateProfileImage:
            return .patch
        case .deleteAccount:
            return .delete
        }
    }

    var requiresAuth: Bool {
        switch self {
        case .kakaoLogin, .refreshToken:
            return false
        default:
            return true
        }
    }
}
