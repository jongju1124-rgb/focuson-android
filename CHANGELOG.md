# Changelog

모든 주요 변경사항은 여기에 기록합니다. 형식은 [Keep a Changelog](https://keepachangelog.com/ko/1.1.0/)를 따르며, 버전은 [Semantic Versioning](https://semver.org/lang/ko/)을 따릅니다.

## [Unreleased]

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

[Unreleased]: https://github.com/jongju1124-rgb/focuson-android/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/jongju1124-rgb/focuson-android/releases/tag/v0.1.0
