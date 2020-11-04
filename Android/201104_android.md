## 넷플릭스의 안드로이드와 iOS앱 - 코틀린 멀티플랫폼으로 만들어지다 

> 이 글은 Netflix Technology Blog의 [Netflix Android and iOS Studio Apps - now powered by Kotlin Multiplatform](https://netflixtechblog.com/netflix-android-and-ios-studio-apps-kotlin-multiplatform-d6d4d8d25d23)을 번역 하였다. 

지난 몇 년 동안 Netflixㅇ는 TV프로그램과 영화의 제작을 혁신적으로 제공하기 위해 Prodicle이라는 모바일 앱을 개발해 왔다. 실제 영화의 제작은 빠르게 진행되고 있으며 국가, 지역, 심지어 각 제작마다 요구 사항이 크게 다르다. 제작 작업의 특성상 분산 환경에서 사용자의 1/3미만이 작업에 대해 안정적인 연결을 제공하고 오류에 대한 한계가 제한적인 기기에서 작업이 많은 소프트웨어를 개발하고 있음을 의미 한다. 이러한 이유로 소규모 엔지니어링 팀 으로서 진화하는 고객의 요구를 성공적으로 충족하려면 제품을 제공하기 위해 안정성과 속도를 최적화 해야 함을 알게 되었다. 

신뢰할 수 없는 네트워크 연결 가능성이 있을 수 있기 떄문에 강력한 클라이언트 측 지속성 및 오프라인 지원을 위한 모바일 솔루션이 필요하게 되었다. 빠른 제품을 제공하기 위한 필요성으로 인하여 [멀티 플랫폼 아키텍쳐](https://netflixtechblog.com/making-our-android-studio-apps-reactive-with-ui-components-redux-5e37aac3b244)를 테스트 하게 되었다. 이제 [Kotlin Multiplatform](https://kotlinlang.org/lp/mobile/)을 이용하여 Kotlin에서 플랫폼에 구애받지 않는 비즈니스 로직을 한번만 작성 하고 [Kotlin/Native](https://kotlinlang.org/docs/reference/native-overview.html)코드를 통해 Android용 Kotlin라이브러리와 iOS용 네이티브 공통 프레임 워크로 컴파일 하는 방식으로 한단계 더 나아가고 있다. 

![m_archi](https://miro.medium.com/max/700/1*p4iArxP5Q8XEcBmDPHchtw.png)

### 1. Kotlin Multiplatform

Kotlin Multiplatform을 사용하면 iOS및 Android앱의 비즈니스 로직에 단일 코드 베이스를 적용할 수 있다. 예를 들어 네이티브UI를 구현하거나 플랫폼 별 API로 작업할 때 필요한 경우에만 플랫폼 별 코드를 작성하면 된다. 

Kotlin Multiplatform은 이 업계에서 잘 알려진 기술과는 다른 방식으로 교차 플랫폼(cross-platform) 모바일 개발에 접근 한다. 다른 기술들이 플랫폼 별 앱 개발을 추상화하거나 완전히 대체하는 경우 Kotlin Multiplatform은 기존 플랫폼 별 기술을 보완하며 플랫폼에 구애받지 않는 비즈니스 로직을 대체하는데 적합 하다. 

이 접근 방식은 다음과 같은 여러가지 이유로 효과적이다.

1. Android및 iOS앱은 두 플랫폼 모두에 유사한 또는 경우에 따라 동일한 비즈니스 로직이 작성된 공유 아키텍쳐를 가지고 있다. 
2. Android및 iOS앱에 있는 프로덕션 코드의 약 50%는 기본 플랫폼에서 분리 된다. 
3. 각 플랫폼(Android jetpack compose, Swift UI등)에서 제공하는 최신 기술을 적용하기 위해서 비즈니스 로직은 방해되어서 안된다. 

자, 그래서 우리는 이 것으로 무엇을 해야 하는가? 

### 2. Experience Management 

앞서 언급했듯이 사용자 요구 사항은 프로덕션마다 크게 다르다. 이는 기능 가용성을 전환하고 각 프로덕션의 인 앱 경험을 최적화 하기 위한 많은 앱 구성으로 변환 된다. 앱 자체에서 이러한 구성을 관리하는 코드를 분리하면 앱이 성장함에 따라 복잡성을 줄이는 데 도움이 된다. 코드 공유에 대한 첫번째 탐색에는 내부 경험 관리 도구인 Hendrix를 위한 모바일 SDK의 구현이 포함된다. 

핵심적으로 Hendrix는 구성 값을 계산하는 방법을 표현하는 간단한 해석 언어이다. 이러한 방식은 앱 세션 컨텍스트에서 A/B테스트, 지역성, 장치 속성과 괕은 데이터에 엑세스 할 수 있다. 

사용자 활동에 대한 응답으로 자주 변경되는 구성 값과 결합 된 네트워크 연결 불량은 장치 내 규칙 평가가 서버측 평가 보다 선호됨을 의미 한다. 

이로 인해 우리는 가벼운 Hendrix모바일 SDK를 구축하게 되었다. Kotlin Multiplatform은 중요한 비즈니스 로직을 필요로 하고 전적으로 플랫폼에 구애받지 않기 때문에 훌륭한 후보 이다. 

### 3. Implementation

빠른 이해를 위해 Hendrix관련 세부 정보를 건너 뛰고 Kotlin/Swift대신 Kotlin Multiplatform을 사용 하는 것 과 관련된 몇가지 차이점에 대해 설명하도록 하겠다. 

#### 3.1 Build

Android의 경우 기존처럼 개발 한다. Hendrix Multiplatform SDK는 다른 종속성을 가진 라이브러리와 동일한 방식으로 gradle을 통해 가져온다. iOS의 경우 네이티브 라이브러리가 범용 프레임 워크로 Xcode프로젝트에 포함되어 있다. 

#### 3.2 Developer ergonomics (개발자 디버깅을 말하는 듯)

Kotlin Multiplatform소스 코드는 수정, 재 컴파일 할 수 있으며 Android Studio및 Xcode(lldb 지원 포함)에서 breakpoint들 과 함께 디버거를 연결할 수 있다. Android studio의 경우 문제없이 즉지 작동하며 Xcode지원은 TouchLabs의 [xcode-kotlin](https://github.com/touchlab/xcode-kotlin)플러그인을 통해 이루어 진다. 

![debugging kotlin source code from xcode](https://miro.medium.com/max/700/0*whpdb2hoa55wg_uo)

#### 3.3 Networking

Hendrix는 장치에 다운로드 되는 원격 구성 파일인 규칙 집합(rule set(s))을 해석 한다. [Ktor](https://ktor.io/)의 Multiplatform HttpClient를 사용하여 SDK내에 네트워킹 코드를 포함하고 있다. 

#### 3.4 Disk cache

몰론 네트워크 연결을 항상 사용할 수 있는 것은 아니므로, 다운로드 한 규칙 세트(rule sets)를 디스크에 캐시해야 한다. 이를 위해 멀티 플랫폼 지속성을 위해 Android및 기본 데이터베이스 드라이버와 함께 [SQLDelight](https://cashapp.github.io/sqldelight/)를 사용 하고 있다. 

### 4. Final thoughts (from Netflix)

우리는 지난 몇 년 동안 Kotlin Multiplatform의 발전을 예리하게 따라갔으며, 기술이 inflection point에 도달 했다고 믿는다. Xcode의 도구 및 통합 빌드 시스템이 크게 향상되었기 때문에 유 유지 관리 할 필요성이 없다는 잇점이 더 크다. 

Android및 iOS앱 간에 추가 코드를 공유할 수 있는 기회가 많다. 자바 스크립트도 앞으로 가능할 수 있다는 점을 고려 하면 기술의 잠재적인 향후 발전 가능성에 대해서는 매우 흥미로워 진다. 

우리는 모바일 앱을 공유 된 비즈니스 로직을 사용하여 얇은(thin) UI레이어로 발전시킬 수 있는 가능성에 기대 하며 이 여정에서 학습한 내용을 계속해서 공유 할 것 이다. 