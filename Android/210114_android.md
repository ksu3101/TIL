## When LiveData and Kotlin don’t play well together

> 이 글은 Danny Preussler의 [When LiveData and Kotlin don’t play well together](https://medium.com/google-developer-experts/when-livedata-and-kotlin-dont-play-hand-in-hand-30149aa794ec)을 번역 하였다. 

`LiveData`의 아이디어는 매우 흥미롭다. 리액티브 스트림의 아이디어를 기반으로 할 때에는 항상 RxJava와 자동 라이프 사이클에 대한 처리를 추가 해야 했다. 이는 Android의 문제였기도 했다. `LiveData`는 타이밍이 좋지 않았다. Kotlin이 Android에 영향을 미치기 전에 알려졌으며 때로는 둘 다 잘 어울리지는 않았다. 왜 그러는지 그리고 어떠한 일들이 발생할 수 있는지 살펴 보도록 하자. 

### The good and bad

`LiveData`의 아이디어는 매우 간단하다. Observable 디자인 패턴의 라이프 사이클을 적용한 구현이다. 또한 이를 다시 구독하면 마지막으로 발행된 값을 다시 받을 수 있다. 고정적인 메시지가 존재 하는 EventBus의 타입화 된 버전과 비교할 수도 있다.

이것은 `LiveData`의 핵심 기능 중 하나이다. 하지만 개발자들은 항상 그런 기능들을 원하지는 않는다. 어떤 오류값이 있다고 가정해보자. 예를 들어 어떤 작업후 와 같이 구독한 뒤의 오류 값을 다시 표시하고 싶지 않을 수 있다. 

이를 해결하는 한가지 방법은 [`SingleLiveEvent`](https://github.com/android/architecture-samples/blob/dev-todo-mvvm-live/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/SingleLiveEvent.java)이며 오류와 같은 일회성의 이벤트 처리에 적합 하다. 

### Both worlds

하지만 둘 다 원한다면 어떨까? 마지막 값이 "sticky"이고 잠재적인 오류를 반복해서 표시하지 않기를 원할 때 말이다. 이 경우에는 소비된 `LiveData`에서 오류 값을 제거 하는것이 좋다. 

아래 처럼 `LiveData`의 구현을 살펴 보도록 하자. 현재 값은 아래 필드에 저장 된다. 

```java
private volatile Object data;
```

이 멤버는, 처음에 `NOT_SET`으로 설정 되어 있다.

```java
static final Object NOT_SET = new Object();
```

안타깝게도 `NOT_SET`은 `public`이 아니기 때문에 외부에서 접근할 수 없다. 

### What now?

이 문제를 찾아본 경우 가장 제안되는 해결방법중 하나는 단순히 `null`으로 설정하는 것 이다. 

아래 코드를 살펴보자.

```kotlin
viewModel.results.observe(lifecycleOwner) { result ->
    when (result) {
        SomeResult.Error -> {
            handleError()
            viewModel.results.reset()
        }
        SomeResult.Result -> { handleResult(result) }
    }
}
```

`ViewModel`의 구현은 아래와 같다. 

```kotlin
class MyViewModel: ViewModel() {
    private val mutableResults = MutableLiveData<SomeResult>()

    val results: LiveData<SomeResult>
        get() = mutableResults

    fun reset() {
        mutableResults.value = null
    }
}
```

잘 작동되는가? 아쉽게도 방금 작성한 코드에서는 크래시가 발생 한다. 

### But why? 

이 문제에 대해 알지 못하고 앱을 배포할 수도 있다. (그럴경우 다음에 단위 테스트를 추가 하도록 한다)

여기에서 문제는 `LiveData`를 컴파일러에 대해 non-nullable인 것으로 선언 했다는 것 이다. 

옵저버는 이 `null`값으로 설정 된 뒤 그에 대한 알림을 받은 다음 내부에서 값을 가져오려 시도할 때 `KotlinNullPointerException`을 발생하며 즉시 crash가 발생 한다. 이는 코틀린 작동 방식의 일부이다. 

`LiveData`는 Java로 작성 되었다. 그래서 값을 null으로 설정 할 수 있다. 따라서 (글)작성자는 다른 jetpack라이브러리에 있는 것 처럼 `@Nullable`주석을 추가 할 수 있다. 하지만 `LiveData`를 non-null으로 선언하였음에도 항상 `getValue()`또는 `Observer`에서는 nullable 타입을 반환한다. 제네릭은 런타임 정보(자바 용)이고 어노테이션은 컴파일 타임에 전달될 정보이기 때문에 이를 해결 할 수 없다. 그리고 Java에서는 null 타입이 없다.

이는 버그가 아니고 기능이기 때문이다. 

### How can you solve this?

간단한 방법은 `LiveData`를 nullable으로 처리하는 것 이다. 

```kotlin
private val mutableResults = MutableLiveData<SomeResult?>()

val results: LiveData<SomeResult?>
    get() = mutableResults
```

이제 옵저버에서 이를 처리 하고 null이 아닌 값에도 반응 할 수 있다. 그리고 더 중요한 것은 컴파일러에서 null타입에 대해 강제하려 한다는 점 이다. 

### Is there another way?

이전에 원래 `NOT_SET`값에 액세스 할 수 없다고 하였다. 하지만 이는 전적으로 사실이 아니다. jetpack패키지에 무언가를 설정하여 이를 public처럼 사용할 수 있다. 

```kotlin
package androidx.lifecycle

fun <T> LiveData<T>.reset(){
    this.value = LiveData.NOT_SET as T?
}
```

이 코드는 좀 더러워 보인다. 

하지만 그 외에는 실제로 허용 되는가? 적어도 이는 금지되지는 않았다. Google play의 앱 용 비공개 API에 대한 엄격한 [새 규칙](https://developer.android.com/distribute/best-practices/develop/restrictions-non-sdk-interfaces)에도 불구하고 문제 없다. 

실제 문제는 다르겠지만 가장 중욯나 것 은 공개 API의 일부가 아니므로 언제든지 변경될 수 있는 `LiveData`의 구현 세부 사항에 의존한다는 것 이다. 

### Ok, what now? 

또 다른 대안은 자신만의 `NOT_SET`을 가질수 있도록 결과를 래핑 하는 것 이다. 

```kotlin
sealed class ViewModelResult {
    data class Result(val result: SomeResult): ViewModelResult()
    object NotSet: ViewModelResult()
}
```

`NotSet`을 사용하여 이전 값을 지우고 `Observer`에서 무시할 수도 있다. 

가장 아름다운 방법은 아니지만 문제를 해결할 수 있는 깔끔한 방법이기는 하다.  Jose Alcérreca 의 [이 기사](https://medium.com/androiddevelopers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150)에서도 권장된다.

### Anything else?

또한 처음에 `LiveData`가 필요한지 다시 생각해 보자. `StateFlow`및 `ShareFlow`를 통해 코틀린은 훌륭한 대안들을 제공 한다. `LiveData`를 고수하고 싶다면 이를 같이 쓰거나 고정 또는 비 고정 이벤트를 가질 수 있는 방법이 있는지 생각해보도록 하자. 

이 경우 non-null `LiveData`를 null으로 설정할 수 없고 이를 컴파일러에서 오류가 발생하도록 하는 사용자 지정 Lint를 추가하도록 Linter를 업데이트 하는 것을 고려해 보도록 하자. 

조심하도록 하자. 이것은 `LiveData`의 문제가 아니며 다른 라이브러리에서도 발생할 수 있다. 