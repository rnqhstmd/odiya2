import SwiftUI

struct OdiyaAlert {
    let title: String
    let message: String
    let primaryButton: AlertButton
    let secondaryButton: AlertButton?

    struct AlertButton {
        let title: String
        let role: ButtonRole?
        let action: () -> Void

        init(_ title: String, role: ButtonRole? = nil, action: @escaping () -> Void = {}) {
            self.title = title
            self.role = role
            self.action = action
        }
    }

    /// 확인만 있는 정보성 Alert
    static func info(title: String, message: String, onConfirm: @escaping () -> Void = {}) -> OdiyaAlert {
        OdiyaAlert(
            title: title,
            message: message,
            primaryButton: AlertButton("확인", role: .cancel, action: onConfirm),
            secondaryButton: nil
        )
    }

    /// 확인/취소가 있는 확인형 Alert
    static func confirm(title: String, message: String, confirmTitle: String = "확인", onConfirm: @escaping () -> Void) -> OdiyaAlert {
        OdiyaAlert(
            title: title,
            message: message,
            primaryButton: AlertButton(confirmTitle, role: .destructive, action: onConfirm),
            secondaryButton: AlertButton("취소", role: .cancel)
        )
    }
}
