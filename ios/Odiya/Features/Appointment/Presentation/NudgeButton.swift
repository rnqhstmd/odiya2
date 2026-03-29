import SwiftUI

struct NudgeButton: View {

    // MARK: - Properties

    var onNudge: (() -> Void)? = nil

    // MARK: - State

    @StateObject private var cooldown = NudgeCooldownTimer()
    @State private var isPressed: Bool = false
    @State private var showCheckmark: Bool = false

    // MARK: - Body

    var body: some View {
        Button {
            guard !cooldown.isCooldown else { return }
            triggerNudge()
        } label: {
            ZStack {
                if cooldown.isCooldown {
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
        .disabled(cooldown.isCooldown)
        .scaleEffect(isPressed ? 0.85 : 1.0)
        .animation(.spring(response: 0.3, dampingFraction: 0.5), value: isPressed)
        .onDisappear { cooldown.stop() }
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
                    .trim(from: 0, to: CGFloat(cooldown.cooldownRemaining / cooldown.cooldownDuration))
                    .stroke(Color.white, lineWidth: 2)
                    .frame(width: 18, height: 18)
                    .rotationEffect(.degrees(-90))
                    .animation(.linear(duration: 1), value: cooldown.cooldownRemaining)
            }
            Text("\(Int(cooldown.cooldownRemaining / 60))분 \(Int(cooldown.cooldownRemaining) % 60)초")
                .font(.caption)
                .fontWeight(.semibold)
                .foregroundStyle(.white.opacity(0.8))
                .monospacedDigit()
        }
    }

    private var buttonBackground: Color {
        cooldown.isCooldown
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
            cooldown.startCooldown()
        }
    }
}

#Preview {
    VStack(spacing: 20) {
        NudgeButton()
    }
    .padding()
}
