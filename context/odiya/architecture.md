# odiya (어디야) 아키텍처

> 전체 구조 요약과 주제별 상세 문서 링크를 관리합니다.

## 시스템 구조

| 계층 | 기술 | 비고 |
|------|------|------|
| iOS 앱 | Swift + SwiftUI (iOS 17+) | Clean Architecture + MVVM |
| 지도 | 카카오맵 iOS SDK | 네이티브 렌더링 |
| 장소 검색 | 카카오 로컬 API | 백엔드 프록시 |
| 이동시간 (자동차) | 카카오모빌리티 자동차 길찾기 API | 실시간 교통 반영 |
| 이동시간 (대중교통) | ODsay 대중교통 API | door-to-door |
| 이동시간 (도보) | 직선거리 기반 추정 | API 미사용 |
| 인증 | KakaoTalk SDK → JWT | accessToken → 백엔드 → JWT |
| 백엔드 | Spring Boot 3.4.4 (Java 21) | 멀티모듈 Gradle |
| DB | MySQL 8.0 | HikariCP, QueryDSL |
| 캐시 | Redis 7.0 (Master/Replica) | 이동시간 캐시, 재촉 쿨다운, RefreshToken |
| 이벤트 | Kafka 3.5.1 | KRaft, 배치 리스너 |
| 푸시 | FCM (Firebase Cloud Messaging) | APNs 경유 |
| 모니터링 | Prometheus + Grafana | Micrometer 기반 |
| 스케줄러 | Spring Batch | 이동시간 재계산, 출발 알림 |
| 배포 (서버) | AWS EC2 | |
| 배포 (앱) | TestFlight | |

## 백엔드 모듈 구조

```
odiya2/
├── apps/
│   ├── commerce-api          ← REST API 서버 (port 8080)
│   │   └── interfaces/api/   ← Controller, DTO, ApiResponse
│   │   └── application/      ← Facade (오케스트레이션)
│   │   └── domain/           ← Service, Entity, Repository 인터페이스
│   │   └── infrastructure/   ← Repository 구현체, 외부 API 클라이언트
│   │   └── config/           ← Security, Swagger
│   ├── commerce-batch        ← Spring Batch (스케줄 작업)
│   └── commerce-streamer     ← Kafka 컨슈머
├── modules/
│   ├── jpa                   ← MySQL + Hibernate + QueryDSL + BaseEntity
│   ├── redis                 ← Master/Replica Redis + Lettuce
│   └── kafka                 ← 프로듀서/컨슈머 설정
└── supports/
    ├── jackson               ← JSON 직렬화
    ├── logging               ← Logback + Slack 알림
    └── monitoring            ← Prometheus + 분산 추적
```

### 레이어 구조 (commerce-api)

```
Controller (@RestController)
    ↓ DTO 변환
Facade (@Component)
    ↓ 비즈니스 오케스트레이션
Service (@Service)
    ↓ 도메인 로직
Repository (interface)
    ↓ 구현체
Infrastructure (JPA, Redis, 외부 API)
```

### 현재 구현 상태

| 도메인 | Controller | Facade | Service | Entity | 상태 |
|--------|-----------|--------|---------|--------|------|
| Auth | AuthV1Controller | AuthFacade | TokenService | - | ✅ 완료 |
| User | UserV1Controller | UserFacade | UserService | User | ✅ 완료 |
| Friend | FriendV1Controller | FriendFacade | FriendService | Friendship | ✅ 완료 |
| Tag | TagV1Controller | FriendFacade | TagService | Tag | ✅ 완료 |
| Appointment | - | - | - | - | ⬜ 미구현 |
| DeparturePlace | DeparturePlaceV1Controller | DeparturePlaceFacade | DeparturePlaceService | DeparturePlace | ✅ 완료 |
| UserSettings | UserSettingsV1Controller | UserSettingsFacade | UserSettingsService | UserSettings | ✅ 완료 |
| Notification | - | - | - | - | ⬜ 미구현 |
| Place (검색) | - | - | - | - | ⬜ 미구현 |
| TravelTime | - | - | - | - | ⬜ 미구현 |

## 외부 API 연동

| 서비스 | 호출 위치 | 인증 헤더 | 용도 |
|--------|----------|----------|------|
| 카카오 사용자 API | 백엔드 | `Bearer {사용자accessToken}` | 로그인 시 사용자 정보 조회 |
| 카카오 로컬 API | 백엔드 | `KakaoAK {REST_API_KEY}` | 장소 검색 (키워드 → 좌표) |
| 카카오모빌리티 API | 백엔드 | `Authorization: KakaoAK {REST_API_KEY}` | 자동차 이동시간 |
| ODsay API | 백엔드 | Query param `apiKey` | 대중교통 이동시간 |
| FCM (Firebase) | 백엔드 | 서비스 계정 JSON | 푸시 알림 전송 |
| 카카오맵 SDK | iOS | 네이티브 앱 키 (SDK init) | 지도 렌더링 |
| 카카오 로그인 SDK | iOS | 네이티브 앱 키 (SDK init) | 소셜 로그인 |
| 카카오 ShareApi | iOS | 네이티브 앱 키 (SDK init) | 약속 초대 공유 |

## API 응답 규격

모든 API는 `ApiResponse<T>` 래퍼를 사용한다:

```json
{
  "meta": {
    "result": "SUCCESS",
    "errorCode": null,
    "message": null
  },
  "data": { ... }
}
```

에러 시:

```json
{
  "meta": {
    "result": "FAIL",
    "errorCode": "USER_NOT_FOUND",
    "message": "사용자를 찾을 수 없습니다."
  },
  "data": null
}
```

## 주제 문서

| 주제 | 설명 |
|------|------|
| [약속 관리](약속-관리/) | 약속 CRUD, 초대 수락/거절, 상태 관리, 약속 상세 API |
| [출발·이동시간](출발-이동시간/) | 출발지 CRUD, 이동시간 계산/재계산, 사용자 설정 API |
| [푸시 알림](푸시-알림/) | FCM 연동, 알림 CRUD, 재촉(Nudge) API, 스케줄링 |
| [친구 관리](친구-관리/) | 친구 요청/수락, 태그 CRUD, 사용자 검색 API |
| [유저 인증](유저-인증/) | 카카오 로그인, JWT, 프로필, 탈퇴 API |
| [지도](지도/) | 장소 검색 API, 이동시간 외부 API, 지도 렌더링 |
| [내 약속](내-약속/) | 약속 목록, 재촉 버튼 API 매핑 |
| [캘린더](캘린더/) | 캘린더 데이터 조회 API 매핑 |
| [디자인 & UX](디자인-UX/) | iOS 네이티브 디자인 시스템, 화면 구성, UX 패턴 |
