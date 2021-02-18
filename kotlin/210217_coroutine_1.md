# Coroutine - Asynchronous programming techniques

- [1. Asynchronous programming techniques](https://github.com/ksu3101/TIL/blob/master/kotlin/210217_coroutine_1.md)
- [2. Coroutines basics](https://github.com/ksu3101/TIL/blob/master/kotlin/210218_coroutine_2.md)
- 3. Create a basic coroutine - tutorial
- WIP

Kotlin은 언어로, 표준 라이브러리에서 최소한의 저수준 API을 제공 하여 다양한 다른 라이브러리에서 코루틴을 활용할 수 있도록 도와준다. 

`kotlinx.coroutines`는 JetBrains에서 개발한 코루틴을 위해 제공 되는 라이브러리이다. 여기에는 `launch`, `async`등을 포함하여 이 가이드에서 다루는 여러 고 수준의 코루틴 지원 요소들이 포함 되어 있다. 

이 가이드는 `kotlinx.coroutines`의 핵심 기능에 대한 가이드 이며 여러 주제로 나누어진 예제들과 함께 제공 된다. 코루틴을 사용 하고 싶다면 코루틴에 대한 의존을 추가 해야 한다. 예를 들어 안드로이드의 경우 `build.gradle`에 아래와 같이 추가 한다. 

```gradle
dependencies {
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2'
}
```

- 이 내용은 [Kotlin 공식 가이드 문서](https://kotlinlang.org/docs/home.html)을 바탕으로 하였다. 

## 1. Asynchronous programming techniques

오랜 시간 동안 개발자로서 우리는 해결해야 할 문제들 중, 어플리케이션의 blocking을 방지 하는 방법에 직면 하게 되었다. 데스크 탑, 모바일 또는 서버측 응용 프로그램을 개발하는 등 상관없이 사용자가 기다리게 하거나 하는 병목현상을 일으키는 원인은 피해야 한다. 이 문제를 해결 하기 위해 아래와 같은 방법을 사용 한다. 

- Threading
- Callbacks
- Futures, promises, 그리고 다른 것 들
- Reactive Extensions
- Coroutines 

코루틴이 무엇인지 살펴보기전 에 다른 솔루션에 대해 간단하게 알아보도록 하자. 

### 1.1 Threading

스레드는 어플리케이션의 blocking을 방지 하는 가장 잘 알려진 방법 이다. 

```kotlin
fun postItem(item: Item) {
    val token = preparePost()
    val post = submitPost(token, item)
    processPost(post)
}

fun preparePost(): Token {
    // 요청을 한 뒤 메인 스레드를 blocking 한다 
    return token
}
```

위 코드에서 `preparePost()`가 오랫동안 실행 되는 프로세스이고 결과적으로 사용자 인터페이스들을 차단하게 된다고 가정 해보자. 우리가 할 수 있는 것은 이 프로세스를 별도의 스레드에서 실행 하게 하는 것 이다. 그러면 UI가 block 되는것을 막을 수 있다. 이것은 일반적인 기술이지만 몇가지 단점을 갖고 있다. 

- 스레드는 사용 비용이 저렴하지 않다. 스레드는 비용을 많이 필요로 하는 컨텍스트 전환(context switch)이 필요 하다.
- 스레드는 무한하지 않다. 시작할 수 있는 스레드의 갯수는 운영체제에 의해 제한 된다. 서버측 응용 프로그램에서는 이로 인하여 심각한 병목현상이 발생 할 수 있다. 
- 스레드를 항상 사용할 수 있지는 않다. JavaScript와 같은 일부 플랫폼에서는 스레드를 지원하지 않는다. 
- 스레드는 다루기 어렵다. 스레드의 디버깅, 경쟁 조건 방지(Avoding race conditions)는 다중 스레드 환경에서 흔히 겪게 되는 일반적인 문제이기도 하다. 

### 1.2 Callbacks

콜백을 사용 하면 하나의 함수를 매개변수로 다른 함수에 전달 하고 프로세스가 완료 되면 함수를 호출 하는 아이디어 이다. 

```kotlin
fun postItem(item: Item) {
    preparePostAsync { token ->
        submitPostAsync(token, item) { post ->
            processPost(post)
        }
    }
}

fun preparePostAsync(callback: (Token) -> Unit) {
    // make request and return immediately
    // arrange callback to be invoked later
}
```

이것은 보다 더 우아한 해결방법처럼 보이지만 그래도 몇가지 문제들이 존재 한다. 

- 중첩된 콜백이 갖는 어려움. 일반적으로 콜백으로 사용 되는 함수는 종종 자체 콜백이 필요할 떄가 있다. 이로 인해 일련의 중첩된 콜백이 발생하여 비직관적인 코드가 발생 한다. 이러한 패턴을 종종 제목이 있는 크리스마스 트리라고 한다. 
- 오류 처리가 복잡해 진다. 중첩된 모델에서는 이러한 오류처리 및 전달을 다소 복잡하게 만든다. 

콜백은 javaScript와 같은 이벤트 루프 아키텍처에서는 일반적이지만 거기에서도 일반적으로 사람들을 promises 나 reactive extension과 같은 다른 접근 방식을 사용 하는 쪽으로 이동하게 된다. 

### 1.3 Futures

promises나 future(언어/플랫폼에 따라 언급 될 수 있는 다른 용어도 있다)는 우리가 콜을 하면 언젠가 `Promise`라는 객체와 함께 반환 될 것 이라는 것을 기본으로 작동한다. 

```kotlin
fun postItem(item: Item) {
    preparePostAsync()
        .thenCompose { token ->
            submitPostAsync(token, item)
        }
        .thenAccept { post ->
            processPost(post)
        }

}

fun preparePostAsync(): Promise<Token> {
    // makes request an returns a promise that is completed later
    return promise
}
```

이 방법을 사용 하려면 프로그래밍 방식에 일괄적인 변경이 필요해 진다. 

- 다른 프로그래밍 모델. 콜백과 마찬가지로 프로그래밍 모델은 하향식 명령 방식에서 체인 호출을 사용하는 구성 모델로 변경 해야 한다. 루프, 예외 처리 등과 같은 전통적인 프로그램 구조는 일반적으로 이 모델에서는 더이상 유효하지 않다. 
- 다른 API. 일반적으로 플랫폼에 따라 다를 수 있는 `thenCompose`또는 `thenAccept`와 같은 완전히 새로운 API를 배워야 한다. 
- 특정한 반환 유형. 반환 유형은 우리가 필요로 하는 실제 데이터 타입에 멀어지고 대신 새로운 유형 `Promise`를 반환 한다. 
- 오류 처리가 복잡해 질 수 있다. 

### 1.4 Reactive extensions 

Rx(Reactive Extensions)는 Erik Meijer에 의해 C#에서 도입 되었다. .Net플랫폼에서 확실하게 사용 되었지만 Netflix가 이를 java로 포팅하여 이름을 RxJava로 정하기 전 까진 주류 채택되진 않았었다. 그 이후로 JavaScript(RxJS)를 포함한 다양한 플랫폼을 위한 수많은 포트로 제공 되었다. 

Rx의 이면에 있는 아이디어는 관찰(observable) 가능한 스트림으로 사용 하는 것 이다. 여기에서 우리는 이제 데이터를 스트림(무한한 양의 데이터)으로 생각하고 이런 스트림을 옵저빙 할 수 있다. 실제로 Rx는 단순히 데이터를 조작 할 수 있는 일련의 확장을 가진 `Observer`패턴 이다. 

접근 방식에는 Future와 매우 유사하게 느껴지지만, Future는 개별 요소를 반환하는 것 으로 생각 할 수 있으며, Rx는 스트림을 반환 한다. 그러나 이전과 유사하게 프로그래밍 모델에 대한 완전히 새로운 사고 방식을 소개 하게 된다. 이는 "모든 것으 스트림이며, 옵저빙 할 수 있다". 

이것은 문제에 접근하는 다른 방법과 비동기코드를 작성할 떄 우리가 사용 하는 것에 상당히 큰 변화를 말한다. Futurues와 반대되는 한 가지 이점은 너무 많은 플랫폼으로 이식 되었기 때문에 C#, Java, JavaScript또는 Rx를 사용할 수 있는 다른 언어와 상관없이 일관된 API경험을 적용할 수 있다는 점 이다. 

또 Rx는 오류 처리에 다소 좋은 접근 방식을 사용 하고 있다. 

### 1.5 Coroutines 

비동기 코드 작업에 대해 코틀린의 접근 방식은 일시 중단 가능한 계산의 개념인 코루틴을 사용 하는 것 이다. 즉, 함수가 어느 시점에서 실행을 일시적으로 중단 하고 나중에 다시 시작할 수 있다는 생각인 것 이다. 

그러나 코루틴의 이점 중 하나는 개발자에게 비 차단 코드를 작성하는 것이 기본적으로 차단 코드를 작성하는 것 과 동일하다는 점 이다. 프로그래밍 모델 자체는 실제로 변경되지 않는다. 

예를 들어 다음 코드를 살펴 보면,

```kotlin
fun postItem(item: Item) {
    launch {
        val token = preparePost()
        val post = submitPost(token, item)
        processPost(post)
    }
}

suspend fun preparePost(): Token {
    // makes a request and suspends the coroutine
    return suspendCoroutine { /* ... */ }
}
```

이 코드는 메인 스레드를 차단하지 않고 오랫동안 실행되는 작업을 진행 한다. `preparePost`는 일시 중단 가능한 함수이므로 앞에 `suspend`키워드를 붙였다. 이것은 위에서 언급하였듯이 함수가 특정 시점에서 실행 된뒤 일시 정지 되었다가 다시 시작됨을 의미 한다. 

- 함수 시그니쳐는 정확히 동일하게 유지 된다. 유일한 차이점은 여기에 추가 되는 `suspend`키워드 이다. 그러나 반환 하는 데이터 유형은 우리가 원하는 반환 유형 그대로 이다. 
- 코드는 본질적으로 코루틴을 시작하는 `launch`라는 함수를 사용하는 것 외에도 특별한 구문을 필요로 하지 않아 하향식 코드를 작성 하는 것 처럼 작성 된다. 
- 프로그래밍 모델과 API는 동일하게 유지된다. 루프, 예외 처리 등을 계속 사용할 수 있으며 완전히 새 API세트를 배울 필요가 없다. 
- 플랫폼 독립적이다. JVM, JavaScript또는 다른 플랫폼을 대상으로 해도 우리가 작성한 코드는 동일하다. 내부적으로 컴파일러는 각 플랫폼에 맞게 조정 된다. 

코루틴은 코틀린이 만들어낸 것은 말할것도 없으며 그렇다고 해서 새로운 개념은 아니다. 수십년동안 상요 되어 왔으며 Go와 같은 다른 프로그래밍에서 인기를 갖고 있다. 하지만 주목해야 할 중요한 점은 코틀린에서 구현되는 방식에서 대부분의 기능이 라이브러리에 위임된다는 것 이다. 실제로 `suspend`키워드 외 다른 키워드는 언어에 추가 되지 않았다. 이것은 구문의 일부로 async및 await이 있는 C#과 같은 언어와는 다소 다르다. 코틀린을 사용 하면 이는 라이브러리 함수 일 뿐 이다. 