import SwiftUI

struct Step4ConfirmView: View {

    @ObservedObject var viewModel: CreateAppointmentViewModel

    private var dateFormatter: DateFormatter {
        let f = DateFormatter()
        f.locale = Locale(identifier: "ko_KR")
        f.dateFormat = "yyyy년 M월 d일 (E)"
        return f
    }

    private var timeFormatter: DateFormatter {
        let f = DateFormatter()
        f.locale = Locale(identifier: "ko_KR")
        f.dateFormat = "a h:mm"
        return f
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {

                // MARK: - 요약 카드
                VStack(alignment: .leading, spacing: 0) {
                    SummaryRow(icon: "text.bubble.fill", label: "약속 이름", value: viewModel.name.isEmpty ? "-" : viewModel.name)
                    Divider().padding(.leading, 44)
                    SummaryRow(icon: "mappin.circle.fill", label: "장소", value: viewModel.selectedPlace?.name ?? "-")
                    Divider().padding(.leading, 44)
                    SummaryRow(icon: "calendar", label: "날짜", value: dateFormatter.string(from: viewModel.date))
                    Divider().padding(.leading, 44)
                    SummaryRow(icon: "clock.fill", label: "시간", value: timeFormatter.string(from: viewModel.time))
                    Divider().padding(.leading, 44)
                    SummaryRow(
                        icon: "person.2.fill",
                        label: "참여자",
                        value: viewModel.selectedFriends.isEmpty
                            ? "없음"
                            : viewModel.selectedFriends.map(\.nickname).joined(separator: ", ")
                    )
                }
                .background(Color(.systemBackground))
                .clipShape(RoundedRectangle(cornerRadius: 14))
                .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color(.systemGray4), lineWidth: 1))

                // MARK: - 이동수단
                VStack(alignment: .leading, spacing: 10) {
                    Label("이동수단", systemImage: "arrow.triangle.turn.up.right.circle.fill")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundStyle(OdiyaColors.primary)

                    HStack(spacing: 8) {
                        ForEach(TransportType.allCases) { type in
                            TransportButton(
                                type: type,
                                isSelected: viewModel.transportType == type
                            ) {
                                viewModel.transportType = type
                            }
                        }
                    }
                }

                // MARK: - 출발지
                VStack(alignment: .leading, spacing: 10) {
                    Label("출발지", systemImage: "house.fill")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundStyle(OdiyaColors.primary)

                    // 등록된 장소 카드
                    VStack(spacing: 0) {
                        ForEach(viewModel.departurePlaces) { place in
                            Button {
                                viewModel.selectDeparturePlace(place)
                            } label: {
                                HStack(spacing: 12) {
                                    Image(systemName: place.iconName)
                                        .foregroundStyle(OdiyaColors.primary)
                                        .frame(width: 24)
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(place.label)
                                            .font(.subheadline)
                                            .fontWeight(.medium)
                                            .foregroundStyle(.primary)
                                        Text(place.address)
                                            .font(.caption)
                                            .foregroundStyle(.secondary)
                                            .lineLimit(1)
                                    }
                                    Spacer()
                                    if viewModel.departurePlace == place {
                                        Image(systemName: "checkmark.circle.fill")
                                            .foregroundStyle(OdiyaColors.primary)
                                    }
                                }
                                .padding(.horizontal, 14)
                                .padding(.vertical, 12)
                                .contentShape(Rectangle())
                            }
                            .buttonStyle(.plain)

                            if place.id != viewModel.departurePlaces.last?.id {
                                Divider().padding(.leading, 50)
                            }
                        }
                    }
                    .background(Color(.systemBackground))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color(.systemGray4), lineWidth: 1))

                    // 다른 장소에서 출발
                    Button {
                        viewModel.showDeparturePlaceSearch = true
                    } label: {
                        HStack(spacing: 8) {
                            Image(systemName: "magnifyingglass")
                            Text("다른 장소에서 출발")
                        }
                        .font(.subheadline)
                        .fontWeight(.medium)
                        .foregroundStyle(OdiyaColors.primary)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .background(OdiyaColors.odiya100)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                    }
                    .buttonStyle(.plain)

                    // 나중에 지정
                    Button {
                        viewModel.clearDeparturePlace()
                    } label: {
                        HStack(spacing: 8) {
                            Image(systemName: "clock")
                            Text("나중에 지정")
                        }
                        .font(.subheadline)
                        .fontWeight(.medium)
                        .foregroundStyle(viewModel.departurePlace == nil ? OdiyaColors.primary : .secondary)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .background(viewModel.departurePlace == nil ? OdiyaColors.odiya100 : Color(.systemGray6))
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                    }
                    .buttonStyle(.plain)
                }

                Spacer(minLength: 24)
            }
            .padding(.horizontal, 20)
            .padding(.top, 24)
        }
        .sheet(isPresented: $viewModel.showDeparturePlaceSearch) {
            DeparturePlaceSearchView { place in
                viewModel.selectDeparturePlace(place)
            }
        }
    }
}

// MARK: - SummaryRow

private struct SummaryRow: View {
    let icon: String
    let label: String
    let value: String

    var body: some View {
        HStack(alignment: .top, spacing: 10) {
            Image(systemName: icon)
                .font(.system(size: 15))
                .foregroundStyle(OdiyaColors.primary)
                .frame(width: 24)

            Text(label)
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .frame(width: 60, alignment: .leading)

            Text(value)
                .font(.subheadline)
                .fontWeight(.medium)
                .foregroundStyle(.primary)
                .multilineTextAlignment(.leading)

            Spacer()
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 12)
    }
}

// MARK: - TransportButton

private struct TransportButton: View {
    let type: TransportType
    let isSelected: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            VStack(spacing: 4) {
                Image(systemName: type.iconName)
                    .font(.system(size: 18))
                Text(type.shortName)
                    .font(.caption2)
                    .fontWeight(.medium)
                    .lineLimit(1)
                    .minimumScaleFactor(0.7)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 10)
            .background(isSelected ? OdiyaColors.primary : Color(.systemGray6))
            .foregroundStyle(isSelected ? .white : .secondary)
            .clipShape(RoundedRectangle(cornerRadius: 10))
            .overlay(
                RoundedRectangle(cornerRadius: 10)
                    .stroke(isSelected ? OdiyaColors.primary : Color.clear, lineWidth: 1.5)
            )
        }
        .buttonStyle(.plain)
        .animation(.easeInOut(duration: 0.15), value: isSelected)
    }
}

#Preview {
    Step4ConfirmView(viewModel: CreateAppointmentViewModel())
}
