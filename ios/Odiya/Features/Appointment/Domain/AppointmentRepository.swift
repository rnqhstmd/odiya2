import Foundation

protocol AppointmentRepository {
    func createAppointment(request: CreateAppointmentRequest) async throws -> Appointment
    func getAppointment(id: Int64) async throws -> Appointment
    func getMyAppointments(status: String, cursor: Int64?, size: Int) async throws -> (appointments: [Appointment], hasNext: Bool)
    func updateAppointment(id: Int64, request: UpdateAppointmentRequest) async throws -> Appointment
    func cancelAppointment(id: Int64) async throws
    func acceptInvitation(id: Int64) async throws
    func rejectInvitation(id: Int64) async throws
    func inviteParticipants(id: Int64, userIds: [Int64]) async throws
    func updateDeparture(id: Int64, request: UpdateDepartureRequest) async throws -> DepartureUpdateResponseDTO
    func nudge(id: Int64, targetUserIds: [Int64]) async throws
    func searchPlaces(keyword: String, page: Int) async throws -> PlaceSearchResponseDTO
}
