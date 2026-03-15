import UIKit
import KakaoSDKCommon
import UserNotifications

class AppDelegate: NSObject, UIApplicationDelegate {

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        KakaoSDK.initSDK(appKey: AppEnvironment.current.kakaoAppKey)
        requestNotificationPermission(application)
        return true
    }

    // MARK: - Push Notification Permission

    private func requestNotificationPermission(_ application: UIApplication) {
        UNUserNotificationCenter.current().requestAuthorization(
            options: [.alert, .badge, .sound]
        ) { granted, _ in
            guard granted else { return }
            DispatchQueue.main.async {
                application.registerForRemoteNotifications()
            }
        }
    }

    // MARK: - APNs Token

    /// APNs 토큰을 받으면 백엔드에 디바이스 등록
    /// TODO: Firebase Messaging 연동 시 이 메서드 대신 MessagingDelegate.messaging(_:didReceiveRegistrationToken:) 사용
    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        let token = deviceToken.map { String(format: "%02.2hhx", $0) }.joined()
        Task {
            do {
                let repository = NotificationRepositoryImpl()
                try await repository.registerDevice(token: token)
            } catch {
                print("Failed to register device token: \(error.localizedDescription)")
            }
        }
    }

    func application(
        _ application: UIApplication,
        didFailToRegisterForRemoteNotificationsWithError error: Error
    ) {
        // 디바이스 토큰 등록 실패 — 시뮬레이터에서 정상
    }

    // MARK: - Logout Helper

    /// 로그아웃 시 디바이스 토큰 비활성화
    static func deactivateDeviceToken(_ token: String) {
        Task {
            do {
                let repository = NotificationRepositoryImpl()
                try await repository.deactivateDevice(token: token)
            } catch {
                print("Failed to deactivate device token: \(error.localizedDescription)")
            }
        }
    }
}
