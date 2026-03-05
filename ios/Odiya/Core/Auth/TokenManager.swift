import Foundation

actor TokenManager {

    static let shared = TokenManager()

    private let service = "com.odiya.token"
    private let accessTokenKey = "accessToken"
    private let refreshTokenKey = "refreshToken"

    private init() {}

    // MARK: - Access Token

    var accessToken: String? {
        guard let data = KeychainHelper.load(service: service, account: accessTokenKey) else {
            return nil
        }
        return String(data: data, encoding: .utf8)
    }

    func saveAccessToken(_ token: String) throws {
        guard let data = token.data(using: .utf8) else { return }
        try KeychainHelper.save(data: data, service: service, account: accessTokenKey)
    }

    // MARK: - Refresh Token

    var refreshToken: String? {
        guard let data = KeychainHelper.load(service: service, account: refreshTokenKey) else {
            return nil
        }
        return String(data: data, encoding: .utf8)
    }

    func saveRefreshToken(_ token: String) throws {
        guard let data = token.data(using: .utf8) else { return }
        try KeychainHelper.save(data: data, service: service, account: refreshTokenKey)
    }

    // MARK: - Token Pair

    func saveTokens(accessToken: String, refreshToken: String) throws {
        try saveAccessToken(accessToken)
        try saveRefreshToken(refreshToken)
    }

    func clearTokens() {
        KeychainHelper.delete(service: service, account: accessTokenKey)
        KeychainHelper.delete(service: service, account: refreshTokenKey)
    }

    var isLoggedIn: Bool {
        accessToken != nil
    }
}
