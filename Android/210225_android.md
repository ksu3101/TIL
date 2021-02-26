## An opinionated guide on improved Kotlin code

> 이 글은 Gabor Varadi의 [An opinionated guide on improved Kotlin code](https://medium.com/@Zhuinden/an-opinionated-guide-on-how-to-make-your-kotlin-code-fun-to-read-and-joy-to-work-with-caa3a4036f9e)을 번역 하였다. 내용에 빈약한 내용이 있어 코드와 설명을 일부 추가 하였음. 

(원본 글 작성자)는 이 글을 꼭 쓰고 싶었다고 한다. 

글에서 제공 될 팁과 코틀린 스타일 권장 사항(순서 없음)이 이 글을 보는 개발자들이 더 나은 코틀린 코드를 작성 하는데 도움이 되기를 바란다. 

### 생성자에 2개 이상 인수가 있을 경우 클래스 정의 라인과 같은 줄에 표기 하지 말자. 

> For 2 or more constructor arguments, prefer not to keep the properties on the same line as the class name in the constructor definition

```kotlin
// bad
class MyClass(val a: A, val b: B): MyPrarentClass(a, b), MyInterface {
    // ...
}

// ok 
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

> 참고 : 예제에서 null체크 예제를 위해 `it` 인수의 이름을 명시적으로 지정하지 않았음

### 2개 이상의 람다가 함수에 전달된 경우 항상 람다 인수에 이름을 지정 한다. 

RxJava를 사용 하면 아래와 같은 코드를 볼 수 있다. 

```kotlin
observable.subscribe({
    showData(it)            // no
}, {
    it.printStackTrace()    // no
})
```

그러나 함수에 전달되는 람다가 2개 이상이기 때문에 지정된 이름을 갖는 인수를 사용 하는 경우 람다를 읽는 것이 훨씬 더 쉽다. 

```kotlin
observable.subscribe(
    onNext = { data ->
        showData(data)
    }, 
    onError = { e ->
        e.printStackTrace()
    }
)
```

### 공용 함수이고 인터페이스 함수의 "재정의"가 아닌 경우 함수의 반환 유형을 지정 한다. 

간단한 함수의 경우 코틀린의 한줄 할당 구문을 사용할 수도 있지만, 특히 라이브러리를 작성 하는 경우 올바른 예상 타입을 반환하는지 확인 하는게 더 좋지만 어느 쪽이든 좋은 방법이다. 

```kotlin
override fun getItemCount() = 0     // ok

val date: SimpleDateFormat      // typed
    get() = SimpleDateFormat("yyyy-MM-dd")
```
> Java와 같이 사용 하는 경우 함수의 반환 타입을 될 수 있으면 명시 해주는 것 이 좋다. 특히 dagger와 같은 어노테이션 프로세스를 사용할 경우.

### 튜플(tuple)로 사용하지 않는 데이터 클래스에 대해 position decomposition을 사용하지 말 것. 

튜블(`Pair`나 `Triple`같은 것)은 위치 정보를 포함하고 일반적으로 의미 정보를 갖지는 않는다. (단 색상은 예외적이다. 기술적으로 ARGB순서 값의 튜플 이지만 배열은 변경 될 가능성이 아예 없다)

데이터 클래스 `Book`과 같은 일반 데이터 클래스는 `val (title, author) = Book`과 같은 position decomposition에 사용 되서는 안된다. 변경될 가능성이 높고 필드 순서의 변경으로 인하여 기존 코드가 눈에 띄지 않게 손상될 수 있다. 

### 할당할때 `if-else`대신 `when`을 사용 하고 항상 `} else if`보다 `when`을 선호 할 것. 

다음과 같은 코드가 있을 때, 

```kotlin
val x = if (condition) {
    0
} else {
    1
}
```

`when`키워드를 이용하여 조건과 할당된 값만을 깔끔하게 그룹화 하여 읽고 이해하기 쉽게 만들 수 있다. 

```kotlin
val x = when {
    condition -> 0
    else -> 1
}
```

이 스타일을 적용할 때 IDE lint는 `when (condition) { }`을 사용하도록 요청할 수 있지만 불필요할 정도로 더 장황하게 만든다. 따라서 조건이 실제로 단순한 bool표현식 인 경우 `when()`그 자체로 표현될 수있다. 

그러나 `when(value)`는 열거형 및 sealed클래스에 매우 유용하며, 이 경우 표현식을 완전하게 모두 완성해줘야 한다. 

```kotlin
when (enum) {
    A -> ...
    B -> ...
    C -> ...
}.safe()

fun <T> T.safe() = this
```

`when(val x = expression) {`은 때떄로 사용될 수 있지만 상황에 따라 다르다. 

### 항상 `var`보다 `val`을 선호 한다. (`var`이 필요하지 않은 상황에서)

값혹은 참조가 변경되지 않아야 한 경우 변경 불가능(immutable)하게 만들어야 한다. 

### 변경 가능한(Mutable) 데이터 구조를 일반 인수 또는 함수 입력 매개 변수로 노출하지 않는다. 

`ArrayList<T>`를 사용하거나 `ArrayList`, `MutableList`, `LinkedList`등과 같이 변경 가능한 값을 포함하는 `LiveData`가 있는 경우 코틀린의 변경 불가능한 컬렉션을 사용하려면 `List<T>`이어야 한다. Java에서도 다음과 같은 패턴은 일반적이다. 

```java
public List<String> someMethod(List<String> values) {
    List<String> newValues = new ArrayList<>(values);
    // newValues를 갖고 뭔가 함..
    return Collections.unmodifiableList(newValues);
}
```

그러나 코틀린에서는 타입 시스템이 리스트에 대한 수정을 허용하지 않기 때문에 이를 단순화 할 수 있다. 

```kotlin
fun List<String>.someMethod(): List<String> {
    val newValues = toMutableList()
    // newValues를 갖고 뭔가 함.. 
    return newValues.toList()
}
```

기억할 사항 : `LiveData<ArrayList<T>>`는 `LiveData<List<T>>`로 사용 해야 한다. 

### 백업 속성을 포함하여 모든 변수 이름에 `_prefix`를 사용하지 않는다. 

이것에 대해서는 개인적인 성향이 있을 가능성이 높지만, 뒷받침 속성의 private필드는 중요한 것을 나타내는 경향이 있으므로 _접두사는 예민한 것으로 읽는다. 

`_name`을 사용하는 것 보다 더 나은 이름을 지정할 수 있다면 `currentName`과 같은 이름으로 대신 사용하는 것 이 좋다. 

### 실제로 필요한 경우에만 internal 가시성을 사용 하라. 

대부분의 단일 모듈앱에서는 Java의 패키지 가시성을 대체하기 위하여 internal가시성을 사용 한다. 이 경우 비 라이브러리 모듈이라면 private 또는 public이 의도를 더 명확하게 만들어 줄 것이다. 

### 최상위 확장 기능을 사용할 수 있다면 "__Util"클래스/객체를 만들지 말 것. 

이제 "정적 도움 함수"를 최상위 수준으로 만들 수 있으며 확장 프로그램을 사용 할 때 관용적인 코틀린 코드를 만들 수 있는 경우가 더 많아졌다. 

### 튜플을 공용 함수의 반환 유형으로 노출하지 말고 속성의 이름이 지정되지 않은 범위를 최소화 할 것. 

떄떄로 `Triple<String, String, String>`을 반환 유형으로 노출하는 API를 볼 수 있다. 그러나 이것은 각 속성이 실제로 무엇인지에 대한 의미 정보가 없으므로 실제 의미가 부여된 이름을 갖는 데이터 클래스를 선호 한다. 

인라인 클래스가 없는 튜플은 일반적으로 위치 분해를 사용하여 가능한 한 빨리 속성으로 확인 되어야 한다. 

```kotlin
combineTuple(name, password)
    .observe(viewLifecyleOwner) { (name, password) -> 
        // ...
    }
```

`pair.first`및 `pair.second`를 사용하지 말고 항상 위치 분해를 선호 하자. 속성 중 하나만 필요한 경우 다음처럼 사용할 수도 있다. 

```kotlin
val (_, password) = tuple
```
 