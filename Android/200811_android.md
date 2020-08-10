## Glide 1 - Basic

Glide에 대한 숙련이 필요하다고 느낀 요즘, [Glide v4 Documentation](https://bumptech.github.io/glide/)를 참고 하여 내용을 번역하면서 필요한 내용들 위주로 정리 하였다.

추가로 Glide가 Java로 작성되어 있다 보니 Kotlin으로 적용 하면서 불편한점을 확장함수 등 으로 정리 해서 좀 더 직관적이고 관리하기 좋은 코드로 만들고 같이 정리 하려 함 :) 

### 1. [Basic usage](https://bumptech.github.io/glide/doc/getting-started.html)

Glide를 이용한 이미지의 로딩은 기본적으로 아래처럼 간단하게 사용 할 수 있다. 

```kotlin
Glide.with(context)
    .load("https://...")
    .into(imageView)
```

만약 대상 뷰에 불러오려할 이미지의 로딩을 취소 하려면 아래처럼 `clear()`메소드를 이용해 대기중인 작업들을 취소 할 수 있다.

```kotlin
Glide.with(context)
    .clear(imageView)
```

Glide에서 참조된 context(Activity나 Fragment)가 제거 될 때 Glide을 통해 로딩될 작업들은 함께 자동으로 중지되므로 `clear()`를 꼭 안드로이드 컴포넌트의 라이프사이클에 동기화해줄 필요는 없다. 

#### 1.1 Customizing requests

Glide에서 사용되는 이미지의 Transformations, Transitions 그리고 Caching과 같은 Option들은 `RequestOptions`을 이용해 이미지 로드 각 요청간에 공통으로 적용 할 수 있다. 생성한 `RequestOptions`인스턴스를 `apply()`함수를 통해 적용한다.

```kotlin
val sharedOptions = RequestOptions()
    .placeholder(R.drawable.placeholder)
    .centerCrop()

Glide.with(context)
    .load("https://...")
    .apply(sharedOptions)
    .into(imageView1)

Glide.with(context)
    .load("https://...")
    .apply(sharedOptions)
    .into(imageView2)    
```

#### 1.2 RecyclerView

`RecyclerView`에서는 Adapter의 콜백 함수 중 `onBindViewHolder()`에서 Glide를 통해 이미지를 로드 한다. 

```kotlin
override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val url = items.get(position).url
    Glide.with(context)
        .load(url)
        .into(holder.imageView)
}
```

위 예제 `onBindViewHolder()`함수의 경우 `url`이 null 이거나 empty string일 수도 있다. 이 경우 Glide에 `placeHolder()`나 `error()`를 이용해 플레이스 홀더 혹은 에러시 대체할 이미지를 보여준다. 만약 그 도 없다면 비어있는 이미지뷰가 존재 한다. 

좋은 방법은 `url`이 존재하지 않을 경우 Glide를 통해 이미지 로드를 아예 하지 않고, 직접 `holder.imageView`인스턴스를 참조 하여 이미지를 set하는 것 이다. 그 전에 `onBindViewHolder()`콜백을 통해 재 사용된 View에서 Glide를 통해 로딩된 이미지가 존재 하거나 작업이 있을 수 있으므로 `clear()`시켜 주면 좋다. (`clear()`메소드 콜을 통해 확실하게 이전 에 로드하려고 했던 이미지를 보여지지 않게 하려는 목적이다)

```kotlin
override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val url = items.get(position).url
    if (!url.isNullOrEmpty()) {
        Glide.with(context)
            .load(url)
            .into(holder.imageView)
    } else {
        Glide.with(context)
            .clear(holder.imageView)
        holder.imageView.setImageDrawable(R.drawable.placeholder)
    }    
}
```

#### 1.3 Non-View Targets

꼭 `ImageView`를 대상으로 하는게 아닌 후처리를 위해서 `into()`메소드를 통해 비동기로 이미지를 불러오는 방법이 있다. 

```kotlin
Glide.with(this)
    .load("https://...")
    .into(object:CustomTarget<Drawable>() {
        override fun onLoadCleared(placeholder: Drawable?) {
            // `onResourceReady()`콜백을 통해 제공된 Drawable의 clear후 불리는 콜백
        }

        override fun onResourceReady(
            resource: Drawable,
            transition: Transition<in Drawable>?
        ) {
            // 여기에서 완전히 로딩된 이미지 Drawable을 처리 한다
        }
})
```

`onResourceReady()`콜백 메소드에 비동기로 불리어진 이미지가 Drawable로 래핑되어서 전달 된다. 이 메소드 내부에서 Drawable을 이용 해 View에 대한 처리를 하면 된다. 

`into()`메소드에 적용된 `CustomTarget<T>`는 Java 추상클래스이어서 코틀린으로 적용 할 때 무명클래스로 구현하여 인스턴스화 시켜주어야 하기 때문에 불필요한 코드들이 생성되므로 이를 확장 함수로 수정 하면 아래와 같다. 

```kotlin
inline fun <T> RequestBuilder<T>.into(crossinline target: (T) -> Unit) =
    into(object : CustomTarget<T>() {
        override fun onLoadCleared(placeholder: Drawable?) {}

        override fun onResourceReady(resource: T, transition: Transition<in T>?) {
            target(resource)
        }
    })

inline fun <T> RequestBuilder<T>.into(
    crossinline target: (T) -> Unit,
    crossinline loadCleared: (placeHodler: Drawable?) -> Unit?
) = into(object : CustomTarget<T>() {
        override fun onLoadCleared(placeholder: Drawable?) {
            loadCleared.let { it(placeholder) }
        }

        override fun onResourceReady(resource: T, transition: Transition<in T>?) {
            target(resource)
        }
    })
```

확장함수에 대한 사용법은 아래와 같다. 

```kotlin
Glide.with(this)
    .load("https://...")
    .into<Drawable> {
        imageView.setImageDrawable(it)
    }
```

`Target`에 대한 내용은 추 후에 정리 하려 한다. 

#### 1.4 Background threads

백그라운드 스레드를 이용해 이미지를 로딩하는 방법은 `submit(width: Int, height: Int)`메소드를 이용하여 사용할 수 있다. 

```kotlin
val futureTarget: FutureTarget<Bitmap> = Glide.with(context)
    .asBitmap()
    .load("https://...")
    .submit(width, height)
```

위 예제 에서 얻은 `futureTarget`인스턴스를 통해서 백그라운드 스레드를 통해서 얻은 Bitmap인스턴스를 얻을 수 있다. 

```kotlin
val bitmap = futureTarget.get()
```

`FutureTarget`인스턴스를 갖고 있는 경우 기존 `clear()`메소드를 이용해서 대기중인 작업을 중지 시킬 수 있다. 

```kotlin
Glide.with(context)
    .clear(futureTarget)
```

꼭 백그라운드 스레드를 사용하지 않아도 `Bitmap`인스턴스로 이미지를 얻고 싶다면 `asBitmap()`메소드와 `Target<T>`을 통해 비동기로 불러와 후처리를 할 수 있다. 

```kotlin
Glide.with(context)
    .asBitmap()
    .load("https://...")
    .into(object: Target<Bitmap> {
        // ... 
    })
```
