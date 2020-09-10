## Lottie for Android - Advanced 2

Airbnb의 애니메이션 지운 라이브러리인 [Lottie](http://airbnb.io/lottie/#/README)에서 안드로이드 관련 문서를 번역하고 정리해보려 한다. 

1. [Lottie for Android - Lottie란 무엇인가](https://github.com/ksu3101/TIL/blob/master/Android/200904_android.md)
2. [Lottie for Android - Basic](https://github.com/ksu3101/TIL/blob/master/Android/200905_android.md)
3. [Lottie for Android - Advanced 1](https://github.com/ksu3101/TIL/blob/master/Android/200906_android.md)
4. [Lottie for Android - Advanced 2](https://github.com/ksu3101/TIL/blob/master/Android/200907_android.md)
5. [Lottie for Android - Advanced 3](https://github.com/ksu3101/TIL/blob/master/Android/200910_android.md)
6. [Lottie for Android - Advanced 4](https://github.com/ksu3101/TIL/blob/master/Android/200911_android.md)

### 1. 동적 프로퍼티

런타임중 프로퍼티를 동적으로 업데이트 할 수 있다. 동적으로 업데이트 가능한 프로퍼티의 경우 아래와 같은 다양한 목적으로 사용할 수 있다. 

- 테마. (낮, 밤-다크 테마등 과 같은 임의의 테마)
- 오류또는 성공과 같은 이벤트의 응답.
- 이벤트에 대한 응답으로 애니메이션의 한 부분. 
- 디자인 타임에 알려지지 않은 View또는 기타 값들에 대한 응답. 

#### 1.1 After Effects에 대해 이해하기 

Lottie에서 애니메이션 속성을 변경하는 방법에 대해 이해하려면 먼저 애니메이션 속성이 Lottie에 저장되는 방식을 이해해야 한다. 애니메이션 속성은 After Effects의 정보 계층 구조를 모방한 데이터 트리에 저장된다. After Effects에서 `Composition`은 각각 고유한 타임 라인이 있는 `Layer`의 모음이다. 

`Layer`개체는 문자열 이름이 있으며 해당 컨텐츠는 이미지, 모양 레이어들, 채워진 색 정보, 선 또는 그리기 위해 필요한 모든 항목이 될 수있다. After Effects의 각 개체에는 이름이 존재 한다. Lottie는 `KeyPath`를 이용하여 각 개체의 이름으로 이러한 개체와 속성을 찾을 수 있다. 

#### 1.2 사용 방법 

런타임중 프로퍼티들을 업데이트 하려면 아래 3개가 필요하다. 

1. `KeyPath`
2. `LottieProperty`
3. `LottieValueCallback`

#### 1.3 `Keypath`

`KeyPath`는 업데이트 될 특정 컨텐츠 또는 컨텐츠 집합을 대상으로 하는데 사용 된다. KeyPath는 원본 애니메이션의 After Effects컨텐츠 계층에 해당하는 문자열 목록으로 지정된다. 

KeyPath에는 컨텐츠의 특정 이름이나 와일드 카드가 포함될 수 있다. 

- 와일드 카드 `*`
  - 와일드 카드는 key path의 해당 위치에 있는 단일 컨텐츠 이름과 일치 한다. 
- 글로벌 스타 `**`
  - 글로벌 스타는 0개 이상의 레이어와 일치 한다. 

#### 1.4 `KeyPath`의 확인 

`KeyPath`에는 확인되는 컨텐츠에 대한 내부 참조를 저장할 수 있는 기능이 있다. 새로운 KeyPath객체를 만들면 확인하지 않는다. `LottieDrawable`및 `LottieAnimationView`에는 `KeyPath`를 사용하고 각각 단일 컨텐츠로 확인 되며 내부적으로 확인되는 0개 이상의 확인 된 KeyPath목록을 반환하는 `resolveKeyPath()`메소드가 있다. 

이처럼 하려면 개발 환경에서 `new KeyPath("**")`를 확인하고 반환된 목록을 기록(log)한다. 그러나 `ValueCallback`과 함께 `**`을 단독으로 사용하면 안된다. 이는 애니메이션의 모든 컨텐츠에 적용되기 때문이다. KeyPath를 확인하고 이후에 값에 대한 콜백을 추가하려면 해당 메소드에서 반환된 Keypaths를 사용 하면 된다. 내부적으로 확인되어진 컨텐츠를 찾기 위해 트리를 검색할 필요가 없기 때문이다. 

#### 1.5 `LottieProperty`

`LottieProperty`는 설정할 수 있는 프로퍼티의 열거형(enumeration) 이다. After Effects의 애니메이션 가능한 값에 해당 하며 사용 가능한 프로퍼티는 위에 나열되어 있으며 이는 `LottieProperty`의 문서에서 잘 정리되어 있다. 

#### 1.6 `ValueCallback`

`ValueCallback`은 애니메이션이 렌더링 될 때마다 호출된다. 

1. 혅재 키 프레임의 시작 프레임.
2. 현재 키 프레임의 끝 프레임. 
3. 현재 키 프레임의 시작 값. 
4. 현재 키 프레임의 끝 값. 
5. 시간 보간(Time interpolation)없이 현재 키 프레임에서 0부터 1까지의 진행. 
6. 시간 보간이 적용된 현재 키프레임의 진행. 
7. 전체 애니메이션의 0부터 1까지의 진행. 

생성자에서 올바른 유형의 단일 값을 얻고 항상 이를 반환하는 `LottieStaticValueCallback`과 같은 헬퍼 `ValueCallback`와 같은 하위 클래스도 있다. 

##### 1.6.1 `ValueCallback` 클래스 

- `LottieValueCallback` : 생성자에 정적 값을 설정하거나 `getValue()`메소드를 재정의 하여 모든 프레임에 값을 설정 한다. 
- `LottieRelativeTYPEValueCallback` : 생성자에서 정적 값을 설정하거나 `getOffset()`메소드를 재정의 하여 `taht`값을 설정하면 각 프레임의 실제 애니메이션 값에대한 오프셋으로 적용 된다. `TYPE`은 `LottieProperty`매개 변수와 동일한 유형이다. 
- `LottieInterpolatedTYPEValue` : 시작 값, 종료 값 및 선택적 보간(Interpolater)를 제공하여 값이 전체 애니메이션에서 자동으로 보간되도록 한다. `TYPE`은 `LottieProperty`매개 변수와 동일한 유형이다. 

#### 1.7 사용 방법 

```kotlin
  animationView.addValueCallback(
      KeyPath("Shape Layer", "Rectangle", "Fill"),
      LottieProperty.COLOR_FILTER) { Color.RED };
```

```kotlin
animationView.addValueCallback(
    KeyPath("Shape Layer", "Rectangle", "Fill"),
    LottieProperty.COLOR_FILTER) { frameInfo -> frameInfo.overallProgress < 0.5 ? Color.GREEN : Color.RED }
);
```

#### 1.8 애니메이션 프로퍼티들 

##### 1.8.1 `Transform`, 변환

- `TRANSFORM_ANCHOR_POINT`
- `TRANSFORM_POSITION`
- `TRANSFORM_OPACITY`
- `TRANSFORM_SCALE`
- `TRANSFORM_ROTATION`

##### 1.8.2 `Fill`, 채워진 색

- `COLOR` (그라디언트 아닌 컬러)
- `OPACITY`
- `COLOR_FILTER`

##### 1.8.3 `Stroke`, 선

- `COLOR` (그라디언트 아닌 컬러)
- `STROKE_WIDTH`
- `OPACITY`
- `COLOR_FILTER`

##### 1.8.4 `Ellipse`, 원

- `POSITION`
- `ELLIPSE_SIZE`

##### 1.8.5 `Polystar`, 많은 꼭지점을 갖는 별모양의 쉐이프

- `POLYSTAR_POINTS`
- `POLYSTAR_ROTATION`
- `POSITION`
- `POLYSTAR_OUTER_RADIUS`
- `POLYSTAR_OUTER_ROUNDEDNESS`
- `POLYSTAR_INNER_RADIUS` (star)
- `POLYSTAR_INNER_ROUNDEDNESS` (star)

##### 1.8.6 `Repeater`

- 모든 변환(transform) 가능한 객체
- `REPEATER_COPIES`
- `REPEATER_OFFSET`
- `TRANSFORM_ROTATION`
- `TRANSFORM_START_OPACITY`
- `TRANSFORM_END_OPACITY`

##### 1.8.7 `Layers`

- 모든 변환 가능한 객체
- `TIME_REMAP` (병합 레이어만 가능)

#### 1.9 주목할만한 프로퍼티들

##### 1.9.1 시간의 재 매핑

컴포지션 및 사전 구성(컴포지션 레이어)에는 시간을 다시 매핑할 수 있는 프로퍼티가 있다. 시간 재 매핑에 대한 값의 콟백을 설정하면 특정 레이어의 진행률을 제어할 수 있다. 그렇게 하려면 값의 콜백에서 원하는 시간 값을 초 단위로 반환하면 된다. 

##### 1.9.2 색상 필터

After Effects속성에 1:1매핑을 하지 않는 유일한 애니메이션 가능 프로퍼티는 채우기(Fill)내용에 설정할 수 있는 색상 필터 프로퍼티이다. 이는 레이어에 블렌드 모드(blend mod)를 설정하는 데 사용할 수 있다. 겹치는 내용이 아닌 해당 채우기의 색상에만 적용 된다. 

##### 1.10 `addColorFilter()`API에서의 마이그레이션

이전 API인 `addColorFilter()`를 이용하여 색상을 동적으로 변경한 경우 새로운 API로 마이그레이션 해야 한다. 이렇게 하려면 아래처럼 코드를 변경 하면 된다. 

```java
// 이전 코드 1
animationView.addColorFilter(colorFilter);

// 새로운 코드 1 
animationView.addValueCallback(
  new KeyPath("**"), 
  LottieProperty.COLOR_FILTER, 
  new LottieValueCallback<ColorFilter>(colorFilter)
);

// 이전 코드 2
animationView.addColorFilterToLayer("hello_layer", colorFilter);

// 새로운 코드 2
animationView.addValueCallback(
  new KeyPath("hello_layer", "**"), 
  LottieProperty.COLOR_FILTER, 
  new LottieValueCallback<ColorFilter>(colorFilter)
);

// 이전 코드 3
animationView.addColorFilterToContent("hello_layer", "hello", colorFilter);

// 새로운 코드 3
animationView.addValueCallback(
  new KeyPath("hello_layer", "**", "hello"), 
  LottieProperty.COLOR_FILTER, 
  new LottieValueCallback<ColorFilter>(colorFilter)
);

// 이전 코드 4
animationView.clearColorFilters();

// 새로운 코드 4
animationView.addValueCallback(
  new KeyPath("**"), 
  LottieProperty.COLOR_FILTER, 
  null
);
```