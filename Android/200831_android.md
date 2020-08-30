## Hilt와 Dagger의 annotations 사용 정리

[Manuel Vivo의 글 'Hilt and Dagger annotations cheat sheet](https://medium.com/androiddevelopers/hilt-and-dagger-annotations-cheat-sheet-9adea070e495)을 번역하고 정리 하였다.

이 글에서는 dagger-hilt에서 사용되는 어노테이션들을 정리하고 어떻게 사용하는지 간단하게 정리하려 한다. 

- 글 작성시점에 `dagger-hilt`는 `2.28-alpha`버전이 최신 버전이다.

### Hilt and Dagger Annotations 

Hilt는 안드로이드 클래스에 대해 컨테이너를 제공하고 자동으로 수명주기를 관리 하여 앱에서 DI(의존성 주입)를 수행하는 표준 방법을 정의하고 그에대한 API를 제공 한다. Hilt는 DI라이브러리인 Dagger를 기반으로 빌드되므로 Dagger가 제공하는 컴파일 시간, 향상된 런타임 성능, 높은 확장성 및 안드로이드 스튜디오의 지원등의 이점을 누릴 수 있다. 

이 문서를 사용하면 다양한 Hilt및 Dagger의 어노테이션의 기능과 사용법에 대해 빠르게 확인 할 수있다. DI및 Hilt의 자세한 내용은 [이 가이드](https://developer.android.com/training/dependency-injection/hilt-android)를 확인하도록 하자. 단계별 학습 접근 방식을 선호하는 경우 안드로이드의 [Code lab](https://codelabs.developers.google.com/codelabs/android-hilt)에서 Hilt사용을 확인 하도록 하자. 

- [Manuel Vivo가 작성한 원본 Cheat sheet PDF파일 다운로드 경로](https://developer.android.com/images/training/dependency-injection/hilt-annotations.pdf)

#### 1. `@HiltAndroidApp`

안드로이드 `Application`클래스를 상속한 앱의 Application클래스에 추가하는 주석. 어노테이션 프로세서를 통해 생성될 코드들의 시작점이다. 

```kotlin
@HiltAndroidApp
class MyApplication: Application() {    
}
```

#### 2. `@AndroidEntryPoint`

어노테이션을 추가한 안드로이드 컴포넌트 클래스에 DI 컨테이너에 추가 한다. 

```kotlin
@AndroidEntryPoint
class MyActivity: AppCompatActivity() {    
}
```

#### 3. `@Inject`

특정 인스턴스를 외부에서 주입 한다. 주입 대상은 아래와 같다. 

##### 3.1 생성자 주입 

생성자에 선언된 클래스 타입에 따라 해당 인스턴스를 생성하거나 수명주기에 따라 유지중인 인스턴스를 주입시켜준다. 

```kotlin
class SomeViewModel @inject constructor(
    private val service: SomeService
) {    
}
```

##### 3.2 필드 주입

`@AndroidEntryPoint`주석이 추가된 클래스의 멤버 객체의 인스턴스를 생성하거나 수명주기에 따라 유지중인 인스턴스를 주입시켜준다. 필드 주입의 경우 지연된 초기화 방식으로 선언 되어야 하며, `private`은 사용할 수 없다. 

```kotlin
@AndroidEntryPoint
class MyActivity: AppCompatActivity() {    
    @Inject lateinit var service: SomeService
}
```

#### 4. `@ViewModelInject`

Hilt에게 안드로이드 아키텍쳐 컴포넌트 요소중 하나인 `ViewModel`의 생성자 주입임을 알려준다. 

- `ViewModel`의 경우 일반적으로 Factory패턴을 이용하여 인스턴스를 생성하거나 유지중인 인스턴스를 얻는다. 이런 Factory패턴을 직접 만들 필요 없이 dagger에서 제공 하는데 대상 ViewModel의 생성자에 `@ViewModelInject`어노테이션을 작성하면 된다. 
- `ViewModel`의 상태 및 데이터를 유지시키려 하는 경우 `SavedStateHandle`클래스를 사용하는데 ViewModel의 Factory를 사용한다면 `@Assisted`어노테이션을 `SavedStateHandle`에 추가 하여 주입 받을 수 있다. 

```kotlin
class SomeViewModel @ViewModelInject constructor(
    private val service: SomeService,
    @Assisted private val state: SavedStateHandle
): ViewModel() {    
}
```

#### 5. `@InstallIn`

설치할 Module을 Hilt에게 알려준다. 

```kotlin
@InstallIn(ApplicationComponent::class)
@Module
class SomeModule {
}
```

위 예제코드에서는 어플리케이션 스코프의 모듈을 정의한 `SomeModule`클래스를 `ApplicationComponent`클래스에 설치하겠다고 Hilt에게 알려주는 코드이다. 

#### 6. `@Module`

사용할 모듈 클래스들의 인스턴스를 제공하는 방법을 Hilt에게 알려준다. 

```kotlin
@InstallIn(ApplicationComponent::class)
@Module
class SomeModule {
}
```

#### 7. `@Provides`

생성자를 통해 `@Inject`가 불가능한 형식에 대한 바인딩을 추가 한다. `@Provides`어노테이션이 추가된 메소드는 아래를 정보를 Hilt에 제공 해야 한다.  

- 메소드의 반환 타입은 바인딩될 모듈의 타입이다. 
- 메소드의 패러미터들은 바인딩 될 모듈이 의존을 갖는 객체들이다. 
- 메소드의 바디는 해당 모듈의 인스턴스를 제공하는 방법을 Hilt에게 알려준다. Hilt는 해당 인스턴스를 제공해야 할 때마다 메소드의 바디를 실행한다. 

```kotlin
@InstallIn(ApplicationComponent::class)
@Module
class SomeModule {
    @Provides
    fun provideSomeService(converterFactory: GsonConverterFactory): SomeService {
        return Retrofit.Builder()
                    .baseUrl("https://...")
                    .addConverterFactory(converterFactory)
                    .build()
                    .create(SomeService::class.java)
    }
}
```

#### 8. `@Binds`

`@Provides`와는 다르게 인터페이스 모듈의 인스턴스를 추상 클래스를 통해 제공한다. `@Binds`어노테이션은 아래 2개의 정보를 Hilt에 제공 해야 한다. 

- 메소드의 반환 타입은 어떤 인터페이스의 인스턴스를 제공하는지를 Hilt에게 알려준다. 
- 메소드의 패러미터는 인터페이스의 실제 구현 클래스를 Hilt에게 알려준다. 

추가적으로, 모듈은 추상클래스이어야 하며 메소드 또한 추상 메소드 이어야 한다. 

```kotlin
@InstallIn(ApplicationComponent::class)
@Module
abstract class SomeModule {
    @Binds
    abstract fun provideSomeService(someServiceImpl: SomeServiceImpl): SomeService
}
```

#### 9. Scope Annotations

컨테이너의 객체 범위(Scope)를 지정 한다. 

Hilt에서 모듈의 바인딩은 범위가 지정되어 있지 않아 모듈을 요청할 때 마다 새로운 인스턴스를 생성한다. 하지만 바인딩을 특정 구성요소의 범위로 지정하여 인스턴스를 범위내에서 한개의 인스턴스를 존재하게 할 수 있다. 

```kotlin
@Singleton
class SomeService @Inject constructor(
    private val anotherService: AnotherService
) {    
}
```

Hilt는 Dagger와 동일하게 각 스코프에 대한 컴포넌트 클래스를 생성한다. Hilt에서 제공하는 안드로이드 컴포넌트별 범위 지정자는 아래와 같다. 

|안드로이드 클래스|생성될 컴포넌트|범위|
|---|---|---|
|`Application`|`ApplicationComponent`|`@Singleton`|
|`ViewModel`|`ActivityRetainedComponent`|`@ActivityRetainedScope`|
|`Activity`|`ActivityComponent`|`@ActivityScoped`|
|`Fragment`|`FragmentComponent`|`@FragmentScoped`|
|`View`|`ViewComponent`|`@ViewComponent`|
|`@WithFragmentBindings`어노테이션이 지정된 `View`|`ViewWithFragmentComponent`|`@ViewScoped`|
|`Service`|`ServiceComponent`|`@ServiceScoped`|

#### 10. Qualifiers for predefined bindings

안드로이드 컴포넌트에 대해 종속성을 가진 사전 정의된 바인딩이다. 사전 한정자는 `@ApplicationContext`와 `@ActivityContext`를 제공 한다. 

```kotlin
@Singleton
class SomeService @Inject constructor(
    @ApplicationContext val context: Context,
    private val anotherService: AnotherService
) {    
}
```

#### 11. `@EntryPoint`

Hilt에서 지원하지 않는 클래스에 대해 의존을 갖는 필드를 삽입할 때 사용 한다. 예를 들어 안드로이드 컴포넌트 중 `ContentProvider`는 Hilt에서 지원하지 않는데, 이를 `@EntryPoint`를 사용해 진입점을 적용하고 `@InstallIn`을 추가하여 설치할 컴포넌트를 지정하면 된다. 

```kotlin
class SomeContentProvider: ContentProvider() {
    @EntryPoint
    @InstallIn(ApplicationComponent::class)
    interface SomeContentProviderEntryPoint() {
        fun someService(): SomeService
    }

    override fun query(...): Cursor {
        val appContext = context?.applicationContext ?: throw IllegalStateException()
        val hiltEntryPoint = EntryPointAccessors.formApplication(appContext, SomeContentProviderEntryPoint:: class.java)
        val someService = hiltEntryPoint.someService()

        // ... 
    }
}
```

