# odiya (어디야) 구현 추적

> PRD 요구사항별 구현 상태를 추적합니다.

## 범례

- ✅ 반영됨 — 코드에 구현 완료
- 🔶 iOS UI만 — iOS 마크업 완료, 백엔드 미구현 (Mock 데이터)
- ⬜ 미반영 — 정책/설계만 확정, 코드 미구현

---

## 유저 인증

| 항목 | iOS | 백엔드 | 상태 |
|------|:---:|:------:|------|
| KakaoTalk OAuth 로그인 | ✅ | ✅ | 완료 |
| JWT 발급 (access 1시간 + refresh 30일) | - | ✅ | 백엔드 완료 |
| 카카오 닉네임·프로필 이미지 연동 | - | ✅ | 백엔드 완료 |
| 앱 내 프로필 변경 (닉네임, 이미지) | ✅ | ✅ | 완료 — PR #32 (S3 Presigned URL) |
| 탈퇴 시 즉시 데이터 삭제 | ✅ | ✅ | 완료 — PR #32 |

---

## 약속 관리

| 항목 | iOS | 백엔드 | 상태 |
|------|:---:|:------:|------|
| 약속 생성 (이름·장소·날짜/시간·참여자) | ✅ | ✅ | 완료 — PR #12 iOS 연동 |
| 약속 초대 → 참여자 푸시 알림 발송 | - | ✅ | 백엔드 완료 — `AppointmentFacade.create()` |
| 초대 수락/거부 (각자 앱에서) | ✅ | ✅ | 완료 — PR #12 iOS 연동 |
| 미수락 약속 카카오톡 공유 | ✅ | - | 완료 — `bb191d2`, ShareApi |
| 약속 상태 전이 (PENDING → CONFIRMED → COMPLETED / CANCELLED) | - | ✅ | 백엔드 완료 |
| 호스트: 약속 수정 (약속 시간 전) | ✅ | ✅ | 완료 — PR #12 iOS 연동 |
| 호스트: 약속 취소 | ✅ | ✅ | 완료 — PR #12 iOS 연동 |
| 호스트: 추가 초대 | ✅ | ✅ | 완료 — PR #12 iOS 연동 |
| 약속 상세 화면 (지도+참여자+재촉+공유) | ✅ | ✅ | 완료 — PR #12 iOS 연동, `AppointmentDetailInfo` |
| 장소/시간 변경 시 이동시간 재계산 트리거 | - | ✅ | 백엔드 완료 — `AppointmentFacade.updateAppointment()` 캐시 무효화 + 비동기 재계산 |

---

## 출발·이동시간

| 항목 | iOS | 백엔드 | 상태 |
|------|:---:|:------:|------|
| 출발지 등록 (집/회사/기타) | ✅ | ✅ | 완료 — PR #14 iOS 연동 |
| 이동수단 4종 (CAR_PARKING / CAR_PICKUP / TRANSIT / WALKING) | ✅ | ✅ | 완료 — PR #14 iOS 연동 |
| 사용자 설정 (기본 이동수단, 주차버퍼, 여유시간) | ✅ | ✅ | 완료 — PR #14 iOS 연동 |
| 약속 생성 시 소요시간 사전 계산 & 저장 | - | ✅ | 백엔드 완료 — `AppointmentFacade.create()` → `travelTimeService.calculateAndSave()` |
| 역산 공식 (약속시간 - API소요시간 - 주차버퍼 - 여유시간) | - | ✅ | 백엔드 완료 — `TravelTimeService`, `TravelTime.departureAlertAt` |
| 당일 자동차 재계산 (1시간 전 30분 간격 → 30분 전 10분 간격) | - | ✅ | 백엔드 완료 — `TravelTimeRecalcScheduler` (odiya-batch) |
| 5분+ 변동 시 참여자 알림 | - | ✅ | 백엔드 완료 — `TRAVEL_TIME_CHANGED` 알림 (5분 기준) |

---

## 푸시 알림

| 항목 | iOS | 백엔드 | 상태 |
|------|:---:|:------:|------|
| FCM 디바이스 토큰 등록 | ✅ | ✅ | 완료 — PR #13 iOS 연동, `DeviceV1Controller` |
| 출발 알림 | - | ✅ | 백엔드 완료 — `DepartureReminderScheduler` (odiya-batch) |
| 이동시간 변동 알림 | - | ✅ | 백엔드 완료 — `TRAVEL_TIME_CHANGED` in `AppointmentFacade` |
| 리마인더 알림 (1시간 전) | - | ✅ | 백엔드 완료 — `AppointmentReminderScheduler` (odiya-batch) |
| 재촉하기 (Nudge) 알림 | ✅ | ✅ | 완료 — PR #13 iOS 연동, `NotificationFacade.nudge()` |
| 초대 알림 | - | ✅ | 백엔드 완료 — `APPOINTMENT_INVITE` in `AppointmentFacade` |
| 약속 변경/취소 알림 | - | ✅ | 백엔드 완료 — `APPOINTMENT_UPDATED`, `APPOINTMENT_CANCELLED` |
| 출발지 미등록 알림 | - | ✅ | 백엔드 완료 — `DepartureLocationMissingScheduler` (약속 2시간 전) |
| 친구 요청 알림 | - | ✅ | 백엔드 완료 — `FRIEND_REQUEST`, `FRIEND_ACCEPTED` in `FriendFacade` |
| 재촉하기 쿨다운 (동일 대상 5분) | - | ✅ | 백엔드 완료 — `NudgeCooldownRepository` (Redis) |
| 알림 목록 조회/읽음 처리 | ✅ | ✅ | 완료 — PR #13 iOS 연동, `NotificationV1Controller` |

---

## 친구 관리

| 항목 | iOS | 백엔드 | 상태 |
|------|:---:|:------:|------|
| 친구 요청 → 수락 플로우 | ✅ | ✅ | 완료 — PR #11 iOS 연동 |
| 닉네임 검색으로 사용자 찾기 | ✅ | ✅ | 완료 — PR #11 iOS 연동 |
| 양방향 친구 관계 | ✅ | ✅ | 완료 — PR #11 iOS 연동 |
| 친구 끊기 (= 차단) | ✅ | ✅ | 완료 — PR #11 iOS 연동 |
| 관계 태그 (친구/연인/가족 + 사용자 정의) | ✅ | ✅ | 완료 — PR #14 iOS 연동 |
| 태그 커스텀 색상 (프리셋 팔레트 + 컬러피커) | ✅ | ✅ | 완료 — PR #14 iOS 연동 |
| 태그 수정/삭제 (삭제 시 기본 태그로 복귀) | ✅ | ✅ | 완료 — PR #14 iOS 연동 |
| QR 코드 친구 추가 | ✅ | ✅ | 완료 — iOS 단독 구현 (`odiya://friend?userId=` 스킴, 기존 친구 요청 API 재사용) |

---

## 지도

| 항목 | iOS | 백엔드 | 상태 |
|------|:---:|:------:|------|
| 카카오맵 iOS SDK 지도 렌더링 | ✅ | - | 완료 — PR #28, `e8ae6ca` |
| 카카오 로컬 API 장소 검색 | - | ✅ | 백엔드 완료 — `PlaceV1Controller`, Redis 캐시 |
| 카카오모빌리티 API 자동차 이동시간 계산 | - | ✅ | 백엔드 완료 — `KakaoMobilityClient`, fallback 포함 |
| ODsay API 대중교통 이동시간 계산 | - | ✅ | 백엔드 완료 — `OdsayClient`, fallback 포함 |
| 도보 직선거리 기반 소요시간 추정 | - | ✅ | 백엔드 완료 — Haversine `WalkingCalculator` |
| 외부 앱 연결 (카카오맵 > 네이버맵 > 애플맵) | ✅ | - | 완료 — `bb191d2` |

---

## 내 약속

| 항목 | iOS | 백엔드 | 상태 |
|------|:---:|:------:|------|
| 다가오는/지난 약속 탭 | ✅ | ✅ | 완료 — PR #12 iOS 연동, `getMyAppointments(UPCOMING/PAST)` |
| 임박한 약속 강조 + 출발 카운트다운 | ✅ | ✅ | 완료 — PR #12 iOS 연동, `departureAlertAt` 포함 |
| 재촉 버튼 | ✅ | ✅ | 완료 — PR #12 iOS 연동, `nudge()` API |

---

## 캘린더

| 항목 | iOS | 백엔드 | 상태 |
|------|:---:|:------:|------|
| 월간 뷰 (약속 날짜 도트 표시, 태그 색상 반영) | ✅ | ✅ | 완료 — PR #15 iOS 연동, `getCalendarData()` |
| 주간 뷰 (타임라인 약속 블록) | ✅ | ✅ | 완료 — PR #15 iOS 연동 |
| 날짜 탭 → 약속 리스트 | ✅ | ✅ | 완료 — PR #15 iOS 연동 |
| 빈 날짜 탭 → 약속 생성 진입 | ✅ | - | 완료 — iOS 단독 |

---

## 디자인 & UX

### v1 / v1.5 (기반 — 완료)

| 항목 | iOS | 상태 |
|------|:---:|------|
| 3탭 네비게이션 (캘린더/약속/친구) + 🔔👤 | ✅ | 완료 |
| 컬러 시스템 (오디 보라 팔레트 v1) | ✅ | 완료 — v2 로 대체 예정 |
| 약속 생성 스텝 위저드 (Step 1~4) | ✅ | 완료 |
| Step3 카카오톡 스타일 참여자 선택 | ✅ | 완료 |
| Step4 출발지 카드 + "나중에 지정" | ✅ | 완료 |
| Alert 패턴 (토스트 제거) | ✅ | 완료 |
| 재촉하기 UX (Haptic + bounce) | ✅ | 완료 — PR #13 iOS 연동 |
| Pencil 프로토타입 (12화면) | ✅ | 완료 — 2026-03-22 제작 |
| 아바타 그라데이션 이니셜 | ✅ | 프로토타입 적용 |
| 카운트다운 배너 (핑크 그라데이션) | ✅ | 프로토타입 적용 |
| Apple 로그인 UI | - | 불필요 — 카카오 로그인만 사용 |
| QR 코드 친구 추가 UI | ✅ | 프로토타입 적용 |

### v2 전면 리뉴얼 (2026-04-22 착수 — 디자인 번들 기반)

토스 + Linear 감성 · 깊은 보라 11단 + 모던 그레이 + 산호 액센트 · Pretendard + SF Pro · 전체 다크모드 지원. 소스: Claude Design 번들 `오디야 디자인 v2.html`. 상세 스펙: [`context/odiya/디자인-UX/v2-design-renewal.md`](디자인-UX/v2-design-renewal.md).

| # | 작업 | iOS | 상태 | 비고 |
|:-:|------|:---:|:----:|------|
| 1 | 디자인 토큰 v2 (Colors/Typography/Shadows/Radius + 동적 다크) | ✅ | PR #34 | `OdiyaColors` rewrite (p/g/accent/태그 8종 + 시맨틱 동적) · `OdiyaTypography` 신규 (Pretendard+SF Pro+Dynamic Type) · `OdiyaShadows` 신규 (2-layer + colorScheme) · `OdiyaRadiusV2` 추가. Asset Catalog 대신 `Color(lightHex:darkHex:)` 헬퍼 채택 |
| 2 | 공통 컴포넌트 v2 (Button/Card/Chip/Avatar/AvatarStack/Segmented) | 🔄 | 진행 중 | `OdiyaButton`/`OdiyaCard`+`OdiyaSectionLabel`/`OdiyaChip`/`OdiyaAvatarV2`+`OdiyaAvatarStackV2`/`OdiyaSegmented` 신규. MainTabView 리스킨은 탭바를 쓰는 화면별 v2 마이그레이션(Stage 4~) 시 `.toolbarBackground(.ultraThinMaterial)` 로 처리 예정 |
| 3 | 로그인 v2 (Apple 제거, 카카오만) | ⬜ | 대기 | |
| 4 | 내 약속 + HERO 카운트다운 v2 | ⬜ | 대기 | tabular-nums 필수 |
| 5 | 약속 상세 v2 (Variant B · 지도 풀블리드 + 글래스 카드) | ⬜ | 대기 | 재촉 CTA accent+glow |
| 6 | 캘린더 v2 (Variant A · 리치 프리뷰) | ⬜ | 대기 | **월/주 토글 제거** — 월간 단독 |
| 7 | 약속 만들기 4스텝 v2 | ⬜ | 대기 | |
| 8 | 친구·설정·태그·프로필 v2 | ⬜ | 대기 | 태그 solid = 개인화 액센트 전파 |
| 9 | 모달/시트 v2 (알림·재촉 옵션 B·프로필 메뉴) | ⬜ | 대기 | 재촉 시트에서 **메시지 선택 제거** (스펙 충실) |
| - | 전체 다크모드 지원 | ⬜ | 대기 | 시맨틱 토큰만으로 자동 전환 |

---

## 전체 API 요약

| 도메인 | 구현 완료 | 미구현 | 합계 |
|--------|:--------:|:------:|:----:|
| Auth | 3 | 0 | 3 |
| User | 4 | 0 | 4 |
| Friend | 8 | 0 | 8 |
| Tag | 4 | 0 | 4 |
| Appointment | 10 | 0 | 10 |
| DeparturePlace | 4 | 0 | 4 |
| Notification | 4 | 0 | 4 |
| Device | 2 | 0 | 2 |
| Place | 1 | 0 | 1 |
| UserSettings | 1 | 0 | 1 |
| **합계** | **41** | **0** | **41** |

---

## 백엔드 개발 순서

### Phase 1: 친구 시스템 — `FriendV1Controller` + `TagV1Controller` ✅ 완료

**의존성:** User (✅ 완료)

| 작업 | API 수 |
|------|:------:|
| Friendship 엔티티 (양방향 관계, PENDING/ACCEPTED 상태) | - |
| Tag 엔티티 (기본 3개 자동 생성) | - |
| 친구 요청/수락/거절/끊기/목록 | 7 |
| 사용자 검색 (닉네임) | 1 |
| 태그 CRUD | 4 |
| **소계** | **12** |

**이유:** 약속 생성 시 참여자 선택의 선행 조건. 가장 독립적이라 먼저 착수 가능.

### Phase 2: 출발지 관리 — `DeparturePlaceV1Controller` ✅ 완료

**의존성:** User (✅ 완료)

| 작업 | API 수 |
|------|:------:|
| DeparturePlace 엔티티 | - |
| 출발지 CRUD | 4 |
| 사용자 설정 (기본 이동수단, 주차버퍼, 여유시간) | 1 |
| **소계** | **5** |

**이유:** Phase 1과 병렬 가능. 약속 생성 시 출발지 선택에 필요.

### Phase 3: 약속 CRUD + 초대 — `AppointmentV1Controller` ✅ 완료

**의존성:** User ✅, Friend (Phase 1), DeparturePlace (Phase 2)

| 작업 | API 수 |
|------|:------:|
| Appointment 엔티티 + AppointmentParticipant 엔티티 | - |
| 약속 생성/상세/수정/취소 | 4 |
| 초대 수락/거절/추가 초대 | 3 |
| 약속 목록 (upcoming/past) | 1 |
| 캘린더 데이터 조회 (monthly) | 1 |
| 출발지/이동수단 변경 | 1 |
| **소계** | **10** |

**이유:** 핵심 도메인. Phase 1~2 완료 후 착수.

### Phase 4: 장소 검색 + 이동시간 — `PlaceV1Controller` + 내부 서비스 ✅ 완료

**의존성:** Appointment (Phase 3), 외부 API 키 (카카오 REST API, ODsay)

| 작업 | API 수 |
|------|:------:|
| 카카오 로컬 API 클라이언트 (장소 검색) | 1 |
| 카카오모빌리티 API 클라이언트 (자동차 이동시간) | - (내부) |
| ODsay API 클라이언트 (대중교통 이동시간) | - (내부) |
| 도보 이동시간 계산 (Haversine) | - (내부) |
| TravelTime 엔티티 + Redis 캐시 | - |
| **소계** | **1** (+내부 서비스) |

**이유:** 외부 API 키가 필요. Phase 3의 약속 생성/변경과 연동.

### Phase 5: 푸시 알림 — `NotificationV1Controller` + `DeviceV1Controller` ✅ 완료

**의존성:** Appointment (Phase 3), Firebase 설정

| 작업 | API 수 |
|------|:------:|
| Firebase Admin SDK 연동 | - |
| DeviceToken 엔티티 | - |
| Notification 엔티티 | - |
| FCM 토큰 등록/삭제 | 2 |
| 알림 목록/읽음/전체읽음/미읽은수 | 4 |
| 재촉하기 (Nudge) + Redis 쿨다운 | 1 (내부 서비스 포함) |
| 알림 발송 서비스 (친구요청, 초대, 재촉 등 9종) | - (내부) |
| Kafka 이벤트 (발행/소비/DLQ/재시도) | - (odiya-streamer) |
| **소계** | **7** (+내부 서비스) |

### Phase 6: 스케줄링 + 재계산 — `odiya-batch` ✅ 완료

**의존성:** 전체 (Phase 1~5 완료 후)

| 작업 | 상태 |
|------|------|
| 출발 알림 스케줄러 (약속시간 - 이동시간 - 버퍼) | ✅ `DepartureReminderScheduler` |
| 당일 자동차 이동시간 재계산 (30분/10분 간격) | ✅ `TravelTimeRecalcScheduler` |
| 리마인더 알림 (약속 1시간 전) | ✅ `AppointmentReminderScheduler` |
| 출발지 미등록 알림 (약속 2시간 전) | ✅ `DepartureLocationMissingScheduler` |
| 약속 완료 상태 전이 (약속시간 경과 → COMPLETED) | ✅ `AppointmentCompletionScheduler` |
| 알림 정리 (오래된 알림 삭제) | ✅ `NotificationCleanupScheduler` |

---

## 남은 작업

PRD 기준 전 기능 구현 완료. 아래는 추가 구현 항목. (2026-03-29 기준)

| 영역 | 항목 | 상태 | 비고 |
|------|------|:----:|------|
| ~~iOS~~ | ~~앱 내 프로필 변경 (닉네임, 이미지) API 연동~~ | ✅ | 완료 — PR #32 (S3 Presigned URL 업로드) |
| ~~iOS~~ | ~~탈퇴 시 데이터 삭제 API 연동~~ | ✅ | 완료 — PR #32 (RootRouter 라우팅 연결) |
| ~~iOS~~ | ~~QR 코드 친구 추가~~ | ✅ | 완료 — PR #32 (CIQRCodeGenerator + AVCaptureSession) |
| ~~iOS~~ | ~~카카오맵 SDK 지도 렌더링~~ | ✅ | PR #28, `e8ae6ca` |
| ~~iOS~~ | ~~외부 앱 연결 (카카오맵 > 네이버맵 > 애플맵)~~ | ✅ | `bb191d2` |
| ~~iOS~~ | ~~미수락 약속 카카오톡 공유~~ | ✅ | `bb191d2`, ShareApi |

---

## Phase 1~2는 병렬 착수 가능

```
Phase 1 (친구+태그) ──┐
                       ├→ Phase 3 (약속) → Phase 4 (장소+이동시간) → Phase 6 (스케줄링)
Phase 2 (출발지)  ────┘                  → Phase 5 (푸시 알림) ──→
```
