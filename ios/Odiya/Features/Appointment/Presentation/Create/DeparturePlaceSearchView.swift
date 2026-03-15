import SwiftUI

struct DeparturePlaceSearchView: View {

    @Environment(\.dismiss) private var dismiss
    @State private var searchText: String = ""
    @State private var saveAsPlace: Bool = false
    @State private var selectedResult: SearchResult? = nil
    var onSelect: (DeparturePlace) -> Void

    private var filteredResults: [SearchResult] {
        guard !searchText.isEmpty else { return [] }
        return SearchResult.samples.filter {
            $0.name.localizedCaseInsensitiveContains(searchText) ||
            $0.address.localizedCaseInsensitiveContains(searchText)
        }
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // 검색바
                HStack(spacing: 10) {
                    Image(systemName: "magnifyingglass")
                        .foregroundStyle(.secondary)
                    TextField("장소 검색", text: $searchText)
                        .font(.subheadline)
                    if !searchText.isEmpty {
                        Button {
                            searchText = ""
                            selectedResult = nil
                        } label: {
                            Image(systemName: "xmark.circle.fill")
                                .foregroundStyle(.secondary)
                        }
                    }
                }
                .padding(.horizontal, 14)
                .padding(.vertical, 10)
                .background(Color(.systemGray6))
                .clipShape(RoundedRectangle(cornerRadius: 10))
                .padding(.horizontal, 16)
                .padding(.top, 12)

                // 검색 결과
                if filteredResults.isEmpty && !searchText.isEmpty {
                    Spacer()
                    Text("검색 결과가 없어요")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                    Spacer()
                } else {
                    List {
                        ForEach(filteredResults) { result in
                            Button {
                                selectedResult = result
                            } label: {
                                HStack(spacing: 12) {
                                    Image(systemName: "mappin.circle.fill")
                                        .foregroundStyle(OdiyaColors.primary)
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(result.name)
                                            .font(.body)
                                            .foregroundStyle(.primary)
                                        Text(result.address)
                                            .font(.caption)
                                            .foregroundStyle(.secondary)
                                    }
                                    Spacer()
                                    if selectedResult?.id == result.id {
                                        Image(systemName: "checkmark.circle.fill")
                                            .foregroundStyle(OdiyaColors.primary)
                                    }
                                }
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .listStyle(.plain)
                }

                // 저장 옵션 + 완료 버튼
                if selectedResult != nil {
                    Divider()
                    VStack(spacing: 12) {
                        Toggle(isOn: $saveAsPlace) {
                            Label("이 장소를 출발지로 저장", systemImage: "bookmark")
                                .font(.subheadline)
                        }
                        .tint(OdiyaColors.primary)

                        Button {
                            guard let result = selectedResult else { return }
                            let place = DeparturePlace(
                                id: result.id,
                                label: result.name,
                                address: result.address,
                                latitude: result.latitude,
                                longitude: result.longitude
                            )
                            onSelect(place)
                            dismiss()
                        } label: {
                            Text("출발지로 선택")
                                .font(.subheadline)
                                .fontWeight(.semibold)
                                .foregroundStyle(.white)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 14)
                                .background(OdiyaColors.primary)
                                .clipShape(RoundedRectangle(cornerRadius: 12))
                        }
                        .buttonStyle(.plain)
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
                    .background(Color(.systemBackground))
                }
            }
            .navigationTitle("출발지 검색")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("취소") { dismiss() }
                }
            }
        }
    }
}

// MARK: - SearchResult

private struct SearchResult: Identifiable {
    let id: Int64
    let name: String
    let address: String
    let latitude: Double
    let longitude: Double

    static let samples: [SearchResult] = [
        SearchResult(id: 1, name: "강남역", address: "서울 강남구 강남대로 396", latitude: 37.4979, longitude: 127.0276),
        SearchResult(id: 2, name: "서울역", address: "서울 용산구 한강대로 405", latitude: 37.5547, longitude: 126.9707),
        SearchResult(id: 3, name: "잠실역", address: "서울 송파구 올림픽로 지하 265", latitude: 37.5133, longitude: 127.1002),
        SearchResult(id: 4, name: "홍대입구역", address: "서울 마포구 양화로 160", latitude: 37.5563, longitude: 126.9236),
        SearchResult(id: 5, name: "이태원역", address: "서울 용산구 이태원로 지하 180", latitude: 37.5340, longitude: 126.9948),
    ]
}

#Preview {
    DeparturePlaceSearchView { place in
        print(place)
    }
}
