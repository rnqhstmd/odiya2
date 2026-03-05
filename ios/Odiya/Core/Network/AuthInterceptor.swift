import Foundation

/// 401 토큰 갱신 인터셉터 (동시 요청 처리)
actor AuthInterceptor {

    static let shared = AuthInterceptor()

    private var isRefreshing = false
    private var pendingContinuations: [CheckedContinuation<URLRequest, Error>] = []

    private init() {}

    func applyToken(to request: URLRequest) async throws -> URLRequest {
        var mutableRequest = request
        if let token = await TokenManager.shared.accessToken {
            mutableRequest.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        return mutableRequest
    }

    func handleUnauthorized(original request: URLRequest) async throws -> URLRequest {
        if isRefreshing {
            return try await withCheckedThrowingContinuation { continuation in
                pendingContinuations.append(continuation)
            }
        }

        isRefreshing = true

        do {
            try await refreshTokens()
            let newRequest = try await applyToken(to: request)

            for continuation in pendingContinuations {
                do {
                    let updated = try await applyToken(to: request)
                    continuation.resume(returning: updated)
                } catch {
                    continuation.resume(throwing: error)
                }
            }
            pendingContinuations.removeAll()
            isRefreshing = false

            return newRequest
        } catch {
            for continuation in pendingContinuations {
                continuation.resume(throwing: error)
            }
            pendingContinuations.removeAll()
            isRefreshing = false

            await TokenManager.shared.clearTokens()
            throw APIError.unauthorized("세션이 만료되었습니다. 다시 로그인해주세요.")
        }
    }

    private func refreshTokens() async throws {
        guard let currentRefreshToken = await TokenManager.shared.refreshToken else {
            throw APIError.unauthorized("리프레시 토큰이 없습니다.")
        }

        let body = RefreshTokenRequest(refreshToken: currentRefreshToken)

        guard let url = URL(string: AppEnvironment.current.baseURL + APIEndpoint.refreshToken.path) else {
            throw APIError.unknown("잘못된 URL입니다.")
        }

        var request = URLRequest(url: url)
        request.httpMethod = HTTPMethod.post.rawValue
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try JSONEncoder().encode(body)

        let (data, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse,
              httpResponse.statusCode == 200 else {
            throw APIError.unauthorized("토큰 갱신에 실패했습니다.")
        }

        let decoder = JSONDecoder()
        let apiResponse = try decoder.decode(APIResponse<TokenResponseDTO>.self, from: data)

        guard apiResponse.isSuccess, let tokenData = apiResponse.data else {
            throw APIError.unauthorized("토큰 갱신에 실패했습니다.")
        }

        try await TokenManager.shared.saveTokens(
            accessToken: tokenData.accessToken,
            refreshToken: tokenData.refreshToken
        )
    }
}
