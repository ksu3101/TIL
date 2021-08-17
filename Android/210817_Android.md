# Safe delay in Android Views: goodbye Handlers, Hello Coroutines!

> 이 글은 Julien Salvi의 [Safe delay in Android Views: goodbye Handlers, Hello Coroutines!](https://juliensalvi.medium.com/safe-delay-in-android-views-goodbye-handlers-hello-coroutines-cd47f53f0fbf)을 번역 하였다. 

[`Looper`](https://developer.android.com/reference/android/os/Looper)를 사용하지 않고 [`Handler`](https://developer.android.com/reference/android/os/Handler)를 사용 하면 작업이 자동으로 크래시나거나 오류가 발생하는 등의 버그가 발생할 수 있기 떄문에 최신 Android버전에서는 더이상 사용하지 않는다. 그래서 (구글 안드로이드 공식)문서에 따르면, [`Executor`](https://developer.android.com/reference/java/util/concurrent/Executor)를 사용하여 `View`의 `Handler`대체품으로서 사용할 수 있지만 수명 주기에 따라 동작되지 않기 때문에 문제가 발생할 수 있다. 

(이 문서에서는)코루틴과 수명주기 라이브러리를 활용하여 `Handler().postDelay()`를 대체 하여 지정된 시간 후 안정하게 작업을 실행하는 방법에 대해 살펴 보도록 하자. 

## Handler: old fashioned way

`Handler()`생성자가 더 이상 사용하지 않아도 `getHandler()`를 호출하여 `View`의 핸들러에 접근하거나 View에서 직접 `postDelay()`를 호출 할 수 있다. 이렇게 하면 더 이상 사용되지 않는 방법을 이용하지 않아도 주어진 시간 동안 작업을 지연할 수 있게 된다. 

```kotlin
class MyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): View(context, attrs, defStyleAttr) {
    init {
        // accessing the view handler
        handler.postDelayed({ /* 지연될 작업 내용 */ }, 300L)

        // delay directly your action
        postDelayed({/* 지연될 작업 내용 */}, 300L)
    }
}
```

이 작업이 보이지 않는 곳 에서 어떻게 작동하는지 살펴 보자. 새 이벤트가 Push되면, View를 처리하는 스레드와 연결된 핸들러에 접근 할 수 있게 된다. `postDelay()`를 사용하여 새 작업이 진행될 떄 마다 새로운 `Runnable`이 메시지 대기열에 추가 된다. (runnable은 UI스레드에서 실행됨을 기억 할 것)

이렇게 되면 View가 분리(Detached)되거나 파괴(Destroyed)될 때 이로 인하여 예기치 않은 동작이나 크래시가 발생할 수 있다. 

## Kotlin + Coroutine + Lifecycle

(이번에는)코틀린, 코루틴 및 수명 주기 라이브러리르 사용하여 `Handler().postDelay()`보다 더 나은 솔루션을 구현하는 방법에 대해 살펴 보자. 먼저 프로젝트에 아래와 같은 종속을 확인 한다. 

```
implementation 'androidx.lifecycle:lifecycle-runtime-ktx:x.x.x'
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:x.x.x'
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:x.x.x'
```

이제 기능을 구현할 준비가 되었다. (View에 대해)안전한 지연을 적용하려면 수명주기 라이브러리에서 제공 하는 `findViewTreeLifecycleOwner()`에 제공할 `lifecycleOwner`를 찾아야 한다. 

그러면 수명주기의 코루틴 [`Dispatcher`](https://kotlinlang.org/docs/coroutine-context-and-dispatchers.html)로 새 코루틴을 시작할 수 있게 된다. (UI스레드에서 작업을 수행하기 위해서 기본적으로 Main Dispatcher를 사용 할 것 이다.) 지정된 시간 동안 코루틴에서 작업을 실행하기 위해서 메인 블록의 함수를 호출하기 전에 `delay()`메소드를 적용 한다. 

그렇게 하면 수명주기와 연결되기 때문에 지연된 작업을 View에서 안전하게 실행할 수 있게 된다. 

```kotlin
fun View.delayOnLifecycle(
    durationInMillis: Long,
    dispatcher: CoroutineDispatcher = Dispatcher.Main,
    block: () -> Unit
): Job? = findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
    lifecycleOwner.lifecycle.coroutineScope.launch(dispatcher) {
        delay(durationInMillis)
        block()
    }
}
```

이제 이 확장 함수를 사용하여 다음과 같이 View에 연결된 일부 작업을 안전하게 지연하여 실행할 수 있게 된다. 

```kotlin
myView.delayOnLifecycle(500L) {
    // 지연 시간 후 해야 할 작업들은 여기 
}
```

## Conclusion

안드로이드에서 멀티 스레드를 사용 하기 위해서는 `Activity`, `Fragment`등의 수명 주기를 고려해야 하므로 항상 고통스러운 일 이었다. 하지만 코루틴과 수명주기 라이브러리 덕분에 이제 우리는 멀티 스레드를 더 안전하게 사용할 수 있게 되었다. 

