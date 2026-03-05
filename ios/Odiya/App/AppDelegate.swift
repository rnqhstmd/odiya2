import UIKit
import KakaoSDKCommon

class AppDelegate: NSObject, UIApplicationDelegate {

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        KakaoSDK.initSDK(appKey: AppEnvironment.current.kakaoAppKey)
        return true
    }
}
