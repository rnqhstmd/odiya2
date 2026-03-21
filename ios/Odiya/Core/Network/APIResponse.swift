import Foundation

/// 백엔드 ApiResponse<T> 매핑
struct APIResponse<T: Decodable>: Decodable {
    let meta: Metadata
    let data: T?

    struct Metadata: Decodable {
        let result: Result
        let errorCode: String?
        let message: String?

        enum Result: String, Decodable {
            case SUCCESS
            case FAIL
        }
    }

    var isSuccess: Bool {
        meta.result == .SUCCESS
    }
}
