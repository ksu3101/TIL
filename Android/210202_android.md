## Modelling UI State on Android

> 이 글은 Stojan Anastasov의 [Migrate from LiveData to StateFlow and SharedFlow](https://alexzh.com/migrate-from-livedata-to-stateflow-and-sharedflow/)을 번역 하였다. 

Android 앱 개발을 위해 Google에서 권장하는 접근 방식은 ViewModel에 UI상태를 유지 하고 View에서 이를 관찰(Observe)하도록 하는 것 이다. 이를 위해 LiveData, StateFlow, RxJava 또는 유사한 도구들을 사용할 수 있다. 그러나 UI상태를 모델링하는 방법은 무엇이 있을까? 데이터 클래스(data class) 또는 봉인 클래스(sealed class)를 사용하는가? 하나의 관찰(observe)가능한 속성(property)를 사용하는가?

이글에서는 이러한 접근 방식들의 장단점들을 설명 하고 사용하는 방법을 결졍 하는데 도움이 되는 도구들을 제시 하려 한다. 이 글은 Elm guide의 [TYpes as Sets](https://guide.elm-lang.org/appendix/types_as_sets.html)에서 크게 영감을 받았다고 한다. 

### Types as sets

데이터 구조를 만들어 코드의 값이 가능한지 실제의 유효한 값과 정확하게 일치 하는지 확인할 필요가 있다. 이렇게 하면 잘못된 데이터와 관련된 여러 버그를 방지 하는데 도움이 된다. 이를 달성하려면 먼저 타입들과 집합간의 관계를 이해 해야 한다. 

타입은 값의 집합으로 생각 할 수 있으며, 고유한 요소를 포함하고 그 사이에는 순서가 존재하지 않는다. 예를 들면:

- `Nothing` - 비어있는 세트. 이는 아무런 요소를 포함하지 않는다. 
- `Unit` - 싱글턴(Singleton) 세트. 이는 단 하나의 요소만을 갖는다. 
- `Boolean` - `true`혹은 `false`값 만을 갖는다.
- `Int` - 숫자를 갖는다. 예를 들면 `-2`, `-1`, `0`, `1`, `2`...
- `Float` - 숫자를 갖는다. 예를 들면 `0.1`, `0.01`, `1.0`...
- `String` - 문자열을 갖는다. 예를 들면 `""`, `"a"`, `"b"`, `"kotlin"`, `"Android"`, `"Hello World!"`...

따라서 `val x: Boolean`이라고 쓸 때 `x`는 `Boolean`값 집합에 속하고 `true`또는 `false`일 수 있음을 의미 한다. 

### Cardinality

수학에서 카디널리티는 집합의 "요소 수"의 척도 이다. 예를 들어 `Boolean`세트 에서는 `[true, false]`요소만 존재 하므로 `Boolean`의 카디널리티는 `=2`이다.

위에서 언급한 세트들의 카디널리티를 살펴보면:

- `Nothing` - 0
- `Unit` - 1
- `Boolean` - 2
- `Short` - 65535
- `Int` - ∞
- `Float` - ∞
- `String` - ∞

> 참고 : Int및 Float의 카디널리티는 사실 무한대는 아니지만 2^32라는 큰 수 이다. 

앱을 빌드 할 때 내장된 유형을 사용하고 데이터 클래스 및 봉인된 클래스와 같은 구성을 사용하여 사용자 정의 유형을 만들 수도 있다. 

### Product Types (*)

Kotlin의 제품 유형 중 하나는 `Pair`과 `Triple`이다. 그들의 카디널리티를 확인해보도록 하자. 

- `Pair<Unit, Boolean>` - 카디널리티(Unit) * 카디널리티(Boolean) - 1 * 2 = 2
- `Pair<Boolean, Boolean>` - 2 * 2 = 4

`Pair<Unit, Boolean>`는 아래의 요소들만 갖는다. 

- `Pair(Unit, false)`
- `Pair(Unit, true)`

다른 예제로:

- `Triple<Unit, Boolean, Boolean>` = 1 * 2 * 2 = 4
- `Pair<Int, Int>` = 카디널리티(Int) * 카디널리티(Int) = ∞ * ∞ = ∞

타입을 `Pair` / `Triple`과 결합하면 카디널리티가 증가 한다. (따라서 Products Types라는 이름을 주었다)

`Pair` / `Triple`은 각각 2개와 3개의 속성을 가진 데이터 클래스의 제네릭 버전이다. 

```kotlin
data class User(
    val emailVerified: Boolean, 
    val isAdmin: Boolean
)
```

위 데이터 클래스는 4개의 카디널리티를 갖는다. 4개의 가능한 요소는 아래와 같다. 

- `User(false, false)`
- `User(false, true)`
- `User(true, false)`
- `User(true, true)`

### Sum Types (+)

Kotlin에서 봉인된 클래스를 사용 하여 Sum타입을 구현한다. 봉인된 클래스를 사용하여 타입을 결합 할 때 총 카디널리티는 멤버 카디널리티의 합과 동일 하다. 예를 들면 아래와 같다. 

```kotlin
sealed class NotificationSetting
object Disabled : NotificationSettings()    // 싱글턴 단일 오브젝트 -> cardinality = 1
data class Enabled(val pushEnabled: Boolean, val emailEnabled: Boolean) : NotificationSettings()

// cardinality = cardinality (Disabled)  + cardinality(Enabled)
// cardinality = 1 + (2 * 2)
// cardinality = 1 + 4 = 5

sealed class Location
object Unknown : Location()
data class Somewhere(val lat: Float, val lng: Float) : Location()

// cardinality = cardinality (Unknown)  + cardinality(Somewhere)
// cardinality = 1 + (∞ * ∞)
// cardinality = 1 + ∞ = ∞
```

### Nullable Types

`Location`타입을 모델링 하는 또 다른 방법은 아래처럼 `nullable`타입 데이터 클래스를 사용 하는 방법이다. 

```kotlin
data class Location(
    val lat: Float,
    val lng: Float
)

// nullable Location type
val location: Location?
```

이 경우에는 위치를 알 수 없는 경우 `null`을 사용 한다. 이 두 표현은 동일한 카디널리티를 갖고 있으며 정보 손실 없이 둘 사이를 변환할 수 있다. 추가적인 예를 들면 아래와 같다. 

- `Unit?` - 카디널리티 = 2(1 + 카디널리티(Unit))
- `Boolean?` - 3 (1 + 카디널리티(Boolean))

### Enum

Enum은 Kotlin에서 Sum타입을 나타내는 또 다른 방법이다. 

```kotlin
enum class Color { RED, YELLOW, GREEN }
```

`Color`의 카디널리티는 요소의 수(위 예제의 경우에는 3이 된다)와 같다. 다른 방법으로 봉인된 클래스를 사용하는 방법은 아래와 같다. 

```kotlin
sealed class Color
object Red: Color()
object Yellow: Color()
object Green: Color()
```

봉인된 클래스를 사용해도 카디널리티는 동일하게 3 이다. 

### Why does it matter

타입을 집합 및 카디널리티로 생각 하면 잘못된 데이터와 관련된 모든 종류의 버그를 방지하기 위해 데이터 모델링시 도움이 된다. 예를 들어 신호등을 모델링 한다고 가정 해보도록 하자. 가능한 색상은 빨간색, 노란색 및 녹색 일 것 이다. 코드로 표현하기 위해 다음과 같이 생각 할 수 있다. 

- "red", "yellow", "green"이 유효한 옵션이고 나머지는 모두 유효하지 않은 데이터가 된다. 그렇기 때문에 사용자가 "red"대신 "rad"라고 입력하면 오류가 발생 한다. 여기에서 문제의 근본적인 원인은 카디널리티이다. 문자열의 카디널리티는 무한대 인데 명확한 값은 3개뿐이므로 ∞-3의 잘못된 값이 있음을 알 수 있다. 

- `data class Color(red:Boolean, yellow:Boolean, green:Boolean)`이 있을 때 `Color(true, false, false)`는 빨간 색임을 나타낸다. 그러나 이 경우 유효하지 않은 데이터가 발생할 수 있음의 여지를 남기고 있다. `Color(true, true, true)`일 경우 이 값이 유효한지 확인 하려면 검사및 테스트 하는 코드가 필요 할 것 이다. 데이터 클래스인 `Color`의 카디널리티는 8이고 잘 못된 값은 8 - 3인 5이다. `String`보다 훨씬 적지만 그래도 여전히 개선이 필요 하다. 

- `enum class Color { RED, YELLOW, GREEN }`은 카디널리티가 3 이다. 문제의 가능한 유효값과 정확하게 일치 한다. 이제 잘못된 값은 존재하지 않으므로 데이터 유효성을 확인하기 위한 테스트는 필요하지 않다. 

잘못된 값을 배제하는 방식으로 데이터를 모델링 하게 되면 코드도 짧아지고 명확하며 테스트 하기 쉬워지는 장점이 있음을 기억 하도록 하자.

### Exposing State from a ViewModel

예제로 신호등의 엔드포인트를 호출 시 신호등의 색상(빨간색, 노란색 또는 녹색)을 표시하는 앱을 만들어보려 한다. 비동기 네트워크 통신중에는 `ProgressBar`가 표시될 것 이다. 통신이 성공 하면 색상이 설정된 View가 보여지며 오류가 발생한 경우 일반 텍스트가 있는 `TextView`를 보여준다. 한번에 하나의 View만 보여지게 될 것 이며 오류를 다시 시도할 가능성은 없다고 가정 해 보자. 

```kotlin
class TrafficLightViewModel : ViewModel() {
    val state: LiveData<TrafficLightState> = TODO()
}
```

색상에 대해서는 3개의 값을 가지는 enum으로 색상을 표시 할 것 이며 이는 `TrafficLightState`데이터 클래스에 `nullable`타입으로 갖는다.

```kotlin
data class TrafficLightState(
    val isLoading: Boolean, 
    val isError: Boolean, 
    val color: Color?
)
```

한가지 방법은 세가지 속성이 있는 데이터 클래스이다. 그러나 이것은 유효하지 않은 상태를 가질 수 있다. 예를 들면 `TrafficLightState(true, true, Color.RED)`와 같은 형태일 것 이다. 이 경우 로딩과 오류가 모두 true이다. ProbressBar또는 오류 TextView를 한번에 보여주면 안되야 한다. 이 경우 `TrafficLightState`의 카디널리티는 `Boolean * Boolean * Color? = 16`이다.

이 경우 관찰 가능한(Observable) 속성을 사용해보면 어떨까? 

```kotlin
class TrafficLightViewModel : ViewModel() {
    val loading: LiveData<Boolean> = TODO()
    val error: LiveData<Boolean> = TODO()
    val color: LiveData<Color?> = TODO()
}
```

카디널리티는 여전히 `Boolean * Boolean * Color?`이므로 `16`이다. 데이터 클래스 접근 방식과 동일한 카디널리티를 갖게 되며 잘못된 값을 활성화 할 수 있다. 

다른 접근 방식으로는 봉인된 클래스를 사용 하는 것 이다. 

```kotlin
sealed class TrafficLightState
object Loading : TrafficLightState()
object Error : TrafficLightState()
data class Success(val color: Color) : TrafficLightState()
```

카디널리티는 1(로딩중) + 1(오류) + 3(성공) 총 5가 되며 아래 생각한 상태와 일치함을 알 수 있다. 

- 네트워크 통신 중 : `Loading`상태, 그리고 `ProgressBar`를 보여 준다.
- 통신중 오류 발생 : `Error`상태, 그리고 오류 메시지가 담긴 `TextView`를 보여 준다. 
- 성공 : `Success`상태, 그리고 가져온 색상을 `View`에 업데이트 하여 보여 준다. 

이들은 모두 유효한 접근 방식이며 UI상태를 디자인 할 때 사용 된다. 그러나 그 중 하나만이 가장 유효한 상태와 정확하게 일치함을 알 수 있다. 이런 방식으로 버그를 제거 하고 코드를 단순화 시켜 테스트 수를 줄여보도록 하자. 

### Conclusion

코드에서 데이터를 모델링 하기 위해 `Boolean, Int, String`.. 과 같은 내장된 타입들을 사용 하고 Kotlin에서는 데이터 클래스 및 봉인된 클래스와 같은 구성을 사용하여 사용자 정의 타입을 만들 수 있다. 

다른 언어 구성은 데이터 모델의 카디널리티에 다른 영향을 미친다. 올바른 데이터 조합을 선택하여 잘못된 상태의 수를 최대한 줄이면 더 간단하고 강력한 코드가 생성됨을 잊지 말자. 