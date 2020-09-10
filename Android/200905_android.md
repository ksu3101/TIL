## Lottie for Android - Basic

Airbnb의 애니메이션 지운 라이브러리인 [Lottie](http://airbnb.io/lottie/#/README)에서 안드로이드 관련 문서를 번역하고 정리해보려 한다. 

1. [Lottie for Android - Lottie란 무엇인가](https://github.com/ksu3101/TIL/blob/master/Android/200904_android.md)
2. [Lottie for Android - Basic](https://github.com/ksu3101/TIL/blob/master/Android/200905_android.md)
3. [Lottie for Android - Advanced 1](https://github.com/ksu3101/TIL/blob/master/Android/200906_android.md)
4. [Lottie for Android - Advanced 2](https://github.com/ksu3101/TIL/blob/master/Android/200907_android.md)
5. [Lottie for Android - Advanced 3](https://github.com/ksu3101/TIL/blob/master/Android/200910_android.md)
6. [Lottie for Android - Advanced 4](https://github.com/ksu3101/TIL/blob/master/Android/200911_android.md)

### 1. 시작하며 

Lottie를 사용하려면 프로젝트의 `build.gradle`파일에 아래의 종속을 추가 하면 된다. 

```
dependencies {
    implementation "com.airbnb.android:lottie:$lottieVersion"
}
```

Lottie의 마지막 버전은 아래 이미지에서 확인 하자. 

![lottie_ver_badge](https://maven-badges.herokuapp.com/maven-central/com.airbnb.android/lottie/badge.svg)

#### 1.1 샘플 앱 

샘플앱은 직접 빌드 하거나 [google play store](https://play.google.com/store/apps/details?id=com.airbnb.lottie)에서 다운로드 받아 설치해서 사용해볼수 있다. 

### 2. 코어 클래스들 

- `LottieAnimationView` : `ImageView`를 확장하며 Lottie애니메이션을 불러오는 가장 기본적인 방법이다. 
- `LottieDrawable` : `LottieAnimationView`와 동일한 API를 갖고 있지만 원하는 모든 View를 대상으로 사용할 수 있다. 
- `LottieComposition` : 애니메이션을 반복하기 위한 상태를 갖지 않는(stateless) 모델 이다. `LottieCompositionFactory`로 만들고 `LottieDrawable`또는 `LottieAnimationView`에 설정할 수 있다. 

### 3. 애니메이션 불러오기

Lottie는 API 16이상을 지원한다. Lottie 애니메이션은 아래의 방법들로 불러와 사용할 수 있다. 

- `src/main/res/raw`디렉터리에 존재 하는 json 애니메이션 파일. 
- `src/main/assets`디렉터리에 존재하는 json 파일. 
- `src/main/assets`디렉터리에 존재하는 zip 압축 파일. [images docs](http://airbnb.io/lottie/#/android?id=images)에서 더 자세한 정보를 볼 수 있다. 
- json혹은 zip파일의 url. 
- json문자열. 문자열의 소스는 네트워크 스택을 포함하여 무엇이든 될 수 있다. 
- json혹은 zip파일에 대한 `InputStream`.

#### 3.1 레이아웃 XML에서 애니메이션뷰 사용하기

XML레이아웃에서 가장 간단하게 애니메이션을 보여주기위한 방법으로는 `LottieAnimationView`를 사용하는 방법이다. 또한 xml에서 문자열 이름을 사용하는 대신 `R`을 통해 애니메이션에 대한 정적 참조를 사용할 수 있으므로, `lottie_rawRes`xml 프로퍼티를 사용하는것이 좋다. 

```xml
<com.airbnb.lottie.LottieAnimationView
        android:id="@+id/animation_view"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"

        app:lottie_rawRes="@raw/hello_world"
        // or
        app:lottie_fileName="hello_world.json"

        // Loop indefinitely
        app:lottie_loop="true"
        // Start playing as soon as the animation is loaded
        app:lottie_autoPlay="true" />
```

위 예제에서는 `app:lottie_rawRes`프로퍼티를 이용해 `/res/raw/hello_world`애니메이션을 불러오는 방법과 `app:lottie_fileName`프로퍼티를 이용해 직접 json파일 이름을 문자열로 명시 하여 불러오는 방법이다. 

#### 3.2 코드를 이용한 애니메이션의 설정

`LottieAnimationView`에서 직접 많은 Lottie의 API를 사용할 수 있다. 그에 대해선 API에 대한 클래스 참조를 확인 하도록 하자.

#### 3.3 애니메이션의 캐시 처리

모든 Lottie애니메이션은 기본적으로 LRU캐시로 캐시 된다. `res/raw`또는 `/assets`에서 불러온 애니메이션에 대해 기본 캐시 키가 생성된다. 다른 API로는 캐시 키를 직접 설정해야 한다. `RecyclerView`의 좋아요와 같은 하트 애니메이션과 같이 애니메이션에 대해 여러 애니메이션의 요청을 병렬로 실행하는 경우 후속되는 요청은 기존 작업에 대해 참조 하므로 한번만 호출 하면 된다. (이는 Lottie의 버전이 2.6.0 이상에서만 해당 된다)

### 4. `ImageView`에서 Lottie로 마이그레이션 하기

#### 4.1. 기존 애니메이션 이미지 설정 방법

png 또는 벡터 드로어블(xml)을 `/res/drawable`에 넣고 레이아웃에 추가 한다. 

```xml
<ImageView
        android:id="@+id/image_view"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:scaleType="centerCrop"
        android:src="@drawable/your_drawable" />
```

#### 4.2 Lottie의 애니메이션 뷰 설정 방법 

json파일을 `/res/raw`에 넣고 레이아웃에 추가 한다. 

```xml
<com.airbnb.lottie.LottieAnimationView
        android:id="@+id/animation_view"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:scaleType="centerCrop"
        app:lottie_rawRes="@raw/your_animation"
        app:lottie_loop="true"
        app:lottie_autoPlay="true" />
```

`LottieAnimationView`는 `ImageView`를 확장하고 있으므로 Drawable을 사용하는 경우에도 `ImageView`를 `LottieAnimationView`로 교체하여 적용할 수 있다. 