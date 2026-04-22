# 디자인 v2 — 전면 리뉴얼 (2026-04-22)

- 작성일: 2026-04-22
- 수정일: 2026-04-22
- 관련 레포: odiya-ios
- 상위 문서: [디자인 & UX](README.md)

---

## 리뉴얼 배경

Claude Design 툴(`claude.ai/design`)에서 토스 + Linear 감성으로 iOS 화면 전체를 재설계한 번들이 전달됐다. 사용자와 채팅 로그(`chats/chat1.md`)에서 확정된 방향:

- **톤**: 차분한 프리미엄 (Linear · Arc 감성)
- **밀도**: 에어리, 한 화면 한 메시지
- **시그니처**: 큼직한 출발 카운트다운 HERO · 재촉 CTA 강조 · 태그 색 = 앱 개인화 액센트
- **Variants**: 주요 화면에 A/B/C 탐색 포함 — 프로덕션엔 하나만 채택 (아래 참조)
- **Apple 로그인 제거** — 카카오만 연동됨
- **재촉 시트** = 옵션 B (누구 재촉할지만 선택, 메시지 선택지는 스펙 외라 제거)
- **다크모드** — 전체 화면 지원 (SwiftUI `colorScheme` 기반)

원본 번들 위치: `/tmp/odiya-design-v2/extracted/odiya/` (세션 한정, 영구 저장이 필요하면 `references/design-v2-bundle/` 로 옮길 것).

---

## 채택한 변형 (A/B/C 중)

| 화면 | 채택 | 근거 |
|------|------|------|
| 캘린더 월간 뷰 | **A · 리치 이벤트 프리뷰** | 토스 미니멀(B)은 정보량 부족, 타임라인(C)은 월간엔 부적합. A가 "약속 관리" 핵심 정보(태그·제목·시간)를 한눈에 제공 |
| 약속 상세 HERO | **B · 지도 풀블리드 + 글래스 카드** | 오디야의 핵심이 "언제 출발해야 해 + 어디로" 이므로 지도 일체감이 시그니처. 거대숫자(A)는 카운트다운 카드로 흡수, 링(C)은 과한 장식 |

나머지 화면은 번들과 동일 구조로 1개 버전만 존재.

---

## 컬러 시스템 v2

v1 단일 보라 팔레트 → 깊은 보라 + 모던 그레이 + 산호 액센트의 3축 체계로 확장. 전 스케일은 `tokens-v2.jsx` 를 그대로 SwiftUI 토큰으로 포팅한다.

### Primary — 깊은 보라 (11단)

| 토큰 | 값 | 용도 |
|------|----|------|
| `p50`  | `#F8F5FC` | 초희미 배경 틴트 |
| `p100` | `#F1EAF9` | Surface (선택된 날짜, 임박 카드 배경) |
| `p200` | `#E3D5F2` | |
| `p300` | `#C9B3E8` | Tertiary (배지, 선택 하이라이트) |
| `p400` | `#A78BDA` | 태그 라벤더 solid |
| `p500` | `#8B5FBF` | Secondary — 보조 버튼, 프레스 |
| `p600` | `#6B46C1` | Interactive 텍스트 · 탭바 활성 |
| `p700` | `#4C2A8F` | **Primary** — 메인 CTA, 강조 |
| `p800` | `#3A1F6B` | |
| `p900` | `#261648` | 다크 배경 위 Primary |
| `p950` | `#1A0E33` | 그라데이션 끝 |

### Neutrals — 모던 그레이 (13단)

`g50`~`g950` · 토스 스타일 warm-cool 그레이. `g100`·`g150` 은 보조 카드/시트 배경, `g500`~`g600` 은 보조 텍스트, `g800`~`g950` 은 다크 배경.

### Accent — 재촉/긴급 전용

| 토큰 | 값 | 용도 |
|------|----|------|
| `accent`     | `#FF5E7A` | 재촉 CTA, 긴급 강조 (v1 Nudge `#D94F8A`를 산호쪽으로 이동) |
| `accentDeep` | `#E23D5E` | 프레스 상태 |
| `accentSoft` | `#FFE4E9` | 재촉 시트 하이라이트 |

### Functional

| 토큰 | 값 | 용도 |
|------|----|------|
| `success` | `#00C896` | 확정, 완료 |
| `warning` | `#FFA61E` | 출발 임박 |
| `danger`  | `#FF4D4F` | 에러, 취소, 삭제 |
| `info`    | `#2E7CF6` | |
| `kakao`   | `#FEE500` | 카카오 로그인 (고정) |

### Surfaces (라이트 / 다크)

| 토큰 | Light | Dark |
|------|-------|------|
| bg   | `#FAFAFC` | `#0B0B11` |
| card | `#FFFFFF` | `#17171E` |
| card2 | `g100`    | `#1F1F28` |
| sep  | `rgba(17,17,28,0.07)` | `rgba(255,255,255,0.08)` |
| text | `#0B0B11` | `#F4F4F7` |
| text2 | `#5C5C68` | `rgba(244,244,247,0.65)` |
| text3 | `#9090A0` | `rgba(244,244,247,0.4)` |

### 태그 팔레트 (친구 태그 = 앱 개인화 액센트)

| 키 | 이름 | solid | soft |
|----|------|-------|------|
| `lavender` | 라벤더 | `#A78BDA` | `#EDE4FA` |
| `rose`     | 로즈   | `#FF8FA3` | `#FFE0E7` |
| `sky`      | 스카이 | `#66B2FF` | `#DCEEFF` |
| `mint`     | 민트   | `#3ECFAE` | `#D4F5EC` |
| `amber`    | 앰버   | `#FFB547` | `#FFEACC` |
| `coral`    | 코랄   | `#FF7A6B` | `#FFE1DD` |
| `olive`    | 올리브 | `#8FAD62` | `#E5EED6` |
| `slate`    | 슬레이트 | `#8B94A5` | `#E4E8EE` |

선택된 친구 태그의 `solid` 가 해당 약속 카드의 좌측 컬러바, 아바타 그라데이션, 캘린더 도트, 상세 화면 액센트로 전파된다.

---

## 타이포그래피 v2

- **한글**: Pretendard Variable (이미 로딩 중)
- **영문·숫자**: SF Pro Text / SF Pro Display (iOS 시스템)
- **숫자 정렬**: 모든 시간·카운트다운·거리 숫자에 `tabular-nums` (SwiftUI `.monospacedDigit()`) 적용

| 역할 | 크기 | 굵기 | letterSpacing | 비고 |
|------|------|------|---------------|------|
| 큰 타이틀 (Nav large) | 28 | 800 | -0.8 | 홈/약속/친구 상단 |
| 섹션 헤더 | 20 | 700 | -0.5 | |
| HERO 카운트다운 숫자 | 72~84 | 800 | -2 | tabular-nums 필수 |
| Body | 15~17 | 400~500 | -0.2 | |
| 라벨/캡션 | 12~13 | 600 | -0.2 | |
| 버튼 (lg) | 17 | 700 | -0.3 | |

---

## 레이아웃 · 모션 · 섀도

- **Radius**: xs 6 / sm 10 / **md 14** / lg 18 / xl 22 / **xxl 28** / pill 999 — 카드는 xxl, 버튼은 md~lg
- **Spacing**: 4/8/12/16/20/24/32/40/56 스케일
- **Shadow (라이트)**
  - `card`: `0 1px 2px rgba(17,17,28,.04), 0 4px 12px rgba(17,17,28,.04)`
  - `lift`: `0 2px 4px rgba(17,17,28,.05), 0 12px 32px rgba(17,17,28,.08)`
  - `hero`: `0 4px 10px rgba(76,42,143,.12), 0 20px 48px rgba(76,42,143,.22)` — HERO 카드 전용
  - `glow`: `0 8px 24px rgba(255,94,122,.35)` — 재촉 CTA 전용
- **다크 섀도**: 보더 톤으로 대체 (`0 0 0 1px rgba(255,255,255,.04)` + 미세 블랙 섀도)
- **블러 (탭바/알림 시트)**: `backdropFilter: blur(24px) saturate(180%)` — SwiftUI 에선 `.ultraThinMaterial`

---

## 다크모드 전체 지원 방침

1. `OdiyaColors` 를 **시맨틱 토큰 구조**로 재구성. 모든 사용처는 하드코딩 hex 대신 시맨틱 이름만 참조.
   - `Color.odiya.primary` (Light=`p700`, Dark=`p500`)
   - `Color.odiya.background` / `.surface` / `.surface2` / `.separator` / `.text` / `.text2` / `.text3`
   - `Color.odiya.accent` (재촉) / `.accentGlow`
2. 자산은 Asset Catalog 의 **Any / Dark** appearance 로 각 토큰 정의.
3. SwiftUI 뷰는 `@Environment(\.colorScheme)` 에 의존하지 않고 토큰만으로 두 모드 자동 대응.
4. 지도 렌더링: 카카오맵 dark 스타일 적용 여부를 `KakaoMapView` 에서 스킴에 맞춰 스위치.
5. 상태바 `.preferredColorScheme` 는 건드리지 않고 시스템 스킴을 따른다.

---

## 주요 패턴 변경

### 로그인 — Apple 제거

- 상단 `ODIYA` 로고 + 보라 그라데이션 원형 배경 유지
- 카카오 로그인 버튼만 (노란색 `#FEE500`, 풀블리드, `r=lg`)
- 하단 약관 문구는 유지
- 상태바 클리핑 이슈 해결됨 (상단 padding 64, 상태바 높이 62 — `common-v2.jsx` 반영)

### 재촉 시트 — 옵션 B (스펙 충실)

- 제목 "재촉하기"
- "아직 출발 안 한 친구" 만 선택 가능 · 이미 출발/도착한 친구는 disabled
- **메시지 선택지 제거** (스펙상 푸시 문구는 서버에서 고정: `{이름}이(가) 출발하래요!`)
- 5분 쿨다운 안내 문구 inline
- CTA: "N명 재촉하기" (선택 인원 수 반영)
- 전송 후 Haptic `.impact(.medium)` + bounce + 체크마크 → 쿨다운 타이머(원형 프로그레스)

### 캘린더 — 리치 프리뷰 (Variant A)

- 월/주 토글 **제거** (v2 에선 월간 단독, 주간은 약속 리스트 섹션으로 흡수)
- 요일 헤더 · 날짜 셀 · 태그 도트 최대 3개(초과 시 `+N`)
- 선택된 날짜: `p100` 배경 + `p700` 텍스트
- 하단: 선택 날짜 약속 리스트 (풀 카드, 태그 좌측 컬러바 + 시간 + 참여자 스택)

### 약속 상세 — 지도 풀블리드 + 글래스 (Variant B)

- 상단 50% 지도 풀블리드 (route 라인 + 출발/도착 마커)
- 지도 위에 반투명 글래스 카드(`.ultraThinMaterial`) 띄움:
  - 카운트다운 큰 숫자 (tabular-nums)
  - 약속명 · 장소 · 태그 컬러바
  - 참여자 아바타 스택
- 하단 액션: [길찾기] [카톡공유] [재촉] — 재촉은 `accent` + glow 섀도
- 다크: 글래스 카드가 더 짙은 블러 + 보더

### 내 약속 — HERO 카운트다운

- 큰 타이틀 "내 약속"
- HERO 카드: 다음(가장 임박한) 약속만 풀카드로 — 큰 카운트다운 숫자 + 태그 컬러바
- 그 아래 스크롤 리스트: 컴팩트 카드들 (태그 좌측바 + 제목 + 시간 + 참여자)
- 세그먼트 [다가오는] [지난] 은 리스트 위로 이동 (HERO 는 "다가오는" 일 때만)

### 탭바

- 3탭 (캘린더/내 약속/친구) — 기존과 동일
- `backdropFilter blur(24px)` = SwiftUI `.ultraThinMaterial`
- 활성 아이콘: `p600` + 라벨 `fontWeight:600`

---

## v2 화면 목록 (25 아트보드 → 프로덕션 화면)

| 섹션 | 번들 아트보드 | 프로덕션 반영 | 다크 |
|------|--------------|--------------|:----:|
| 로그인 | `login`, `login-dark` | LoginView (v2 리스킨) | ✅ |
| 캘린더 | `cal-a`, `cal-a-dark` (cal-b/c 는 탐색용) | CalendarView (v2) | ✅ |
| 약속 상세 | `det-b`, `det-b-dark` (det-a/c 는 탐색용) | AppointmentDetailView (v2) | ✅ |
| 내 약속 | `appts`, `appts-dark` | MyAppointmentsView (v2) | ✅ |
| 친구 | `friends` | FriendListView (v2) | ✅ |
| 설정 | `settings` | SettingsView (v2) | ✅ |
| 약속 만들기 | `c1`~`c4` | AppointmentCreate Step1~4 (v2) | ✅ |
| 친구 추가 | `add-friend` | FriendAddView (v2) | ✅ |
| 태그 관리 | `tags` | TagManagementView (v2) | ✅ |
| 알림 시트 | `notif` | NotificationListView (v2) | ✅ |
| 재촉 시트 | `nudge` | NudgeSheet (v2, 옵션 B) | ✅ |

---

## 구현 작업 순서 (추천)

격리 원칙: 토큰 → 컴포넌트 → 작은 화면 → 시그니처 화면 → 복잡한 플로우 → 시트

| # | 단계 | 영향 파일 | 완료 기준 |
|---|------|----------|----------|
| 1 | **디자인 토큰** — 컬러/타이포/스페이싱/쉐도우 | `UI/Theme/OdiyaColors.swift` (재구성) · 신규 `OdiyaTypography.swift` · `OdiyaShadows.swift` · `OdiyaRadius.swift` · Assets Any/Dark | 토큰만으로 라이트/다크 자동 전환 |
| 2 | **공통 컴포넌트** v2 | `UI/Components/OdiyaButton.swift` (신규 또는 재정의) · `ProfileImageView` (태그 그라데이션) · `AvatarStack` (신규) · `OdiyaCard` (신규) · `OdiyaChip` (신규) · `TabBar` (`.ultraThinMaterial`) | 스토리북 대용 프리뷰 라이트/다크 확인 |
| 3 | **로그인** (Apple 제거, 리스킨) | `Features/Auth/Presentation/LoginView.swift` | 카카오 단일 버튼, v2 톤, 다크 OK |
| 4 | **내 약속 + HERO** (시그니처) | `Features/Appointment/Presentation/MyAppointmentsView.swift` · `AppointmentCardView` · 신규 `AppointmentHeroCard.swift` | HERO 카운트다운 tabular-nums |
| 5 | **약속 상세 v2 (Variant B)** | `AppointmentDetailView.swift` · 지도 overlay glass card · 재촉 버튼(accent+glow) | 지도 풀블리드 + 글래스 카드, 다크 OK |
| 6 | **캘린더 v2 (Variant A)** | `Features/Calendar/Presentation/CalendarView.swift` (월/주 토글 제거, 태그 도트 v2) | 선택 날짜 하이라이트 p100 |
| 7 | **약속 만들기 4스텝 v2** | `AppointmentCreateStep1~4View.swift` | 에어리 여백, 진행률 바 p700 |
| 8 | **친구·설정·태그 관리 v2** | `FriendListView` · `FriendAddView` · `SettingsView` · `TagManagementView` · `ProfileView` | 태그 solid 전파 확인 |
| 9 | **모달/시트 v2** | `NotificationListView` (🔔 시트) · `NudgeSheet` (옵션 B) · `ProfileMenuView` · `TagEditSheet` · `DeparturePlaceEditSheet` | 재촉 시트에서 메시지 선택 제거 |

각 단계마다 별도 커밋. 단계 3~9 은 라이트/다크 프리뷰 스크린샷을 PR에 첨부.

---

## 원본 번들 매핑

| 번들 파일 | 역할 | 참조 대상 iOS 파일 |
|----------|------|-------------------|
| `tokens-v2.jsx` | 전체 컬러·태그·폰트·스페이싱 토큰 | `OdiyaColors.swift`, `OdiyaTypography.swift` 등 |
| `common-v2.jsx` | Phone/StatusBar/TabBar/Nav/Btn/Chip/Card/Seg/Map2/Ava | `UI/Components/*` |
| `v2-calendar.jsx` | Cal_A/B/C | `CalendarView.swift` (A 채택) |
| `v2-detail.jsx` | Det_A/B/C | `AppointmentDetailView.swift` (B 채택) |
| `v2-create.jsx` | Create_1~4 + Login | `AppointmentCreateStep*.swift`, `LoginView.swift` |
| `v2-misc.jsx` | Appts/Friends/Settings/FriendAdd/TagManage/NotifSheet/NudgeSheet | 각 Feature Presentation |
| `ios-frame.jsx` | 번들 프레임 도구 | 참조 없음 (번들 전용) |
| `design-canvas.jsx` | 번들 캔버스 도구 | 참조 없음 (번들 전용) |
| `오디야 디자인 v2.html` | 엔트리 | 전체 조립 참고 |

---

## 검증 체크리스트 (각 단계 공통)

- [ ] 라이트·다크 양쪽에서 `ContrastRatio ≥ 4.5` (본문), `≥ 3` (큰 텍스트)
- [ ] Dynamic Type Large 에서 깨지지 않음
- [ ] VoiceOver 라벨/힌트 보강
- [ ] 시간·카운트다운·거리는 `tabular-nums` 적용
- [ ] 태그 색이 해당 화면 전역 액센트로 일관되게 전파
- [ ] 재촉 CTA glow 섀도 (라이트) / 보더 강조 (다크)
- [ ] 탭바 `.ultraThinMaterial` 블러 확인
