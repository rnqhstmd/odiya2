import Foundation
import Combine

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

    func openNavigation() {
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
