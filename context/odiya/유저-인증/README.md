# 유저 인증

- 작성일: 2026-03-03
- 수정일: 2026-03-04 (백엔드 API 명세 추가 — 구현 완료 기준)
- 관련 레포: odiya-ios, odiya-api

---

## 소셜 로그인

KakaoTalk OAuth를 유일한 로그인 수단으로 사용한다.

### 로그인 흐름

```
앱 실행
  → KakaoTalk SDK로 인증
  → 서버에 카카오 토큰 전송
  → 서버에서 카카오 사용자 정보 조회
  → 신규 유저면 자동 가입 / 기존 유저면 로그인
  → JWT 발급 → 앱 저장
```

---

## JWT 토큰

| 토큰 | 유효기간 | 용도 |
|------|----------|------|
| Access Token | 1시간 | API 요청 인증 |
| Refresh Token | 30일 | Access Token 갱신 |

- Access Token 만료 시 Refresh Token으로 자동 갱신
- Refresh Token 만료 시 카카오 재로그인 필요

---

## 프로필

| 항목 | 출처 | 변경 가능 |
|------|------|-----------|
| 닉네임 | 카카오 닉네임 (초기값) | 앱 내 변경 가능 |
| 프로필 이미지 | 카카오 프로필 이미지 (초기값) | 앱 내 변경 가능 |

---

## 탈퇴

- 앱 내 탈퇴 요청 시 **즉시 데이터 삭제**
- 삭제 대상: 계정, 프로필, 친구 관계, 약속 참여 기록
- 호스트인 약속이 남아있으면 탈퇴 전 약속 취소 또는 호스트 위임 필요

---

## 백엔드 API

> 공통: 모든 응답은 `ApiResponse<T>` 래퍼 (`{ meta: { result, errorCode, message }, data: T }`). 인증: `Authorization: Bearer {accessToken}` 헤더 (별도 표기 없으면 인증 필요)

### 인증 API — AuthV1Controller (✅ 구현 완료)

#### 1. 카카오 로그인

| 항목 | 값 |
|------|-----|
| Method | `POST` |
| URL | `/api/v1/auth/kakao/login` |
| 인증 | 불필요 |
| iOS 화면 | LoginView |

**Request Body:**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| kakaoAccessToken | String | O | 카카오 SDK에서 받은 accessToken |

**Response Body (`TokenResponse`):**

| 필드 | 타입 | 설명 |
|------|------|------|
| accessToken | String | JWT access token (1시간) |
| refreshToken | String | JWT refresh token (30일) |

**동작:** 카카오 accessToken으로 카카오 API(`/v2/user/me`) 호출 → 사용자 정보 조회 → 신규면 자동 가입 → JWT 발급

#### 2. 토큰 갱신

| 항목 | 값 |
|------|-----|
| Method | `POST` |
| URL | `/api/v1/auth/token/refresh` |
| 인증 | 불필요 |
| iOS 화면 | AuthInterceptor (자동) |

**Request Body:**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| refreshToken | String | O | 기존 refresh token |

**Response Body (`TokenResponse`):**

| 필드 | 타입 | 설명 |
|------|------|------|
| accessToken | String | 새 JWT access token |
| refreshToken | String | 새 JWT refresh token (회전) |

**동작:** 기존 refreshToken 검증 → 새 토큰 쌍 발급 → 기존 refreshToken 무효화 (토큰 회전)

#### 3. 로그아웃

| 항목 | 값 |
|------|-----|
| Method | `POST` |
| URL | `/api/v1/auth/logout` |
| 인증 | 필요 |
| iOS 화면 | ProfileMenuView |

**Request Body:**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| refreshToken | String | O | 무효화할 refresh token |

**Response:** 데이터 없음 (성공 시 `meta.result = SUCCESS`)

---

### 사용자 API — UserV1Controller (✅ 구현 완료)

#### 4. 내 정보 조회

| 항목 | 값 |
|------|-----|
| Method | `GET` |
| URL | `/api/v1/users/me` |
| iOS 화면 | ProfileMenuView, ProfileView |

**Response Body (`UserResponse`):**

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 사용자 ID |
| nickname | String | 닉네임 |
| profileImageUrl | String | 프로필 이미지 URL (nullable) |

#### 5. 닉네임 변경

| 항목 | 값 |
|------|-----|
| Method | `PATCH` |
| URL | `/api/v1/users/me/nickname` |
| iOS 화면 | ProfileView |

**Request Body:**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| nickname | String | O | 새 닉네임 (최대 20자, 중복 불가) |

**Response Body (`UserResponse`):** 변경된 사용자 정보

#### 6. 프로필 이미지 변경

| 항목 | 값 |
|------|-----|
| Method | `PATCH` |
| URL | `/api/v1/users/me/profile-image` |
| iOS 화면 | ProfileView |

**Request Body:**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| profileImageUrl | String | O | 새 프로필 이미지 URL |

**Response Body (`UserResponse`):** 변경된 사용자 정보

#### 7. 회원 탈퇴

| 항목 | 값 |
|------|-----|
| Method | `DELETE` |
| URL | `/api/v1/users/me` |
| iOS 화면 | SettingsView |

**Response:** 데이터 없음

**동작:** soft-delete (deletedAt 설정) → 관련 데이터 정리
