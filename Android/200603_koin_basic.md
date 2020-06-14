## 1. Koin

Koin 은 Service locator pattern을 기반으로 만들어진 도구 이다. 이는 Dependency injection tool 으로 사용 할 수 있다. 이 패턴은 장,단점을 명확하게 갖고 있다. Koin 은 순수 코틀린으로만 작성 되어 있고 다른 라이브러리나 APT 등에 대한 디펜던시가 없어 가볍게 사용하기에 좋다. 

아래 Koin 에 대해서는 [Koin core documents](https://doc.insert-koin.io/)를 참고하여 작성 하였다. 

### 1.1 Service Locator Pattern

Service locator pettern 은 서비스를 구현한 클래스의 인스턴스는 숨겨진채 로 외부 어디에서든 서비스에 접근하여 해당 서비스를 사용 하는 패턴이다. 개발자는 이 서비스의 구현은 몰라도 되며 제공될 기능들을 서비스에서 제공 하는 인터페이스들을 구현해 주기만 하면 된다. 

DI 로 사용되는 Service locator pattern 은 서비스 그 자체인 코어 와 외부에서 각 서비스를 통해 제공될 인터페이스를 통해 DI 에서 사용 될 인스턴스를 생성, 반환 해주는 코드만 작성해주면 된다. 이 패턴에서 사용 되는 구체 서비스 자체는 보통 싱글탠 패턴을 갖고 있어 정확히 보면 싱글턴을 래핑한 인터페이스 라고 봐도 무방하다. 

이 패턴은 런타임 시점에 필요한 상황에 따라 인스턴스를 생성 하거나 가져오게 한다. 이 때 인스턴스가 존재 하지 않으면(null) 예외를 발생 하게 되며 이는 런타임 시점에 에러핸들링이 필수 이며 DI 로 사용 하고 있을 떄 에는 DI 모듈들의 디자인이 중요해 진다. 필요 시점에 명확하게 해당 인스턴스를 제공 해야 하기 때문이다. 

### 2. Modules definitions

Koin 에서 기본적으로 모듈을 정의 하기 위해선 `module()`  함수를 통해서 한다. 

```kotlin
val modules = module {
  // 여기에 모듈들을 정의 한다. 
}
```

#### 2.1 `single`

```kotlin
class SomeService() 

val modules = module {
  single { SomeService() } 
}
```

`single` 함수를 통해 제공될 모듈은 싱글턴 패턴을 사용 하여 runtime 중 단 한개의 인스턴스를 보장 한다. `single` 함수로 제공 될 모듈의 인스턴스를 요청 시 이미 만들어진 인스턴스를 제공 하며 런타임 중 에는 다시 생성하지 않는다. (안드로이드의 경우 완전히 재시작 되지 않는 이상) 

#### 2.2 `factory` 

```kotlin
class SomeModel() 

val modules = module {
  factory { SomeModel() }
}
```

`factory` 는 해당 클래스의 인스턴스를 호출시 마다 생성해서 준다. 생성된 인스턴스는 요청할 떄마다 새로운 인스턴스가 생성되기 때문에 새로운 인스턴스는 이전에 생성된 인스턴스와 전혀 다르며 내부 또한 다르다. 

#### 2.3 binding an interface 

`single` 과 `factory` 두개의 함수는 기본적으로 제네릭 타입 `T` 를 반환하는 `single { T }`와 같은 형태를 갖고 있다. (코틀린의)제네릭 타입을 적용 함 으로서 코드가 더 유연해지는 장점이 있다고 할 수 있다. 

하지만 interface 혹은 추상 클래스등 상속관계를 가진 모듈의 정의에 대해서는 조금 더 고민해 볼 필요가 있다.
예제로 아래와 같은 인터페이스와 그를 구현핸 클래스가 있다고 가정해 보자. 

```kotlin
interface Service { 
  fun doSomething()
}

class ServiceImpl(): Service {
  override fun doSomething() {
    // ...
  }
}
```

주입받는 측 에서는 `ServiceImpl` 클래스 보다는 `Service` 라는 인터페이스만 알고 있는게 좋다. 실제 비즈니스에서는 `ServiceImpl` 이 변경 되었어도 `Service` 가 변경되지 않아 주입 받는측 에서는 수정이 필요하지 않기 때문이다. 

그렇기 때문에 module 에서는 아래와 같이 타입 캐스팅을 통해 `ServiceImpl` 이 아닌 `Service` 로 모듈을 캐스팅 할 수 있다. 

```kotlin
val modules = module {
  single { ServiceImpl() as Service }
  // 혹은 
  single<Service> { ServiceImpl() }
}
```

#### 2.4 `bind`

모듈에 대한 추가적인 타입 정의를 `bind` 함수를 통해 할 수 있다. 

```kotlin
interface Service { 
  fun doSomething()
}

class ServiceImpl(): Service {
  override fun doSomething() {
    // ...
  }
}
```

위와 같은 인터페이스와 구현 클래스가 있을경우 아래처럼 bind 처리 해 줄 수 있다. 

```kotlin
val module = module {
  single {
    ServiceImpl()
  } bind Service::class
}
```

#### 2.5 name & default bindings

모듈 정의에 대해 qualifier 로 문자열인 이름을 두어 구분하게 할 수 있다. 예를 들어 하나의 인터페이스를 구현한 클래스 모듈을 여러개 선언 한 경우 Koin 에서는 단 하나의 클래스만 가지고 있을 수 있기 때문에 이를 구분하기 위해서 `named` 라는 키 를 두어 구분하게 하는 것 이다. 

```kotlin
val module = module {
  single<Service>(named("default")) {
    ServiceImpl()
  }

  single<Service>(named("service")) {
    ServiceImpl()
  }
}

val service: Service by inject<Service>(name = named("default"))
```

예를 들어 아래와 같은 모듈선언도 있을수 있는데, 

```kotlin
val module = module {
  single<Service> { 
    ServiceImpl1()
  }

  single<Service>(named("service")) {
    ServiceImpl2()
  }
}
```

- `val service: Service by inject()` 의 경우 `ServiceImpl` 인스턴스를 주입받게 된다. 
- `val service: Service by inject(named("service"))` 일 경우엔 `ServiceImpl2` 인스턴스를 주입 받는다.

#### 2.6 Declaring injection paramters

모듈 주입시점 에서 생성자를 통해 특정 값, 인스턴스등 을 필요할 경우가 있다. 몰론 Koin 모듈내 에서 제공되는 인스턴스의 경우 `get()` 함수를 통해 쓸 수 있지만 주입 대상의 값등에 의존이 있을 경우 람다를 통해 값을 제공 할 수 있다. 

```kotlin
class MessageHelper(
  val context: Context
)

class SomeViewModel: ViewModel() {
  val context: Context
  val messageHelper: MessageHelper by inject {
    parametersOf(context)
  }
  // ... 
}
```

위 예제의 경우 `MessageHelper` 라는 클래스는 생성자에서 `Context` 클래스의 인스턴스를 필요로 한다. 이 Context 의 인스턴스가 주입 대상 클래스인 `SomeViewModel`에서 보유 하고 있을 때 이를 `parametersOf()` 함수를 이용 하여 해당 모듈에 제공한다. 

#### 2.7 Create instance at start

`single` 모듈은 싱글턴이긴 하지만 기본적으로 lazy initialize 라서 최초 호출 시점에 인스턴스가 생성된다. 하지만 앱의 실행과 동시에 해당 모듈의 인스턴스를 만들게 하려면 해당 모듈에 `createAtStart` 프로퍼티를 `true`로 하면 된다. 

```kotlin
val module = module {
  single<Service>(createAtStart = true) {
    ServiceImpl()
  }
}
```

### 3. get & inject components

Koin 에서 모듈의 주입방법은 2가지가 있다. 

- `by` 키워드와 `inject()` 함수를 사용 한 위임 방법
- `get()` 함수를 이용한 주입. 

```kotlin
val modules = module {
  single<Service> { ServiceImpl() }
  single<Service>(named("service")) { ServiceImpl2() }
}

val service = get<Service>()
val service2 = get<Service>(named("service"))
```

### 4. Scope 

Scope 는 주입될 인스턴스에 생명주기를 두어, 사용 할수 있는 범위를 갖게 한다. 기본적으로 `single, factory` 정의 된 모듈은 각가의 scope 를 갖고 있다. 

- `single` : 앱이 실행되는 동안 존재 하는 싱글턴 인스턴스이며 제거 할 수 없다. 
- `factory` : 호출 시 마다 새 인스턴스를 만든다. 생성된 인스턴스는 사용하지 않으면 GC에 의해 제거 된다. 

`scope()` 함수를 사용하면 한정된 시간 혹은 특정 단위로 인해 인스턴스가 유지된다. 

```kotlin
val module = module {
  scope(named("scope_name")) {
    scoped { ServiceImpl() }
  }
}
```

`scope` 함수를 적용하기 위해서는 위처럼 문자열 qualifier, 혹은 class type qualifier 를 적용 해야 한다. 

#### 4.1 Using scope

Scope에 대해 예제를 이용해서 알아보자. 

```kotlin
class A
class B
class C
```

위와 같은 클래스 3개가 있을 경우, `B`, `C` 인스턴스들에 대해 스코프 범위를 적용 한다. 

```kotlin
val module = module {
  factory { A() }
  scope<A> {
    scoped { B() }
    scoped { C() }
  }
}
```

B, C 의 인스턴스는 `factory()` 로 생성된 A 인스턴스의 범위에 영향을 받는다. 

```kotlin
// Koin의 main scope(default-factory) 로 적용된 인스턴스 주입
val a: A = koin.get<A>()

// `a` 인스턴스의 스코프를 가져온다. 
val scopeForA = a.getOrCreateScope()

// `a` 인스턴스로부터 스코프가 적용된 b, c 인스턴스를 주입 한다.
val b = scopeForA.get<B>()
val c = scopeForB.get<C>()
```

혹은 다른 방법으로 `scope()` 클래스 프로퍼티를 사용 한다. 

```kotlin
val a: A = koin.get<A>()

val b = a.scope.get<B>()
val c = a.scope.get<C>()
```

얻은 Scope 를 제거 하고 연결된 인스턴스들을 같이 모두 제거 하려하면 `closeScope()` 함수를 이용 한다. 

```kotlin
a.closeScope()
```

### 5. KoinTest

Koin 에서 유닛테스트를 하기 위해서는 `KoinTest` 를 상속하고 제공되는 함수들을 사용 하면된다. 

```kotlin
class ComponentA
class ComponentB(val a: ComponentA)

class KoinUnitTester: KoinTest {
  val componentB: ComponentB by inject()

  @Test
  fun injectMyComponentTest() {
    startKoin{
      modules(
        module {
          single { ComponentA() }
          single { ComponentB(get()) }
        }
      )
    }
  }
  val componentA = get<ComponentA>()

  assertNotNull(a)
  assertEquals(componentA, componentB.a)
}
```

인스턴스에 대해 Mocking 하려 할 때엔 mock provider 를 통해 인스턴스를 mocking 할 수 있다. 

```kotlin
@get:Rule
val mockProvider = MockProviderRule.create { clazz ->
  Mockito.mock(clazz.java)
}
```

### 6. Koin with Android 

안드로이드에서 Koin 을 사용 하기 위해서는 모듈을 `Application` 클래스를 상속한 클래스에서 `onCreate()`를 override 하여 `startkoin()` 함수를 이용 해 모듈을 적용 시켜주면 된다. 

```kotlin
class ComponentA()
class ComponentB(val module: Module1)

// Modules.kt
val modules = module {
  single { ComponentA() }
  single { ComponentB(get()) }
}

// MyApplication.kt
class MyApplication: Application() {
  override fun onCreate() {
    super.onCreate()
    startKoin {
      androidLogger()
      androidContext(this@MyApplication)
      modules(modules)
    }
  }    
}
```

#### 6.1 LifecycleScope 

Koin 에서 제공하는 `lifecycleScope` 프로퍼티를 이용 하여 안드로이드 컴포넌트 라이프 사이클에 연동하여 사용 할 수 있다. 간단히 보면 해당 컴포넌트가 실행 되면 bind 되며, 컴포넌트가 destroyed 되면 같이 unbind 되고 destroy 된다고 생각 하면 된다. 이는 위에서 설명한 `Scope` 와 동일 하다고 보면 된다. 

예제로 Activity를 대상으로 `lifecycleScope` 프로퍼티를 통해 주입을 해보자. 

```kotlin
val module = module {
  scope<SomeActivity> {
    scoped { SomeComponent() }
  }
}

class SomeActivity: AppCompatActivity() {
  // 인스턴스를 scope기준으로 주입 한다. 
  val someComponent: SomeComponent by lifecycleScope.inject()
}
```

#### 6.2 ViewModel 

`koin-android-viewmodel` 의 `viewModel()`함수를 사용 해 ViewModel 인스턴스를 안드로이드 컴포넌트 생명주기에 맞춰 정의 할 수 있다. `viewModel()` 함수는 기본적으로 `factory` 와 동일한 scope를 갖고 있다. 

```kotlin
val module = module {
  viewModel { 
    SomeViewModel(get(), get())
  }
}
```

정의된 ViewModel 인스턴스를 주입받기 위해서는 아래 2가지 방법으로 주입 할 수 있다. 

- `by viewModel()` :  by 위임 키워드를 이용한 늦은 초기화 
- `getViewModel()` : 해당 인스턴스를 즉시 가져옴 


하나의 ViewModel 에 대해 여러개의 안드로이드 컴포넌트에서 사용 하기 위해서는 `sharedViewModel()` 을 사용 한다. 

```kotlin
class AFragment: Fragment() {
  val viewModel by sharedViewModel<SomeViewModel>()
}

class BFragment: Fragment() {
  val viewModel by sharedViewModel<SomeViewModel>()
}
```

`SavedStateHandle` 을 갖는 ViewModel 의 경우 `stateViewModel()` 을 이용 한다. 

```kotlin
class SomeViewModel(val handle: SavedStateHandle ): ViewModel()

val module = module {
  viewModel{ (handle: SavedStateHandle ) ->
    SomeViewModel(handle)
   }
}

class SomeFragment: Fragment() {
  val stateViewModel: SomeViewModel by stateViewModel()
  // 혹은
  val stateViewModel: SomeViewModel by stateViewModel(bundle = { myBundle })
}
```

> `SavedStateHandle` 은 ViewModel 의 첫번째 argument 이어야 한다. 