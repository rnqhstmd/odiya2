import SwiftUI

struct DeparturePlaceEditSheet: View {

    @Environment(\.dismiss) private var dismiss

    let existingPlace: DeparturePlace?
    let onSave: (DeparturePlace) -> Void

    @State private var label: String
    @State private var address: String

    init(existingPlace: DeparturePlace?, onSave: @escaping (DeparturePlace) -> Void) {
        self.existingPlace = existingPlace
        self.onSave = onSave
        _label = State(initialValue: existingPlace?.label ?? "")
        _address = State(initialValue: existingPlace?.address ?? "")
    }

    private var isValid: Bool {
        !label.trimmingCharacters(in: .whitespaces).isEmpty &&
        !address.trimmingCharacters(in: .whitespaces).isEmpty
    }

    var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 24) {
                // MARK: - 라벨
                VStack(alignment: .leading, spacing: 8) {
                    Text("라벨")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundStyle(.secondary)
                    TextField("예: 집, 회사, 기타", text: $label)
                        .padding(12)
                        .background(Color(.systemGray6))
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                }

                // MARK: - 주소
                VStack(alignment: .leading, spacing: 8) {
                    Text("주소")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundStyle(.secondary)
                    HStack {
                        TextField("주소 검색 (추후 지원)", text: $address)
                        if !address.isEmpty {
                            Button {
                                address = ""
                            } label: {
                                Image(systemName: "xmark.circle.fill")
                                    .foregroundStyle(.secondary)
                            }
                        }
                    }
                    .padding(12)
                    .background(Color(.systemGray6))
                    .clipShape(RoundedRectangle(cornerRadius: 10))

                    Text("실제 주소 검색 기능은 추후 지원 예정입니다.")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }

                Spacer()

                // MARK: - 저장 버튼
                Button {
                    savePlace()
                } label: {
                    Text("저장")
                        .fontWeight(.semibold)
                        .frame(maxWidth: .infinity)
                        .frame(height: 50)
                        .background(isValid ? OdiyaColors.primary : Color(.systemGray4))
                        .foregroundStyle(.white)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                }
                .disabled(!isValid)
            }
            .padding(24)
            .navigationTitle(existingPlace == nil ? "출발지 추가" : "출발지 편집")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("취소") { dismiss() }
                }
            }
        }
        .presentationDetents([.medium])
    }

    private func savePlace() {
        let place = DeparturePlace(
            id: existingPlace?.id ?? UUID().uuidString,
            label: label.trimmingCharacters(in: .whitespaces),
            address: address.trimmingCharacters(in: .whitespaces),
            latitude: existingPlace?.latitude ?? 37.5665,
            longitude: existingPlace?.longitude ?? 126.9780
        )
        onSave(place)
        dismiss()
    }
}

#Preview {
    DeparturePlaceEditSheet(existingPlace: nil) { _ in }
}
