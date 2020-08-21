## Glide 10 - Debugging

Glide에 대한 숙련이 필요하다고 느낀 요즘, [Glide v4 Documentation](https://bumptech.github.io/glide/)를 참고 하여 내용을 번역하면서 필요한 내용들 위주로 정리 하였다.

추가로 Glide가 Java로 작성되어 있다 보니 Kotlin으로 적용 하면서 불편한점을 확장함수 등 으로 정리 해서 좀 더 직관적이고 관리하기 좋은 코드로 만들고 같이 정리 하려 함 :) 

### 1. [로컬 로그]](https://bumptech.github.io/glide/doc/debugging.html)

기기에 접근가능한 경우, adb logcat또는 IDE를 이용하여 로그를 읽어볼 수 있다. 아래 명령어를 사용하여 모든 태그들에 대한 로깅을 활성화 할 수도 있다. 

```
adb shell setprop log.tag.<tag_name> <VERBOSE|DEBUG>
```

`VERBOSE`로그는 더 장황하지만 유용한 정보들을 더 포함하고 있다. 태그에 따라 `VERBOSE`와 `DEBUG`를 모두 시도하여 어떤 정보가 최상의 정보를 제공하는지 확인 할 수 있다. 

#### 1.1 이미지 요청 에러

Glide의 이해하기 쉬운 높은수준의 로그는 `Glide`태그로 작성 된다. 

```
adb shell setprop log.tag.Glide DEBUG
```

Glide 태그는 성공 및 실패한 요청과 로그 수준에 따라 다른 세부 수준을 모두 기록 한다. 성공한 요청을 기록하려면 `VERBOSE`를 사용 해야 한다. `DEBUG`를 사용하면 더 자세한 오류메시지 까지 포함되어 기록 된다. 

`setLogLevel(Int)`메소드를 사용하여 Glide로그를 더 상세하게 제어할 수도 있다. 그리고 `setLogLevel()`을 사용하면 개발자 빌드에서 더 자세한 로그를 사용할 수 있지만, 릴리즈 빌드에서는 사용할 수 없다. 

#### 1.2 예상치 못한 캐시의 누락 

Glide의 `Engine`로그 태그는 요청이 수행되는 방법에 대한 세부 정보를 제공하고 해당 리소스를 저장하는데 사용되는 모든 메모리 캐시 키들을 포함한다. 한 위치의 메모리에 존재하는 이미지가 다른 위치에서 사용되지 않는 이유를 디버깅 하고 싶다면 `Engine`태그를 사용하여 캐시 키를 직접 비교하여 차이점을 확인 할 수 있다. 

각 요청이 시작된뒤 `Engine`태그는 이미지 요청이 캐시와 활성된 리소스, 기존 이미지 로드 또는 새로운 이미지 로드에서 완료 될 것임을 기록 한다. 

캐시는 리소스가 사용중이 아니었지만 메모리 캐시에서 사용할 수 있음을 의미한다. 

활성 리소스는 일반적으로 View에서 다른 대상에 의해 사용되고 있음을 의미 한다. 

기존 이미지 로드는 리소스를 메모리에서 사용할 수 없었지만 다른 대상이 이전에 동일한 리소스를 요청 했으며 로드가 이미 진행중임을 의미한다. 

마지막으로 새로운 이미지의 로드는 리소스가 메모리에 없거나 이미 로드된적이 없기 때문에 요청이 새 이미지를 로드하려함을 시작했다고 의미한다. 

#### 1.3 `RequestListener`와 사용자 정의 로그

프로그래밍 방식으로 오류와 성공한 이미지로드를 추적하고, 어플리케이션에서 이미지의 전체 캐시에 대한 적중률을 추적하거나 로컬 로그를 조금 더 세밀하게 제어하고 싶은 경우 `RequestListener`인터페이스를 사용하면 된다. `RequestListener`는 `RequestBuilder`의 `listener()`메소드를 사용하여 각 로드에 추가할 수 있다. 

```kotlin
Glide.with(context)
    .load("http://...")
    .listener(object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            Log.e(TAG, "Load failed", e)
            e?.let {
                for (t in e.rootCauses) {
                    Log.e(TAG, "Caused by", t)
                }
                it.logRootCauses(TAG)
            }
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            // 이미지 로드가 성공했을 경우 여기에서 로그 처리 한다 
            return false
        }
    })
    .into(imageView)
```

`GlideException`에는 여러개의 `Throwable`로 구성되어있을 수 있다. Glide의 각 로드에는 등록 된 구성요소(ModelLoader, ResourceDecoder, Encoder)를 사용하여 지정된 모델 (URL, 파일 등)에서 리소스(Bitmap, GifDrawable 등)를 로드하는데 사용할 수 있는 방법들이 있다. 각 `Throwable`은 `Glide`의 컴포넌트들이 실패한 이유를 설명해줄 것 이다. 특정 요청이 실패한 경우 이유를 이해하기 위해서는 모든 예외를 검사해야 할 수 있다. 

예를 들어 URL을 대상으로 로드 중일때 네트워크 오류로 인하여 로드에 실패했음은 `HttpException`을 찾아서 원인을 분석할 수 있다. 

```kotlin
e?.let {
    for (t in e.rootCauses) {
        if (t is HttpException) {
            Log.e(TAG, "Request failed due to HttpException!", t)
            break
        }     
    }
    // ... 
}
```

생성한 `RequestListener`인스턴스는 여러 이미지 로드에 동일하게 재사용할 수 있다. 이 경우 새로운 인스턴스를 생성하는데 드는 비용을 감소시켜 줄 것이다. 

### 2. 누락된 이미지와 로컬 로그 

어떤 경우에는 이미지가 로드되지 않고 `Glide`태그 또는 `Engine`태그가 있는 로그들이 이미지 요청에 대해 기록되지 않을 수도 있다. 이떄 가능한 원인으로는 아래와 같다. 

#### 2.1 이미지 요청을 실패 한 경우 

이미지 요청에 대해 `into()`메소드 또는 `submit()`을 호출하고 있는지 확인한다. 두 방법중 하나를 사용하지 않는다면 Glide에서는 이미지 로드를 시작하지 않는다. 

#### 2.2 이미지를 로드할 대상의 사이즈가 누락된 경우 

`into()`또는 `submit()`을 호출하고 있는데 여전히 로그가 출력되지 않는다면, 가장 가능성이 높은 경우로서 Glide가 로드하려는 `View`또는 `Target`의 크기를 결정할 수 없는 경우이다. 

##### 2.2.1 사용자 정의 Target

사용자 지정 `Target`을 사용하는 경우 `getSize()`를 구현했고 너비와 높이가 0이 아닌 지정된 콜백을 호출 했거나, `ViewTarget`과 같은 `Target`을 자식 클래스로 지정했는지를 확인 해 본다. 

##### 2.2.2 View

`View`를 대상으로 로드한 경우 가능성이 높은 경우는, View가 레이아웃단계를 거치지 않거나 너비나 높이가 0일 때 이다. 뷰의 `visibility`속성이 `View.GONE`으로 설정되어 있거나 상위뷰에 연결되지 않은 경우 뷰가 레이아웃 단계를 통과하지 못할 수 있다. View혹은 부모View의 너비와 높이에 대해 `wrap_content`및 `match_parent`의 특정 조합이 있는 경우 View는 유효하지 않거나 0의 너비와 높이를 받을 수 있다. 이 경우 0이 아닌 고정된 크기를 View에 제공하거나 특정 크기를 Glide에 전달하여 `override(Int, Int)`API로 요청에 적용할 수 있다. 

### 3. 메모리 부족 에러 (Out of memory erros)

대부분의 OOM오류는 Glide가 아닌 Glide를 사용하는 어플리케이션의 문제로 인한것 이다. 

어플리케이션에서 OOM의 일반적인 원인은 두가지 이다. 

1. 지나치게 큰 객체의 메모리 할당
2. 메모리 누수 (할당된 상태이며 해제되지 않은 객체)

#### 3.1 지나치게 큰 객체의 메모리 할당 

단일 페이지 혹은 이미지를 로드시 OOM이 발생하는 경우 어플리케이션이 불필요하게 큰 이미지를 로드하는것 일 수도 있다. 

비트맵이 이미지를 표시하는데 필요한 메모리양은 "너비 * 높이 * 픽셀당 바이트"이다. 픽셀당 바이트수는 이미지를 표시하는데 사용하는 `Bitmap.Config`에 따라 다르지만, 일반적으로 `ARGB_8888`비트맵에서는 픽셀당 4바이트가 필요하다. 결과적으로 1080p이미지를 로드하려면 8mb의 램이 필요하다. 이미지가 크면 클 수록 더 많은 램이 필요하므로 12메가 픽셀 이미지에는 상당히 방대한 48mb가 필요하다.

Glide에는 `Target`, `ImageView`에 대한 로드에 `override()`옵션에서 제공하는 크기에 따라 자동으로 이미지를 다운 샘플링 한다. Glide에서 지나치게 큰 할당이 진행될 경우 일반적으로 `Target`또는 `override()`의 크기가 너무 크거나 큰 이미지와 함께 `Target.SIZE_ORIGINAL`을 사용하고 있음을 의미한다. 

지나치게 큰 할당을 수정하려면 `Target.SIZE_ORIGINAL`을 피하고 `ImageView`의 크기 또는 `override()`를 통해 Glide에 제공될 이미지의 사이즈가 적합한지 확인 한다. 

#### 3.2 메모리 누수 (Memory leak)

동일한 작업을 반복할때 메모리의 사용량은 점차적으로 늘어나고 OOM이 발생할 수 있는데 이 때 메모리 누수가 발생할 수 있다. 

[Android의 문서](https://developer.android.com/studio/profile/investigate-ram.html)에서는 메모리 사용에 대한 추적 및 디버깅에 대해 많은 정보를 제공 한다. 메모리 누수를 조사하기 위해서 힙(heap)을 캡쳐하여 덤프하고 더이상 사용하지 않지만 살아남아있는 Activity, Fragment또는 기타 객체를 찾아야 한다. 

메모리 누수를 수정하기 위해서는 Activity와 Fragment의 수명주기에 적절한 지점에서 필요없는 참조를 제거 하여 필요없는 인스턴스를 유지하지 않도록 한다. 힙 덤프(heap dump)를 사용하면 어플리케이션에서 불필요한 참조를 찾아 제거할 수 있도록 도와준다. 

MAT또는 다른 메모리 분석기를 사용하여 모든 `Bitmap`객체에 대한 약함참조를 제외한 최단 경롤르 나열한 다음 의심스러운 참조 체인을 찾는것으로 시작하는것이 도움이 되는 경우가 많다. 또한 메모리 분석기에서 검색하여 각 Activity의 인스턴스가 한번만 있고 각 Fragment의 예상 인스턴스 갯수만 존재하는지 확인 할 수 있다. 

### 4. 그 외 일반적인 이슈들

#### 4.1 “You can’t start or clear loads in RequestListener or Target callbacks”

`onResourceReady()`또는 `Target`, `RequestListener`의 `onLoadFailed()`등 에서 새로운 로드를 시작하려고 하면 Glide에서 예외가 발생 한다. 재활용 되는 이미지 로드를 처리하기가 어렵기 떄문에 예외를 throw하는것 이다. 

다행히 이런경우는 수정하기 쉽다. Glide 4.3.0부터 `error()`메소드를 사용하면 된다. `error()`는 기본 요청이 실패한 경우에 새요청을 시작하는 `RequestBuilder`를 사용 한다. 

```kotlin
Glide.with(context)
    .load("http://...")
    .error(
        Glide.with(context)
            .load(failBackUrl)
    ).into(imageView)
```

Glide 4.3.0이전에는 안드로이드의 `Handler`를 사용하여 요청과 함께 `Runnable`을 시작하도록 할 수 있다. 

```kotlin
Glide.with(this)
    .load("")
    .listener(object : RequestListener<Drawable> {
        // ...

        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            handler.post {
                Glide.with(context)
                    .load(failbackUrl)
                    .into(imageView)
            }
            // ...
        }
    })
    .into(imageView)
```

#### 4.2 “cannot resolve symbol ‘GlideApp’”

생성된 API를 사용할 때 어노테이션 프로세서가 Glide의 API를 생성하지 못하는 오류가 발생할 수 있다. 때로는 이러한 오류가 설정과 관련이 있지만 다른 경우에는 완전히 관련없을수도 있다. 

종종 관련없는 오류는 근본적인 원인이 아닌 오류 메시지의 갯수로 숨겨진다. 다른 오류가 많아서 빌드 로그에서 원인을 찾을 수 없는것 이다. 이 문제가 발생하였고 Gradle을 사용중인 경우 아래를 Gradle에 추가 하여 gradle이 출력할 오류 메시지의 수를 늘려 보도록 하자. 

```
allprojects {
  gradle.projectsEvaluated {
    tasks.withType(JavaCompile) {
        options.compilerArgs << "-Xmaxerrs" << "1000"
    }
  }
}
```