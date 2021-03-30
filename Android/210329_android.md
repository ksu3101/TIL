## All About PendingIntents

> 이 글은 Nicole Borrelli의 [All About PendingIntents](https://medium.com/androiddevelopers/all-about-pendingintents-748c8eb8619)을 번역 하였다. 

`PendingIntent`는 Android프레임 워크의 중요한 부분이지만 사용 가능한 대부분의 개발자들은 구현 세부 정보에 초점을 잘 맞추곤 한다. 

Android12 에서는 PendingIntent가 변경 가능하거나 변경 불가능한 시기를 명시적으로 결정 해야 하는 업데이트를 포함하여 PendingIntent에 대한 중요한 변경 사항이 포함되어 있으므로, PendingIntent가 수행하는 작업, 시스템에서 이를 사용하기 위한 방법및 떄떄로 PendingIntent를 사용해야 하는 이유에 대해 자세히 설명 하려고 한다. 

## What is PendingIntent?

`PendingIntent`는 `Intent`의 기능을 래핑하는 동시에 앱이 향후 작업에 대한 응답으로 앱을 대신하여 다른 앱이 수행해야 하는 작업을 할수 있도록 해 준다. 예를 들면, 래핑된 Intent는 알람이 울리거나 사용자가 이 알람에 대한 알림을 탭할 때 호출 될 수 있을 것 이다. 

PendingIntent의 주요 측면은 다른 앱이 앱을 대신하여 인텐트를 호출한다는 점 이다. 즉, 다른 앱에서는 인텐트를 호출 할 때 앱의 ID를 사용하게 된다. 

PendingIntent가 일반 인텐트인것 처럼 동일한 동작을 갖도록 시스템은 생성된 것 과 동일한 ID로 PendingIntent를 트리거 한다. 알람이나 알림과 같은 대부분의 상황에서 이것은 앱 자체의 ID가 되는 것 이다. 

앱이 PendingIntent와 함께 작동할 수 있는 다양한 방법과 왜 사용하는지에 대해 살펴 보도록 하자. 

## Common case 

PendingIntent를 사용하는 가장 일반적으고 기본적인 방법은 알림(Notification)과 관련된 작업이다. 

```kotlin
val intent = Intent(applicationContext, MainActivity::class.java).apply {
    action = NOTIFICATION_ACTION
    data = deepLink
}

val pendingIntent = PendingIntent.getActivity(
    applicationContext,
    NOTIFICATION_REQUEST_CODE,
    intent,
    PendingIntent.FLAG_IMMUTABLE
)

val notification = NotificationCompat.Builder(
        applicationContext,
        NOTIFICATION_CHANNEL
    ).apply {
        // ...
        setContentIntent(pendingIntent)
        // ...
    }.build()

notificationManager.notify(
    NOTIFICATION_TAG,
    NOTIFICATION_ID,
    notification
)
```

앱을 실행할 표준 유형의 Intent를 구성하고 알림에 추가하기 전 `PendingIntent`로 래핑하는 것을 확인 할 수 있다. 

이 경우 수행하고자 하는 정확한 작업이 있으므로 `FLAG_IMMUTABLE`이라는 플래그를 사용하여 전달 하는 앱에서 수정할 수 없는 PendingIntent를 구성 한다. 

이제 `NotificationManagerCompat.notify()`를 호출하면 완료 된다. 시스템은 알림을 표시하고 사용자가 알림을 클릭하게 되면 `PendingIntent`에서 `PendingIntent.send()`를 호출하여 앱을 시작하게 된다. 

## Updating an Immutable PendingIntent

앱이 PendingIntent를 업데이트 해야 하는 경우 이를 변경가능해야 한다고 생각 할 수 있겠지만 항상 그런것은 아니다. PendingIntent를 생성하는 앱은 `FLAG_UPDATE_CURRENT`플래그를 전달하여 항상 업데이트 하게 할 수 있다. 

```kotlin
val updatedIntent = Intent(applicationContext, MainActivity::class.java).apply {
   action = NOTIFICATION_ACTION
   data = differentDeepLink
}

// Because we're passing `FLAG_UPDATE_CURRENT`, this updates
// the existing PendingIntent with the changes we made above.
val updatedPendingIntent = PendingIntent.getActivity(
   applicationContext,
   NOTIFICATION_REQUEST_CODE,
   updatedIntent,
   PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
)
// The PendingIntent has been updated.
```

이제 PendingIntent를 변경 가능한 상태로 만드는 이유에 대해 설명 하겠다. 

## Inter-app APIs

일반적인 경우는 시스템과 상호 작용하는 데만 유용하지 않다. 작업을 수행한 뒤 콜백을 수신하기 위해 `startActivityForResult()`및 `onActivityResult()`를 사용하는 것 이 가장 일반적이지만 이것이 유일한 방법은 아니다. 

앱과 통합될 수 있도록 API를 제공하는 온라인 서비스 앱을 생각해보자. 음식 주문 프로세스를 시작 하는데 사용되는 자체 인텐트를 추가 하여 PendingIntent를 허용할 수 있다. 주문앱은 주문이 전달 된 후에만 PendingIntent를 시작 한다. 

이 경우 주문 앱은 주문이 전달되는데 상당한 시간이 걸릴 수 있고 사용자가 이 상황에서 기다리게 강요하는것은 맞지 않기 때문에 Acitivty의 결과를 전달하는 대신 PendingIntent를 사용 한다. 

온라인 주문 앱이 인텐트에 대해 아무것도 변경하지 않기를 바라기 때문에 변경 불가능한 PendingIntent를 만들어야 한다. 주문이 도착했을때 그대로 배송되어야 하기 떄문이다. 

## Mutable PendingIntents

그러나 우리가 주문앱의 개발자이고 사용자가 이를 호출 한 앱으로 다시 보낼 메시지를 입력할 수 있는 기능을 추가 하고 싶다면 어떻게 해야 할까? 호출 앱이 "피자 타임 입니다!"와 같이 표시할 수 있도록 허용해야 한다. 

이에 대한 답으로는 변경 가능한 PendingIntent를 사용하는 것 이다. 

PendingIntent는 본질적으로 Intent를 래핑한 래퍼 이므로 래핑된 Intent를 가져오고 업데이트 하기 위하여 호출 할 수 있는 `PendingIntent.getIntent()`라는 메소드가 있을것 이라고 생각할 수 있겠지만 그렇지 않다. 

매개 변수를 사용하지 않는 PendingIntent의 `send()`메소드 외 에도 Intent를 허용하는 몇가지 다른 버전들이 존재 한다. 

```kotlin
fun PendingIntent.send(
    context: Context!, 
    code: Int, 
    intent: Intent?
)
```

이 인텐트 매개 변수는 PendingIntent에 포함 된 Intent를 대체하는 것 이 아니라 PendingIntent가 생성 될 때 제공되지 않은 래핑 된 Intent의 매개 변수를 채우는데 사용 된다. 

다음 예제를 살펴 보자. 

```kotlin
val orderDeliveredIntent = Intent(applicationContext, OrderDeliveredActivity::class.java).apply {
   action = ACTION_ORDER_DELIVERED
}

val mutablePendingIntent = PendingIntent.getActivity(
   applicationContext,
   NOTIFICATION_REQUEST_CODE,
   orderDeliveredIntent,
   PendingIntent.FLAG_MUTABLE
)
```

이 PendingIntent는 온라인 주문 앱에 전달된다. 배송이 완료 된 뒤 주문 앱은 `customMessage`를 가져와 다음과 같은 추가 인텐트로 다시 보내질 수 있다. 

```kotlin
val intentWithExtrasToFill = Intent().apply {
   putExtra(EXTRA_CUSTOMER_MESSAGE, customerMessage)
}

mutablePendingIntent.send(
   applicationContext,
   PENDING_INTENT_CODE,
   intentWithExtrasToFill
)
```

그러면 호출 앱은 인텐트에서 `EXTRA_CUSTOMER_MESSAGE`를 보고 메세지를 표시할 수 있게 될 것 이다. 

## Important considerations when declaring pending intent mutability

변경 가능한 PendingIntent를 만들 때 항상 Intent에서 시작될 구성 요소를 명시적으로 설정 하자. 이를 수신할 정확한 클래스를 명시적으로 설정하여 위에서 수행한 방식으로 수행할 수 있겠지만 `Intent.setComponent()`를 호출하여 수행할 수도 있다. 

앱에 `Intent.setPackage()`를 호출하는 것이 더 간단할 수 있다. 이 메소드를 사용 하면 여러 구성요소들의 불일치 가능성에 대해 주의 해야 한다. 가능하면 Intent를 받을 특정 구성 요소들은 지정되어 있는게 좋다. 

`FLAG_IMMUTABLE`로 생성된 PendingIntent의 값을 재정의하려고 하면 자동으로 실패되며 원래 래핑된 Intent가 수정되지 않은 상태로 전달 된다. 

앱은 변경 불가능한 경우에도 항상 자체 PendingIntent를 업데이트 할 수 있다. PendingIntent를 변경 가능하게 만드는 유일한 이유는 어떤 방법으로든 래핑 된 Intent를 업데이트 할 수 있어야 하기 때문이다. 

## Details on flags 

우리는 PendingIntent를 만들 때 사용할 수 있는 몇가지 플래그에 대해 약간 이야기했지만 다루어야 할 몇가지 다른 플래그들도 있다. 

`FLAG_IMMUTABLE` : `PendingIntent.send()`에 intent를 전달하는 다른 앱이 PendingIntent내부의 Intent를 수정할 수 없음을 나타낸다. 앱은 항상 `FLAG_UPDATE_CURRENT`를 사용하여 자체 PendingIntent를 수정할 수 있다. Android12버전 이전에는 플래그 없이 생성된 PendingIntent를 기본적으로 변경 할 수 있었다. 

Android12버전(API23) 이후로 PendingIntent는 기본적으로 변경할수 없게 된다.   

`FLAG_MUTABLE` : PendingIntent내부의 인텐트가 `PendingIntent.send()`의 인텐트 매개변수의 값을 병합하여 앱이 해당 내용을 업데이트 할 수 있도록 한다.

항상 변경 가능한 PendingIntent의 래핑 된 Intent 컴포넌트이름(ComponentName)을 입력해야 한다. 그렇게 하지 않으면 보안 취약점이 발생하기 떄문이다. 

이 플래그는 Android12버전에서 추가 되었으며 이전 버전에서는 `FLAG_IMMUTABLE`플래그 없이 생성된 모든 PendingIntent모두가 암시적으로 변경 가능 했었다.   

`FLAG_UPDATE_CURRENT` : 시스템이 PendingIntent를 저장하는 대신 새 추가 데이터로 기존 PendingIntent를 업데이트 하도록 요청 한다. PendingIntent가 등록되지 않은 경우 이 플래그가 사용 된다.  

`FLAG_ONE_SHOT` : PendingIntent가 한번만 전송되도록 한다. (`PendingIntent.send()`를 통해서) 이는 PendingIntent를 다른 앱에 전달 할 때 중요할 수 있다. Intent가 한번만 전송될 수 있어야 하는 경우에 사용 되기 떄문이다. 이는 편의성이나 일부 작업들을 여러분 수행하지 못하도록 하기 위함이다. 또한 `replay attacks`와 같은 문제를 미리 방지 할 수 있다.   

`FLAG_CANCEL_CURRENT` : 새로 인텐트를 등록하기 전에 기존 PendingIntent가 존재 하는 경우 이를 취소한다. 특정 PendingIntent가 하나의 앱으로 전송 되었고 이를 다른앱으로 전송하여 잠재적으로 데이터를 업데이트 하려는 경우 중요하게 사용될 수 있다. 첫번째 경우 더이상 `send()`를 호출할 수 없지만 두번째의 경우에는 호출이 가능 하다. 

## Receiving PendingIntents

때로 시스템 또는 기타 프레임워크에서 API호출의 반환으로 `PendingIntent`를 제공 할 수 있다. 그 예로는 Android11에 추가 된 `MediaStore.createWriteRequest()`이다. 

```kotlin
static fun MediaStore.createWriteRequest(
    resolver: ContentResolver, 
    uris: MutableCollection<Uri>
): PendingIntent
```

앱에서 생성 된 PendingIntent가 앱의 ID로 실행 되는 것 처럼 시스템에서 생성된 PendingIntent는 시스템의 고유 ID로 실행 되어 진다. 이 API의 경우 이를 통해 앱이 `Uris`컬렉션에 대한 쓰기 권한을 앱에 부여할 수 있는 Activity를 실행 할 수 있다. 

