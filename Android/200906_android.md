## Lottie for Android - Advanced 1

Airbnb의 애니메이션 지운 라이브러리인 [Lottie](http://airbnb.io/lottie/#/README)에서 안드로이드 관련 문서를 번역하고 정리해보려 한다. 

1. [Lottie for Android - Lottie란 무엇인가](https://github.com/ksu3101/TIL/blob/master/Android/200904_android.md)
2. [Lottie for Android - Basic](https://github.com/ksu3101/TIL/blob/master/Android/200905_android.md)
3. Lottie for Android - Advanced 1

### 1. 애니메이션 리스너 

애니메이션을 제어하기 위해 리스너 콜백을 추가할 수 있다. 

```kotlin
animationView.addAnimatorUpdateListener((animation) -> {
    // Do something.
});
animationView.playAnimation();
...
if (animationView.isAnimating()) {
    // Do something.
}
...
animationView.setProgress(0.5f);
...
```

애니메이션의 상태 업데이트의 콜백 리스너에서 `animation.getAnimatedValue()`는 현재 설정된 최소 프레임/최대 프레임 [0 ,1]에 관계 없이 애니메이션의 진행률을 반환한다. 

`animation.getAnimatedFraction()`은 설정된 최소/최대 프레임(`minFrame, maxFrame`)을 고려하여 애니메이션의 진행률을 반환 한다.

#### 1.2. 사용자 정의 에니메이터

대부분 사용 사례에서는 `playAnimation()`메소드 콜 만으로 충분하지만 자체 애니메이터의 업데이트 콜백에서 `setProgress()`을 호출할 수 있다. 이것은 제스쳐, 다운로드 진행률 또는 스크롤 위치와 같은 것에 애니메이션을 연결하는데 유용할 수 있다. 

```java
// Custom animation speed or duration.
ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
animator.addUpdateListener(animation -> {
    animationView.setProgress(animation.getAnimatedValue());
});
animator.start();
```

### 2. 애니메이션의 반복 

Lottie는 `ValueAnimator`를 미러링 하는 고급 반복 기능을 지원한다. 따라서, `ValueAnimator`처럼 `setRepeatMode()`또는 `setRepeatCount()`등 메소드를 호출 할 수 있따. 혹은 xml에서 `lottie_loop="true"`프로퍼티를 사용하여 애니메이션의 반복을 활성화 설정 할 수 있다. 

`setMinFrame`, `setMaxFrame`또는 `setMinAndMaxFrame`을 사용하여 애니메이션의 특정 부분을 반복하게 할 수도 있다. 프레임의 진행율(0.0 ~ 1.0)또는 마커 이름(After Effects에서 지정)을 사용하는 여러버전이 존재 한다. 

### 3. 애니메이션의 크기 (px vs dp)

Lottie는 After Effects의 모든 px값을 장치의 dps로 변환하여 모든 장치가 동일한 크기로 렌더링 되도록 한다. 즉, After Effects에서 1920x1080의 애니메이션을 만드는 대신 After Effects에서 411x731px에 가까워야 한다. 이는 대부분 휴대폰의 dp화면 크기에 해당 한다. 

그러나 애니메이션이 완벽한 크기가 아닌 경우 두가지 옵션중 선택 할 수 있다. 

#### 3.1 `ImageView`의 `scaleType`설정

`LottieAnimationView`는 `ImageView`를 상속한 래핑된 뷰 이며, `centerCrop`과 `centerInside`를 지원하므로 둘 다 필요에 따라 맞추어 사용 하면 된다. 

#### 3.2 이미지 스케일의 Up/Down

`LottieAnimationView`와 `LottieDrawable`에는 모두 애니메이션을 수동으로 확대 또는 축소하는데 사용할 수 있는 `setScale(Float)` API를 제공 한다. 이는 거의 유용하지 않지만 특정 상황에 사용할 수 있다. 

그러나 `scaleType`과 함께 애니메이션을 축소 해 보면 Lottie가 프레임당 렌더링 해야 하는 양이 줄어든다. 이것은 큰 Mattes나 마스크가 있는 경우 특히 유용하다. 

더불어, 애니메이션이 느리게 진행되는 경우 [성능 문서](http://airbnb.io/lottie/#/android/performance)를 확인 하도록 하자. 
