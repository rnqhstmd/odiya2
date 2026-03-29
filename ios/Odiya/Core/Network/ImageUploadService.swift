import Foundation

actor ImageUploadService {

    static let shared = ImageUploadService()

    private let session: URLSession

    init(session: URLSession = .shared) {
        self.session = session
    }

    func upload(imageData: Data, to presignedUrl: String, contentType: String) async throws {
        guard let url = URL(string: presignedUrl) else {
            throw APIError.unknown("잘못된 업로드 URL입니다.")
        }

        var request = URLRequest(url: url)
        request.httpMethod = "PUT"
        request.setValue(contentType, forHTTPHeaderField: "Content-Type")
        request.httpBody = imageData

        let (_, response) = try await session.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse,
              (200...299).contains(httpResponse.statusCode) else {
            throw APIError.unknown("이미지 업로드에 실패했습니다.")
        }
    }
}
