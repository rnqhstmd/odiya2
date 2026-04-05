import SwiftUI

struct MonthlyCalendarView: View {

    @ObservedObject var viewModel: CalendarViewModel
    var displayMode: MonthlyDisplayMode = .dot

    private let weekdayHeaders = ["월", "화", "수", "목", "금", "토", "일"]
    private let columns = Array(repeating: GridItem(.flexible(), spacing: 0), count: 7)

    var body: some View {
        VStack(spacing: 0) {
            // 요일 헤더
            LazyVGrid(columns: columns, spacing: 0) {
                ForEach(weekdayHeaders, id: \.self) { day in
                    Text(day)
                        .font(.caption)
                        .fontWeight(.medium)
                        .foregroundColor(.secondary)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 8)
                }
            }

            Divider()

            // 날짜 그리드
            LazyVGrid(columns: columns, spacing: 0) {
                ForEach(Array(viewModel.calendarDays(for: viewModel.currentMonth).enumerated()), id: \.offset) { _, date in
                    if let date = date {
                        switch displayMode {
                        case .dot:
                            DotDayCell(
                                date: date,
                                tags: viewModel.tags(for: date),
                                isToday: viewModel.korCalendar.isDateInToday(date),
                                isSelected: viewModel.korCalendar.isDate(date, inSameDayAs: viewModel.selectedDate),
                                isCurrentMonth: viewModel.korCalendar.isDate(date, equalTo: viewModel.currentMonth, toGranularity: .month)
                            ) {
                                viewModel.selectedDate = date
                            }
                        case .list:
                            ListDayCell(
                                date: date,
                                appointments: viewModel.appointments(for: date),
                                isToday: viewModel.korCalendar.isDateInToday(date),
                                isSelected: viewModel.korCalendar.isDate(date, inSameDayAs: viewModel.selectedDate),
                                isCurrentMonth: viewModel.korCalendar.isDate(date, equalTo: viewModel.currentMonth, toGranularity: .month)
                            ) {
                                viewModel.selectedDate = date
                            }
                        }
                    } else {
                        Color.clear
                            .frame(height: displayMode == .dot ? 60 : 80)
                    }
                }
            }
        }
        .gesture(
            DragGesture(minimumDistance: 40)
                .onEnded { value in
                    if value.translation.width < 0 {
                        viewModel.moveMonth(by: 1)
                    } else {
                        viewModel.moveMonth(by: -1)
                    }
                }
        )
        .animation(.easeInOut(duration: 0.2), value: displayMode)
    }
}

// MARK: - Dot Mode Cell (기존)

private struct DotDayCell: View {

    let date: Date
    let tags: [Tag]
    let isToday: Bool
    let isSelected: Bool
    let isCurrentMonth: Bool
    let onTap: () -> Void

    private var day: Int {
        Calendar.current.component(.day, from: date)
    }

    private var displayedTags: [Tag] {
        Array(tags.prefix(3))
    }

    private var extraTagCount: Int {
        max(0, tags.count - 3)
    }

    var body: some View {
        Button(action: onTap) {
            VStack(spacing: 4) {
                // 날짜 숫자
                ZStack {
                    if isToday {
                        Circle()
                            .fill(OdiyaColors.primary)
                            .frame(width: 28, height: 28)
                    } else if isSelected {
                        Circle()
                            .fill(OdiyaColors.odiya100)
                            .frame(width: 28, height: 28)
                    }

                    Text("\(day)")
                        .font(.subheadline)
                        .fontWeight(isToday ? .bold : .regular)
                        .foregroundColor(
                            isToday ? .white :
                            isCurrentMonth ? .primary : Color(.systemGray3)
                        )
                }
                .frame(width: 28, height: 28)

                // 태그 도트
                HStack(spacing: 2) {
                    ForEach(displayedTags) { tag in
                        TagDotView(color: tag.color, size: 5)
                    }
                    if extraTagCount > 0 {
                        Text("+\(extraTagCount)")
                            .font(.system(size: 8))
                            .foregroundColor(.secondary)
                    }
                }
                .frame(height: 8)
            }
            .frame(maxWidth: .infinity)
            .frame(height: 60)
        }
        .buttonStyle(.plain)
    }
}

// MARK: - List Mode Cell (새로 추가)

private struct ListDayCell: View {

    let date: Date
    let appointments: [Appointment]
    let isToday: Bool
    let isSelected: Bool
    let isCurrentMonth: Bool
    let onTap: () -> Void

    private var day: Int {
        Calendar.current.component(.day, from: date)
    }

    private var sortedAppointments: [Appointment] {
        appointments.sorted { $0.dateTime < $1.dateTime }
    }

    /// 셀에 표시할 최대 약속 수
    private var displayedAppointments: [Appointment] {
        Array(sortedAppointments.prefix(2))
    }

    private var extraCount: Int {
        max(0, appointments.count - 2)
    }

    var body: some View {
        Button(action: onTap) {
            VStack(alignment: .leading, spacing: 2) {
                // 날짜 숫자
                HStack {
                    ZStack {
                        if isToday {
                            Circle()
                                .fill(OdiyaColors.primary)
                                .frame(width: 22, height: 22)
                        } else if isSelected {
                            Circle()
                                .fill(OdiyaColors.odiya100)
                                .frame(width: 22, height: 22)
                        }

                        Text("\(day)")
                            .font(.caption)
                            .fontWeight(isToday ? .bold : .regular)
                            .foregroundColor(
                                isToday ? .white :
                                isCurrentMonth ? .primary : Color(.systemGray3)
                            )
                    }
                    .frame(width: 22, height: 22)

                    Spacer()
                }

                // 약속 미니 리스트
                if appointments.isEmpty {
                    Spacer()
                } else {
                    VStack(alignment: .leading, spacing: 1) {
                        ForEach(displayedAppointments) { appointment in
                            MiniAppointmentLabel(appointment: appointment)
                        }
                        if extraCount > 0 {
                            Text("+\(extraCount)개")
                                .font(.system(size: 8))
                                .foregroundColor(.secondary)
                        }
                    }
                    Spacer(minLength: 0)
                }
            }
            .padding(.horizontal, 2)
            .padding(.vertical, 4)
            .frame(maxWidth: .infinity, alignment: .leading)
            .frame(height: 80)
            .background(isSelected ? OdiyaColors.odiya50 : Color.clear)
            .clipShape(RoundedRectangle(cornerRadius: 4))
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Mini Appointment Label

private struct MiniAppointmentLabel: View {

    let appointment: Appointment

    private var tagColor: Color {
        appointment.participants.first(where: { $0.tag != nil })?.tag?.color ?? OdiyaColors.primary
    }

    private var timeText: String {
        appointment.dateTime.hourMinuteFormatted
    }

    var body: some View {
        HStack(spacing: 2) {
            RoundedRectangle(cornerRadius: 1)
                .fill(tagColor)
                .frame(width: 2, height: 12)

            Text("\(timeText) \(appointment.name)")
                .font(.system(size: 8))
                .foregroundColor(.primary)
                .lineLimit(1)
        }
    }
}

#Preview {
    VStack(spacing: 20) {
        MonthlyCalendarView(viewModel: CalendarViewModel(), displayMode: .dot)
        Divider()
        MonthlyCalendarView(viewModel: CalendarViewModel(), displayMode: .list)
    }
    .padding()
}
