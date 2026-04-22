import CoreGraphics

/// 디자인 시스템 스페이싱 토큰.
/// 하드코딩된 `.padding(16)` / `.padding(.horizontal, 20)` 대신 사용한다.
///
/// 신규 View 작성 시 이 토큰을 우선 사용하고, 값이 맞지 않으면 디자인 스펙 확인 후
/// 필요한 경우 아래 enum에 추가한다. 매직 숫자 직접 기입은 지양한다.
enum OdiyaSpacing {
    /// 2pt — 아이콘-텍스트 최소 간격 (hairline)
    static let xxs: CGFloat = 2
    /// 4pt — tight (칩 내부, 아이콘 주변)
    static let xs: CGFloat = 4
    /// 8pt — compact (작은 버튼 padding)
    static let sm: CGFloat = 8
    /// 12pt — default row 수직 패딩, 카드 내부 간격
    static let md: CGFloat = 12
    /// 16pt — default 수평 패딩, 카드 외부 간격
    static let lg: CGFloat = 16
    /// 20pt — 큰 버튼/카드 수평 패딩, 섹션 간격
    static let xl: CGFloat = 20
    /// 24pt — 섹션 상하 여백
    static let xxl: CGFloat = 24
    /// 32pt — 큰 섹션 간격
    static let xxxl: CGFloat = 32
}

/// 디자인 시스템 모서리 라운딩 토큰.
/// `.cornerRadius(14)`, `.cornerRadius(12)` 등 산재된 값을 통합한다.
///
/// v1 값(sm/md/lg/xl)은 기존 호출처 호환을 위해 유지하고, v2 전용 값은 별도 enum
/// (`OdiyaRadiusV2`)으로 제공한다. v2 스펙은 `context/odiya/디자인-UX/v2-design-renewal.md` 참조.
enum OdiyaRadius {
    /// 6pt — 작은 칩, 배지
    static let sm: CGFloat = 6
    /// 10pt — 기본 버튼, 리스트 로우
    static let md: CGFloat = 10
    /// 14pt — 카드, 큰 컨테이너
    static let lg: CGFloat = 14
    /// 20pt — 모달, 바텀시트
    static let xl: CGFloat = 20
}

/// v2 라운딩 스케일 — `tokens-v2.jsx` 의 `T2.r` 직역.
/// v1과 값이 겹치는 구간(sm=6, md=10, v2 md 14/v1 lg 14)도 있지만, 의미 구분을
/// 위해 별도 namespace 를 둔다.
enum OdiyaRadiusV2 {
    /// 6pt — 작은 배지
    static let xs: CGFloat  = 6
    /// 10pt — 작은 칩, 입력 필드
    static let sm: CGFloat  = 10
    /// 14pt — 기본 버튼 (md), 작은 카드
    static let md: CGFloat  = 14
    /// 18pt — 큰 버튼 (lg)
    static let lg: CGFloat  = 18
    /// 22pt — 모달 상단
    static let xl: CGFloat  = 22
    /// 28pt — v2 기본 카드 (에어리)
    static let xxl: CGFloat = 28
    /// 풀 라운드 (pill)
    static let pill: CGFloat = 999
}
