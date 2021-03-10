# Cancellation and timeout

1. [Asynchronous programming techniques](https://github.com/ksu3101/TIL/blob/master/kotlin/210217_coroutine_1.md)
2. [Coroutines basics](https://github.com/ksu3101/TIL/blob/master/kotlin/210218_coroutine_2.md)
3. [Create a basic coroutine - tutorial](https://github.com/ksu3101/TIL/blob/master/kotlin/210219_coroutine_3.md)
4. [Cancellation and timeout](https://github.com/ksu3101/TIL/blob/master/kotlin/210310_coroutine_4.md)
5. Composing suspending functions
6. Coroutine context and dispatchers
7. Asynchronous Flow
8. Channels
9. Shared mutable state and concurrency

- 이 내용은 [Kotlin 공식 가이드 문서](https://kotlinlang.org/docs/home.html)을 바탕으로 하였다. 

### 1. Cancelling coroutine execution﻿

오래 실행되는 어플리케이션에서 백그라운드 코루틴에 대해 정밀한 제어가 필요할 수 있다. 예를 들면, 사용자가 코루틴을 시작한 페이지를 종료했을 수 있으며 실행한 코루틴의 결과에 대해 필요하지 않을 경우 해당 작업을 취소할 수 있는 것 이다. `launch`함수는 실행중인 코루틴을 취소할때 해당 `Job`인스턴스를 반환 한다. 

```kotlin
val job = launch {
    repeat(1000) { i ->
        println("job: I'm sleeping $i ...")
        delay(500L)
    }
}
delay(1300L) // delay a bit
println("main: I'm tired of waiting!")
job.cancel() // cancels the job
job.join() // waits for job's completion 
println("main: Now I can quit.")
```

위 코드를 실행하면 아래와 같은 결과를 얻는다. 

```
job: I'm sleeping 0 ...
job: I'm sleeping 1 ...
job: I'm sleeping 2 ...
main: I'm tired of waiting!
main: Now I can quit.
```

main이 `job.cancel()`을 호출하자마자 취소 되었으므로 코루틴의 출력은 더이상 표시되지 않는다. 그리고 `cancel`및 `join`함수들이 결합된 확장 함수인 `cancelAndJoin()`함수도 있다. 

### 2. Cancellation is cooperative

코루틴의 취소는 협력적이라고 할 수 있다. `kotlinx.coroutines`의 모든 suspending 함수들은 취소할 수 있다. 코루틴를 취소 하면 `CancellationException`을 발생 시킨다. 그러나 코루틴이 연산중 취소가 확인되지 않았다면 다음 예제와 같이 취소할수 없다. 

```kotlin
val startTime = System.currentTimeMillis()
val job = launch(Dispatchers.Default) {
    var nextPrintTime = startTime
    var i = 0
    while (i < 5) { // computation loop, just wastes CPU
        // print a message twice a second
        if (System.currentTimeMillis() >= nextPrintTime) {
            println("job: I'm sleeping ${i++} ...")
            nextPrintTime += 500L
        }
    }
}
delay(1300L) // delay a bit
println("main: I'm tired of waiting!")
job.cancelAndJoin() // cancels the job and waits for its completion
println("main: Now I can quit.")
```

이를 실행하여 5회 반복 후 작업이 자동으로 완료될 때 까지 취소 후 에도 "I'm sleeping"이 계속 출력되는지 확인 한다. 

```
job: I'm sleeping 0 ...
job: I'm sleeping 1 ...
job: I'm sleeping 2 ...
main: I'm tired of waiting!
job: I'm sleeping 3 ...
job: I'm sleeping 4 ...
main: Now I can quit.
```

### 3. Making computation code cancellable

연산 코드를 취소가능하게 만드는 방법에는 두가지가 있다. 첫번째는 취소를 확인하는 suspending 함수를 주기적으로 호출 하는 것 이다. 그 목적을 위해 좋은 선택인 `yeild`함수가 있다. 다른 하나는 취소 상태를 명시적으로 확인 하는 것 이다. 후자의 경우에 대한 코드를 살펴 보자. 

이전 예제에서 `while (i < 5)`를 `while (isActive)`로 바꿔서 실행해 보자. 

```kotlin
val startTime = System.currentTimeMillis()
val job = launch(Dispatchers.Default) {
    var nextPrintTime = startTime
    var i = 0
    while (isActive) { // cancellable computation loop
        // print a message twice a second
        if (System.currentTimeMillis() >= nextPrintTime) {
            println("job: I'm sleeping ${i++} ...")
            nextPrintTime += 500L
        }
    }
}
delay(1300L) // delay a bit
println("main: I'm tired of waiting!")
job.cancelAndJoin() // cancels the job and waits for its completion
println("main: Now I can quit.")
```

실행 결과는 아래와 같다. 

```
job: I'm sleeping 0 ...
job: I'm sleeping 1 ...
job: I'm sleeping 2 ...
main: I'm tired of waiting!
main: Now I can quit.
```

보다시피 이제 반복이 취소됨을 확인 할 수 있다. `isActive`는 `CoroutineScope`객체를 통해 코루틴 내 에서 사용할 수 있는 확장 속성이다. 

### 4. Closing resources with finally

취소가능한 suspending함수는 취소할 경우 `CancellationException`을 throw하며 이는 일반적인 방법으로 처리할 수 있다. 예를 들어 `{ ... } finally { ... }`을 사용하고 Kotlin `use`함수는 코루틴이 취소될 때 정상적으로 종료 작업을 실행하게 한다. 

```kotlin
val job = launch {
    try {
        repeat(1000) { i ->
            println("job: I'm sleeping $i ...")
            delay(500L)
        }
    } finally {
        println("job: I'm running finally")
    }
}
delay(1300L) // delay a bit
println("main: I'm tired of waiting!")
job.cancelAndJoin() // cancels the job and waits for its completion
println("main: Now I can quit.")
```

`join()`과 `cancelAndJoin()`은 모두 종료 작업이 완료될 때 까지 대기하므로 위의 예제는 아래와 같은 출력을 확인할 수 있다. 

```
job: I'm sleeping 0 ...
job: I'm sleeping 1 ...
job: I'm sleeping 2 ...
main: I'm tired of waiting!
job: I'm running finally
main: Now I can quit.
```

### 5. Run non-cancellable block 

이전 예제의 finally블록에서 suspending함수를 사용하려 하면 이 코드를 실행하려 하는 코루틴이 취소되기 떄문에 `CancellationException`이 발생 한다. 정상적으로 작동하는 모든 종료 작업(파일 사용 종료, 작업 취소 또는 모든 종류의 통신 채널 종료)은 일반적으로 non-blocking이며 suspending함수가 아니기 때문에 일반적으로 문제가 되지 않는다. 그러나 드물게 취소된 코루틴에서 suspending해야 하는 경우 다음 예제와 같이 `withContext()`함수 및 `NonCancellable`컨텍스트를 사용하여 `withContext(NonCancellable) { ... }`에서 해당 코드를 래핑하여 처리 할 수 있다. 

```kotlin
val job = launch {
    try {
        repeat(1000) { i ->
            println("job: I'm sleeping $i ...")
            delay(500L)
        }
    } finally {
        withContext(NonCancellable) {
            println("job: I'm running finally")
            delay(1000L)
            println("job: And I've just delayed for 1 sec because I'm non-cancellable")
        }
    }
}
delay(1300L) // delay a bit
println("main: I'm tired of waiting!")
job.cancelAndJoin() // cancels the job and waits for its completion
println("main: Now I can quit.")
```

위 코드의 출력은 아래와 같다. 

```
job: I'm sleeping 0 ...
job: I'm sleeping 1 ...
job: I'm sleeping 2 ...
main: I'm tired of waiting!
job: I'm running finally
job: And I've just delayed for 1 sec because I'm non-cancellable
main: Now I can quit.
```

### 6. Timeout 

코루틴의 실행을 취소하는 실질적인 이유는 실행시간이 시간을 초과 했기 때문이다. 해당 작업에 대한 참조를 수동으로 추적하고 별도의 코루틴을 따로 실행하여 지연 후 추적된 참조를 취소시키는 `withTimeout`함수가 있다. 다음 예제를 보도록 하자. 

```kotlin
withTimeout(1300L) {
    repeat(1000) { i ->
        println("I'm sleeping $i ...")
        delay(500L)
    }
}
```

위 코드의 출력은 아래와 같다. 

```
I'm sleeping 0 ...
I'm sleeping 1 ...
I'm sleeping 2 ...
Exception in thread "main" kotlinx.coroutines.TimeoutCancellationException: Timed out waiting for 1300 ms
    at (Coroutine boundary. (:-1) 
    at FileKt$main$1$1.invokeSuspend (File.kt:-1) 
    at FileKt$main$1.invokeSuspend (File.kt:-1) 
Caused by: kotlinx.coroutines.TimeoutCancellationException: Timed out waiting for 1300 ms
    at (Coroutine boundary .(:-1)
    at FileKt$main$1$1 .invokeSuspend(File.kt:-1)
    at FileKt$main$1 .invokeSuspend(File.kt:-1)
```

`withTimeout()`함수에 의해 throw되는 `TimeoutCancellationException`은 `CancellationException`의 서브 클래스이다. 이전에는 콘솔 출력창에 스택 트레이스가 출력되는 것을 본적이 없을 것 이다. 취소된 코루틴 내부의 `CancellationException`은 코루틴 완료의 정상적인 이유로 간주되기 때문이다. 그러나 이 예제에서는 main함수 내 에서 `withTimeout()`을 사용 했다. 

취소는 예외일뿐이므로 모든 리소스는 일반적인 방식으로 닫혀진다. 추가작업을 수행해야 하거나 `withTimeoutOrNull`함수를 사용할 경우 `try {...} catch(e: TimeoutCancellationException) {...}`블록에서 래핑하여 처리 할 수 있다. `withTimeout`함수와 동일 하지만 시간 초과시 예외가 아닌 `null`을 반환 한다. 

```kotlin
val result = withTimeoutOrNull(1300L) {
    repeat(1000) { i ->
        println("I'm sleeping $i ...")
        delay(500L)
    }
    "Done" // will get cancelled before it produces this result
}
println("Result is $result")
```

코드를 실행하면 아래처럼 예외가 발생하지 않고 출력됨을 확인할 수 있다. 

```
I'm sleeping 0 ...
I'm sleeping 1 ...
I'm sleeping 2 ...
Result is null
```

### 7. Asynchronous timeout and resources

`withTimeout()`의 시간 초과 이벤트는 해당 블록에서 실행중인 코드와 관련하여 비동기적이며 시간이 초과된 블록 내부에서 반환되기 전에 언제든지 발생할 수 있다. 블록 외부에서 종료하거나 해제해야하는 리소스가 존재할 경우 이 를 염두해 두어야 한다. 

```kotlin
var acquired = 0

class Resource {
    init { acquired++ } // Acquire the resource
    fun close() { acquired-- } // Release the resource
}

fun main() {
    runBlocking {
        repeat(100_000) { // Launch 100K coroutines
            launch { 
                val resource = withTimeout(60) { // Timeout of 60 ms
                    delay(50) // Delay for 50 ms
                    Resource() // Acquire a resource and return it from withTimeout block     
                }
                resource.close() // Release the resource
            }
        }
    }
    // Outside of runBlocking all coroutines have completed
    println(acquired) // Print the number of resources still acquired
```

위 코드를 실행하면 항상 0이 출력되는 것은 아니지만 컴퓨터의 타이밍에 따라 달라질 수 있다. 실제로 0이 아닌 값을 보려면 이 예제에서 시간을 조정해야 한다. 

이 문제를 해결하려면 `withTimeout`블록에서 반환하는 것 과 반대로 리소스에 대한 참조를 변수에 저장해야 한다. 

```kotlin
runBlocking {
    repeat(100_000) { // Launch 100K coroutines
        launch { 
            var resource: Resource? = null // Not acquired yet
            try {
                withTimeout(60) { // Timeout of 60 ms
                    delay(50) // Delay for 50 ms
                    resource = Resource() // Store a resource to the variable if acquired      
                }
                // We can do something else with the resource here
            } finally {  
                resource?.close() // Release the resource if it was acquired
            }
        }
    }
}
// Outside of runBlocking all coroutines have completed
println(acquired) // Print the number of resources still acquired
```

이 코드는 항상 0을 출력 한다. 이는 자원이 누출되지 않음을 알 수 있다. 