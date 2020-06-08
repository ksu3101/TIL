## Dagger vs Koin 

Android 개발 환경에서 DI(Dependency Injection)를 위해 사용 되는 도구로 [Dagger](https://dagger.dev/dev-guide/android.html) 와 [Koin](https://insert-koin.io/) 이 있다. 이 도구는 지향하는 목표는 비슷하지만 사용하는 방법과 내부 흐름은 전혀 다르다. 

이 두개의 도구를 간단히 비교 하면 아래와 같다. 

||Dagger|Koin|
|---|---|---|
|사용 언어, 패턴|JAVA|Kotlin, Service Locator pattern|
|DI 시점|Compile|Runtime|
|장점|<ul> <li>런타임 중 에러 없어 안정적</li> <li>퍼포먼스 오버헤드가 적음</li> </ul>|<ul> <li>Dagger 에 비해 컴파일 속도가 빠름</li> <li>코틀린의 장점들을 그대로 사용 가능</li> <li>학습이 빠르며 가독성 높은 코드</li> </ul>|
|단점|<ul> <li>컴파일 시 오버헤드 존재</li> <li>학습곡선이 높고 디버깅이 어려움</li> </ul>|<ul> <li>런타임중 에러가 발생 에러 핸들링 필수</li> <li>Dagger 에 비해 런타임 중 오버헤드 발생</li> </ul>|

[Android developer 의 Dependency Injection 문서](https://developer.android.com/training/dependency-injection#choosing-right-di-tool) 에서는 프로젝트의 크기(추가적으로 앱 내부에서 보여지게 될 화면의 갯수) 에 따라 크면 클 수록 Dagger 를, 그에 반해 작을수록 Service locator pattern 의 사용을 추천 하였다. 그와 더불어 위 장,단점을 참고 하여 어떤 di tool 을 사용할지 결정 하면 될 거 같다. 

- Module, Components
  - 어플리케이션 모듈과 각 하위단 모듈들의 선언및 injection 방법
- Scope
  - Activity, Fragment 외 특정 스코프 정의 
- Singleton, Factory, Qualifier
  - 모듈 인스턴스의 스코프 범위 지정 
- Android support
  - 안드로이드에 대한 지원 (ViewModel, State ViewModel)
- 그 외
  - 코드의 가독성, 유연성, 복잡성, 디버깅 등 

### 1. Dagger 

Dagger는 Annotation processor를 통해 컴파일 시점에 annotation을 추가한 대상 (추상)클래스, 인터페이스, 클래스 멤버 프로퍼티, 클래스 생성자 등 대입되는 코드를 생성 해준다. 그래서 annotation 을 작성 한 뒤 최ㅏ소한 한번 컴파일을 거쳐 정상적으로 코드들이 생성 되고 난 뒤에 문제없이 사용 할 수 있다. 이때 생성되는 코드들은 순수한 Java 이며 라이브러리도 Java 기반으로 작성 되어 있다. 

### 2. Koin

Koin 은 Service locator pattern을 기반으로 만들어진 도구 이다. 이는 Dependency injection tool 으로 사용 할 수 있다. 이 패턴은 장,단점을 명확하게 갖고 있다. Koin 은 순수 코틀린으로만 작성 되어 있고 다른 라이브러리나 APT 등에 대한 디펜던시가 없어 가볍게 사용하기에 좋다. 

#### 2.1 Service Locator Pattern

Service locator pettern 은 서비스를 구현한 클래스의 인스턴스는 숨겨진채 로 외부 어디에서든 서비스에 접근하여 해당 서비스를 사용 하는 패턴이다. 개발자는 이 서비스의 구현은 몰라도 되며 제공될 기능들을 서비스에서 제공 하는 인터페이스들을 구현해 주기만 하면 된다. 

DI 로 사용되는 Service locator pattern 은 서비스 그 자체인 코어 와 외부에서 각 서비스를 통해 제공될 인터페이스를 통해 DI 에서 사용 될 인스턴스를 생성, 반환 해주는 코드만 작성해주면 된다. 이 패턴에서 사용 되는 구체 서비스 자체는 보통 싱글탠 패턴을 갖고 있어 정확히 보면 싱글턴을 래핑한 인터페이스 라고 봐도 무방하다. 

이 패턴은 런타임 시점에 필요한 상황에 따라 인스턴스를 생성 하거나 가져오게 한다. 이 때 인스턴스가 존재 하지 않으면(null) 예외를 발생 하게 되며 이는 런타임 시점에 에러핸들링이 필수 이며 DI 로 사용 하고 있을 떄 에는 DI 모듈들의 디자인이 중요해 진다. 필요 시점에 명확하게 해당 인스턴스를 제공 해야 하기 때문이다. 

#### 2.2 Modules 

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
  startKoin {
    modules(modules)
  }
}
```

위 코드는 Koin 을 안드로이드 앱 에서 사용 하기 위해서 작성한 코드 들 중 일부이다. `Application` 을 상속한 클래스에서 `startKoin` 의 함수 구현을 통해 모듈들을 등록했음을 확인 할 수 있다. Koin 을 안드로이드에서 시작할때에는 위와 같은 기본적인 코드를 바탕으로 작업 하게 된다. 

##### 2.2.1 `single`

```kotlin
class SomeService() 

val modules = module {
  single { SomeService() } 
}
```

`single` 함수를 통해 제공될 모듈은 싱글턴 패턴을 사용 하여 runtime 중 단 한개의 인스턴스를 보장 한다. `single` 함수로 제공 될 모듈의 인스턴스를 요청 시 이미 만들어진 인스턴스를 제공 하며 런타임 중 에는 다시 생성하지 않는다. (안드로이드의 경우 완전히 재시작 되지 않는 이상) 

##### 2.3.2 `factory` 

```kotlin
class SomeModel() 

val modules = module {
  factory { SomeModel() }
}
```

`factory` 는 해당 클래스의 인스턴스를 호출시 마다 생성해서 준다. 생성된 인스턴스는 요청할 떄마다 새로운 인스턴스가 생성되기 때문에 새로운 인스턴스는 이전에 생성된 인스턴스와 전혀 다르며 내부 또한 다르다. 


