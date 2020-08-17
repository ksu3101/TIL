## Glide 7 - Configuration (2/2)

Glide에 대한 숙련이 필요하다고 느낀 요즘, [Glide v4 Documentation](https://bumptech.github.io/glide/)를 참고 하여 내용을 번역하면서 필요한 내용들 위주로 정리 하였다.

추가로 Glide가 Java로 작성되어 있다 보니 Kotlin으로 적용 하면서 불편한점을 확장함수 등 으로 정리 해서 좀 더 직관적이고 관리하기 좋은 코드로 만들고 같이 정리 하려 함 :) 

- Configuration을 "설정"으로 작성하였음 
- Configuration본문의 내용이 방대해서 2개로 나뉘었고 [이전 페이지](https://github.com/ksu3101/TIL/blob/master/Android/200816_android.md)에서 계속 됩니다. 

### 3. 컴포넌트의 등록 

Application과 라이브러리 모두 Glide의 기능을 확장하는 여러 컴포넌트들을 등록할 수 있다. 사용 가능한 컴포넌트들은 아래와 같다. 

1. `ModelLoader`는 Url들, Uri들 임의의 POJO들 과 같은 사용자 커스텀 모델들 및 `InputStream`이나 `FileDescriptor`등 데이터를 로드 한다. 
2. `ResourceDecoder`는 새로운 Drawable이나 Bitmap과 같은 리소스들 혹은 `InputStream`이나 `FileDescriptor`등을 디코딩 한다. 
3. `Encoder`는 `InputStream`이나 `FileDescriptor`등 데이터를 Glide의 디스크 캐시에 저장한다. 
4. `ResourceTranscoder`는 `BitmapResource`와 같은 리소스를 다른 타입의 리소스로 변환한다. 
5. `ResourceEndocer`는 `BitmapResource`나 `DrawableResource`와 같은 리소스를 Glide의 디스크 캐시에 저장한다. 

컴포넌트는 `AppGlideModules`및 `LibraryGlideModules`의 `registerComponents()`메소드에서 `Registry`클래스를 이용해 등록하면 된다. 

```kotlin
@GlideModule
class MyAppGlideModule: AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.append(...)
    }
}
```

`GlideModule`에는 여러개의 컴포넌트들을 등록 할 수 있다. 

#### 3.1 "로드"의 상세보기

기본적으로 Glide에 등록 된 컴포넌트와 모듈에 등록된 컴포넌트를 포함하여 등록된 컴포넌트 요소 세트는 이미 요청의 경로를 설정하는데 사용 된다. 각 이미지 요청은 `load()`에 제공된 모델에서 `as()`에 지정된 리소스 타입으로의 단계별 진행이다. 

이미지 요청에 대한 진행은 다음단계로 구성 된다.

1. Model -> Data (`ModelLoader`에 의해 처리 된다)
2. Data -> Resource (`ResourceDecoder`에 의해 처리 된다)
3. Resource -> Transcoded Resource -> (선택사항이며, `ResourceTranscoder`에 의해 처리 된다)

`Encoder`는 2단계 전에 데이터를 Glide의 디스크 캐시에 저장 할 수 있으며, `ResourceEndocer`는 3단계 전에 Glide의 디스크 캐시에 저장할 수 있다. 

#### 3.2 컴포넌트들의 순서 

`Registry`의 `prepend()`, `append()`및 `replace()`메소드를 이용해 Glide가 각 `ModelLoader`와 `ResourceDecoder`를 시도하려는 순서를 설정할 수 있다. 컴포넌트의 순서를 변경하면 모델 또는 데이터의 특정 하위 집합(IE 특정 유형의 Uri 또는 특정 이미지 포맷들)을 처리하는 컴포넌트를 등록할 수 있으며, 나머지를 처리 하기 위해 추가된 범용 컴포넌트를 사용할 수 있다. 

##### 3.2.1 `prepend()`

`ModelLoader`또는 `ResourceDecoder`가 실패할 경우 Glide의 기본 동작으로 돌아가려는 기존 데이터의 하위 집합을 처리하기 위해서는 `prepend()`메소드를 사용 한다. 

`prepend()`는 `ModelLoader`또는 `ResourceDecoder`가 이전에 등록 된 다른 모든 컴포넌트보다 먼저 호출되고 먼저 실행될 수 있는지 확인 한다. `ModelLoader`또는 `ResourceDecoder`가 `handles()`메소드에서 false를 반환하거나 실패한다면, 다른 모든 `ModelLoader`또는 `ResourceDecoder`가 등록된 순서대로 한번에 하나씩 호출되어 대체를 제공 한다. 

##### 3.2.2 `append()`

`append()`메소드를 사용하여 새로운 유형의 데이터를 처리 하거나 Glide의 기본 동작에 fallback을 추가 한다. `append()`메소드는 Glide의 기본값이 시도된후에만 `ModelLoader`또는 `ResourceDecoder`가 호출되도록 한다. Glide의 기본 컴포넌트가 처리하려는 하위 유형(예를 들어 특정 Uri혹은 하위 클래스)을 처리하려는 경우 `prepend()`메소드를 사용하여 Glide의 기본 구성 요소가 사용자 정의 구성 요소보다 먼저 리소스를 로드하지 않도록 해야 할 수 있다. 

##### 3.2.3 `replace()`

Glide의 기본 동작을 완전히 대체하고 실행되지 않도록 하려면 `replace()`메소드를 사용 한다. `replace()`는 주어진 모델 및 데이터 클래스를 처리하는 모든 `ModelLoader`를 제거한 다음 대신 `ModelLoader`를 추가한다. `replace()`는 특히 OkHttp또는 Volley와 같은 라이브러리로 Glide의 네트워크 로직을 교체할때 유용하다. 

#### 3.3 `ModelLoader`의 추가 

예를들어, 새로운 사용자 커스텀 Model개체에 대한 `InputStream`을 가져올 수 있는 `ModelLoader`를 추가하려면 다음을 수행한다. 

```kotlin
@GlideModule
class MyAppGlideModule: AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.append(Photo::class, InputStream::class, CustomModelLoader.Factory())
    }
}
```

`append()`메소드는 여기에서 안전하게 사용할 수 있다. `Photo.class`는 어플리케이션에 특정한 사용자 커스텀 모델 개체이므로, Glide에는 대체해야하는 기본 동작이 없음을 알고 있다. 

반대로 `BaseGlideUriLoader`에서 새로운 유형의 문자열 Url에 대한 처리를 추가하려면, `prepend()`메소드를 사용하여 `ModelLoader`가 Glide의 문자열에 대한 기본 `ModelLoader`보다 먼저 실행되도록 해야 한다. 

```kotlin
@GlideModule
class MyAppGlideModule: AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(String::class, InputStream::class, CustomUrlModelLoader.Factory())
    }
}
```

마지막으로, 네트워킹 라이브러리와 같은 특정 유형에 대한 Glide의 기본처리를 완전히 제거 하고 대체하려면 `replace()`메소드를 사용 한다.

```kotlin
@GlideModule
class MyAppGlideModule: AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.replace(GlideUrl::class, InputStream::class, OkHttpUrlLoader.Factory())
    }
}
```

### 4. 모듈 클래스와 어노테이션들 

Glide v4에는 `AppGlideModule`과 `LibraryGlideModule`의 두가지 클래스를 사용하여 Glide 싱글톤을 구성 한다. 두 클래스 모두 `ModelLoaders`, `ResourceDecoders`등과 같은 추가 컴포넌트를 등록할 수 있다. `AppGlideModules`만 캐시 구현 및 크기와 같은 앱 특정 설정을 구성할 수 있다. 

#### 4.1 `AppGlideModule`

어플리케이션이 `AppGlideModule`에서 메소드를 구현하거나 통합 라이브러리를 사용하려는 경우 모든 어플리케이션은 선택적으로 `AppGlideModule`의 구현을 추가할 수 있다. `AppGlideModule`의 구현은 Glide의 어노테이션 프로세서가 발견한 모든 `LibraryGlideModules`와 결합 된 단일 클래스를 생성할 수 있도록 하는 신호 역할을 한다. 

주어진 어플리케이션에는 `AppGlideModule`구현이 하나만 있을 수 있다. (그렇지 않다면 컴파일시 오류가 발생한다) 결과적으로 라이브러리는 `AppGlideModule`구현을 제공해서는 안된다. 

#### 4.2 `@GlideModule` 어노테이션 

Glide가 `AppGlideModule`및 `LibraryGlideModule`구현을 올바르게 검색하려면 두 클래스의 모든 구현에 `@GlideModule`어노테이션을 추가 해야 한다. 어노테이션을 통해 Glide의 어노테이션 프로세서는 컴파일시 모든 구현을 검색할 수 있다. 

#### 4.3 어노테이션 프로세서 

추가적으로 `AppGlideModule`과 `LibraryGlideModules`를 모두 검색하려면 모든 라이브러리와 어플리케이션이 Glide의 어노테이션 프로세서에 대한 종속성도 포함해야 한다. 

### 5. 충돌들 

어플리케이션은 여러 라이브러리에 종속될 수 있으며, 각 라이브러리에는 하나 이상의 `LibraryGlideModules`가 포함될 수 있습니다. 드물게 이러한 `LibraryGlideModules`는 충돌하는 옵션을 정의하거나 어플리케이션이 피하려는 동작을 포함 할 수있다. 어플리케이션은 `AppGlideModule`에 `@Excludes`어노테이션을 추가하여 이러한 충돌을 해결하거나 원치 않는 종속성을 피할 수 있다. 

예를 들어 피하고 싶은 `LibraryGlideModule`이 있는 라이브러리 `com.example.unwanted.GlideModule`에 의존한 경우, `

```kotlin
@Excludes(com.example.unwanted.GlideModule::class)
@GlideModule
class MyAppGlideModule: AppGlideModule() {
}
```

여러개의 모듈을 제외할 수 도 있다. 

```kotlin
@Excludes(com.example.unwanted.GlideModule::class,  com.example.conflicing.GlideModule::class)
@GlideModule
class MyAppGlideModule: AppGlideModule() {
}
```

### 6. 매니페스트 파싱

Glide v3의 `GlideModules`와의 하위 호환성을 유지하기 위해 Glide는 어플리케이션 및 포함된 라이브러리 모두에서 `AndroidManifest.xml`파일을 계속 파싱하고 메니페스트에 나열된 기존 `GlideModules`를 포함한다. 이 기능은 향후 버전에서 제거될 예정이지만 전환을 쉽게 하기 위해 아직은 유지중인 기능이다. 

이미 Glide v4 `AppGlideModule`및 `LibraryGlideModule`로 마이그레이션 한 경우 메니페스트 파싱을 완전히 비활성화 할 수 있다. 이렇게 하면 Glide의 초기 시작 시간을 개선하고 메타 데이터 구문 분석을 시도 할 때 발생할 수 있는 몇가지 잠재적 문제를 방지 할 수 있다. 메니페스트 파싱을 비활성화하려면 `AppGlideModule`구현에서 `isManifestParsingEnabled()`메소드를 재정의 하면 된다. 

```kotlin
@GlideModule
class MyAppGlideModule: AppGlideModule() {
    override fun isManifestParsingEnabled(): Boolean {
        return false
    }
}
```


