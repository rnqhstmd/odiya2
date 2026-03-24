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

    func shareToKakao() {
        let dateFormatter = DateFormatter()
        dateFormatter.locale = Locale(identifier: "ko_KR")
        dateFormatter.dateFormat = "M월 d일 (E) a h:mm"
        let dateString = dateFormatter.string(from: appointment.dateTime)

        let template = FeedTemplate(
            content: Content(
                title: appointment.name,
                description: "\(dateString)\n\(appointment.placeName)",
                imageUrl: URL(string: "https://odiya.app/og-image.png") ?? URL(string: "https://via.placeholder.com/300")!,
                link: Link(
                    iosExecutionParams: ["appointmentId": "\(appointment.id)"]
                )
            ),
            buttons: [
                Button(
                    title: "약속 확인하기",
                    link: Link(
                        iosExecutionParams: ["appointmentId": "\(appointment.id)"]
                    )
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
