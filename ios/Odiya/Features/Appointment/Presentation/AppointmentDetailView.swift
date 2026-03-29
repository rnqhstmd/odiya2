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
        .sheet(isPresented: $viewModel.showEditSheet) {
            editSheet
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
        KakaoMapContainerView(
            latitude: viewModel.appointment.latitude,
            longitude: viewModel.appointment.longitude,
            height: 220,
            markerTitle: viewModel.appointment.placeName,
            isScrollEnabled: false
        )
    }

    // MARK: - Content Section

    private var contentSection: some View {
        VStack(alignment: .leading, spacing: 24) {
            if viewModel.appointment.isImminent {
                countdownBanner
            }
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

    // MARK: - Countdown Banner

    private var countdownBanner: some View {
        HStack(spacing: 12) {
            Image(systemName: "clock.fill")
                .font(.title2)
                .foregroundStyle(.white)

            VStack(alignment: .leading, spacing: 2) {
                Text("약속 시간이 다가오고 있어요!")
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .foregroundStyle(.white)

                DepartureCountdownBannerText(targetDate: viewModel.appointment.dateTime)
                    .id(viewModel.appointment.dateTime)
            }

            Spacer()
        }
        .padding(16)
        .background(OdiyaColors.nudgeGradient)
        .clipShape(RoundedRectangle(cornerRadius: 14))
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
                        .id(alertAt)
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
            ProfileImageView(imageUrl: participant.profileImageUrl, nickname: participant.nickname, size: 40)

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
                    viewModel.prepareEdit()
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

    // MARK: - Edit Sheet

    private var editSheet: some View {
        NavigationStack {
            Form {
                Section("약속 이름") {
                    TextField("약속 이름", text: $viewModel.editName)
                }
                Section("약속 시간") {
                    DatePicker(
                        "날짜 및 시간",
                        selection: $viewModel.editDateTime,
                        in: Date()...,
                        displayedComponents: [.date, .hourAndMinute]
                    )
                    .datePickerStyle(.graphical)
                    .environment(\.locale, Locale(identifier: "ko_KR"))
                }
            }
            .navigationTitle("약속 수정")
            .navigationBarTitleDisplayMode(.inline)
            .alert("오류", isPresented: Binding(
                get: { viewModel.errorMessage != nil && viewModel.showEditSheet },
                set: { if !$0 { viewModel.errorMessage = nil } }
            )) {
                Button("확인", role: .cancel) { viewModel.errorMessage = nil }
            } message: {
                Text(viewModel.errorMessage ?? "")
            }
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("취소") {
                        viewModel.showEditSheet = false
                    }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("저장") {
                        Task { await viewModel.updateAppointment() }
                    }
                    .fontWeight(.semibold)
                    .disabled(viewModel.editName.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
        }
    }

    // MARK: - Helpers

    private var isHost: Bool {
        viewModel.appointment.currentUserIsHost
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

// MARK: - Departure Countdown Banner Text

private struct DepartureCountdownBannerText: View {

    @StateObject private var countdown: CountdownTimer

    init(targetDate: Date) {
        _countdown = StateObject(wrappedValue: CountdownTimer(targetDate: targetDate))
    }

    var body: some View {
        Text(countdownLabel)
            .font(.caption)
            .foregroundStyle(.white.opacity(0.85))
            .monospacedDigit()
            .onAppear { countdown.start() }
            .onDisappear { countdown.stop() }
    }

    private var countdownLabel: String {
        let s = countdown.secondsRemaining
        if s <= 0 { return "지금 출발하세요!" }
        if s < 60 { return "\(s)초 남음" }
        if s < 3600 {
            let mins = s / 60, secs = s % 60
            return s <= 1800 ? "\(mins)분 \(secs)초 남음" : "\(mins)분 남음"
        }
        let hours = s / 3600, mins = (s % 3600) / 60
        return "\(hours)시간 \(mins)분 남음"
    }
}

// MARK: - Departure Countdown Detail View

private struct DepartureCountdownDetailView: View {

    @StateObject private var countdown: CountdownTimer

    init(alertAt: Date) {
        _countdown = StateObject(wrappedValue: CountdownTimer(targetDate: alertAt))
    }

    var body: some View {
        VStack(spacing: 4) {
            Text(countdownText)
                .font(.title3)
                .fontWeight(.bold)
                .foregroundStyle(countdown.secondsRemaining <= 600 ? OdiyaColors.nudge : OdiyaColors.primary)
                .monospacedDigit()
            Text("출발까지")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .onAppear { countdown.start() }
        .onDisappear { countdown.stop() }
    }

    private var countdownText: String {
        let s = countdown.secondsRemaining
        if s <= 0 { return "지금 출발!" }
        if s <= 1800 {
            let mins = s / 60, secs = s % 60
            return mins > 0 ? "\(mins)분 \(secs)초" : "\(secs)초"
        }
        return "\(s / 60)분"
    }
}

#Preview {
    NavigationStack {
        AppointmentDetailView(appointment: MockData.upcomingAppointments[0])
            .navigationTitle("약속 상세")
    }
}
