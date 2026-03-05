import SwiftUI

struct CreateAppointmentView: View {

    @StateObject private var viewModel = CreateAppointmentViewModel()
    @Environment(\.dismiss) private var dismiss

    @State private var currentStep: Int = 1
    private let totalSteps = 4

    // MARK: - Swipe gesture state
    @State private var dragOffset: CGFloat = 0

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {

                // MARK: - 프로그레스바
                GeometryReader { geo in
                    ZStack(alignment: .leading) {
                        Rectangle()
                            .fill(Color(.systemGray5))
                            .frame(height: 4)

                        Rectangle()
                            .fill(OdiyaColors.primary)
                            .frame(
                                width: geo.size.width * CGFloat(currentStep) / CGFloat(totalSteps),
                                height: 4
                            )
                            .animation(.spring(response: 0.4, dampingFraction: 0.8), value: currentStep)
                    }
                }
                .frame(height: 4)

                // MARK: - 스텝 인디케이터
                HStack {
                    ForEach(1...totalSteps, id: \.self) { step in
                        HStack(spacing: 4) {
                            Circle()
                                .fill(step <= currentStep ? OdiyaColors.primary : Color(.systemGray4))
                                .frame(width: 8, height: 8)
                            Text(stepLabel(step))
                                .font(.caption2)
                                .foregroundStyle(step == currentStep ? OdiyaColors.primary : .secondary)
                                .fontWeight(step == currentStep ? .semibold : .regular)
                        }
                        if step < totalSteps {
                            Spacer()
                        }
                    }
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 10)

                Divider()

                // MARK: - 스텝 콘텐츠
                ZStack {
                    if currentStep == 1 {
                        Step1NameDateView(viewModel: viewModel)
                            .transition(stepTransition(forward: true))
                    } else if currentStep == 2 {
                        Step2PlaceView(viewModel: viewModel)
                            .transition(stepTransition(forward: currentStep >= 2))
                    } else if currentStep == 3 {
                        Step3ParticipantsView(viewModel: viewModel)
                            .transition(stepTransition(forward: currentStep >= 3))
                    } else {
                        Step4ConfirmView(viewModel: viewModel)
                            .transition(stepTransition(forward: true))
                    }
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .gesture(
                    DragGesture(minimumDistance: 40)
                        .onEnded { value in
                            let horizontalAmount = value.translation.width
                            let verticalAmount = value.translation.height
                            guard abs(horizontalAmount) > abs(verticalAmount) else { return }

                            if horizontalAmount < 0 {
                                // 왼쪽 스와이프 → 다음
                                goNext()
                            } else {
                                // 오른쪽 스와이프 → 이전
                                goPrev()
                            }
                        }
                )

                // MARK: - 네비게이션 버튼
                Divider()
                HStack(spacing: 12) {
                    if currentStep > 1 {
                        Button {
                            withAnimation { goPrev() }
                        } label: {
                            HStack(spacing: 6) {
                                Image(systemName: "chevron.left")
                                Text("이전")
                            }
                            .font(.subheadline)
                            .fontWeight(.medium)
                            .foregroundStyle(OdiyaColors.primary)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .background(OdiyaColors.odiya100)
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                        }
                        .buttonStyle(.plain)
                        .transition(.opacity)
                    }

                    if currentStep < totalSteps {
                        Button {
                            withAnimation { goNext() }
                        } label: {
                            HStack(spacing: 6) {
                                Text("다음")
                                Image(systemName: "chevron.right")
                            }
                            .font(.subheadline)
                            .fontWeight(.semibold)
                            .foregroundStyle(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .background(canProceed ? OdiyaColors.primary : Color(.systemGray4))
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                        }
                        .buttonStyle(.plain)
                        .disabled(!canProceed)
                    } else {
                        Button {
                            viewModel.createAppointment()
                        } label: {
                            HStack(spacing: 8) {
                                Image(systemName: "checkmark.circle.fill")
                                Text("약속 만들기")
                                    .fontWeight(.bold)
                            }
                            .font(.subheadline)
                            .foregroundStyle(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .background(OdiyaColors.primary)
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 12)
                .background(Color(.systemBackground))
            }
            .navigationTitle(stepTitle)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("취소") { dismiss() }
                        .foregroundStyle(.secondary)
                }
            }
            .alert(viewModel.alertTitle, isPresented: $viewModel.showAlert) {
                Button("확인", role: .cancel) {
                    if viewModel.isCreated {
                        dismiss()
                    }
                }
            } message: {
                Text(viewModel.alertMessage)
            }
        }
    }

    // MARK: - Helpers

    private var canProceed: Bool {
        switch currentStep {
        case 1: return viewModel.canProceedToStep2
        case 2: return viewModel.canProceedToStep3
        case 3: return viewModel.canProceedToStep4
        default: return true
        }
    }

    private var stepTitle: String {
        switch currentStep {
        case 1: return "약속 이름 & 날짜"
        case 2: return "장소 선택"
        case 3: return "참여자 선택"
        case 4: return "확인 및 생성"
        default: return ""
        }
    }

    private func stepLabel(_ step: Int) -> String {
        switch step {
        case 1: return "이름/날짜"
        case 2: return "장소"
        case 3: return "참여자"
        case 4: return "확인"
        default: return ""
        }
    }

    private func stepTransition(forward: Bool) -> AnyTransition {
        .asymmetric(
            insertion: .move(edge: forward ? .trailing : .leading).combined(with: .opacity),
            removal:   .move(edge: forward ? .leading  : .trailing).combined(with: .opacity)
        )
    }

    private func goNext() {
        guard currentStep < totalSteps, canProceed else { return }
        withAnimation(.spring(response: 0.35, dampingFraction: 0.85)) {
            currentStep += 1
        }
    }

    private func goPrev() {
        guard currentStep > 1 else { return }
        withAnimation(.spring(response: 0.35, dampingFraction: 0.85)) {
            currentStep -= 1
        }
    }
}

#Preview {
    CreateAppointmentView()
}
