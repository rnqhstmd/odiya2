<p align="center">
  <img src="https://img.shields.io/badge/iOS-16+-000000?style=for-the-badge&logo=apple&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Swift-5.9-F05138?style=for-the-badge&logo=swift&logoColor=white" />
</p>

# 어디야 (Odiya)

> **"출발했어?" 카톡은 이제 그만.**
>
> 약속 시간을 역산해 출발 알림을 보내고, 친구에게 재촉(Nudge)까지 — 지각 없는 약속의 시작.

---

## 핵심 기능

| 기능 | 설명 |
|------|------|
| **출발 알림** | 실시간 교통 정보 기반으로 이동시간을 계산하고, 출발해야 할 시점에 푸시 알림 |
| **재촉하기 (Nudge)** | "지금 어디야?" 카톡 대신 앱 내에서 한 번의 탭으로 친구를 재촉 |
| **약속 관리** | 약속 생성 · 참여 · 장소 지정을 간편하게 |
| **친구 관리** | 카카오 로그인 기반 친구 추가 및 그룹 관리 |
| **캘린더** | 내 약속 일정을 한눈에 확인 |

## 아키텍처

```
┌─────────────────────────────────────────────────────────┐
│                      iOS Client                         │
│               Swift 5.9 · SwiftUI · iOS 16+             │
│                    Kakao SDK 연동                        │
└────────────────────────┬────────────────────────────────┘
                         │ REST API
┌────────────────────────▼────────────────────────────────┐
│                   Spring Boot Backend                    │
│                                                         │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────┐ │
│  │ commerce-api │  │ commerce-batch│  │commerce-stream.│ │
│  │  (REST API)  │  │  (스케줄링)   │  │  (이벤트 처리) │ │
│  └──────┬───────┘  └──────┬───────┘  └───────┬────────┘ │
│         │                 │                  │          │
│  ┌──────▼─────────────────▼──────────────────▼────────┐ │
│  │           Modules (JPA · Redis · Kafka)             │ │
│  └────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────┐ │
│  │        Supports (Jackson · Logging · Monitoring)    │ │
│  └────────────────────────────────────────────────────┘ │
└─────────────────────────┬───────────────────────────────┘
                          │
          ┌───────────────┼───────────────┐
          ▼               ▼               ▼
       MySQL           Redis        Kakao / ODsay API
                     (캐시/세션)     (지도 · 이동시간)
```

## 프로젝트 구조

```
odiya/
├── ios/                          # iOS 앱 (SwiftUI)
│   ├── Odiya/
│   ├── OdiyaTests/
│   └── Package.swift
│
├── apps/                         # Spring Boot 실행 모듈
│   ├── commerce-api              # REST API 서버
│   ├── commerce-batch            # 배치 처리
│   └── commerce-streamer         # 이벤트 스트리밍
│
├── modules/                      # 재사용 가능한 인프라 모듈
│   ├── jpa                       # JPA 설정 + 엔티티
│   ├── redis                     # Redis 캐싱
│   └── kafka                     # Kafka 메시징
│
├── supports/                     # 부가 기능 모듈
│   ├── jackson                   # JSON 직렬화
│   ├── logging                   # 로깅
│   └── monitoring                # Prometheus + Grafana
│
└── docker/                       # 로컬 인프라
    ├── infra-compose.yml
    └── monitoring-compose.yml
```

## 시작하기

### 사전 요구사항

- Java 21+
- Docker & Docker Compose
- Xcode 15+ (iOS 개발 시)

### Backend

```bash
# 1. 인프라 실행 (MySQL, Redis, Kafka)
docker-compose -f ./docker/infra-compose.yml up -d

# 2. API 서버 실행
./gradlew :apps:commerce-api:bootRun --args='--spring.profiles.active=local'
```

### iOS

Xcode에서 `ios/Odiya.xcodeproj`를 열고 빌드합니다.

### 모니터링

```bash
docker-compose -f ./docker/monitoring-compose.yml up -d
```

Grafana: [http://localhost:3000](http://localhost:3000) (admin / admin)

## 기술 스택

| Layer | Stack |
|-------|-------|
| **iOS** | Swift 5.9 · SwiftUI · Kakao SDK |
| **Backend** | Spring Boot 3.4 · Java 21 · Spring Cloud |
| **Database** | MySQL · Redis |
| **Messaging** | Kafka |
| **Infra** | Docker Compose · Prometheus · Grafana |
| **Testing** | JUnit 5 · Testcontainers · Mockito |
| **External API** | Kakao Map · ODsay (대중교통 이동시간) |

## 라이선스

이 프로젝트에 포함된 라이선스 정보는 [LICENSE](LICENSE) 파일을 참조하세요.
