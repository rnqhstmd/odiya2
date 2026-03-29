import SwiftUI

enum ViewMode: String, CaseIterable {
    case monthly = "월간"
    case weekly  = "주간"
}

/// 월간 뷰 내 표시 방식
enum MonthlyDisplayMode: String, CaseIterable {
    case dot  = "도트"
    case list = "리스트"

    var iconName: String {
        switch self {
        case .dot:  return "circle.grid.3x3.fill"
        case .list: return "list.bullet"
        }
    }
}

struct CalendarView: View {

    @StateObject private var viewModel = CalendarViewModel()
    @State private var viewMode: ViewMode = .monthly
    @State private var monthlyDisplayMode: MonthlyDisplayMode = .dot
    @Environment(\.showNotifications) private var showNotifications
    @Environment(\.showProfile) private var showProfile

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // 상단 헤더
                header

                Divider()

                // 캘린더 본체
                if viewMode == .monthly {
                    MonthlyCalendarView(viewModel: viewModel, displayMode: monthlyDisplayMode)
                        .padding(.horizontal, 4)
                } else {
                    WeeklyCalendarView(viewModel: viewModel)
                }

                Divider()

                // 선택된 날짜 약속 목록
                DayAppointmentListView(viewModel: viewModel)
                    .frame(maxHeight: .infinity)
            }
            .refreshable {
                await viewModel.reloadCurrentMonth()
            }
        }
    }

    // MARK: - Header

    private var header: some View {
        HStack(spacing: 12) {
            // 월/연 타이틀
            Text(viewModel.monthTitle(for: viewModel.currentMonth))
                .font(.title3)
                .fontWeight(.bold)
                .foregroundColor(.primary)
                .animation(.none, value: viewModel.currentMonth)

            Spacer()

            // 월간 뷰일 때 도트/리스트 토글
            if viewMode == .monthly {
                Button {
                    withAnimation(.easeInOut(duration: 0.2)) {
                        monthlyDisplayMode = monthlyDisplayMode == .dot ? .list : .dot
                    }
                } label: {
                    Image(systemName: monthlyDisplayMode == .dot ? "list.bullet" : "circle.grid.3x3.fill")
                        .font(.system(size: 15, weight: .medium))
                        .foregroundColor(OdiyaColors.primary)
                        .frame(width: 32, height: 32)
                        .background(OdiyaColors.odiya100)
                        .clipShape(Circle())
                }
            }

            // 오늘 버튼
            Button {
                viewModel.goToToday()
            } label: {
                Text("오늘")
                    .font(.subheadline)
                    .fontWeight(.medium)
                    .foregroundColor(OdiyaColors.primary)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 6)
                    .background(OdiyaColors.odiya100)
                    .clipShape(Capsule())
            }

            // 뷰 모드 피커
            Picker("보기 방식", selection: $viewMode) {
                ForEach(ViewMode.allCases, id: \.self) { mode in
                    Text(mode.rawValue).tag(mode)
                }
            }
            .pickerStyle(.segmented)
            .frame(width: 100)

            Button { showNotifications.wrappedValue = true } label: {
                Image(systemName: "bell")
                    .font(.system(size: 15, weight: .medium))
                    .foregroundColor(OdiyaColors.primary)
                    .frame(width: 32, height: 32)
            }

            Button { showProfile.wrappedValue = true } label: {
                Image(systemName: "person.circle")
                    .font(.system(size: 15, weight: .medium))
                    .foregroundColor(OdiyaColors.primary)
                    .frame(width: 32, height: 32)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
    }
}

#Preview {
    CalendarView()
}
