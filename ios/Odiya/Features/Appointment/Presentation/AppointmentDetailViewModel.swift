import Foundation
import UIKit
import Combine
import KakaoSDKShare
import KakaoSDKTemplate

@MainActor
final class AppointmentDetailViewModel: ObservableObject {

    // MARK: - Published

    @Published var appointment: Appointment
    @Published var isLoading: Bool = false
    @Published var errorMessage: String? = nil
    @Published var showCancelConfirm: Bool = false
    @Published var showEditSheet: Bool = false
    @Published var editName: String = ""
    @Published var editDateTime: Date = Date()

    // MARK: - Dependencies

    private let repository: AppointmentRepository

    // MARK: - Init

    init(appointment: Appointment, repository: AppointmentRepository = AppointmentRepositoryImpl()) {
        self.appointment = appointment
        self.repository = repository
    }

    // MARK: - Methods

    func loadDetail() async {
        isLoading = true
        errorMessage = nil
        defer { isLoading = false }
        do {
            appointment = try await repository.getAppointment(id: appointment.id)
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func sendNudge() async {
        let targetIds = appointment.participants
            .filter { !$0.isHost }
            .map { $0.id }
        guard !targetIds.isEmpty else { return }
        isLoading = true
        defer { isLoading = false }
        do {
            try await repository.nudge(id: appointment.id, targetUserIds: targetIds)
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func acceptInvitation() async {
        isLoading = true
        errorMessage = nil
        defer { isLoading = false }
        do {
            try await repository.acceptInvitation(id: appointment.id)
            await loadDetail()
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func rejectInvitation() async {
        isLoading = true
        errorMessage = nil
        defer { isLoading = false }
        do {
            try await repository.rejectInvitation(id: appointment.id)
            await loadDetail()
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func cancelAppointment() async {
        isLoading = true
        errorMessage = nil
        defer { isLoading = false }
        do {
            try await repository.cancelAppointment(id: appointment.id)
            appointment.status = .cancelled
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func prepareEdit() {
        editName = appointment.name
        editDateTime = appointment.dateTime
        showEditSheet = true
    }

    func updateAppointment() async {
        guard editName != appointment.name || editDateTime != appointment.dateTime else {
            showEditSheet = false
            return
        }
        isLoading = true
        errorMessage = nil
        defer { isLoading = false }
        do {
            let request = UpdateAppointmentRequest(
                name: editName != appointment.name ? editName : nil,
                placeName: nil,
                placeAddress: nil,
                latitude: nil,
                longitude: nil,
                dateTime: editDateTime != appointment.dateTime ? editDateTime : nil
            )
            _ = try await repository.updateAppointment(id: appointment.id, request: request)
            await loadDetail()
            showEditSheet = false
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    // MARK: - 외부 앱 길안내

    func openNavigation() {
        let lat = appointment.latitude
        let lng = appointment.longitude
        let name = appointment.placeName
            .addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""

        // 우선순위: 카카오맵 > 네이버맵 > 애플맵
        let candidates: [(scheme: String, urlString: String)] = [
            ("kakaomap://", "kakaomap://route?ep=\(lat),\(lng)&by=CAR"),
            ("nmap://", "nmap://route/car?dlat=\(lat)&dlng=\(lng)&dname=\(name)&appname=com.odiya"),
            ("maps://", "maps://?daddr=\(lat),\(lng)&dirflg=d")
        ]

        for candidate in candidates {
            guard let schemeURL = URL(string: candidate.scheme),
                  let targetURL = URL(string: candidate.urlString) else { continue }

            // 애플맵은 항상 열 수 있으므로 canOpenURL 체크 생략
            if candidate.scheme == "maps://" || UIApplication.shared.canOpenURL(schemeURL) {
                UIApplication.shared.open(targetURL)
                return
            }
        }
    }

    // MARK: - 카카오톡 공유

    private static let shareDateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "ko_KR")
        formatter.dateFormat = "M월 d일 (E) a h:mm"
        return formatter
    }()

    func shareToKakao() {
        let dateString = Self.shareDateFormatter.string(from: appointment.dateTime)

        guard let imageUrl = URL(string: "https://odiya.app/og-image.png") else {
            errorMessage = "카카오톡 공유 이미지 URL 생성에 실패했습니다."
            return
        }

        let appointmentLink = Link(iosExecutionParams: ["appointmentId": "\(appointment.id)"])

        let template = FeedTemplate(
            content: Content(
                title: appointment.name,
                description: "\(dateString)\n\(appointment.placeName)",
                imageUrl: imageUrl,
                link: appointmentLink
            ),
            buttons: [
                Button(
                    title: "약속 확인하기",
                    link: appointmentLink
                )
            ]
        )

        guard ShareApi.isKakaoTalkSharingAvailable() else {
            errorMessage = "카카오톡이 설치되어 있지 않습니다."
            return
        }

        ShareApi.shared.shareDefault(templatable: template) { [weak self] sharingResult, error in
            DispatchQueue.main.async {
                if let error {
                    self?.errorMessage = "공유에 실패했습니다: \(error.localizedDescription)"
                    return
                }
                if let url = sharingResult?.url {
                    UIApplication.shared.open(url)
                }
            }
        }
    }
}
