import SwiftUI

struct AppointmentDetailView: View {

    @StateObject private var viewModel: AppointmentDetailViewModel
    @Environment(\.dismiss) private var dismiss

    init(appointment: Appointment) {
        _viewModel = StateObject(wrappedValue: AppointmentDetailViewModel(appointment: appointment))
    }

    // MARK: - Body

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                mapPlaceholder
                contentSection
            }
        }
        .ignoresSafeArea(edges: .top)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            if isHost {
                hostToolbar
            }
        }
        .onAppear {
            Task { await viewModel.loadDetail() }
        }
        .overlay {
            if viewModel.isLoading {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(Color.black.opacity(0.1))
            }
        }
        .alert("약속 취소", isPresented: $viewModel.showCancelConfirm) {
            Button("취소하기", role: .destructive) {
                Task { await viewModel.cancelAppointment() }
            }
            Button("닫기", role: .cancel) {}
        } message: {
            Text("약속을 취소하시겠습니까? 이 작업은 되돌릴 수 없습니다.")
        }
    }

    // MARK: - Map Placeholder

    private var mapPlaceholder: some View {
        ZStack {
            Rectangle()
                .fill(OdiyaColors.odiya100)
                .frame(height: 220)
            VStack(spacing: 8) {
                Image(systemName: "mappin.circle.fill")
                    .font(.system(size: 40))
                    .foregroundStyle(OdiyaColors.primary)
                Text(viewModel.appointment.placeName)
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .foregroundStyle(OdiyaColors.odiya700)
                Text("지도는 카카오맵 연동 후 표시됩니다")
                    .font(.caption)
                    .foregroundStyle(OdiyaColors.odiya500)
            }
        }
    }

    // MARK: - Content Section

    private var contentSection: some View {
        VStack(alignment: .leading, spacing: 24) {
            basicInfoSection
            Divider()
            transportSection
            Divider()
            participantsSection
            Divider()
            actionButtons
        }
        .padding(20)
    }

    // MARK: - Basic Info

    private var basicInfoSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(viewModel.appointment.name)
                .font(.title2)
                .fontWeight(.bold)

            infoRow(
                icon: "mappin.circle.fill",
                iconColor: OdiyaColors.primary,
                title: viewModel.appointment.placeName,
                subtitle: viewModel.appointment.placeAddress
            )

            infoRow(
                icon: "calendar",
                iconColor: OdiyaColors.odiya500,
                title: viewModel.appointment.dateTime.koreanDateFormatted,
                subtitle: viewModel.appointment.dateTime.koreanTimeFormatted
            )
        }
    }

    // MARK: - Transport Section

    private var transportSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("이동 정보")
                .font(.headline)

            HStack(spacing: 16) {
                // 이동수단
                VStack(spacing: 4) {
                    Image(systemName: viewModel.appointment.transportType.iconName)
                        .font(.title2)
                        .foregroundStyle(OdiyaColors.primary)
                    Text(viewModel.appointment.transportType.shortName)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
                .frame(minWidth: 56)

                Divider().frame(height: 44)

                // 소요시간
                if let duration = viewModel.appointment.durationMinutes {
                    VStack(spacing: 4) {
                        Text("\(duration)분")
                            .font(.title3)
                            .fontWeight(.semibold)
                            .foregroundStyle(.primary)
                        Text("소요시간")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }

                    Divider().frame(height: 44)
                }

                // 출발 카운트다운
                if let alertAt = viewModel.appointment.departureAlertAt {
                    DepartureCountdownDetailView(alertAt: alertAt)
                    Divider().frame(height: 44)
                }

                // 출발지
                if let label = viewModel.appointment.departurePlaceLabel {
                    VStack(spacing: 4) {
                        Image(systemName: "house.fill")
                            .font(.title2)
                            .foregroundStyle(OdiyaColors.odiya500)
                        Text(label)
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }

                Spacer()
            }
            .padding(14)
            .background(OdiyaColors.odiya50)
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }

    // MARK: - Participants Section

    private var participantsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("참여자 \(viewModel.appointment.participants.count)명")
                .font(.headline)

            ForEach(viewModel.appointment.participants) { participant in
                participantRow(participant)
            }
        }
    }

    private func participantRow(_ participant: Participant) -> some View {
        HStack(spacing: 12) {
            ProfileImageView(imageUrl: participant.profileImageUrl, size: 40)

            VStack(alignment: .leading, spacing: 2) {
                HStack(spacing: 6) {
                    Text(participant.nickname)
                        .font(.subheadline)
                        .fontWeight(.medium)
                    if participant.isHost {
                        Text("주최자")
                            .font(.caption2)
                            .fontWeight(.semibold)
                            .foregroundStyle(OdiyaColors.primary)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(OdiyaColors.odiya100)
                            .clipShape(Capsule())
                    }
                }
                if let tag = participant.tag {
                    HStack(spacing: 4) {
                        TagDotView(color: tag.color, size: 8)
                        Text(tag.name)
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }
            }

            Spacer()

            participantStatusBadge(participant.status)
        }
        .padding(.vertical, 4)
    }

    private func participantStatusBadge(_ status: ParticipantStatus) -> some View {
        let (label, color): (String, Color) = {
            switch status {
            case .accepted: return ("수락", OdiyaColors.success)
            case .pending:  return ("대기중", OdiyaColors.warning)
            case .rejected: return ("거절", OdiyaColors.danger)
            }
        }()
        return Text(label)
            .font(.caption2)
            .fontWeight(.semibold)
            .foregroundStyle(color)
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(color.opacity(0.12))
            .clipShape(Capsule())
    }

    // MARK: - Action Buttons

    private var actionButtons: some View {
        VStack(spacing: 12) {
            if viewModel.appointment.canNudge {
                NudgeButton {
                    Task { await viewModel.sendNudge() }
                }
                .frame(maxWidth: .infinity)
            }

            HStack(spacing: 12) {
                Button {
                    viewModel.openNavigation()
                } label: {
                    Label("길찾기", systemImage: "map.fill")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundStyle(OdiyaColors.primary)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(OdiyaColors.odiya100)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                }

                Button {
                    viewModel.shareToKakao()
                } label: {
                    Label("카톡 공유", systemImage: "message.fill")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundStyle(Color(hex: 0x3A1D1D))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(OdiyaColors.kakaoYellow)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                }
            }
        }
    }

    // MARK: - Host Toolbar

    @ToolbarContentBuilder
    private var hostToolbar: some ToolbarContent {
        ToolbarItem(placement: .navigationBarTrailing) {
            Menu {
                Button {
                    // TODO: 수정 화면 연결
                } label: {
                    Label("약속 수정", systemImage: "pencil")
                }
                Button(role: .destructive) {
                    viewModel.showCancelConfirm = true
                } label: {
                    Label("약속 취소", systemImage: "xmark.circle")
                }
            } label: {
                Image(systemName: "ellipsis.circle")
                    .foregroundStyle(OdiyaColors.primary)
            }
        }
    }

    // MARK: - Helpers

    private var isHost: Bool {
        viewModel.appointment.participants.first(where: { $0.isHost })?.id == 0
    }

    private func infoRow(icon: String, iconColor: Color, title: String, subtitle: String) -> some View {
        HStack(alignment: .top, spacing: 10) {
            Image(systemName: icon)
                .foregroundStyle(iconColor)
                .frame(width: 20)
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.subheadline)
                    .fontWeight(.medium)
                Text(subtitle)
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        }
    }
}

// MARK: - Departure Countdown Detail View

private struct DepartureCountdownDetailView: View {

    let alertAt: Date
    @State private var minutesRemaining: Int = 0
    @State private var timer: Timer? = nil

    var body: some View {
        VStack(spacing: 4) {
            Text(minutesRemaining <= 0 ? "지금 출발!" : "\(minutesRemaining)분")
                .font(.title3)
                .fontWeight(.bold)
                .foregroundStyle(minutesRemaining <= 10 ? OdiyaColors.nudge : OdiyaColors.primary)
                .monospacedDigit()
            Text("출발까지")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .onAppear { startTimer() }
        .onDisappear { timer?.invalidate() }
    }

    private func startTimer() {
        updateRemaining()
        timer = Timer.scheduledTimer(withTimeInterval: 60, repeats: true) { _ in
            updateRemaining()
        }
    }

    private func updateRemaining() {
        let minutes = Calendar.current.dateComponents([.minute], from: Date(), to: alertAt).minute ?? 0
        minutesRemaining = max(0, minutes)
    }
}

// MARK: - Date Extension

private extension Date {
    var koreanDateFormatted: String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "ko_KR")
        formatter.dateFormat = "yyyy년 M월 d일 (E)"
        return formatter.string(from: self)
    }

    var koreanTimeFormatted: String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "ko_KR")
        formatter.dateFormat = "a h:mm"
        return formatter.string(from: self)
    }
}

#Preview {
    NavigationStack {
        AppointmentDetailView(appointment: MockData.upcomingAppointments[0])
            .navigationTitle("약속 상세")
    }
}
