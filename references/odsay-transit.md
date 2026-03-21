# ODsay 대중교통 경로 검색 API

> 공식 문서: https://lab.odsay.com/guide/releaseReference

## 구현 체크리스트

### 기본 연동
- [ ] 엔드포인트: `GET https://api.odsay.com/v1/api/searchPubTransPathT`
- [ ] 인증: `apiKey` **쿼리 파라미터**로 전달 (헤더가 아님)
- [ ] 좌표 파라미터: `SX`(출발 경도), `SY`(출발 위도), `EX`(도착 경도), `EY`(도착 위도)
- [ ] API 키가 URL에 노출됨 → 액세스 로그에서 쿼리스트링 제외 설정 필요

### 응답 파싱
- [ ] `result.path[0].info.totalTime` — 단위가 **분**임. 변환 불필요 (카카오모빌리티는 초)
- [ ] `result`가 null인 경우 처리
- [ ] `result.path`가 null 또는 빈 배열인 경우 처리
- [ ] `pathType`: 1=지하철, 2=버스, 3=지하철+버스

### 에러 처리
- [ ] 에러 코드 `-8`: 파라미터 형식 오류
- [ ] 에러 코드 `-9`: 필수 입력값 누락
- [ ] 에러 코드 `-99`: 경로 없음 → SERVICE_UNAVAILABLE 예외
- [ ] HTTP 500: 서버 오류 → fallback 처리
- [ ] 타임아웃: **5초** 설정 (카카오모빌리티보다 응답이 느릴 수 있음)

### 쿼터 관리
- [ ] 무료 일일 한도: 1,000건/일
- [ ] Redis 캐시 적용 (TTL **6시간**) — 시간표 기반이라 길게 잡아도 무방
- [ ] 캐시 히트 시 API 미호출 확인

### 응답 구조 참조

```json
{
  "result": {
    "path": [{
      "pathType": 1,
      "info": {
        "totalTime": 45,
        "totalPayment": 1250,
        "busTransitCount": 1,
        "subwayTransitCount": 0,
        "totalWalk": 350
      },
      "subPath": [{
        "trafficType": 2,
        "sectionTime": 25,
        "startName": "강남역",
        "endName": "서울역"
      }]
    }]
  }
}
```

| 필드 | 단위 | 설명 |
|------|------|------|
| `info.totalTime` | **분** | 총 소요시간 |
| `info.totalPayment` | 원 | 총 요금 |
| `info.totalWalk` | 미터 | 총 도보 거리 |
| `subPath[].trafficType` | - | 1=지하철, 2=버스, 3=도보 |
