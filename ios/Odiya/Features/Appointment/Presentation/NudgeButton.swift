import SwiftUI

struct NudgeButton: View {

    // MARK: - Properties

    var onNudge: (() -> Void)? = nil

    // MARK: - State

    @State private var isPressed: Bool = false
    @State private var showCheckmark: Bool = false
    @State private var isCooldown: Bool = false
    @State private var cooldownRemaining: Double = 0
    @State private var cooldownTimer: Timer? = nil

    private let cooldownDuration: Double = 300 // 5분

    // MARK: - Body

    var body: some View {
        Button {
            guard !isCooldown else { return }
            triggerNudge()
        } label: {
            ZStack {
                if isCooldown {
                    cooldownLabel
                } else if showCheckmark {
                    checkmarkLabel
                } else {
                    nudgeLabel
                }
            }
            .frame(height: 44)
            .padding(.horizontal, 20)
            .background(buttonBackground)
            .clipShape(Capsule())
        }
        .buttonStyle(.plain)
        .disabled(isCooldown)
        .scaleEffect(isPressed ? 0.85 : 1.0)
        .animation(.spring(response: 0.3, dampingFraction: 0.5), value: isPressed)
        .simultaneousGesture(
            DragGesture(minimumDistance: 0)
                .onChanged { _ in isPressed = true }
                .onEnded { _ in isPressed = false }
        )
    }

    // MARK: - Sub Views

    private var nudgeLabel: some View {
        HStack(spacing: 6) {
            Image(systemName: "bell.fill")
                .font(.subheadline)
            Text("재촉하기")
                .font(.subheadline)
                .fontWeight(.semibold)
        }
        .foregroundStyle(.white)
    }

    private var checkmarkLabel: some View {
        HStack(spacing: 6) {
            Image(systemName: "checkmark")
                .font(.subheadline)
            Text("전송됨")
                .font(.subheadline)
                .fontWeight(.semibold)
        }
        .foregroundStyle(.white)
    }

    private var cooldownLabel: some View {
        HStack(spacing: 8) {
            ZStack {
                Circle()
                    .stroke(Color.white.opacity(0.3), lineWidth: 2)
                    .frame(width: 18, height: 18)
                Circle()
                    .trim(from: 0, to: CGFloat(cooldownRemaining / cooldownDuration))
                    .stroke(Color.white, lineWidth: 2)
                    .frame(width: 18, height: 18)
                    .rotationEffect(.degrees(-90))
                    .animation(.linear(duration: 1), value: cooldownRemaining)
            }
            Text("\(Int(cooldownRemaining / 60))분 \(Int(cooldownRemaining) % 60)초")
                .font(.caption)
                .fontWeight(.semibold)
                .foregroundStyle(.white.opacity(0.8))
                .monospacedDigit()
        }
    }

    private var buttonBackground: Color {
        isCooldown
            ? OdiyaColors.nudge.opacity(0.5)
            : (showCheckmark ? OdiyaColors.success : OdiyaColors.nudge)
    }

    // MARK: - Actions

    private func triggerNudge() {
        let generator = UIImpactFeedbackGenerator(style: .medium)
        generator.impactOccurred()

        onNudge?()

        withAnimation {
            showCheckmark = true
        }

        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
            withAnimation {
                showCheckmark = false
            }
            startCooldown()
        }
    }

    private func startCooldown() {
        isCooldown = true
        cooldownRemaining = cooldownDuration

        cooldownTimer?.invalidate()
        cooldownTimer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { _ in
            Task { @MainActor in
                cooldownRemaining -= 1
                if cooldownRemaining <= 0 {
                    cooldownTimer?.invalidate()
                    cooldownTimer = nil
                    isCooldown = false
                }
            }
        }
    }
}

#Preview {
    VStack(spacing: 20) {
        NudgeButton()
    }
    .padding()
}
