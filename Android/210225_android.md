## An opinionated guide on improved Kotlin code

> 이 글은 Gabor Varadi의 [An opinionated guide on improved Kotlin code](https://medium.com/@Zhuinden/an-opinionated-guide-on-how-to-make-your-kotlin-code-fun-to-read-and-joy-to-work-with-caa3a4036f9e)을 번역 하였다. 

(원본 글 작성자)는 이 글을 꼭 쓰고 싶었다고 한다. 

글에서 제공 될 팁과 코틀린 스타일 권장 사항(순서 없음)이 이 글을 보는 개발자들이 더 나은 코틀린 코드를 작성 하는데 도움이 되기를 바란다. 

### 생성자에 2개 이상 인수가 있을 경우 클래스 정의 라인과 같은 줄에 표기 하지 말자. 

> For 2 or more constructor arguments, prefer not to keep the properties on the same line as the class name in the constructor definition

```kotlin
class MyClass(
    val a: A,
    val b: B
): MyPrarentClass(a, b), MyInterface {
    // ...
}
```

클래스 선언부에 세번째 인수이상을 추가 하려 할 때 화면 오른쪽에 너무 멀리 존재 하므로 화면 가시성이 떨어지므로 멀티 라인으로 생성자의 인수를 정의 하는 것 이 바람직 하다. 

차례로 나열 하게 되면 (1.4.21+ 이후의 후행 쉼표 포함) 더 나은 가독성과 확장성을 가져오게 된다. 

### 범위 지정 함수인 `let`은 할당 또는 `return`문에서만 사용 하고 일반적인 제어 흐름으로는 사용하지 말자. 

종종 코틀린 코드에서 다음과 같은 작업을 수행하는 경향이 있다. 

```kotlin
fun myMethod(a: A?) {
    a?.let {    // 안좋은 코드
        it.x()  // 이것도
        it.y()  // 이것도
        it.z()  // 이것도
    }
}
```

> 참고 : 위와 같은 메소드에서 `A?`는 null 허용 여부를 활용하기 위해 `A`로도 대체 될 수 있다. 이것은 단지 예제일 뿐이다. 

유지 관리 및 코드 가독성을 높이기 위해서 간단한 할당 혹은 return문에만 `?.let {}`을 사용하는것이 좋으며 일반적인 제어 흐름으로는 사용하지 않는 것이 좋다. 

예를 들어 아래와 같은 체인코드를 보면 이해하기 쉬울 것 이다. 

```kotlin
val protoClass = savedInstanceState?.getByteArray("protoValue")
        ?.let { ProtoClass.parceFrom(it) }
```

그러나 이 경우 `?.let {}`은 할당의 일부가 된다. 

또한, `?.let {}`을 `?:`와 연계 하여 기본값을 제공 할 수 있지만 (특히 `takeIf`와 같이 사용하여) 사이드 이펙트를 실행 하는데 사용 해서는 안된다. 

사실, `x?.let {} ?: run {}`은 `if-else`문 및 null체크에 대한 용도가 아니며 해당 용도로 이 형식들을 사용해서는 안된다! 이는 예기치 않은 부작용이 발생할 수 있다. 

### 여러 줄, 람다 내 에서 암시적인 인수 이름 `it`을 모두 사용하지 않는다. 

간단히 말해서, `it`은 단일 행 표현식이나 단순한 람다에서만 사용 한다. 

여러줄로 된 함수에서 `it`이 보이면 의미있는 이름으로 바꿔주는것이 좋다. (예외적으로 오류일 경우 `e`조차도 의미가 더 많이 있다)

### 항상 암시적인 인수 이름 대신 `map`, `flatMap`, `switchMap`, `flatMapLatest`등 람다 인수 및 그 뒤에 오는 함수에서 이름을 지정 한다. 

작업 중 현재 타입을 다른 타입이나 의미에 매핑하는 함수가 온다면 다음 함수의 입력인수 이름을 의미있는 이름으로 변경 해야 한다. 

```kotlin
val namesStartingWithZ = itemList.map { it.name }
        .filter { name -> name.startWith("Z") }
```

### 다른 분기가 없는 경우 null체크 대신 elvis-operator와 결합 하여 이른 return을 하자. 

필드 수준에서 변경 가능한 nullable필드 또는 `observe {}`등이 선택적인 값을 제공 하는 것 은 매우 일반적이다. 이 경우 null 체크를 수행 해야 한다. 

이 경우 사람들은 일반적인 제어 흐름문으로 `?.let {}`을 많이 사용 한다. 

변수를 `val x = x`에 할당 하고 `if (x != null) {}`확인 (원래 변수 이름을 유지 하면서 세이프 캐스팅을 사용)을 수행하는 것이 바람직 하다. 

```kotlin
val x = x
if (x != null) {
    // `x`값을 이제 non-null으로 사용 할 수 있다. 
}
```

또한 evlis + return(또는 elvis + break, elvis + continue)에 의존할 수 있다. 

```kotlin
liveData.observe(viewLifecycleOwner) {
    val value = it ?: return@observe
    // ... 
}
```