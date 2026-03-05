# odiya (어디야) 관련 프로젝트

모노레포 (`odiya2`)로 운영한다.

## 디렉토리 구조

```
odiya2/
├── ios/                          ← Xcode 프로젝트 (Swift + SwiftUI)
│   └── Odiya/
│       ├── App/                  ← AppDelegate, OdiyaApp, Environment
│       ├── Core/Models/          ← 도메인 모델, MockData
│       ├── Features/             ← 기능별 모듈 (Auth, Appointment, Friend, Calendar, Settings, Notification)
│       │   └── {Feature}/
│       │       ├── Domain/       ← UseCase, Repository 프로토콜
│       │       ├── Data/         ← Repository 구현체, DTO
│       │       └── Presentation/ ← View, ViewModel
│       └── UI/                   ← 공통 UI (Navigation, Components, Theme)
│
├── apps/                         ← Spring Boot 실행 가능 앱 (3개)
│   ├── commerce-api/             ← REST API 서버 (port 8080)
│   ├── commerce-batch/           ← Spring Batch (스케줄 작업)
│   └── commerce-streamer/        ← Kafka 컨슈머
│
├── modules/                      ← 공유 인프라 모듈
│   ├── jpa/                      ← MySQL + Hibernate + QueryDSL
│   ├── redis/                    ← Master/Replica Redis + Lettuce
│   └── kafka/                    ← Kafka 프로듀서/컨슈머
│
├── supports/                     ← 유틸리티 모듈
│   ├── jackson/                  ← JSON 직렬화
│   ├── logging/                  ← Logback + Slack 알림
│   └── monitoring/               ← Prometheus + 분산 추적
│
├── docker/
│   ├── infra-compose.yml         ← MySQL, Redis, Kafka, Kafka UI
│   └── monitoring-compose.yml    ← Prometheus, Grafana
│
└── context/                      ← 프로젝트 문서 (도메인별 context)
    └── odiya/
```

## 기술 스택

| 계층 | 기술 |
|------|------|
| iOS 앱 | Swift + SwiftUI (iOS 17+) |
| 백엔드 | Spring Boot 3.4.4, Java 21 |
| 빌드 | Gradle 8.x (Kotlin DSL), 멀티모듈 |
| DB | MySQL 8.0 (HikariCP) |
| 캐시 | Redis 7.0 (Master/Replica) |
| 이벤트 | Kafka 3.5.1 (KRaft) |
| 인증 | KakaoTalk OAuth → JWT |
| 푸시 | FCM (Firebase Cloud Messaging) |
| 모니터링 | Prometheus + Grafana |

## 배포

| 대상 | 방식 |
|------|------|
| iOS | TestFlight ($99/년) |
| 서버 | AWS EC2 |
| DB | MySQL (EC2 내 또는 RDS) |
| Redis | EC2 내 또는 ElastiCache |

## 외부 API 키

| 서비스 | 키 종류 | 사용처 | 상태 |
|--------|---------|--------|------|
| 카카오 | 네이티브 앱 키 | iOS (로그인, 지도, 공유) | ✅ 발급 완료 |
| 카카오 | REST API 키 | 백엔드 (로컬 API, 모빌리티) | ⬜ 발급 필요 |
| ODsay | API Key | 백엔드 (대중교통 이동시간) | ⬜ 발급 필요 |
| Firebase | 서비스 계정 JSON | 백엔드 (FCM 푸시 전송) | ⬜ 설정 필요 |
| Firebase | GoogleService-Info.plist | iOS (FCM 수신) | ⬜ 설정 필요 |
