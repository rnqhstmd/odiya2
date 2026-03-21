# 카카오 로그인 (iOS SDK + 백엔드 REST API)

> iOS SDK: https://developers.kakao.com/docs/latest/ko/kakaologin/ios
> REST API: https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api

## 구현 체크리스트

### iOS SDK 초기화
- [ ] `KakaoSDK.initSDK(appKey: "NATIVE_APP_KEY")` — AppDelegate에서 호출
- [ ] SceneDelegate에서 `AuthApi.isKakaoTalkLoginUrl(url)` → `AuthController.handleOpenUrl(url:)` 처리
- [ ] Info.plist `LSApplicationQueriesSchemes`: `kakaokompassauth`, `kakaolink`, `kakaoplus`
- [ ] Info.plist `CFBundleURLSchemes`: `kakao{NATIVE_APP_KEY}`

### iOS 로그인 호출
- [ ] `UserApi.isKakaoTalkLoginAvailable()` 체크
- [ ] 카카오톡 설치됨 → `loginWithKakaoTalk`
- [ ] 미설치 → `loginWithKakaoAccount` (웹 로그인)
- [ ] `oauthToken.accessToken` → 백엔드 `/api/v1/auth/login`으로 전송

### 백엔드 사용자 정보 조회
- [ ] 엔드포인트: `GET https://kapi.kakao.com/v2/user/me`
- [ ] 인증 헤더: `Authorization: Bearer {사용자의 카카오 ACCESS_TOKEN}`
- [ ] `kakao_account.profile.nickname` — null 가능 (동의 항목에 따라)
- [ ] `kakao_account.profile.profile_image_url` — null 가능
- [ ] 카카오 accessToken은 백엔드에 저장하지 않음 → 자체 JWT 발급

### 토큰 관리
- [ ] 카카오 액세스 토큰 (iOS): 12시간
- [ ] 카카오 액세스 토큰 (REST API): 6시간
- [ ] 카카오 리프레시 토큰: 2개월 (만료 1개월 전부터 갱신 가능)
- [ ] odiya JWT: access 1시간 + refresh 30일 (Redis 저장)

### 쿼터
- [ ] 월 공유 한도: 3,000,000건/월
- [ ] 액세스 토큰 발급: 10분당 20개
- [ ] 리프레시 토큰 발급: 60분당 30개

### odiya 인증 플로우 참조

```
iOS → 카카오 로그인 SDK → accessToken 획득
iOS → POST /api/v1/auth/login (accessToken 전달)
백엔드 → GET kapi.kakao.com/v2/user/me (사용자 정보 조회)
백엔드 → JWT 발급 (access 1시간 + refresh 30일)
백엔드 → JWT를 iOS에 응답
```
