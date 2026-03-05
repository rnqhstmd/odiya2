import Foundation
import Combine

@MainActor
final class AppointmentDetailViewModel: ObservableObject {

    // MARK: - Published

    @Published var appointment: Appointment

    // MARK: - Init

    init(appointment: Appointment) {
        self.appointment = appointment
    }

    // MARK: - Methods

    func sendNudge() {
        // TODO: 백엔드 연동 시 실제 API 호출
        print("[Nudge] \(appointment.name) 참여자에게 재촉 전송")
    }

    func openNavigation() {
        // TODO: 카카오맵 또는 Apple Maps 딥링크 연동
        let lat = appointment.latitude
        let lng = appointment.longitude
        let name = appointment.placeName.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        let urlString = "maps://?ll=\(lat),\(lng)&q=\(name)"
        guard let url = URL(string: urlString) else { return }
        print("[Navigation] 길찾기 열기: \(url)")
    }

    func shareToKakao() {
        // TODO: 카카오 SDK 연동
        print("[KakaoShare] '\(appointment.name)' 약속 카톡 공유")
    }
}
