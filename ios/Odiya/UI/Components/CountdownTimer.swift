import Foundation

/// 공통 카운트다운 타이머. 30분 이내 시 초 단위, 그 외 분 단위로 갱신.
/// RunLoop.main(.common) 모드로 스크롤 중에도 동작.
@MainActor
final class CountdownTimer: ObservableObject {

    @Published private(set) var secondsRemaining: Int = 0

    let targetDate: Date
    private var timer: Timer?

    init(targetDate: Date) {
        self.targetDate = targetDate
    }

    func start() {
        updateAndReschedule()
    }

    func stop() {
        timer?.invalidate()
        timer = nil
    }

    private func updateAndReschedule() {
        timer?.invalidate()
        let seconds = Int(targetDate.timeIntervalSince(Date()))
        secondsRemaining = max(0, seconds)
        guard secondsRemaining > 0 else {
            timer = nil
            return
        }
        let interval: TimeInterval = secondsRemaining <= 1800 ? 1 : 60
        // RunLoop.main(.common) — 스크롤 중에도 갱신, 메인 스레드 보장
        let newTimer = Timer(timeInterval: interval, repeats: false) { [weak self] _ in
            Task { @MainActor in
                self?.updateAndReschedule()
            }
        }
        timer = newTimer
        RunLoop.main.add(newTimer, forMode: .common)
    }

    deinit {
        // deinit은 nonisolated — Timer는 메인 스레드에서 invalidate 필요
        let t = timer
        DispatchQueue.main.async { t?.invalidate() }
    }
}
