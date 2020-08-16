## Glide 6 - Configuration (1/2)

Glide에 대한 숙련이 필요하다고 느낀 요즘, [Glide v4 Documentation](https://bumptech.github.io/glide/)를 참고 하여 내용을 번역하면서 필요한 내용들 위주로 정리 하였다.

추가로 Glide가 Java로 작성되어 있다 보니 Kotlin으로 적용 하면서 불편한점을 확장함수 등 으로 정리 해서 좀 더 직관적이고 관리하기 좋은 코드로 만들고 같이 정리 하려 함 :) 

- Configuration을 "설정"으로 작성하였음 
- Configuration본문의 내용이 방대해서 2개로 나뉘었음. 

### 1. [Configuration](https://bumptech.github.io/glide/doc/configuration.html)

Glide 4.9.0버전 이후 Glide의 설정을 변경하거나 확장해서 사용할 필요가 있을 수 있다.  

Application에서 아래와 같은 상황에는 Configuration의 설정이 필요하다. 

- 하나 이상의 통합 라이브러리 사용
- Glide의 설정 변경 (디스크 캐시의 사이즈, 위치 변경 혹은 메모리 캐시 사이즈 변경 등)
- Glide의 API 확장 

하지만, 라이브러리의 경우에는 라이브러리에 하나 이상의 컴포넌트를 등록하려는 경우에만 설정이 필요하다. 

#### 1.1 Applications

통합 라이브러리 와 / 혹은 Glide의 API 확장을 사용하려는 어플리케이션은 아래의 내용을 충족해야 한다. 

1. 단 하나의 `AppGlideModule`클래스의 확장및 구현
2. 최소 하나 이상의 `LibraryGlideModule`의 선택적 구현
3. `AppGlideModule`과 구현된 모든 `LibraryGlideModule`에 `@GlideModule`어노테이션 추가
4. Glide의 어노테이션 프로세서 라이브러리 종속 추가
5. `AppGlideModules`에 대한 프로가드 keep 추가

[Flickr 샘플 앱](https://github.com/bumptech/glide/blob/master/samples/flickr/src/main/java/com/bumptech/glide/samples/flickr/FlickrGlideModule.java)의 `AppGlideModule`예제를 참고 하면 아래와 같다. 

```java
@GlideModule
public class FlickrGlideModule extends AppGlideModule {
  @Override
  public void registerComponents(Context context, Glide glide, Registry registry) {
    registry.append(Photo.class, InputStream.class, new FlickrModelLoader.Factory());
  }
}
```
Glide의 어노테이션 프로세서의 종속을 gradle파일에 추가 한다. 

```
compile 'com.github.bumptech.glide:annotations:4.11.0'
annotationProcessor 'com.github.bumptech.glide:compiler:4.11.0'
```

- 필요에 따라 `compile`을 `implementation`으로 변경 한다.
- kapt를 사용한다면 `annotationProcessor`를 `kapt`로 변경한다.

마지막으로, `AppGlideModule`클래스의 프로가드 설정에 `keep`을 적용 하여 난독화를 하지 않게 해준다. 

```
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl
```

#### 1.2 Libraries 

사용자 커스텀 컴포넌트를 등록하지 않은 라이브러리 프로젝트의 경우, 이 페이지의 내용을 건너뛸 수 있다. 

라이브러리에 `ModelLoader`와 같은 사용자 커스텀 컴포넌트를 등록한 경우 아래와 같은 내용을 수행할 수 있다. 

1. 새로운 컴포넌트를 등록한 하나 이상의 `LibraryGlideModule`확장 및 구현 
2. 모든 `LibraryGlideModule`에 `@GlideModule`어노테이션이 추가 되어야 함
3. Glide의 어노테이션 프로세서 라이브러리 종속 추가

예제로, Glide의 [OkHttp 통합 라이브러리](https://github.com/bumptech/glide/blob/master/integration/okhttp3/src/main/java/com/bumptech/glide/integration/okhttp3/OkHttpLibraryGlideModule.java)의 적용 예제를 참고 하면 아래와 같다. 

```java
@GlideModule
public final class OkHttpLibraryGlideModule extends LibraryGlideModule {
  @Override
  public void registerComponents(Context context, Glide glide, Registry registry) {
    registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory());
  }
}
```

`@GlideModule`어노테이션을 사용하기 위해선 아래와 같은 어노테이션 프로세서 라이브러리 종속을 gradle에 추가해 준다.

```
compile 'com.github.bumptech.glide:annotations:4.11.0'
```

##### 1.2.1 라이브러리에서 `AppGlideModule`피하기 

라이브러리에는 `AppGlideModule`의 구현이 포함되지 않아야 한다. 그렇게 하면 라이브러리에 의존하는 모든 Application이 종속성을 관리하거나 Glide의 캐시 크기 및 위치 와 같은 옵션을 설정하는 것 을 방지 할 수 있다. 

또한 예를 들어 두개의 라이브러리에 `AppGlideModule`이 포함되어 있는 경우 Application이 둘 모두에 의존하려면 컴파일을 할 수가 없으며 하나 또는 다른 라이브러리를 선택할 수 밖에 없다. 

즉, 라이브러리는 Glide의 생성된 API를 사용할 수 없지만 Glide의 표준 `RequestBuilder`와 `RequestOptions`를 사용한 요청은 잘 동작 한다. 

### 2. Application Options

`AppGlideModule`을 상속받아 구현한 경우 Glide의 메모리, 디스크 캐시의 사용량을 완전히 제어할 수 있다. 

#### 2.1 메모리 캐시 

기본적으로 Glide는 `LruResourceCache`를 사용 한다. 이는 `MemoryCache`인터페이스의 구현이며 고정된 크기의 메모리와 Lru알고리즘에 따라 제거되는 페이지를 제어할 수 있다. `LruResourceCache`의 크기는 `MemorySizeCalculator`클래스에 의해 결정되며 이는 장치의 RAM용량이 낮거나 화면 해상도 여부에 상관없이 장치의 메모리 클래스만 확인 한다. 

- Lru알고리즘은 페이징 으로 메모리를 관리 하는 "페이지"에 대한 교체 알고리즘으로서 가장 오래동안 참조하지 않은 페이지를 새로운 페이지로 교체 한다. Lru알고리즘의 상세는 구글을 검색해 보자 ;)

Application에서는 `MemoryCache`사이즈fmf `AppGlideModule`의 `applyOptions(Context, GlideBuilder)`메소드를 통해 `MemorySizeCalculator`로 설정할 수 있다. 

```kotlin
@GlideModule
class MyAppGlideModule: AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val calculator: MemorySizeCalculator = MemorySizeCalculator.Builder(context)
            .setMemoryCacheScreens(2)
            .build()
        builder.setMemoryCache(LruResourceCache(calculator.getMemoryCacheSize()))
    }
}
```

메모리 캐시크기를 직접 재정의 할 수도 있다. 

```kotlin
@GlideModule
class MyAppGlideModule: AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val memCacheSizeByte = 1024 * 1024 * 20     // 20mb
        builder.setMemoryCache(LruResourceCache(memCacheSizeByte));
    }
}
```

사용자 정의 `MemoryCache`를 적용할 수 도 있다.

```kotlin
@GlideModule
class MyAppGlideModule: AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setMemoryCache(MyCustomAppMemoryCacheImpl())
    }
}
```

#### 2.2 Bitmap pool 

Glide에서는 `LruBitmapPool`을 기본 `BitmapPool`로 사용 한다. `LruBitmapPool`은 고정된 메모리크기와 LRU 페이지 제거를 수행한다. 메모리 기본 크기는 장치의 화면 크기와 density, 메모리 클래스 및 `isLowRamDevice()`메소드의 반환값을 기반으로 정해진다. 메모리 크기의 계산은 Glide의 `MemoryCache`에 대해 메모리 캐시 사이즈가 정해지는 방식과 유사하게 Glide의 `MemorySizeCalculator`에 의해 수행 된다. 

Application은 `MemorySizeCalculator`를 구성하여 `applyOptions(Context, GlideBuilder)`메소드를 사용하여 `AppGlideModule`에서 `BitmapPool`크기를 사용자 지정할 수 있다. 

```kotlin
@GlideModule
class MyAppGlideModule: AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val calculator: MemorySizeCalculator = MemorySizeCalculator.Builder(context)
            .setMemoryCacheScreens(2)
            .build()
        builder.setBitmapPool(LruBitmapPool(calculator.getBitmapPoolSize()))
    }
}
```

- 이 외 예제코드는 이전 (1)의 예제와 비슷하지만 `setBitmapPool()`메소드를 통해 설정하는 부분만 다르다. 그래서 생략 하였음 

#### 2.3 Disk Cache

Glide에서는 `DiskLruCacheWrapper`를 기본 `DiskCache`로 사용 한다. `DiskLruCacheWrapper`는 고정된 디스크 캐시이며 Lru제거를 사용 한다. 기본 디스크 캐시 사이즈는 `250MB`이며 어플리케이션의 특정 디렉토리 내 cache폴더를 만들어 저장 한다. 

Application에서는 외부 저장소등 으로 위치를 변경할 수도 있다. 변경 하는 방법은 이전 메모리 캐시와 동일 하게 `AppGlideModule`을 상속한 클래스의 `applyOptions(Context, GlideBuilder)`메소드를 통해서 할 수 있다. 

```kotlin
@GlideModule
class MyAppGlideModule: AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDiskCache(ExternalCacheDiskCacheFactory(context))
    }
}
```

디스크 캐시 사이즈를 지정할 수 있다. 

```kotlin
@GlideModule
class MyAppGlideModule: AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val diskCacheSizeByte = 1024 * 1024 * 100     // 100mb
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, diskCacheSizeByte));
    }
}
```

디스크 캐시를 할 저장소를 외, 내부로 변경할 수 있다. 

```kotlin
@GlideModule
class MyAppGlideModule: AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val diskCacheSizeByte = 1024 * 1024 * 100     // 100mb
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, "cacheFolderName", diskCacheSizeByte));
    }
}
```

Application은 자체적으로 `DiskCache`인터페이스를 구현한 클래스의 인스턴스를 생성하기 위해 `DiskCache.Factory`를 사용한다. `Factory`인터페이스를 통해 백그라운드 스레드에서 `DiskCache`를 열어 캐시가 [StrictMode](https://developer.android.com/reference/android/os/StrictMode.html)을 위반하지 않고 대상 폴더의 존재여부를 확인 하는것 등과 같은 I/O를 수행할수 있도록 한다. 

- `StrictMode`란 메인스레드에서 디스크나 네트워크 접근 등의 비효율적 작업을 하려는 것 을 감지하여 앱이 문제없이 돌아가도록 도와주는 기능이다. 
- StrictMode을 통해 ANR을 방지 하고 디스크 I/O에서 병목현상을 방지 할 수 있다. 

```kotlin
@GlideModule
class MyAppGlideModule: AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDiskCache {  // object : Diskcache.Factory
            YourAppCustomDiskCache()
        }
    }
}
```

#### 2.4 기본 Request Options 

`RequestOptions`는 기본적으로 이미지 요청별로 지정이 되지만 `AppGlideModule`을 사용하여 Application에서 시작되는 모든 이미지 요청들에 적용할 기본 `RequestOptions`집합을 적용 할 수 있다. 

```kotlin
@GlideModule
class MyAppGlideModule: AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDefaultRequestOptions(
            RequestOptions()
                .format(DecodeFormat.PREFER_RGB_565)
                .disallowHardwareConfig()
        )
    }
}
```

`GlideBuilder`에서 `setDefaultRequestOptions()`메소드를 통해 적용된 옵션은 새 이미지요청을 생성하는 즉시 적용된다. 결과적으로 개별 이미지 요청에 적용된 옵션은 `GlideBuilder`에 설정된 옵션들을 재정의 한다. 

`RequstManagers`를 사용하면 특정 `RequestManager`로 시작된 모든 이미지 요청의 로드에 대해 기본 `RequestOptions`를 설정할 수 있다. 각 `Activity`및 `Fragment`는 자체 `RequestManager`를 가져오므로 `RequestManager`의 `applyDefaultRequestOptions()`메소드를 사용 하여 특정 `Activity`나 `Fragment`에만 적용되는 기본 `RequestOptions`를 설정할 수 있다. 

```kotlin
Glide.with(this)
    .applyDefaultRequestOptions(
        RequestOptions()
            .format(DecodeFormat.PREFER_RGB_565)
            .disallowHardwareConfig()
    )
```

`RequestManager`에는 `AppGlideModule`의 `GlideBuilder`또는 `RequestManager`를 통해 이전에 설정한 기존 `RequestOptions`를 완전히 대체하는 `setDefaultRequestOptions()`메소드도 있다. `setDefaultRequestOptions()`는 다른곳에서 설정한 중요한 기본값을 실수로 재정의해버릴 수 있기 떄문에 사용에 주의를 해야 한다. 그렇기 떄문에 `applyDefaultRequestOptions()`메소드를 사용하는게 사용하기에 더 안정적이고 직관적이여서 추천한다. 

#### 2.5 `UncaughtThrowableStrategy`

비트맵 이미지를 불러오고 있을때 예외가 발생한다면(예를 들어 `OutOfMemoryException`), Glide는 `GlideExcecutor`을 사용 한다. `UncaughtThrowableStrategy`의 기본 전략은 장치의 logcat에 예외 정보를 기록 하는 것 이다. 이러한 예외시 전략은 Glide 4.2.0부터 사용자 정의할 수 있다. 

```kotlin
@GlideModule
class MyAppGlideModule: AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val uncaughtStrategy: GlideExecutor.UncaughtThrowableStrategy = ...
        builder.setDiskCacheExecutor(newDiskCacheExecutor(uncaughtStrategy))
        builder.setResizeExecutor(newSourceExecutor(uncaughtStrategy))
    }
}
```

- `setResizeExecutor()`메소드는 글 작성 시점에 deprecated된 상태 이다.

#### 2.6 Log level

이미지 요청이 실패할때 Android의 `Log`클래스 값 중 하나와 함께 `setLogLevel()`을 이용해 로그의 레벨을 지정할 수 있다. 

```kotlin
@GlideModule
class MyAppGlideModule: AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setLogLevel(Log.DEBUG)
    }
}
```

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

