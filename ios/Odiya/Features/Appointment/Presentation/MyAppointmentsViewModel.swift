import Foundation
import Combine

@MainActor
final class MyAppointmentsViewModel: ObservableObject {

    // MARK: - Published

    @Published var upcomingAppointments: [Appointment] = []
    @Published var pastAppointments: [Appointment] = []
    @Published var isLoading: Bool = false
    @Published var errorMessage: String? = nil

    // MARK: - Pagination state

    private var upcomingCursor: Int64? = nil
    private var pastCursor: Int64? = nil
    private var hasNextUpcoming: Bool = false
    private var hasNextPast: Bool = false

    // MARK: - Dependencies

    private let repository: AppointmentRepository

    // MARK: - Init

    init(repository: AppointmentRepository = AppointmentRepositoryImpl()) {
        self.repository = repository
        Task { await loadAppointments() }
    }

    // MARK: - Methods

    func loadAppointments() async {
        isLoading = true
        errorMessage = nil
        defer { isLoading = false }

        await withTaskGroup(of: Void.self) { group in
            group.addTask { await self.loadUpcoming(reset: true) }
            group.addTask { await self.loadPast(reset: true) }
        }
    }

    func loadMoreUpcoming() async {
        guard hasNextUpcoming, !isLoading else { return }
        await loadUpcoming(reset: false)
    }

    func loadMorePast() async {
        guard hasNextPast, !isLoading else { return }
        await loadPast(reset: false)
    }

    // MARK: - Private

    private func loadUpcoming(reset: Bool) async {
        do {
            let cursor = reset ? nil : upcomingCursor
            let result = try await repository.getMyAppointments(status: "UPCOMING", cursor: cursor, size: 20)
            if reset {
                upcomingAppointments = result.appointments
            } else {
                upcomingAppointments += result.appointments
            }
            upcomingAppointments.sort { $0.dateTime < $1.dateTime }
            hasNextUpcoming = result.hasNext
            upcomingCursor = result.appointments.last.map { $0.id }
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func loadPast(reset: Bool) async {
        do {
            let cursor = reset ? nil : pastCursor
            let result = try await repository.getMyAppointments(status: "PAST", cursor: cursor, size: 20)
            if reset {
                pastAppointments = result.appointments
            } else {
                pastAppointments += result.appointments
            }
            pastAppointments.sort { $0.dateTime > $1.dateTime }
            hasNextPast = result.hasNext
            pastCursor = result.appointments.last.map { $0.id }
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
