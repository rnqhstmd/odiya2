import SwiftUI

struct Step1NameDateView: View {

    @ObservedObject var viewModel: CreateAppointmentViewModel

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 28) {

                // MARK: - 약속 이름
                VStack(alignment: .leading, spacing: 8) {
                    Label("약속 이름", systemImage: "pencil")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundStyle(OdiyaColors.primary)

                    TextField("예) 스터디 모임, 저녁 약속...", text: $viewModel.name)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 14)
                        .background(Color(.systemGray6))
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(
                                    viewModel.name.isEmpty ? Color.clear : OdiyaColors.primary,
                                    lineWidth: 1.5
                                )
                        )
                }

                // MARK: - 날짜
                VStack(alignment: .leading, spacing: 8) {
                    Label("날짜", systemImage: "calendar")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundStyle(OdiyaColors.primary)

                    DatePicker(
                        "",
                        selection: $viewModel.date,
                        displayedComponents: .date
                    )
                    .datePickerStyle(.graphical)
                    .accentColor(OdiyaColors.primary)
                    .environment(\.locale, Locale(identifier: "ko_KR"))
                    .padding(8)
                    .background(Color(.systemGray6))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }

                // MARK: - 시간
                VStack(alignment: .leading, spacing: 8) {
                    Label("시간", systemImage: "clock")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundStyle(OdiyaColors.primary)

                    DatePicker(
                        "",
                        selection: $viewModel.time,
                        displayedComponents: .hourAndMinute
                    )
                    .datePickerStyle(.wheel)
                    .accentColor(OdiyaColors.primary)
                    .environment(\.locale, Locale(identifier: "ko_KR"))
                    .labelsHidden()
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 4)
                    .background(Color(.systemGray6))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }

                Spacer(minLength: 24)
            }
            .padding(.horizontal, 20)
            .padding(.top, 24)
        }
    }
}

#Preview {
    Step1NameDateView(viewModel: CreateAppointmentViewModel())
}
