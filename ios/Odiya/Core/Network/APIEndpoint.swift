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
    case searchUsers(nickname: String)

    // Friend
    case getFriends(tagId: Int64?)
    case sendFriendRequest
    case getReceivedRequests
    case acceptFriendRequest(requestId: Int64)
    case rejectFriendRequest(requestId: Int64)
    case removeFriend(friendUserId: Int64)
    case changeFriendTag(friendUserId: Int64)

    // Tag
    case getTags
    case createTag
    case updateTag(id: Int64)
    case deleteTag(id: Int64)

    // Appointment
    case createAppointment
    case getAppointment(id: Int64)
    case getMyAppointments(status: String, cursor: Int64?, size: Int)
    case updateAppointment(id: Int64)
    case cancelAppointment(id: Int64)
    case acceptInvitation(id: Int64)
    case rejectInvitation(id: Int64)
    case inviteParticipants(id: Int64)
    case updateDeparture(id: Int64)
    case nudge(id: Int64)

    // Place
    case searchPlaces(keyword: String, page: Int)

    // Notification
    case getNotifications(cursor: Int64?, size: Int)
    case markNotificationAsRead(id: Int64)
    case markAllNotificationsAsRead
    case getUnreadCount

    // Device
    case registerDevice
    case deactivateDevice(token: String)

    // DeparturePlace
    case getDeparturePlaces
    case createDeparturePlace
    case updateDeparturePlace(id: Int64)
    case deleteDeparturePlace(id: Int64)

    // UserSettings
    case getUserSettings
    case updateUserSettings

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
        case .searchUsers:      return "/api/v1/users/search"
        case .getFriends:       return "/api/v1/friends"
        case .sendFriendRequest:            return "/api/v1/friends/request"
        case .getReceivedRequests:          return "/api/v1/friends/requests/received"
        case .acceptFriendRequest(let id):  return "/api/v1/friends/request/\(id)/accept"
        case .rejectFriendRequest(let id):  return "/api/v1/friends/request/\(id)/reject"
        case .removeFriend(let id):         return "/api/v1/friends/\(id)"
        case .changeFriendTag(let id):      return "/api/v1/friends/\(id)/tag"
        case .getTags:                      return "/api/v1/tags"
        case .createTag:                    return "/api/v1/tags"
        case .updateTag(let id):            return "/api/v1/tags/\(id)"
        case .deleteTag(let id):            return "/api/v1/tags/\(id)"

        case .createAppointment:        return "/api/v1/appointments"
        case .getAppointment(let id):   return "/api/v1/appointments/\(id)"
        case .getMyAppointments:        return "/api/v1/appointments/me"
        case .updateAppointment(let id): return "/api/v1/appointments/\(id)"
        case .cancelAppointment(let id): return "/api/v1/appointments/\(id)"
        case .acceptInvitation(let id): return "/api/v1/appointments/\(id)/accept"
        case .rejectInvitation(let id): return "/api/v1/appointments/\(id)/reject"
        case .inviteParticipants(let id): return "/api/v1/appointments/\(id)/invite"
        case .updateDeparture(let id):  return "/api/v1/appointments/\(id)/departure"
        case .nudge(let id):            return "/api/v1/appointments/\(id)/nudge"

        case .searchPlaces:             return "/api/v1/places/search"

        case .getNotifications:         return "/api/v1/notifications"
        case .markNotificationAsRead(let id): return "/api/v1/notifications/\(id)/read"
        case .markAllNotificationsAsRead: return "/api/v1/notifications/read-all"
        case .getUnreadCount:           return "/api/v1/notifications/unread-count"
        case .registerDevice:           return "/api/v1/devices"
        case .deactivateDevice(let token): return "/api/v1/devices/\(token)"

        case .getDeparturePlaces:       return "/api/v1/departure-places"
        case .createDeparturePlace:     return "/api/v1/departure-places"
        case .updateDeparturePlace(let id): return "/api/v1/departure-places/\(id)"
        case .deleteDeparturePlace(let id): return "/api/v1/departure-places/\(id)"
        case .getUserSettings:          return "/api/v1/users/me/settings"
        case .updateUserSettings:       return "/api/v1/users/me/settings"

        case .getCalendarData:          return "/api/v1/appointments/calendar"
        }
    }

    var queryItems: [URLQueryItem]? {
        switch self {
        case .searchUsers(let nickname):
            return [URLQueryItem(name: "nickname", value: nickname)]
        case .getFriends(let tagId):
            if let tagId { return [URLQueryItem(name: "tagId", value: "\(tagId)")] }
            return nil
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
        case .kakaoLogin, .refreshToken, .logout,
             .sendFriendRequest, .acceptFriendRequest, .rejectFriendRequest,
             .createTag, .createDeparturePlace:
            return .post
        case .getMyProfile, .getFriends, .getReceivedRequests, .getTags, .searchUsers,
             .getDeparturePlaces, .getUserSettings, .getCalendarData:
            return .get
        case .updateNickname, .updateProfileImage, .changeFriendTag, .updateTag,
             .updateDeparturePlace, .updateUserSettings:
            return .patch
        case .deleteAccount, .removeFriend, .deleteTag, .deleteDeparturePlace:
            return .delete

        case .createAppointment:        return .post
        case .getAppointment:           return .get
        case .getMyAppointments:        return .get
        case .updateAppointment:        return .patch
        case .cancelAppointment:        return .delete
        case .acceptInvitation:         return .post
        case .rejectInvitation:         return .post
        case .inviteParticipants:       return .post
        case .updateDeparture:          return .patch
        case .nudge:                    return .post

        case .searchPlaces:             return .get

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
