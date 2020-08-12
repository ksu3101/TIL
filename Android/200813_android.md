## Glide 3 - Transformations 

Glide에 대한 숙련이 필요하다고 느낀 요즘, [Glide v4 Documentation](https://bumptech.github.io/glide/)를 참고 하여 내용을 번역하면서 필요한 내용들 위주로 정리 하였다.

추가로 Glide가 Java로 작성되어 있다 보니 Kotlin으로 적용 하면서 불편한점을 확장함수 등 으로 정리 해서 좀 더 직관적이고 관리하기 좋은 코드로 만들고 같이 정리 하려 함 :) 

- 이번 문서에서는 `Transformations`을 다루고 있는데 이를 "변환"이라고도 작성 하였음. 

### 1. [Transformations](https://bumptech.github.io/glide/doc/transformations.html)

Glide에서 Transformations는 가져온 이미지리소스를 변환시킨 다른 리소스로 반환한다. 일반적으로 비트맵을 대상으로 crop과 같은 일부를 자르는 작업이나 필터등을 적용, 애니메이션 GIF 와 같은 것 들을 말하곤 한다. 

Glide에서는 기본적으로 아래와 같은 기본 Transformations를 제공 한다. 

- CenterCrop
- FitCenter
- CircleCrop

Transformations를 적용 하려면 `RequestOptions`를 이용하면 된다. 

```kotlin
Glide.with(context)
    .load("http://...")
    .fitCenter()
    .into(imageView)
```

아래처럼 `RequestOptions`을 `apply()`하여 공유해 사용할 수도 있다. 

```kotlin
val opt: RequestOptions = RequestOptions().centerCrop()

Glide.with(context)
    .load("https://...")
    .apply(opt)
    .into(imageView)
```

### 2. 여러개의 Trnasformations적용 

기본적으로 `transform()`메소드를 이용하여 특정 여러개의 Transformations를 적용 할 수 있다. 여러개의 Transformations를 적용 하기 위해서는 `MultiTransformation`을 이용해 각 Transformations클래스의 인스턴스를 래핑 해 주면 된다. 

```kotlin
Glide.with(context)
    .load("http://...")
    .transform(MultiTransformation(FitCenter(), MyCustomTransformation()))
    .into(imageView)
```

`MultiTransformation`클래스를 사용하지 않고 적용할 Transformations들 만을 작성해도 된다. `transform()`메소드 중 `varargs`를 받는 메소드가 있기 때문이다. 

```kotlin
Glide.with(context)
    .load("http://...")
    .transform(FitCenter(), MyCustomTransformation())
    .into(imageView)
```

### 3. 사용자 정의 Transformations

Glide에서는 기본적으로 제공 되는 내장 Transformations을 제공 하지만, 다른 Transformations가 필요한 경우 고유한 사용자 정의 Transformations를 만들어 사용할 수 있다. 

예를 들어 Bitmap에 대한 사용자 정의 변환을 만들고 싶을때 `BitmapTransformation`을 상속한 서브클래스를 만들어서 사용하면 된다. 

Glide에서 기본적으로 제공 하는 `BitmapTransformation`을 상속한 `FitCenter`클래스의 구현을 보면 알 수 있다. 

```java
public class FitCenter extends BitmapTransformation {
  private static final String ID = "com.bumptech.glide.load.resource.bitmap.FitCenter";
  private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

  @Override
  protected Bitmap transform(
      @NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
    return TransformationUtils.fitCenter(pool, toTransform, outWidth, outHeight);
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof FitCenter;
  }

  @Override
  public int hashCode() {
    return ID.hashCode();
  }

  @Override
  public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
    messageDigest.update(ID_BYTES);
  }
}
```

아래의 `FillSpace`예제는 [Glide v4 - BitmapTransformation](https://bumptech.github.io/glide/doc/transformations.html#bitmaptransformation)문서 에서 제공 하는 클래스를 코틀린으로 작성한 예 이다. 

```kotlin
class FillSpace: BitmapTransformation() {
    private val ID = "com.bumptech.glide.transformations.FillSpace"
    private val ID_BYTES: ByteArray = ID.toByteArray(Charset.forName("UTF-8"))

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        if (toTransform.width == outWidth && toTransform.height == outHeight) {
            return toTransform
        }
        return Bitmap.createScaledBitmap(toTransform, outWidth, outHeight, /* filter */ true)
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
    }

    override fun equals(other: Any?): Boolean {
        return other is FillSpace
    }

    override fun hashCode(): Int {
        return ID.hashCode()
    }
}
```

`BitmapTransformation`을 상속한 뒤 `transform()`와 `updateDiskCacheKey()`메소드들은 필수로 구현해주어야 하며 .`transform()`메소드에서 실제 비트맵의 변환 내용을 작성 하면 된다. 

#### 3.1 Required methods

Glide에서는 메모리와 디스크 캐싱을 사용하고 있다. `BitmapTransformation`을 상속한 서브클래스의 경우 아래 3개의 메소드를 필수적으로 재정의 해 주어야 캐싱을 문제없이 사용할 수 있다. 

1. `equals()`
2. `hashCode()`
3. `updateDiskCacheKey()`

클래스 내부에 `String`으로 구성된 `ID`를 만들고 `ByteArray`로 전환하여 위 3개의 재정의된 메소드들에서 캐싱할때 사용 되는 구분자 혹은 키로 사용된다. 

만약 `BitmapTransformation`을 적용하는데 넘겨받은 매개변수가 존재할 경우, 이를 `ByteBuffer`를 이용해 저장해주어야 한다. 예를 들어 Glide에서 기본제공 하는 Transformations중 하나인 `RoundedCorners`를 보면 `updateDiskCacheKey()`에서 전달받은 매개변수 `roundingRadius`을 저장함을 확인 할 수 있다. 

```java
public final class RoundedCorners extends BitmapTransformation {
  private static final String ID = "com.bumptech.glide.load.resource.bitmap.RoundedCorners";
  private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

  private final int roundingRadius;

  /**
   * @param roundingRadius the corner radius (in device-specific pixels).
   * @throws IllegalArgumentException if rounding radius is 0 or less.
   */
  public RoundedCorners(int roundingRadius) {
    Preconditions.checkArgument(roundingRadius > 0, "roundingRadius must be greater than 0.");
    this.roundingRadius = roundingRadius;
  }

  @Override
  protected Bitmap transform(
      @NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
    return TransformationUtils.roundedCorners(pool, toTransform, roundingRadius);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof RoundedCorners) {
      RoundedCorners other = (RoundedCorners) o;
      return roundingRadius == other.roundingRadius;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Util.hashCode(ID.hashCode(), Util.hashCode(roundingRadius));
  }

  @Override
  public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
    messageDigest.update(ID_BYTES);

    byte[] radiusData = ByteBuffer.allocate(4).putInt(roundingRadius).array();
    messageDigest.update(radiusData);
  }
}
```

사용자 정의 `Transformations`를 만드는데 가장 중요한 점들중 하나는 `equals()`, `hashCode()`의 재정의 이다. 이 메소드들이 재정의 되어 정확한 값을 반환하지 않는다면 정상적으로 작동하지 않을것 이다. 또한, `updateDiskCacheKey()`메소드의 경우 서브클래스가 되면 무조건적으로 구현해주어야 한다. 

### 4. Special Behavior in Glide

#### 4.1 Transformations 재사용 

`Transformations`는 stateless이다. 그렇기 때문에 `Transformations`인스턴스를 생성한 뒤 재사용해도 같은 결과를 보장해주어야 한다. 일반적으로 `Transformations`인스턴스는 한번만 생성하고 이를 재사용하기 위해 여러 이미지로드에 적용하는것이 좋다. 

이는 인스턴스를 생성할때 비용을 최소화하여 퍼포먼스에 영향을 주지 않는 것이 옳기 때문이라고 생각 된다. 

#### 4.2 ImageView를 위한 자동 변환 

Glide에서 `ImageView`를 대상으로 이미지 로딩을 시작 하면 해당 뷰의 `ScaleType`에 따라 `FitCenter`또는 `CenterCrop`을 자동으로 적용 할 수 있다. 

만약 scale type이 `CENTER_CROP`이라면 Glide에서는 자동으로 `CenterCrop`변환을 적용 한다. 혹은 scale type이 `FIT_CENTER`이거나 `CENTER_INSIDE`라면 Glide에서는 자동으로 `FitCenter`변환을 적용 해 준다. 

몰론 다른 Trnasformations를 적용 했다면 `RequestOptions`에서 변환이 재정의 되어 적용한 Trnasformations가 최종적으로 적용 될 것 이다. 

#### 4.3 Custom resources

Glide 4.0을 사용 하면 디코딩 대상 리소스의 적용시 변환 유형을 정확히 알지 못할 수도 있다. 예를 들어, Drawable resource를 요청하기 위해 `asDrawable()`을 사용 했을 때 `BitmapDrawable`의 하위 클래스인 `GifDrawable`을 얻을 수 도 있다. 

이 경우 bitmap trnasformations에 `BitmapDrawable`, `GifDrawable`타입을 적용 하여 정확한 리소스 타입을 적용하여 변환 할 수 있다. 

(이 부분은 솔직히 뭔소린지 잘 모르겠지만, asDrawable()을 했을때 슈퍼클래스를 얻으므로 실제는 서브클래스이지만  서브클래스타입은 정확히 무엇인지 모를 수도 있으니, RequestOptions를 통해 정확한 타입을 적용할 수 있다 인듯? 자세한 내용은 좀 더 알아봐야겠다..)
