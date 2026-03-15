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

    // Notification
    case getNotifications(cursor: Int64?, size: Int)
    case markNotificationAsRead(id: Int64)
    case markAllNotificationsAsRead
    case getUnreadCount

    // Device
    case registerDevice
    case deactivateDevice(token: String)

    var path: String {
        switch self {
        case .kakaoLogin:       return "/api/v1/auth/kakao/login"
        case .refreshToken:     return "/api/v1/auth/token/refresh"
        case .logout:           return "/api/v1/auth/logout"
        case .getMyProfile:     return "/api/v1/users/me"
        case .updateNickname:   return "/api/v1/users/me/nickname"
        case .updateProfileImage: return "/api/v1/users/me/profile-image"
        case .deleteAccount:    return "/api/v1/users/me"
        case .getNotifications: return "/api/v1/notifications"
        case .markNotificationAsRead(let id): return "/api/v1/notifications/\(id)/read"
        case .markAllNotificationsAsRead: return "/api/v1/notifications/read-all"
        case .getUnreadCount:   return "/api/v1/notifications/unread-count"
        case .registerDevice:   return "/api/v1/devices"
        case .deactivateDevice(let token): return "/api/v1/devices/\(token)"
        }
    }

    var queryItems: [URLQueryItem]? {
        switch self {
        case .getNotifications(let cursor, let size):
            var items: [URLQueryItem] = [URLQueryItem(name: "size", value: "\(size)")]
            if let cursor {
                items.append(URLQueryItem(name: "cursor", value: "\(cursor)"))
            }
            return items
        default:
            return nil
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
        case .getNotifications, .getUnreadCount:
            return .get
        case .markNotificationAsRead:
            return .patch
        case .markAllNotificationsAsRead, .registerDevice:
            return .post
        case .deactivateDevice:
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
