## Glide 11 - Hardware Bitmaps

Glide에 대한 숙련이 필요하다고 느낀 요즘, [Glide v4 Documentation](https://bumptech.github.io/glide/)를 참고 하여 내용을 번역하면서 필요한 내용들 위주로 정리 하였다.

추가로 Glide가 Java로 작성되어 있다 보니 Kotlin으로 적용 하면서 불편한점을 확장함수 등 으로 정리 해서 좀 더 직관적이고 관리하기 좋은 코드로 만들고 같이 정리 하려 함 :) 

### 1. [하드웨어 비트맵이란 무엇인가?]](https://bumptech.github.io/glide/doc/hardwarebitmaps.html)

Android O버전에 추가된 새로운 비트맵 타입으로 `Bitmap.Config.HARDWARE`가 있다. 하드웨어 비트맵은 그래픽 처리 프로세스의 메모리에만 픽셀 데이터를 저장하여 비트맵이 화면에 그려지는데 최적화된 성능을 갖고 있다. 

### 2. 왜 하드웨어 비트맵을 써야 하는가? 

하드웨어 비트맵용으로는 단 하나의 픽셀데이터의 복사된 데이터가 저장된다. 일반적으로 어플리케이션 프로그램의 메모리(픽셀 바이트 배열)에 픽셀 데이터 복사본이 하나 있고, 그래픽 메모리에 복사본 하나(픽셀이 GPU에 업로드 된 후)가 더 존재 한다. 하드웨어 비트맵은 GPU에 업로드 된 사본만 유지시킨다. 그 결과, 

- 하드웨어 비트맵에는 다른 비트맵구성과 달리 절반의 메모리만 필요한다. 
- 하드웨어 비트맵은 그려지는 동안 텍스쳐 업로드로 인하여 생길 수 있는 버벅거림을 방지 한다. 

### 3. 하드웨어 비트맵은 어떻게 해야 활성화할 수 있는가? 

Glide는 기본적으로 하드웨어 비트맵이 사용되도록 설정되어 있기때문에 변경할 필요는 없으며, 비활성화할때만 설정이 필요하다.

만약 전역적으로 비활성화 설정인 상태에서 일시적으로 활성화 설정하려면, Glide의 이미지 요청에서는 기본 `DecodeFormat`을 `DecodeFormat.PREFER_ARGB_8888`으로 설정 한다. 어플리케이션의 모든 요청에 대해 디코드 포맷을 적용하려면 `GlideModule`의 기본 옵션에서 `DecodeFormat`을 설정 하면 된다. 

### 4. 하드웨어 비트맵은 어떻게 해야 비활성화 할 수 있는가?

하드웨어 비트맵을 비활성화해야 하는 경우 `disallowHardwareConfig()`메소드를 사용하여 특정 요청에 대해 하드웨어 비트맵을 비활성화 할 수 있다. 

```kotlin
Glide.with(context)
    .load("http://...")
    .disallowHardwareConfig()
    .into(imageView)
```

### 5. 하드웨어 비트맵을 사용할때 손상되는 것 은 무엇인가? 

그래픽 메모리에 픽셀 데이터를 저장하면 픽셀 데이터에 쉽게 접근할수 없으므로 경우에 따라 예외가 발생할 수 있다. 알려진 사례는 아래와 같다. 

- 픽셀 데이터를 아래의 자바 메소드를 이용해 읽거나 쓸때,
  - `Bitmap.getPixel()`
  - `Bitmap.getPixels()`
  - `Bitmap.copyPixelsToBuffer()`
  - `Bitmap.copyPixelsFromBuffer()`
- 픽셀 데이터를 네이티브 코드를 통해 읽거나 쓸때,
- 하드웨어 비트맵을 소프트웨어 `Canvas`에 그리려고 시도 할 때, 
  ```kotlin
  val canvas = Canvas(normalBitmap)
  canvas.drawBitmap(hardwareBitmap, 0, 0, Paint())
  ```
- `View`에 비트맵을 소프트웨어 레이어 타입으로 그리려 할때, 
  ```kotlin
  val imageView = ...
  imageView.setImageBitmap(hardwareBitmap)
  imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
  ```
- 너무 많은 file descriptor를 열었을 때,
  - 하드웨어 비트맵은 파일 디스크립터를 사용한다. 프로세스는 각 사용할 수 있는 FD(File descriptor)제한이 있다. (AOS O및 이전 버전에 한해 1024, O-MR1 이후의 버전에서는 32k). Glide는 하드웨어 비트맵의 수를 제한하려고 시도하지만, 이미 많은 수의 FD가 할당된 경우 문제가 생길 수 있다. 
- `ARGB_8888`비트맵이 전제조건인 상황인 경우,
- `Canvas`를 사용하여 뷰의 계층을 그리는 코드에 의해 발생한 스크린 샷 처리
  - `PixelCopy`는 O버전 이상에서 대신 사용할 수 있다. 
- 공유된 요소의 전환(OMR1) 예제에서 발생 (아래는 예제 로그)
  ```
  java.lang.IllegalStateException: Software rendering doesn't support hardware bitmaps
  at android.graphics.BaseCanvas.throwIfHwBitmapInSwMode(BaseCanvas.java:532)
  at android.graphics.BaseCanvas.throwIfCannotDraw(BaseCanvas.java:62)
  at android.graphics.BaseCanvas.drawBitmap(BaseCanvas.java:120)
  at android.graphics.Canvas.drawBitmap(Canvas.java:1434)
  at android.graphics.drawable.BitmapDrawable.draw(BitmapDrawable.java:529)
  at android.widget.ImageView.onDraw(ImageView.java:1367)
  [snip]
  at android.view.View.draw(View.java:19089)
  at android.transition.TransitionUtils.createViewBitmap(TransitionUtils.java:168)
  at android.transition.TransitionUtils.copyViewImage(TransitionUtils.java:102)
  at android.transition.Visibility.onDisappear(Visibility.java:380)
  at android.transition.Visibility.createAnimator(Visibility.java:249)
  at android.transition.Transition.createAnimators(Transition.java:732)
  at android.transition.TransitionSet.createAnimators(TransitionSet.java:396)
  [snip]
  ```

### 6. 하드웨어 비트맵을 사용시 효율성이 떨어지는 상황은? 

경우에 따라 사용자의 작업이 중단되는것을 피하기 위해, `Bitmap`클래스는 비용이 많이 들어가는 그래픽 메모리 복사를 수행 한다. 이러한 방법 중 하나가 사용되는 경우에는 느린 방법이 사용되는 빈도에 따라 시작하기 위해서 하드웨어 비트맵 구성을 사용하지 않는 것 이 좋다. 아래의 메소드를 사용하는 경우 프레임워크 에서는 "하드웨어 비트맵에서 픽셀을 읽으려는 시도는 매우 느린 작업입니다."라는 메시지를 기록하고 `StrictMode.noteSlowCall()`를 실행한다. 

- `Bitmap.copy`
- `Bitmap.createBitmap`
- `Bitmap.writeToParcel`
- `Bitmap.extractAlpha`
- `Bitmap.sameAs`