# 내 약속

- 작성일: 2026-03-03
- 수정일: 2026-03-04 (탭 구조 변경: 3탭 + 🔔👤, [+] 약속 생성 버튼, 백엔드 API 매핑 추가)
- 관련 레포: odiya-ios, odiya-api

---

## 탭 구조

내 약속은 3탭 중 **약속 탭** (Tab 2)에 위치한다.

| 탭 | 아이콘 | 화면 |
|----|--------|------|
| 캘린더 (기본 홈) | `calendar` | CalendarView |
| **약속** | `calendar.badge.clock` | **MyAppointmentsView** |
| 친구 | `person.2` | FriendListView |

### toolbar 버튼

| 버튼 | 아이콘 | 동작 |
|------|--------|------|
| [+] | `plus` | 약속 생성 (CreateAppointmentView) `.sheet` |
| 🔔 | `bell` | 알림 목록 (NotificationListView) `.sheet` |
| 👤 | `person.circle` | 내 정보/설정 (ProfileMenuView) `.sheet` |

---

## 화면 구성

사용자의 약속 목록을 보여준다.

### 세그먼트

| 세그먼트 | 내용 |
|----------|------|
| 다가오는 약속 | CONFIRMED 상태, 약속 시간 임박순 정렬 |
| 지난 약속 | COMPLETED / CANCELLED 상태, 최신순 정렬 |

---

## 약속 카드

각 약속은 카드 형태로 표시된다.

### 카드 노출 정보

| 항목 | 설명 |
|------|------|
| 약속 이름 | |
| 장소 | 장소명 + 간략 주소 |
| 날짜/시간 | |
| 참여자 | 프로필 이미지 스택 (최대 4명 + 나머지 수) |
| 상태 | CONFIRMED / COMPLETED / CANCELLED 배지 |

---

## 임박한 약속 강조

가장 가까운 약속 카드에 추가 정보를 표시한다.

| 요소 | 설명 |
|------|------|
| 출발 카운트다운 | "출발까지 N분" 실시간 타이머 |
| 이동수단 아이콘 | 선택된 이동수단 표시 |
| 재촉 버튼 | 약속 30분 전부터 활성화. 참여자에게 Nudge 전송 |

---

## 빈 상태

- 다가오는 약속이 없을 때: 안내 문구 + "약속 만들기" 버튼
- 지난 약속이 없을 때: 안내 문구

---

## 백엔드 API 매핑

내 약속 전용 API는 없다. 다른 도메인 API를 사용한다.

| iOS 동작 | 호출 API | 참조 문서 |
|----------|---------|----------|
| 화면 진입 (다가오는) | `GET /api/v1/appointments/me?status=UPCOMING` | [약속-관리](../약속-관리/) |
| 화면 진입 (지난) | `GET /api/v1/appointments/me?status=PAST` | [약속-관리](../약속-관리/) |
| 약속 카드 탭 → 상세 | `GET /api/v1/appointments/{id}` | [약속-관리](../약속-관리/) |
| [+] 약속 생성 | `POST /api/v1/appointments` | [약속-관리](../약속-관리/) |
| 재촉 버튼 | `POST /api/v1/appointments/{id}/nudge` | [푸시-알림](../푸시-알림/) |
| 🔔 배지 숫자 | `GET /api/v1/notifications/unread-count` | [푸시-알림](../푸시-알림/) |
