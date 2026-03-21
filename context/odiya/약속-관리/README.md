# 약속 관리

- 작성일: 2026-03-03
- 수정일: 2026-03-04 (출발지 나중에 지정, 참여자 카카오톡 스타일 UX, Step4 출발지 카드)
- 관련 레포: odiya-ios, odiya-api

---

## 약속 생성

약속 생성 시 아래 항목을 입력한다.

| 항목 | 설명 | 필수 |
|------|------|------|
| 약속 이름 | 자유 입력 | O |
| 장소 | 지도 기반 검색으로 선택 | O |
| 날짜/시간 | 약속 일시 | O |
| 참여자 | `.accepted` 친구에서 선택 (카카오톡 칩 스타일) | O |
| 이동수단 | 대중교통/자동차/도보/자전거 | O (기본값: 대중교통) |
| 출발지 | 등록된 장소 선택 / 새 장소 검색 / "나중에 지정" | △ (Optional) |

### 출발지 (departurePlace)

- `DeparturePlace?` — Optional로 변경 (2026-03-04)
- 등록된 장소에서 선택하거나, "다른 장소에서 출발"로 검색 가능
- "나중에 지정" 선택 시 `nil`로 저장 → 약속 시간 전까지 설정 가능

### 참여자 선택 (Step 3)

- 카카오톡 단톡방 초대 스타일 UI
- 검색바 → 선택칩(프로필+✕, 가로 스크롤) → 친구 리스트
- `.accepted` 상태 친구만 표시
- 하단에 선택 인원 수 표시

---

## 초대 및 수락/거부

### 초대 발송

- 호스트가 약속 생성 시 선택한 친구들에게 **푸시 알림**이 발송된다.
- 호스트는 약속 시간 전까지 추가 초대를 보낼 수 있다.

### 수락/거부

- 초대받은 친구는 각자 앱에 접속하여 해당 약속을 **수락** 또는 **거부**한다.
- **수락 시**: 약속 참여자로 확정. 이동시간 계산, 출발 알림 등 기본 플로우를 따른다.
- **거부 시**: 약속에서 빠진다. 해당 약속과 관련된 알림을 더 이상 받지 않는다.

### 카카오톡 단톡방 공유

- 호스트는 아직 수락하지 않은 약속을 **카카오톡 단톡방에 공유**할 수 있다.
- 공유 메시지에는 약속 정보(이름, 장소, 시간)와 앱 딥링크가 포함된다.
- 딥링크를 통해 앱에서 바로 해당 약속의 수락/거부 화면으로 이동한다.

> 카카오톡 공유는 초대 대상이 앱 푸시를 놓쳤을 때의 보조 수단이다. 공유 자체가 초대를 대체하지는 않는다.

---

## 약속 상태

```
PENDING → CONFIRMED → COMPLETED
                    → CANCELLED
```

| 상태 | 설명 |
|------|------|
| PENDING | 초대 발송 후 참여자 응답 대기 중 |
| CONFIRMED | 참여자가 수락하여 약속 확정 |
| COMPLETED | 약속 시간이 지나 완료 |
| CANCELLED | 호스트가 취소 |

---

## 호스트 권한

호스트는 자동으로 참여자에 포함된다.

| 권한 | 설명 |
|------|------|
| 약속 수정 | 약속 시간 전까지 가능. 변경 시 참여자에게 알림 발송 |
| 약속 취소 | 약속 전체 취소. 참여자에게 알림 발송 |
| 추가 초대 | 약속 시간 전까지 추가 참여자 초대 가능 |
| 카톡 공유 | 미수락 약속을 카카오톡 단톡방에 공유 |

---

## 장소/시간 변경 시 연동

- 장소 또는 시간이 변경되면 모든 참여자의 이동시간이 자동 재계산된다.
- 재계산된 출발 알림 시각이 참여자에게 갱신된다.

---

## 백엔드 API

> 공통: 모든 응답은 `ApiResponse<T>` 래퍼. 인증: `Bearer {accessToken}` 헤더 (별도 표기 없으면 인증 필요)

### 약속 API — AppointmentV1Controller (✅ 구현 완료)

#### 1. 약속 생성

| 항목 | 값 |
|------|-----|
| Method | `POST` |
| URL | `/api/v1/appointments` |
| iOS 화면 | CreateAppointmentView (Step4 완료) |

**Request Body:**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| name | String | O | 약속 이름 |
| placeName | String | O | 장소명 |
| placeAddress | String | O | 장소 주소 |
| latitude | Double | O | 위도 |
| longitude | Double | O | 경도 |
| dateTime | String | O | 약속 일시 (ISO-8601) |
| participantIds | List&lt;Long&gt; | O | 초대할 친구 사용자 ID 목록 |
| transportType | String | O | CAR_PARKING / CAR_PICKUP / TRANSIT / WALKING |
| departurePlaceId | Long | X | 출발지 ID (null이면 "나중에 지정") |

**Response Body (`AppointmentResponse`):**

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 약속 ID |
| name | String | 약속 이름 |
| placeName | String | 장소명 |
| placeAddress | String | 장소 주소 |
| latitude | Double | 위도 |
| longitude | Double | 경도 |
| dateTime | String | 약속 일시 (ISO-8601) |
| status | String | PENDING / CONFIRMED / COMPLETED / CANCELLED |
| hostId | Long | 호스트 사용자 ID |
| transportType | String | 이동수단 |
| durationMinutes | Integer? | 예상 이동시간 (분) |
| departureAlertAt | String? | 출발 알림 시각 (ISO-8601) |
| departurePlaceLabel | String? | 출발지 이름 |
| participants | List&lt;ParticipantResponse&gt; | 참여자 목록 |

**ParticipantResponse:**

| 필드 | 타입 | 설명 |
|------|------|------|
| userId | Long | 사용자 ID |
| nickname | String | 닉네임 |
| profileImageUrl | String? | 프로필 이미지 |
| status | String | ACCEPTED / PENDING / REJECTED |
| isHost | Boolean | 호스트 여부 |

**동작:** 호스트 자동 ACCEPTED → 출발지 있으면 이동시간 계산 → 참여자에게 APPOINTMENT_INVITE 푸시

#### 2. 약속 상세 조회

| 항목 | 값 |
|------|-----|
| Method | `GET` |
| URL | `/api/v1/appointments/{id}` |
| iOS 화면 | AppointmentDetailView |

**Response Body (`AppointmentDetailResponse`):**

AppointmentResponse 필드 전체 + 추가 필드:

| 필드 | 타입 | 설명 |
|------|------|------|
| canNudge | Boolean | 재촉 가능 여부 (약속 30분 전~약속시간) |
| nudgeCooldownSeconds | Integer? | 재촉 쿨다운 남은 초 (null이면 쿨다운 아님) |
| isHost | Boolean | 현재 사용자가 호스트인지 |
| myStatus | String | 현재 사용자의 참여 상태 |

#### 3. 내 약속 목록

| 항목 | 값 |
|------|-----|
| Method | `GET` |
| URL | `/api/v1/appointments/me?status={status}&cursor={cursor}&size={size}` |
| iOS 화면 | MyAppointmentsView |

**Query Parameters:**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| status | String | X | UPCOMING(기본) / PAST |
| cursor | Long | X | 커서 (마지막 약속 ID) |
| size | Integer | X | 페이지 크기 (기본 20) |

**Response Body:**

| 필드 | 타입 | 설명 |
|------|------|------|
| appointments | List&lt;AppointmentResponse&gt; | 약속 목록 |
| hasNext | Boolean | 다음 페이지 존재 여부 |

#### 4. 캘린더 데이터 조회

| 항목 | 값 |
|------|-----|
| Method | `GET` |
| URL | `/api/v1/appointments/calendar?year={year}&month={month}` |
| iOS 화면 | CalendarView |

**Query Parameters:**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| year | Integer | O | 년도 |
| month | Integer | O | 월 (1~12) |

**Response Body (`List<CalendarDayResponse>`):**

| 필드 | 타입 | 설명 |
|------|------|------|
| date | String | 날짜 (yyyy-MM-dd) |
| appointments | List&lt;CalendarAppointmentResponse&gt; | 해당 날짜 약속 요약 |

**CalendarAppointmentResponse:**

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 약속 ID |
| name | String | 약속 이름 |
| dateTime | String | 약속 일시 |
| placeName | String | 장소명 |
| tagColor | String | 대표 태그 색상 (도트 표시용) |

#### 5. 약속 수정 (호스트)

| 항목 | 값 |
|------|-----|
| Method | `PATCH` |
| URL | `/api/v1/appointments/{id}` |
| iOS 화면 | AppointmentDetailView |

**Request Body:** (변경할 필드만 포함)

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| name | String | X | 약속 이름 |
| placeName | String | X | 장소명 |
| placeAddress | String | X | 장소 주소 |
| latitude | Double | X | 위도 |
| longitude | Double | X | 경도 |
| dateTime | String | X | 약속 일시 |

**Response Body (`AppointmentResponse`):** 수정된 약속

**동작:** 장소/시간 변경 시 → 이동시간 재계산 → 참여자에게 APPOINTMENT_CHANGED 푸시

#### 6. 약속 취소 (호스트)

| 항목 | 값 |
|------|-----|
| Method | `DELETE` |
| URL | `/api/v1/appointments/{id}` |
| iOS 화면 | AppointmentDetailView |

**Response:** 데이터 없음

**동작:** 상태 → CANCELLED → 참여자에게 APPOINTMENT_CANCELLED 푸시 → 출발 알림 스케줄 제거

#### 7. 초대 수락

| 항목 | 값 |
|------|-----|
| Method | `POST` |
| URL | `/api/v1/appointments/{id}/accept` |
| iOS 화면 | NotificationListView |

**Response:** 데이터 없음

**동작:** 참여 상태 → ACCEPTED → 출발지 있으면 이동시간 계산 → 전원 수락 시 약속 CONFIRMED

#### 8. 초대 거절

| 항목 | 값 |
|------|-----|
| Method | `POST` |
| URL | `/api/v1/appointments/{id}/reject` |
| iOS 화면 | NotificationListView |

**Response:** 데이터 없음

#### 9. 추가 초대 (호스트)

| 항목 | 값 |
|------|-----|
| Method | `POST` |
| URL | `/api/v1/appointments/{id}/invite` |
| iOS 화면 | AppointmentDetailView |

**Request Body:**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| userIds | List&lt;Long&gt; | O | 추가 초대할 사용자 ID 목록 |

**Response:** 데이터 없음

#### 10. 약속 출발지 설정/변경

| 항목 | 값 |
|------|-----|
| Method | `PATCH` |
| URL | `/api/v1/appointments/{id}/departure` |
| iOS 화면 | AppointmentDetailView, Step4ConfirmView |

**Request Body:**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| departurePlaceId | Long | X | 등록된 출발지 ID |
| transportType | String | X | 이동수단 변경 시 |

**Response Body:**

| 필드 | 타입 | 설명 |
|------|------|------|
| durationMinutes | Integer | 계산된 이동시간 |
| departureAlertAt | String | 출발 알림 시각 |
| transportType | String | 이동수단 |
| departurePlaceLabel | String | 출발지 이름 |

**동작:** 이동시간 계산 (카카오모빌리티/ODsay/직선거리) → 저장 → 출발 알림 스케줄 갱신

---

### 약속 상세 화면 (AppointmentDetailView) — iOS 미구현

약속 상세 화면에서 사용하는 API 매핑:

| 기능 | API |
|------|-----|
| 화면 진입 | `GET /api/v1/appointments/{id}` |
| 재촉하기 | `POST /api/v1/appointments/{id}/nudge` → [푸시-알림](../푸시-알림/) |
| 길찾기 | iOS 외부 앱 연결 (API 호출 없음) |
| 카카오톡 공유 | iOS ShareApi (API 호출 없음) |
| 약속 수정 | `PATCH /api/v1/appointments/{id}` |
| 약속 취소 | `DELETE /api/v1/appointments/{id}` |
| 추가 초대 | `POST /api/v1/appointments/{id}/invite` |
| 출발지 변경 | `PATCH /api/v1/appointments/{id}/departure` |
