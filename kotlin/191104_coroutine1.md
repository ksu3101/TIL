# 코틀린의 코루틴 #1

- 아래 내용은 [Kotlin 공식 가이드 문서](https://kotlinlang.org/docs/reference/coroutines/basics.html)을 바탕으로 공부한 내용을 정리 한 내용이다. 

## 1. 기본 

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

본질적으로 코루틴은 경량화 된 스레드(light-weight thread) 이다.

## 2. blocking 스레드와 non-blocking 스레드의 연계

## 3. background job 

## 4. 동시성 구조

## 5. Scope builder 