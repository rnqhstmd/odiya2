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
    case updateTag(tagId: Int64)
    case deleteTag(tagId: Int64)

    var path: String {
        switch self {
        case .kakaoLogin:       return "/api/v1/auth/kakao/login"
        case .refreshToken:     return "/api/v1/auth/token/refresh"
        case .logout:           return "/api/v1/auth/logout"
        case .getMyProfile:     return "/api/v1/users/me"
        case .updateNickname:   return "/api/v1/users/me/nickname"
        case .updateProfileImage: return "/api/v1/users/me/profile-image"
        case .deleteAccount:    return "/api/v1/users/me"
        case .searchUsers(let nickname):
            return "/api/v1/users/search?nickname=\(nickname.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? nickname)"
        case .getFriends(let tagId):
            if let tagId { return "/api/v1/friends?tagId=\(tagId)" }
            return "/api/v1/friends"
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
        }
    }

    var method: HTTPMethod {
        switch self {
        case .kakaoLogin, .refreshToken, .logout,
             .sendFriendRequest, .acceptFriendRequest, .rejectFriendRequest,
             .createTag:
            return .post
        case .getMyProfile, .getFriends, .getReceivedRequests, .getTags, .searchUsers:
            return .get
        case .updateNickname, .updateProfileImage, .changeFriendTag, .updateTag:
            return .patch
        case .deleteAccount, .removeFriend, .deleteTag:
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
