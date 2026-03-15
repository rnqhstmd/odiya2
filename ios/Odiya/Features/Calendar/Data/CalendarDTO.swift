import Foundation

struct CalendarAppointmentDTO: Decodable {
    let id: Int64
    let name: String
    let dateTime: String
    let placeName: String
    let tagColor: String?
}

struct CalendarDayResponseDTO: Decodable {
    let date: String
    let appointments: [CalendarAppointmentDTO]
}
