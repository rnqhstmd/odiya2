# FCM (Firebase Cloud Messaging) HTTP v1 API

> 공식 문서: https://firebase.google.com/docs/cloud-messaging/send/v1-api

> 구 레거시 API는 2024년 7월부터 셧다운. **반드시 HTTP v1 API 사용**.

## 구현 체크리스트

### 기본 연동
- [ ] 엔드포인트: `POST https://fcm.googleapis.com/v1/projects/{projectId}/messages:send`
- [ ] 인증: 서비스 계정 OAuth2 (`Authorization: Bearer {ACCESS_TOKEN}`)
- [ ] 스코프: `https://www.googleapis.com/auth/firebase.messaging`
- [ ] 서비스 계정 JSON 경로: `GOOGLE_APPLICATION_CREDENTIALS` 환경변수 또는 직접 로드
- [ ] Spring Boot: `firebase-admin` SDK → `FirebaseMessaging.getInstance().send()` 사용

### 메시지 구성
- [ ] `token` / `topic` / `condition` 중 하나만 타겟으로 지정
- [ ] `notification`: title, body (포그라운드 알림)
- [ ] `data`: key-value 커스텀 데이터 (appointmentId, type 등)
- [ ] `apns.headers.apns-priority`: `"10"` (즉시), `"5"` (백그라운드)
- [ ] `apns.payload.aps.sound`: `"default"` (알림 소리)
- [ ] `apns.payload.aps.badge`: 뱃지 카운트

### APNs 연동 (iOS)
- [ ] Firebase 콘솔에서 APNs 인증 키(.p8) 등록
- [ ] `mutable-content: 1` 설정 시 Notification Service Extension 필요
- [ ] 푸시 권한 요청 (iOS 앱)

### 에러 처리
- [ ] HTTP 404 `UNREGISTERED` → **DB에서 디바이스 토큰 비활성화** (필수)
- [ ] HTTP 400 `INVALID_ARGUMENT` → 요청 검증
- [ ] HTTP 401 `UNAUTHENTICATED` → 서비스 계정 확인
- [ ] HTTP 403 `SENDER_ID_MISMATCH` → projectId 확인
- [ ] HTTP 429 `QUOTA_EXCEEDED` → 백오프 후 재시도
- [ ] HTTP 503 `UNAVAILABLE` → 지수 백오프 재시도
- [ ] 개별 발송 실패 시 나머지 참여자는 계속 처리

### 메시지 구조 참조

```json
{
  "message": {
    "token": "device_token",
    "notification": { "title": "출발 시간이에요!", "body": "약속 장소까지 30분" },
    "data": { "appointmentId": "123", "type": "DEPARTURE_REMINDER" },
    "apns": {
      "headers": { "apns-priority": "10" },
      "payload": { "aps": { "badge": 1, "sound": "default" } }
    }
  }
}
```

### 성공 응답
```json
{ "name": "projects/{projectId}/messages/{messageId}" }
```
