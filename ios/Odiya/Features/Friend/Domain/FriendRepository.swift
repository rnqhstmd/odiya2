import Foundation

protocol FriendRepository {
    func getFriends(tagId: Int64?) async throws -> [FriendResponseDTO]
    func sendFriendRequest(targetUserId: Int64) async throws
    func getReceivedRequests() async throws -> [FriendRequestResponseDTO]
    func acceptFriendRequest(requestId: Int64) async throws
    func rejectFriendRequest(requestId: Int64) async throws
    func removeFriend(friendUserId: Int64) async throws
    func changeFriendTag(friendUserId: Int64, tagId: Int64) async throws

    func getTags() async throws -> [TagResponseDTO]
    func createTag(name: String, colorHex: String) async throws -> TagResponseDTO
    func updateTag(tagId: Int64, name: String?, colorHex: String?) async throws -> TagResponseDTO
    func deleteTag(tagId: Int64) async throws

    func searchUsers(nickname: String) async throws -> [UserSearchResponseDTO]
}
