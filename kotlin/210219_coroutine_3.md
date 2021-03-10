# Create a basic coroutine – tutorial

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

코틀린 1.1은 비동기 non-blocking 코드등을 작성하는 새로운 방법인 코루틴을 도입 하였다. 이 튜토리얼에서는 기존 자바 라이브러리를 위한 헬퍼(helper)와 래퍼(wrapper)모음 인 `kotlinx.coroutines`라이브러리의 도움으로 코틀린의 코루틴을 사용하기 위한 기본 사항들을 살펴 본다. 

### 1. Setup a project

> 프로젝트 생성 과정과 [Maven에서의 설정](https://kotlinlang.org/docs/coroutines-basic-jvm.html#maven)은 생략 하였음. 

#### 1.1 Gradle 

생성한 프로젝트 모듈의 `build.gradle`파일 에 아래와 같이 코루틴 라이브러리 의존을 추가 한다. 

```gradle
dependencies {
    ...
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2'
}
```

### 2. My First coroutine 

코루틴은 가벼운 경량화된 스레드(thread)로 생각 해 보자. 스레드와 마찬가지로 코루틴은 병렬로 실행되고 서로를 기다리고 통신도 할 수 있다. 가장 큰 차이점은 코루틴은 매우 저렴하고 거의 무로에 가깝다는 점 이다. 반면에 스레드는 시작 하고 유지 하는데 비용이 많이 들어 간다. 천개의 스레드는 현대 기기에 있어 심각한 도전이 될 수 있을 것 이다. 

그렇다면 코루틴을 어떻게 시작 해야 할까? 일단 `launch {}` 함수를 사용해 보자.

```kotlin
launch {
    ...
}
```

이것은 새로운 코루틴을 시작한다. 기본적으로 코루틴은 공유 스레드 풀(shared thread pool)에서 실행 된다. 스레드는 여전히 코루틴을 기반으로 한 프로그램에 존재 하지만 하나의 스레드가 많은 코루틴을 실행할 수 있다. 

이제 `launch`를 사용하는 전체 프로그램을 살펴 보도록 하자. 

```kotlin
println("Start")

// 코루틴 시작
GlobalScope.launch {
    delay(1000)
    println("Hello")
}

Thread.sleep(2000) // 2초 대기 
println("Stop")
```

이 코드는 1초동안 대기 하고 `Hello`를 출력하는 코루틴을 시작 한다. `Thread.sleep()`과 같은 `delay()`함수를 사용하고 있지만 스레드를 차단하지 않고 코루틴 자체만 일시 중단 하니 더 좋다. 코루틴이 대기 하는 동안 스레드가 풀(thread pool)로 반환되고 대기가 완료 되면 코루틴이 스레드 풀(thread pool)의 사용 가능한 스레드에서 다시 시작 된다. 

`main()`함수를 실행 하는 메인 스레드는 코루틴이 완료 될 때 까지 기다려야 한다. 그렇지 않으면 `Hello`가 출력 되기 전 에 프로그램이 종료 될 것 이다. 

`main()`함수 내부에서 동일한 non-blocking `delay()`함수를 직접 사용하려고 하면 컴파일러 오류가 발생 할 것 이다. 

> `suspend` 함수는 코루틴 또는 다른 suspend함수에서만 호출 할 수 있다. 

`delay()`함수는 코루틴안에 있지 않기 때문이다. 코루틴을 시작하고 완료 될 때 까지 대기 하는 `runBlocking{}`으로 래핑 하면 `delay()`를 사용 할 수 있다. 

```kotlin
runBlocking {
    delay(2000)
}
```

따라서 결과적으로 프로그램은 먼저 `Start`를 출력한 다음 `launch{}`를 통해 코루틴을 실행 하고, `runBlocking{}`을 통해 다른 코루틴을 실해한 뒤 완료 될 떄 까지 blocking된 다음 `Stop`을 출력 하게 되는 것 이다. 한편, 첫 번째 코루틴은 `Hello`를 완료하고 출력 한다. 마치 스레드처럼. 

### 3. Let's run a lot of them

이제 코루틴이 스레드보다 정말 저렴하다는것을 확인 하였다. 이제 백만개의 코루틴을 시작하는 것은 어떨까? 먼저 백만개의 스레드를 시작 해 보자. 

```kotlin
val c = AtomicLong()

for (i in 1..1_000_000L)
    thread(start = true) {
        c.addAndGet(i)
    }

println(c.get())
```

이것은 각각 공통 카운터 `c`에 값을 더하는 1,000,000개의 스레드를 시작 한다. 

이것을 이제 코루틴으로 똑같이 해보자. 

```kotlin
val c = AtomicLong()

for (i in 1..1_000_000L)
    GlobalScope.launch {
        c.addAndGet(i)
    }

println(c.get())
```

이 예제는 1초 이내에 완료 되지만 일부 코루틴은 `main()`이 결과를 출력 하기 전에 완료되지 않기 떄문에 임의의 숫자를 출력 한다. 

스레드에 적용할 수 있는 동일한 동기화 수단을 사용할 수 있겠지만 (이 경웨 `CountDownLatch`를 추천 한다) 더 안전하고 깔끔한 방법을 선택 해 보도록 하자. 

### 4. Async: returning a value from a coroutine

코루틴을 시작 하는 또 다른 방법은 `async {}`이다. `launch {}`와 비슷하지만 코루틴의 결과를 반환하는 `await()`함수가 이쓴ㄴ `Deferred<T>`의 인스턴스를 반환 한다. `Deffered<T>`는 매우 기본적인 `Future`이다. (완전한 JDK future도 지원 되지만 여기서는 `Deffered`로 한정 하도록 한다)

`Deffered`객체를 유지 함녀서 백만개의 코루틴을 다시 만들어 보자. 이제 코루틴에서 추가 할 숫자를 반환 할 수 있기 때문에 Atomic카운터가 필요하지 않다. 

```kotlin
val deferred = (1..1_000_000).map { n ->
    GlobalScope.async {
        n
    }
}
```

이미 시작 되었으므로 결과를 수집 하기만 하면 된다. 

```kotlin
val sum = deferred.sumOf { it.await().toLong() }
```

우리는 단순히 모든 코루틴을 얻고 여기에서 그 결과를 기다린 다음 모든 결과를 표준 라이브러리 함수 `sumOf()`에 의해 함께 추가 된다. 그러나 컴파일러에서는 문제를 제기할 것 이다. 

> `suspend` 함수는 코루틴 또는 다른 suspend함수에서만 호출 할 수 있다.  

`await()`함수는 계산이 끝날 떄 까지 일시 중단되어야 하기 떄문에 코루틴 외부에서 호출 할 수 없으며 코루틴만 non-blocking방식으로 일시 중단 할 수 있다. 그렇다면 이것을 코루틴에 넣어 보자. 

```kotlin
runBlocking {
    val sum = deferred.sumOf { it.await().toLong() }
    println("Sum: $sum")
}
```

이제 모든 코루틴이 완료 되었기 때문에 `500000500000` 이라는 값을 출력할 것 이다. 

코루틴이 실제로 병렬로 실행되었는지 확인 해 보자. 각 `async`에 1초 `delay()`를 추가 하면 결과적으로 프로그램이 1'000'000초 (11.5일 이상)동안 실행 되지 않는다. 

```kotlin
val deferred = (1..1_000_000).map { n ->
    GlobalScope.async {
        delay(1000)
        n
    }
}
```

하지만, 내 컴퓨터(글 작성자)에서 약 10초가 걸리므로 이 코루틴은 병렬로 실행되었음을 확인 할 수 있다. 

### 5. Suspending functions 

이제 workload("1초 대기 후 숫자 반환")를 별도의 함수로 추출 한다고 가정 해보자. 

```kotlin
fun workload(n: Int): Int {
    delay(1000)
    return n
}
```

익숙한 오류가 보일 것 이다. 

> `suspend` 함수는 코루틴 또는 다른 suspend함수에서만 호출 할 수 있다. 

이 것이 의미하는 바를 조금 더 파헤쳐 보자. 코루틴의 가장 큰 장점은 스레드를 차단하지 않고 일시 중단 할 수 있다는 것 이다. 컴파일러는 이를 가능하게 하기 위해 몇가지 특수 코드를 보여야 하므로 코드에서 명시적으로 일시 중단 할 수 있는 함수임을 표시 해야 한다. 그래서 `suspend` 선언 키워드를 사용 해야 한다. 

```kotlin
suspend fun workload(n: Int): Int {
    delay(1000)
    return n
}
```

이제 코루틴에서 `workload()`를 호출 하면 컴파일러는 일시 중단 될 수 있음을 알고 그에 따라 준비 하게 된다. 

```kotlin
GlobalScope.async {
    workload(n)
}
```

`workload()`함수는 코루틴(또는 다른 suspend 함수)에서 호출 할 수 있지만 코루틴 외부에서는 호출 할 수 없다. 당연히 위에서 사용한 `delay()`및 `await()`자체가 `suspend`로 선언되어 있으므로 `runBlocking {}`에 넣고 `{}`또는 `async{}`를 시작 해야 한다. 