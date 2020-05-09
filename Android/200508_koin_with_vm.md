## Koin 에서 ViewModel 주입 방법

Koin 에서 모듈의 인스턴스들은 `single` 이나 `factory` 등 으로 각각의 상황에 맞추어서 인스턴스를 사용하면 된다. 하지만 ViewModel 의 경우 안드로이드 컴포넌트 라이프 사이클에 맞추어 동작 하기 떄문에 Activity, Fragment 에 대한 `ViewModelProvider` 를 이용하여 ViewModel 인스턴스를 사용 해야 한다. 

이번에는 Koin 을 이용 하여 ViewModel 을 주입받아 사용 하는 방법에 대해서 정리 하였다. 내용을 정리함에 앞서 Koin 의 [ViewModel DSL - Document](https://doc.insert-koin.io/#/koin-android/viewmodel)를 참고 하였다.

### 1. Simple injection ViewModel

#### 1.1 ViewModel 컴포넌트 

아래 예제는 일반적인 ViewModel 에 대한 모듈 선언이다. 

```kotlin
val viewModelsModules = module {
  viewModel<SomeViewModel> {
        SomeViewModel(get(), get(), get())
    }
}
```

`viewModel()` 함수는 생성될 `ViewModel` 의 인스턴스를 아래 함수 내용을 보면 알 수 있듯이 `factory()` 를 통해 관리 하게된다. `factory()` 는 주입될 대상 모듈의 인스턴스를 생성하고 있지 않다가 호출 하는 시점마다 생성 하여 주입 하게 한다. \

`ViewModelProvider` 를 사용하지 않고 이대로도 사용 할 수 있긴 하다. 하지만 안드로이드 컴포넌트 들의 생명주기에 상관없이 작동할 것 이고 `ViewModel` 사용함에 있어 가장 큰 장점을 잃어버리는 것 이다. 

```kotlin
inline fun <reified T : ViewModel> Module.viewModel(
    qualifier: Qualifier? = null,
    override: Boolean = false,
    noinline definition: Definition<T>
): BeanDefinition<T> {
    val beanDefinition = factory(qualifier, override, definition)
    beanDefinition.setIsViewModel()
    return beanDefinition
}
```

#### 1.2 ViewModel 의 주입

ViewModel 컴포넌트의 주입 방법은 아래의 2가지 방법이 있다. 

- `by viewModel()` : `by` 키워드를 이용하여 `viewModel()` 함수에 초기화를 위임 하여 ViewModel 인스턴스를 주입 받는다. 
- `getViewModel()` : ViewModel 인스턴스를 직접 가져온다. 

```kotlin
class SomeFragment: Fragment() {
  val viewModel1: SomeViewModel by viewModel()
  val viewModel2: AnotherViewModel = getViewModel()
  
  // ...
}
```

`by viewModel()` 과 `getViewModel()` 의 차이점은 `lazy` 위임 여부이다. 아래 코드를 보면 이해할 수 있다. 

```kotlin
inline fun <reified T : ViewModel> ViewModelStoreOwner.viewModel(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): Lazy<T> {
    return lazy(LazyThreadSafetyMode.NONE) { getViewModel(T::class, qualifier, parameters) }
}
```

위 코드는 `viewModel()` 함수인데 내부에서 `lazy()` 함수를 이용하여 `getViewModel()` 을 그대로 사용함을 알 수 있다. 그리고 `lazy()` 에 `LazyThreadSafetyMode.NONE` 모드를 사용 하였는데 이 때에는 멀티스레드에서의 thread safity 를 보장하지 않는다. 

`getViewModel()` 함수의 경우 함수 내부에서 전달될 `ViewModelParameter` 의 `Bundle` 포함 여부에 따라서 `SavedStateHandle` 을 포함한 ViewModel 을 생성할지 여부를 알 수 있다. 이에 대해서는 아래에서 다시 보도록 하자. 

### 2. Shared ViewModel

생성된 한개의 ViewModel 인스턴스는 액티비티 에 추가된 여러개의 프래그먼트 에서 같이 주입되어 사용 할 수 있다. 

fragment class
```kotlin
class SomeFragment: Fragment() {
  val vm: SomeViewModel by shareViewModel()
}

class AnotherFragment: Fragment() {
  val vm: SomeViewModel by shareViewModel()
}
``

### 3. ViewModel with injection params

코인 에서는 ViewModel 의 주입 시점에 앞서 인스턴스 생성시 필요한 작업을 할수 있다. 아럐 예제를 보자. 

```kotlin
// 예제 View Model
class SomeViewModel(
  val id: Long,
  val name: String,
  // ... 
): ViewModel() { }

// 예제 모듈 선언 
val viewModelsModules = module {
  viewModel<SomeViewModel> {
        (id: Long, name: String) -> SomeViewModel(id, name, get(), get())
    }
}
```

아래는 위 예제에 대한 주입 예제 이다. 고차함수의 구현을 통해 패러미터를 전달 함을 알 수 있다. 

```kotlin
class SomeFragment: Fragment() {
  val vm: SomeViewModel by viewModel{ parametersOf(10L, "Kim") }
}
```

### 4. ViewModel with `SavedStateHandle` instance

안드로이드 에서 제공하는 ViewModel 에 State Bundle 을 포함하여 이전 상태에 대한 데이터를 임시로 저장했다가 다시 보여줘야 할 필요가 있다. (예를 들어 액티비티가 종료된 뒤 다시 복귀되었을때) 이 경우 ViewModel 에서는 `SavedStateHandle` 인스턴스를 받아 `Bundle` 을 사용 하는 것 처럼 사용 할 수 있다. 

아래의 예제를 보자. 

```kotlin
// SavedStateHandle 를 사용 하려는 ViewModel 일 경우 생성자의 첫번째 parameter 는 `SavedStateHandle` 이어야 한다 
class SomeStateViewModel(
  val state: SavedStateHandle,
  val anotherService: AnotherService
): ViewModel()

// ViewModel 컴포넌트 선언 부
val modules {
  viewModel { 
    (state: SavedStateHandle) -> SomeStateViewModel(state, get())
  }
}
```

`SavedStateHandle` 을 가진 ViewModel 의 주입은 `stateViewModel()` 을 이용 하여 다음과 같이 하면 된다. 

```kotlin
class SomeFragment: Fragment() {
  val vm: SomeStateViewModel by stateViewModel()
  val vm: SomeStateViewModel by stateViewModel(bundle = myBundle)
}
```

코인에서는 내부적으로 `Bundle` 이 null 이 아닐 경우 알아서 Factory 를 구분하여 ViewModel provider 를 지정해 준다. 

```kotlin
internal fun <T : ViewModel> Scope.createViewModelProvider(
        viewModelParameters: ViewModelParameter<T>
): ViewModelProvider {
    return ViewModelProvider(
            viewModelParameters.viewModelStore,
            if (viewModelParameters.bundle != null) {
                stateViewModelFactory(viewModelParameters)
            } else {
                defaultViewModelFactory(viewModelParameters)
            }
    )
}
```
