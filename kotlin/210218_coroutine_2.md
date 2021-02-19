# Coroutine - Coroutines basics

1. [Asynchronous programming techniques](https://github.com/ksu3101/TIL/blob/master/kotlin/210217_coroutine_1.md)
2. [Coroutines basics](https://github.com/ksu3101/TIL/blob/master/kotlin/210218_coroutine_2.md)
3. [Create a basic coroutine - tutorial](https://github.com/ksu3101/TIL/blob/master/kotlin/210219_coroutine_3.md)
4. Cancellation and timeout
5. Composing suspending functions
6. Coroutine context and dispatchers
7. Asynchronous Flow
8. Channels
9. Shared mutable state and concurrency

- 이 내용은 [Kotlin 공식 가이드 문서](https://kotlinlang.org/docs/home.html)을 바탕으로 하였다. 

## 1. Coroutines basics

이번 섹션에서는 코루틴의 기본 개념을 다룬다. 

### 1.1 Your First coroutine

아래의 코드를 실행 하면 다음과 같은 출력 결과를 확인 할 수 있다. 

```kotlin
import kotlinx.coroutines.*

fun main() {
    GlobalScope.launch {    // 새로운 코루틴을 백그라운드 스레드로 실행 한다. 
        delay(1000L)        // non-blocking 방식으로 1초 동안 딜레이 시킨다. 
        println("World.")   // 딜레이 후 글자를 출력 한다. 
    }
    println("Hello,")       // 메인 스레드에서 코루틴 이 딜레이 걸린 동안(non-blocking) 출력 된다
    Thread.sleep(2000L)     // 메인 스레드 를 2초동안 딜레이 시켜준다. 
}

// 출력 결과 
Hello,
World.
```

본질적으로 코루틴은 경량화 된 스레드(light-weight thread) 이다. `CoroutineScope` 내 `launch` 코루틴 빌더를 통한 블록 내 코드를 `GlobalScope` 로 실행 시킨 것 이다. `GlobalScope` 의 lifetime 은 application 의 lifetime 에 의존하기 때문이다. 

> 안드로이드 에서는 Acvitiy 가 종료 되도 application 은 아직 살아 있기 때문에 코루틴 코드는 계속 실행 될 수 있다. 하지만 만약 application 이 강제 종료 되거나 하면 코루틴 의 블럭 내 코드도 종료 되어버릴 수 있다. 

위 코드에서 `GlobalScope.launch { ... }` 와 `thread { ... }`, 그리고 `delay { ... }` 와 `Thread.sleep(...)` 을 서로 바꾸어도 동일한 결과를 얻을 수 있다. 

만약 `GlobalScope.launch` 를 `thread` 로 변경 하면 컴파일러는 다음과 같은 에러를 알려준다. 

```
Error: Kotlin: Suspend functions are only allowed to be called from a coroutine or another suspend function
```

컴파일 오류가 발생 하는 이유는 `delay` 때문이다. 이 함수는 `suspending function` 으로서 non-blocking thread 이다. 

### 1.2 Bridging blocking and non-blocking worlds﻿

첫번째 예제는 동일한 코드에서 non-blocking `delay(...)`와 blocking `Thread.sleep(...)`을 혼합해서 사용 하고 있다. 이 경우 어느쪽이 blocking하고 있는지, 아닌지 추적하기 쉽다. `runBlocking`코루틴 빌더를 사용하여 blocking에 대해 명시적으로 설명 해보도록 하겠다. 

```kotlin
import kotlinx.coroutines.*

fun main() { 
    GlobalScope.launch { // 백그라운드에서 새로운 코루틴을 실행한다. 
        delay(1000L)
        println("World!")
    }
    println("Hello,") // 메인 스레드서 계속 진행 되지만
    runBlocking {     // 블럭 내 코드는 메인 스레드를 block하고
        delay(2000L)  // JVM을 활성 상태로 유지하기 위해 2초동안 지연 된다 
    } 
}
```

결과는 이전 예제와 동일 하지만 코드는 non-blocking delay만 사용 한다. `runBlocking`을 호출 하는 메인 스레드는 `runBlocking`내부의 코루틴이 완료 될 때 까지 block된다. 

아래 예제는 `runBlocking`을 사용 하여 `main`함수의 실행을 래핑 하여 보다 관용적인 방식으로 다시 작성될 수도 있다. 

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking<Unit> { // 메인 코루틴 시작
    GlobalScope.launch { // 백그라운드 스레드로 새로운 코루틴을 실행 
        delay(1000L)
        println("World!")
    }
    println("Hello,") // 메인 스레드에서 계속 진행 되지만
    delay(2000L)      // JVM활성화를 위해 2초간 지연 된다. 
}
```

여기에서 `runBlocking<Unit> { ... }`은 최상위 메인 코루틴을 시작하는데 사용 되는 어댑터로서 작동 한다. 코틀린에서 `main` 함수는 `Unit`을 반환해야 하므로 `Unit`반환 유형을 명시적으로 지정 한다.

그리고 suspending 함수를 위한 단위 테스트의 예제 이다. 

```kotlin
class MyTest {
    @Test
    fun testMySuspendingFunction() = runBlocking<Unit> {
        // here we can use suspending functions using any assertion style that we like
    }
}
```

### 1.3 Waiting for a job

다른 코루틴이 작동하는 동안 잠시 지연하는 것은 좋은 방법은 아니다. 우리가 시작한 백그라운드 작업이 완료 될 때 까지(non-blocking방식 으로) 명시적으로 기다려야 한다. 

```kotlin
val job = GlobalScope.launch { // 새로운 코루틴을 실행 하고 그에 대해 `job`이란 인스턴스로 갖는다
    delay(1000L)
    println("World!")
}
println("Hello,")
job.join() // 코루틴 작업이 끝날 때 까지 기다린다 
```

이제 결과는 완전히 동일하지만 메인 코루틴의 코드는 어떤식으로든 백그라운드 작업의 실행시간과 관련이 없어졌으므로 훨씬 좋아졌습니다. 

### 1.4 Structured concurrency﻿

코루틴의 실제 사용을 위해 여전히 필요한 것 이 있다. `GlobalScope.launch`를 사용할 때 최상위 코루틴을 만든다. 가볍지만 실행하는 동안 여전히 메모리 리소스를 소비하게 된다. 새로 시작된 코루틴에 대한 참조를 유지하는 것을 잊더라도 여전히 실행되고 있다. 코루틴의 코드가 멈춘다면 (예를 들어, 너무 오래 지연된 경우) 그리고 너무 많은 코루틴을 시작하고 메모리가 부족하면 어떻게 될까? 실행 된 모둔 코루틴에 대한 참조를 수동으로 유지하고 join해야 하는 것 은 오류가 발생하기 쉽다. 

더 나은 해결방법이 있다. 코드에서 구조화된 동시성(structured concurrency)을 사용할 수 있다. `GlobalScope`에서 루틴을 시작 하는 대신 일반적으로 스레드에서 하는것 처럼(스레드는 항상 전역적이다) 수행중인 작업의 특정 범위 내 에서 코루틴을 시작 할 수 있다. 

예를 들면 `runBlocking`코루틴 빌더를 사용 하여 코루틴으로 변환되는 main함수가 있다. `runBlocking`을 포함한 모든 코루틴 빌더는 코드 블록의 범위에 `CoroutineScope`인스턴스를 추가 한다. 외부 코루틴(예를 들어 `runBlocking`)은 해당 범위에서 시작된 모든 코루틴이 완료 될 때 까지 완료 되지 않기 때문에 명시적으로 join하지 않아도 지정된 범위 내 에서 코루틴을 실행할 수 있다. 따라서, 예제를 더 간단하게 만들어 줄 수 있다. 

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking { // this: CoroutineScope
    launch { // launch a new coroutine in the scope of runBlocking
        delay(1000L)
        println("World!")
    }
    println("Hello,")
}
```

### 1.5 Scope builder

다른 빌더에서 제공 하는 코루틴의 범위(scope)외에도 `coroutineScope`빌더를 사용 하여 자체 범위를 선언할 수 있다. 코루틴 범위를 만들고 시작된 모든 자식 코루틴이 완료될 때 까지 완료되지 않는다. 

`runBlocking`과 `coroutineScope`는 자신과 모든 자식 코루틴이 완료 될 때 까지 기다리기 때문에 비슷해 보일 수 있다. 가장 큰 차이점은 `runBlocking`메소드는 대기를 위해 현재 스레드를 blocking하는 반면, `coroutineScope`는 다른 목적을 위해 기본 스레드를 해제 하기만 하면 일시 중단된다는 점 이다. 그 차이 때문에 `runBlocking`은 일반 함수이고 `coroutineScope`는 suspend함수 이다. 

아래 예제를 통해 확인 할 수 있다. 

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking { // this: CoroutineScope
    launch { 
        delay(200L)
        println("Task from runBlocking")
    }
    
    coroutineScope { // Creates a coroutine scope
        launch {
            delay(500L) 
            println("Task from nested launch")
        }
    
        delay(100L)
        println("Task from coroutine scope") // This line will be printed before the nested launch
    }
    
    println("Coroutine scope is over") // This line is not printed until the nested launch completes
}
```

위 예제를 실행한 결과는 아래와 같다. 

```
Task from coroutine scope
Task from runBlocking
Task from nested launch
Coroutine scope is over
```

`coroutineScope`가 아직 완료되지 않은 경우에도 "Task from coroutine scope"메시지 (중첩된 실행을 기다리는 동안)바로 뒤에 "Task from runBlocking"이 실행되고 인쇄되는 것 을 확인 할 수 있다. 

### 1.6 Extract function refactoring﻿

`launch {...}`함수 내부의 코드 블록을 별도의 함수로 추출 해 보자. 이 코드에서 "Extract function"리팩토링을 수행 해보면 `suspend`키워드가 붙은 새 함수가 생성 된다. 이것은 첫번째 `suspend` 함수이다. `suspend`함수는 일반 함수처럼 코루틴 내부에서 사용할 수 있지만, 그들의 추가 기능은 코루틴의 실행을 suspend하기 위해 다른 suspend함수(예를 들어 delay같은)를 차례로 사용할 수 있다는 점 이다. 

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    launch { doWorld() }
    println("Hello,")
}

// this is your first suspending function
suspend fun doWorld() {
    delay(1000L)
    println("World!")
}
```

그러나 추철된 함수에 현재 범위에서 호출되는 코루틴 빌더가 포함되어 있다면 어떻게 될까? 이 경우 추출된 함수는 `suspend`로는 충분하지 않다. `doWorld()`를 `CoroutineScope`의 확장 메소드로 만드는 것 이 해결방법중 하나 이곘지만 API를 명확하게 사용 하는 것은 아니므로 좋은 방법은 아니다. 관용적인 해결방법으로는 명시적인 `CoroutineScope`를 대상 함수를 포함하는 클래스의 필드로 사용 하거나 외부 클래스가 `CoroutineScope`를 구현할 때 암시적으로 하나를 갖게 하는 것 이다. 마지막 방법으로 `CoroutineScope(coroutineContext)`를  사용할 수 있지만 이러한 접근 방식은 더이상 메소드의 실행 범위를 제어할 수 없기 때문에 구조적으로 안전하지 않다. 비공개 API만이 빌더를 사용할 수 있다. 

### 1.7 Coroutines ARE light-weight﻿

다음 코드를 보자. 

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    repeat(100_000) { // launch a lot of coroutines
        launch {
            delay(5000L)
            print(".")
        }
    }
}
```

이 코드를 보면 10만개의 코루틴을 시작하고 5초 후 각 코루틴은 `.`을 출력 하는것 으로 보인다. 이 코드를 실행 하면 어떻게 될까? (대부분 메모리 부족 오류가 발생 할 것 이다)

### 1.8 Global coroutines are like daemon threads

다음 코드는 `GlobalScope`에서 "I'm sleeping"을 1초에 두번 인쇄한 다음 약간의 지연 후 메인 함수에서 반환하는 장기 실행 코루틴을 실행 하는 코드이다. 

```kotlin
GlobalScope.launch {
    repeat(1000) { i ->
        println("I'm sleeping $i ...")
        delay(500L)
    }
}
delay(1300L) // just quit after delay
```

실행해 보면 3줄을 출력하고 종료되는 것 을 확인 할 수 있다. 

```
I'm sleeping 0 ...
I'm sleeping 1 ...
I'm sleeping 2 ...
```

`GlobalScope`에서 시작된 활성 코루틴은 프로세스를 활성 상태로 유지 하지 않는다. 그들은 데몬 스레드(daemon thread)와 같다. 