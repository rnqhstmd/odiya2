# 친구 관리

- 작성일: 2026-03-03
- 수정일: 2026-03-04 (요청/수락 플로우, 차단=끊기 통합, FriendshipStatus, AddFriendView 재작성)
- 관련 레포: odiya-ios, odiya-api

---

## 친구 추가

### 친구 요청 → 수락 플로우

닉네임 검색으로 친구 요청을 보내고, 상대방이 수락하면 친구 관계가 확정된다.

#### 플로우

```
1. 앱에서 "친구 추가" (친구 탭 toolbar [+])
2. 두 섹션으로 구성:
   - 받은 요청: 수락/거절 버튼
   - 친구 검색: 닉네임으로 오디야 사용자 검색 → 요청 발송
3. 요청 발송 → 상대방에게 푸시 알림 (친구 요청)
4. 상대방이 수락 → 양방향 친구 관계 확정
5. 수락 완료된 친구만 약속에 초대 가능
```

---

## 친구 관계

- **양방향**: A가 B와 친구이면 B도 A와 친구
- 친구 요청은 상대방이 수락해야 확정 (FriendshipStatus)
- 한쪽이 친구 끊기 시 양방향 해제

---

## 관계 태그

친구에게 **색상 도트**를 부여하여 시각적으로 그룹핑한다. 크롬 탭 그룹처럼 색깔이 핵심 식별자이고, 이름은 보조 라벨이다.

### 기본 태그

앱 설치 시 3개 태그가 기본 제공된다. 사용자가 이름·색상을 자유롭게 수정할 수 있다.

| 태그 | 기본 색상 | 값 | 설명 |
|------|----------|-----|------|
| 친구 | ● 하늘 | `#5AC8FA` | 기본값. 태그 미지정 시 자동 부여 |
| 연인 | ● 핑크 | `#FF2D55` | |
| 가족 | ● 초록 | `#34C759` | |

### 사용자 정의 태그

- 사용자가 직접 태그를 생성할 수 있다.
- 태그 이름과 색상을 자유롭게 설정.
- 예: "직장", "동아리", "동네" 등

### 태그 색상

크롬 탭 그룹처럼 **색상 도트**가 태그의 핵심 식별자이다.

- **프리셋 팔레트 12색** + **iOS 커스텀 컬러피커** 제공
- 태그 색상은 친구 목록의 도트, 캘린더 월간 뷰의 도트, 주간 뷰의 블록 색상에 반영된다.

#### 프리셋 팔레트

| 색상 | 값 |
|------|-----|
| 빨강 | `#FF3B30` |
| 주황 | `#FF9500` |
| 노랑 | `#FFCC00` |
| 초록 | `#34C759` |
| 민트 | `#00C7BE` |
| 하늘 | `#5AC8FA` |
| 파랑 | `#007AFF` |
| 보라 | `#AF52DE` |
| 라벤더 | `#C9A8E8` |
| 핑크 | `#FF2D55` |
| 갈색 | `#A2845E` |
| 회색 | `#8E8E93` |

### 태그 관리

- 태그 생성 후 **이름·색상 수정** 가능
- 태그 삭제 시 해당 태그가 부여된 친구들은 "친구(기본)" 태그로 자동 복귀
- 한 친구에게 하나의 태그만 부여 가능
- 친구 목록에서 색상 도트로 태그를 구분하며, 상단 필터 칩으로 태그별 필터링 가능

---

## 친구 끊기 (= 차단)

- **별도 차단 기능 없음** — "친구 끊기"가 곧 차단
- 친구 끊기 시 양방향 관계 해제 + 서로 요청 불가
- BlockedUsersView 삭제됨 (별도 차단 목록 없음)
- `Friend.isBlocked` 제거 → `Friend.status: FriendshipStatus` (.pending / .accepted) 사용

## FriendshipStatus

```swift
enum FriendshipStatus: String, Codable, CaseIterable {
    case pending  = "PENDING"   // 요청 보냄, 수락 대기
    case accepted = "ACCEPTED"  // 친구 확정
}
```

- `.pending`: 친구 요청 발송 후 상대방 수락 대기
- `.accepted`: 양방향 친구 확정 → 약속 초대 가능

## 친구 추가 화면 (AddFriendView)

### 2섹션 구성

1. **받은 요청**: pending 상태 친구 목록 + [수락] [거절] 버튼
2. **친구 검색**: 닉네임 검색 → [요청] 버튼 → Alert "요청을 보냈어요"

### UX

- 수락 시: Alert "친구가 되었어요!" → 목록에서 제거
- 거절 시: 목록에서 제거 (별도 알림 없음)
- 요청 시: 버튼 → "요청됨" 비활성 상태로 변경

---

## 백엔드 API

> 공통: 모든 응답은 `ApiResponse<T>` 래퍼. 인증: `Bearer {accessToken}` 헤더 (별도 표기 없으면 인증 필요)

### 친구 API — FriendV1Controller (⬜ 미구현)

#### 1. 내 친구 목록

| 항목 | 값 |
|------|-----|
| Method | `GET` |
| URL | `/api/v1/friends?status=ACCEPTED&tagId={tagId}` |
| iOS 화면 | FriendListView, Step3ParticipantsView |

**Query Parameters:**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| status | String | X | ACCEPTED(기본) / PENDING |
| tagId | Long | X | 태그 필터링 |

**Response Body (`List<FriendResponse>`):**

| 필드 | 타입 | 설명 |
|------|------|------|
| friendUserId | Long | 친구의 사용자 ID |
| nickname | String | 닉네임 |
| profileImageUrl | String? | 프로필 이미지 URL |
| tag | TagResponse | 태그 정보 (id, name, color) |
| status | String | ACCEPTED / PENDING |

#### 2. 사용자 검색 (닉네임)

| 항목 | 값 |
|------|-----|
| Method | `GET` |
| URL | `/api/v1/users/search?nickname={keyword}` |
| iOS 화면 | AddFriendView (검색) |

**Query Parameters:**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| nickname | String | O | 검색 키워드 (2자 이상) |

**Response Body (`List<UserSearchResponse>`):**

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 사용자 ID |
| nickname | String | 닉네임 |
| profileImageUrl | String? | 프로필 이미지 URL |
| friendStatus | String? | ACCEPTED / PENDING / null(미관계) |

#### 3. 친구 요청 보내기

| 항목 | 값 |
|------|-----|
| Method | `POST` |
| URL | `/api/v1/friends/request` |
| iOS 화면 | AddFriendView (요청) |

**Request Body:**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| targetUserId | Long | O | 요청 대상 사용자 ID |

**Response:** 데이터 없음

**동작:** Friendship 레코드 생성 (PENDING) → 대상에게 FRIEND_REQUEST 푸시 알림

#### 4. 받은 친구 요청 목록

| 항목 | 값 |
|------|-----|
| Method | `GET` |
| URL | `/api/v1/friends/requests/received` |
| iOS 화면 | AddFriendView (받은 요청) |

**Response Body (`List<FriendRequestResponse>`):**

| 필드 | 타입 | 설명 |
|------|------|------|
| requestId | Long | 친구 요청 ID |
| fromUserId | Long | 요청한 사용자 ID |
| nickname | String | 요청한 사용자 닉네임 |
| profileImageUrl | String? | 프로필 이미지 URL |
| createdAt | String | 요청 시각 (ISO-8601) |

#### 5. 친구 요청 수락

| 항목 | 값 |
|------|-----|
| Method | `POST` |
| URL | `/api/v1/friends/request/{requestId}/accept` |
| iOS 화면 | AddFriendView, NotificationListView |

**Response:** 데이터 없음

**동작:** 양방향 ACCEPTED 관계 생성 → 요청자에게 FRIEND_ACCEPTED 푸시

#### 6. 친구 요청 거절

| 항목 | 값 |
|------|-----|
| Method | `POST` |
| URL | `/api/v1/friends/request/{requestId}/reject` |
| iOS 화면 | AddFriendView, NotificationListView |

**Response:** 데이터 없음

#### 7. 친구 끊기 (= 차단)

| 항목 | 값 |
|------|-----|
| Method | `DELETE` |
| URL | `/api/v1/friends/{friendUserId}` |
| iOS 화면 | FriendListView |

**Response:** 데이터 없음

**동작:** 양방향 관계 해제 + blocked 플래그 설정 (서로 재요청 불가)

#### 8. 친구 태그 변경

| 항목 | 값 |
|------|-----|
| Method | `PATCH` |
| URL | `/api/v1/friends/{friendUserId}/tag` |
| iOS 화면 | FriendListView (컨텍스트 메뉴) |

**Request Body:**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| tagId | Long | O | 변경할 태그 ID |

**Response:** 데이터 없음

---

### 태그 API — TagV1Controller (⬜ 미구현)

#### 9. 내 태그 목록

| 항목 | 값 |
|------|-----|
| Method | `GET` |
| URL | `/api/v1/tags` |
| iOS 화면 | TagManagementView, FriendListView (필터 칩) |

**Response Body (`List<TagResponse>`):**

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 태그 ID |
| name | String | 태그 이름 |
| color | String | 색상 hex (예: #5AC8FA) |
| isDefault | Boolean | 기본 태그 여부 (삭제 불가) |
| friendCount | Integer | 해당 태그의 친구 수 |

#### 10. 태그 생성

| 항목 | 값 |
|------|-----|
| Method | `POST` |
| URL | `/api/v1/tags` |
| iOS 화면 | TagManagementView |

**Request Body:**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| name | String | O | 태그 이름 |
| color | String | O | 색상 hex |

**Response Body (`TagResponse`):** 생성된 태그

#### 11. 태그 수정

| 항목 | 값 |
|------|-----|
| Method | `PATCH` |
| URL | `/api/v1/tags/{tagId}` |
| iOS 화면 | TagManagementView |

**Request Body:**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| name | String | X | 새 이름 |
| color | String | X | 새 색상 hex |

**Response Body (`TagResponse`):** 수정된 태그

#### 12. 태그 삭제

| 항목 | 값 |
|------|-----|
| Method | `DELETE` |
| URL | `/api/v1/tags/{tagId}` |
| iOS 화면 | TagManagementView |

**Response:** 데이터 없음

**동작:** 기본 태그(친구/연인/가족)는 삭제 불가 (400 에러). 삭제 시 해당 태그의 친구들 → 기본 "친구" 태그로 자동 복귀
