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
1. JUnit과 Espresso의 설정
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
    testCompile 'junit:junit:4.12'
}
```
Android Studio 2.2와 gradle 2.2환경에서 프로젝트를 만들면 JUnit 모듈과 Espresso모듈이 이미 추가되어 있다. `defaultConfig`속성 내부에 `testInstrumentationRunner`속성과 `dependencies`속성의 `espresso-core`가 추가된것을 확인 해 볼 수 있다.

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
- `initTest()` : `@Before`는 테스트 클래스가 만들어지고 난 뒤 각 단위 테스트를 하기 전에 가장 먼저 한번 실행되는 메소드 이다.  
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
  
## 5. 실제와 비슷한 단위 테스트용 클래스 만들기   
작성중..   

## 6. 실제처럼 테스트 해 보기 
작성중..  



