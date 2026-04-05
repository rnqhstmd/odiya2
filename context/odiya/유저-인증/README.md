# 유저 인증

- 작성일: 2026-03-03
- 수정일: 2026-04-05 (프로필 이미지 변경 S3 Presigned URL 흐름, Storage API 명세 추가 — PR #32)
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

### 프로필 이미지 변경 흐름 (S3 Presigned URL)

```
1. ProfileView → PhotosPicker로 이미지 선택
2. Task.detached: 1024×1024 리사이즈 + JPEG 0.8 압축 (UI 스레드 블로킹 방지)
3. POST /api/v1/storage/presigned-url → { presignedUrl, imageUrl } 수령
4. iOS ImageUploadService: presignedUrl로 S3에 직접 PUT (Content-Type 포함)
5. PATCH /api/v1/users/me/profile-image { profileImageUrl: imageUrl }
6. 서버가 User.profileImageUrl 갱신 → 응답으로 갱신된 UserResponse 반환
```

**보안 정책:**
- Presigned URL은 인증된 사용자 본인 스코프로만 발급 (`profile-images/{userId}/{uuid}/{filename}`)
- 파일명은 DTO 정규식(`^[\w\-.]+\.(jpg|jpeg|png|webp|heic)$`) + 서비스 단 `new File().getName()` 이중 방어
- URL 만료: 기본 10분 (`presigned-url-expiration: 600`)

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

---

### 스토리지 API — StorageV1Controller (✅ 구현 완료)

> 프로필 이미지 같은 사용자 파일을 S3에 업로드하기 위한 Presigned URL을 발급한다.

#### 8. Presigned URL 발급

| 항목 | 값 |
|------|-----|
| Method | `POST` |
| URL | `/api/v1/storage/presigned-url` |
| iOS 화면 | ProfileView (프로필 이미지 변경) |

**Request Body:**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| fileName | String | O | 파일명 (정규식 `^[\w\-.]+\.(jpg\|jpeg\|png\|webp\|heic)$`, 최대 255자) |
| contentType | String | O | MIME (`image/jpeg`, `image/png`, `image/webp`, `image/heic`) |

**Response Body (`PresignedUrlResponse`):**

| 필드 | 타입 | 설명 |
|------|------|------|
| presignedUrl | String | iOS가 PUT 업로드할 시간 제한 URL (기본 만료 10분) |
| imageUrl | String | 업로드 완료 후 프로필 이미지 URL로 저장할 public URL |

**동작:**
1. 인증된 사용자 ID로 objectKey 생성: `profile-images/{userId}/{uuid}/{sanitizedFileName}`
2. `S3Presigner.presignPutObject()`로 presigned URL 발급
3. Path Traversal 방지: `new File(fileName).getName()`으로 basename만 추출 (DTO 정규식과 이중 방어)
4. 인증 체계: AWS 자격증명은 `accessKey`/`secretKey` 설정 시 `StaticCredentialsProvider`, 비어 있으면 `DefaultCredentialsProvider` (IAM Role/환경변수 체인)로 fallback
