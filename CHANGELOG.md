# Changelog

모든 주요 변경사항은 여기에 기록합니다. 형식은 [Keep a Changelog](https://keepachangelog.com/ko/1.1.0/)를 따르며, 버전은 [Semantic Versioning](https://semver.org/lang/ko/)을 따릅니다.

## [Unreleased]

## [0.5.0] - 2026-04-20

### 🌊 브랜드 리뉴얼: "앱차단" → "잠수모드"

### Changed
- **앱 이름**: `앱차단` → **`잠수모드`**
  - 차단이라는 수동적·처벌적 프레이밍 대신, **"잠깐 사라진다"** 는 슬랭 "잠수타다" 차용
  - "나 잠수모드야" 자연스러운 사회적 호칭 확보
- **수면 메타포** 전면 적용
  - 세션 → **잠수**
  - 시작 → **잠수 시작**
  - 종료 → **수면 위로**
  - "차단됨" 오버레이 → **"잠수 중"**
  - "14:54 후에 풀려요" → **"14:54 후에 올라와요"**
  - 엄격모드: "종료할 수 없어요" → "수면 위로 올라올 수 없어요"
- **접근성 서비스 라벨**: `앱차단 서비스` → `잠수모드 서비스`
- **서비스 채널명**: `차단 세션` → `잠수 세션`
- **토스트 문구** — WidgetActionReceiver 전부 수면 메타포로 교체
  - "이미 잠수 중이에요"
  - "수면 위로 올라왔어요"
  - "엄격모드라 수면 위로 못 올라와요"
- **통계 화면** 라벨: `총 집중 시간` → `총 잠수 시간`, `완주 세션` → `완주 잠수`

### 색상·아이콘
- **브랜드 색** `#FF5A3FFF` (보라) → **`#FF0B3B6B`** (심해 블루)
- **런처 아이콘** 재설계: 수면 파동 2줄 + 아래로 가라앉는 물방울 + 거품
- 프리셋 개별 색 (파랑·초록·보라) 은 기능적 구분자로 유지

### 호환성
- `applicationId`: `com.focuson.app` 그대로 유지 → **기존 데이터·세션·Pro 라이선스 100% 보존**
- 내부 클래스명 (`FocusOnApp`, `AppBlockerAccessibilityService` 등) 유지
- **Gyuwon flavor** (`장규원이 중간고사 대비`) 는 strings override 로 영향 없음

### 왜 "잠수모드"?
- ✅ 한국인에게 이미 익숙한 슬랭 "잠수타다" = 앱이 하는 일과 정확히 일치
- ✅ 차단이 아닌 "잠깐 사라짐" 긍정 프레이밍 — 설치 저항 ↓
- ✅ 사회적 호칭 — "나 잠수모드야" 자연스럽게 입에 붙음
- ✅ 학생부터 직장인까지 범용
- ✅ 독자성 (동일 이름 경쟁앱 없음)

## [0.4.2] - 2026-04-19

### Changed
- **Standard 에디션 이름 변경** — "포커스온" → **"앱차단"**
  - `app_name` + 관련 `accessibility_service_label`·description·위젯 타이틀 일괄 교체
  - `applicationId` 는 `com.focuson.app` 그대로 유지 (기존 설치 데이터 보존)
  - 내부 클래스명 (`FocusOnApp`, `FocusOnTheme`, `AppBlockerAccessibilityService`) 도 그대로
- **Gyuwon 에디션 (`장규원이 중간고사 대비`) 은 변경 없음** — flavor override 그대로 유지
- 서비스의 "우리 앱 감지" 키워드는 `applicationInfo.loadLabel()` 기반 런타임 구성이라 자동 반영

### 호환성
- 기존 v0.4.1 (포커스온) 사용자는 덮어쓰기 설치 시 앱 이름만 "앱차단" 으로 바뀌고 데이터·세션·Pro 라이선스 모두 유지됨

### ⚠️ 주의
"AppBlock" 은 전 세계 1천만+ 다운로드 실존 앱. 사이드로드 상태에선 문제없지만 Play Store 출시 시 충돌 가능성 있음. 장기적으로는 독자적 이름 고려 권장.

## [0.4.1] - 2026-04-19

### Changed
- **Pro 티어 단순화** — 기존 3티어(1,000 / 2,900 / 4,900원) → **FREE + PRO 2단계**
  - `ProTier` enum: `FREE`, `PRO`(2,900원) 만 남김
  - `ProScreen`: 단일 Pro 카드 + 결제 다이얼로그
  - `LicenseInputDialog`: 티어 선택 UI 제거 (자동 PRO)
  - `scripts/generate_license.py`: 인자에서 tier 제거 (`python scripts/generate_license.py <email>`)
- Pro 혜택 설명 통합: "전체 통계 + 무제한 커스텀 프리셋(예정) + 향후 모든 Pro 업데이트"

### Why
1,000원은 심리적으로 "왜 이것만?" 느낌이 강함, 4,900원은 진입 저항. 2,900원 단일 티어가 "커피 한 잔" 포지셔닝과 의사결정 속도 면에서 가장 깔끔.

## [0.4.0] - 2026-04-18

### Added
- **Pro 티어 시스템** — 1회 결제 평생 이용
  - `ProTier` enum (FREE / TIER1 1,000원 / TIER2 2,900원 / TIER3 4,900원)
  - `SettingsStore.proTier` 로 상태 관리 + `LicenseVerifier` HMAC 오프라인 검증 (서버 없음)
- **통계 화면** (`StatsScreen`)
  - 총 집중 시간 / 완주 세션 / 총 세션 / 완주율
  - 최근 7일 (무료) ~ 30일 (Pro) 일별 막대 그래프
  - 무료 사용자는 그래프 밑에 Pro 업그레이드 안내 카드
- **Pro 업그레이드 화면** (`ProScreen`)
  - 3티어 카드 선택 → 결제 다이얼로그 (토스 송금 / 계좌 복사 / 이메일 문의)
  - 라이선스 키 등록 다이얼로그 (이메일 + 티어 + 16자리 키)
- **홈 화면 하단** — [📊 통계] [💛 Pro] 버튼 추가

### Infra
- `buildConfig = true`, `LICENSE_SECRET` / `TOSS_ID` / 계좌정보 등 `keystore.properties` → `BuildConfig` 로 주입
- `scripts/generate_license.py` — 개발자용 라이선스 키 발급 CLI
- `SessionDao` 에 통계 집계 쿼리 (`totalFocusedMsSince`, `completedCountSince`, `findSince` 등)

### 무료 vs Pro 기능 분할
| 기능 | FREE | TIER1 | TIER2 | TIER3 |
|------|:---:|:---:|:---:|:---:|
| 기본 차단 (3 프리셋) | ✅ | ✅ | ✅ | ✅ |
| 차단 오버레이·위젯·엄격모드 | ✅ | ✅ | ✅ | ✅ |
| 통계 — 지난 7일 | ✅ | ✅ | ✅ | ✅ |
| 통계 — 전체 기간 | ❌ | ✅ | ✅ | ✅ |
| 무제한 커스텀 프리셋 | ❌ | ❌ | ✅ (예정) | ✅ (예정) |
| 스케줄 자동화 | ❌ | ❌ | ❌ | ✅ (예정) |
| 스마트 트리거 | ❌ | ❌ | ❌ | ✅ (예정) |

※ TIER2/TIER3 신규 기능은 다음 릴리즈에서 구현. v0.4.0 은 **결제 · 통계 인프라** 중심.

## [0.3.3] - 2026-04-18

### Added
- **에디션(productFlavors) 2종** — 같은 코드베이스에서 두 APK 동시 빌드
  - `standard` — 기존 "포커스온" (applicationId `com.focuson.app`)
  - `gyuwon` — 신규 "장규원이 중간고사 대비" (applicationId `com.focuson.app.gyuwon`)
    - 프리셋 이름: `공부중` / `학원` / `학교`
    - 위젯 칩 레이블도 자동 치환
    - 접근성 서비스 라벨 `장규원이 중간고사 대비 차단 서비스`
- **Strict bypass 판별 키워드를 런타임 계산** — 앱 라벨/패키지명을 PackageManager 에서 읽어 구성하여 에디션마다 자동 적용

### Changed
- `FOCUSON_KEYWORDS` 제거 → `focusonKeywords` (instance-level lazy) 로 이관
- 위젯 레이아웃의 하드코딩 칩 레이블(`"수험생"/"직장인"/"명상"`) 을 `@string/widget_preset_*_short` 로 교체

## [0.3.2] - 2026-04-18

### Fixed
- **엄격모드 — 다른 앱의 제거·강제중지가 모두 막히던 버그** — 원래 포커스온 무력화를 막으려던 로직이 "설정 화면 + 제거/중지 키워드"만 보고 모두 차단하고 있었음. 화면 텍스트에 "포커스온"/"focuson" 이 언급되어야만 `GLOBAL_ACTION_BACK` 트리거하도록 추가 조건 검증.
  - ✅ Settings > Apps > Chrome 에서 [삭제] → 통과
  - 🚫 Settings > Apps > 포커스온 에서 [삭제] → 차단
  - ✅ 다른 접근성 서비스 끄기 → 통과
  - 🚫 "포커스온 차단 서비스" 끄기 → 차단

## [0.3.1] - 2026-04-18

### Fixed
- **차단 오버레이 탭으로 나가기** — `FLAG_NOT_FOCUSABLE` 때문에 터치가 오버레이를 통과해서 아래 앱으로 새던 버그. 플래그 제거 + `performGlobalAction(GLOBAL_ACTION_HOME)` 으로 확실하게 홈으로 이동
- 오버레이 레이아웃 간결화 (타이틀/메시지/시간 폰트·여백 축소)

### Changed
- **위젯 프리셋 탭 → 바로 세션 시작** — 앱을 여는 게 아니라 `WidgetActionReceiver` broadcast → 서비스 바로 시작
  - 프리셋 기본값(defaultDurationMin, strictByDefault) 으로 시작
  - 권한 없으면 토스트 + MainActivity 로 이동
  - 이미 세션 진행 중이면 무시
- **위젯 UI 상태 반영형으로 재설계**
  - 비활성: 상단 "포커스온 / 프리셋 탭하면 바로 세션 시작" · 4칩 모두 선명
  - 활성: 상단 "📚 수험생 중 · 12:34" · 현재 모드 칩만 선명, 나머지 프리셋은 흐림
  - **OFF 칩** 추가 (맨 왼쪽)
    - 활성 세션: 빨강, 탭하면 세션 종료 (엄격모드 제외)
    - 비활성: 회색, 탭해도 no-op (앱 열기만)

### Removed
- `MainActivity` 의 `widget_preset_id` 자동 펼침 로직 (위젯이 직접 서비스 호출하므로 불필요)

## [0.3.0] - 2026-04-18

### Added
- **홈 화면 위젯** — 4×2 크기 (리사이즈 가능)
  - 비활성: 3개 프리셋 칩 (📚 수험생 / 💼 직장인 / 🧘 명상) — 탭하면 해당 프리셋이 펼쳐진 채로 앱 열림
  - 활성 세션: 모드 이름 + 남은 시간(MM:SS) 표시. 탭하면 세션 화면으로
  - 5초마다 자동 갱신 (BlockSessionService ticker 연동)

### Changed
- `MainActivity` `launchMode="singleTop"` — 위젯 재탭 시 새 인스턴스 대신 기존 액티비티 재활용
- `HomeScreen` 에 `initialExpandedMode` 파라미터 추가 (위젯에서 넘어온 프리셋 자동 펼침)

## [0.2.2] - 2026-04-18

### Added
- **세션 시간 직접 입력** — 프리셋 칩(15/30/60/120/240/480분) 오른쪽에 "✏️ 직접 입력" 칩 추가. 탭하면 1~720분 범위 숫자 입력 다이얼로그 열림
- 분 표시를 "150분" 같은 긴 값 대신 "2시간 30분"으로 포맷

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

[Unreleased]: https://github.com/jongju1124-rgb/focuson-android/compare/v0.5.0...HEAD
[0.5.0]: https://github.com/jongju1124-rgb/focuson-android/releases/tag/v0.5.0
[0.4.2]: https://github.com/jongju1124-rgb/focuson-android/releases/tag/v0.4.2
[0.4.1]: https://github.com/jongju1124-rgb/focuson-android/releases/tag/v0.4.1
[0.4.0]: https://github.com/jongju1124-rgb/focuson-android/releases/tag/v0.4.0
[0.3.3]: https://github.com/jongju1124-rgb/focuson-android/releases/tag/v0.3.3
[0.3.2]: https://github.com/jongju1124-rgb/focuson-android/releases/tag/v0.3.2
[0.3.1]: https://github.com/jongju1124-rgb/focuson-android/releases/tag/v0.3.1
[0.3.0]: https://github.com/jongju1124-rgb/focuson-android/releases/tag/v0.3.0
[0.2.2]: https://github.com/jongju1124-rgb/focuson-android/releases/tag/v0.2.2
[0.2.1]: https://github.com/jongju1124-rgb/focuson-android/releases/tag/v0.2.1
[0.2.0]: https://github.com/jongju1124-rgb/focuson-android/releases/tag/v0.2.0
[0.1.0]: https://github.com/jongju1124-rgb/focuson-android/releases/tag/v0.1.0
