## Glide 2 - Options

Glide에 대한 숙련이 필요하다고 느낀 요즘, [Glide v4 Documentation](https://bumptech.github.io/glide/)를 참고 하여 내용을 번역하면서 필요한 내용들 위주로 정리 하였다.

추가로 Glide가 Java로 작성되어 있다 보니 Kotlin으로 적용 하면서 불편한점을 확장함수 등 으로 정리 해서 좀 더 직관적이고 관리하기 좋은 코드로 만들고 같이 정리 하려 함 :) 

### 1. [RequestBuilder options](https://bumptech.github.io/glide/doc/options.html)

Glide를 이용해 불러올 이미지들의 작업을 대상으로 여러가지 Options들을 `Glide.with()`메소드를 통해 반환되는  `RequestBuilder`를 이용해 설정할 수 있다. 

설정가능한 옵션들은 아래와 같다. 

- Placeholder : 이미지가 완전히 로딩되기 전 까지 대신 보여주게 될 이미지 
- Transformation : 이미지에 대한 후처리 설정들 
- Caching strategies : 캐싱 정책 
- Component specific options, encode quality, decode Bitmap configuration

예를 들어 이미지에 대해 center crop transformation을 적용 하고 싶다면 아래 처럼 option 메소드를 추가 체이닝 하면  된다.

```kotlin
Glide.with(context)
    .load("https://...")
    .centerCrop()
    .into(imageView)
```

### 2. RequestOptions

이미지로드를 진행할때 적용할 Option을 공통으로 사용하고 싶은 경우 옵션을 `RequestOptions`인스턴스를 이용해 적용 하고 `apply()`메소드를 통해서 공통 적용할 수 있다. 

```kotlin
val options: RequestOptions = RequestOptions().centerCrop(context)

Glide.with(context)
    .load("http://...")
    .apply(options)
    .into(imageView)
```

`apply()`의 경우 여러개를 호출 하여 `RequestOptions`을 중첩하여 사용할 수 있다. 만약 동일한 계열의 옵션을 가진 `RequestOptions`이 존재 한다면 마지막으로 추가한 `RequestOptions`이 적용 된다. 

### 3. TransitionOptions

`TransitionOptions`는 요청한 이미지의 로딩이 완료 시 어떻게 이미지를 보여주게 할 것인지를 설정 할 수 있게 해준다. transition 효과들은 `transition()`메소드를 통해 적용 가능하며, 제공되는 옵션은 다음과 같다. 

- View fade in : 이미지가 해당 뷰에 페이드 애니메이션효과와 함께 보여진다.
- Cross fade from placeholder : 로딩 전 설정된 플레이스 홀더 이미지 와 상호 교차 페이드 애니메이션 효과를 적용해 교체되면서 보여진다. 
- No transition : 기 설정한 트랜지션 효과를 제거하거나 설정하지 않는다.

기본적으로 `No transition`이며 기존 설정된 이미지(플레이스 홀더, 에러 홀더 포함)를 새롭게 불러와진 이미지로 교체된다. 이 경우 이미지가 로딩된 이미지로 교체 되면서 사용자 입장에서는 바뀌었는지 확인이 어려울 수도 있어 약간의 지연효과 처럼 페이드 효과를 적용 할 수 있다. 

Cross fade의 적용예는 아래와 같다. 

```kotlin
Glide.with(context)
    .load("https://...")
    .transition(withCrossFade())
    .into(imageView)
```

위 예제 코드에서는 `Drawable`을 대상으로 처리 하기 때문에 `DrawableTransitionOptions.withCrossFade()`메소드를 사용 하였다. 하지만, 만약 Bitmap을 불러오는 경우 `BitmapTransitionOptions`을 사용 해야 한다. 

### 4. RequestBuilder

`RequestBuilder`는 Glide에서 이미지요청의 중심점이며 URL등을 통해 이미지를 가져오는 역할을 담당한다. `RequestBuilder`을 통해 설정할 수 있는 옵션들은 아래와 같다. 

- Bitmap, Drawable등 불러올 이미지 리소스의 타입
- url, model등 이미지 리소스를 불러올 대상
- 불러온 이미지 리소스가 보여지게 될 view
- `RequestOption`의 적용
- `TransitionOption`의 적용
- 이미지 로딩 시`thumbnail()`의 적용 여부 

`RequestBuilder`인스턴스는 `Glide.with()`메소드를 호출 시 얻을 수 있다. 

```kotlin
val requestBuilder: RequestBuilder<Drawable> = Glide.with(context).asDrawable()
```

혹은 `load()`함수를 이용해서도 얻을 수 있다. 

```kotlin
val requestBuilder: RequestBuilder<Drawable> = Glide.with(context).load("https://...")
```

#### 4.1 이미지 리소스 타입 설정 

불러올 이미지를 Bitmap, 혹은 Drawable타입으로 처리해서 인스턴스로 콜백 할 것인지 여부를 `as...`메소드들을 통해 정할 수 있다. 

```kotlin
val requestBuilder: RequestBuilder<Bitmap> = Glide.with(context).asBitmap()
```

#### 4.2 RequestOptions, TransitionOptions 적용 

미리 정의된 `RequestOption`이나 `TransitionOption`을 `apply(), transition()`메소드를 통해서 적용 할 수 있다. 

```kotlin
val requestBuilder: RequestBuilder<Drawable> = Glide.with(context).asDrawable()
requestBuilder.apply(requestOptions)
requestBuilder.transition(transitionOptions)
```

`RequestBuilder`인스턴스는 필요한 경우 재사용가능 하므로 다시 이미지를 불러올 수 있다. 

```kotlin
val requestBuilder: RequestBuilder<Drawable> 
    = Glide.with(context)
        .asDrawable()
        .apply(requestOptions)

imageViews.mapIndexed { index, imageView -> 
    val url = urls[index]
    requestBuilder.load(url).into(imageView)
}
```

#### 4.3 Thumbnail 요청 

Glide의 `thumbnail()`을 사용 하여 실제 이미지의 로딩과 함께 동시에 시작 하여 미리보기 썸네일 이미지를 먼저 가져온뒤 실제 이미지를 불러오게 할 수 있다. 자세히 말하면 `thumbnail()`으로 요청되는 이미지는 `load()`로 불러올 이미지보다 먼저 요청 되며 문제 없이 썸네일을 가져오면 `load()`를 통해 실제 가져오게 될 이미지가 로드되어 설정되기 전 까지 보여진다. 

`thumbnail()`으로 보여질 이미지는 저해상도의 작은 이미지를 대상이며, 이를 디스크 캐시와 함께 사용한다면 그 효율은 더 좋아질 것 이다. 

`thumbnail()`의 사용 에제는 아래와 같다. 

```kotlin
Glide.with(context)
    .load(realImageUrl)
    .thumbnail(
        Glide.with(context)
            .load(thumbnailUrl)
    ).into(imageView)
```

위 예제의 경우 썸네일전용의 저해상도 이미지의 url이 제공되고 있을 때 사용할 수 있다. 하지만 썸네일을 제공하지 않는 경우 원본 url을 대상으로도 `thumbnail()`api를 사용 할 수 있다. 

썸네일이 존재 하지 않는 이미지 url혹은 local 리소스인 경우 Glide의 `override()`나 `sizeMultiplier()`를 이용하여 썸네일 이미지 요청을 저해상도로 강제 하여 실제 썸네일 이미지를 로드한것 처럼 사용 할 수 있다. 

```kotlin
val thumbnailSize: Int = ...
Glide.with(context)
    .load(url)
    .thumbnail(
        Glide.with(context)
            .load(url)
            .override(thumbnailSize)
    ).into(imageView)
```

`sizeMultiplier()`메소드를 이용하여 불러올 이미지의 크기에 대해 일정한 비율로 불러오는 방법이 있다. 

```kotlin
Glide.with(context)
    .load(url)
    .thumbnail(0.25f)   // sizeMultiplier
    .into(imageView)
```

#### 4.4 이미지 요청 실패시 다른 요청 시도

Glide 4.3.0버전 이상에서는, `RequestBuilder`인스턴스를 이용해 대상 이미지의 로딩이 실패하였을 경우 `error()` api를 이용 하여 실패시 지정된 url의 이미지를 불러오게 할 수 있다. 

```kotlin
Glide.with(context)
    .load(url)
    .error(
        Glide.with(context)
            .load(failImageUrl)
    ).into(imageView)
```

`error()`에 적용된 `RequestBuilder`는 메인으로 불러질 대상 이미지의 요청이 성공했을 경우 실행되지 않는다. 

### 5. Component Options

`Option`클래스를 이용해 사용자 정의된 Glide 컴포넌트로 사용 할 수 있다. 이는 `ModelLoaders, ResourceDecoders, ResourceEncoders, Encoders` 등을 포함한다. 

```kotlin
Glide.with(context)
    .load("http://...")
    .option(MyCustomModelLoader.TIMEOUT_MS, 1000L)  // custom model loader
    .into(imageView)
```

