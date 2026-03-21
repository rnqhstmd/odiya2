import SwiftUI

struct WeeklyCalendarView: View {

    @ObservedObject var viewModel: CalendarViewModel

    private let hourRange = 8...22
    private let hourHeight: CGFloat = 60
    private let timeColumnWidth: CGFloat = 44

    var body: some View {
        VStack(spacing: 0) {
            // 요일 헤더
            weekdayHeader

            Divider()

            // 타임라인 스크롤
            ScrollView(.vertical, showsIndicators: false) {
                HStack(alignment: .top, spacing: 0) {
                    // 시간 라벨 열
                    timeLabels

                    // 날짜별 컬럼
                    HStack(alignment: .top, spacing: 0) {
                        ForEach(viewModel.weekDays(for: viewModel.selectedDate), id: \.self) { date in
                            DayColumn(
                                date: date,
                                appointments: viewModel.appointments(for: date),
                                hourRange: hourRange,
                                hourHeight: hourHeight,
                                isToday: viewModel.korCalendar.isDateInToday(date),
                                isSelected: viewModel.korCalendar.isDate(date, inSameDayAs: viewModel.selectedDate)
                            ) {
                                viewModel.selectedDate = date
                            }
                        }
                    }
                }
            }
        }
        .gesture(
            DragGesture(minimumDistance: 40)
                .onEnded { value in
                    if value.translation.width < 0 {
                        viewModel.moveWeek(by: 1)
                    } else {
                        viewModel.moveWeek(by: -1)
                    }
                }
        )
    }

    // MARK: - Weekday Header

    private var weekdayHeader: some View {
        HStack(spacing: 0) {
            // 시간 열 공백
            Color.clear.frame(width: timeColumnWidth)

            ForEach(viewModel.weekDays(for: viewModel.selectedDate), id: \.self) { date in
                let isToday = viewModel.korCalendar.isDateInToday(date)
                let isSelected = viewModel.korCalendar.isDate(date, inSameDayAs: viewModel.selectedDate)

                VStack(spacing: 2) {
                    Text(weekdayLabel(for: date))
                        .font(.caption2)
                        .foregroundColor(isToday ? OdiyaColors.primary : .secondary)

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
                        Text("\(viewModel.korCalendar.component(.day, from: date))")
                            .font(.subheadline)
                            .fontWeight(isToday ? .bold : .regular)
                            .foregroundColor(isToday ? .white : .primary)
                    }
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 6)
                .onTapGesture {
                    viewModel.selectedDate = date
                }
            }
        }
    }

    // MARK: - Time Labels

    private var timeLabels: some View {
        ZStack(alignment: .topLeading) {
            ForEach(Array(hourRange), id: \.self) { hour in
                Text(String(format: "%02d:00", hour))
                    .font(.system(size: 10))
                    .foregroundColor(.secondary)
                    .frame(width: timeColumnWidth - 4, alignment: .trailing)
                    .offset(y: CGFloat(hour - hourRange.lowerBound) * hourHeight - 7)
            }
        }
        .frame(width: timeColumnWidth)
        .frame(height: CGFloat(hourRange.count) * hourHeight)
    }

    // MARK: - Helpers

    private func weekdayLabel(for date: Date) -> String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "ko_KR")
        formatter.dateFormat = "E"
        return formatter.string(from: date)
    }
}

// MARK: - Day Column

private struct DayColumn: View {

    let date: Date
    let appointments: [Appointment]
    let hourRange: ClosedRange<Int>
    let hourHeight: CGFloat
    let isToday: Bool
    let isSelected: Bool
    let onTap: () -> Void

    var body: some View {
        ZStack(alignment: .topLeading) {
            // 시간 구분선 배경
            VStack(spacing: 0) {
                ForEach(Array(hourRange), id: \.self) { _ in
                    Divider()
                    Spacer().frame(height: hourHeight - 0.5)
                }
            }

            // 오늘 컬럼 배경 강조
            if isToday {
                OdiyaColors.odiya50
                    .ignoresSafeArea(edges: .horizontal)
            }

            // 약속 블록
            ForEach(appointments) { appointment in
                AppointmentBlock(
                    appointment: appointment,
                    hourRange: hourRange,
                    hourHeight: hourHeight
                )
            }
        }
        .frame(maxWidth: .infinity)
        .frame(height: CGFloat(hourRange.count) * hourHeight)
        .contentShape(Rectangle())
        .onTapGesture(perform: onTap)
    }
}

// MARK: - Appointment Block

private struct AppointmentBlock: View {

    let appointment: Appointment
    let hourRange: ClosedRange<Int>
    let hourHeight: CGFloat

    private var blockColor: Color {
        appointment.participants.first(where: { $0.tag != nil })?.tag?.color ?? OdiyaColors.primary
    }

    private var topOffset: CGFloat {
        let cal = Calendar.current
        let hour = cal.component(.hour, from: appointment.dateTime)
        let minute = cal.component(.minute, from: appointment.dateTime)
        let clampedHour = max(hourRange.lowerBound, min(hour, hourRange.upperBound))
        let hourOffset = CGFloat(clampedHour - hourRange.lowerBound)
        let minuteOffset = CGFloat(minute) / 60.0
        return (hourOffset + minuteOffset) * hourHeight
    }

    private var blockHeight: CGFloat {
        let durationMinutes = appointment.durationMinutes ?? 60
        let height = CGFloat(durationMinutes) / 60.0 * hourHeight
        return max(height, 20)
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 1) {
            Text(appointment.name)
                .font(.system(size: 10, weight: .semibold))
                .lineLimit(1)
            Text(appointment.placeName)
                .font(.system(size: 9))
                .lineLimit(1)
        }
        .padding(.horizontal, 4)
        .padding(.vertical, 2)
        .frame(maxWidth: .infinity, alignment: .leading)
        .frame(height: blockHeight)
        .background(blockColor.opacity(0.75))
        .clipShape(RoundedRectangle(cornerRadius: 4))
        .foregroundColor(.white)
        .padding(.horizontal, 1)
        .offset(y: topOffset)
    }
}

#Preview {
    WeeklyCalendarView(viewModel: CalendarViewModel())
}
