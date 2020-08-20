## Glide 9 - Resource Reuse

Glide에 대한 숙련이 필요하다고 느낀 요즘, [Glide v4 Documentation](https://bumptech.github.io/glide/)를 참고 하여 내용을 번역하면서 필요한 내용들 위주로 정리 하였다.

추가로 Glide가 Java로 작성되어 있다 보니 Kotlin으로 적용 하면서 불편한점을 확장함수 등 으로 정리 해서 좀 더 직관적이고 관리하기 좋은 코드로 만들고 같이 정리 하려 함 :) 

### 1. [자원 재활용](https://bumptech.github.io/glide/doc/resourcereuse.html)

Glide의 리소스는 비트맵, byte배열, int배열 및 다양한 POJO등이 포함된다. Glide는 어플리케이션의 메모리 변동량을 제한하기 위해 가능할때마다 리소스를 재사용하려고 한다. 

### 2. 자원 재활용의 이점

객체를 과하게 크게 메모리 할당하면 어플리케이션의 가비지 컬렉터(GC) 오버헤드가 크게 증가할 수 있다. 안드로이드의 Dalvik런타임은 최신의 ART런타임보다 GC패널티가 훨씬 높지만 어떤 기기를 사용하던 과도한 메모리 할당은 어플리케이션의 성능을 저하 시키게 된다. 

#### 2.1 Dalvik 

Dalvik 기기 (AOS Lollipop이전 버전 기기)는 논의할 가치가 있는 과도한 메모리 할당에 대해 예외적으로 큰 불이익을 받는다. Dalvik에는 GC의 처리 방식에 대하여 `GC_CONCURRENT`와 `GC_FOR_ALLOC`의 두가지 기본 모드가 있다. 

- `GC_CONCURRENT`는 각 컬렉션에 대해 약 5ms동안 메인 스레드를 두번 차단한다. 각 작업이 단일 프레임(16ms)보다 작기 때문에 `GC_CONCURRENT`로 인하여 어플리케이션이 프레임이 저하되어 보이진 않는다. 
- `GC_FOR_ALLOC`는 최소 125ms동안 메인 스레드를 차단하면서 시스템이 정지된다. `GC_FOR_ALLOC`는 사실상 항상 어플리케이션이 여러 프레임 저하를 확인 할 수 있으며, 특히 스크롤하는 동안 눈에 띄는 끊김 현상을 유발 한다. 

불행히도 Dalvik은 그리 대단하지도 않은 메모리 할당(예를 들어 16kb버퍼)도 잘 처리하지 못한다. 반복적인 어느정도 규모가 있는(예를 들면 비트맵) 메모리 할당들은 `GC_FOR_ALLOC`을 발생 시킨다. 따라서, 더 많이 메모리 할당할수록 발생하는 world garbage collections으로 인하여 어플리케이션이 중지 되고 더 많은 프레임 저하가 발생 하게 된다. 

- World garbage collections : major한 인스턴스들을 다수 힙 에서 GC하는 경우 모든 스레드는 GC를 마칠때 까지 중지 된다. 이때를 Stop the World라고 한다. 보통 일반적인 GC튜닝이라고 하면 이러한 GC때 모든 스레드의 중지 시간을 최소화 시켜주는 것 을 말한다. 

### 3. Glide가 리소스를 추적하고 재사용하는 방법 

Glide는 자원 재사용에 대해 관대한 접근 방식을 취한다. Glide는 안전하다고 판단되는 경우 기회에 따라 리소스를 재사용하지만, Glide는 호출자가 각 이미지 요청후 리소스를 재활용하도록 요구하지는 않는다. 호출자가 리소스 사용이 완료 되었음을 명시적으로 알리지 않는 한 리소스는 재활용 되거나 재사용되지 않는다. 

#### 3.1 참조 카운터

리소스가 사용중인 시기와 재사용이 안전한 시기를 확인하기 위하여 Glide는 각 리소스에 대한 참조 카운터를 관리 한다. 

##### 3.1.1 참조 카운터의 증가 

리소스를 로드하는 `into()`를 호출 할 때 마다 해당 리소스에 대해 참조 카운터가 1씩 증가 한다. 

##### 3.1.2 참조 카운터의 감소

참조 카운터는 호출자가 다음과 같은 방법으로 리소스 사용이 완료되었음을 알릴때 감소한다. 

1. 리소스가 로드된 `View`혹은 `Target`에서 `clear()`메소드를 호출 하였을 때 참조 카운터는 감소 된다. 
2. 이미지 요청으로 인해 새로운 리소스가 로드된 `View`혹은 `Target`에서 `into()`메소드가 호출 되었을 때 카운터가 감소한다. 

#### 3.2 리소스의 해제 

참조 카운터가 0에 도달하면 리소스가 해제되고 재사용을 위해 Glide로 반환된다. 리소스가 재사용을 위해 Glide로 반환 된 후에는 더 이상 계속 사용하는 것이 안전하지 않습니다. 

1. `getImageDrawable()`메소드를 이용해 `ImageView`에 로드된 `Bitmap`또는 `Drawable`을 얻고 이를 표시하려 할 때.(애니메이션 또는 `TransitionDrawable`, 기타 메소드에서 `setImageDrawable`사용)
2. `SimpleTarget`을 사용하여 `onLoadCleared()`콜백에서 `View`의 리소스를 제거하지 않고 `View`에 리소스를 로드 할 때.
3. Glide로 로드된 모든 비트맵에서 `recycle()`을 호출 한 경우.

`View`와 `Target`을 제거 한 뒤 리소스를 참조 하는것은 안전하지 않다. 해당 리소스가 파괴되었거나 다른 이미지를 표시하기 위해 재사용 될 수 있기 때문에 해당 리소스를 계속 사용하는 어플리케이션에서 정의되지 않은 동작, 그래픽 손상 또는 충돌이 발생할 수 있기 떄문이다. 

예를 들어, Glide로 다시 해제된 후 비트맵은 `BitmapPool`에 저장되고 이후에 새 이미지를 위해 재사용되거나 `recycle()`이 호출 될 수 있다. 비트맵을 계속 참조하고 원본 이미지를 포함할 것 으로 예상하는것은 안전하지 않다. 

#### 3.3 Pooling 

대부분의 Glide 재활용 로직은 비트맵을 대상으로 하지만, 모든 리소스는 `recycle()`메소드를 구현한 재사용 가능한 데이터를 풀링 할 수 있다. `ResourceDecoder`는 원하는 `Resource`를 API구현을 통해 자유롭게 반환할 수 있으므로 사용자는 자체 `Resource`와 `ResourceDecoder`를 구현하여 새로운 유형에 대한 풀링을 사용자 지정하여 제공 할 수 있다. 

특히 비트맵의 경우 Glide는 `Bitmap`리소스 객체를 얻고 재사용 할 수 있는 `BitmapPool`인터페이스를 제공한다. Glide의 `BitmapPool`은 Glide의 싱글톤을 이용해 모든 컨텍스트에서 얻을 수 있다. 

```kotlin
Glide.get(context)
    .getBitmapPool()
```

마찬가지로 `Bitmap`풀링에 대한 더 많은 제어를 원한다면 `BitmapPool`을 자유롭게 구현하여 `GlideModule`을 사용하여 Glide에 제공할 수 있다. 

### 4. 일반적인 오류

풀링을 사용하면 사용자가 리소스나 비트맵을 잘못 사용하고 있는지 알 수 없다. Glide는 가능한경우 assertions을 추가하려고 시도 하지만, 근본적으로 `Bitmap`을 소유하고 있지 않기 때문에 호출자가 `clear()`메소드 또는 새로운 요청을 통해서 `Bitmap`또는 기타 리소스의 사용을 중지했다고 보장 할 수 없다. 

#### 4.1 자원 재사용시 오류의 증상들

Glide의 `Bitmap`또는 기타 리소스 풀링에 문제가 발생했음을 알리는 몇가지 지표가 있다. 일반적인 증상에 대해 아래에 작성하였지만 이 증상들이 발생할 수 있는 모든 증상은 아니다. 

##### 4.1.1 재활용(recycled) 처리된 비트맵을 사용하지 말 것

Glide의 `BitmapPool`은 고정된 크기를 갖는다. 비트맵이 재사용되지 않고 풀에서 제거되면 Glide는 `recycle()`메소드를 호출 한다. 어플리케이션이 Glide에 재활용해도 안전하다고 알려도 실수로 `Bitmap`을 계속 유지하는 경우 어플리케이션은 `Bitmap`을 이용해 화면에 그리기를 시도하여 `onDraw()`메소드 내 에서 문제가 발생할 수 있다. 

이 문제는 하나의 대상이 두개의 `ImageView`에 사용되고 있고 `ImageView`중 하나가 `BitmapPool`에 배치 된 후에도 재활용 된 비트맵에 액세스하려고 시도하기 때문일 수도 있다. 

비트맵 재활용 오류는 아래의 요인으로 인해 재현하기 어려울 수도 있다. 

1. 비트맵을 비트맵 풀 에 넣을떄,
2. 비트맵이 재활용 처리 되었을 때,
3. 비트맵의 재활용으로 이어지는 `BitmapPool`과 메모리 캐시의 크기

이 문제를 쉽게 재현할 수 있도록 다음 코드를 `GlideModule`에 넣을 수 있다. 

```kotlin
@GlideModule
class MyAppGlideModule: AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val bitmapPoolSizeByte = 1024 * 1024 * 0
        val memoryCacheSizeByte = 1024 * 1024 * 0
        builder.setMemoryCache(LruResourceCache(memoryCacheSizeByte.toLong()))
        builder.setBitmapPool(LruBitmapPool(bitmapPoolSizeByte.toLong()))
    }
}
```

위 코드는 메모리 캐싱과 `BitmapPool`의 크기가 모두 0이다. 따라서 `Bitmap`을 사용하지 않으면 즉시 `recycle()`메소드가 호출되어 재활용 된다. 이 문제는 디버깅을 통해서 훨씬 더 빨리 발견할 수있다. 

##### 4.1.2 재활용 처리된 비트맵에 대해 `reconfigure()`메소드를 호출하지 말 것

리소스는 더이상 사용하지 않을 때 Glide의 `BitmapPool`으로 반환된다. 이는 요청(자원을 제어하는 쪽)의 수명주기를 기반으로 내부에서 처리된다. 해당 비트맵에서 `recycle()`을 호출했지만 여전히 풀에 있는 경우 Glide는 이 리소스를 재사용할 수 없으며, 앱이 "Can't call reconfigure() on a recycled bitmap"라는 예외 메시지와 함께 종료 된다. 여기에서 한가지 중요한 점은 문제가 되는 코드가 실행된 위치가 아니라 앱의 다른 지점에서 향후에 크래시가 발생할 가능성이 존재 한다는 것 이다.

##### 4.1.3 여러개의 뷰들에 보여지는 이미지들이 깜박이거나 동일한 이미지가 보여짐

`Bitmap`이 `BitmapPool`으로 여러번 반환되거나 풀로 반횐되었지만 `View`에 의해 여전히 유지되는 경우 다른 이미지가 `Bitmap`으로 디코딩될 수 있다. 이 경우 `Bitmap`의 내용이 새로운 이미지로 대체 된다. 뷰는 이 프로세스 중에 여전히 비트맵을 그리려고 시도 할 수 있으며, 이로 인하여 원래 뷰에 새 이미지가 표시 된다. 

#### 4.2 재사용 오류의 원인 

이미지 재사용 오류의 일반적인 몇가지 원인은 다음과 같다. 

##### 4.2.1 두 개의 다른 리소스를 동인한 대상에 로드하려 했을 때

Glide의 단일 대상에 여러 리소스를 로드하는 안전한 방법은 없다. 사용자는 `thumbnail()` API를 사용하여 일련의 리소스를 `Target`에 로드할 수 있지만, 다음에 `onResourceReady()`를 호출 할 때 까지 각 이전 리소스를 참조하는 것 이 안전하다. 

일반적으로 더 나은 답은 두번째 View를 사용하고 두번째 View에 두 번째 이미지를 로드 하는 것 이다. (예를 들면 `ViewSwitcher`와 같은 것)

##### 4.2.2 자원을 `Target`에 로드 하고, `Target`을 지우거나 재사용한뒤, 자원을 계속 참조 했을 때

이 오류를 방지하는 가장 쉬운 방법은 `onLoadCleared()`가 호출 될 때 리소스에 대한 모든 참조가 무효화 되도록 하는 것 이다. 일반적으로 `Bitmap`을 로드 한 다음 `Target`을 역 참조하고 `Target`에서 `into()`또는 `clear()`를 다시 호출 하지 않는 것이 안전하다. 그러나 비트맵을 로드하고 `Target`을 지운 뒤에도 비트맵을 계속 참조하는 것 은 안전하지 않다. 

마찬가지로 리소스를 View에 로드 한 다음 `getImageDrawable()`또는 다른 수단을 통해 View에서 리소스를 얻고 다른 곳 에서 계속 참조하는 것 은 안전하지 않다. 

##### 4.2.3 `Transformation<Bitmap>`에서 원본 비트맵을 재활용 했을 때

`transform()`에 전달 된 원래 `Bitmap`은 `Transformation`에서 반환 된 `Bitmap`이 `transform()`에 전달 된 인스턴스와 동일한 인스턴스가 아닌 경우 재활용 된다. 이것은 Picasso와 같은 다른 이미지 로더 라이브러리와의 중요한 차이점 이다. `BitmapTransformation`은 Glide의 리소스 생성을 처리하기 위한 상용구를 제공하지만 재활용은 내부적으로 수행 되므로 `Transformation`과 `BitmapTransformation`모두 전달 된 `Bitmap`또는 리소스를 재활용해서는 안된다. 

