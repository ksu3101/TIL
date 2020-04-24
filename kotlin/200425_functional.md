## Inline functions

코틀린에서 함수의 패러미터의 구현을 람다로 구현 하고 이 람다로 구현된 함수의 레퍼런스를 갖게되어 객체처럼 사용 할 수 있다. 이때 함수의 구현을 패러미터 혹은 반환자를 이용 해서 좀 더 유연한 함수를 구현할 수 있다. 이 를 `Higher-order function`(고차 함수) 라고 한다.

고차함수의 예 로 코틀린의 collection 함수중 하나인 `foldRight()`를 보도록 하겠다. `foldRight()` 함수의 원형은 아래와 같다. 

```kotlin
public inline fun <T, R> List<T>.foldRight(initial: R, operation: (T, acc: R) -> R): R {
    var accumulator = initial
    if (!isEmpty()) {
        val iterator = listIterator(size)
        while (iterator.hasPrevious()) {
            accumulator = operation(iterator.previous(), accumulator)
        }
    }
    return accumulator
}
```

`fold()` 와 비슷하지만 `foldRight()` 는 콜렉션의 맨 마지막 원소부터 0번째 인덱스의 원소까지 이터레이션 하면서 함수의 결과를 다음 함수에 전달 한다. 

```kotlin
fun main() {
    val items = listOf(1, 2, 3, 4, 5)
    val sum = items.foldRight(0) { it, acc ->
        println("it = $it, acc = $acc")
        it + acc
    }
    println("result = $sum")
}
```

위 예제 에서 print 된 결과를 보면 아래와 같다. 

```
it = 5, acc = 0
it = 4, acc = 5
it = 3, acc = 9
it = 2, acc = 12
it = 1, acc = 14
result = 15
```

맨 뒤의 항목부터 0번째 인덱스의 원소까지 이터레이션 하면서 구현한 함수를 거치는 것 을 확인 할 수 있다. 

`foldRight()` 에서 구현해야 할 `operation` 이라는 패러미터의 형태는 `(T, acc: R) -> R` 이다. 위의 예제와 같이 대입해 보면, `T` 는 예제에서 `List<Int>` 인 `items` 의 각 원소를 말하며 `acc: R` 은 최초 initialized value 인 `R` 이며 그리고 `operation` 함수를 거치고 난 뒤 반환된 값이다. 

결국 마지막 인덱스에서 0 번까지 이터레이션 하면서 각 항목에 대해 함수에 대한 결과를 반환하고, 이를 다음 함수에 전달 한뒤 마지막 결과를 `foldRight()` 함수에서 반환함을 알 수 있다. 

## inline, noinline, crossinline

고차함수의 경우 익명함수(java에서는 `Function` 이라는 인터페이스의 구현) 의 실체화를 통해 익명함수 구현 내부의 내용을 익명함수의 내부로 옳기고 인스턴스화 시켜준다. 인스턴스화 시켜 줄 때 에는 Java 에서 말하는 `new` 의 과정이 필요 한 것이다. 

하지만 고차함수가 많아지면 많아질수록 그리고 접근이 쉬운 고차함수의 특성 상 많은 수의 익명함수들이 new 되어진다. 이는 런타임시 오버헤드가 발생 할 수 있는 원인이 될 것 이다. 

그래서 고차함수의 익명 함수를 `inline` 처리 하여 런타임시 오버헤드를 줄일 수 있다. 

### `inline` 

inline 키워드는 익명함수 의 구현 내용을 Function 구현체 에 만들지 않고 컴파일시 호출되는 곳 에 적용 시켜준다. 말보다는 아래 예제 코드를 보면 이해가 더욱 더 빠를 것 이다. 

```kotlin 
fun noneInlinedFunc(block: () -> String) {
    println(block)
}

inline fun inlinedFunc(block: () -> String) {
    println(block())
}
```

동일한 작업을 수행하는 두가지 고차함수를 만들고 이 함수를 아래와 같이 각각 호출 하려 한다. 

```kotlin
fun runner1() {
    noneInlinedFunc {
        val a = 10
        val b = 20
        "${a+b}"
    }
}

fun runner2() {
    inlinedFunc {
        val a = 10
        val b = 20
        "${a+b}"
    }
}
```

이 코드들을 디컴파일 한 결과는 아래와 같다. 

```java
public static final void runner1() {
   noneInlinedFunc((Function0)null.INSTANCE);
}
```

일단 첫번째 함수로, `inline` 을 작성하지 않은 호출 함수의 내부이다. `Function0` 인스턴스를 생성함을 알 수 있다. (디컴파일의 코드에 대해서는 조금더 확인이 필요하지만 인스턴스에 대한 캐스팅이 되어진것 으로 보아서는 어떠한 싱글턴 인스턴스를 가져와서 invoke 하는 것 같다) 

```kotlin
public static final void runner2() {
   int $i$f$inlinedFunc = false;
   int var1 = false;
   int a = 10;
   int b = 20;
   String var4 = String.valueOf(a + b);
   boolean var5 = false;
   System.out.println(var4);
}
```

두번째 함수인 `inline` 키워드가 포함된 고차함수를 콜 한 함수의 내부 디컴파일 코드 이다. 구현될 익명 함수의 구현내부가 호출 함수내부에 있음을 확인 할 수 있다. 그렇기 떄문에 코드 블럭 내의 작업을 제외한 호출함수에서 어떠한 인스턴스를 새로 생성하는 작업은 보이지 않는다. 

inline 키워드를 사용 하면 익명함수를 구현한 고차함수 등을 사용 할 때 유용하다. 하지만 단점이 있다면, 

1. `inline` 을 사용 하는 함수를 호출하는 쪽 에서 block 내 많은 코드를 작성 시 컴파일 할 때 옳겨지는 코드의 양이 증가 하므로 호출되는 측에서 작성 되는 익명함수 내 구현코드는 적을 수록 좋다. 
2. 컴파일시 약간의 퍼포먼스 영향이 있을 수 있다. 

### `noinline`, `crossinline`

`inline` 키워드를 적용한 함수를 사용 할 때 임의로 특정 고차함수에 대해 inline 을 사용하지 않을 경우 해당 패러미터에 `noinline` 키워드를 사용 할 수 있다. 

`crossinline` 키워드는 패러미터로 전달될 익명함수 내부에서 다른 컨텍스트 (로컬 객체, 중첩 함수, 스레드) 를 통해서 호출 하도록 한다. 

### `reified`





