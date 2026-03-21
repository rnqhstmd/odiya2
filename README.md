# odiya (어디야)

약속 시간 역산 출발 알림 + 재촉하기(Nudge)가 핵심인 커플/친구 약속 관리 앱.

## 프로젝트 구조

```
odiya2/
├── backend/          ← Spring Boot 백엔드 (멀티모듈 Gradle)
│   ├── apps/         ← 실행 가능한 Spring Boot 애플리케이션
│   │   ├── 📦 odiya-api       (REST API 서버)
│   │   ├── 📦 odiya-batch     (스케줄러)
│   │   └── 📦 odiya-streamer  (Kafka 컨슈머 + FCM)
│   ├── modules/      ← 공유 인프라 모듈
│   │   ├── 📦 jpa, 📦 redis, 📦 kafka
│   ├── supports/     ← 유틸리티 모듈
│   │   ├── 📦 jackson, 📦 monitoring, 📦 logging
│   └── docker/       ← Docker Compose (MySQL, Redis, Kafka)
├── ios/              ← SwiftUI iOS 앱
├── context/          ← 도메인 컨텍스트 (용어, 아키텍처, 상태 추적)
├── references/       ← 외부 API 규격 문서
└── docs/             ← 가이드 문서
```

## Getting Started

### 인프라 실행
```shell
docker-compose -f ./backend/docker/infra-compose.yml up -d
```

### 백엔드 실행
```shell
cd backend && ./gradlew :apps:odiya-api:bootRun
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

### 모니터링
```shell
docker-compose -f ./backend/docker/monitoring-compose.yml up -d
```
http://localhost:3000 로 접속 (admin/admin)

### iOS
Xcode에서 `ios/Odiya.xcodeproj` 열기 → iPhone 시뮬레이터 선택 → Cmd+R
