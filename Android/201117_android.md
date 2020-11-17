## Kotlin으로 Dagger-Hilt를 사용할때 가이드 - Android에서 의존성 주입을 사용하는 쉬운 방법 

> 이 글은 Filip Stanis의 [A pragmatic guide to Hilt with Kotlin - An easy way to use dependency injection in your Android app](https://medium.com/androiddevelopers/a-pragmatic-guide-to-hilt-with-kotlin-a76859c324a1)을 번역 하였다. 

[Hilt](https://developer.android.com/training/dependency-injection/hilt-android)는 Dagger를 기반으로 만들어진 새로운 Dependency Injection(의존성 주입) 라이브러리로 Android앱에서의 사용을 더 간단하게 해준다. 이 가이드는 안드로이드 프로젝트에서 Hilt를 사용할 때 도움이 되는 몇가지 코드 스니펫(code snippets)을 기준으로 핵심적인 기능을 설명할 것 이다. 

### 1. Setting up Hilt

안드로이드 앱 에서 Hilt를 설정하려면 먼저 [Gradle빌드 설정 가이드](https://dagger.dev/hilt/gradle-setup)를 따라하도록 한다. 

모든 플러그인을 설치 한 뒤 `@HiltAndroidApp`주석을 `Application`클래스를 상속한 클래스에 추가 하여 Hilt를 사용 한다. 이 외 다른 작업을 필요로 하지는 않는다. 

```kotlin
@HiltAndroidApp
class MyApplication : Application()
```

### 2. Defining and injecting dependencies

의존성 주입을 사용하는 코드를 작성할 때 고려해야 할 두가지 주요 구성요소가 있다. 

1. 주입될 클래스 인스턴스
2. 의존을 갖고있는 주입 대상 클래스 

이 둘은 상호 배타적이지 않으며 클래스는 주입 가능하고 의존을 갖고 있다. 

#### 2.1 Make a dependency injectable

Hilt에서 무언가를 주입 가능하게 만드려면 Hilt에게 해당 인스턴스를 만드는 방법을 알려주어야 한다. 이러한 방법들을 바인딩(binding)이라고 한다. 

Hilt에서 바인딩을 정의하는 방법은 세가지가 있다. 

##### 2.1.1 생성자에 `@Inject`주석 추가 하기

모든 클래스는 `@Inject`주석이 달린 생성자를 적용 할 수 있으므로 프로젝트의 어느 곳 에서나 의존성 주입을 사용할 수 있다. 

```kotlin
class DatMilk @Inject constructor() {
    // ... 
}
```

##### 2.1.2 모듈에 적용 하기

Hilt에서 무언가를 주입 가능(인스턴스의 생성과 이를 제공하는 방법)하게 만드는 나머지 두가지 방법은 모듈을 사용 하는 것 이다. 

Hilt의 모듈은 인터페이스나 시스템 서비스와 같이 인스턴스를 만다는 방법을 Hilt에게 알려주는 "레시피"들의 모음으로 생각 할 수 있다. 

또한 테스트중 제공될 모듈들은 다른 모듈들로 쉽게 교체할 수 있다. 예를 들어 인터페이스의 구현을 mock과 같은 테스트용 인스턴스로 쉽게 대체 할 수 있다. 

모듈은 `@InstallIn`주석을 사용하여 지정된 Hilt 컴포넌트 요소에 설치 된다. 이에 대해서는 나중에 더 자세하게 살펴보도록 하자. 

- Option 1: `@Binds`주석을 사용 하여 인터페이스에 대한 바인딩 만들기 

`Milk`클래스의 인스턴스가 요청될 때 실제 코드에서 `OatMilk`라는 인스턴스를 제공하려면 모듈 내부에 추상 메소드를 만들고 `@Binds`주석을 추가 해 준다. 이 방법이 작동하기 위해서는 `OatMilk`자체가 주입 가능해야 하며, 생성자를 `@Inject`로 주석을 추가 해야 한다. 

```kotlin
interface Milk {
    // ...
}

class OatMilk @Inject constructor(): Milk {
    // ... 
}

@Module
@InstallIn(ActivityComponent::class) 
abstract class MilkModule {
    @Binds
    abstract fun bindMilk(oatMilk: OatMilk): Milk
}
```

- Option 2: `@Provides`주석을 사용 하여 (인스턴스 의)팩토리 함수를 제공 하기

인스턴스를 직접 구성할 수 없는 경우 공급자(provider)를 제공 할 수 있다. 공급자는 객체의 인스턴스를 반환하는 팩토리 함수가 된다. 

이에 대한 예제로, `Context`로 부터 얻는 `ConnectivityManager`라는 시스템 서비스의 인스턴스를 얻는 모듈 팩토리 메소드 예제가 있다. 

```kotlin
@Module
@InstallIn(ApplicationComponent::class) 
object ConnectivityManagerModule {
    @Provides
    fun provideConnectivityManager(
         @ApplicationContext context: Context
    ) = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
}
```

`Context`의 인스턴스는 `@ApplicationContext`또는 `@AcitivtyContext`등의 주석을 추가하여 주입 받아 사용할 수 있다. 

##### 2.1.3 Inject a dependency

인스턴스가 이제 주입 가능하다면 Hilt를 사용하여 두가지 방법으로 주입 받을 수 있다. 

1. 생성자의 패러미터로 주입 받는 방법 

```kotlin
interface Milk
interface Coffee

class Latte @Inject constructor(
    private val milk: Milk,
    private val coffee: Coffee
) {
    // ...
}
```

생성자가 `@Inject`주석이 추가 되었다면 Hilt는 주입될 타입에 따라 정의된 바인딩을 통해 모든 매개 변수들을 주입 한다. 

2. 클래스의 멤버 변수로 주입 받는 방법 

```kotlin
interface Milk
interface Coffee

@AndroidEntryPoint
class LetterActivity: AppCompatActivity() {
    @Inject lateinit var milk: Milk
    @Inject lateinit var coffee: COffee

    // ... 
}
```

만약 클래스가 진입점 즉, 주입되어질 대상 이라면 `@AndroidEntryPoint`주석을 지정하면 `@Inject`주석이 추가된 모든 필드에 대해 인스턴스가 주입 된다. (`@AndroidEntryPoint`에 대해서는 다음 섹션에서 설명)

`@Inject`주석이 추가된 필드는 public접근자를 가져야 한다. 또한 주입 전의 초기 값이 `null`이 되므로 `nullable` 인스턴스가 되지 않도록 lateinit으로 만드는 것 이 편리하다. 

주입될 인스턴스를 클래스의 멤버 필드로 갖는 경우 클래스에 `Activity`와 같은 매개변수가 없는 생성자가 있어야 하는 경우에 유용하다고 할 수 있다. 대부분의 경우에는 생성자 패러미터를 통해 의존성 주입을 할 수 있다. 

### 3. Other important concepts

#### 3.1 Entry point

많은 경우에 클래스가 주입되어 생성된뒤 의존성 주입이 진행됨을 기억 하고 있을 것 이다. 어떤 경우에는 의존성 주입을 통해 생성되지 않아씾만 여전히 주입된 클래스가 있다. 이에 대해 좋은 예제로는 Hilt가 아니느 안드로이드 프레임워크에 의해 생성되는 활동들이 될 것 이다. 

이러한 클래스들은 Hilt의 이존성 그래프에 대한 진입점이며, Hilt는 주입이 필요한 의존이 있음을 알아야 한다. 

##### 3.1.1 Android Entry Point

대부분의 진입점은 Android의 진입점 중 하나가 될 것 이다. 

- `Activity`
- `Fragment`
- `View`
- `Service`
- `BroadcastReceiver`

이 경우 `@AndroidEntryPoint`로 주석을 추가 한다. 

```kotlin
@AndroidEntryPoint
class LatteActivity: AppCompatActivity() {
    // ...
}
```

##### 3.1.2 Other entry points

대부분의 앱은 Android 진입점만 필요 하지만 아직 Hilt에서 지원하지 않는 Dagger라이브러리 외 또는 Android 컴포넌트와 인터페이스를 적용 하려는 경우 Hilt에 수동으로 접근하기 위한 고유 진입점을 만들어야 할 수 있다. 임의의 클래스에 대해 진입점을 제공하는 방법에 대해서는 [이 글](https://dagger.dev/hilt/entry-points.html)을 통해 자세하게 알아 볼 수 있다. 

##### 3.1.3 `ViewModel`

`ViewModel`의 경우에는 특별한 경우라고 할 수 있다. 프레임워크에서 인스턴스를 생성해야 하므로 직접 인스턴스화 되진 않지만 Android의 진입점도 아니다. 대신 `ViewModel`은 `@Inject`가 다른 클래스에서 동작하는 방식과 유사하게 `viewModels()`를 사용 하여 생성 될 때에 대해 Hilt가 의존성 주입을 사용할 수 있도록 하는 `@ViewModelInject`주석을 사용하게 된다. 

```kotlin
interface Milk
interface Coffee

class LatteViewModel @ViewModelInject constructor(
    private val milk: Milk,
    private val coffee: COffee
): ViewModel() {
    // ...
}

@AndroidEntryPoint
class LatteActivity: AppCompatActivity() {
    private val viewModel: LatteViewModel by viewModels()
}
```

`ViewModel`의 `SavedStateHandle`을 사용해야 하는 경우 `@Assisted`주석을 추가 하여 생성자 매개 변수로 주입받을 수 있다. 

```kotlin
class LatteViewModel @ViewModelInject constructor(
    @Assisted private val savedState: SavedStateHandle,
    private val milk: Milk,
    private val coffee: COffee
): ViewModel() {
    // ...
}
```

`@ViewModelInject`를 사용하려면 몇가지 의존을 더 추가해야 한다. 이에 대해 자세한 내용은 [Hilt와 Jetpack 통합](https://developer.android.com/training/dependency-injection/hilt-jetpack)을 참고 하도록 하자. 

##### 3.1.4 Components

각 모듈들은 `@InstallIn(<component>)`을 사용하여 지정된 Hilt 컴포넌트 요소에 설치 된다. 모듈의 컴포넌트들은 주로 실수로 인하여 잘못된 위치에 의존성 주입을 하는 것 을 방지하기 위해 사용 된다. 예를 들어, `@InstallIn(ServiceComponent::class)`은 주석이 달려진 모듈의 바인딩 및 공급자가 Activity에서 사용되는 것 을 방지해 준다. 

또한 바인딩 범위(scope)는 모듈이 있는 컴포넌트에 지정할 수 있다.

##### 3.1.5 Scopes

기본적으로 바인딩에 대해서는 스코프가 지정되지 않는다. 위 예제코드들을 보면, `Milk`를 주입 할 때마다 `OatMilk`의 새로운 인스턴스를 주입한다. 이 떄 `@ActivityScoped`주석을 추가 하면 `ActivityComponent`에 대한 바인딩 스코프를 지정하게 된다. 

```kotlin
@Module
@InstallIn(ActivityComponent::class) 
abstract class MilkModule {
    @ActivityScoped
    @Binds
    abstract fun bindMilk(oatMilk: OatMilk): Milk
}
```

모듈의 스코프가 지정되었으므로 Hilt는 Acitivty인스턴스 당 하나의 `OatMilk`인스턴스를 생성하게 될 것이다. 그리고 해당 `OatMilk`인스턴스는 해당 Activity의 수명주기에 연결된다. Activity의 `onCreate()`가 호출될때 인스턴스가 생성되어 주입되고 Activity의 `onDestroy()`가 호출 될 때 인스턴스는 제거 된다. 

```kotlin
@AndroidEntryPoint
class LatteActivity: AppCompatActivity() {
    @Inject lateinit var milk: Milk
    @Inject lateinit var moreMilk: Milk     // 같은 인스턴스가 또 있다
    // ...
}
```

위와 같은 경우 `milk`와 `moreMilk`는 같은 `OatMilk`인스턴스 이다. 그러나 `LatteActivity`의 인스턴스가 있는 경우 각각 고유한 `OatMilk`인스턴스가 존재 한다. 

이에 따라 Activity에 주입된 다른 의존성 주입된 인스턴스는 동일한 스코프를 갖게 되며, 따라서 동일한 `OatMilk`인스턴스를 사용 한다. 

```kotlin
// `Milk`인스턴스는 `LatteActivity`의 수명주기와 연결 되어 있으므로,
// `Fridge`인스턴스가 만들어지기 전에 생성 된다. 
class Fridge @Inject constructor(
    private val milk: Milk
) {
    // ... 
}

@AndroidEntryPoint
class LatteActivity: AppCompatActivity() {
    // 아래의 고유한 한개의 `Milk`인스턴스들을 참조 하고 있다. 
    @Inject lateinit var milk: Milk
    @Inject lateinit var moreMilk: Milk
    @Inject lateinit var fridge: Fridge
    @Inject lateinit var backupFridge: Fridge

    // ...
}
```

스코프는 모듈이 설치된 컴포넌트에 따라 다르다. `@ActivityScoped`는 `ActivityComponent`내부에 설치된 모듈 내부의 바인딩에만 적용 될 수 있다. 

또한 스코프는 주입 된 인스턴스의 수명주기를 결정 하게 된다. 이 경우 `Fridge`및 `LatteActivity`에서 사용 하는 `Milk`의 단일 인스턴스는 `LatteActivity`에 대해 `onCreate()`가 호출 될 때 생성되고, `onDestroy()`에서 제거 된다. 또한 Activity에 대한 `onDestroy()`의 호출을 포함하게 되므로 `Milk`가 컴포넌트의 설정 변경에 의해 인스턴스가 "생존"하지 못함을 의미 한다.이는 `@ActivityRetainedScope`와 같이 수명주기가 더 긴 스코프를 사용 하여 이를 극복 할 수 있다. 

사용가능한 스코프와 각각의 스코프에 해당하는 컴포넌트들에 대한 수명주기 목록은 [Hilt 컴포넌트 요소](https://dagger.dev/hilt/components.html)를 참고 하도록 하자. 

##### 3.1.6 Provider Injection

주입된 인스턴스의 생성을 보다 직접적으로 제어해야 할 경우가 있을 수 있다. 예를 들어 비즈니스 로직에 따라 필요할 때 에만 인스턴스를 하나 또는 여러개 주입해야 할 필요가 있을 수 있다. 이 경우 `dagger.Provider`를 사용 할 수 있다. 

```kotlin
class Spices @Inject constructor() {
    // ...
}

class Latte @Inject constructor(
    private val spiceProvider: Provider<Spices>
) {
    fun addSpices() {
        val spices = spicesProvider.get()   // `Spices`인스턴스르 생성하고 얻는다. 
        // ...
    }
}
```

프로바이더 주입은 의존이나 주입되는 방법에 관계 없이 사용할 수 있다. 주입 되는 모든 것 은 `Provider<>`에 래핑 하여 대신 프로바이더 주입을 사용할 수 있다. 

### 4. 마무리 

의존성 주입 프레임워크(예, Dagger나 Guice)는 전통적으로 크고 복잡한 프로젝트와 연결 되어 왔다. 그러나 사용하기 쉽고 설정이 간단하기 떄문에 Hilt는 코드 베이스의 크기에 상관없이 모든 유형의 앱 에서 쉽게 사용할 수 있으며 Dagger의 모든 기능을 제공 한다. 

Hilt의 작동 방식과 유용한 다른 기능들에 대해서 더 자세하게 알아보고 싶다면 [공식 웹 사이트](https://dagger.dev/hilt/)에서 자세한 참조 문서들을 참고 하도록 하자. 