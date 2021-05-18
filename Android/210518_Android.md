# Kotlin SharedFlow or: How I learned to stop using RxJava and love the Flow

> 이 글은 Aleksandra Krzemień의 [Kotlin SharedFlow or: How I learned to stop using RxJava and love the Flow](https://proandroiddev.com/kotlin-sharedflow-or-how-i-learned-to-stop-using-rxjava-and-love-the-flow-e1b59d211715)을 번역 하였다.

코루틴은 정말 훌륭하다. Kotlin 1.3이 코루틴과 함께 소개 되었을때 이 것들이 게임 체인저가 될 것임을 바로 알게 되었다. 그리고 배우기도 쉽고 이해하기도 쉬우며 사용하기도 쉽다. `coroutines 1.0.0`이 릴리즈 되었을 때 많은 사람들이 이제 RxJava가 사용되지 않을것이라고 예상하기도 했다.

하짐나, 코루틴의 훌륭함에도 불구 하고 RxJava는 여전히 계속 사용되고 있는것 으로 나타났다. 왜일까? 아마도 그 이유중 하나는 RxJava가 코루틴보다 더 잘 알려져 있기 때문일것이다. 그러나 더 중요한 이유가 있다. 코루틴은 RxJava에서는 사용할 수 있는 것 들을 아직 제공하고 있지 못하기 때문이다. 

## RxJava is not only `Single`

대부분의 앱에서 RxJava는 주로 API요청에 대한 처리에 사용 되었다. Retrofit을 이용하여 REST API를 호출 하고, RxJava의 Single을 반환하여 적절한 스케쥴러를 통해 응답을 매핑하는 것 이다. 이 시나리오에서 코루틴은 매우 사용하기에 적합하다. 엔드 포인트 메소드를 suspend하도록 변경 하기만 하면 된다. 하지만 그럼에도 RxJava는 그 이상의 역할을 수행 한다. 반응형 프로그래밍의 진정한 강점은 진행중인 데이터 스트림을 처리하는데 있다. 

예제 1 : 앱에 몇단계의 데이터 업로드 프로세스(백그라운드 스레드에서 수행 됨)가 있으며 각 단계별의 업데이트로 인하여 UI가 실시간으로 업데이트 되도록 한다. 이는 `Observable`과 `onNext()`의 좋은 사례들로 보인다. 이 시나리오에서는 `suspend`함수가 단일 값만 반환할 수 있기 때문에 일반적인 코루틴으로는 처리 할 수 없게 된다. 이 경우 코루틴으로는 적절한 도구가 없었다. 하지만 Kotlin Flow가 제공 되는 `coroutines 1.2.0`이 도입 되면서 상황이 바뀌게 되었다. 이전에는 RxJava로만 처리 할 수 있었던 거의 모든 케이스들을 강력한 kotlin Flow와 함께 사용 하여 코루틴으로 처리 할 수 있게 된 것 이다. 

RxJava와 코루틴에 대한 경험이 있는 경우 코드를 앞서 언급한 개념들을 모두 사용해야 하므로 Flow로 전환할때에는 이 과정들이 원할하게 진행 될 수 있어야 한다. 예제1의 코드를 살펴 보도록 하자. 

```kotlin
fun uploadData(): Observable<UpdateStep> = Observable.create<UpdateStep> {
    it.onNext(UpdateStep.Step1)
    doUpdateStep1()
    it.onNext(UpdateStep.Step2)
    doUpdateStep2()
    it.onNext(UpdateStep.Step3)
    doUpdateStep3()
    it.onNext(UpdateStep.Success)
}.onErrorReturn {
    UpdateStep.Error(it)
}
```

위 코드는 RxJava를 사용했을때의 예제 이다. 

```kotlin
fun uploadData(): Flow<UpdateStep> = flow {
    emit(UpdateStep.Step1)
    doUpdateStep1()
    emit(UpdateStep.Step2)
    doUpdateStep2()
    emit(UpdateStep.Step3)
    doUpdateStep3()
    emit(UpdateStep.Success)
}.catch {
    emit(UpdateStep.Error(it))
}
```

그리고 Flow를 사용한 예제 이다. 

이제 Observable을 `subscribe()`하거나 Flow를 `collect()`하기만 하면 된다. 이런식으로 쉽게 코루틴은 RxJava를 대체할 수 있는 단계를 제공하였다. 하지만 이 것이 최종 단계는 아니다. 

## You're hot then you're cold

RxJava를 좀 더 광범위하게 사용하는 사람에게 콜드/핫 스트림의 개념이 잘 알려져 있다. `Observable.create()`또는 `Flowable.just()`에 의해 생성된 스트림은 cold이다. 즉, 코드를 실행하기 위해 시작하고 무엇인가를 구독 할 때에만 값을 내보낸다. 이는 `flow {}`또는 `flowOf()`를 사용하여 처리 할 수 있는 데이터 스트림이다. 이 메소드를 생성된 Flow는 Cold Flow라고 한다. 

코루틴이 여전히 제공하지 못했던 마지막 RxJava의 사용 사례는 바로 Hot Flow였다. 반응형 프로그래밍에서 핫 스트림은 값을 방출하기 위해 아무것도 관찰할 필요가 없음을 의미 한다. 이것은 BLE 연결상태를 처리하기 위해 앱은 항상 연결 상태 변경에 대해 알고 있어야 하며 관찰자에게 해당 사항이 있거나 도착하자마자 알려야 한다. (예제 2) Hot Stream은 RxJava에서 `PublishSubject`및 `BehaviorSubject`를 이용하여 구현한다. 다음은 그 예제 이다. 

```kotlin
class BleManager : BleConnectionObserver {
    private val _bleConnectionStateSubject = BehaviorSubject.createDefault(ConnectionState.Disconnected)
    val bleConnectionStateObservable: Observable<ConnectionState> = _bleConnectionStateSubject.hide()
    val currentConnectionState: ConnectionState
        get() = _bleConnectionStateSubject.value

    override fun onDeviceConnected(device: BluetoothDevice) {
        /* ... */
        _bleConnectionStateSubject.onNext(ConnectionState.Connected)
    }

    override fun onDeviceDisconnected() {
        /* ... */
        _bleConnectionStateSubject.onNext(ConnectionState.Disconnected)
    }
}
```

오랫동인 이 기능에 대해 코루틴 API는 제공되지 못했지만 2020년 10월 `corutines 1.4.0`릴리즈와 함께 제공 되기 시작했다. 하지만 다시 한번 보도록 하자. Kotlin Flow가 릴리즈 되기 전 부터 `Channels`라는 개념이 있었다. 이는 코루틴을 사용하여 생산자-소비자 문제를 해결하기 위해 설계되었다. 유연한 `send()`및 `receive()`메소드를 제공하는 `ChannelInterface`는 보기엔 간단해 보이지만 실제로는 꽤 복잡하다. 이는 처리 해야 하는 동시성이 발생할 수 있는 통신의 양쪽에서 독립적인 코루틴을 사용하기 있기 떄문에 동일한 리소스에 대한 동시 액세스와 관련된 문제가 발생할 수 있다. 또한 channels API가 아직 완전히 안정적이지 않은 것 도 있었다. 여기엔 많은 실험과 프리뷰 방법들이 있었다. 운이 좋게도 Kotlin팀은 코루틴을 엉망으로 만들지 않고 데이터 흐름을 훨씬 깔끔하게 만드는 `SharedFlow`및 `StateFlow`의 개념을 고안하였다.

이제 가장 중요한 부분이다. `SharedFlow`는 RxJava의 `PublishSubject`와 동일하다. 이를 통해 핫 플로우를 생성하고 배압(Backpressure)및 리플레이를 처리 하기 위한 전략을 지정할 수 있게 된다. `StateFlow`는 RxJava의 `BehaviorSubject`에 해당 하는 `SharedFlow`의 특별한 케이스이다. 최신 값을 유지하는 핫 플로우를 생성 하므로 예제 2번의 BLE연결 상태를 처리 하기 위한 완벽한 대상이 된다. 

```kotlin
class BleManager : BleConnectionObserver {
    private val _bleConnectionStateFlow = MutableStateFlow(ConnectionState.Disconnected)
    val bleConnectionStateFlow: StateFlow<ConnectionState> = _bleConnectionStateFlow

    override fun onDeviceConnected(device: BluetoothDevice) {
        /* ... */
        _bleConnectionStateFlow.value = ConnectionState.Connected
    }

    override fun onDeviceDisconnected() {
        /* ... */
        _bleConnectionStateFlow.value = ConnectionState.Disconnected
    }
}
```

Shared 또는 State의 Flow들은 모두 변경 가능한 (Mutable)형식으로 사용할 수 있으며 또한 변경 불가능한(Immutable) 방식으로 노출 될 수 있다. 이는 `LiveData`라는 개념으로 잘 알려져 있다. RxJava에서는 `Observable`뒤에 `BehaviorSubject`를 숨기고 현재 상태를 별도로 노출 해 야 했다. 하지만 Flow를 사용하면 변경할 수 없는 `StateFlow`형식을 사용하여 여전히 최신값을 얻을 수 있다. 

`SharedFlow`와 `StateFlow`를 올바르게 사용하려면 다음 몇가지 사항을 기억해야 한다. 

- `StateFlow`는 병합된다. 즉, 이전 값과 동일한 새 값으로 값을 업데이트 하면 업데이트가 전달되지 않게 된다. 
- `SharedFlow`는 적절한 리플레이/버퍼 설정을 필요로 한다. 대부분의 경우 소비자가 없어도 생산자가 정지되지 않도록 하기만 하면 된다. 예를 들어, 리플레이나 버퍼 용량을 양수 값으로 설정 하거나 `bufferOverflow`를 `SUSPEND`외의 값으로 설정 하면 된다. 
- `StateFlow`와 `SharedFlow`는 완료되지 않기 떄문에 `onCompletionCallback`에 의존되지 않으며 `collect`가 정상적으로 완료되지 않는 점을 기억해야 한다. (coolect를 호출한 코루틴 또한 마찬가지이다)

## Ok, cool, but do we even need that?

`SharedFlow`와 `StateFlow`는 나(원본 글 작성자)에게 있어 진정한 게임 체인저였고 이들이 소개 되었을 때 RxJava를 완전히 제거할 수 있을 것 이라고 생각 되었다. 또한 최근 `StateFlow`는 데이터 바인딩 작업에 대한 알파 지원도 받게 되었다. 그리고 더 좋은 점은 데이터 바인딩에서 `StateFlow`를 사용하면 `LiveData`와 마찬가지로 (안드로이드 컴포넌트의)수명주기를 인식한다는 것 이다! 이젠 UI가 준비되지 않았을때 더 이상 업데이트를 할 필요가 없다. 그러나 `lifecycleScope.launch {}`를 사용하여 프래그먼트에서 `ShareFlow`를 `collect`한다는 것 은 수명주기를 인식하지 않는 것 은 기억 해야 한다. 그리고 `launchWhenStarted`를 사용하거나 앱이 백그라운드로 전환 될 때에는 작업들을 취소해주어야 한다. 

아직, 여러분 중 일부는 다음과 같이 말할 수 있겠다.

> "왜 SharedFlow가 필요한걸까? 위에서 설명했던 사례들의 대부분은 LiveData로 처리될 수 있다!"

그렇다면 다음과 같이 묻도록 하겠다. 

> "클린 아키텍쳐에 대해 들어본 적 이 있나요?"

몰론 프래그먼트에서 ViewModel의 변경 사항을 관찰해야 할 때 LiveData로 충분하기는 하지만 떄로는 다른 모듈 또는 일반적으로 추상화 계층 뒤 어딘가에서 이벤트를 관찰해야 할 때가 있을 수 있다. 안타깝게도 LiveData는 안드로이드의 클래스이므로 이 경우 작동하지 않으며 다른 도메인 모듈(일반적으로 기본 추상화된 레이어)은 순수 코틀린으로 작성되어야 한다. 이 것이 바로 SharedFlow가 코틀린 클래스이기 때문에 빛을 발하게 된다. 도메인 코드를 안드로이드 클래스들에 연결하지 않고도 데이터베이스 또는 API에 노출할 수 있게 된다. 이는 정말 멋진 접근 방식처럼 들린다. 게다가 LiveData를 사용할 때 특별한 접근 방식이 필요한 `SingleLiveEvent`문제에 대한 솔루션으로 `SharedFlow`를 사용할 수 있는것도 있다. 
