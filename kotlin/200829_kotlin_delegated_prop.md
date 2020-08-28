### Kotlin - Delegated Properties (위임된 프로퍼티)

- 이 문서는 [Kotlin 공식 문서 중 Delegated Properties](https://kotlinlang.org/docs/reference/delegated-properties.html)를 번역하여 정리한 문서이다. 
- 이 자주 사용되는 단어들
  - property : 속성, 클래스의 멤버 혹은 필드와 같은 객체. 이 문서에서는 위임되어 초기화 및 값이 설정될 대상이다. 이 게시글에서는 일괄적으로 "프로퍼티"라고 작성 하였다. (속성이라는 직역된 이름을 사용할 수 있지만 개인적으로 헷갈림)
  - delegate : 대리자, 위임자, property를 초기화 및 값을 설정할 때 대신 설정해주는 람다등 과 같은 인스턴스들 이다. 이 문서에서는 일괄적으로 "위임" 혹은 "위임자"라는 이름을 사용 하였다. 
  - 이번 문서는 일단 간단하게 정리를 하고 나중에 보기 좋게 문맥을 고쳐야 할거 같다.. 번역 난이도도 높고 각 명사들에 대해 정확하게 한글로 어떻게 적용해야 할지 어렵다.... 

### Delegated Properties

공통된 종류의 프로퍼티가 있을때 필요할 때마다 수동으로 구현하여 설정할 수 있지만, 한번에 이를 구현하고 라이브러리에 놓는것이 매우 좋다. 예를 들면 아래와 같다. 

- 지연된(lazy) 프로퍼티 : 값은 최초 접근할때만 처리되어 설정 된다. 
- 관찰 가능한 프로퍼티 : 관찰자의 핸들러는 이 프로퍼티의 변경 사항에 대한 알림을 받는다. 
- 각 프로퍼티에 대해서 `Map`에 프로퍼티를 저장 할 때. 

이러한 사례들을 다루기 위해 코틀린에서는 위임된 프로퍼티를 지원 한다. 

```kotlin
class Example {
    var p: String by Delegate()
}
```

위임된 프로퍼티의 구문은 `val/var <property name>: <type> by <expression>`의 형태로 작성 된다. 프로퍼티에 해당하는 `get()` (및 `set()`)들 은 `getValue()`함수 및 `setValue()`함수에 의해 위임되기 때문에 `by`이후의 expression(표현식)은 delegate(위임자)가 된다. 프로퍼티의 위임은 특정 인터페이스를 구현할 필요는 없지만 `getValue()`함수 (`var`일 경우 `setValue()`)을 구현해야 한다. 예를 들면, 

```kotlin
import kotlin.reflect.KProperty

class Delegate {
     operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return "$thisRef, thank you for delegating '${property.name}' to me!"
    }
 
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        println("$value has been assigned to '${property.name}' in $thisRef.")
    }
}
```

예제 `Example`클래스 를 보면 `Delegate`클래스의 인스턴스에게 위임하는 프로퍼티 `p`를 읽기위해 접근을 시도하면, `Delegate`클래스의 `getValue()`함수가 호출 되어 `p`는 `getValue()`에서 반환되는 객체가 위임되어 값이 설정 된다. 

```kotlin
val e = Example()
println(e.p)
```

위 예제의 경우에는 아래와 같은 내용을 출력하게 된다.

```
Example@33a17727, thank you for delegating ‘p’ to me!
```

마찬가지로, `p`에 값을 할당하면 `setValue()`함수가 호출되어 값이 설정 된다. 

```kotlin
e.p = "NEW"
println(e.p)
```

위 예제를 실행하면 아래처럼 출력 하게 된다. 

```
NEW has been assigned to ‘p’ in Example@33a17727.
```

추가로 코틀린 1.1버전 부터는 함수 또는 코드 블록 내에 위임된 프로퍼티를 선언할 수 있으므로 반드시 클래스의 구성원일 필요는 없다. 

### 1. Standard delegates (표준 대리자)

코틀린 표준 라이브러리에서는 여러 종류의 유용한 위임를 위한 팩토리 메소드들을 제공 한다.

#### 1.1 Lazy

`lazy()`함수는 패러미터로 전달받은 람다를 이용해 lazy 프로퍼티를 구현하기 위한 위임자 역할을 한다. `lazy()`는 `Lazy<T>`의 인스턴스를 반환하는 함수이다. `lazy()`의 `get()`에 대한 첫번째 함수 호출은 `lazy()`에 전달된 람다를 실행하고 람다에서 반환되는 값을 저장 한다. 그리고 이어지는 `get()`의 함수호출은 이전에 저장했던 값을 그대로 반환한다.

```kotlin
val lazyValue: String by lazy {
    println("computed!")
    "hello"
}

fun main() {
    println(lazyValue)
    println(lazyValue)
}
```

위 예제를 실행시키면 아래처럼 출력 된다. 

```
computed!
hello
hello
```

기본적으로 지연 프로퍼티는 동기화되어 적용 된다. 값은 하나의 스레드에서만 처리되며 모든 스레드는 같은 값을 보게 된다.  여러 스레드에서 동시에 실행할 수 있도록 초기화 위임의 동기화가 필요하지 않은 경우 `LazyThreadSafetyMode.PUBLICATION`을 `lazy()`함수에 매개 변수로 전달 하면 된다. 그리고 초기화가 항상 프로퍼티를 사용하는 스레드와 동일한 스레드에서 발생한다고 확신하는 경우 `LazyThreadSafetyMode.NONE`을 사용할 수 있다. 이 경우 스레드 안정성의 보장 및 관련 오버헤드를 발생시키지 않는다. 

#### 1.2 Observable

`Delegates.observable()`함수는 두개의 인수, 즉 초기값과 수정을 위한 핸들러인 람다를 사용 한다. 이 핸들러는 프로퍼티가 할당 할 때마다 호출된다. `Delegates.observable()`의 핸들러에는 3개의 매개 변수가 있다. 그것은 할당될 프로퍼티, 이전 값과 새로운 값 이다. 

```kotlin
import kotlin.properties.Delegates

class User {
    var name: String by Delegates.observable("<no name>") {
        prop, old, new ->
        println("$old -> $new")
    }
}

fun main() {
    val user = User()
    user.name = "first"
    user.name = "second"
}
```

새 값으로 할당을 하지 않으려면 `vetoable()`함수를 사용 하면 된다. 할당을 거부하겠다고 전달된 핸들러에서는 새 속성값 할당이 수행되기 전에 호출 된다. 

### 2. Delegating to another property (다른 프로퍼티에 위임 하기)

코틀린 1.4버전부터 프로퍼티는 getter 및 setter를 다른 프로퍼티에 위임할 수 있다. 이러한 위임은 최상위 및 클래스 프로퍼티(멤버 및 확장된)모두에 사용 할 수 있다. 위임 프로퍼티는 다음과 같을 수 있다. 

- 최상위 프로퍼티 
- 동일한 클래스의 멤버 또는 확장 프로퍼티
- 다른 클래스의 멤버 또는 확장 프로퍼티 

프로퍼티를 다른 프로퍼티에게 위임하려면 위임 이름에 `::`한정자를 사용 한다. (예를 들어, `this::delegate`또는 `MyClass:delegate`)

```kotlin
class MyClass(var memberInt: Int, val anotherClassInstance: ClassWithDelegate) {
    var delegatedToMember: Int by this::memberInt
    var delegatedToTopLevel: Int by ::topLevelInt
    
    val delegatedToAnotherClass: Int by anotherClassInstance::anotherClassInt
}
var MyClass.extDelegated: Int by ::topLevelInt
```

예를 들어 이전 버전의 프로퍼티와 새 버전의 프로퍼티의 이름을 바꾸고 기능은 그대로 사용하고 싶은 경우에 유용하게 쓸 수 있다. 새 프로퍼티를 도입하고 `@Deprecated`주석으로 이전 프로퍼티에 주석을 추가 하고 구현을 위임하면 된다. 

```kotlin
class MyClass {
   var newName: Int = 0
   @Deprecated("Use 'newName' instead", ReplaceWith("newName"))
   var oldName: Int by this::newName
}

fun main() {
   val myClass = MyClass()
   // Notification: 'oldName: Int'는 곧 제거될 예정이다. 
   // 'newName'을 사용 하는것 을 추천 한다. 
   myClass.oldName = 42
   println(myClass.newName) // 42
}
```

### 3. Storing properties in a Map (Map에 프로퍼티를 저장 하기)

일반적인 사용 방법중 하나는 Map에 프로퍼티 값을 저장하는 것 이다. 이는 JSON파싱 또는 기타 "동적"작업과 같은 어플리케이션에서 자주 발생한다. 이 경우 Map인스턴스 자체를 위임 된 속성의 위임자로 사용 할 수 있다. 

```kotlin
class User(val map: Map<String, Any?>) {
    val name: String by map
    val age: Int     by map
}
```

예를 들어 위 처럼 생성자에서 Map인스턴스를 받을 경우, 아래의 예제 코드 처럼 프로퍼티를 Map에 저장할 수 있다. 

```kotlin
val user = User(mapOf(
    "name" to "John doe",
    "age" to 25
))
```

위임된 프로퍼티는 Map에서 값을 가져 올때 기존 방식 처럼 키-값 쌍으로 가져온다. 

```kotlin
println(user.name) // Prints "John Doe"
println(user.age)  // Prints 25
```

값 수정이 가능한 `MutableMap`을 사용하는 경우 `var`의 프로퍼티에 적용할 수 있다. 

```kotlin
class MutableUser(val map: MutableMap<String, Any?>) {
    var name: String by map
    var age: Int     by map
}
```

### 4. Local delegated properties (지역 프로퍼티 위임)

지역 변수를 위임된 프로퍼티로(예를 들어 `lazy`) 선언하여 사용할 수 있다. 

```kotlin
fun example(computeFoo: () -> Foo) {
    val memoizedFoo by lazy(computeFoo)

    if (someCondition && memoizedFoo.isValid()) {
        memoizedFoo.doSomething()
    }
}
```

### 5. Property delegate requirements (프로퍼티 위임시 요구사항)

이 항목에서는 객체를 위임하기위한 요구 사항들을 요약한다. 

읽기 전용 속성(`val`)의 경우 위임자는 다음 매개변수와 함께 연산자 함수 `getValue()`를 제공 해야 한다. `getValue()`함수에서는 아래 2개의 매개변수를 제공 한다. 

- `thisRef` : 동일하거나 프로퍼티 소유자의 상위 타입 이어야 한다. (확장 프로퍼티의 경우 확장중인 타입)
- `property` : `KProperty<*>`타입 또는 상위 타입 이어야 한다. 

`getValue()`함수는 프로퍼티(또는 하위 타입)과 동일한 타입을 반환해야 한다. 

```kotlin
class Resource

class Owner {
    val valResource: Resource by ResourceDelegate()
}

class ResourceDelegate {
    operator fun getValue(thisRef: Owner, property: KProperty<*>): Resource {
        return Resource()
    }
}
```

가변 속성(`var`)의 경우 위임자는 아래 3개의 매개변수와 함께 연산자 함수 `setValue()`를 추가로 제공 해야한다. 몰론 `getValue()`함수도 제공 해야 한다. 

- `thisRef` : 동동일하거나 프로퍼티 소유자의 상위 타입 이어야 한다. (확장 프로퍼티의 경우 확장중인 타입)
- `property` : `KProperty<*>`타입 또는 상위 타입 이어야 한다. 
- `value` : 프로퍼티(또는 상위 유형)과 동일한 유형이어야 한다. 

```kotlin
class Resource

class Owner {
    var varResource: Resource by ResourceDelegate()
}

class ResourceDelegate(private var resource: Resource = Resource()) {
    operator fun getValue(thisRef: Owner, property: KProperty<*>): Resource {
        return resource
    }
    operator fun setValue(thisRef: Owner, property: KProperty<*>, value: Any?) {
        if (value is Resource) {
            resource = value
        }
    }
}
```

`getValue()`또는 `setValue()`함수들은 위임자 클래스의 멤버 또는 확장 함수로 제공 될 수 있다. 확장 함수의 경우 기존 제공되지 않던 클래스에 프로퍼티를 위임해야 할 때 편리하다. `getValue()`와 `setValue()`모두 `operator` 키워드로 표시 되어야 한다. 

코틀린 표준 라이브러리의 `ReadOnlyProperty`및 `ReadWriteProperty`인터페이스를 이용하여 새 클래스를 만들지 않고도 위임자를 익명 객체로 만들 수 있다. 이 인터페이스들은 위임을 하기 위해 필요한 함수들을 제공한다. `ReadOnlyProperty`에서는 `getValue()`를 구현해야 하며, `ReadWriteProperty`는 이를 확장하고 `setValue()`를 추가로 구현해야 한다. 따라서, `ReadOnlyProperty`가 예상 될 때마다 `ReadWriteProperty`를 전달 할 수 있다. 

```kotlin
fun resourceDelegate(): ReadWriteProperty<Any?, Int> =
    object : ReadWriteProperty<Any?, Int> {
        var curValue = 0 
        override fun getValue(thisRef: Any?, property: KProperty<*>): Int = curValue
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
            curValue = value
        }
    }

val readOnly: Int by resourceDelegate()  // ReadWriteProperty 를 val 객체로 선언 
var readWrite: Int by resourceDelegate()
```

#### 5.1 Translation rules (번역 규칙)

내부적으로 모든 위임된 프로퍼티에 대해 코틀린 컴파일러는 보조 프로퍼티를 생성하고 여기에 위임한다. 예를 들어, 프로퍼티 `prop`의 경우 숨겨진 프로퍼티 `prop$delegate`가 생성되고 접근자(accessors)의 코드는 다음 추가될 프로퍼티에 간단히 위임한다. 

```kotlin
class C {
    var prop: Type by MyDelegate()
}

// 아래 코드는 컴파일러가 생성한 코드 이다. 
class C {
    private val prop$delegate = MyDelegate()
    var prop: Type
        get() = prop$delegate.getValue(this, this::prop)
        set(value: Type) = prop$delegate.setValue(this, this::prop, value)
}
```

코틀린 컴파일러는 인수 `prop`에 대한 모든 필수 정보를 제공한다. 첫번째 인수 `this`는 외부 클래스 `C`의 인스턴스를 참조하고 `this::prop`은 `prop`자체를 설명 하는 `KProperty`타입의 객체이다. 

#### 5.2 Providing a delegate (대리자 제공)

`provideDelegate`오퍼레이터(operator)를 정의하여 프로퍼티의 구현이 위임되는 객체를 확장할 수 있다. `by`의 오른쪽에 사용된 객체가 `provideDelegate`를 멤버 또는 확장함수로 정의하면 해당 함수가 호출되어 프로퍼티의 위임자 인스턴스를 생성한다. 

`provideDelegate`의 가능한 사용 사례중 하나는 초기화시 프로퍼티의 일관성을 확인하는 것 이다. 

예를 들어 바인딩 하기 전에 프로퍼티 이름을 확인하려면 아래와 같이 작성할 수 있다. 

```kotlin
class ResourceDelegate<T> : ReadOnlyProperty<MyUI, T> {
    override fun getValue(thisRef: MyUI, property: KProperty<*>): T { ... }
}
    
class ResourceLoader<T>(id: ResourceID<T>) {
    operator fun provideDelegate(
            thisRef: MyUI,
            prop: KProperty<*>
    ): ReadOnlyProperty<MyUI, T> {
        checkProperty(thisRef, prop.name)
        // 위임자 생성 
        return ResourceDelegate()
    }

    private fun checkProperty(thisRef: MyUI, name: String) { ... }
}

class MyUI {
    fun <T> bindResource(id: ResourceID<T>): ResourceLoader<T> { ... }

    val image by bindResource(ResourceID.image_id)
    val text by bindResource(ResourceID.text_id)
}
```

`provideDelegate`의 매개변수는 `getValue()`의 매개변수와 동일 하다. 

- `thisRef` : 동동일하거나 프로퍼티 소유자의 상위 타입 이어야 한다. (확장 프로퍼티의 경우 확장중인 타입)
- `property` : `KProperty<*>`타입 또는 상위 타입 이어야 한다. 

`provideDelegate`메소드는 `MyUI`인스턴스를 만드는 동안 각 프로퍼티에 대해 호출되며, 필요한 유효성 검사를 즉시 수행한다. 

프로퍼티와 위임자간의 바인딩을 가로채는 이 기능이 없으면 동일한 기능을 수행하기 위해 프로퍼티의 이름을 명시적으로 전달해줘야 하는데, 이것은 편리하지 않은 기능이다. 

```kotlin
// 프로퍼티의 이름을 검사하는데 `provideDelegate`를 사용하지 않는 함수적인 방법 
class MyUI {
    val image by bindResource(ResourceID.image_id, "image")
    val text by bindResource(ResourceID.text_id, "text")
}

fun <T> MyUI.bindResource(
        id: ResourceID<T>,
        propertyName: String
): ReadOnlyProperty<MyUI, T> {
   checkProperty(this, propertyName)
   // 위임자 생성 
}
```

생성된 코드에서 `provideDelegate`메소드가 호출되어 보조 `prop$delegate`프로퍼티를 초기화 한다. 프로퍼티 선언 `val prop: Type by MyDelegate()`에 대해 생성된 코드를 위 생성된 코드(`providerDelegate`메소드가 없는 경우)와 비교 한다. 

```kotlin
class C {
    var prop: Type by MyDelegate()
}

// 아래코드는 컴파일러에 의해 생성된 코드이다. 
// `provideDelegate()`함수가 유효화 된 때는, 
class C {
    // `provideDelegate`를 위임자 프로퍼티로 생성하고 추가 했을 때 이다. 
    private val prop$delegate = MyDelegate().provideDelegate(this, this::prop)
    var prop: Type
        get() = prop$delegate.getValue(this, this::prop)
        set(value: Type) = prop$delegate.setValue(this, this::prop, value)
}
```

`provideDelegate()`메소드는 보조 프로퍼티 생성에만 영향을 미치며 getter와 setter에 대해 생성된 코드에는 영향을 주지 않는다. 

표준 라이브러리인 `PropertyDelegateProvider`인터페이스를 사용하면 새 클래스를 만들지않고도 위임 공급자를 만들 수 있다. 

```kotlin
val provider = PropertyDelegateProvider { thisRef: Any?, property ->
    ReadOnlyProperty<Any?, Int> {_, property -> 42 }
}

val delegate: Int by provider
```
