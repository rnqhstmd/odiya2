import Foundation

extension URLRequest {

    /// 디버그용 cURL 명령어 출력
    func logCURL() {
        #if DEBUG
        var components = ["curl -v"]

        if let method = httpMethod {
            components.append("-X \(method)")
        }

        if let url = url?.absoluteString {
            components.append("'\(url)'")
        }

        if let headers = allHTTPHeaderFields {
            for (key, value) in headers.sorted(by: { $0.key < $1.key }) {
                components.append("-H '\(key): \(value)'")
            }
        }

        if let bodyData = httpBody, let body = String(data: bodyData, encoding: .utf8) {
            components.append("-d '\(body)'")
        }

        print("[API Request] \(components.joined(separator: " \\\n\t"))")
        #endif
    }
}
