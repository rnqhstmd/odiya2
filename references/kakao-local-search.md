# 카카오 로컬 API — 키워드 장소 검색

> 공식 문서: https://developers.kakao.com/docs/latest/ko/local/dev-guide

## 구현 체크리스트

### 기본 연동
- [ ] 엔드포인트: `GET https://dapi.kakao.com/v2/local/search/keyword.json`
- [ ] 인증 헤더: `Authorization: KakaoAK {REST_API_KEY}`
- [ ] 필수 파라미터: `query` (검색어)
- [ ] 페이지네이션: `page` (1~45), `size` (1~15)

### 응답 파싱
- [ ] `meta.is_end` — 마지막 페이지 여부
- [ ] `documents[].x` — **경도** (String 타입, 숫자가 아님)
- [ ] `documents[].y` — **위도** (String 타입)
- [ ] 좌표가 빈 문자열인 결과 필터링 필요 (`hasValidCoordinates`)
- [ ] `meta.pageable_count` 최대 45 → page × size > 675건 조회 불가

### 에러 처리
- [ ] 검색어 null/빈 문자열 → 400 Bad Request (API 호출 전 검증)
- [ ] 검색어 100자 초과 → 400 Bad Request (API 호출 전 검증)
- [ ] HTTP 401: API 키 오류
- [ ] HTTP 429: 쿼터 초과

### 쿼터 관리
- [ ] 무료 일일 한도: 100,000건/일
- [ ] 월 공유 한도: 3,000,000건/월
- [ ] Redis 캐시 적용 (TTL **24시간**) — Cache-Aside 패턴
- [ ] 캐시 키: `place:{SHA-256(keyword)앞16자}:{page}:{size}`

### 카테고리 코드 참조

| 코드 | 의미 | 코드 | 의미 |
|------|------|------|------|
| `FD6` | 음식점 | `CE7` | 카페 |
| `MT1` | 대형마트 | `CS2` | 편의점 |
| `SW8` | 지하철역 | `AT4` | 관광명소 |
| `HP8` | 병원 | `PM9` | 약국 |

### 응답 구조 참조

```json
{
  "meta": { "total_count": 127, "pageable_count": 45, "is_end": false },
  "documents": [{
    "place_name": "스타벅스 강남점",
    "address_name": "서울 강남구 역삼동 123",
    "road_address_name": "서울 강남구 테헤란로 123",
    "x": "127.027621",
    "y": "37.497952",
    "phone": "02-123-4567",
    "place_url": "http://place.map.kakao.com/12345"
  }]
}
```
