
이 문서에서는 기존 Dagger 를 사용 했을때 발생하는 문제들에 대해서 `dagger.android` 의 API 를 사용 하여 단순화 시켜보는 내용들에 대해서 정리 하였다. 

### 1. Android with Dagger issue

Dagger 에 대해서 잘 알려진 것은 `Component` 인터페이스를 선언 하고 `Module` 클래스를 만들어 Component 에 인스턴스를 제공 하기 위해 provider 메소드를 만든뒤, 필요한 안드로이드 컴포넌트 등 에서 생성자, 클래스 멤버 등에 어노테이션을 적용 하여 주입 받는 형태 이다. 예를 들면 아래와 같다. 

```kotlin
@Component(modules = [ SomeModule::class ])
interface SomeComponent {
    // 모듈 인스턴스 제공 api method
    fun service(): Service
    fun module(service: Service): SomeModule

    // injector(to parameter)
    fun inject(activity: SomeActivity)
    fun inject(fragment: SomeFragment)    
}

@Module
class SomeModule {
    @Provide
    fun provideService(): Service {
        return ServiceImpl()
    }

    @Provide
    fun provideModule(service: Service): SomeModule {
        return SomeModuleImpl(service)
    }
}

// injector fragment
class SomeFragment: Fragment() {
    @inject lateinit var someModule: SomeModule 

    // ... 
}
```

이는 잘 알려진 Dagger 의 기본적인 사용 방법이다. 하지만 위 방법은 문제가 있는데 아래와 같은 보일러플레이트 코드가 발생 하는 점 이다. 

```kotlin
class SomeActivity: Activity() {
    @Inject lateinit var service: Service

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (getContext() as SomeApplication).getApplicationContext()
            .getApplicationComponent()
            .activity(this)
            .build()
            .inject(this)

        // 이제 service 인스턴스를 사용 할 수 있다. 
    }
}
```

Activity 의 `onCreate()` 콜백 메소드내 에서 Application Context를 가져오고 Application Componenet 를 가져온뒤 하위 컴포넌트인 Activity Component 를 얻고 `Service` 컴포넌트의 인스턴스를 주입하기 위해 `inject()` 메소드를 콜 하는 경우 이다. 이러한 코드는 Activity, Fragment 등 안드로이드 컴포넌트가 새로 생성될 때 마다 추가 해주어야 한다. 그러면서 반복되는 코드가 발생 한다. 

반복되는 코드를 옳기기 위해 복사-붙여넣기를 자주 사용 하게 되고 해당 코드는 실제 작업자 외 에는 아무도 모를 수도 있다. 직접 코드를 봐야만 이해할 수 있는 코드가 생겨 추 후 리팩토링에 불리함이 발생할 것 이다. 

DI를 사용 하는 가장 중요한 핵심읜 "의존성 분리" 이다. 위 예제에서 예를 들면 `SomeActivity` 는 `Service` 라는 인터페이스 와 의존을 갖고 있지만 실제로 주입 받는 대상은 `Service` 의 구현 클래스의 인스턴스 이다. 하지만 `SomeActivity` 는 `Service` 라는 인터페이스만 알고 있기 때문에 의존성이 분리 되어 있다. 

하지만 위 코드의 경우 `Service` 는 인터페이스로 되어 있음에도 불구 하고 `SomeActivity` 에서는 주입을 위해 ApplcationContext 를 가져와서 ApplicationComponent 를 필요로 하고 `inject()` 메소드를 통해 주입을 받아야 한다. 주입 대상인 `SomeActivity` 은 의존성 주입을 주입을 위해서 사용 되는 것 들에 대해서는 알 필요가 없다. 

그래서 `dagger.android` 에서 제공 되는 API 를 통해서 보일러 플레이트를 줄이고 의존성 분리 원칙에 위배되는 문제들을 좀더 단순화 하여 안드로이드 컴포넌트에 알맞게 사용 하는 방법에 대해서 정리 하려 한다. 

이 문서는 [Dagger dev](https://dagger.dev/dev-guide/android)가이드 문서를 참고 하여 작성하였다. 

#### 1.1 `dagger.android`

`dagger.android` 패키지 아래의 클래스에서 제공 되는 API를 활용 하여 반복되며 유지, 보수가 어려운 보일러 플레이트 코드를 줄일 수 있다. 이는 안드로이드 컴포넌트와 Androidx(Jetpack) 들과 Dagger의 원할한 사용을 위해서는 새로운 선택지가 되어 주었다. 몰론 추가된 API 와 개념을 학습 해야만 제대로 사용 할 수 있다. 

#### 1.2 Application component 

Application component 를 사용 하기 위해서는 컴포넌트 팩토리를 통해 `Context` 를 갖고 `ApplicationComponent` 를 반환하는 `Factory` 를 정의 한다. 여기서의 Factory 는 호출시 새로운 컴포넌트의 인스턴스를 `Provider` 함수를 통해 생성 하여 반환한다. 

`Bindsinstance` 어노테이션은 컴포넌트 Builder내 의 메소드에 추가 하거나 컴포넌트 Factory의 parameter로 추가 하여 해당 인스턴스를 컴포넌트가 가지고 있는 특정한 키에 바인딩 하게 해 준다. 

```kotlin
@Singleton
@Component(modules = [ApplicationModule::class]) 
interface ApplicationComponent: AndroidInjector<SomeApplication> {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance applicationContext: Context): ApplicationComponent
    }

    fun someService(): SomeService
}

@Module
object ApplicationModule {
    @Singleton
    @Provides
    fun provideSomeService(): SomeService {
        return SomeServiceImpl()
    }
}
```

위에서 정의 한 `Factory` 인터페이스는 컴파일러에 의해 `DaggerApplicationComponent` 의 내부 정적 클래스를 통해 `factory()` 메소드를 통해 인스턴스화 한다. 그 내용은 아래와 같다. 

```java
public final class DaggerApplicationComponent implements ApplicationComponent {
    private Provider<Context> applicationContextProvider;
    // ...
    public static ApplicationComponent.Factory factory() {
        return new Factory();
    }

    // ...
    private static final class Factory implements ApplicationComponent.Factory {
        @Override
        public ApplicationComponent create(Context applicationContext) {
            Preconditions.checkNotNull(applicationContext);
            return new DaggerApplicationComponent(applicationContext);
        }
    }
    // ...
}
```

`Factory` 인터페이스의 `create()` 메소드의 패러미터에 `Bindsinstance` 어노테이션 으로 인해 추가됬음을 확인 할 수 있다. 


#### 1.2 Activity에 주입

```kotlin
@Subcomponent(modules = [SomeActivityModule::class]) 
interface SomeActivityComponent: AndroidInjector<SomeActivity> {
    @Subcomponent.Factory
    interface Factory: AndroidInjector.Factory<SomeActivity>
    // more components 
}

@Module(subcomponents = [SomeActivityComponent::class])
abstract class SomeActivityModules {
    @Binds
    @IntoMap
    @ClassKey(SomeActivity::class)
    abstract fun bindSomeActivityFactory(factory: SomeActivityComponent.Factory): AndroidInjector.Factory<*>
}

// application component
@Singleton
@Component(modules = [ApplicationModule::class, SomeActivityModules::class]) 
interface ApplicationComponent: AndroidInjector<SomeApplication> {
    // 생략
}
```

`Subcomponent` 어노테이션으로 작성된 `SomeActivity`의 라이프 사이클과 동기화된 컴포넌트 `SomeActivityComponent`의 Factory 를 작성 한다. 그리고 정의된 sub component를 `ApplicationComponent` 에 추가 하여 컴포넌트의 계층에 추가 시켜준다. 

그 다음으로는 `Application`에 `HasAndroidInjector` 인터페이스를 구현하도록 하여 `androidInjector()` 함수를 구현한다. 

```kotlin
class SomeApplication: Application, HasAndroidInjector {
    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun onCreate() {
        super.onCreate()
        DaggerYourApplicationComponent.create()
            .inject(this)
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }
}
```

위 방법의 경우 `DaggerApplication` 이라는 추상 클래스를 상속하는 방법도 있다. 

`AndroidInjector.inject()`를 호출 시 `DispatchingAndroidInjector<Any>`를 `Application`으로 부터 받아 어노테이션으로 정의된 액티비티에 `inject()` 메소드 를 통해 주입된다. 

#### 1.3 제공되는 유형

Dagger 에서 제공되는 기본 유형은 `DaggerFragment, DaggerActivity, DaggerService, DaggerIntentService, DaggerBroadcastReceiver, DaggerContentProvider, DaggerApplication` 으로 안드로이드 컴포넌트를 대상으로 제공된다. 복잡한 클래스 상속관계를 갖지 않았다면 제공되는 클래스들을 상속받는것 을 추천 하며 필요한 경우 `applicationInjector()` 함수를 재정의 하면 된다. 

필요한 경우 `dagger.android.support` 지원 라이브러리 패키지를 참고 하도록 하자. 

### 일단 결론

> 작은 프로젝트 일 때 : Koin  
> 큰 프로젝트 일 때 : Dagger

Dagger는 그 단점들에도 불구 하고 여전히 선택할 가치가 많은 매우 강력하며 효율높은 도구 이다. 하지만 안그래도 매우 높고 높던 러닝커브에 `dagger.android` 가 생기고 새롭게 제공된 API에 대한 개념이 어려워서 예전보다 다루기 더 어려워진것 같다. 그래도 러닝커브를 넘어 서는 많은 장점들 때문에 사용할 수 밖에 없다고 생각 된다. 

다른 DI 도구로 [Koin](https://insert-koin.io/)이 있다. 순수한 코틀린으로 만들어진 이 도구는 Dagger와 다르게 직관적이며 다루기 쉽다는 장점이 있지만 `Service locator pattern`특유의 단점으로 런타임시 예외 핸들링이 필수 이며 런타임시 발생되는 오버헤드에 대해 Dagger보다 뚜렷한 장점을 갖고 있지 않다는 단점이 있다. 그래도 Koin에서는 Kotlin의 장점들을 그대로 모두 사용 할 수 있어서 서로 상대적인 장-단점을 갖고 있다고 생각 한다. 

Koin과 Dagger 의 선택은 프로젝트의 크기(예를 들면 보여질 화면과 추가될 컴포넌트의 갯수, 모듈간 의존성)에 따라 선택 하면 좋다. 작은 프로젝트에 가벼운 의존을 갖는 컴포넌트 간 관계 라면 Koin을, 많은 화면과 모듈-컴포넌트 그리고 복잡한 의존관계가 예상 되며 런타임 시 모든 오류의 핸들링에 대한 자신이 없다면 Dagger를 선택 하는것을 추천 한다. 

ps. Dagger는 공부를 더 해야겠다.. 문서를 기반으로 번역하면서 정리 하고 예제 프로젝트로 공부 함에도 아직도 어렵게만 느껴진다. 일단 돌아는 가지만 개념 파악하기 너무나도 어렵다. 어노테이션 프로세서 특유의 디버깅 메세지는 마치 난독화 처리 되어서 나오는 것 같다.. 

ps2. 아직 정리가 부족한 multi binding, scope, qulifier, producer, testing 등 이 있다. 이것들은 나중에 천천히 정리 해 보아야겠다.