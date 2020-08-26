## LeakCanary 2 - Fixing a memory leak

이전 회사에서 [LeakCanary](https://github.com/square/leakcanary)을 사용해본 경험이 있었다. 당시에는 적용 대상 안드로이드 앱 에서 발생할 수 있는 메모리누수에 대해 감지하고 정보를 제공하는 정도록 알고 있었으며 그에 대해 깊이 공부하지는 않았었다. 

그래서 이번 기회에 [LeakCanary의 github.io문서](https://square.github.io/leakcanary/)를 기반으로 간단하게 정리해보려 한다. 

### 1. [Fixing a memory leak](https://square.github.io/leakcanary/fundamentals-fixing-a-memory-leak/)

메모리 누수는 어플리케이션이 더 이상 필요하지 않는 객체에 대한 참조를 해제 하지 않아 계속 유지되는 프로그래밍 오류이다. 이는 코드 어디인가에 지워지지 않은 참조가 유지 되고 있기 때문에 발생한다. 

메모리 누수를 고치기 위한 4단계는 아래와 같다. 

1. 메모리 누수에 대한 흔적을 찾는다. 
2. 의심가는 참조들에 대해 하나씩 검사하여 용의자를 찾아본다. 
3. 메모리 누수에 대한 직접적인 원인을 제공하는 참조를 찾는다. 
4. 코드를 수정하고 메모리 누수를 고친다. 

LeakCanary는 처음 2단계를 지원한다. 마지막 2단계는 개발자에게 달려 있다. 

### 2. 메모리 누수에 대한 흔적 찾기 

`leak trace`(누수 추적)는 GC(Garbage collection) Root 에서 유지된 객체 까지의 가장 강력한 참조 경로에 대하여 텍스트로 보여주는 것 이라고 생각하면 좋다. 즉, 객체를 메모리에 보유하고 있으므로 GC수집을 방지하는 참조 경로들을 leak trace로 보는 것이다. 

예를 들어 정적 필드에 존재하는 싱글톤 패턴의 헬퍼클래스를 보도록 하자. 

```java
class Helper {
}

class Utils {
  public static Helper helper = new Helper();
}
```

LeaCanary에게 싱글턴 인스턴스가 GC될 것으로 예상한다고 알린다.

```
AppWatcher.objectWatcher.watch(Utils.helper)
```

leak trace에서 해당 싱글턴인스턴스에 대한 누출 추적은 다음과 같다.

```
┬───
│ GC Root: Local variable in native code
│
├─ dalvik.system.PathClassLoader instance
│    ↓ PathClassLoader.runtimeInternalObjects
├─ java.lang.Object[] array
│    ↓ Object[].[43]
├─ com.example.Utils class
│    ↓ static Utils.helper
╰→ java.example.Helper
```

맨 위 `PathClassLoader`인스턴스는 GC루트, 특히 네이티브 코드의 로컬 변수에 의헤 보유된다. GC Root는 항상 연결할 수 있는 특수한 객체이다. 즉, GC할 수 없는 객체이다. GC루트에는 아래와 같은 4가지 주요 유형이 존재 한다. 

- 스테드 스택에 속하는 지역 변수. 
- 활성화된 자바 스레드의 인스턴스. 
- 절대 해제되지 않는 시스템 클래스. 
- 네이티브 코드로 제어되는 네이티브 참조. 

```
┬───
│ GC Root: Local variable in native code
│
├─ dalvik.system.PathClassLoader instance
```

`├─`으로 시작하는 행은 자바 객체(클래스, 객체 배열 혹은 인스턴스)를 나타내고 `│↓`으로 시작하는 행은 다음 행에서 자바 객체에 대한 참조를 나타낸다. 

`PathClassLoader`에는 `Object`배열에 대한 참조인 `runtimeInternalObjects`필드가 있다. 

```
├─ dalvik.system.PathClassLoader instance
│    ↓ PathClassLoader.runtimeInternalObjects
├─ java.lang.Object[] array
```

`Object`배열에서 43번째 위치에 있는 요소는 `Utils`클래스에 대한 참조 이다. 

```
├─ java.lang.Object[] array
│    ↓ Object[].[43]
├─ com.example.Utils class
```

`╰→`으로 시작하는 행은 메모리 누수 객체, 즉 `AppWatcher.objectWatcher.watch()`로 전달되는 객체를 나타낸다. 

`Utils`클래스에는 `Helper`싱글턴 인스턴스인 메모리 누수객체에 대한 참조로 정적 `Helper`필드가 있다. 

```
├─ com.example.Utils class
│    ↓ static Utils.helper
╰→ java.example.Helper instance
```

### 3. 의심가는 참조로부터 용의자 좁혀가기 

leak trace는 참조 경로 이다. 처음에는 해당 경로의 모든 참조가 메모리 누수를 유발하는것 으로 의심되지만 LeakCanary는 의심되는 참조를 자동으로 좁혀준다. 그 의미를 이해하기 위해 해당 프로세스를 수동을 살펴보자. 

다음은 잘못된 안드로이드 코드의 예시이다. 

- Application context에서 `Acitivty`에 add된 `View`의 인스턴스를 갖고있어 액티비티가 종료 된 후에도 View의 인스턴스 참조는 남아있어 메모리 누수의 원인이 된다. 

```java
class ExampleApplication : Application() {
  val leakedViews = mutableListOf<View>()
}

class MainActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main_activity)

    val textView = findViewById<View>(R.id.helper_text)

    val app = application as ExampleApplication
    // This creates a leak, What a Terrible Failure!
    app.leakedViews.add(textView)
  }
}
```

LeakCanary는 다음과 같은 leak trace를 생성한다. 

```
┬───
│ GC Root: System class
│
├─ android.provider.FontsContract class
│    ↓ static FontsContract.sContext
├─ com.example.leakcanary.ExampleApplication instance
│    ↓ ExampleApplication.leakedViews
├─ java.util.ArrayList instance
│    ↓ ArrayList.elementData
├─ java.lang.Object[] array
│    ↓ Object[].[0]
├─ android.widget.TextView instance
│    ↓ TextView.mContext
╰→ com.example.leakcanary.MainActivity instance
```

위 leak trace를 읽게 되면 아래처럼 읽을수 있다. 

> `FontsContract`클래스는 시스탬 클래스(GC Root : 시스템 클래스 참조)이며 배열을 참조하는 `ArrayList`인스턴스를 참조하는 `leakedViews`필드가 존재 하는 `ExampleApplication`인스턴스를 참조하는 `sContext`정적 멤버가 있다. `MainActivity`의 파괴된 인스턴스를 참조하는 `mContext`필드가 있는 `TextView`를 참조하는 요소가 있다. 

LeakCanary는 ~~~밑줄을 사용하여 메모리 누수를 일으키는 것 으로 의심되는 모든 참조를 강조하여 표시 한다. 

```
┬───
│ GC Root: System class
│
├─ android.provider.FontsContract class
│    ↓ static FontsContract.sContext
│                           ~~~~~~~~
├─ com.example.leakcanary.ExampleApplication instance
│    Leaking: NO (Application is a singleton)
│    ↓ ExampleApplication.leakedViews
│                         ~~~~~~~~~~~
├─ java.util.ArrayList instance
│    ↓ ArrayList.elementData
│                ~~~~~~~~~~~
├─ java.lang.Object[] array
│    ↓ Object[].[0]
│               ~~~
├─ android.widget.TextView instance
│    ↓ TextView.mContext
│               ~~~~~~~~
╰→ com.example.leakcanary.MainActivity instance
```

그리고, LeakCanary는 leak trace에서 객체의 상태 및 수명주기에 대해 추론한다. Android앱에서 `Application`인스턴스는 GC되지 않는 싱글턴이므로 메모리 누수가 발생하지 않는다. `Leaking: NO (Application is a singleton)`

그래서 LeakCanary는 메모리 누수가 `FontsContract.sContext`로 인한 것이 아니라는 결론을 내린다. 갱신된 leak trace는 아래와 같다. 

```
┬───
│ GC Root: System class
│
├─ android.provider.FontsContract class
│    ↓ static FontsContract.sContext
├─ com.example.leakcanary.ExampleApplication instance
│    Leaking: NO (Application is a singleton)
│    ↓ ExampleApplication.leakedViews
│                         ~~~~~~~~~~~
├─ java.util.ArrayList instance
│    ↓ ArrayList.elementData
│                ~~~~~~~~~~~
├─ java.lang.Object[] array
│    ↓ Object[].[0]
│               ~~~
├─ android.widget.TextView instance
│    ↓ TextView.mContext
│               ~~~~~~~~
╰→ com.example.leakcanary.MainActivity instance
```

`TextView`인스턴스는 `mContext`필드를 통해 파괴 된 `MainActivity`인스턴스를 참조 한다. `View`는 컨텍스트의 수명 주기내에서 유지되지 않아야 한다. 따라서 LeakCanary는 이 `TextView`의 인스턴스가 메모리 누수되고 있음을 알게 된다. 
`Leaking: YES (View.mContext references a destroyed activity)` 

따라서, 메모리 누수된 `TextView.mContext`에 대해 업데이트 된 leak trace는 아래와 같다. 

```
┬───
│ GC Root: System class
│
├─ android.provider.FontsContract class
│    ↓ static FontsContract.sContext
├─ com.example.leakcanary.ExampleApplication instance
│    Leaking: NO (Application is a singleton)
│    ↓ ExampleApplication.leakedViews
│                         ~~~~~~~~~~~
├─ java.util.ArrayList instance
│    ↓ ArrayList.elementData
│                ~~~~~~~~~~~
├─ java.lang.Object[] array
│    ↓ Object[].[0]
│               ~~~
├─ android.widget.TextView instance
│    Leaking: YES (View.mContext references a destroyed activity)
│    ↓ TextView.mContext
╰→ com.example.leakcanary.MainActivity instance
```

요약하면, LeakCanary는 leak trace의 객체 상태를 검사하여 이러한 객체들이 누수되는지 (누수 되었을 경우 YES 아닐경우 NO)알아 내고 해당 정보를 활용하여 의심스러운 참조들을 좁혀나간다. 사용자 정의 `ObjectInspector`을 구현하여 LeakCanary가 코드 베이스에서 작동하는 방식을 개선하거나 수정할 수도 있다. 

### 4. 메모리 누수를 발생시키는 원인인 참조를 찾기 

이전 예제에서 LeakCanary는 `ExampleApplication.leakedViews, ArrayList.elementData`및 `Object[].[0]`들에 대한 의심되는 참조들의 범위를 좁혔었다. 

```
┬───
│ GC Root: System class
│
├─ android.provider.FontsContract class
│    ↓ static FontsContract.sContext
├─ com.example.leakcanary.ExampleApplication instance
│    Leaking: NO (Application is a singleton)
│    ↓ ExampleApplication.leakedViews
│                         ~~~~~~~~~~~
├─ java.util.ArrayList instance
│    ↓ ArrayList.elementData
│                ~~~~~~~~~~~
├─ java.lang.Object[] array
│    ↓ Object[].[0]
│               ~~~
├─ android.widget.TextView instance
│    Leaking: YES (View.mContext references a destroyed activity)
│    ↓ TextView.mContext
╰→ com.example.leakcanary.MainActivity instance
```

`ArrayList.elementData`및 `Object[].[0]`은 `ArrayList`의 세부적인 구현 정보이며 `ArrayList`의 구현에 버그가 없을 가능성이 낮으므로 메모리 누수를 유발하는 참조가 남아있을 만한 후보는 유일한 참조인 `ExampleApplication.leakedViews`이다.

### 5. 메모리 누수 고치기 

메모리 누수의 원인이 되는 참조를 찾으면 해당 참조가 무엇에 대한 것 인지, 언제 지워졌어야 하는지, 왜 해결되지 않았는지 파악해야 한다. 때로는 정확한 원인을 찾아내기 위해 더 많은 정보가 필요할 것 이다. 그럴 경우 `hprof`파일을 직접 탐색 하여 leak trace너머 다른곳 혹은 깊은 곳을 파헤칠 수 있다. 