import Foundation

enum MockData {

    // MARK: - Tags

    static let tags: [Tag] = [
        .lover,
        .friend,
        .family,
        Tag(id: 4, name: "직장", colorHex: "#007AFF", isDefault: false),
        Tag(id: 5, name: "동아리", colorHex: "#AF52DE", isDefault: false),
    ]

    // MARK: - Friends

    static let friends: [Friend] = [
        Friend(id: 1, nickname: "김민지", profileImageUrl: nil, tag: .lover, status: .accepted),
        Friend(id: 2, nickname: "박서준", profileImageUrl: nil, tag: .friend, status: .accepted),
        Friend(id: 3, nickname: "이하늘", profileImageUrl: nil, tag: .friend, status: .accepted),
        Friend(id: 4, nickname: "정우진", profileImageUrl: nil, tag: .family, status: .accepted),
        Friend(id: 5, nickname: "최지우", profileImageUrl: nil, tag: tags[3], status: .accepted),
    ]

    static let pendingFriends: [Friend] = [
        Friend(id: 101, nickname: "강다은", profileImageUrl: nil, tag: .friend, status: .pending),
        Friend(id: 102, nickname: "윤성호", profileImageUrl: nil, tag: .friend, status: .pending),
    ]

    // MARK: - Departure Places

    static let departurePlaces: [DeparturePlace] = [
        DeparturePlace(id: 1, label: "집", address: "서울 강남구 역삼동", latitude: 37.4979, longitude: 127.0276),
        DeparturePlace(id: 2, label: "회사", address: "서울 종로구 종로1가", latitude: 37.5704, longitude: 126.9831),
    ]

    // MARK: - Participants

    static let participants: [Participant] = [
        Participant(id: 0, nickname: "나", profileImageUrl: nil, status: .accepted, isHost: true, tag: nil),
        Participant(id: 1, nickname: "김민지", profileImageUrl: nil, status: .accepted, isHost: false, tag: .lover),
        Participant(id: 2, nickname: "박서준", profileImageUrl: nil, status: .pending, isHost: false, tag: .friend),
    ]

    // MARK: - Appointments

    static let upcomingAppointments: [Appointment] = [
        Appointment(
            id: 1,
            name: "스터디 모임",
            placeName: "강남역 2번출구",
            placeAddress: "서울 강남구 역삼동 858",
            latitude: 37.4979,
            longitude: 127.0276,
            dateTime: Date().addingTimeInterval(3600 * 2),
            status: .confirmed,
            participants: participants,
            hostId: 0,
            transportType: .transit,
            durationMinutes: 42,
            departurePlaceLabel: "집",
            departureAlertAt: Date().addingTimeInterval(3600)
        ),
        Appointment(
            id: 2,
            name: "저녁 약속",
            placeName: "홍대입구",
            placeAddress: "서울 마포구 서교동 395-166",
            latitude: 37.5563,
            longitude: 126.9236,
            dateTime: Date().addingTimeInterval(3600 * 5),
            status: .confirmed,
            participants: [participants[0], participants[1]],
            hostId: 0,
            transportType: .carParking,
            durationMinutes: 35,
            departurePlaceLabel: "회사",
            departureAlertAt: Date().addingTimeInterval(3600 * 4)
        ),
    ]

    static let pastAppointments: [Appointment] = [
        Appointment(
            id: 3,
            name: "브런치",
            placeName: "카페노티드 성수",
            placeAddress: "서울 성동구 성수동",
            latitude: 37.5445,
            longitude: 127.0560,
            dateTime: Date().addingTimeInterval(-3600 * 24 * 2),
            status: .completed,
            participants: [participants[0], participants[1]],
            hostId: 0,
            transportType: .walking,
            durationMinutes: 15,
            departurePlaceLabel: "집",
            departureAlertAt: nil
        ),
        Appointment(
            id: 4,
            name: "취소된 모임",
            placeName: "이태원",
            placeAddress: "서울 용산구 이태원동",
            latitude: 37.5340,
            longitude: 126.9948,
            dateTime: Date().addingTimeInterval(-3600 * 24),
            status: .cancelled,
            participants: participants,
            hostId: 0,
            transportType: .transit,
            durationMinutes: 50,
            departurePlaceLabel: "집",
            departureAlertAt: nil
        ),
    ]

    static let allAppointments: [Appointment] = upcomingAppointments + pastAppointments

    // MARK: - Notifications

    static let notifications: [AppNotification] = [
        AppNotification(
            id: "n1",
            type: .friendRequest,
            title: "강다은님이 친구 요청을 보냈어요",
            body: "수락하면 서로 약속에 초대할 수 있어요",
            createdAt: Date().addingTimeInterval(-120),
            isRead: false
        ),
        AppNotification(
            id: "n2",
            type: .appointmentInvite,
            title: "\"스터디 모임\" 초대가 도착했어요",
            body: "김민지님이 약속에 초대했어요",
            createdAt: Date().addingTimeInterval(-600),
            isRead: false
        ),
        AppNotification(
            id: "n3",
            type: .departureAlert,
            title: "출발 시간이에요!",
            body: "지금 나가야 약속 시간에 맞출 수 있어요",
            createdAt: Date().addingTimeInterval(-3600),
            isRead: true
        ),
        AppNotification(
            id: "n4",
            type: .nudge,
            title: "김민지님이 재촉했어요",
            body: "빨리 출발하세요! 🏃",
            createdAt: Date().addingTimeInterval(-7200),
            isRead: true
        ),
    ]

    // MARK: - User

    static let currentUser = User(id: 0, nickname: "김철수", profileImageUrl: nil)
}
