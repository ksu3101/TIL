# Kotlin 의 테스트 코드 

## 1. 개요
기존 java 에서는 테스트 코드를 junit 과 mockito 를 이용 하여 작성 하였다. mockito 를 이용하여 쉽게 테스트 코드를 작성 할 수 는 있지만 가독성이 아주 높다고는 생각 되지 않았다. 그리고 프로젝트에 kotlin 을 적용 하면서 테스트 코드 또한 코틀린으로 작성하고 싶어졌으며 그와 동시에 중위함수(infix function) 을 이용 하여 가독성 높은 코드를 작성 해 보고 싶었다. 

그러던 와중 [Kluent](https://github.com/MarkusAmshove/Kluent) 라는 라이브러리를 발견 하였다. mockito 의 각 메소드 들을 중위함수로 연결 해 주거나 제네릭에서 컴파일 되지 않아 오류가 많은 mockito 를 위한 추가 적인 도구 라고 생각 하면 된다. 

아래는 kluent 를 참고 하여 혹은 스스로 만든 테스트 코드를 위해 만들어진 utility function 들 이다. 리턴타입이나 함수 내부의 역활을 보면 mockito 의 메소드 대신 콜 해주며 역할을 대신 함을 알 수 있다. 

```kotlin
inline fun <reified T : Any> mock() = Mockito.mock(T::class.java)

infix fun <T : Any> T.spy(obj: T): T = Mockito.spy(obj)

fun <T: Any> spy(obj: T): T = Mockito.spy(obj)

infix fun <T : Any> T.given(call: T?): OngoingStubbing<T> = Mockito.`when`(call)

fun <T: Any> given(call: T?): OngoingStubbing<T> = Mockito.`when`(call)

infix fun <T : Any> OngoingStubbing<T>.willReturn(returnValue: T?): OngoingStubbing<T> = this.thenReturn(returnValue)

infix fun <T : Any> OngoingStubbing<T>.willThrow(throwable: Throwable): OngoingStubbing<T> = this.thenThrow(throwable)

fun <T : Any> OngoingStubbing<T>.andAnswer(answer: Answer<Any>): OngoingStubbing<T> = this.thenAnswer(answer)

// then

infix fun <T : Any> T?.shouldBe(expected: T?) = this.apply { assertEquals(expected, this) }

infix fun <T : Any> T?.shouldNotBe(expected: T?) = this.apply { assertNotSame(expected, this) }

infix fun <T : Any> T?.shouldEqualTo(expected: T?) = this.apply { assertEquals(expected, this) }

infix fun <T : Any> T?.shouldNotEqualTo(expected: T?) = this.apply { assertNotSame(expected, this) }

// verify
fun <T : Any> T.verify(): T = Mockito.verify(this)
fun <T: Any> T.hasCalled(): T = this.verify()

fun <T : Any> T.verify(mode: VerificationMode): T = Mockito.verify(this, mode)

fun <T : Any> T.verifyTimes(stubCounts: Int): T = Mockito.verify(this, times(stubCounts))
fun <T: Any> T.hasCalledCounts(expectedInvocationCounts: Int): T = this.verifyTimes(expectedInvocationCounts)

fun <T : Any> T.verifyNever(): T = Mockito.verify(this, never())
fun <T: Any> T.hasNotCalled(): T = this.verifyNever()

fun <T : Any> T.verifyAtLeastOnce(): T = Mockito.verify(this, atLeastOnce())
fun <T: Any> T.hasCalledAtLeastOnce(): T = this.verifyAtLeastOnce()

fun <T : Any> T.verifyAtLeast(minNumberOfInvocations: Int): T = Mockito.verify(this, atLeast(minNumberOfInvocations))
fun <T : Any> T.hasCalledAtLeast(minNumberOfInvocations: Int): T = this.verifyAtLeast(minNumberOfInvocations)

fun <T : Any> T.verifyAtMost(maxNumberOfInvocations: Int): T = Mockito.verify(this, atMost(maxNumberOfInvocations))
fun <T: Any> T.hasCalledAtMost(maxNumberOfInvocations: Int): T = this.verifyAtMost(maxNumberOfInvocations)

fun <T : Any> T.verifyCalls(wantedNumberOfInvocations: Int): T = Mockito.verify(this, calls(wantedNumberOfInvocations))

// numbers (Numbers interface 로 바꿀순 없나?)

infix fun Int.biggerThan(expected: Int) = (this > expected)

infix fun Int.biggerThanOrEqualTo(expected: Int) = (this >= expected)

infix fun Int.smallThan(expected: Int) = (this < expected)

infix fun Int.smallThanOrEqualTo(expected: Int) = (this <= expected)

infix fun Float.biggerThan(expected: Float) = (this > expected)

infix fun Float.biggerThanOrEqualTo(expected: Float) = (this >= expected)

infix fun Float.smallThan(expected: Float) = (this < expected)

infix fun Float.smallThanOrEqualTo(expected: Float) = (this <= expected)

infix fun Long.biggerThan(expected: Long) = (this > expected)

infix fun Long.biggerThanOrEqualTo(expected: Long) = (this >= expected)

infix fun Long.smallThan(expected: Long) = (this < expected)

infix fun Long.smallThanOrEqualTo(expected: Long) = (this <= expected)

// Test Observable functions

infix fun <T : Any> TestObserver<T>.assertValueAtFirst(predicate: Predicate<T>) = this.apply { assertValueAt(0, predicate) }

infix fun <T : Any> TestObserver<T>.assertValueAtSecond(predicate: Predicate<T>) = this.apply { assertValueAt(1, predicate) }

infix fun <T : Any> TestObserver<T>.assertValueAtThird(predicate: Predicate<T>) = this.apply { assertValueAt(2, predicate) }

infix fun <T : Any> TestObserver<T>.assertValueAtFourth(predicate: Predicate<T>) = this.apply { assertValueAt(3, predicate) }

infix fun <T : Any> TestObserver<T>.assertValueAtFifth(predicate: Predicate<T>) = this.apply { assertValueAt(4, predicate) }

infix fun <T : Any> TestObserver<T>.assertError(predicate: Predicate<Throwable>) = this.apply { assertError(predicate) }

infix fun <T : Any> TestObserver<T>.assertValueCount(expectedCount: Int) = this.apply { assertValueCount(expectedCount) }

infix fun <T : Any> TestObserver<T>.assertCompleted(isCompleted: Boolean) = this.apply {
    if (isCompleted) assertComplete() else assertNotComplete()
}

fun <T : Any> TestObserver<T>.assertNoErrorOccurred() = this.apply { assertNoErrors() }

// Test Subscriber's functions

infix fun <T: Any> TestSubscriber<T>.assertValueAtFirst(predicate: Predicate<T>) = this. apply { assertValueAt(0, predicate) }

infix fun <T: Any> TestSubscriber<T>.assertValueAtSecond(predicate: Predicate<T>) = this.apply { assertValueAt(1, predicate) }

infix fun <T: Any> TestSubscriber<T>.assertValueAtThird(predicate: Predicate<T>) = this.apply { assertValueAt(2, predicate) }

infix fun <T: Any> TestSubscriber<T>.assertValueAtFourth(predicate: Predicate<T>) = this.apply { assertValueAt(3, predicate) }

infix fun <T: Any> TestSubscriber<T>.assertValueAtFifth(predicate: Predicate<T>) = this.apply { assertValueAt(4, predicate) }

infix fun <T: Any> TestSubscriber<T>.assertValueCount(expectedCount: Int) = this.apply { assertValueCount(expectedCount) }

infix fun <T: Any> TestSubscriber<T>.asserCompleted(isCompleted: Boolean) = this.apply {
    if (isCompleted) assertComplete() else assertNotComplete()
}

fun <T : Any> TestSubscriber<T>.assertNoErrorOccured() = this.apply { assertNoErrors() }
```

## 2. 사용 예제 
테스트 코드는 기본적으로 `Given, When, Then` 으로 구성된다. 몰론 이 부분에 대해서는 개발자들 마다 다르다. 내가 작성한 코드의 작성 예를 몇개 보면 아래와 같다. 

 ```kotlin
    @Test
    fun addition_isCorrect() {
        val cal2 = SimpleCalculator(15)   // given

        cal2.add(1000)    // when

        cal2.value shouldEqualTo 1015   // then
    }
 ```
 위 테스트 코드를 통해 어떤 객체를 인스턴스 화 하거나 mocking 한 뒤 결과를 stubbing 해주고 테스트 환경을 만들어준 뒤 테스트 구성을 해 준다. 그리고 그 테스트 후 결과가 내가 생각한 결과와 맏는지 확인 한다. 위는 간단한 예 이고 복잡한 형태를 보도록 하자. 
 
```kotlin
class ExampleUnitTest {
    private val cal : Calculator<Int> = mock()

    @Test
    fun addition_isCorrect() {
        given(cal.value) willReturn 1000
        val cal2 = SimpleCalculator(15)

        cal2.minus(cal.value)

        cal2.value shouldEqualTo 15
    }
}
```
`Calculator` 라는 인터페이스 를 `mock()` 목킹 하고 이 목에서 value 를 가져올때 `willReturn()` 으로 1000 이라는 값을 반환하게 하였다. infix 로 인하여 좀 더 읽기 쉽고 쓰기 쉽게 테스트 코드를 작성 할 수 있다. 

## 3. 결론
kotlin 의 extension function 과 infix function 은 매우 유용하다. 특히 유틸리티성 메소드를 만들때 에도 유용하다. 다음에는 String 이나 List 등 에서 자주 사용되는 확장 함수들을 만들어 보도록 하겠다. 

