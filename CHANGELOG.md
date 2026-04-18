# Changelog

모든 주요 변경사항은 여기에 기록합니다. 형식은 [Keep a Changelog](https://keepachangelog.com/ko/1.1.0/)를 따르며, 버전은 [Semantic Versioning](https://semver.org/lang/ko/)을 따릅니다.

## [Unreleased]

## [0.2.1] - 2026-04-18

### Changed
- **차단 오버레이** — 동작하지 않던 "홈으로 돌아가기" 버튼 제거, "홈 버튼을 눌러 나가세요" 안내만 표시
- **세션 종료** — 확인 다이얼로그 없이 버튼 누르면 즉시 종료
- **접근성 권한 가이드** — 삼성 One UI 실제 동작 순서에 맞춰 3단계로 재구성 (먼저 접근성 토글 시도 → 제한된 설정 ⋮ 활성화 → 다시 토글 ON)

## [0.2.0] - 2026-04-17

### Added
- **앱 선택 화면 카테고리화** — 설치된 앱을 SNS / OTT / 음악 / 웹툰 / 커뮤니티 / 쇼핑 / 게임 / 메신저 / 브라우저 / 뉴스 / 기타로 자동 분류
- **차단 중 앱 상단 고정** — 현재 체크된 앱을 "✅ 차단 중" 섹션으로 묶어 맨 위에 표시
- **sticky 헤더** — 스크롤 중에도 현재 보고 있는 카테고리가 고정됨
- 주요 한국/글로벌 앱 100+ 개 카테고리 매핑 (넷플릭스, 쿠팡, 배민, 리니지, 원신 등)

### Changed
- AppCategorizer 기반으로 prefix 휴리스틱 추가 (com.ncsoft.*, com.netmarble.* 등 게임 퍼블리셔)

## [0.1.0] - 2026-04-17

### 🎉 최초 공개 릴리즈

#### Added
- 3종 프리셋 (수험생 / 직장인 / 명상) 기반 앱·웹사이트 차단
- 세션 타이머 (15분 ~ 8시간)
- 엄격모드 — 접근성 끄기 / 앱 삭제 / 강제 중지 시도 자동 차단
- 사이트 차단 — Chrome / 삼성 인터넷 / Edge / Firefox / Brave / 네이버 웨일
- 커스터마이즈 화면 — 차단할 앱 / 사이트 편집
- Foreground Service + 부팅 후 자동 세션 복원
- "제한된 설정 해제" 단계별 안내 다이얼로그
- 햅틱 피드백, 저장 전 이탈 보호, 파괴적 작업 확인 다이얼로그

#### Performance
- APK 크기 17.7 MB → 1.4 MB (R8 + material-icons-extended 제거 + arm64-v8a 전용)
- 설치 앱 목록 5분 메모리 캐시
- SessionScreen 재구성 빈도 500ms → 1초 단위

#### Security / Privacy
- 네트워크 통신 없음 (INTERNET 권한 선언 안 함)
- 분석 / 광고 / 크래시 리포터 없음
- 모든 데이터 앱 전용 샌드박스에만 저장

---

[Unreleased]: https://github.com/jongju1124-rgb/focuson-android/compare/v0.2.1...HEAD
[0.2.1]: https://github.com/jongju1124-rgb/focuson-android/releases/tag/v0.2.1
[0.2.0]: https://github.com/jongju1124-rgb/focuson-android/releases/tag/v0.2.0
[0.1.0]: https://github.com/jongju1124-rgb/focuson-android/releases/tag/v0.1.0
