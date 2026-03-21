import XCTest
@testable import Odiya

final class APIClientTests: XCTestCase {

    private var sut: APIClient!
    private var session: URLSession!

    override func setUp() {
        super.setUp()
        let config = URLSessionConfiguration.ephemeral
        config.protocolClasses = [MockURLProtocol.self]
        session = URLSession(configuration: config)
        sut = APIClient(session: session)
    }

    override func tearDown() {
        sut = nil
        session = nil
        MockURLProtocol.requestHandler = nil
        super.tearDown()
    }

    // MARK: - Success

    func test_request_성공_응답_디코딩() async throws {
        // given
        let expectedUser = UserResponseDTO(id: 1, nickname: "테스트", profileImageUrl: nil)
        let apiResponse = """
        {
            "meta": { "result": "SUCCESS", "errorCode": null, "message": null },
            "data": { "id": 1, "nickname": "테스트", "profileImageUrl": null }
        }
        """

        MockURLProtocol.requestHandler = { request in
            let response = HTTPURLResponse(
                url: request.url!,
                statusCode: 200,
                httpVersion: nil,
                headerFields: nil
            )!
            return (response, apiResponse.data(using: .utf8)!)
        }

        // when
        let result = try await sut.request(
            endpoint: .getMyProfile,
            responseType: UserResponseDTO.self
        )

        // then
        XCTAssertEqual(result.id, expectedUser.id)
        XCTAssertEqual(result.nickname, expectedUser.nickname)
    }

    // MARK: - Error

    func test_request_서버_에러_응답() async {
        // given
        let apiResponse = """
        {
            "meta": { "result": "FAIL", "errorCode": "Bad Request", "message": "잘못된 요청입니다." },
            "data": null
        }
        """

        MockURLProtocol.requestHandler = { request in
            let response = HTTPURLResponse(
                url: request.url!,
                statusCode: 400,
                httpVersion: nil,
                headerFields: nil
            )!
            return (response, apiResponse.data(using: .utf8)!)
        }

        // when / then
        do {
            _ = try await sut.request(
                endpoint: .getMyProfile,
                responseType: UserResponseDTO.self
            )
            XCTFail("에러가 발생해야 합니다.")
        } catch let error as APIError {
            XCTAssertEqual(error, .badRequest("잘못된 요청입니다."))
        } catch {
            XCTFail("APIError 타입이어야 합니다: \(error)")
        }
    }

    // MARK: - Void

    func test_requestVoid_성공() async throws {
        // given
        let apiResponse = """
        {
            "meta": { "result": "SUCCESS", "errorCode": null, "message": null },
            "data": null
        }
        """

        MockURLProtocol.requestHandler = { request in
            let response = HTTPURLResponse(
                url: request.url!,
                statusCode: 200,
                httpVersion: nil,
                headerFields: nil
            )!
            return (response, apiResponse.data(using: .utf8)!)
        }

        // when / then - 에러 없이 완료되면 성공
        try await sut.requestVoid(endpoint: .logout, body: LogoutRequest(refreshToken: "token"))
    }
}
