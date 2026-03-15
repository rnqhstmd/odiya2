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

    // DeparturePlace
    case getDeparturePlaces
    case createDeparturePlace
    case updateDeparturePlace(id: Int64)
    case deleteDeparturePlace(id: Int64)

    // UserSettings
    case getUserSettings
    case updateUserSettings

    // Tag
    case getTags
    case createTag
    case updateTag(id: Int64)
    case deleteTag(id: Int64)

    var path: String {
        switch self {
        case .kakaoLogin:       return "/api/v1/auth/kakao/login"
        case .refreshToken:     return "/api/v1/auth/token/refresh"
        case .logout:           return "/api/v1/auth/logout"
        case .getMyProfile:     return "/api/v1/users/me"
        case .updateNickname:   return "/api/v1/users/me/nickname"
        case .updateProfileImage: return "/api/v1/users/me/profile-image"
        case .deleteAccount:    return "/api/v1/users/me"
        case .getDeparturePlaces:       return "/api/v1/departure-places"
        case .createDeparturePlace:     return "/api/v1/departure-places"
        case .updateDeparturePlace(let id): return "/api/v1/departure-places/\(id)"
        case .deleteDeparturePlace(let id): return "/api/v1/departure-places/\(id)"
        case .getUserSettings:          return "/api/v1/users/me/settings"
        case .updateUserSettings:       return "/api/v1/users/me/settings"
        case .getTags:          return "/api/v1/tags"
        case .createTag:        return "/api/v1/tags"
        case .updateTag(let id): return "/api/v1/tags/\(id)"
        case .deleteTag(let id): return "/api/v1/tags/\(id)"
        }
    }

    var method: HTTPMethod {
        switch self {
        case .kakaoLogin, .refreshToken, .logout:
            return .post
        case .getMyProfile:
            return .get
        case .updateNickname, .updateProfileImage:
            return .patch
        case .deleteAccount:
            return .delete
        case .getDeparturePlaces:
            return .get
        case .createDeparturePlace:
            return .post
        case .updateDeparturePlace:
            return .patch
        case .deleteDeparturePlace:
            return .delete
        case .getUserSettings:
            return .get
        case .updateUserSettings:
            return .patch
        case .getTags:
            return .get
        case .createTag:
            return .post
        case .updateTag:
            return .patch
        case .deleteTag:
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
