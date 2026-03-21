import SwiftUI

struct DayAppointmentListView: View {

    @ObservedObject var viewModel: CalendarViewModel

    private var dayAppointments: [Appointment] {
        viewModel.appointments(for: viewModel.selectedDate)
    }

    private var dateHeaderText: String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "ko_KR")
        formatter.dateFormat = "M월 d일 (E)"
        return formatter.string(from: viewModel.selectedDate)
    }

    var body: some View {
        VStack(spacing: 0) {
            // 날짜 헤더 + "+" 버튼
            HStack {
                Text(dateHeaderText)
                    .font(.headline)
                    .fontWeight(.semibold)
                    .foregroundColor(.primary)

                Spacer()

                Button {
                    // 약속 생성 진입 (Mock)
                } label: {
                    Image(systemName: "plus")
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(OdiyaColors.primary)
                        .frame(width: 32, height: 32)
                        .background(OdiyaColors.odiya100)
                        .clipShape(Circle())
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)

            Divider()

            if dayAppointments.isEmpty {
                // 빈 상태
                VStack(spacing: 8) {
                    Image(systemName: "calendar.badge.exclamationmark")
                        .font(.system(size: 36))
                        .foregroundColor(OdiyaColors.odiya300)
                    Text("약속이 없는 날이에요")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 32)
            } else {
                // 약속 목록
                ScrollView(.vertical, showsIndicators: false) {
                    LazyVStack(spacing: 0) {
                        ForEach(dayAppointments) { appointment in
                            NavigationLink(destination: AppointmentDetailPlaceholderView(appointment: appointment)) {
                                AppointmentRow(appointment: appointment)
                            }
                            .buttonStyle(.plain)

                            Divider()
                                .padding(.leading, 16)
                        }
                    }
                }
            }
        }
        .background(Color(.systemBackground))
    }
}

// MARK: - Appointment Row

private struct AppointmentRow: View {

    let appointment: Appointment

    private var timeText: String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "ko_KR")
        formatter.dateFormat = "HH:mm"
        return formatter.string(from: appointment.dateTime)
    }

    private var tagColor: Color {
        appointment.participants.first(where: { $0.tag != nil })?.tag?.color ?? OdiyaColors.primary
    }

    var body: some View {
        HStack(spacing: 12) {
            // 태그 색상 인디케이터
            RoundedRectangle(cornerRadius: 2)
                .fill(tagColor)
                .frame(width: 4, height: 44)

            // 시간
            Text(timeText)
                .font(.footnote)
                .foregroundColor(.secondary)
                .frame(width: 44, alignment: .leading)

            // 약속 정보
            VStack(alignment: .leading, spacing: 2) {
                Text(appointment.name)
                    .font(.subheadline)
                    .fontWeight(.medium)
                    .foregroundColor(.primary)
                    .lineLimit(1)

                Text(appointment.placeName)
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .lineLimit(1)
            }

            Spacer()

            // 상태 뱃지
            Text(appointment.status.displayName)
                .font(.caption2)
                .fontWeight(.semibold)
                .foregroundColor(statusColor(appointment.status))
                .padding(.horizontal, 8)
                .padding(.vertical, 3)
                .background(statusColor(appointment.status).opacity(0.12))
                .clipShape(Capsule())

            Image(systemName: "chevron.right")
                .font(.caption)
                .foregroundColor(Color(.systemGray3))
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 10)
        .contentShape(Rectangle())
    }

    private func statusColor(_ status: AppointmentStatus) -> Color {
        switch status {
        case .confirmed: return OdiyaColors.primary
        case .pending:   return OdiyaColors.warning
        case .completed: return OdiyaColors.success
        case .cancelled: return OdiyaColors.danger
        }
    }
}

// MARK: - Placeholder Detail View

struct AppointmentDetailPlaceholderView: View {
    let appointment: Appointment

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "calendar")
                .font(.system(size: 48))
                .foregroundColor(OdiyaColors.primary)
            Text(appointment.name)
                .font(.title2)
                .fontWeight(.bold)
            Text(appointment.placeName)
                .font(.body)
                .foregroundColor(.secondary)
        }
        .navigationTitle("약속 상세")
        .navigationBarTitleDisplayMode(.inline)
    }
}

#Preview {
    NavigationStack {
        DayAppointmentListView(viewModel: CalendarViewModel())
    }
}
