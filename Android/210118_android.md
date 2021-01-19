## Hilt: custom entry points

> 이 글은 Bevan Steele의 [Hilt: custom entry points](https://www.rockandnull.com/hilt-entrypoint-example/)을 번역 하였다. 

[Hilt](https://dagger.dev/hilt/)는 Android를 위한 훌륭한 의존성 주입 프레임워크이다. [Dagger](https://dagger.dev/dev-guide/)의 모든 장점들을 [쉽고 간결하게 사용](https://www.rockandnull.com/android-hilt-tutorial/)할 수 있게 해준다. 

Android(컴포넌트)클래스에 `@AndroidEntryPoint`어노테이션을 추가 하기만 하면 마법처럼 `@Inject`어노테이션이 달린 클래스가 주입된다. 

`@AndroidEntryPoint`어노테이션은 Fragment, View, Service 그리고 BroadcastReceiver와 같은 기본 시스템 클래스에 대해서 의존성 주입을 지원한다. 그러나 주입도구는 마법과 같은 존재이다. 시스템 클래스(또는 인스턴스화 되지 않은 것)을 발견하였지만 주입해야 할 경우 어떻게 해야 할까? 

이것은 원본 글 작성자가 (Jetpack의 앱 시작 라이브러리 에서) 초기화된 클래스들을 주입해야 한다는 것 을 알았을 때 Hilt로 마이그레이션 하면서 발생하였다. 여기에서 사용자 정의된 진입점이 필요하였다. 

### Entry point interface

`@inject`를 이용해 무엇을 주입 시키려 하기 위해 인스턴스를 가져 오려면 `@EntryPoint`어노테이션이 달린 인터페이스에 "접근자"메소드를 추가 해야 한다. 

```kotlin
@EntryPoint
@InstallIn(SingletonComponent::class) 
interface InitializerEntryPoint {
    fun myHelper(): MyHelper
}
```

주입된 인스턴스가 속한 동일한 컴포넌트에 이 것들을 "설치" 해야 한다. 따라서 Acitivty의 범위(Activity-scope) 및 싱글톤 범위(Singleton-scope)의 인스턴스가 필요한 경우 둘 이상의 진입점 인터페이스가 필요할 수 있다. 

### Where to add the entry point interface.

[공식문서](https://dagger.dev/hilt/entry-points.html)에 따르면 이상적으로 이러한 인터페이스는 제공되는 개체가 아니라 사용되는 개체와 가까워야 한다. 예를 들어 Jetpack의 Initializer예제에서 이러한 인터페이스는 Initializer내부 에 정의 되어야 하는 것 이다. 

```kotlin
class MyCoolInitizlier: Initializer<Unit> {
    // ...

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface InitializerEntryPoint {
        fun myHelper(): MyHelper
    }
}
```

이것은 이러한 진입점 인터페이스가 `@Inject`될 수 없는 특수한 경우에만 존재한다는 것 을 분명히 하기 위한 것 이다. 달느 모든 경우에 대해서는 표준적인 `@Inject`및 `@AndroidEntryPoint`어노테이션을 사용 해야 한다. 

### How to use them

마지막으로, 이러한 진입점 인터페이슬르 사용 하려면 정적인 Hilt메소드를 사용하여 해당 "접근자"메소드에 접근 하여 인스턴스를 가져오기만 하면 된다. 

```kotlin
val myHelper = EntryPoints.get(applicationContext, InitializerEntryPoint::class.java).myHelper()
```