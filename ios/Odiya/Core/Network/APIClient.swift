import Foundation

actor APIClient {

    static let shared = APIClient()

    private let session: URLSession
    private let decoder: JSONDecoder
    private let encoder: JSONEncoder

    init(session: URLSession = .shared) {
        self.session = session
        self.decoder = JSONDecoder()
        self.encoder = JSONEncoder()
    }

    // MARK: - Public

    func request<T: Decodable>(
        endpoint: APIEndpoint,
        body: (any Encodable)? = nil,
        responseType: T.Type
    ) async throws -> T {
        let urlRequest = try buildRequest(endpoint: endpoint, body: body)
        return try await execute(urlRequest, responseType: responseType, endpoint: endpoint)
    }

    func requestVoid(
        endpoint: APIEndpoint,
        body: (any Encodable)? = nil
    ) async throws {
        let urlRequest = try buildRequest(endpoint: endpoint, body: body)
        try await executeVoid(urlRequest, endpoint: endpoint)
    }

    // MARK: - Private

    private func buildRequest(
        endpoint: APIEndpoint,
        body: (any Encodable)?
    ) throws -> URLRequest {
        guard var components = URLComponents(string: AppEnvironment.current.baseURL + endpoint.path) else {
            throw APIError.unknown("잘못된 URL입니다.")
        }

        if let queryItems = endpoint.queryItems, !queryItems.isEmpty {
            components.queryItems = queryItems
        }

        guard let url = components.url else {
            throw APIError.unknown("잘못된 URL입니다.")
        }

        var request = URLRequest(url: url)
        request.httpMethod = endpoint.method.rawValue
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        if let body {
            request.httpBody = try encoder.encode(body)
        }

        return request
    }

    private func execute<T: Decodable>(
        _ request: URLRequest,
        responseType: T.Type,
        endpoint: APIEndpoint
    ) async throws -> T {
        var urlRequest = request

        if endpoint.requiresAuth {
            urlRequest = try await AuthInterceptor.shared.applyToken(to: urlRequest)
        }

        #if DEBUG
        urlRequest.logCURL()
        #endif

        let (data, response) = try await performRequest(urlRequest)

        guard let httpResponse = response as? HTTPURLResponse else {
            throw APIError.unknown("응답을 처리할 수 없습니다.")
        }

        #if DEBUG
        data.prettyPrintJSON()
        #endif

        if httpResponse.statusCode == 401 && endpoint.requiresAuth {
            let retryRequest = try await AuthInterceptor.shared.handleUnauthorized(original: urlRequest)
            let (retryData, retryResponse) = try await performRequest(retryRequest)

            guard let retryHTTP = retryResponse as? HTTPURLResponse else {
                throw APIError.unknown("응답을 처리할 수 없습니다.")
            }
            return try decodeResponse(data: retryData, statusCode: retryHTTP.statusCode, type: responseType)
        }

        return try decodeResponse(data: data, statusCode: httpResponse.statusCode, type: responseType)
    }

    private func executeVoid(
        _ request: URLRequest,
        endpoint: APIEndpoint
    ) async throws {
        var urlRequest = request

        if endpoint.requiresAuth {
            urlRequest = try await AuthInterceptor.shared.applyToken(to: urlRequest)
        }

        #if DEBUG
        urlRequest.logCURL()
        #endif

        let (data, response) = try await performRequest(urlRequest)

        guard let httpResponse = response as? HTTPURLResponse else {
            throw APIError.unknown("응답을 처리할 수 없습니다.")
        }

        if httpResponse.statusCode == 401 && endpoint.requiresAuth {
            let retryRequest = try await AuthInterceptor.shared.handleUnauthorized(original: urlRequest)
            let (retryData, retryResponse) = try await performRequest(retryRequest)

            guard let retryHTTP = retryResponse as? HTTPURLResponse else {
                throw APIError.unknown("응답을 처리할 수 없습니다.")
            }
            try checkVoidResponse(data: retryData, statusCode: retryHTTP.statusCode)
            return
        }

        try checkVoidResponse(data: data, statusCode: httpResponse.statusCode)
    }

    private func performRequest(_ request: URLRequest) async throws -> (Data, URLResponse) {
        do {
            return try await session.data(for: request)
        } catch {
            throw APIError.networkError("네트워크 연결을 확인해주세요.")
        }
    }

    private func decodeResponse<T: Decodable>(
        data: Data,
        statusCode: Int,
        type: T.Type
    ) throws -> T {
        let apiResponse: APIResponse<T>
        do {
            apiResponse = try decoder.decode(APIResponse<T>.self, from: data)
        } catch {
            throw APIError.decodingError(error.localizedDescription)
        }

        guard apiResponse.isSuccess, let responseData = apiResponse.data else {
            throw APIError.from(statusCode: statusCode, message: apiResponse.meta.message)
        }

        return responseData
    }

    private func checkVoidResponse(data: Data, statusCode: Int) throws {
        if statusCode >= 200 && statusCode < 300 { return }

        let apiResponse: APIResponse<EmptyData>?
        apiResponse = try? decoder.decode(APIResponse<EmptyData>.self, from: data)

        throw APIError.from(statusCode: statusCode, message: apiResponse?.meta.message)
    }
}

private struct EmptyData: Decodable {}
