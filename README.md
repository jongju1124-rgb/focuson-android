# 포커스온 (FocusOn)

한국어 중심 집중 모드 앱 — 수험생 / 직장인 / 명상 프리셋으로 앱·웹사이트를 차단합니다. Galaxy S25 (One UI 7 / Android 15) 기준으로 설계됐지만 **Android 8.0 (API 26) 이상** 모든 기기에서 동작합니다.

**광고 없음 · 결제 없음 · 개인정보 수집 없음** — 모든 데이터는 폰 안에만 저장됩니다.

---

## 다운로드

**최신 릴리즈 APK →** [GitHub Releases](https://github.com/jongju1124-rgb/focuson-android/releases/latest)

---

## 주요 기능

- **앱 차단** — 선택한 앱을 실행하면 즉시 홈으로 복귀 + 전체화면 안내
- **웹사이트 차단** — Chrome / 삼성 인터넷 / Edge / Firefox / Brave / 네이버 웨일 주소창 감지
- **프리셋 3종**
  - 📚 **수험생** — SNS·웹툰·쇼핑·게임·OTT 기본 차단
  - 💼 **직장인** — SNS·OTT·커뮤니티 기본 차단 (업무 앱은 통과)
  - 🧘 **명상** — 전화·시계·본 앱만 허용 (화이트리스트)
- **엄격모드** — 세션 중 접근성 끄기 · 앱 삭제 · 강제 중지 시도 자동 차단
- **Foreground Service** — 프로세스가 죽어도 세션 상태 유지, 부팅 후 자동 복원
- **한국어 UI** · Material 3 디자인

---

## 설치

### 1) 다운로드
GitHub Releases 페이지에서 `app-release.apk` 받기.

### 2) 알 수 없는 출처 허용
처음 받는 APK라면 Android가 차단할 수 있어요:
- 설정 → **앱** → 우측 상단 ⋮ → **특별 접근** → **알 수 없는 앱 설치** → 파일 관리자/Chrome에 권한 부여

### 3) 설치
파일 앱에서 APK 탭 → "설치".

### 4) 권한 세팅
앱 처음 실행 시 **필수 4개 + 선택 2개** 권한을 차례로 허용:

#### 필수
- 알림 (세션 타이머 표시)
- 다른 앱 위에 표시 (차단 안내 오버레이)
- 사용정보 접근 (실행 중인 앱 감지)
- **접근성 서비스** — 앱·웹사이트 차단의 핵심

#### 선택
- 정확한 알람 (예약 세션용)
- 배터리 최적화 제외 (삼성 기기 권장)

### ⚠️ 접근성 허용이 막힐 때 (Android 13+)

사이드로드 앱은 Android 보안 정책상 접근성 서비스가 기본 차단됩니다. 2단계가 필요해요:

**1단계:** 설정 → 앱 → **포커스온** → 우측 상단 **⋮** → **"제한된 설정 허용"** 토글
**2단계:** 앱으로 돌아와 접근성 카드의 "허용하기" 탭 → 접근성 설정에서 "포커스온 차단 서비스" ON

앱 내부에 동일한 단계 가이드 다이얼로그가 있으니 "허용하기" 누르면 자동으로 안내됩니다.

### One UI(삼성) 백그라운드 종료 방지

- 설정 → 배터리 → **백그라운드 사용 제한** → "절대 절전 안 함"에 포커스온 추가
- 설정 → 배터리 → **잠자는 앱** 목록에서 포커스온 **제거**

---

## 사용법

1. 홈에서 프리셋 카드 탭 → 펼침
2. 세션 시간(15분 ~ 8시간) + 엄격모드 선택
3. **시작** → 확인 다이얼로그 → 세션 화면
4. 차단된 앱 실행 → 자동 홈 복귀 + 오버레이
5. 차단된 사이트 접속 → 뒤로가기 + 오버레이

**커스터마이즈:** 홈 카드 → "설정" → 차단 목록 편집 (저장하지 않고 나가면 확인 다이얼로그로 보호)

---

## 빌드 (소스에서 직접)

### 요구사항

| 항목 | 버전 |
|------|------|
| JDK | 17 (AGP 8.7+ 요구) |
| Android SDK | Platform 35, Build-tools 35.0.0+ |
| Gradle | 8.10.2 (wrapper 자동) |

### 절차

```bash
# 1) JDK 17 설치
winget install EclipseAdoptium.Temurin.17.JDK

# 2) Android SDK cmdline-tools 설치 (https://developer.android.com/studio)
#    platform-tools, platforms;android-35, build-tools;35.0.0

# 3) 경로 설정
export JAVA_HOME=/c/Program\ Files/Eclipse\ Adoptium/jdk-17.0.x-hotspot
export ANDROID_HOME=/path/to/Android/Sdk

# 4) local.properties 생성
echo "sdk.dir=/path/to/Android/Sdk" > local.properties

# 5) 빌드
./gradlew assembleDebug           # 디버그 APK
./gradlew assembleRelease         # 릴리즈 APK (R8 + 리소스 축소 적용)
```

출력:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

### Release 서명 키 직접 만들기

```bash
keytool -genkey -v -keystore signing/focuson-release.jks \
  -alias focuson -keyalg RSA -keysize 2048 -validity 10000
```

`keystore.properties` 생성:
```properties
storeFile=signing/focuson-release.jks
storePassword=YOUR_PASSWORD
keyAlias=focuson
keyPassword=YOUR_PASSWORD
```

> `keystore.properties`와 `signing/` 폴더는 `.gitignore` 대상이라 **절대 공개 저장소에 올리지 마세요**. 키를 잃어버리면 후속 업데이트를 같은 앱 ID로 배포할 수 없습니다.

---

## 아키텍처

```
UI (Compose)
    ↓ (직접 Repository 호출, ViewModel 없음)
Data: Room + DataStore + PackageManager
    ↓
Domain: BlockEngine (AtomicReference 싱글톤)
    ↑
Service:
  - AppBlockerAccessibilityService (앱 포그라운드 + URL 감지)
  - BlockSessionService (Foreground Service 타이머)
```

- **BlockEngine** — 접근성 서비스가 O(1)로 읽어 차단 판정
- **엄격모드** — 접근성이 "앱 제거" / "접근성 설정" / "강제 중지" 화면을 감지해 자동 BACK
- **Room `@Transaction`** — 규칙 저장이 원자적 (중간 취소돼도 파손 없음)

---

## 개인정보 처리방침

모든 데이터는 폰의 앱 전용 저장소(`/data/data/com.focuson.app/`)에만 저장되며 네트워크로 전송되지 않습니다. 자세한 내용은 [PRIVACY.md](PRIVACY.md) 참고.

---

## 제한사항

- 안전 모드 부팅 / 공장 초기화로는 우회 가능 (Android 시스템 설계)
- Chrome 시크릿 탭은 접근성 제약으로 URL 파싱 실패할 수 있음
- 삼성 One UI 고유 설정 화면 패키지명 전부 커버하진 못함
- Play Store 배포용 아님 (접근성 정책 리스크)

---

## 라이선스

개인 사용 목적 · 수정·재배포 자유 · 무보증.
