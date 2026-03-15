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

    // Appointment
    case createAppointment
    case getAppointment(id: Int64)
    case getMyAppointments(status: String, cursor: Int64?, size: Int)
    case getCalendarData(year: Int, month: Int)
    case updateAppointment(id: Int64)
    case cancelAppointment(id: Int64)
    case acceptInvitation(id: Int64)
    case rejectInvitation(id: Int64)
    case inviteParticipants(id: Int64)
    case updateDeparture(id: Int64)
    case nudge(id: Int64)

    // Place
    case searchPlaces(keyword: String, page: Int)

    var path: String {
        switch self {
        case .kakaoLogin:       return "/api/v1/auth/kakao/login"
        case .refreshToken:     return "/api/v1/auth/token/refresh"
        case .logout:           return "/api/v1/auth/logout"
        case .getMyProfile:     return "/api/v1/users/me"
        case .updateNickname:   return "/api/v1/users/me/nickname"
        case .updateProfileImage: return "/api/v1/users/me/profile-image"
        case .deleteAccount:    return "/api/v1/users/me"

        case .createAppointment:        return "/api/v1/appointments"
        case .getAppointment(let id):   return "/api/v1/appointments/\(id)"
        case .getMyAppointments:        return "/api/v1/appointments/me"
        case .getCalendarData:          return "/api/v1/appointments/calendar"
        case .updateAppointment(let id): return "/api/v1/appointments/\(id)"
        case .cancelAppointment(let id): return "/api/v1/appointments/\(id)"
        case .acceptInvitation(let id): return "/api/v1/appointments/\(id)/accept"
        case .rejectInvitation(let id): return "/api/v1/appointments/\(id)/reject"
        case .inviteParticipants(let id): return "/api/v1/appointments/\(id)/invite"
        case .updateDeparture(let id):  return "/api/v1/appointments/\(id)/departure"
        case .nudge(let id):            return "/api/v1/appointments/\(id)/nudge"

        case .searchPlaces:             return "/api/v1/places/search"
        }
    }

    var queryItems: [URLQueryItem]? {
        switch self {
        case .getMyAppointments(let status, let cursor, let size):
            var items: [URLQueryItem] = [
                URLQueryItem(name: "status", value: status),
                URLQueryItem(name: "size", value: "\(size)")
            ]
            if let cursor = cursor {
                items.append(URLQueryItem(name: "cursor", value: "\(cursor)"))
            }
            return items
        case .getCalendarData(let year, let month):
            return [
                URLQueryItem(name: "year", value: "\(year)"),
                URLQueryItem(name: "month", value: "\(month)")
            ]
        case .searchPlaces(let keyword, let page):
            return [
                URLQueryItem(name: "keyword", value: keyword),
                URLQueryItem(name: "page", value: "\(page)")
            ]
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

        case .createAppointment:        return .post
        case .getAppointment:           return .get
        case .getMyAppointments:        return .get
        case .getCalendarData:          return .get
        case .updateAppointment:        return .patch
        case .cancelAppointment:        return .delete
        case .acceptInvitation:         return .post
        case .rejectInvitation:         return .post
        case .inviteParticipants:       return .post
        case .updateDeparture:          return .patch
        case .nudge:                    return .post

        case .searchPlaces:             return .get
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
