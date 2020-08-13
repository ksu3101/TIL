## Glide 4 - Targets 

Glide에 대한 숙련이 필요하다고 느낀 요즘, [Glide v4 Documentation](https://bumptech.github.io/glide/)를 참고 하여 내용을 번역하면서 필요한 내용들 위주로 정리 하였다.

추가로 Glide가 Java로 작성되어 있다 보니 Kotlin으로 적용 하면서 불편한점을 확장함수 등 으로 정리 해서 좀 더 직관적이고 관리하기 좋은 코드로 만들고 같이 정리 하려 함 :) 

### 1. [Targets](https://bumptech.github.io/glide/doc/targets.html)

Glide에서 `Targets`은 이미지 리소스에 대한 요청과 요청자에 대한 중재자 역활을 한다. Targets은 placeholder를 보여주거나 리소스를 불러오고, 각 리소스 요청에 대한 적절한 처리를 한다. 

`Targets`로 자주 사용 되는 대상은 `ImageView`를 대상으로 하는 `ImageViewTargets`이다. 이를 통해 placeholder를 보여주고 Drawable이나 Bitmap을 ImageView에 보여주게 한다. 사용자는 이 `Targets`를 구현하거나 Glide에서 제공하는 기본 클래스들을 사용 할 수도 있다. 

#### 1.2 Targets 지정

Glide의 `into(Target)`메소드를 이용해 최종적으로 이미지 리소스를 요청하는것 외 에도, 요청 결과를 수신할 대상을 지정하는데 사용 된다. 

아래 예제에서는 `Target`인터페이스를 구현하는 방법이다. 

```kotlin
val target: Target<Drawable> =
    Glide.with(context)
        .load("http://...")
        .into(object: Target<Drawable> {
            // ...
        })
```

Glide에서 제공하는 `into(ImageView)`메소드를 이용할 수도 있다. 메소드의 변수로 전달되는 `ImageView`인스턴스는 `ImageViewTarget`으로 래핑되어 처리 된다. 

```kotlin
val target: Target<Drawable> =
    Glide.with(context)
        .load("http://...")
        .into(imageView)
```

#### 1.3 요청 취소와 재사용 방법 

`into(Target)`과 `into(ImageView)`의 경우 `Target`을 반환한다. 반환되는 이 `Target`인스턴스는 재 사용할 수 있다.

```kotlin
val target: Target<Drawable> =
    Glide.with(context)
        .load("http://...")
        .into(object: Target<Drawable> {
            // ...
        })

Glide.with(context)
    .load(anotherUrl)
    .into(target)
```

`Target`인스턴스는 대기중인 요청을 취소 시킬수 있는 `clear()`메소드를 제공 한다. 

```kotlin
val target: Target<Drawable> =
    Glide.with(context)
        .load("http://...")
        .into(object: Target<Drawable> {
            // ...
        })

Glide.with(context)
    .clear(target)
```

만약 ImageView와 같은 View를 대상으로 한 요청의 Target은 `ViewTarget`으로 사용 된다. 이 클래스에서는 각 View에 대해 안드로이드 프레임워크 메소드인 `getTag()`, `setTag()`를 사용 하여 정보를 저장한다. 그렇기 때문에 View 인스턴스만 있다면 `clear()`혹은 `into()`를 이용해 재 사용할 수 있다. 

```kotlin
Glide.with(context)
    .load("http://...")
    .into(imageView)

Glide.with(context)
    .load(anotherUrl)
    .into(imageView)

Glide.with(context)
    .clear(imageView)
```

`ViewTarget`에서만 가능한 것 으로, 각 리소스 요청에 대해 생성되는 인스턴스의 불러오는 과정에서 클리어 과장까지에 대한 Glide의 정보를 요청하여 얻을 수도 있다. 

```kotlin
Glide.with(context)
    .load("http://...")
    .into(DrawableImageViewTarget(imageView))
```

이 경우 `ViewTarget`을 상속해야 하며, `setRequest()`와 `getRequest()`를 구현해주어야 사용 할 수 있다고 한다. 

- 솔직히 이부분에 대해서는 잘 이해가 안되지만, `ViewTarget`을 상속한 Target을 사용할 때 request에 대한 정보를 가진 인스턴스를 get/setter를 통해 얻어올 수 있다는 것 같다. 

#### 1.4 clear

불러오기를 완료한 리소스(Drawable, Bitmap)을 완전히 다 사용한 경우 요청에 대한 `Target`을 지우는것이 좋은 습관이다. `Target`을 `clear()`하지 않으면 CPU와 메모리와 같은 시스템자원이 낭비될테고, 추후의 리소스 요청이 실행되지 않거나 잘못된 정보가 화면에 보여질 수도 있다. 

특히 하나의 View등을 대상으로 여러가지의 요청이 있을 경우(예를 들면 `RecyclerView`의 각 아이템 뷰 에서의 이미지 로드) 정확한 이미지리소스를 보여주는것을 보장하고 싶다면 유효한 정보인지 확인하고 이미지 리소스를 요청하거나 `clear()`하여 대기중일수도 있는 작업을 취소한 뒤 이미지 요청을 시도 하는 것 이 좋다. 

### 2. Sizes and Dimensions

Glide는 기본적으로 `getSize()`메소드를 이용해 Targets에서 제공한 크기를 리소스 요청의 크기로 사용 한다. Glide에서는 URL을 통해 이미지 리소스를 down sampling한뒤 자르고 크기를 변환하여 메모리 사용량을 최소화 시키고 불러오는 속돌르 최대한 빠르게 한다. 

#### 2.1 View Targets

`ViewTarget`은 `View`의 속성을 검사하거나 `OnPreDrawListener`를 사용 하여 렌더링 직전에 `View`를 측정하여 `getSize()`를 구현한다. 그래서 Glide는 대부분의 이미지들을 표시될 뷰와 일치하도록 자동으로 크기를 조정할 수 있다. 더 작은 이미지를 불러오면 Glide가 더 빠르게 불러오고 디스크 캐시처리된 후 더 적은 메모리를 사용하게 되며, 만약 뷰들의 크기가 일관적으로 같다면 Glide의 BitmapPool의 적중률을 더 높일 수 있다. 

`ViewTarget`은 아래와 같은 로직을 따른다. 

1. 만약 `View`의 레이아웃 변수가 0 보다 크고 `padding`보다 크다면, 레이아웃 변수를 사용 한다. 
2. 만약 `View`의 치수가 0보다 크고 `padding`보다 크다면, 해당 치수를 사용 한다. 
3. 만약 `View`의 레이아웃 변수가 `wrap_content`이거나 최소한 한개 이상의 레이아웃 전달이 발생했다면, `Target.SIZE_ORIGINAL`이나 `override()`함수를 통해 사이즈를 수정 하라는 warning 로그가 출력 된다. 
4. 그렇지 않은 경우(레이아웃 변수가 `match_parent`, 0, `wrap_content`이고 레이아웃 전달이 발생하지 않았을 경우), 레이아웃 전달이 발생하기를 기다린 뒤 발생했다면 위 (1)번부터 로직을 다시 밟는다. 

`RecyclerView`를 사용할 때 가끔 `View`를 재사용할때 기존 위치의 사이즈를 변경된 지금으 위치에서 그대로 사용 할 수도 있다. 이럴 경우 새로운 `ViewTarget`인스턴스를 생성하고 `waitForLayout`변수를 `true`로 설정하면 레이아웃전달이 발생될 때 까지 대기한다. 

```kotlin
override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    Glide.with(context)
        .load(urls.get(position))
        .into(DrawableImageViewTarget(holder.imageView, /* waitForLayout */ true))
}
```

#### 2.2 View 사이즈의 성능

일반적으로 Glide에서는 로드될 뷰에 명시적인 dp사이즈가 설정되어 있을때 가장 빠르고 예측 가능한 결과들을 제공한다. 그러나 명시적인 dp설정이 불가능한 경우 Glide는 `OnPreDrawListeners`를 이용해 layout weight, `match_parent`등과 같은 상대적인 사이즈에 대한 지원도 가능 하다. 

#### 2.3 대안 

Glide가 뷰의 사이즈를 잘못 가져오는것 같다면 `ViewTarget`을 확장하고 자체적인 로직을 구성하거나 `RequestOptions`에서 `override()`메소드를 이용해 사이즈를 수동으로 재정의 할 수 있다. 

### 3. Custom Targets

만약 custom `Target`을 사용하고 있으며 `View`를 대상으로 불러오기를 하지 않을때 `ViewTarget`을 상속한 클래스는 `getSize()`메소드를 구현해야 한다. 

간단하게 하자면, `getSize()`를 구현하여 매개변수로 주어진 콜백을 바로 호출하게 하는 것 이다. 

```kotlin
override fun getSize(cb: SizeReadyCallback) {
    cb.onSizeReady(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
}
```

하지만 `Target.SIZE_ORIGINAL`을 사용하면 불러올 이미지가 큰 경우 매우 비효율적이거나 OOM예외가 발생할 수 있다. 다른 방법으로는, `Target`의 생성자를 통해 사이즈를 전달하여 콜복에 전달 할 수 있다. 

```kotlin
class CustomTarget<T>(
    val int width,
    val int height
): Target<T> {
    override fun getSize(cb: SizeReadyCallback) {
        cb.onSizeReady(width, height)
    }
}
```

콜백의 경우 메모리 릭을 방지하기 위해 `removeCallback`을 구현해주도록 한다. 

```kotlin
override fun removeCallback(cb: SizeReadyCallback) {
    sizeDeterminer.removeCallback(cb)
}
```

더 많은 예제를 참고하려면 Glide에서 제공 하는 `ViewTarget`클래스 내부를 보는것을 추천한다. 

### 4. 애니메이션 리소스 와 Custom Targets. 

만약 `GifDrawable`, 실제 리소스 유형을 View에 불러올 경우 가능하면 `into(ImageView)`메소드를 사용해야 한다. 하지만 `View`를 대상으로 불러오지 않는다면, `ViewTarget`은 `SimpleTarget`과 같은 custom `Target`을 이용해 `GifDrawable`, 애니메이션 리소스 을 불러오면 된다. `SimpleTarget`에서는 `onResourceReady()`콜백을 통해 전달받은 `resource`를 `start()`하여 `GifDrawable`의 애니메이션을 시작하게 할 수 있다. 

```kotlin
Glide.with(context)
    .asGif()
    .load("http://...")
    .into(object: SimpleTarget<GifDrawable>(){
        override fun onResourceReady(
            resource: GifDrawable,
            transition: Transition<in GifDrawable>?
            ) {
                resource.start()
            }
        })
```

`GifDrawable`이 아닌 `Bitmap`으로 불러온경우 `Drawable`을 `Animatable`로 타입 체크 후 사용할 수 있다. 

```kotlin
Glide.with(context)
    .load("http://...")
    .into(object: SimpleTarget<Drawable>(){
        override fun onResourceReady(
            resource: Drawable,
            transition: Transition<in Drawable>?
            ) {
                if (resource is Animatble) {
                    resource.start()
                }
            }
        })
```

> `SimpleTarget`의 경우 이 글 작성시점에서는 deprecated처리 되어 있어 사용을 추천하지 않으며 `CustomViewTarget`혹은 `ViewTarget`을 상속하여 사용함을 추천한다.
> `SimpleTarget`을 사용 한 뒤 꼭 `clear()`를 해주어야 한다. 