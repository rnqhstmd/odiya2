import SwiftUI

struct Step2PlaceView: View {

    @ObservedObject var viewModel: CreateAppointmentViewModel
    @FocusState private var isSearchFocused: Bool

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {

                // MARK: - 검색
                HStack(spacing: 10) {
                    Image(systemName: "magnifyingglass")
                        .foregroundStyle(OdiyaColors.primary)
                    TextField("장소를 검색하세요", text: $viewModel.placeSearchText)
                        .focused($isSearchFocused)
                        .onSubmit {
                            Task { await viewModel.searchPlaces() }
                        }
                    if viewModel.isSearchingPlaces {
                        ProgressView()
                            .scaleEffect(0.8)
                    } else if !viewModel.placeSearchText.isEmpty {
                        Button {
                            viewModel.placeSearchText = ""
                            viewModel.searchedPlaces = []
                        } label: {
                            Image(systemName: "xmark.circle.fill")
                                .foregroundStyle(.secondary)
                        }
                    }
                }
                .padding(.horizontal, 14)
                .padding(.vertical, 12)
                .background(Color(.systemGray6))
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(isSearchFocused ? OdiyaColors.primary : Color.clear, lineWidth: 1.5)
                )
                .onChange(of: viewModel.placeSearchText) { newValue in
                    if newValue.isEmpty {
                        viewModel.searchedPlaces = []
                    }
                }

                // MARK: - 지도 플레이스홀더
                ZStack {
                    Rectangle()
                        .fill(OdiyaColors.odiya100)
                        .frame(height: 160)
                        .clipShape(RoundedRectangle(cornerRadius: 14))
                        .overlay(
                            RoundedRectangle(cornerRadius: 14)
                                .stroke(OdiyaColors.odiya300, lineWidth: 1)
                        )

                    VStack(spacing: 8) {
                        Image(systemName: "mappin.circle.fill")
                            .font(.system(size: 36))
                            .foregroundStyle(OdiyaColors.primary)

                        if let place = viewModel.selectedPlace {
                            Text(place.name)
                                .font(.subheadline)
                                .fontWeight(.semibold)
                                .foregroundStyle(OdiyaColors.primary)
                            Text(place.roadAddress ?? place.address)
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        } else {
                            Text("장소를 선택하면 여기에 표시됩니다")
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                        }
                    }
                }

                // MARK: - 검색 결과
                if !viewModel.filteredPlaces.isEmpty {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("검색 결과")
                            .font(.caption)
                            .fontWeight(.semibold)
                            .foregroundStyle(.secondary)
                            .padding(.horizontal, 4)

                        VStack(spacing: 0) {
                            ForEach(viewModel.filteredPlaces) { place in
                                PlaceRow(
                                    place: place,
                                    isSelected: viewModel.selectedPlace == place
                                ) {
                                    viewModel.selectedPlace = place
                                    isSearchFocused = false
                                }

                                if place.id != viewModel.filteredPlaces.last?.id {
                                    Divider().padding(.leading, 48)
                                }
                            }

                            if viewModel.hasNextPlaces {
                                Button {
                                    Task { await viewModel.loadMorePlaces() }
                                } label: {
                                    Text("더 보기")
                                        .font(.subheadline)
                                        .foregroundStyle(OdiyaColors.primary)
                                        .frame(maxWidth: .infinity)
                                        .padding(.vertical, 10)
                                }
                            }
                        }
                        .background(Color(.systemBackground))
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(Color(.systemGray4), lineWidth: 1)
                        )
                    }
                }

                Spacer(minLength: 24)
            }
            .padding(.horizontal, 20)
            .padding(.top, 24)
        }
        .onTapGesture { isSearchFocused = false }
    }
}

// MARK: - PlaceRow

private struct PlaceRow: View {
    let place: PlaceResponseDTO
    let isSelected: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 14) {
                Image(systemName: isSelected ? "mappin.circle.fill" : "mappin.circle")
                    .font(.system(size: 22))
                    .foregroundStyle(isSelected ? OdiyaColors.primary : .secondary)
                    .frame(width: 28)

                VStack(alignment: .leading, spacing: 2) {
                    Text(place.name)
                        .font(.subheadline)
                        .fontWeight(isSelected ? .semibold : .regular)
                        .foregroundStyle(isSelected ? OdiyaColors.primary : .primary)
                    Text(place.roadAddress ?? place.address)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }

                Spacer()

                if isSelected {
                    Image(systemName: "checkmark")
                        .font(.caption)
                        .fontWeight(.bold)
                        .foregroundStyle(OdiyaColors.primary)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }
}

#Preview {
    Step2PlaceView(viewModel: CreateAppointmentViewModel())
}
