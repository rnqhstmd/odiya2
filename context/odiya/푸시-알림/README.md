# 푸시 알림

- 작성일: 2026-03-03
- 수정일: 2026-03-03 (약속 초대 푸시 구체화)
- 관련 레포: odiya-ios, odiya-api

---

## 알림 유형

| # | 유형 | 트리거 | 내용 |
|---|------|--------|------|
| 1 | 출발 알림 | 역산 공식 기반 출발 시각 도래 | "지금 출발하면 딱 맞아요!" |
| 2 | 이동시간 변동 | 재계산 결과 10분+ 변동 | "이동시간이 {N}분 늘었어요" |
| 3 | 리마인더 | 약속 1시간 전 | "1시간 후 약속이 있어요" |
| 4 | 재촉하기 (Nudge) | 참여자가 재촉 버튼 탭 | "{이름}이(가) 출발하래요!" |
| 5 | 약속 초대 | 호스트가 약속 생성/추가 초대 | "{이름}이(가) 약속에 초대했어요" |
| 6 | 약속 변경 | 호스트가 약속 정보 변경 | "약속 정보가 변경됐어요" |
| 7 | 약속 취소 | 호스트가 약속 취소 | "약속이 취소됐어요" |
| 8 | 출발지 미등록 | 약속 D-1인데 출발지 미설정 | "내일 약속 출발지를 등록해주세요" |
| 9 | 친구 요청 | 상대방이 친구 요청 전송 | "{이름}이(가) 친구 요청을 보냈어요" |

---

## 재촉하기 (Nudge)

odiya의 핵심 소셜 기능. "출발했어?" 카톡을 앱 내로 대체한다.

### 활성 조건

- 약속 **30분 전**부터 활성화
- 약속 시간이 지나면 비활성화

### 쿨다운

- 동일 대상에게 재촉하기: **5분** 쿨다운
- 쿨다운 중에는 버튼 비활성화 + 남은 시간 표시
- Redis로 쿨다운 상태 관리

### 전송

- APNs를 통한 즉시 푸시 알림 전송
- 알림에 재촉한 사람의 이름 포함

---

## 스케줄링

| 알림 유형 | 스케줄링 방식 |
|-----------|---------------|
| 출발 알림 | 서버 사이드 task queue. 약속 생성/변경 시 스케줄 등록 |
| 이동시간 변동 | 재계산 주기에 따라 체크 후 조건부 발송 |
| 리마인더 | 약속 시간 기준 1시간 전 스케줄 |
| 재촉하기 | 실시간 (사용자 액션 → 즉시 전송) |
| 기타 | 이벤트 발생 시 즉시 전송 |

### 정확도 목표

- 출발 알림: 스케줄 시각 대비 **±1분** 이내 도달
- APNs priority: `.alert` (high priority)

---

## 백엔드 API

> 공통: 모든 응답은 `ApiResponse<T>` 래퍼. 인증: `Bearer {accessToken}` 헤더

### 알림 API — NotificationV1Controller (⬜ 미구현)

#### 1. 알림 목록 조회

| 항목 | 값 |
|------|-----|
| Method | `GET` |
| URL | `/api/v1/notifications?cursor={cursor}&size={size}` |
| iOS 화면 | NotificationListView |

**Query Parameters:**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| cursor | Long | X | 마지막 알림 ID (커서 페이지네이션) |
| size | Integer | X | 페이지 크기 (기본 20) |

**Response Body:**

| 필드 | 타입 | 설명 |
|------|------|------|
| notifications | List&lt;NotificationResponse&gt; | 알림 목록 |
| hasNext | Boolean | 다음 페이지 존재 |

**NotificationResponse:**

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 알림 ID |
| type | String | FRIEND_REQUEST / APPOINTMENT_INVITE / DEPARTURE_ALERT / NUDGE / APPOINTMENT_CHANGED / APPOINTMENT_CANCELLED / REMINDER / DEPARTURE_NOT_SET |
| title | String | 알림 제목 |
| body | String | 알림 본문 |
| isRead | Boolean | 읽음 여부 |
| createdAt | String | 생성 시각 (ISO-8601) |
| referenceId | Long? | 관련 엔티티 ID (약속 ID, 친구 요청 ID 등) |

#### 2. 알림 읽음 처리

| 항목 | 값 |
|------|-----|
| Method | `PATCH` |
| URL | `/api/v1/notifications/{id}/read` |
| iOS 화면 | NotificationListView |

**Response:** 데이터 없음

#### 3. 전체 읽음 처리

| 항목 | 값 |
|------|-----|
| Method | `PATCH` |
| URL | `/api/v1/notifications/read-all` |
| iOS 화면 | NotificationListView |

**Response:** 데이터 없음

#### 4. 읽지 않은 알림 수

| 항목 | 값 |
|------|-----|
| Method | `GET` |
| URL | `/api/v1/notifications/unread-count` |
| iOS 화면 | MainTabView (🔔 배지) |

**Response Body:**

| 필드 | 타입 | 설명 |
|------|------|------|
| count | Integer | 읽지 않은 알림 수 |

---

### 재촉 API (⬜ 미구현)

#### 5. 재촉하기 (Nudge)

| 항목 | 값 |
|------|-----|
| Method | `POST` |
| URL | `/api/v1/appointments/{appointmentId}/nudge` |
| iOS 화면 | MyAppointmentsView (재촉 버튼), AppointmentDetailView |

**Response Body:**

| 필드 | 타입 | 설명 |
|------|------|------|
| nudgedUserIds | List&lt;Long&gt; | 재촉 전송된 사용자 ID 목록 |
| cooldownSeconds | Integer | 쿨다운 시간 (초) |

**비즈니스 로직:**

| 조건 | 동작 |
|------|------|
| 약속 30분 전 ~ 약속시간 | 허용 |
| 그 외 시간 | 403 Forbidden |
| 5분 이내 재전송 | 409 Conflict + `{ remainingSeconds }` |

**쿨다운 관리 (Redis):**

| 키 패턴 | TTL | 설명 |
|---------|-----|------|
| `nudge:{appointmentId}:{fromUserId}` | 300초 (5분) | 재촉 쿨다운 |

**동작:** 쿨다운 체크 → 약속 내 다른 참여자 전원에게 NUDGE 푸시 → Redis 쿨다운 키 설정

---

### 디바이스 토큰 API (⬜ 미구현)

#### 6. FCM 디바이스 토큰 등록

| 항목 | 값 |
|------|-----|
| Method | `POST` |
| URL | `/api/v1/devices/token` |
| iOS 화면 | AppDelegate (앱 시작 시 자동) |

**Request Body:**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| token | String | O | FCM 디바이스 토큰 |
| platform | String | O | IOS / ANDROID |

**Response:** 데이터 없음

**동작:** 기존 토큰이 있으면 갱신, 없으면 생성. 사용자 1명에 여러 디바이스 가능.

#### 7. 디바이스 토큰 삭제

| 항목 | 값 |
|------|-----|
| Method | `DELETE` |
| URL | `/api/v1/devices/token` |
| iOS 화면 | 로그아웃 시 자동 |

**Request Body:**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| token | String | O | 삭제할 FCM 디바이스 토큰 |

**Response:** 데이터 없음
