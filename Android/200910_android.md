## Lottie for Android - Advanced 3

Airbnb의 애니메이션 지운 라이브러리인 [Lottie](http://airbnb.io/lottie/#/README)에서 안드로이드 관련 문서를 번역하고 정리해보려 한다. 

1. [Lottie for Android - Lottie란 무엇인가](https://github.com/ksu3101/TIL/blob/master/Android/200904_android.md)
2. [Lottie for Android - Basic](https://github.com/ksu3101/TIL/blob/master/Android/200905_android.md)
3. [Lottie for Android - Advanced 1](https://github.com/ksu3101/TIL/blob/master/Android/200906_android.md)
4. [Lottie for Android - Advanced 2](https://github.com/ksu3101/TIL/blob/master/Android/200907_android.md)
5. Lottie for Android - Advanced 3

### 1. 이미지 

Lottie는 벡터 모양(shapes)으로 작업하도록 설계 되어 있다. Lottie는 이미지 렌더링을 지원하지만 사용에는 단점이 존재 한다. 

- (이미지)파일은 같은 벡터 애니메이션보다 크기가 크다. 
- 크기가 변경되면 이미지는 픽셀화된다. 
- 애니메이션에 복잡성이 더해진다. 하나의 이미지 파일 대신 json파일 혹은 애니메이션 프레임에 해당하는 모든 이미지들이 필요해 진다. 

### 2. 이미지의 설정 

다음 3가지 방법으로 Lottie이미지를 설정 할 수 있다. 

#### 2.1 `src/assets`

이미지를 사용해야 하는 경우 이미지를 `src/assets`의 폴더에 넣는다. 그리고 `LottieAnimationView`또는 `LottieDrawable`에서 `setImageAssetsFolder()`메소드를 호출 하여 이미지가 저장된 assets폴더로 Lottie를 통해 불러와야 한다. 다시 말하면 bodymovin이 내보내는 이미지가 이름이 변경되지 않은 폴더에 있는지 확인한다. (img_# 이어야 한다) LottieDrawable을 직접 사용하는 경우, 애니메이션 작업이 끝나면 `recycleBitmaps()`를 호출 해야 한다. 

#### 2.2 zip 압축 파일

json과 이미지가 함께 압축된 zip파일을 만들 수 있다. Lottie는 압축을 플고 내용을 읽을 수 있다. 이것은 로컬 파일 또는 원격 URL을 통해 수행할 수 있다. 

#### 2.3 자신만의 이미지 제공 

떄로는 APK의 사용 공간을 절약하거나 네트워크에서 애니메이션을 다운로드 하는 경우가 있다. 이런 경우 `LottieAnimationView`또는 `LottieDrawable`에서 `imageAssetDelegate()`를 설정할 수 있다. 

델리게이트(Delegate)는 Lottie가 이미지를 렌더링 할때마다 호출 된다. 이미지의 이름을 전달하고 비트맵을 반환하도록 요청한다. 아직 다운로드 되지 않은 경우(예를 들어 아직 다운로드 중 인경우) null을 반환하면 Lottie는 null이 아닌 값을 반환할 때 까지 모든 프레임에서 계속 비트맵의 여부를 묻게된다. 

```java
animationView.setImageAssetDelegate(new ImageAssetDelegate() {
 @Override public Bitmap fetchBitmap(LottieImageAsset asset) {
   if (downloadedBitmap == null) {
       // 아직 비트맵을 전달받지 못했다. 
       // Lottie는 계속 비트맵이 non-null인지 여부를 계속 확인 할 것 이다. 
       return null;
   }
   return downloadedBitmap;
 }
});
```

### 3. Lottie가 APK파일 크기에 미치는 영향은? 

미치는 영향은 미미하다. 

- 최대 ~800개의 메소드들.
- 압축되지 않은 111kb크기를 갖는 파일들. 
- play store를 통해 다운로드시 gzip으로 압축되어 45kb으로 줄어든다. 

### 4. Lottie vs Android Vector Drawable(AVD)

After Effects애니메이션을 렌더링 하는 방법에는 2가지 방법이 있다. 

1. Bodymovin으로 json을 내보내고 lottie-android으로 이를 재생한다. 
2. Bodymovin으로 `AndroidVectorDrawable` xml을 내보내고 Android SDK를 사용하여 재생한다. 

#### 4.1 Lottie의 장점 

- 훨씬 더 많은 After Effects기능 세트를 지원한다. 
- 애니메이션을 제스처, 이벤트등에 연결하도록 진행률을 수동으로 설정 할 수 있다. 
- 네트워크에서 애니메이션을 다운로드할 수 있다. 
- 동적인 재생 속도. 
- anti-aliassing이 설정된 마스크. (픽셀의 계단 방지 효과가 적용된 마스크)
- 애니메이션의 특정부분의 색상을 동적으로 변경 가능. 

#### 4.2 AnimatedVectorDrawable의 장점

- `RenderThread`에서 애니메이션 재생시 메인스레드 보다 더 빠른 성능을 보장. 

#### 4.3 Bodymovin AVD Exporter

Bodymovin은 필요에 따라 일부 애니메이션을 직접 AVD로 내보낼 수 있다. 하지만 아직 실험적인 기능들이 존재 하며 Lottie및 AVD기능의 일부만 지원 한다. 




