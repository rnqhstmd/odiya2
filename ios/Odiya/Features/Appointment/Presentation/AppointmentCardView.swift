import SwiftUI

struct AppointmentCardView: View {

    let appointment: Appointment
    var isImminent: Bool = false

    // MARK: - Body

    var body: some View {
        if isImminent {
            imminentCard
        } else {
            regularCard
        }
    }

    // MARK: - Imminent Card

    private var imminentCard: some View {
        VStack(alignment: .leading, spacing: 12) {
            // 헤더
            HStack {
                Label("임박", systemImage: "clock.fill")
                    .font(.caption)
                    .fontWeight(.semibold)
                    .foregroundStyle(.white)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 4)
                    .background(OdiyaColors.primaryGradient)
                    .clipShape(Capsule())
                Spacer()
                CountdownView(targetDate: appointment.dateTime)
            }

            // 약속명
            Text(appointment.name)
                .font(.title3)
                .fontWeight(.bold)
                .foregroundStyle(OdiyaColors.odiya900)

            // 장소
            HStack(spacing: 4) {
                Image(systemName: "mappin.circle.fill")
                    .foregroundStyle(OdiyaColors.primary)
                VStack(alignment: .leading, spacing: 2) {
                    Text(appointment.placeName)
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundStyle(.primary)
                    Text(appointment.placeAddress)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
            }

            // 날짜/시간
            HStack(spacing: 4) {
                Image(systemName: "calendar")
                    .foregroundStyle(OdiyaColors.odiya500)
                Text(appointment.dateTime, style: .date)
                    .font(.subheadline)
                Text(appointment.dateTime, style: .time)
                    .font(.subheadline)
            }
            .foregroundStyle(.secondary)

            Divider()

            // 참여자 + 이동수단
            HStack {
                ParticipantStackView(participants: appointment.participants)
                Spacer()
                transportInfo
            }

            // 출발까지 카운트다운
            if let minutes = appointment.minutesUntilDeparture {
                departureCountdownView(minutes: minutes)
            }

            // 재촉하기 버튼
            if appointment.canNudge {
                NudgeButton()
            }
        }
        .padding(16)
        .background(OdiyaColors.odiya100)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(OdiyaColors.odiya300, lineWidth: 1.5)
        )
        .shadow(color: OdiyaColors.odiya500.opacity(0.15), radius: 12, x: 0, y: 4)
    }

    // MARK: - Regular Card

    private var regularCard: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 4) {
                    Text(appointment.name)
                        .font(.headline)
                        .foregroundStyle(.primary)
                    HStack(spacing: 4) {
                        Image(systemName: "mappin")
                            .font(.caption)
                            .foregroundStyle(OdiyaColors.odiya500)
                        Text(appointment.placeName)
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                    }
                    HStack(spacing: 4) {
                        Image(systemName: "calendar")
                            .font(.caption)
                            .foregroundStyle(OdiyaColors.odiya500)
                        Text(appointment.dateTime.koreanFormatted)
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }
                Spacer()
                statusBadge
            }

            HStack {
                ParticipantStackView(participants: appointment.participants, imageSize: 24)
                Spacer()
                transportInfo
            }
        }
        .padding(16)
        .background(Color(.systemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .shadow(color: .black.opacity(0.06), radius: 6, x: 0, y: 2)
    }

    // MARK: - Sub Views

    private var transportInfo: some View {
        HStack(spacing: 4) {
            Image(systemName: appointment.transportType.iconName)
                .font(.caption)
                .foregroundStyle(OdiyaColors.odiya500)
            if let duration = appointment.durationMinutes {
                Text("\(duration)분")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        }
    }

    private var statusBadge: some View {
        Text(appointment.status.displayName)
            .font(.caption2)
            .fontWeight(.semibold)
            .foregroundStyle(.white)
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(statusColor)
            .clipShape(Capsule())
    }

    private var statusColor: Color {
        switch appointment.status {
        case .confirmed: return OdiyaColors.success
        case .completed: return Color(.systemGray)
        case .cancelled: return OdiyaColors.danger
        case .pending:   return OdiyaColors.warning
        }
    }

    private func departureCountdownView(minutes: Int) -> some View {
        HStack(spacing: 6) {
            Image(systemName: "figure.walk")
                .foregroundStyle(OdiyaColors.nudge)
            Text("출발까지 \(minutes)분")
                .font(.subheadline)
                .fontWeight(.semibold)
                .foregroundStyle(OdiyaColors.nudge)
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(OdiyaColors.nudge.opacity(0.1))
        .clipShape(RoundedRectangle(cornerRadius: 10))
    }
}

// MARK: - Countdown View (매초 갱신)

private struct CountdownView: View {

    @StateObject private var countdown: CountdownTimer

    init(targetDate: Date) {
        _countdown = StateObject(wrappedValue: CountdownTimer(targetDate: targetDate))
    }

    var body: some View {
        Text(countdownText)
            .font(.caption)
            .fontWeight(.semibold)
            .foregroundStyle(countdown.secondsRemaining <= 1800 ? OdiyaColors.nudge : OdiyaColors.primary)
            .monospacedDigit()
            .onAppear { countdown.start() }
            .onDisappear { countdown.stop() }
    }

    private var countdownText: String {
        let s = countdown.secondsRemaining
        if s <= 0 { return "지금!" }
        if s <= 1800 {
            let mins = s / 60, secs = s % 60
            return mins > 0 ? "\(mins)분 \(secs)초 후" : "\(secs)초 후"
        }
        if s < 3600 { return "\(s / 60)분 후" }
        let h = s / 3600, m = (s % 3600) / 60
        return m == 0 ? "\(h)시간 후" : "\(h)시간 \(m)분 후"
    }
}

#Preview {
    ScrollView {
        VStack(spacing: 16) {
            AppointmentCardView(
                appointment: MockData.upcomingAppointments[0],
                isImminent: true
            )
            AppointmentCardView(
                appointment: MockData.upcomingAppointments[1],
                isImminent: false
            )
            AppointmentCardView(
                appointment: MockData.pastAppointments[0],
                isImminent: false
            )
            AppointmentCardView(
                appointment: MockData.pastAppointments[1],
                isImminent: false
            )
        }
        .padding()
    }
    .background(Color(.systemGroupedBackground))
}
