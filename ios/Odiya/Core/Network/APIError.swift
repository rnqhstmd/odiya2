import Foundation

/// 백엔드 ErrorType 매핑
enum APIError: Error, Equatable {
    // 백엔드 ErrorType 대응
    case badRequest(String?)          // 400
    case unauthorized(String?)        // 401
    case notFound(String?)            // 404
    case conflict(String?)            // 409
    case internalError(String?)       // 500
    case serviceUnavailable(String?)  // 503

    // 클라이언트 에러
    case networkError(String)
    case decodingError(String)
    case unknown(String)

    var userMessage: String {
        switch self {
        case .badRequest(let msg):
            return msg ?? "잘못된 요청입니다."
        case .unauthorized(let msg):
            return msg ?? "인증이 필요합니다."
        case .notFound(let msg):
            return msg ?? "존재하지 않는 요청입니다."
        case .conflict(let msg):
            return msg ?? "이미 존재하는 리소스입니다."
        case .internalError(let msg):
            return msg ?? "일시적인 오류가 발생했습니다."
        case .serviceUnavailable(let msg):
            return msg ?? "서비스를 일시적으로 사용할 수 없습니다."
        case .networkError(let msg):
            return msg
        case .decodingError:
            return "데이터를 처리할 수 없습니다."
        case .unknown(let msg):
            return msg
        }
    }

    static func from(statusCode: Int, message: String?) -> APIError {
        switch statusCode {
        case 400: return .badRequest(message)
        case 401: return .unauthorized(message)
        case 404: return .notFound(message)
        case 409: return .conflict(message)
        case 500: return .internalError(message)
        case 503: return .serviceUnavailable(message)
        default: return .unknown(message ?? "알 수 없는 오류가 발생했습니다.")
        }
    }
}
