## Glide 7 - Caching

Glide에 대한 숙련이 필요하다고 느낀 요즘, [Glide v4 Documentation](https://bumptech.github.io/glide/)를 참고 하여 내용을 번역하면서 필요한 내용들 위주로 정리 하였다.

추가로 Glide가 Java로 작성되어 있다 보니 Kotlin으로 적용 하면서 불편한점을 확장함수 등 으로 정리 해서 좀 더 직관적이고 관리하기 좋은 코드로 만들고 같이 정리 하려 함 :) 

### 1. [Glide에서의 캐시 처리](https://bumptech.github.io/glide/doc/caching.html)

기본적으로, Glide는 이미지에 대한 새 요청을 시작하기전에 여러 계층에 존재할 수 있는 캐시들을 확인 한다. 

1. 활성된 리소스 : 요청된 이미지가 지금 다른 뷰에서 보여지고 있는 중 인가? 
2. 메모리 캐시 : 요청된 이미지가 최근에 불리워진 적 있고 여전히 메모리에 존재 하는가? 
3. 리소스 : 요청된 이미지는 이전에 디코딩, 변환 및 디스크 캐시에 저장된 적이 있는가? 
4. 데이터 : 요청된 이미지가 이전에 디스크 캐시에 저장되었는가? 

처음의 두 단계는 리소스가 메모리에 있는지 확인한뒤 존재한다면 즉시 이미지를 얻을 수 있다. 다음의 두번째 단계는 이미지가 디스크에 있는지 확인하고 비동기방법으로 빠르게 반환될 수 있는지 확인 한 뒤 이미지를 얻는방법이다. 

이 4개의 단계에서 이미지를 찾지 못했다면, Glide는 원본의 소스(Uri, Url등)를 통해 데이터를 얻을 것 이다. 

Glide의 캐시에 대한 크기, 위치등의 상세 설정을 수정하려면 [Configurations](https://github.com/ksu3101/TIL/blob/master/Android/200816_android.md)페이지를 참고 하면 된다. 

### 2. 캐시 키 

Glide v4에서 모든 캐시의 키는 적어도 아래 2개를 포함해야 한다. 

1. 이미지 요청에 대한 모델(File, Uri, Url). 사용자 커스텀 모델을 사용한 경우 `hashCode()`와 `equals()`메소드들을 올바르게 재정의 해서 구현해야 한다. 
2. 선택적으로 사용할 수 있는 `Signature`

사실, 1~3단계의 캐시 키(위 활성 리소스, 메모리 케시, 리소스 디스크 캐시)에는 다음과 같은 여러 데이터도 포함된다. 

1. 이미지의 width, height 
2. `Transformation` 존재시 해당 객체
3. 추가된 `Options`객체
4. 요청 대상의 데이터 타입 (Bitmap, GIF 등)

활성 리소스 및 메모리 캐시에 사용되는 키는 비트맵 또는 기타 디코딩 시간 전용 매개 변수의 구성에 영향을 주는 것과 같은 메모리 옵션을 설정해야 하므로 다른 리소스 디스크 캐시에 사용되는 키와 약간 다를수 밖에 없다. 

디스크 캐시 에서는 디스크 캐시 키의 이름을 생성하기 위해 키의 개별 요소를 해시하여 단일 문자열 키를 만든다음 디스크 캐시에서 파일 이름으로 사용 한다.

### 3. 캐시 상세 설정

Glide에서는 이미지 요청별로 이미지 로드가 Glide의 캐시와 상호작용할 수 있는 방식을 설정 방법에 대한 다양한 옵션들을 제공하고 있다. 

#### 3.1 디스크 캐시 전략 

`DiskCacheStrategy`는 `diskCacheStrategy()`메소드와 함께 요청별로 적용할 수 있다. 사용 가능한 전략을 사용하면 이미지 로드가 디스크 캐시를 사용하거나 쓰는 것을 방지하거나 로드를 뒷받침하는 수정되지 않은 원본 데이터만 캐시하거나 이미지 로드에 의해 생성된 변환된 썸네일 이미지만 혹은 둘 다 캐시하도록 선택 할 수 있다. 

디스크 캐시에서 기본 전략인 `AUTOMATIC`은 로컬 및 원격 이미지에 대하 최적의 전략을 사용 한다. `AUTOMATIC`은 원격 데이터들을 이미지 로드 할 때(예로 URL들) 뒷받침하는 수정되지 않은 데이터만 저장 한다. 원격 데이터를 다운로드 하는 것은 이미 디스크에 있는 데이터의 크기를 조정하는것 에 비해 많은 비용이 들어가기 때문이다. 로컬 데이터의 경우 `AUTOMATIC`은 두번째 썸네일 이미지의 크기 또는 유형을 생성해야 하는 경우 원본 데이터를 검색하는 것 이 저렴하기 때문에 변환된 썸네일만 저장 한다. 

`DiskCacheStrategy`를 적용하는 방법은 아래와 같다. 

```kotlin
Glide.with(context)
    .load("https://...")
    .diskCacheStrategy(DiskCacheStrategy.ALL)
    .into(imageView)
```

#### 3.2 캐시에서만 이미지 로드 하기 

필요에 따라 이미지가 캐시에 존재하지 않는 경우 이미지로드가 실패할 수 있다. 이렇게 하려면 `onlyRetrieveFromCache()`메소드를 사용 하면 된다. 

```kotlin
Glide.with(context)
    .load("https://...")
    .onlyRetrieveFromCache(true)
    .into(imageView)
```

이미지가 메모리 캐시 또는 디스크 캐시에서 발견되면 해당 이미지를 로드하게 된다. 하지만 `onlyRetrieveFromCache()`의 옵션이 `true`로 설정되었다면 이미지 가 캐시에 존재하지 않을경우 이미지로드가 실패한다. 

#### 3.3 캐시를 하지 않기

특정 이미지요청이 디스크 캐시나 메모리 캐시 또는 둘 모두를 건너 뛰도록 하려면 Glide에서는 몇가지 대안을 제공 하고 있다. 

메모리 캐시만 건너 뛰려면, `skipMemoryCache()`메소드를 사용 한다. 

```kotlin
Glide.with(context)
    .load("http://...")
    .skipMemoryCache(true)
    .into(imageView)
```

디스크 캐시만 건너 뛰려면, `diskCacheStrategy()`에 `NONE`옵션을 설정 한다. 

```kotlin
Glide.with(context)
    .load("http://...")
    .diskCacheStrategy(DiskCacheStrategy.NONE)
    .into(imageView)
```

두 캐시를 모두 사용하지 않는다면 두개의 옵션을 모두 추가한다. 

```kotlin
Glide.with(context)
    .load("http://...")
    .diskCacheStrategy(DiskCacheStrategy.NONE)
    .skipMemoryCache(true)
    .into(imageView)
```

### 4. 캐시 무효화

디스크 캐시는 해시처리된 키 이므로 특정 URL또는 파일 경로에 해당하는 디스크의 모든 캐시된 파일을 단순히 삭제할 수 있는 방법이 없다. 원본 이미지를 로드하거나 캐시 할 수만 있다면 문제는 더 간단할 수 있지만, Glide는 썸네일도 캐시하고 다양한 변환을 제공하기 때문에 캐시에 새 파일이 생성되고 캐시 된 모든 버전을 추적하고 삭제 한다. 

실제로 캐시 파일을 무효화하는 가장 좋은 방법은 가능한 경우 컨텐츠가 변경 될 때(url, Uri, 파일 경로 등) 식별자를 변경 하는 것 이다. 

#### 4.1 사용자 캐시 무효화 설정 

식별자 변경이 어렵거나 불가능한 경우가 많기 때문에 Glide는 사용자가 제어하는 추가 데이터를 캐시 키에 혼합할 수 있는 `signature()` API도 제공 한다. 서명(Signatures)은 미디어 저장소 컨텐츠 및 일부 버전 관리 메타 데이터를 유지할 수 있는 모든 컨텐츠에 대해 잘 작동한다. 

- 미디어 스토어 컨텐츠 : 이 경우 Glide의 `MediaStoreSignature`클래스를 서명으로 사용할 수 있다. `MediaStoreSignature`를 사용하면 미디어 저장소 항목의 수정 날짜시간, MIME유형 및 Orientation을 캐시 키에 더할 수 있다. 이 세가지 속성은 편집 및 업데이트를 안정적으로 포착하므로, 미디어 저장소에 썸내일을 캐시할 수 있다. 
- 파일들 : `ObjectKey`를 사용하여 파일의 수정 날짜 시간을 추가 할 수 있다. 
- Url들 : Url을 무효화 하는 가장 좋은 방법은 서버가 URL을 변경하고 URL의 컨텐츠가 변경될 때 클라이언트를 업데이트 하는지 확인하는 것 이지만, 대신 `ObjectKey`를 사용하여 임의의 메타 데이터(예로 버전과 같은 번호)를 추가할 수 있다. 

서명을 추가하는 방법은 아래 예제를 참고하자. 

```kotlin
Glide.with(context)
    .load("http://...")
    .signature(ObjectKey(yourVersionMetadata))
    .into(imageView)
```

미디어 스토어 서명은 아래처럼 사용 한다. 

```kotlin
Glide.with(context)
    .load("http://...")
    .signature(MediaStoreSignature(mimeType, dateModified, orientation))
    .into(imageView)
```

다른 방법으로 `Key`인터페이스를 구현하여 사용자 서명을 정의할 수 있다. 이 때, `equals()`, `hashCode()`, `updateDiskCacheKey()`메소드들을 구현해야 한다. 

```kotlin
class IntegerVersionSignature(
    val currentVer: Int
): Key {
    override fun equals(other: Any?): Boolean {
        if (other is IntegerVersionSignature) {
            return currentVer == other.currentVer
        }
        return false
    }

    override fun hashCode(): Int {
        return currentVer
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ByteBuffer.allocate(Integer.SIZE)
            .putInt(currentVer)
            .array()
        )
    }
}
```

성능 저하를 방지하려면 이미지를 로딩하려 할 때 사용할 수 있도록 백그라운드 스레드에서 버전관리 메타 데이터들을 일괄 로드하는것이 좋다. 

다른 모든 방법이 실패하고 식별자를 변경하거나 적절한 버전 메타 데이터를 추적할 수 없는 경우 `diskCacheStrategy()`메소드 및 `DiskCacheStrategy.NONE`을 사용하여 디스크 캐싱을 완전히 비활성화 할 수도 있다. 

### 5. 리소스의 관리 

Glide의 디스크 및 메모리 캐시는 LRU알고리즘을 적용하고 있다. 즉, 한도에 도달하거나 한도에 가깝게 계속 사용할 때 까지 더 많은 메모리와 디스크 공간을 차지 한다. 유연성을 위해 Glide는 어플리케이션에서 사용하는 리소스를 관리할 수 있는 몇가지 방법을 제공 한다. 

더 큰 메모리 캐시, 비트맵 풀 및 디스크 캐시의 크기는 일반적으론 어느 정도 까지는 다소 나은 성능을 제공 한다. 캐시 사이즈를 변경하는 경우 변경 전과 후의 성능을 신중하게 측정하여 성능과 크기의 절충이 합리적인지 여부를 확인 해야 한다. 

#### 5.1 메모리 캐시의 관리 

기본적으로 Glide의 메모리 캐시와 `BitmapPool`은 `ComponentCallBacks2`에 응답하고 프레임워크가 제공하는 수준에 따라 다양한 수준으로 컨텐츠를 자동으로 제거 한다. 따라서 일반적으로 캐시 또는 `BitmapPool`을 동적으로 모니터링 하거나 지울 필요가 없다. 그러나 필요한 경우 Glide는 몇가지의 수동 옵션들을 제공 한다. 

##### 5.1.1 영구적인 캐시 크기의 변경 

어플리케이션 전체에서 Glide에 사용할 수 있는 RAM의 크기를 변경하려면 [Configurations](https://github.com/ksu3101/TIL/blob/master/Android/200816_android.md)을 참고 하자. 

##### 5.1.2 임시적인 캐시 크기의 변경

Glide가 앱의 특정 부분에서 더 많거나 적은 메모리를 일시적으로 사용하도록 허용하려면 `setMemoryCategory()`메소드를 사용 할 수 있다. 

```kotlin
Glide.with(context)
    .setMemoryCategory(MemoryCategory.LOW)
    // 혹은 
    setMemoryCategory(MemoryCategory.HIGH)
```

앱의 메모리 또는 성능에 민감한 영역을 떠날 때 메모리 크기를 다시 기본값으로 지정해주어야 한다. 

```kotlin
Glide.with(context)
    .setMemoryCategory(MemoryCategory.NORMAL)
```

##### 5.1.3 메모리 캐시 지우기

Glide의 메모리 캐시 및 `BitmapPool`을 간단히 지우려면 `clearMemory()`메소드를 사용 한다. 

```kotlin
Glide.with(context)
    .clearMemory()
```

#### 5.2 디스크 캐시의 관리 

Glide는 런타임시 디스크 캐시 크기에 대한 제한된 제어만 제공하지만, 크기와 구성에 대해서는 `AppGlideModule`에서 변경할 수 있다. 

##### 5.2.1 영구적인 디스크 캐시의 변경

어플리케이션 전체에서 Glide에 사용할 수 있는 디스크캐시의 크기를 변경하려면 [Configurations](https://github.com/ksu3101/TIL/blob/master/Android/200816_android.md)을 참고 하자. 

##### 5.2.2 디스크 캐시 지우기

디스크 캐시의 내용을 모두 지우려면, `clearDiskCache()`메소드를 사용 한다. 이 메소드는 백그라운드 스레드에서 동작 시켜야 한다. 

```kotlin
AsyncTask<Void, Void, Void>() {
    override fun doInBackground(vararg params: Void?): Void {
        Glide.get(context)
            .clearDiskCache()
    }
}
```
