# JUnit과 Espresso를 이용한 TDD 개발 

## 1. 배경
 **테스트 주도 개발 프로세스(TDD)**는 최근 개발 트렌드에서 자동화된 테스트, 컴파일-빌드, 배포 단위에서  중요한 역할을 하고 있다. 이미 TDD 개발 프로세스를 적용한 개발팀들도 많은 편 이다. 특히 민첩한 대응을 요구하는 Agile이나 DevOps환경에서 자동화 도구들은 필수라고 할 수 있다.  

 TDD는 국내에선 아직 완전히 흡수된게 아니다. 아무래도 개발보다 테스트를 먼저 작성한 다는것에 시간낭비하는거 아니냐는 말도 많고 거부감도 적지 않은 편 이니까. 나 또한 이런 개발 방법론을 들었을때 이해가 잘 되지 않았다. 보통 이렇게 잘 와닿지 않는 개발의 이야기는 간단한 방법으로 그 결과를 확인 해 볼 수 있다. 바로 직접 비슷한 환경을 조성하고 개발을 해 본 뒤에 내가 느낀 피드백을 대상으로 고민 해 본 것 이라고 할 수 있겠다. 

 그래서 이번엔 Junit을 이용하여 단위 테스트 코드를 포함한 안드로이드 테스트 앱을 만들어 볼 생각이다. 또한 TDD에 잇점을 가지고 있다는 MVP패턴을 적용 하고 Rx를 적용 할 생각이다. Dummy 테스트 코드를 만들어 가상의 데이터를 콜백 받아서 처리하는 Presenter내부에서 테스트를 할 수 있는 환경을 조성 하고 실제로 정상적인 데이터와 문제가 발생할 가능성이 높은 데이터를 던져 볼 것이다.   

### 1.1 개발 환경
1. Windows 10
2. Android studio 2.2 + gradle 2.2 
3. JUnit 4.12 + Espresso 2.2.2  
4. RxAndroid 1.2.1

### 1.2 목표 
1. JUnit과 [Espresso의 설정](https://developer.android.com/training/testing/start/index.html#config-instrumented-tests)
 1. Espresso : UI 테스팅 모듈. 앱에서 추가한 UI의 기능에 대한 테스트에 적합 하다. 사용자의 UX(클릭, 스와이프 등)에 따른 뷰의 변화도 테스트 할 수 있다. (일명 Coded UI Test)    
2. 테스트 코드의 작성 
3. 실제로 테스트 코드를 실행하여 케이스별 대응 
- 참고 : [TDD 개발 프로세스](https://github.com/ksu3101/TIL/blob/master/ETC/160717_TDD.md)
- 롤리팝 이상 버전을 대상으로 글을 작성 하였다. 미만 버전에서 무슨 문제가 발생 할 지 모른다..   

## 2. JUnit설정을 위한 프로젝트 `build.gradle`
```gradle
apply plugin: 'com.android.application'

android {
    compileSdkVersion 24
    buildToolsVersion "24.0.0"
    defaultConfig {
        applicationId "kr.swkang.testdrivendev"
        minSdkVersion 15
        targetSdkVersion 24
        versionCode 1
        versionName "1.0"
        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
}

dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    androidTestCompile('com.android.support.test.espresso:espresso-core:2.2.2', {
        exclude group: 'com.android.support', module: 'support-annotations'
    })
    compile 'com.android.support:appcompat-v7:24.2.1'
    compile 'io.reactivex:rxandroid:1.2.1'
    compile 'com.android.support.test.espresso:espresso-idling-resource:2.2.2'
    testCompile 'junit:junit:4.12'
}
```
Android Studio 2.2와 gradle 2.2환경에서 프로젝트를 만들면 JUnit 모듈과 Espresso모듈이 이미 추가되어 있다. `defaultConfig`속성 내부에 `testInstrumentationRunner`속성과 `dependencies`속성의 `espresso-core`가 추가된것을 확인 해 볼 수 있다.    
`espresso-idling-resource`는 추후에 설명할 비동기 작업과 연관된 것 이다. 

Espresso모듈이 추가 되어 있지 않은 경우 아래와 같이 추가 해 준다.  
```gradle
dependencies {
  ...
  androidTestCompile 'com.android.support.test.espresso:espresso-core:2.2.1'
}
```
**그리고 안드로이드 OS에서 제공하는 애니메이션의 사용 설정을 끈 다**. UI테스트의 경우 애니메이션으로 인하 즉각적인 UI의 변화를 업데이트/감지 하지 못하여 테스트 실패가 될 수 있다고 한다. `개발자 옵션`에서 다음 애니메이션 항목의 사용 설정을 끄면 된다. 
- `Window animation scale`
- `Transition animation scale`
- `Animatior duration scale`

## 3. 간단한 단위 테스트용 클래스 만들기 
생성된 안드로이드 프로젝트 내부에 테스트 패키지가 만들어 졌을 것 이다. 보통 이 내부에 테스트용 클래스를 정의하고 테스트 시나리오를 작성 하면 된다. 

시간이 없으니 앱 메인 패키지 아래에 아래와 같안 간단한 사칙연산을 수행하는 클래스인 `Calculator`를 만들었다. 이 클래스의 기능들이 정상적으로 작동 하는지 테스트를 해 볼 것이다.   
```java
public class Calculator {
  private int a;
  private int b;

  public Calculator() {
    this.a = 0;
    this.b = 0;
  }

  public int add(int a, int b) {
    this.a = a;
    this.b = b;
    return a + b;
  }

  public int minus(int a, int b) {
    this.a = a;
    this.b = b;
    return a - b;
  }

  public boolean isEqualNumber() {
    return a == b;
  }

  public int getA() {
    return a;
  }

  public void setA(int a) {
    this.a = a;
  }

  public int getB() {
    return b;
  }

  public void setB(int b) {
    this.b = b;
  }
}
```

## 4. 간단한 단위 테스트 해 보기
```java
@RunWith(AndroidJUnit4.class)
@SmallTest
public class TestCalculator {
  private Calculator calculator;

  @Before
  public void initTest() {
    this.calculator = new Calculator();
  }

  @Test
  public void testAddNumbers() {
    int result = calculator.add(10, 20);
    assertThat(result, is(30));
  }

  @Test
  public void testMinusNumbers() {
    int result = calculator.minus(25, 10);
    assertThat(result, is(15));
  }
}
```
`Calculator` 클래스를 이용 해서 테스트 할 내용은 아래와 같다.   
- `TestCalculator` 클래스 : 테스트들을 정의한 클래스. `@RunWith`어노테이션을 이용하여 `AndroidJUnit4`라이브러리를 사용 하고, `@Test`어노테이션으로 단위 테스트 메소드들을 정의 했다. 
- `initTest()` : `@Before`는 테스트 클래스가 만들어지고 난 뒤 각 단위 테스트를 하기 전에 가장 먼저 한번 실행되는 메소드 이다.  테스트 후 어떠한 작업을 하고 싶다면 `@After`를 사용 할 수 있다. 
- `testAddNumbers()` : 어떠한 값 `a`와 `b`를 더하고 그 결과가 맞는지 확인 한다.  
- `testMinusNumbers()` : 어떠한 값 `a`와 `b`를 뺀 뒤 그 결과가 맞는지 확인 한다.  

### 4.1  테스트 빌드 설정 하기
1.  좌측 최 상단의 '+' 버튼을 누른다.   
![1](https://github.com/ksu3101/TIL/blob/master/Android/images/0922tdd_01.png)  
   
2. '+'버튼을 누르면 여러 메뉴들이 보이는데 그 중 `Android Test`를 눌러 안드로이드 테스트를 추가 한다.    
![2](https://github.com/ksu3101/TIL/blob/master/Android/images/0922tdd_02.png)  
   
3. 추가된 테스트의 정보를 수정 한다.    
![3](https://github.com/ksu3101/TIL/blob/master/Android/images/0922tdd_03.png)  
 1. `Name` : 테스트들을 구분하기 위한 이름   
 2. `Test - Class` : 개발자가 만든 테스트 클래스들을 명시한다. 지금은 `TestCalculator`을 테스트 할 것이다.  
 3. `runner` : Junit Runner로 설정 한다.     
    
4. 제대로 설정 됬 다면 위 Selector란에 만든 테스트가 추가 될 것 이다. 테스트용 디바이스나 에뮬레이터를 연결 하고 빌드 하면 테스트를 진행 하게 된다.   
![4](https://github.com/ksu3101/TIL/blob/master/Android/images/0922tdd_04.png)  
      
### 4.1 테스트 도중 오류가 발생했을 경우   
 ![4.1](https://github.com/ksu3101/TIL/blob/master/Android/images/0922tdd_05.png)
- `testMinusNumbers()`메소드에서 잘못된 값을 반환하여 `AssertionError`예외가 발생했음을 알려 준다.  

### 4.2 정상적으로 테스트를 완료 했을 경우    
 ![4.1](https://github.com/ksu3101/TIL/blob/master/Android/images/0922tdd_06.png)
- 정상적으로 테스트 완료 후 빌드 되었음을 알려 준다. 
     
## 5. 실제처럼 단위 테스트용 클래스 만들기  
테스트 케이스 작성 방법은 다음과 같다.    
### 5.1  `ActivityTestRule`을 이용한 테스트 케이스 작성 법      
구글에서 권장하고 있는 테스트 케이스 작성 법 이다. `ActivityTestRule`이라는 보일러플레이트 코드를 기반으로 작성 한다. 기반 코드는 아래와 같은 형식으로 UI테스트를 진행 한다.   
  
![Espresso 2.0](http://i.stack.imgur.com/SKODS.png)      
```java
onView(withId(R.id.some_vew))            // Matchers
    .perform(click())                             // View Actions 
    .check(matches(isDisplayed()));     // View Assertions
```  
- `onView()` : 리소스 id로 명시된 특정 뷰를 찾는다. 
- `perform()` : 찾은 특정 뷰에 어떠한 액션을 한다. 액션에 대해선 하단의 *6.2*를 참고 할 것
- `check()` : 상태를 체크 한다. 보여지고 있는지 입력된 텍스트는 어떠한지 등을 체크 한다.
이 방법을 이용하여 테스트 코드를 작성하면 다음과 같을 것 이다. 

#### 5.1.1 비동기작업과 테스트 코드
네트워크나 I/O등 비동기 작업을 동반한 테스트를 해야 할 때가 있다. 네트워크 API를 비동기로 작업 후 다른 스레드를 통해 데이터를 가져와 파싱한 데이터를 기반으로 뷰를 메인스레드에서 업데이트 하는 등의 작업이다. 
[참고](https://github.com/googlesamples/android-testing/tree/master/ui/espresso/IdlingResourceSample)   

### 5.2 `ActivityInstrumentationTestCase2`을 이용한 테스트 케이스 작성 법 
API 24 이후로 이 방법은 **deprecated**상태 이다. 구글에서는 현재 이 테스트케이스를 권장 하지 않는다고 한다.  
> Note: For new UI tests, we strongly recommend that you write your test in the JUnit 4 style and use the ActivityTestRule class, instead of ActivityInstrumentationTestCase2.    
  
방법은 `ActivityInstrumentationTestCase2`를 상속한 클래스 내부에서 테스트 케이스를 구현하는 방법이다. 이 방법은 deprecated되었으므로 따로 작성하지는 않았다.   
  
## 6. 실제처럼 테스트 해 보기 

### 6.1 테스트 코드의 작성  
```java
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressKey;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestMainActivity {
  
  @Rule
  public ActivityTestRule<MainActivity> activityTestRule
      = new ActivityTestRule<MainActivity>(MainActivity.class);

  private IdlingResource idlingResource;

  @Before
  public void initTest() {
    idlingResource = activityTestRule.getActivity().getIdlingResource();
    Espresso.registerIdlingResources(idlingResource);
  }

  @Test
  public void testTextView() {
    onView(withId(R.id.main_textview))
        .perform(click())
        .check(matches(isDisplayed()));

    onView(withId(R.id.main_textview))
        .check(matches(withText("KANG")));

    onView(withId(R.id.main_edittext))
        .perform(
            click(),
            //pressKey(KeyEvent.KEYCODE_LANGUAGE_SWITCH),  // 안됨
            //pressKey(new EspressoKey.Builder().withKeyCode(KeyEvent.KEYCODE_SPACE).withShiftPressed(true).build()), // 이것도 안됨.. 
            typeText("rkdtjddn"),
            closeSoftKeyboard()
        )
        .check(matches(withText("강성우")));
  }

  @After
  public void unregisterIdlingResources() {
    if (idlingResource != null) {
      Espresso.unregisterIdlingResources(idlingResource);
    }
  }
}
```
- espresso의 static메소드들을 사용해서 UI테스트를 진행하는 것 을 볼 수 있다. 
- 첫번째 테스트는 텍스트뷰를 클릭(기능 없음) 하고 현재 보여지고 있는지 여부를 체크 한다.
- 두번째 테스트는 텍스트뷰에 입력되어진 텍스트가 현재 "KANG"인지 여부를 체크 한다. 
- 세번째 테스트는 에디트 텍스트를 클릭 하고 난 뒤 키보드를 바꾸고 "강성우"를 입력 한다. 그리고 입력한 결과가 "강성우"라고 제대로 입력되었는지 여부를 확인 한다.  
- 테스트 당시 사용하던 기기는 `LG G2`였는데, 제공하는 OEM 기본 소프트 키보드에서는 **한글 키보드**가 먼저 보여진다. 그래서 영문입력의 테스트를 위해서는 키보드를 바꿔 줄 필요가 있다. 
 1. `pressKey()`메소드와  `KeyEvent.KEYCODE_LANGUAGE_SWITCH`를 이용한 스위칭 : *안됨*   
 2. `pressKey()`메소드와 Espresso에서 제공하는 키 입력을 이용한 `Shift`키와 `Space`키 동시 입력을 이용한 스위칭 : *안됨*  
 3. 키보드 팝업시 처음 보여지는 키보드를 변경 한다. 
- 위 방법중 1번과 2번이 왜 안되는지는 모르겠다. 아마 OEM키보드 라서 그런거 일지도.. 
  
### 6.2 비동기 작업에 대한 View 변화에 대한 테스트    
위 테스트 중에서 두번째의 경우 `MainActivity`에서 `MainActivityPresenter`를 통해서 Rx이용하여 어떠한 비동기 상황을 가정 하고 아래처럼 구성 했다.   
```java
public class MainActivityPresenter
    extends BasePresenter {
  private static final String TAG = MainActivityPresenter.class.getSimpleName();

  private MainActivityPresenter.View view;
  private SimpleIdlingResource       idlingResource;

  public MainActivityPresenter(@NonNull MainActivityPresenter.View activity) {
    this.view = activity;
    if (activity instanceof MainActivity) {
      idlingResource = ((MainActivity) activity).getIdlingResource();
    }
  }

  public void getTextDatas() {
    Observable observable = Observable.create(
        new Observable.OnSubscribe<String>() {
          @Override
          public void call(Subscriber<? super String> subscriber) {
            try {
              Thread.sleep(1000); // DUMMY VALUE
            } catch (InterruptedException ie) {
              subscriber.onError(ie);
            } finally {
              subscriber.onNext("KANG");
            }
          }
        }
    ).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread());
    observable.subscribe(
        new Subscriber<String>() {
          @Override
          public void onCompleted() {
          }

          @Override
          public void onError(Throwable e) {
            idlingResource.setIdleState(true);
            view.onError(TAG, e != null ? e.getMessage() : "ERROR");
          }

          @Override
          public void onNext(String result) {
            idlingResource.setIdleState(true);
            view.updateTextView(result);
          }
        }
    );
    idlingResource.setIdleState(false);
  }

  public interface View
      extends BaseView {
    void updateTextView(String message);
  }
}
```
1초간 잠시 대기 했다가 "KANG"라는 텍스트를 서브스크라이버에게 전달하는 간단한 Rx구현체 이다. 네트워크나 파일 등 I/O상황이 많아지면 이러한 비동기 작업에 대한 테스트를 어떻게 하는지 궁금 하다.  
비동기 작업에 대한 UI테스트는 `IdlingResource`인터페이스를 구현하여 설정한다. 아래 클래스는 그 예 이다. 
```java
public class SimpleIdlingResource
    implements IdlingResource {
  private volatile ResourceCallback resourceCallback;
  private AtomicBoolean isIdleNow = new AtomicBoolean();

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public boolean isIdleNow() {
    return isIdleNow.get();
  }

  @Override
  public void registerIdleTransitionCallback(ResourceCallback callback) {
    this.resourceCallback = callback;
  }

  public void setIdleState(boolean isIdleNow) {
    this.isIdleNow.set(isIdleNow);
    if (isIdleNow && resourceCallback != null) {
      resourceCallback.onTransitionToIdle();
    }
  }
}
```
등록 되어진 `IdlingResource`을 구현한 클래스는 여러개의 스레드에서 동시에 접근 할 수 있다. 비동기 상황에 리소스에 대한 접근에 대해서 스레드 세이프 하게 동작하게 하기 위해서 접근 하기 위한 `ResourceCallback`과 boolean값인 현재 상태의 플래그등의 오브젝트들에 읽기/쓰기 대해 *원자성*을 보장 해 줘야 한다.   

외부 클래스(아까의 `MainActivityPresenter`가 그 예)에서는 `setIdleState()`메소드를 통해서 테스트의 Idling상태를 설정 한다. 만약 idling상태가 되면 콜백을 통해서 state를 변경 하기 전까지 대기 한다.  

이렇게 구현되어진 `SimpleIdlingResource`을 `MainActivityPresenter`에서 멤버 변수로 두고 `setIdleState()`메소드를 적시 적소에 호출 하면서 테스트의 대기, 진행을 설정 할 수 있다. 
그 전에 테스트를 구현한 클래스에서 `@Before`어노테이션을 이용하여 `SimpleIdlingResource`을 register하고 `@After`어노테이션에서 un-register 하는것을 잊지 말자.     
   
### 6.2 Espresso에서 제공하는 안드로이드 액션 들 
`ViewInteraction.perform()`나  `DataInteraction.perform()`메소드를 호출 하여 UI요소들의 테스트를 할 수 있다. 테스트 할 수 있는 사용자가 시나리오 내 에서 할 것이라고 예측 가능한 일반적인 액션 들은 아래와 같다.    
- `viewActions.click()`, `doubleClick()`, `longClick()`
- `pressBack()`, `pressImeActionButton()`, `pressKey(keycode)`, `pressMenuKey()`
- `viewActions.typeText()` : 어떤 뷰를 클릭 하고 설정한 텍스트를 입력 하게 한다. 
- `typeTextIntoFocusedView()`, `replaceText()` 
- `viewActions.scrollTo()` : 어떤 뷰를 스크롤 한다. 테스트 할 대상 뷰는 `ScrollView`와 같이 스크롤이 구현되어진 뷰 로서 `android:visibility`속성이 `VISIBLE`상태 이어야 한다. 참고로 보여질 뷰가 `AdapterView`등을 상속한 뷰(`ListView`나 `RecyclerView`등)라면 `onData()`메소드 등을 통해서 스크롤을 할 수 있을 것 이다. 
- `swipeLeft()`, `swipeRight()`, `swipeUp()`, `swipeDown()`
- `viewActions.clearText()` :  어떤 뷰에 입력된 텍스트르 모두 제거 한다. 




