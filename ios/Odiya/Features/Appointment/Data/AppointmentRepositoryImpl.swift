import Foundation

final class AppointmentRepositoryImpl: AppointmentRepository {

    private let apiClient: APIClient

    init(apiClient: APIClient = .shared) {
        self.apiClient = apiClient
    }

    func createAppointment(request: CreateAppointmentRequest) async throws -> Appointment {
        let dto = try await apiClient.request(
            endpoint: .createAppointment,
            body: request,
            responseType: AppointmentResponseDTO.self
        )
        return dto.toDomain()
    }

    func getAppointment(id: Int64) async throws -> Appointment {
        let dto = try await apiClient.request(
            endpoint: .getAppointment(id: id),
            responseType: AppointmentDetailResponseDTO.self
        )
        var appointment = dto.toDomain()
        appointment.currentUserIsHost = dto.isHost
        return appointment
    }

    func getMyAppointments(status: String, cursor: Int64?, size: Int) async throws -> (appointments: [Appointment], hasNext: Bool) {
        let dto = try await apiClient.request(
            endpoint: .getMyAppointments(status: status, cursor: cursor, size: size),
            responseType: AppointmentListResponseDTO.self
        )
        let appointments = dto.appointments.map { $0.toDomain() }
        return (appointments, dto.hasNext)
    }

    func updateAppointment(id: Int64, request: UpdateAppointmentRequest) async throws -> Appointment {
        let dto = try await apiClient.request(
            endpoint: .updateAppointment(id: id),
            body: request,
            responseType: AppointmentResponseDTO.self
        )
        return dto.toDomain()
    }

    func cancelAppointment(id: Int64) async throws {
        try await apiClient.requestVoid(endpoint: .cancelAppointment(id: id))
    }

    func acceptInvitation(id: Int64) async throws {
        try await apiClient.requestVoid(endpoint: .acceptInvitation(id: id))
    }

    func rejectInvitation(id: Int64) async throws {
        try await apiClient.requestVoid(endpoint: .rejectInvitation(id: id))
    }

    func inviteParticipants(id: Int64, userIds: [Int64]) async throws {
        let body = InviteRequest(userIds: userIds)
        try await apiClient.requestVoid(endpoint: .inviteParticipants(id: id), body: body)
    }

    func updateDeparture(id: Int64, request: UpdateDepartureRequest) async throws -> DepartureUpdateResponseDTO {
        return try await apiClient.request(
            endpoint: .updateDeparture(id: id),
            body: request,
            responseType: DepartureUpdateResponseDTO.self
        )
    }

    func nudge(id: Int64, targetUserIds: [Int64]) async throws {
        let body = NudgeRequest(targetUserIds: targetUserIds)
        try await apiClient.requestVoid(endpoint: .nudge(id: id), body: body)
    }

    func searchPlaces(keyword: String, page: Int) async throws -> PlaceSearchResponseDTO {
        return try await apiClient.request(
            endpoint: .searchPlaces(keyword: keyword, page: page),
            responseType: PlaceSearchResponseDTO.self
        )
    }
}
