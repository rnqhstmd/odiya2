import Foundation

final class FriendRepositoryImpl: FriendRepository {

    private let apiClient: APIClient

    init(apiClient: APIClient = .shared) {
        self.apiClient = apiClient
    }

    // MARK: - Friend

    func getFriends(tagId: Int64?) async throws -> [FriendResponseDTO] {
        return try await apiClient.request(
            endpoint: .getFriends(tagId: tagId),
            responseType: [FriendResponseDTO].self
        )
    }

    func sendFriendRequest(targetUserId: Int64) async throws {
        let body = SendFriendRequestBody(targetUserId: targetUserId)
        try await apiClient.requestVoid(endpoint: .sendFriendRequest, body: body)
    }

    func getReceivedRequests() async throws -> [FriendRequestResponseDTO] {
        return try await apiClient.request(
            endpoint: .getReceivedRequests,
            responseType: [FriendRequestResponseDTO].self
        )
    }

    func acceptFriendRequest(requestId: Int64) async throws {
        try await apiClient.requestVoid(endpoint: .acceptFriendRequest(requestId: requestId))
    }

    func rejectFriendRequest(requestId: Int64) async throws {
        try await apiClient.requestVoid(endpoint: .rejectFriendRequest(requestId: requestId))
    }

    func removeFriend(friendUserId: Int64) async throws {
        try await apiClient.requestVoid(endpoint: .removeFriend(friendUserId: friendUserId))
    }

    func changeFriendTag(friendUserId: Int64, tagId: Int64) async throws {
        let body = ChangeFriendTagBody(tagId: tagId)
        try await apiClient.requestVoid(endpoint: .changeFriendTag(friendUserId: friendUserId), body: body)
    }

    // MARK: - Tag

    func getTags() async throws -> [TagResponseDTO] {
        return try await apiClient.request(
            endpoint: .getTags,
            responseType: [TagResponseDTO].self
        )
    }

    func createTag(name: String, colorHex: String) async throws -> TagResponseDTO {
        let body = CreateTagBody(name: name, color: colorHex)
        return try await apiClient.request(
            endpoint: .createTag,
            body: body,
            responseType: TagResponseDTO.self
        )
    }

    func updateTag(tagId: Int64, name: String?, colorHex: String?) async throws -> TagResponseDTO {
        let body = UpdateTagBody(name: name, color: colorHex)
        return try await apiClient.request(
            endpoint: .updateTag(tagId: tagId),
            body: body,
            responseType: TagResponseDTO.self
        )
    }

    func deleteTag(tagId: Int64) async throws {
        try await apiClient.requestVoid(endpoint: .deleteTag(tagId: tagId))
    }

    // MARK: - User Search

    func searchUsers(nickname: String) async throws -> [UserSearchResponseDTO] {
        return try await apiClient.request(
            endpoint: .searchUsers(nickname: nickname),
            responseType: [UserSearchResponseDTO].self
        )
    }
}
