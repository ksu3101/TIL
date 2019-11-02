# High order function, Lambda

람다는 jdk8 이후에 java에서도 interface를 정의 하여 사용 한 적이 있다. 보통 어떠한 이벤트 후 콜백으로 사용 된 경험이 있을 것 이다. 

```java
interface OnItemSelectedListener<T> {
    public boolean onItemSelected(T selectedItem);
}

view.setOnItemSelectedListener(
    new OnItemSelectedListener<String>() {
        @Override
        public boolean onItemSelected(String selectedItem) {
            // event handling... and return boolean value. 
            return true
        }
    };
);

```

위 에서 `setOnItemSelectedListener()` 메소드 처럼 매개변수로 함수 그 자체를 받는 메소드를 고차 함수(high order function) 라고 하며, 매개변수 로 전달되는 함수를 람다(lambda)라고 한다. 

## 사용법

### 람다 문법 

람다는 중괗로 "{ }" 으로 되어 있으며 일반적으로 아래와 같은 형태를 갖는다. 

```kotlin
{ type1: T1, type2: T2 -> `return type` }

// example 
val incrementValue = { value: Int -> value++ }
val sumValues = { x: Int, y: Int -> x + y }
```

### 익명 함수 (Anonymous function)

람다를 패러미터로 사용 할 때 함수의 인스턴스를 생성 해 주어야 한다. 이 때 인스턴스의 선언을 메소드 매개변수를 통해 "익명" 으로 생성하여 전달 할 수 있다. 이는 java 와 비슷 하기도 하다. java 의 경우에는 위 `OnItemSelectedListener` 인터페이스를 메소드 람다 매개변수로 넘기는 예제 코드를 통해서 알 수 있다. 

코틀린에서는 람다 매개변수를 위 java 처럼 선언 해도 되지만 더 간단하게 인터페이스 콜백 선언이 아닌 함수 자체를 선언 하고 인스턴스를 람다 매개변수로 넘겨 줄 수 있다. 

```kotlin
// exmaple view class 
class View {    
    fun <T> setOnItemSelectedListener(listener : (T) -> Boolean ) {
        // ...
        true
    }
}

val view = View()
view.setOnItemSelectedListener<String> { stringValue: String ->
    // event handling... and return boolean value. 
    true
}
```

또한 람다식 에서의 매개변수가 하나만 있을 경우 `it` 키워드로 생략 하여 사용 가능 하다. 

```kotlin
val view = View()
view.setOnItemSelectedListener<String> { 
    println(it) // print string value. and return boolean value. 
    true
}
```

그리고 람다식의 매개변수가 아예 없는 경우 아래처럼 간단히 표현 할 수 있다. 그리고 반환 타입이 없을 경우 `Unit` 을 사용 한다. java 에서의 void 와 비슷 하다고 생각 하면 된다. 

```kotlin
// exmaple view class 
class View {    
    fun <T> setOnItemSelectedListener(listener : () -> Unit ) {
        // ...
    }
}
```

## 결론

고차함수와 람다을 활용 하면 인터페이스 콜백 지옥에서 벗어날 수 있다. 패러미터의 갯수 와 타입 등에 따라 만들어줘야 하는 인터페이스들을 추가로 생성 하지 않고 람다식을 이용 하여 콜백받고 핸들링 할 수 있게 할 수 있는 것 이다. 
