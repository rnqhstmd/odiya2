import SwiftUI
import UIKit

/// 디자인 시스템 v2 타이포그래피 토큰.
///
/// - **한글**: Pretendard Variable (번들에 임베드 필요 — `UIAppFonts` 등록 시 자동 사용)
/// - **영문·숫자**: SF Pro (iOS 시스템 기본)
/// - **숫자 정렬**: 시간·카운트다운·거리 등 tabular 한 숫자에 `.monospacedDigit()` 필수
///
/// Pretendard 폰트 파일이 번들에 없으면 시스템 폰트로 자동 폴백. 런타임 시작 시
/// `isPretendardAvailable` 한 번만 체크되어 이후 호출에선 캐시된 값을 사용한다.
enum OdiyaTypography {

    // MARK: - Pretendard 폰트 패밀리

    private enum PretendardFace: String {
        case regular  = "Pretendard-Regular"
        case medium   = "Pretendard-Medium"
        case semibold = "Pretendard-SemiBold"
        case bold     = "Pretendard-Bold"
        case heavy    = "Pretendard-ExtraBold"
    }

    /// Pretendard 폰트가 앱 번들에 등록되어 있는지 (최초 호출 시 한 번만 평가)
    static let isPretendardAvailable: Bool = {
        UIFont(name: PretendardFace.bold.rawValue, size: 14) != nil
    }()

    /// Pretendard 가 있으면 커스텀 폰트, 없으면 시스템 폰트로 폴백.
    /// `relativeTo` 를 전달해 Dynamic Type 에 대응한다 (접근성·HIG 준수).
    private static func font(
        _ face: PretendardFace,
        size: CGFloat,
        systemWeight: Font.Weight,
        relativeTo textStyle: Font.TextStyle
    ) -> Font {
        if isPretendardAvailable {
            return .custom(face.rawValue, size: size, relativeTo: textStyle)
        }
        return .system(textStyle, design: .default).weight(systemWeight)
    }

    // MARK: - Scale (v2)

    /// 28pt / 800 — Nav 대형 타이틀 (홈/약속/친구 상단). Dynamic Type: largeTitle
    static var navLargeTitle: Font {
        font(.heavy, size: 28, systemWeight: .heavy, relativeTo: .largeTitle)
    }

    /// 20pt / 700 — 섹션 헤더. Dynamic Type: title3
    static var sectionHeader: Font {
        font(.bold, size: 20, systemWeight: .bold, relativeTo: .title3)
    }

    /// 17pt / 700 — 큰 버튼 (lg). Dynamic Type: headline
    static var buttonLg: Font {
        font(.bold, size: 17, systemWeight: .bold, relativeTo: .headline)
    }

    /// 15pt / 600 — 중간 버튼 (md). Dynamic Type: subheadline
    static var buttonMd: Font {
        font(.semibold, size: 15, systemWeight: .semibold, relativeTo: .subheadline)
    }

    /// 14pt / 600 — 작은 버튼 (sm). Dynamic Type: footnote
    static var buttonSm: Font {
        font(.semibold, size: 14, systemWeight: .semibold, relativeTo: .footnote)
    }

    /// 17pt / 400 — 본문. Dynamic Type: body
    static var body: Font {
        font(.regular, size: 17, systemWeight: .regular, relativeTo: .body)
    }

    /// 15pt / 500 — 본문 보조. Dynamic Type: subheadline
    static var bodySm: Font {
        font(.medium, size: 15, systemWeight: .medium, relativeTo: .subheadline)
    }

    /// 13pt / 600 — 라벨. Dynamic Type: footnote
    static var label: Font {
        font(.semibold, size: 13, systemWeight: .semibold, relativeTo: .footnote)
    }

    /// 12pt / 600 — 캡션·배지. Dynamic Type: caption
    static var caption: Font {
        font(.semibold, size: 12, systemWeight: .semibold, relativeTo: .caption)
    }

    /// HERO 카운트다운 숫자 — `.monospacedDigit()` 적용. Dynamic Type: largeTitle
    static var heroCountdown: Font {
        font(.heavy, size: 72, systemWeight: .heavy, relativeTo: .largeTitle).monospacedDigit()
    }

    /// HERO 카운트다운 (컴팩트) — 내 약속 카드용. Dynamic Type: title
    static var heroCountdownSm: Font {
        font(.heavy, size: 48, systemWeight: .heavy, relativeTo: .title).monospacedDigit()
    }

    /// 시간 / 거리 등 tabular 숫자가 들어가는 본문. Dynamic Type: subheadline
    static var tabularBody: Font {
        font(.semibold, size: 15, systemWeight: .semibold, relativeTo: .subheadline).monospacedDigit()
    }
}

// MARK: - Letter-spacing / line-height modifiers

extension View {
    /// v2 타이포 letter-spacing 적용 헬퍼. 스펙 값은 음수(트래킹 좁힘).
    func odiyaTracking(_ tracking: CGFloat) -> some View {
        self.tracking(tracking)
    }
}

/// v2 스펙상 자주 쓰이는 tracking 값들.
enum OdiyaTracking {
    static let navLarge: CGFloat  = -0.8
    static let section: CGFloat   = -0.5
    static let button: CGFloat    = -0.3
    static let body: CGFloat      = -0.2
    static let hero: CGFloat      = -2.0
}
