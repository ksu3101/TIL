## LeakCanary 1 - Basic, How LeakCanary works

이전 회사에서 [LeakCanary](https://github.com/square/leakcanary)을 사용해본 경험이 있었다. 당시에는 적용 대상 안드로이드 앱 에서 발생할 수 있는 메모리누수에 대해 감지하고 정보를 제공하는 정도록 알고 있었으며 그에 대해 깊이 공부하지는 않았었다. 

그래서 이번 기회에 [LeakCanary의 github.io문서](https://square.github.io/leakcanary/)를 기반으로 간단하게 정리해보려 한다. 

- `Basic`에서는 LeakCanary의 기본 정보와 메모리 누수에 대해, 그리고 기본적인 작동원리에 대해서 정리 하였다. 
- `How LeakCanary works`에서는 LeakCanar에서 어떻게 동작하는지 정리 하였다. 

### 1. [LeakCanary Basic](https://square.github.io/leakcanary/)

우선 `LeakCanary`는 안드로이드에서 메모리누수를 감시하고 대상 메모리 누수들에 대한 정보를 제공해주는 안드로이드 라이브러리 이다. 

![leakcanary_1](./images/leakcanary_1.png)

LeakCanary는 메모리 누수에 대한 원인을 직접적으로 알려주지는 않지만 최대한 원인에 대하여 원인의 대상을 찾는대상을 좁힐 수 있는 정보들을 제공해 준다. 


#### 1.1 시작 하기

LeakCanary를 사용하려면 앱의 `build.gradle`파일에 아래와 같은 `leakcanary-android`라이브러리 의존을 추가하기만 하면 된다. 

```
dependencies {
  // 디버그용 빌드에만 적용 하기 위해 debugImplementation을 사용 한다. 
  debugImplementation 'com.squareup.leakcanary:leakcanary-android:2.4'
}
```

LeakCanary가 잘 설정되고 시작되었다면 Logcat을 통해 `LeakCanary`를 필터로 설정하면 아래와 같은 로그를 확인 할 수 있다. 

```
D LeakCanary: LeakCanary is running and ready to detect leaks
```

> LeakCanary는 설정된 앱 에서 자동으로 아래 인스턴스를 대상으로 누수를 감시한다. 
> - 사용 완료 되어 파괴된(destroyed) `Activity`의 인스턴스
> - 파괴된 `Fragment`의 인스턴스
> - 파괴된 `View`의 인스턴스
> - 사용 완료된(cleared) `ViewModel`의 인스턴스

### 2. 메모리 누수란? 

Java를 기반으로 한 런타임 환경에서 메모리 누수는 어플리케이션이 더 이상 필요하지 않은 객체들에 대한 참조가 계속 유지되어 발생하는 오류이다. 그로인하여, 해당 객체에 할당된 메모리를 회수할 수 없게 되므로 결국 OOM(Out of Memory)예외가 발생하게 된다. 

안드로이드를 예로 들면, `Activity`인스턴스는 `onDestroy()`콜백 메소드가 호출 된 후 더이상 필요하지 않으며 해당 인스턴스에 대한 참조를 정적 영역(Static field)에 저장하게 되버리면 가비지 콜렉터를 방해하게 되어 해당 인스턴스는 더이상 필요하지 않음에도 유지되어 메모리를 점유해 낭비 하게 된다. 

#### 2.1 메모리 누수의 일반적인 원인

대부분의 메모리 누수는 객체의 수명주기와 관련된 버그로 인하여 발생한다. 아래는 일반적인 실수들이다. 

- `Fragment.onDestroyView()`에서 `Fragment`의 View에 대한 참조를 지우지 않고 back stack에 `Fragment`의 인스턴스를 추가 한 경우. 
  - [이 Stackoverflow의 답변을 참고하면](https://stackoverflow.com/questions/59503689/could-navigation-arch-component-create-a-false-positive-memory-leak/59504797#59504797), `onDestroyView()`콜백 에서 `Fragment`클래스의 인스턴스 내 참조 하고 있는 View에 대한 객체들에 대해 null을 선언함으로서 더이상 사용하지 않음을 GC에 알려 GC가 원할하게 동작할 수 있도록 보장하게 하는 내용이다. 
- 설정 변경(orientation의 변경 등)으로 인하여 재생성 되는`Activity`의 기존 `Activity` 인스턴스를 `Context`에서 참조하고 있는경우. 
- 수명주기를 갖는 클래스 객체를 참조하는 콜백 리스너(Listener), 브로드 캐스트 수신자 또는 `RxJava`스트림의 구독을 등록한 뒤 수명주기가 끝낼따 까지 구독을 취소하는 작업들을 잊고 작업하지 않은 경우. 

#### 2.2 LeakCanary를 사용해야 하는 이유

안드로이드 어플리케이션에서 메모리 누수는 매우 흔하게 등장하며, 작은 메모리 누수들이 누적되면 앱의 메모리가 부족해져서 OOM예외가 발생한다. LeakCanary는 개발중인 앱에서 메모리 누수를 찾아 고치는데 도움이 된다. Squre사의 엔지니어가 Squre Point of Sale앱에서 LeakCanary를 처음 사용하였을때 메모리 누수를 잡고 OOM의 예외 발생률을 94%까지 줄일수 있었다고 한다.

### 3. LeakCanary의 작동 원리 

LeakCanary가 설치되면 다음 4단계로 메모리 누수를 자동으로 감지하고 보고한다. 

1. 유지된 객체가 감지 되면,
2. 메모리 힙(Heap)을 덤프 하고,
3. 덤프된 힙을 조사한 뒤,
4. 발견된 누수에 대해 분류를 나누고 정리 하고 알려준다. 

#### 3.1 유지된 객체의 감지 

LeakCanary는 안드로이드 수명주기에 연결되어 `Activity`나 `Fragment`들이 파괴되고 GC되어야 하는 시기를 자동으로 감지 한다. 파괴된 객체들은 파괴되어질 객체들에 대한 약한 참조를 갖고 있는 `ObjectWatcher`에게로 전달 된다. LeakCanary는 아래 객체들에 대한 누수를 자동으로 감지한다. 

- 사용 완료 되어 파괴된(destroyed) `Activity`의 인스턴스
- 파괴된 `Fragment`의 인스턴스
- 파괴된 `View`의 인스턴스
- 사용 완료된(cleared) `ViewModel`의 인스턴스

LeakCanary를 통해서 분리된(detached) `View`또는 `Presenter`와 같이 더이상 필요하지 않은 모든 객체들을 볼 수 있다. 

```kotlin
AppWatcher.objectWatcher.watch(myDetachedView, "View was detached")
```

`ObjectWatcher`에서 보유한 약한참조가 5초를 기다린 후 GC를 실행한 뒤 에도 여전히 지워지지 않았다면 감시된 객체가 "유지"된 것으로 간주되어 잠재적으로 누수가 발생하였음으로 간주 한다. LeakCanary는 이를 Logcat을 통해 알려준다. 

```
D LeakCanary: Watching instance of com.example.leakcanary.MainActivity
  (Activity received Activity#onDestroy() callback) 

... 5 seconds later ...

D LeakCanary: Scheduling check for retained objects because found new object
  retained
```

`LeakCanary`는 힙(heap)을 덤프하기 전에 보관된 객체의 수가 임계값에 도달 할 때까지 기다렸다가 최신 개수로 알림(안드로이드 노티피케이션과 Logcat)들을 통해 알려준다. 

![leakcanary_2](./images/leakcanary_2.png)

```
D LeakCanary: Rescheduling check for retained objects in 2000ms because found
  only 4 retained objects (< 5 while app visible)
```

> 기본 임계값은 앱이 화면에 보여지고 있을때 5개의 유지된 객체 이며, 보이지 않은 백그라운드 상태에서는 1개의 유지된 객체이다. 만약, 유지된 객체의 노티피케이션이 보여지고 있을때 앱을 백그라운드로 두면(예를 들어 Home버튼을 눌렀을 때) 임계값이 5 에서 1으로 변경되고 LeakCanary가 5초 이내에 힙을 덤프한다. 만약 등장한 노티피케이션을 터치 하면 LeakCanary는 즉시 힙을 덤프 하게 될 것이다. 

#### 3.2 힙 덤프

`ObjectWatcher`가 보유한 약한참조의 객체수가 임계값에 도달하면 LeakCanary는 Java의 메모리 힙을 안드로이드 파일 시스템의 `.hprof`파일로 덤프 한다. 

> LeakCanary의 힙 덤프 파일의 위치에 대한 자세한 정보는 [이 FAQ](https://square.github.io/leakcanary/faq/#where-does-leakcanary-store-heap-dumps)를 읽어보는 것을 추천 한다. 

힙을 덤프하면 짧은 시간 동안 앱이 정지되며, 그동안 LeakCanary는 아래와 같은 토스트를 표시한다. 

![leakcanary_3](./images/leakcanary_3.png)

#### 3.3 덤프된 힙의 조사 

LeakCanary는 [Shark](https://square.github.io/leakcanary/shark/)를 이용하여 `.hprof`파일을 파싱하여 유지된 객체를 찾는다. 

> Shark는 LeakCanary2 를 지원하는 힙 분석기로서 더 적은 메모리르 사용하고 더 빠른 Kotlin으로 작성된 독립형 힙 분석 라이브러리 이다. 

![leakcanary_4](./images/leakcanary_4.png)

유지된 각 객체에 대해 LeakCanary는 유지된 객체가 GC되는 것을 방지하는 참조 경로인 누수를 추적하기 위해 분석을 시작한다. 

![leakcanary_5](./images/leakcanary_5.png)

분석이 완료 되면 LeakCanary는 요약된 누수 정보와 함께 안드로이드 노티피케이션을 표시하고 결과를 Logcat에 출력 한다. LeakCanary는 각 추적된 누수에 대해 Signature(이하 서명)를 생성한다. 생성된 서명에 대해 동일한 서명을 갖는 누수, 즉 동일한 버그로 인한 메모리 누수를 그룹화 한다. 

![leakcanary_6](./images/leakcanary_6.png)

- 위 메모리 누수에 대한 요약된 정보가 보여지는 안드로이드 노티피케이션에서는 4개의 유지된 객체와 2개의 뚜렷한 메모리 누수에 대해 서명을 적용해 요약된 정보를 보여준다. 
- 아래 로그 에서는 위 이미지의 유지된 객체 및 누수에 대한 분석의 결과에 대하여 자세한 정보를 보여준다. 

```
====================================
HEAP ANALYSIS RESULT
====================================
2 APPLICATION LEAKS

Displaying only 1 leak trace out of 2 with the same signature
Signature: ce9dee3a1feb859fd3b3a9ff51e3ddfd8efbc6
┬───
│ GC Root: Local variable in native code
│
...
```

![leakcanary_7](./images/leakcanary_7.png)

표시된 LeakCanary의 노티피케이션을 터치 하거나 LeakCanary의 런처 아이콘(아래 이미지 참고)를 터치 하면 마지막으로 분석된 누수 정보들에 대해서 자세한 화면을 볼 수 있다. 

아래 LeakCanary의 화면에서 보여지고 있는 목록의 각 행은 같은 서명을 가진 누수 그룹을 뜻한다. LeakCanary는 앱에서 해당 서명으로 누수를 처음 분석하였을 경우 New로 태깅 하여 표시 한다. 

![leakcanary_8](./images/leakcanary_8.png)

분석된 누수에 대한 자세한 정보 화면을 보기 위해서 해당 서명의 누수그룹을 터치 하면 된다. 자세한 정보 호마ㅕㄴ에서는 유지된 객체와 분석된 누출에 대한 정보를 확인 할 수 있다. 

![leakcanary_9](./images/leakcanary_9.png)

서명된 누수는 메모리 누수를 유발하는 것 으로 의심되는 각 참조의 연결 해시(hash)이다. 각 의심되는 참조에 대해서는 빨간색 밑줄(아래 이미지 참고)로 표시 된다. 

![leakcanary_10](./images/leakcanary_10.png)

누수 추적이 텍스트(로그)로 보여질때에는 위와 동일한 의심스러운 참조들에 대하여 `~~~`밑줄이 표시 된다. 

```
...
│  
├─ com.example.leakcanary.LeakingSingleton class
│    Leaking: NO (a class is never leaking)
│    ↓ static LeakingSingleton.leakedViews
│                              ~~~~~~~~~~~
├─ java.util.ArrayList instance
│    Leaking: UNKNOWN
│    ↓ ArrayList.elementData
│                ~~~~~~~~~~~
├─ java.lang.Object[] array
│    Leaking: UNKNOWN
│    ↓ Object[].[0]
│               ~~~
├─ android.widget.TextView instance
│    Leaking: YES (View.mContext references a destroyed activity)
...
```

위의 예제들에서 추적된 누수의 서명은 아래처럼 내부 처리 되어 진다.

```kotlin
val leakSignature = sha1Hash(
    "com.example.leakcanary.LeakingSingleton.leakedView" +
    "java.util.ArrayList.elementData" +
    "java.lang.Object[].[x]"
)
println(leakSignature)
// dbfa277d7e5624792e8b60bc950cd164190a11aa
```

#### 3.4 발견된 누수의 분류 

LeakCanary는 앱에서 찾은 누수를 어플리케이션 누수와 라이브러리 누수 라는 두가지 범주로 구분한다. 라이브러리에서 발생한 누수는 제어할 수 없는 다른 외부 코드의 버그로 인하여 발생하는 누수 이다. 이 누수는 어플리케이션에 영향을 주지만 직접 해결이 어려우므로 LeakCanary에서 분리 하여 알려준다. 

두 범주는 Logcat의 출력에서 구분되어 보여진다. 

```
====================================
HEAP ANALYSIS RESULT
====================================
0 APPLICATION LEAKS

====================================
1 LIBRARY LEAK

...
┬───
│ GC Root: Local variable in native code
│
...
LeakCanary marks 
```

LeakCanary앱 에서 보여지는 라이브러리 누수는 아래처럼 `Library Leak`태그 되어 보여진다. 

![leakcanary_11](./images/leakcanary_11.png)

LeakCanary는 참조에 대한 패턴을 통해 인식하여 알려진 누수의 데이터베이스와 함께 제공된다. 예를 들면 아래처럼 보여진다. 

```
Leak pattern: instance field android.app.Activity$1#this$0
Description: Android Q added a new IRequestFinishCallback$Stub class [...]
┬───
│ GC Root: Global variable in native code
│
├─ android.app.Activity$1 instance
│    Leaking: UNKNOWN
│    Anonymous subclass of android.app.IRequestFinishCallback$Stub
│    ↓ Activity$1.this$0
│                 ~~~~~~
╰→ com.example.MainActivity instance
```

[AndroidReferenceMatchers](https://github.com/square/leakcanary/blob/main/shark-android/src/main/java/shark/AndroidReferenceMatchers.kt#L49)클래스에서 알려진 누수의 전체 목록을 확인 할 수 있다. 인식되지 않는 Android SDK의 누수를 발견하면 Squre사에 문의 하면 된다. 알려진 라이브러리 누수의 목록은 사용자 정의하여 추가 할 수도 있다. 

