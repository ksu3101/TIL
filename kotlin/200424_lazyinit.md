# lateinit, by lazy 

null 을 허용하지 않는 변수에서 초기화의 시점을 좀 더 늦게 할 수 있는 방법들에 대해서 디컴파일 코드와 함께 정리해 보았다. 

## `lateinit`

`lateinit` 은 null을 허용하지 않는 멤버의 수동적인 초기화의 키워드 이다. 

```kotlin
private lateinit var lateInitVar: String
```

위 처럼 `lateinit` 의 키워드가 적용 되는 클래스 멤버는 아래와 같은 규칙을 갖는다. 

- mutable 타입 인 `var` 에만 적용 할 수 있다. (참조 될 인스턴스를 계속 바꿀 수 있다)
- 초기화를 하기 전 접근 시 예외가 발생 한다. 
- getter, setter 정의 불가 한다 
- primitive type엔 적용 불가 한다
- `null` 으로 초기화를 불가 한다. 


```kotlin
class TestClazz {
    private lateinit var lateInitVar: String

    fun checkInstance():Boolean = ::lateInitVar.isInitialized
}
```

그렇다면 이 코드의 구현인 위의 예제를 디컴파일된 자바 코드로 볼 수 있다. (주석을 추가 하였음) 

```java
public final class TestClazz {
   // $FF: synthetic field
   static final KProperty[] $$delegatedProperties = new KProperty[]{(KProperty)Reflection.property1(new PropertyReference1Impl(Reflection.getOrCreateKotlinClass(TestClazz.class), "lazyInitVal", "getLazyInitVal()Ljava/lang/String;"))};
   private String lateInitVar;  // null 인스턴스로 초기화된 상태 

   private final String getLazyInitVal() {
      Lazy var1 = this.lazyInitVal$delegate;
      KProperty var3 = $$delegatedProperties[0];
      boolean var4 = false;
      return (String)var1.getValue();
   }

   public final boolean checkInstance() {
      return ((TestClazz)this).lateInitVar != null;   // 인스턴스의 초기화 여부는 null 체크로 대신하고 있다 
   }

   public TestClazz() {
      this.lazyInitVal$delegate = LazyKt.lazy((Function0)null.INSTANCE);
   }

   // $FF: synthetic method
   public static final String access$getLateInitVar$p(TestClazz $this) {
      String var10000 = $this.lateInitVar;
      if (var10000 == null) {
         // 자동 생성된 Getter 에서는 `lateInitVar`를 초기화 되지 않은 상태에서 접근시 예외를 뱉도록 하고 있다. 중요한 점은 이 때 인스턴스의 초기화 여부를 체크 하는 방법은 null check 이란 것 이다.
         Intrinsics.throwUninitializedPropertyAccessException("lateInitVar");
      }
      return var10000;
   }

   // $FF: synthetic method
   public static final void access$setLateInitVar$p(TestClazz $this, String var1) {
      $this.lateInitVar = var1;
   }
}
```

실제로 구현된 `lateinit var` 의 경우, 컴파일러는 이를 null type을 가질수 있는 일반 레페런스로 취급하고 있으며 초기화의 여부는 null 의 여부 이다. 

## `by lazy` block

`by lazy` 는 멤버의 초기화를 `lazy()` 에 `by` 키워드를 통해 위임(delegation) 함 으로서 늦은 초기화를 함수의 구현으로 반환되는 객체를 통해 하는 방식이다. 

```kotlin
class TestClazz {
    private val lazyInitVal: String  by lazy { "some text" }
}
```

위 예제 에서 `by lazy` 로 작성된 부분을 디컴파일 하기 전에 `lazy()` 함수에 대해서 정리하면 다음과 같다. 

```kotlin
public actual fun <T> lazy(initializer: () -> T): Lazy<T> = SynchronizedLazyImpl(initializer)

public actual fun <T> lazy(mode: LazyThreadSafetyMode, initializer: () -> T): Lazy<T> =
    when (mode) {
        LazyThreadSafetyMode.SYNCHRONIZED -> SynchronizedLazyImpl(initializer)
        LazyThreadSafetyMode.PUBLICATION -> SafePublicationLazyImpl(initializer)
        LazyThreadSafetyMode.NONE -> UnsafeLazyImpl(initializer)
    }
    
public actual fun <T> lazy(lock: Any?, initializer: () -> T): Lazy<T> = SynchronizedLazyImpl(initializer, lock)
```

3개의 함수들을 제공 하고 있는데, 동일한 점은 `lazy`의 람다에 의해 구현될 객체가 스레드동기화 된 상태에서 초기화 됨을 알 수 있다. 첫번째를 제외한 두번쨰, 세번째 함수들은 각각 스레드 세이프티를 제공하기 위한 락 객체를 따로 잡아서 초기화 할지, 혹은 스레드 세이프티 모드를 끄고 인스턴스를 초기화 할 지 여부를 설정할 수 있다. 

이제 예제를 디컴파일하여 상세를 보도록 하자. 

```java
public final class TestClazz {
   // $FF: synthetic field
   static final KProperty[] $$delegatedProperties = new KProperty[]{(KProperty)Reflection.property1(new PropertyReference1Impl(Reflection.getOrCreateKotlinClass(TestClazz.class), "lazyInitVal", "getLazyInitVal()Ljava/lang/String;"))};
   private final Lazy lazyInitVal$delegate;

   private final String getLazyInitVal() {
      Lazy var1 = this.lazyInitVal$delegate;
      KProperty var3 = $$delegatedProperties[0];
      boolean var4 = false;
      return (String)var1.getValue();
   }

   public TestClazz() {
      this.lazyInitVal$delegate = LazyKt.lazy((Function0)null.INSTANCE);
   }
}
```

delegation 될 프로퍼티인 `lazyInitVal` 의 델리게이션 객체를 array에 저장하고 getter 에서 해당 array에서 가져와 얻음을 알 수 있다. 

`lazy()` 에 대한 정리는 다음과 같다. 

- immutable `val` 에서만 사용 가능
- `by lazy()` 블럭에서 초기화 가능
- `lazy()` 함수 에 여러가지 옵션을 사용 할 수 있음
  - `SYNCHRONIZED, PUBLICATION, NONE`
- 내부적으로 동기화되어 thread safe 하다. 
