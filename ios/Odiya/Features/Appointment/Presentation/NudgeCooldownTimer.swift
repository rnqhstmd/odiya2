import Foundation

/// NudgeButton 쿨다운 상태 관리. View struct에서 Timer를 직접 관리하지 않도록 분리.
@MainActor
final class NudgeCooldownTimer: ObservableObject {

    @Published private(set) var isCooldown: Bool = false
    @Published private(set) var cooldownRemaining: Double = 0

    let cooldownDuration: Double

    private var timer: Timer?

    init(cooldownDuration: Double = 300) {
        self.cooldownDuration = cooldownDuration
    }

    func startCooldown() {
        isCooldown = true
        cooldownRemaining = cooldownDuration
        scheduleTick()
    }

    func stop() {
        timer?.invalidate()
        timer = nil
    }

    private func scheduleTick() {
        timer?.invalidate()
        guard cooldownRemaining > 0 else {
            timer = nil
            isCooldown = false
            return
        }
        // RunLoop.main(.common) — 스크롤 중에도 갱신, 메인 스레드 보장
        let newTimer = Timer(timeInterval: 1, repeats: false) { [weak self] _ in
            Task { @MainActor in
                guard let self else { return }
                self.cooldownRemaining -= 1
                self.scheduleTick()
            }
        }
        timer = newTimer
        RunLoop.main.add(newTimer, forMode: .common)
    }

    deinit {
        let t = timer
        DispatchQueue.main.async { t?.invalidate() }
    }
}
