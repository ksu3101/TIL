# Guidelines for writing better tests

> 이 글은 Bevan Steele의 [Guidelines for writing better tests](https://www.rockandnull.com/guidelines-for-writing-better-tests/)을 번역 하였다. 

이론적으로 테스트는 소프트웨어이다. 그래서 숙련된 소프트웨어 엔지니어라면 좋은 소프트웨어를 만드는데 사용한 것 과 동일한 원칙을 테스트 코드에도 적용 할 수 있다. 과연 그럴까?

코드를 작성하는 것 과 같은 방법으로 테스트 코드를 작성하면 유지 보수및 디버깅이 어려운 테스트 코드들이 만들어 질 수 있다. "값"을 "좋음"으로 정의해버리는 테스트는 일반적인 코드와 동일하지 않다. 이 글은 원본 작성자가 수년동안 발견한 몇가지 중요한 지침들을 모아 가이드 하는것을 목표로 한다. 이 글은 완전하지는 않지만 다른 사람들의 시간을 절약 하기 위해 공유할 수 있는 가이드들이 되었으면 한다. 

## Readability is priority number one

테스트는 구문 분석을 위해 이해하기 쉬어야 한다. 즉 DRY(반복하지 않는다)와 같은 일반 코드에 적용되는 모범 사례들을 "위반" 할 수 있다. 실제로는 매번 설정 단계를 반복하더라도 테스트에서 각 테스트 시나리오를 정의 하는 것 이다. 테스트 케이스가 실패하고 다른 누군가가 그 테스트를 디버그 할 때 테스트 케이스의 모든 "준비"들을 한 곳 에서 확인할 수 있어야 한다. 

```kotlin
fun testCase1() {
    val item1 = makeItem("item1")
    service.setItems(item1)

    // ...
}

fun testCase2() {
    val item1 = makeItem("item1")
    service.setItem(item1)

    // ... 
}
```

## Always Arrange-Act-Assert

이 단순하지만 강력한 패턴을 적용하면 모든 테스트에 어느 정도의 균일성을 예외없이 제공할 수 있다. 우리가 [코드 베이스](https://www.rockandnull.com/code-style/)의 공통 코드 스타일에 동의 하는 이유들과 동일하다. 이상적으로 각 테스트의 3단계는 테스트가 한 눈에 훑어보고 쉽게 분석할 수 있도록 빈줄로 구분 한다. 

> 역자 : 테스트 코드를 3가지 단계(given-when-then)으로 작성하고 이 단계단위 코드들을 빈 줄로 구분한다. 

```kotlin
fun testCase1() {
    val item1 = makeItem("item1")
    service.setItems(item1)

    service.callAction1()

    assertThat(storage).isEqualTo(STATUS_1)
}

fun testCase2() {
    val item1 = makeItem("item1")
    val item2 = makeItem("item2")
    service.setItem(item1, item2)

    service.callAction2()

    assertThat(storage).isEqualTo(STATUS_2)
}
```

## Test a single behavior

각 테스트 사례에서는 단일 동작에 대한 테스트를 하는 것 을 목표로 해야 한다. 테스트에서 너무 많은 항목들을 확인하려 하면 테스트가 실패할 때 마다 어디에서 문제가 발생한 것 인지 파악하기 어려워진다. 그렇다고 해서 단일 `assert`문이 있어야 한다는 것 은 아니며, 여러 `assert`문으로 동작을 확인 할 수 있다. 그러나 두개 이상의 동작을 테스트 하려는 경우 테스트를 여러 테스트 케이스들로 분할 하는것에 주의 해야 한다. 

> 역자 : 테스트 코드는 단 한개의 동작에 대해서만 테스트 해야 된다. 테스트 후 검증에 대해서는 여러번 해도 상관없다.

```kotlin
fun testCreate() {
    service.init()

    val response = service.createItem("item1")

    assertThat(storage.read()).contains("item1")
    assertThat(response).isEqualTo("item1")
}
```

## Be careful of external dependencies

스프트웨어 프로젝트에서 소유하지 않은 서비스 또는 API와 같은 외부 종속성을 갖는 것은 일반적이다. 이러한 외부 종속성을 사용하는 소프트웨어를 테스트 하려면 어떻게 될까? 외부 서비스 또는 API에서 mock데이터들(fake implement)를 제공하지 않는 경우 외부 종속에 대한 호출을 래핑한 뒤 래퍼를 mocking하면 된다. 이는 종속성이 다른 것 으로 대체될 때 까지 테스트가 중단되지 않도록 보호하기 위한 것 이다. 몰론 이러한 접근 방식은 외부 종속성 코드에 의해 버그가 있을 경우 테스트에서 이를 감지 하지 못함을 의미 하기 떄문에 이를 유의 해야 한다.

> 역자 : 외부 의존을 가진 인스턴스가 있을 경우 이를 mocking하기 위해 래퍼클래스로 만들어서 mocking한다. 

```kotlin
fun testCase1() {
    service.init(mockWrapper)

    service.callAction()

    verify(mockedWrapper).wasCalled()
}
```

이 간단한 가이드 목록이 더 나은 테스트 코드를 작성하기 위한 좋은 출발점이 되기를 희망 한다. 
