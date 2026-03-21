import SwiftUI

struct TagEditSheet: View {

    @Environment(\.dismiss) private var dismiss

    let existingTag: Tag?
    /// name, colorHex
    let onSave: (String, String) -> Void

    @State private var name: String
    @State private var selectedColor: Color
    @State private var selectedPresetIndex: Int?
    @State private var showColorPicker = false

    private let columns = Array(repeating: GridItem(.flexible()), count: 6)

    init(existingTag: Tag?, onSave: @escaping (String, String) -> Void) {
        self.existingTag = existingTag
        self.onSave = onSave
        _name = State(initialValue: existingTag?.name ?? "")
        _selectedColor = State(initialValue: existingTag?.color ?? OdiyaColors.tagPresets[0])
    }

    var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 24) {
                // MARK: - 태그 이름
                VStack(alignment: .leading, spacing: 8) {
                    Text("태그 이름")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundStyle(.secondary)
                    TextField("태그 이름 입력", text: $name)
                        .padding(12)
                        .background(Color(.systemGray6))
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                }

                // MARK: - 색상 팔레트
                VStack(alignment: .leading, spacing: 12) {
                    Text("색상")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundStyle(.secondary)

                    LazyVGrid(columns: columns, spacing: 12) {
                        ForEach(Array(OdiyaColors.tagPresets.enumerated()), id: \.offset) { index, color in
                            Button {
                                selectedPresetIndex = index
                                selectedColor = color
                            } label: {
                                ZStack {
                                    Circle()
                                        .fill(color)
                                        .frame(width: 44, height: 44)
                                    if selectedPresetIndex == index {
                                        Image(systemName: "checkmark")
                                            .font(.caption)
                                            .fontWeight(.bold)
                                            .foregroundStyle(.white)
                                    }
                                }
                            }
                            .buttonStyle(.plain)
                        }
                    }

                    Button {
                        showColorPicker = true
                    } label: {
                        HStack(spacing: 8) {
                            Circle()
                                .fill(selectedPresetIndex == nil ? selectedColor : Color(.systemGray4))
                                .frame(width: 20, height: 20)
                            Text("커스텀 색상")
                                .font(.subheadline)
                                .foregroundStyle(OdiyaColors.primary)
                        }
                    }
                    .sheet(isPresented: $showColorPicker) {
                        VStack(spacing: 24) {
                            Text("커스텀 색상")
                                .font(.headline)
                                .padding(.top, 24)
                            ColorPicker("색상 선택", selection: $selectedColor, supportsOpacity: false)
                                .labelsHidden()
                                .scaleEffect(2)
                                .frame(height: 200)
                            Button("적용") {
                                selectedPresetIndex = nil
                                showColorPicker = false
                            }
                            .buttonStyle(.borderedProminent)
                            .tint(OdiyaColors.primary)
                            Spacer()
                        }
                        .padding()
                        .presentationDetents([.medium])
                    }
                }

                Spacer()

                // MARK: - 저장 버튼
                Button {
                    saveTag()
                } label: {
                    Text("저장")
                        .fontWeight(.semibold)
                        .frame(maxWidth: .infinity)
                        .frame(height: 50)
                        .background(name.trimmingCharacters(in: .whitespaces).isEmpty ? Color(.systemGray4) : OdiyaColors.primary)
                        .foregroundStyle(.white)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                }
                .disabled(name.trimmingCharacters(in: .whitespaces).isEmpty)
            }
            .padding(24)
            .navigationTitle(existingTag == nil ? "태그 추가" : "태그 편집")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("취소") { dismiss() }
                }
            }
        }
        .presentationDetents([.medium])
    }

    private func saveTag() {
        let colorHex = selectedColor.toHexString()
        onSave(name.trimmingCharacters(in: .whitespaces), colorHex)
        dismiss()
    }
}

// MARK: - Color Hex Helper

private extension Color {
    func toHexString() -> String {
        let uiColor = UIColor(self)
        var r: CGFloat = 0, g: CGFloat = 0, b: CGFloat = 0, a: CGFloat = 0
        uiColor.getRed(&r, green: &g, blue: &b, alpha: &a)
        let ri = Int(r * 255), gi = Int(g * 255), bi = Int(b * 255)
        return String(format: "#%02X%02X%02X", ri, gi, bi)
    }
}

#Preview {
    TagEditSheet(existingTag: nil) { _, _ in }
}
