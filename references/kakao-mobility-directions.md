# 카카오모빌리티 자동차 길찾기 API

> 공식 문서: https://developers.kakaomobility.com/docs/navi-api/directions/

## 구현 체크리스트

### 기본 연동
- [ ] 엔드포인트: `GET https://apis-navi.kakaomobility.com/v1/directions`
- [ ] 인증 헤더: `Authorization: KakaoAK {REST_API_KEY}`
- [ ] 좌표 순서가 **경도,위도**(lng,lat)인지 확인 — 카카오 API 공통 규칙, lat,lng 순서와 반대
- [ ] `origin`, `destination` 파라미터에 `"경도,위도"` 형식 문자열 전달

### 응답 파싱
- [ ] `routes[].result_code == 0` 성공 여부 확인
- [ ] `routes[].summary.duration` — 단위가 **초**임. 분 변환: `(int) Math.ceil(durationSeconds / 60.0)`
- [ ] `routes`가 null이거나 비어있는 경우 처리
- [ ] `result_code != 0`이면 에러 처리 (104=출발지/도착지 동일, 201=경로 없음)

### 에러 처리
- [ ] HTTP 400: 잘못된 파라미터 → BAD_REQUEST 예외
- [ ] HTTP 401: 인증 실패 → API 키 설정 확인
- [ ] HTTP 429: 쿼터 초과 → fallback 처리
- [ ] 타임아웃: **3초** 설정
- [ ] `RestClientResponseException`, `ResourceAccessException` 분기 처리

### 쿼터 관리
- [ ] 무료 일일 한도: 10,000건/일
- [ ] Redis 캐시 적용 (TTL 30분) — 동일 출발지-도착지 재호출 방지
- [ ] 캐시 히트 시 API 미호출 확인

### 요청 파라미터 참조

| 파라미터 | 필수 | 설명 |
|---------|------|------|
| `origin` | O | 출발지 `"경도,위도"` |
| `destination` | O | 도착지 `"경도,위도"` |
| `priority` | X | `RECOMMEND`(기본), `TIME`, `DISTANCE` |
| `avoid` | X | `toll`, `motorway` 등 |

### 응답 구조 참조

```json
{
  "routes": [{
    "result_code": 0,
    "summary": {
      "distance": 12345,
      "duration": 1800,
      "fare": { "taxi": 12000, "toll": 900 }
    }
  }]
}
```

| 필드 | 단위 | 설명 |
|------|------|------|
| `summary.distance` | 미터 | 총 거리 |
| `summary.duration` | **초** | 총 소요시간 |
| `summary.fare.taxi` | 원 | 예상 택시비 |
