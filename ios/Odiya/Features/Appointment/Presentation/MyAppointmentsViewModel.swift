import Foundation
import Combine

@MainActor
final class MyAppointmentsViewModel: ObservableObject {

    // MARK: - Published

    @Published var upcomingAppointments: [Appointment] = []
    @Published var pastAppointments: [Appointment] = []

    // MARK: - Init

    init() {
        loadAppointments()
    }

    // MARK: - Methods

    func loadAppointments() {
        upcomingAppointments = MockData.upcomingAppointments
            .sorted { $0.dateTime < $1.dateTime }
        pastAppointments = MockData.pastAppointments
            .sorted { $0.dateTime > $1.dateTime }
    }
}
