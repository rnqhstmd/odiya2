import Foundation

extension Data {

    /// 디버그용 JSON 포맷팅 출력
    func prettyPrintJSON() {
        #if DEBUG
        if let json = try? JSONSerialization.jsonObject(with: self),
           let prettyData = try? JSONSerialization.data(withJSONObject: json, options: .prettyPrinted),
           let prettyString = String(data: prettyData, encoding: .utf8) {
            print("[API Response]\n\(prettyString)")
        } else if let rawString = String(data: self, encoding: .utf8) {
            print("[API Response] \(rawString)")
        }
        #endif
    }
}
