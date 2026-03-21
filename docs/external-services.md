# odiya 외부 서비스 & 키 관리 가이드

## 1. 외부 서비스 전체 목록

| # | 서비스 | 용도 | 필요한 키 | 발급처 | 무료 쿼터 |
|---|--------|------|----------|--------|----------|
| 1 | 카카오 로그인 API | 소셜 로그인 (사용자 정보 조회) | `KAKAO_REST_API_KEY` | developers.kakao.com | 월 300만건 |
| 2 | 카카오 로컬 API | 장소 검색 (키워드 → 좌표) | 동일 키 | 동일 | 일 10만건 |
| 3 | 카카오모빌리티 API | 자동차 이동시간 계산 | 동일 키 | 동일 | 일 1만건 |
| 4 | ODsay API | 대중교통 이동시간 계산 | `ODSAY_API_KEY` | lab.odsay.com | 일 1,000건 (6개월) |
| 5 | Firebase Admin SDK | FCM 푸시 알림 발송 | 서비스 계정 JSON | console.firebase.google.com | 무제한 |
| 6 | 카카오 SDK (iOS) | 로그인 / 카카오톡 공유 | `KAKAO_NATIVE_APP_KEY` | developers.kakao.com | - |
| 7 | 카카오맵 SDK (iOS) | 지도 렌더링 | 동일 네이티브 앱 키 | developers.kakao.com | 일 30만건 |
| 8 | APNs | iOS 푸시 수신 | APNs 인증 키 (.p8) | developer.apple.com | - |

---

## 2. 인프라 서비스

| 서비스 | 용도 | 로컬 (Docker) | 프로덕션 환경변수 |
|--------|------|--------------|------------------|
| MySQL 8.0 | 메인 DB | `localhost:3306` | `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_USER`, `MYSQL_PWD` |
| Redis 7.0 | 캐시 / 쿨다운 / 리프레시 토큰 | Master `localhost:6379`, Replica `localhost:6380` | `REDIS_MASTER_HOST/PORT`, `REDIS_REPLICA_1_HOST/PORT` |
| Kafka 3.5.1 | 알림 이벤트 발행/소비 | `localhost:19092` | `BOOTSTRAP_SERVERS` |

로컬 인프라 실행:
```bash
cd backend/docker && docker compose -f infra-compose.yml up -d
```

---

## 3. 서비스별 발급 방법

### 3-1. 카카오 (REST API 키 + 네이티브 앱 키)

> 앱 1개 생성으로 로그인 + 로컬 + 모빌리티 + iOS SDK + 지도 전부 사용 가능

1. https://developers.kakao.com 로그인
2. **내 애플리케이션 → 애플리케이션 추가**
3. 앱 생성 후 **앱 키** 탭:
   - **REST API 키** → 백엔드에서 사용 (`KAKAO_REST_API_KEY`)
   - **네이티브 앱 키** → iOS에서 사용 (`KAKAO_NATIVE_APP_KEY`)
4. **플랫폼 → iOS 추가** → 번들 ID 입력
5. **카카오 로그인 → 활성화** → 동의항목 설정 (닉네임, 프로필 이미지)
6. **카카오맵 API 사용 신청** (지도 렌더링용)

**사용 위치:**

| 키 | 사용 모듈 | 설정 파일 |
|----|----------|----------|
| `KAKAO_REST_API_KEY` | odiya-api | `backend/apps/odiya-api/src/main/resources/application.yml` (line 44) |
| `KAKAO_REST_API_KEY` | odiya-batch | `backend/apps/odiya-batch/src/main/resources/application.yml` (line 24) |
| `KAKAO_NATIVE_APP_KEY` | iOS | `ios/Odiya/App/Environment.swift` |

### 3-2. ODsay (대중교통)

1. https://lab.odsay.com 회원가입
2. **개발자 신청 → API 키 발급**
3. 무료: 일 1,000건 (6개월 유효)

**사용 위치:** `backend/apps/odiya-api/src/main/resources/application.yml` (line 55)

### 3-3. Firebase (FCM 푸시)

1. https://console.firebase.google.com → **프로젝트 생성**
2. **프로젝트 설정 → 서비스 계정 → 새 비공개 키 생성**
   - `firebase-service-account.json` 다운로드
3. **iOS 앱 등록** → `GoogleService-Info.plist` 다운로드 → Xcode에 추가
4. **클라우드 메시징 → APNs 인증 키 업로드** (.p8 파일)

**사용 위치:** `backend/apps/odiya-streamer/src/main/resources/application.yml` (line 29)
- `FcmConfig.java`에서 `FIREBASE_CREDENTIALS_PATH` 환경변수로 JSON 파일 경로 로드

### 3-4. Apple APNs (iOS 푸시 수신)

1. https://developer.apple.com → **Certificates, IDs & Profiles**
2. **Keys → + → Apple Push Notifications service (APNs)** 체크
3. `.p8` 키 파일 다운로드 (Key ID 메모)
4. Firebase 콘솔 → **클라우드 메시징 → APNs 인증 키에 .p8 업로드** (Team ID + Key ID 입력)

---

## 4. 환경변수 체크리스트

### 필수 (프로덕션)

```bash
# JWT
JWT_SECRET=                          # 32자 이상 랜덤 문자열

# 카카오 (REST API 키 1개로 로그인+로컬+모빌리티 전부)
KAKAO_REST_API_KEY=                  # developers.kakao.com 앱 키
KAKAO_MOBILITY_REST_API_KEY=         # 동일 키 (batch 모듈용)

# ODsay
ODSAY_API_KEY=                       # lab.odsay.com API 키

# Firebase
FIREBASE_CREDENTIALS_PATH=           # 서비스 계정 JSON 절대 경로

# MySQL
MYSQL_HOST=
MYSQL_PORT=3306
MYSQL_USER=
MYSQL_PWD=

# Redis
REDIS_MASTER_HOST=
REDIS_MASTER_PORT=6379
REDIS_REPLICA_1_HOST=
REDIS_REPLICA_1_PORT=6380

# Kafka
BOOTSTRAP_SERVERS=                   # 예: kafka-broker:9092
```

### 로컬 개발용 `.env` 예시

```bash
# .env (gitignored — 절대 커밋하지 않음)

JWT_SECRET=local-dev-secret-key-must-be-at-least-32-chars

KAKAO_REST_API_KEY=여기에-카카오-REST-API-키
KAKAO_MOBILITY_REST_API_KEY=여기에-카카오-REST-API-키

ODSAY_API_KEY=여기에-ODsay-API-키

FIREBASE_CREDENTIALS_PATH=/path/to/firebase-service-account.json

MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_USER=application
MYSQL_PWD=application

REDIS_MASTER_HOST=localhost
REDIS_MASTER_PORT=6379
REDIS_REPLICA_1_HOST=localhost
REDIS_REPLICA_1_PORT=6380

BOOTSTRAP_SERVERS=localhost:19092
```

---

## 5. 키 관리 전략

### 5-1. 환경별 관리 방식

| 환경 | 관리 방식 | 비고 |
|------|----------|------|
| 로컬 (local) | `.env` 파일 (gitignored) | 개발자 각자 관리 |
| 테스트 (test) | `application-test.yml` 더미 키 | CI 전용, 외부 호출 없음 |
| 개발 서버 (dev) | EC2에 `.env` 파일 배치 | 실제 키, 쿼터 낮음 |
| 프로덕션 (prd) | AWS SSM Parameter Store (SecureString) | 암호화, IAM 접근 제어 |

### 5-2. 민감도 분류

| 분류 | 키 | 유출 시 영향 | 보호 방법 |
|------|---|-------------|----------|
| **Critical** | Firebase 서비스 계정 JSON | FCM 전체 제어권 탈취 | 파일 권한 600, 암호화 저장 |
| **Critical** | `JWT_SECRET` | 토큰 위조 가능 | 32자+ 랜덤, 주기적 회전 |
| **High** | `KAKAO_REST_API_KEY` | API 쿼터 소진 | 도메인/IP 제한 설정 |
| **High** | `ODSAY_API_KEY` | API 쿼터 소진 | IP 제한 설정 |
| **Medium** | MySQL/Redis 인증정보 | DB 접근 | VPC 내부만 허용 |
| **Low** | `KAKAO_NATIVE_APP_KEY` | 앱에 포함 (공개) | 번들 ID 제한으로 보호 |

### 5-3. .gitignore 필수 항목

```gitignore
# 키/시크릿
.env
.env.*
*.p8
firebase-service-account.json
GoogleService-Info.plist
```

---

## 6. 설정 파일 위치 맵

```
odiya2/
├── backend/
│   ├── apps/
│   │   ├── odiya-api/src/main/resources/
│   │   │   └── application.yml      ← JWT, 카카오(로그인+로컬+모빌리티), ODsay
│   │   ├── odiya-batch/src/main/resources/
│   │   │   └── application.yml      ← 카카오모빌리티 (batch 전용)
│   │   └── odiya-streamer/src/main/resources/
│   │       └── application.yml      ← Firebase 서비스 계정 경로
│   ├── modules/
│   │   ├── jpa/src/main/resources/jpa.yml       ← MySQL
│   │   ├── redis/src/main/resources/redis.yml   ← Redis
│   │   └── kafka/src/main/resources/kafka.yml   ← Kafka
│   └── docker/
│       ├── infra-compose.yml        ← MySQL, Redis, Kafka
│       └── monitoring-compose.yml   ← Prometheus, Grafana
├── ios/
│   ├── Package.swift                ← 카카오 SDK 의존성
│   └── Odiya/App/
│       ├── AppDelegate.swift        ← 카카오 SDK 초기화, APNs 등록
│       └── Environment.swift        ← API URL, 카카오 네이티브 앱 키
└── references/                      ← 외부 API 규격 문서
    ├── kakao-login.md
    ├── kakao-local-search.md
    ├── kakao-mobility-directions.md
    ├── odsay-transit.md
    ├── fcm-http-v1.md
    └── kakaomap-ios-sdk.md
```

---

## 7. 현재 누락 & 조치 필요 항목

| # | 항목 | 상태 | 조치 |
|---|------|------|------|
| 1 | Firebase 프로젝트 생성 | ❌ | Firebase 콘솔에서 생성 + 서비스 계정 JSON 다운 |
| 2 | APNs 인증 키 (.p8) | ❌ | Apple Developer에서 발급 → Firebase에 업로드 |
| 3 | GoogleService-Info.plist | ❌ | Firebase에서 iOS 앱 등록 후 다운 → Xcode 추가 |
| 4 | 카카오맵 SDK 의존성 | ❌ | `Package.swift`에 `KakaoMapsSDK-SPM` 추가 |
| 5 | iOS 프로덕션 앱 키 | ❌ | `Environment.swift`의 `YOUR_KAKAO_PRD_APP_KEY` 교체 |
| 6 | `.env.example` 템플릿 | ❌ | 키 이름만 포함한 템플릿 파일 생성 |
| 7 | 로컬 `.env` 파일 | ❌ | 개발자가 직접 키를 채워서 생성 |
