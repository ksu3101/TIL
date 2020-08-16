## Glide 5 - Transitions

Glide에 대한 숙련이 필요하다고 느낀 요즘, [Glide v4 Documentation](https://bumptech.github.io/glide/)를 참고 하여 내용을 번역하면서 필요한 내용들 위주로 정리 하였다.

추가로 Glide가 Java로 작성되어 있다 보니 Kotlin으로 적용 하면서 불편한점을 확장함수 등 으로 정리 해서 좀 더 직관적이고 관리하기 좋은 코드로 만들고 같이 정리 하려 함 :) 

- Transitions들을 일괄적으로 "전환"(효과)라고 표현 하였음 
  - Glide에서 말하는 Transition은 기존 이미지 A(placeholder 등 여러 가지 이미지)가 있을 때 새로 불러온 이미지 B에 대해 어떻게 전환할지(예로 crossfade와 같은 전환 애니메이션) 여부를 적용 하는 것 이다. 
- Request(이미지 리소스에 대한 요청)을 일괄적으로 "요청"으로 표현 하였음 

### 1. [Transitions](https://bumptech.github.io/glide/doc/transitions.html)

Glide에서 `Transitions`를 사용 하면 placeholder이미지로부터 불러올 이미지 리소스로의 이미지 전환을 정의 하여 적용할 수 있다. Transitions은 단일 요청의 컨텍스트로서 처리 되며 다른 요청들에게 전환 효과(예를 들면 크로스페이드와 같은 효과들)를 적용할 수 없다. 

### 2. 기본 전환 효과

Glide v3와는 달리 Glide v4에서는 기본적으로 크로스페이드 혹은 다른 전환이 적용되지 않는다. 현재의 v4에서는 전환을 수동으로 적용해 주어야 한다. 

### 3. 표준 전환 동작 

Glide는 사용자가 요청당 수동으로 적용할 수 있는 다양한 전환을 제공한다. Glide의 기본 제공되는 전환들은 일관된 방식으로 동작하게 되며 이미지가 최종적으로 불리워지는 위치에 따라 특정 상황에서는 실행되지 않을 수 도 있다. 

Glide에서 이미지를 불러오는 위치는 아래의 4개가 존재 한다. 

1. Glide에서 사용중인 메모리 캐시 
2. Glide에서 사용중인 디스크 캐시
3. 기기에 접근 가능한 리소스 파일 혹은 Uri
4. 외부 저장소의 Url또는 Uri 

기본적으로 Glide에서 제공하는 기본 전환들은 불러올 이미지리소스가 Glide의 메모리 캐시에서 가져오는 경우 실행 되지 않는다. 하지만, 불러올 이미지 리소스가 Glide의 디스크 캐시, 로컬 파일 또는 Uri, 원격 외부 저장소의 Uri 또는 Url에서 불러오는 경우에는 Glide에서 제공하는 기본 전환이 실행 된다. 

### 4. 사용자 지정 전환 동작 

`TransitionOptions`는 특정 요청들에 대한 전환 효과를 지정하는데 사용 된다. `TransitionOptions`는 `RequestBuilder`의 `transition()`메소드를 이용해 해당 요청의 전환 효과를 적용할 때 사용 된다. 비트맵 또는 드로어블 타입이 정의된 특정 전환은 `BitmapTransitionOptions`또는 `DrawableTransitionOptions`을 사용 하여 지정할 수 있다. 혹 다른 타입유형의 전환을 적용할 경우 `GenericTransitionOptions`를 사용 하면 된다. 

### 5. 퍼포먼스 향상 팁 

Android플랫폼 에서 애니메이션의 적용은 상당한 리소스를 사용하기에 값어치가 비싸다. 특히 한번에 많은 애니메이션을 시작해야 한다면 더 그럴 것 이다. cross fade와 같은 알파값의 변경과 같은 애니메이션은 많은 영향을 미칠수 있다. 또한, 애니메이션은 가끔 이미지를 디코딩하는데 걸리는 시간보다 애니메이션의 적용 과 실행이 더 오래 걸린다. 

`RecyclerView`와 같은 목록 뷰 에서 애니메이션을 각 아이템마다 적용 하면 이미지 로딩도 느려지고 스크롤이 버벅거리는 등 앱의 퍼포먼스에 악 영향을 미칠수 있다. 성능을 최대화하려면 Glide를 사용하여 `RecyclerView`에 이미지를 불러올때 애니메이션을 피하는것이 일단은 가장 좋다. 특히 이미지가 이미 캐시 되어 있거나 빠르게 불러와질 것 으로 예상되는 경우에 더욱 그러하다. 

그렇기 때문에 사용자가 목록 뷰 에서 스크롤을 할 때 이미지를 불러와야 한다면 미리 메모리캐시에 있도록 미리 로드하는것을 고려하는게 좋다. 

### 6. 일반적인 오류 

#### 6.1 placeholder이미지와 투명한 이미지를 대상으로 cross fading시 오류 

Glide에서 기본 제공되는 cross fade애니메이션은 `TransitionDrawable`을 사용 한다. `TransitionDrawable`은 `setCrossFadeEnabled()`메소드에 제공되는 boolean값에 따라 제어되는 2개의 애니메이션 모드를 제공 한다. 

1. cross fade가 비활성화 되어 있다면 전환 된 이미지가 이전에 보여지던 이미지 위에 fade in 되면서 보여지게 된다. 
2. cross fade가 활성화 되어 있다면 이전에 보여지던 이미지가 불투명한 상태에서 투명한 이미지로 애니메이션 되고, 전환되는 이미지가 투명상태에서 불투명상태로 애니메이션 된다. 

Glide에서는 일반적으로 더 나은 애니메이션들을 제공하기 떄문에 기본적으로 cross fade를 비 활성화 한다. 두 이미지의 알파가 한번에 변경되는 실제 cross fade애니메이션 에서는 두 이미지의 전환 사이에 흰색 플래시를 생성 한다. 

- 위 흰색 플래시라는 건 크로스페이드 애니메이션 할 때 볼 수 있는 것 인데 앞으로 보여지게 될 이미지에서 투명 -> 불투명이 되고, 기존 이미지가 불투명 -> 투명 상태로 전환하면서 중간에 두 이미지가 어중간하게 불투명한 상태가 된다. 이 때 background의 컬러가 그대로 노출이 되는 것을 말한다. (흰색 플래시라는 건 background가 white이기 때문 이다. 다른 background컬러를 적용 했다면 다른 색이 중간에 보인다. )
- 크로스페이드 시 background색이 전환효과에 적용될 두 이미지에 대해 어울리는 컬러라면 이상하지 않겠지만 어울리지 않는 색으로 되어 있을 경우 크로스페이드 자체가 이미지를 이상하게 전환시켜 어울리지 않는 어색한 모습을 보인다.

placeholder이미지가 불러올 이미지보다 크거나 이미지가 부분적으로 투명한 경우, cross fade를 비활성하면 애니메이션이 완료된 뒤 placeholder가 불러올 이미지 뒤에 보여지게 될 수 있다. 이 경우 `DrawableCrossFadeFactory`의 `Builder`를 통해 cross fade의 설정 여부를 `RequestBuilder`에 전달할 수 있으므로 필요시 cross fade의 적용 여부를 설정 해 주어야 한다. 

```kotlin
val factory = DrawableCrossFadeFactory.Builder()
            .setCrossFadeEnabled(false)
            .build()

Glide.with(context)
    .load("http://...")
    .transition(withCrossFade(factory))
    .diskCacheStrategy(DiskCacheStrategy.ALL)
    .placeHolder(R.drawable.placedholer)
    .into(imageView)
```

#### 6.2 요청간 cross fading

`Transitions`는 서로 다른 요청으로 불리어질 이미지들간에 cross fade를 기본적으로 허용하지 않는다. Glide는 기본적으로 새로운 이미지 요청을 처리 하면 기존에 존재한 요청(대기중일 수 있는)을 취소 한다. 그렇기 떄문에 다른 이미지를 불러오고 그 사이에 cross fade를 적용 할 수 없다. 

그렇다고 첫번째 이미지 요청이 완료될때까지 기다리거나 View에서 비트맵 또는 드로어블을 가져와 두번째 요청의 이미지와의 전환 효과를 수동으로 적용하는 것 은 안전하지 않으며 충돌이나 그래픽 손상이 발생할 수 있다. 

대신, 두개의 다른 요청에 불리어진 다른 이미지를 cross fading하는 방법은 두개의 `ImageView`가 포함된 `ViewSwitcher`를 사용 하는 것 이다. 
아니면 기본적으로 두개의 `ImageView`를 두고 각 요청을 달리 하여 처리 한뒤 각 뷰의 알파값을 조정하여 cross fade를 수동으로 조작하는 방법이 좋다. 

### 7. 사용자 정의 전환 효과

사용자 정의 전환효과를 정의 하려면, 

1. `TransitionFactory`를 구현한다
2. 사용자 정의 `TransitionFactory`를 `DrawableTransitionOptions.with()`를 통해 사용한다 

더 자세한 내용은 `DrawableCrossFadeFactory`을 참고 하면 된다. 

- 그래서 통째로 가져왔음. 
- 내부에 `Builder`는 위 "6.1"의 내용을 참고 할 것. 

```java
/**
 * A factory class that produces a new {@link Transition} that varies depending on whether or not
 * the drawable was loaded from the memory cache and whether or not the drawable is the first image
 * to be put on the target.
 *
 * <p>Resources are usually loaded from the memory cache just before the user can see the view, for
 * example when the user changes screens or scrolls back and forth in a list. In those cases the
 * user typically does not expect to see a transition. As a result, when the resource is loaded from
 * the memory cache this factory produces an {@link NoTransition}.
 */
// Public API.
public class DrawableCrossFadeFactory implements TransitionFactory<Drawable> {
  private final int duration;
  private final boolean isCrossFadeEnabled;
  private DrawableCrossFadeTransition resourceTransition;

  protected DrawableCrossFadeFactory(int duration, boolean isCrossFadeEnabled) {
    this.duration = duration;
    this.isCrossFadeEnabled = isCrossFadeEnabled;
  }

  @Override
  public Transition<Drawable> build(DataSource dataSource, boolean isFirstResource) {
    return dataSource == DataSource.MEMORY_CACHE
        ? NoTransition.<Drawable>get()
        : getResourceTransition();
  }

  private Transition<Drawable> getResourceTransition() {
    if (resourceTransition == null) {
      resourceTransition = new DrawableCrossFadeTransition(duration, isCrossFadeEnabled);
    }
    return resourceTransition;
  }

  /** A Builder for {@link DrawableCrossFadeFactory}. */
  @SuppressWarnings("unused")
  public static class Builder {
    private static final int DEFAULT_DURATION_MS = 300;
    private final int durationMillis;
    private boolean isCrossFadeEnabled;

    public Builder() {
      this(DEFAULT_DURATION_MS);
    }

    /** @param durationMillis The duration of the cross fade animation in milliseconds. */
    public Builder(int durationMillis) {
      this.durationMillis = durationMillis;
    }

    /**
     * Enables or disables animating the alpha of the {@link Drawable} the cross fade will animate
     * from.
     *
     * <p>Defaults to {@code false}.
     *
     * @param isCrossFadeEnabled If {@code true} the previous {@link Drawable}'s alpha will be
     *     animated from 100 to 0 while the new {@link Drawable}'s alpha is animated from 0 to 100.
     *     Otherwise the previous {@link Drawable}'s alpha will remain at 100 throughout the
     *     animation. See {@link
     *     android.graphics.drawable.TransitionDrawable#setCrossFadeEnabled(boolean)}
     */
    public Builder setCrossFadeEnabled(boolean isCrossFadeEnabled) {
      this.isCrossFadeEnabled = isCrossFadeEnabled;
      return this;
    }

    public DrawableCrossFadeFactory build() {
      return new DrawableCrossFadeFactory(durationMillis, isCrossFadeEnabled);
    }
  }
}
```
