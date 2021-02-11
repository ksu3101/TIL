## (RxJava)Disposables Can Cause Memory Leaks

> 이 글은 ZAC SWEERS의 [Disposables Can Cause Memory Leaks](https://www.zacsweers.dev/disposables-can-cause-memory-leaks/)을 번역 하였다. 

RxJava에서 사용 되는 모든 `Disposable`은 바인딩 되는 옵저버에 대해 강력한 참조를 갖게 된다. 이로 인해 메모리 누수가 발생 할 수 있다. 

아래의 예제 코드를 보자. 

```kotlin
class TacoViewModel : ViewModel() {

  var compositeDisposable = CompositeDisposable()

  fun loadTaco(activity: Activity) {
    compositeDisposable.add(
      Single.just(Taco())
        .subscribe { taco ->
          // Handle taco...
          println("Taco created in $activity")
        }
    )
  }

  override fun onCleared() {
    compositeDisposable.clear()
  }
}
```

간단하지 않은가? 이 코드는 RxJava에서 메모리 누수를 방지하기 위한 고전적인 패턴이다. 반환 된 `Disposable`을 유지 하고 어떤 스코프에 존재 하던지 간에 "End"이벤트에서 이 Disposable을 폐기/제거 한다. `Single`소스가 종료 되기 전에 `TacoViewModel`의 라이프사이클이 종료 되는 경우 메모리 누수를 방지하기 위해서 이 코드들은 사용 되었다. 

그러나 실제로 Activity가 제거 되어도 `onCleared()`가 호출 될 때 까지 `Disposable`인스턴스는 유지 되고 있기 때문에 실제로 이 작업들은 누출될 수 있다. 

이제 구식인 안드로이드 Activity 메모리 누수가 발생 하였다. 이를 앱에 넣고 `Taco`를 불러오고 LeakCanary가 발생하는 탐지된 메모리 누수를 확인 하면 아래와 같다. 

```
    ┬───
    │ GC Root: System class
    │
   /// ...
    │  
    ├─ autodispose2.sample.TacoViewModel instance
    │    Leaking: UNKNOWN
    │    Retaining 289.4 kB in 7953 objects
    │    ↓ TacoViewModel.compositeDisposable
    │                    ~~~~~~~~~~~~~~~~~~~
    ├─ io.reactivex.rxjava3.disposables.CompositeDisposable instance
    │    Leaking: UNKNOWN
    │    Retaining 289.3 kB in 7949 objects
    │    ↓ CompositeDisposable.resources
    │                          ~~~~~~~~~
    ├─ io.reactivex.rxjava3.internal.util.OpenHashSet instance
    │    Leaking: UNKNOWN
    │    Retaining 289.3 kB in 7948 objects
    │    ↓ OpenHashSet.keys
    │                  ~~~~
    ├─ java.lang.Object[] array
    │    Leaking: UNKNOWN
    │    Retaining 289.3 kB in 7947 objects
    │    ↓ Object[].[0]
    │               ~~~
    ├─ io.reactivex.rxjava3.internal.observers.ConsumerSingleObserver instance
    │    Leaking: UNKNOWN
    │    Retaining 36 B in 2 objects
    │    ↓ ConsumerSingleObserver.onSuccess
    │                             ~~~~~~~~~
    ├─ autodispose2.sample.TacoViewModel$loadTaco$1 instance
    │    Leaking: UNKNOWN
    │    Retaining 16 B in 1 objects
    │    Anonymous class implementing io.reactivex.rxjava3.functions.Consumer
    │    $activity instance of autodispose2.sample.HomeActivity with mDestroyed =
    │    true
    │    ↓ TacoViewModel$loadTaco$1.$activity
    │                               ~~~~~~~~~
    ╰→ autodispose2.sample.HomeActivity instance
    ​     Leaking: YES (ObjectWatcher was watching this because autodispose2.sample.
    ​     HomeActivity received Activity#onDestroy() callback and
    ​     Activity#mDestroyed is true)
    ​     Retaining 144.8 kB in 3973 objects
```

### Springing the Leak 

위 예제에서 `subscribe()`로 전달 된 소비자 람다는 원래 Acitivty에 대한 참조를 유지하고 있으므로 캡쳐링 된 람다(Capturing lambda)라고 한다. 반환된 `Disposable`은 차레대로 이 서브스크라이브에 대한 참조를 유지 한다. 실제로 참조 하고 있는 `ConsumerSingleObserver`을 카나리아의 스택 트레이스에서 확인 할 수 있다. 

```
  ├─ io.reactivex.rxjava3.internal.observers.ConsumerSingleObserver instance
    │    Leaking: UNKNOWN
    │    Retaining 36 B in 2 objects
    │    ↓ ConsumerSingleObserver.onSuccess
    │                             ~~~~~~~~~
    ├─ autodispose2.sample.TacoViewModel$loadTaco$1 instance
    │    Leaking: UNKNOWN
    │    Retaining 16 B in 1 objects
    │    Anonymous class implementing io.reactivex.rxjava3.functions.Consumer
    │    $activity instance of autodispose2.sample.HomeActivity with mDestroyed =
    │    true
    │    ↓ TacoViewModel$loadTaco$1.$activity
    │                               ~~~~~~~~~
```

이 인스턴스는 `onCleared()`가 호출 될 때 까지 `CompositeDisposable`내부에서 영원히 유지 된다. 이는 `loadTaco()`로 전달 된 모든 작업들이 `onCleared()`가 호출 될 때 까지 일시적으로라도 누출 됨을 의미 한다. 

> `onCleared()`에 (메모리 누수를 방지 하기 위한)적절한 처리를 추가 하였지만, 유지 된 `Disposable`의 인스턴스는 여전히 람다에서 캡쳐된 모든 항목들에 대해 참조를 보유하고 있기 때문에 그 자체가 누수가 되어버린 것 이다. 

이것은 모든 `Disposable`에서도 발생할 수 있다. `CompositeDisposable`은 이러한 잠재적인 누출을 축적시키기 때문에 이를 더욱 악화시킬 수 있다.

## "I don't use ViewModel and nothing in our codebase outlives Activity, do I need to think about this?"

> (우리는) ViewModel을 사용하지 않으며 (우리의) 코드 베이스에서 Activity보다 오래 유지되는 인스턴스는 없다. 그럼에도 위와 같은 메모리 누수에 대해서 고민 해야 하는가? 

위의 예제에서는 간단한 ViewModel을 사용 하였다. 그러나 ViewModel을 사용하지 않는 경우 사용하던 구성(`Presenter` 등)으로 변경한 뒤 `Acitivty`에서 메모리 누수를 방지할 수 있는 방법으로 구성 해보도록 한다. 

```kotlin
@Singleton
class HttpClient {
  val compositeDisposable = CompositeDisposable()
  
  fun request(url: String, callback: Callback) {
    // Leaks every callback and everything it 
    // references unless you call shutdown() 🙃
    compositeDisposable.add(makeRequestSingle(url)
        .subscribe { callback.onResponse(it) })
  }
  
  fun cancelRequests() {
    compositeDisposable.clear()
  }
}
```

혹은 Presenter의 구현 예제:

```kotlin
class MyPresenter {
  var disposable: Disposable? = null
  
  fun bind(context: Context) {
    // Leaks context until onStop()
    disposable = Single.just(1)
        .subscribe { println(context) } 
  }
  
  fun onStop() {
    disposable?.dispose()
    // Persists even after this unless you 
    // discard your Presenter or null out the disposable
    disposable = null
  }
}
```

... 등, `Disposable`은 컨텍스트 및 `dispose()`호출 여부에 관계 없이 옵저버/소비자에서 캡쳐 된 모든 인스턴스들을 유지 한다. 

### Solutions

- `Disposable`을 계속 유지 해야 할 경우 이에 대해 제거 할 수 있는 방법을 추가 한다. 필요 할 경우 `dispose()`하지 말고 가능하면 스트림이 종료 될 때 참조를 지우도록 한다. 자신의 `WeakLambdaObserver`또는 이와 유사한 것을 작성하는것 도 방법일 수 있다. 

- 가능한 경우 람다를 캡쳐하지 않는다. 그러나 이는 쉽게 발생하곤 한다. 이럴 경우 lint를 통해 경고를 받아 검사 하도록 하자. 

- 시간을 절약하기 위해 `AutoDispose`를 사용한다. `AutoDispose`는 이 문제를 정확히 방지하지는 못하지만 반환된 `Disposable`을 99%로 제거 해 준다. 

