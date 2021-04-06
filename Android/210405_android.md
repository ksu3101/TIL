# Common Design Patterns and App Architectures for Android

> 이 글은 Matt Luedke, Aaqib Hussain의 [Common Design Patterns and App Architectures for Android](https://www.raywenderlich.com/18409174-common-design-patterns-and-app-architectures-for-android)을 번역 하였다. 

# 1. Getting Started

> "현재의 프로젝트에서 같은 코드들을 재사용 하고 있는가?" 

개발자는 프로젝트내에서 복잡해진 의존들을 추적하기 위해 소요되는 시간을 최소화 해야 한다. 따라서 가능하면 재사용 가능하며 읽기 쉽고 알아보기 쉬운 코드들을 생산해내야 한다. 이런 목표들은 단일 개체에서 부터 전체 프로젝트에 이르기 까지 다음과 같은 범주에 속하는 패턴으로 구성 된다. 

- Creational Patterns : 객체를 어떻게 만드는가? 
- Structural Patterns : 객체를 어떻게 구성 하는가? 
- Behavioral Patterns : 객체들을 어떻게 상호 작용하게 하는가? 

일반적으로 디자인 패턴은 객체를 처리하는 기법이다. 그 것은 객체가 보여주는 반복적인 문제들에 대한 해결책을 제시하고 설계와 관련된 문제를 해결하는데 도움을 준다. 즉, 달느 개발자가 이미 직면했었던 문제들을 알려주며 해결 할 수 있는 증명된 방법들을 제시함으로서 같은 문제가 발생하지 않도록 해 주는 것 이다. 

이번 섹션에서는 각 범주에 속하는 패턴들을 다루고 Android에서는 어떻게 이들을 적용하는지 살펴 본다. 

## Creational Patterns

- Builder
- Dependency Injection
- Singleton
- Factory 

## Structural Patterns

- Adapter
- Facade
- Decorator
- Composite

## Behavioral Patterns

- Command
- Observer
- Strategy
- State

# 2. Creational Patterns 

> "복잡한 객체가 필요한 경우 이 인스턴스를 어떻게 얻을 것 인가?" 

미래의 개발자로서 당신은 "이 객체의 인스턴스가 필요할 때 마다 동일한 생성 코드들을 복사, 붙여넣기 한다"가 아니길 빈다. 객체의 인스턴스를 생성하는 패턴은 객체 인스턴스를 간단하고 재사용및 반복 가능하게 해준다. 

## 2.1 Builder

어떤 레스토랑에서 나만의 샌드위치를 만들수 있다고 생각해보자. 종이에 적혀있는 체크 리스트에서 샌드위치에 넣고 싶은 빵과 재료 및 각종 양념들을 선택할 수 있다. 체크리스트에서 나만의 샌드위치를 만들도록 하려면 이러한 양식들을 작성해서 카운터에 넘겨주기만 하면 된다. 이는 샌드위치를 만드는게 아니라 주문 제작하고 이를 소비 하는 것 이다.  

마찬가지로, Builder패턴은 이 샌드위치를 표현하여 빵을 자르고 피클을 쌓는 것 등 과 같은 객체의 생성을 단순화 한다. 따라서 동일한 구성 프로세스가 다른 속성을 가진 동일한 클래스 객체를 생성할 수 있다. 

안드로이드에서 Builder패턴의 좋은 예제는 `AlertDialog.Builder`이다. 

```kotlin
AlertDialog.Builder(this)
  .setTitle("Sandwich Dialog")
  .setMessage("Please use the spicy mustard.")
  .setNegativeButton("No thanks") { dialogInterface, i ->
    // "No thanks" action
  }
  .setPositiveButton("OK") { dialogInterface, i ->
    // "OK" action
  }
  .show()
```

이 Builder는 단계별로 진행 되며 지정해야 하는 `AlertDialog`만 지정할 수 있다. `AlertDialog.Builder`[문서](https://developer.android.com/reference/android/app/AlertDialog.Builder.html)를 살펴 보면, 알림 다이얼로그를 생성하기 위해 선택할 수 있는 몇가지의 명령들이 더 있음을 확인 할 수 있다. 

## 2.2 Dependency Injection 

Dependency Injection(DI)은 가구가 존재하는 아파트로 이사하는것 과 같다. 필요한 모든 가구들이 이미 있다. 이사를 할 때 이케아의 어떤 책장을 만들기 위해서 가구 배송을 기다리거나 이케아의 가이드 북을 따를 필요도 없다.

소프트웨어 측면에서 DI는 새 객체를 인스턴스화 하는데 필요한 객체들을 제공 한다. 

안드로이드에서는 네트워크 클라이언트, 이미지 로더 또는 `SharedPreferences`와 같은 앱의 다양한 지점에서 동일한 복잡한 객체 인스턴스에 접근하는 일이 있다. 이 때 필요한 인스턴스를 Activity나 Fragment에 바로 삽입하고 즉시 접근하여 사용할 수 있게 해준다. 

현재 DI를 위한 세가지 주요 라이브러리가 존재 한다. Dagger2, Dagger Hilt, Koin이다. 여기에서는 Dagger의 예를 살펴 보도록 하자. Dagger에서는 `@Module`로 클래스에 어노테이션을 추가 하고 다음처럼 `@Provides`메소드를 통해 인스턴스를 제공 받는다. 

```kotlin
@Module
class AppModule(private val app: Application) {
  @Provides
  @Singleton
  fun provideApplication(): Application = app

  @Provides
  @Singleton
  fun provideSharedPreferences(app: Application): SharedPreferences {
    return app.getSharedPreferences("prefs", Context.MODE_PRIVATE)
  }
}
```

위의 모듈은 필요한 객체들을 만들어준다. 더 큰 앱의 경우라면 기능별로 모듈을 분리 하여 여러개의 모듈을 가질 수도 있다. 

마지막으로 `@Inject`어노테이션을 사용하여 필요할 때 마다 종속성을 요청 하고, 포함된 객체를 생성 한 뒤 `lateinit`을 사용 하여 `nullable`이 아닌 인스턴스를 초기화 해 준다. 

```kotlin
@Inject
lateinit var sharedPreferecnes: SharedPreferences
```

예를 들어, `MainActivity`에서 이것을 사용 한 뒤 다른 `Activity`가 `SharedPreferences`객체가 어떻게 되었는지 알 필요없이 바로 사용 할 수 있게 해준다. 

몰론 이는 단순히 설명한 내용이지만 더 자세한 구현과 세부사항은 [Dagger 문서](http://google.github.io/dagger/)를 확인 하면 된다. 

이 패턴은 처음에는 복잡하고 어렵지만 Activity와 Fragment의 코드를 단순화 하는데 도움이 될 수 있다. 

## 2.3 Singleton

Singleton패턴은 클래스의 단일 인스턴스만 존재하게 하여 전역의 접근 포인트에서 접근하도록 해 준다. 이 패턴은 단 하나의 인스턴스로 실제 객체를 모델링 하여 사용할 때 잘 작동한다. 예를 들어, 네트워크 또는 데이터베이스 연결을 도와주는 객체가 있을 경우 이 객체가 두개 이상일 경우 문제 혹은 데이터들을 혼합되는 문제가 발생할 수 있다. 이것이 일부 상황에서 두개 이상의 인스턴스 생성을 제한하려는 목적이다. 

Kotlin에서 `object`키워드는 다른 언어에서와 같이 정적 인스턴스를 따로 지정할 필요 없이 싱글톤을 선언할 수 있도록 해준다. 

```kotlin
object ExampleSingleton {
    fun exampleFunc() {
        // ...
    }
}
```

싱글톤 객체의 멤버에 접근하는 경우 아래처럼 호출 하면 된다. 

```kotlin
ExampleSingleton.exampleFunc()
```

`INSTANCE`정적 필드는 Java에서 코틀린 정적 객체를 사용해야 하는 경우 아래처럼 사용 할 수 있게 해준다. 

```java
ExampleSingleton.INSTANCE.exampleFunc();
```

싱글톤 객체를 사용해 보면 앱 전체에서 해당 클래스의 인스턴스를 하나만 사용하고 있음을 알 수 있다. 

싱글톤패턴은 이해하기 쉽고 가장 간단한 패턴이지만 남용할 경우 위험할 수 있다. 여러곳 에서 인스턴스에 대해 접근할 수 있기 때문에 싱글톤은 추적하기 어려운 예외가 발생할 수 있다. 싱글톤 패턴이 사용하기 쉽지만 유지 관리를 위해서 다른 디자인 패턴을 고려하는게 더 좋을 수 있다. 

## 2.4 Factory 

이름에서 알 수 있듯이 Factory패턴은 모든 객체의 생성을 처리 해 준다. 이 패턴에서 Factory클래스는 인스턴스로 만들 객체를 제어 해 준다. 팩토리 패턴은 많은 공통 객체를 다룰 때 유용 하다. 구체적인 클래스를 지정하지 않으려 하는 곳 에도 사용 할 수 있다. 

빠른 이해를 위해서 아래의 예제 코드를 보도록 하자. 

```kotlin
// 1
interface HostingPackageInterface {
  fun getServices(): List<String>
}

// 2
enum class HostingPackageType {
  STANDARD,
  PREMIUM
}

// 3
class StandardHostingPackage : HostingPackageInterface {
  override fun getServices(): List<String> {
    return ...
  }
}

// 4
class PremiumHostingPackage : HostingPackageInterface {
  override fun getServices(): List<String> {
    return ...
  }
}

// 5
object HostingPackageFactory {
  // 6
  fun getHostingFrom(type: HostingPackageType): HostingPackageInterface {
    return when (type) {
      HostingPackageType.STANDARD -> {
        StandardHostingPackage()
      }
      HostingPackageType.PREMIUM -> {
        PremiumHostingPackage()
      }
    }
  }
}
```

예제 코드에 대해 설명하면 다음과 같다. 

1. 호스팅 플랜에 따른 기본 인터페이스 `HostingPackageInterface` 이다. 
2. 이 enum 클래스는 모든 호스팅의 패키지 유형인 `STANDARD`와 `PREMIUM`을 지정 한다. 
3. `StandardHostingPackage`는 인터페이스를 상속받아 모든 서비스를 리스트로 보여주는데 필요한 방법을 구현 하였다. 
4. `PremiumHostingPackage`는 인터페이스를 상속받아 모든 서비스를 리스트로 보여주는데 필요한 방법을 구현 하였다. 
5. `HostingPackageFactory`는 도우미 메소드가 있는 싱글톤 클래스이다. 
6. `HostingPackageFactory`의 `getHostingFrom()`함수는 매개변수로 받는 호스팅 패키지 유형에 따라, 객체의 인스턴스를 생성 하여 반환 한다. 

이 팩토리는 다음처럼 사용할 수 있다. 

```kotlin
val standardPackage = HostingPackageFactory.getHostingFrom(HostingPackageType.STANDARD)
```

모든 객체의 생성을 하나의 클래스로 하는게 유지 보수에 도움이 된다. 하지만 팩토리 패턴을 부적절하게 사용 하면 과도한 객체들의 생성들로 인하여 팩토리 클래스 자체가 비대해질 수 있다. 그리고 팩토리 클래스 자체가 모든 객체를 제어하게 되므로 단위 테스트가 어려워질 수 도 있다. 

# 3. Structural Patterns

> "이 클래스에서는 어떤 일을 하고 있고 어떻게 합쳐졌는지 알고 있는가?"

일반적인 작업을 수행하는 클래스와 객체 내부등에 익숙한 구조적 패턴을 사용하는 것 이 좋은 방법일 수 있다. 이 경우 Adapter와 Facade는 안드로이드에서 일반적으로 볼 수 있는 두가지 패턴 이다. 

## 3.1 Adapter 

영화 Apollo13의 유명한 장면에서는 사각형 못을 둥근 구멍에 맞추는 작업을 담당하는 엔지니어팀이 있었다. 이것은 은유적인 Adapter의 표현이다. 소프트웨어적인 측면에서 이 패턴을 사용 하면 클래스의 인터페이스를 호환하지 않는 클라이언트의 인터페이스로 변환하여 호환되지 않는 두 클래스가 함께 작동하도록 할 수 있게 한다. 

앱의 비즈니스로직을 보았을때 데이터 조각, 어떠한 상품들, 사용자 또는 예제에서 보여질 Tribble등은 위의 예 에서 정사각형 못을 의미 한다. 그리고 `RecyclerView`는 모든 안드로이드 앱에서 동일 한 기본 개체로서 여기에선 둥근 구멍이 될 것 이다. 

이 상황에서 `RecyclerView.Adapter`의 자식 클래스를 구현하고 문제 없이 작동할 수 있도록 필요한 메소드를 구현할 수 있다. 

```kotlin
class TribbleAdapter(private val tribbles: List<Tribble>) : RecyclerView.Adapter<TribbleViewHolder>() {
  override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): TribbleViewHolder {
    val inflater = LayoutInflater.from(viewGroup.context)
    val view = inflater.inflate(R.layout.row_tribble, viewGroup, false)
    return TribbleViewHolder(view)
  }

  override fun onBindViewHolder(viewHolder: TribbleViewHolder, itemIndex: Int) {
    viewHolder.bind(tribbles[itemIndex])
  }

  override fun getItemCount() = tribbles.size
}
```

`RecyclerView`는 Tribble이 뭔지 모른다. 스타트렉(StarTrek)의 에피소드를 단 하나도 보지 못했기 때문일 수 있다. 대신 데이터를 처리하고 올바른 `ViewHolder`에 bind하는것 이 Adapter가 하는 일 이다. 

## 3.2 Facade 

Facade패턴은 다른 인터페이스 세트를 더 쉽게 사용할 수 있는 상위 레벨의 인터페이스를 제공 한다. 아래의 다이어그램은 이 패턴에 대해 더 자세히 알려준다. 

![facade](./images/facade.png)

예를 들어 Activity에서 도서 목록들이 필요한 경우 로컬 저장소, 캐시 및 API클라이언트의 내부 구동을 몰라도 해당 도서 목록들에 대해 단일 객체를 요청할 수 있어야 한다. 또한 Activity및 Fragment의 코드를 깔끔하고 간결하게 유지 하는 것 외에도 다른 코드에 영향을 주지 않고 API구현에 필요한 작업들을 수행할 수 있어야 한다. 

Square의 [Retrofit](http://square.github.io/retrofit/)은 Facade패턴을 구현하는데 도움이 되는 오픈소스 라이브러리이다. 아래와 같이 클라이언트 클래스에 API데이터를 제공하는 인터페이스를 만든다. 

```kotlin
interface BooksApi {
  @GET("books")
  fun listBooks(): Call<List<Book>>
}
```

클라이언트는 콜백에서 `Book`객체 목록을 전달 받기 위해 `listBooks()`를 호출 해야 한다. 이는 API호출을 깔끔하게 처리 해준다. 이를 통해 클라이언트의 다른 코드에 영향을 주지 않고 작업을 수행할 수있다. 다른 예를 들면 retrofit에 사용자 지정 JSON desirializer를 지정할 때 에도 사용할 수 있다. 

```kotlin
val retrofit = Retrofit.Builder()
  .baseUrl("http://www.myexampleurl.com")
  .addConverterFactory(GsonConverterFactory.create())
  .build()

val api = retrofit.create<BooksApi>(BooksApi::class.java)
```

`GsonConverterFactory`을 사용하는 것 에 대해 보면 JSON deserializer로 작동하고 있음을 확인할 수 있다. Retrofit을 사용 하면 `Interceptor`및 `OkHttpClient`로 작업을 추가로 사용자 정의 하여 클라이언트가 무슨일을 하고 있는지에는 상관 없이 캐싱 및 로깅 작업등을 제어할 수 있다. 

각 개체가 다른 면에서 일어나고 있는 일들에 대해 잘 알지 못하면 못할수록 향후 앱 에서 유지 보수하는게 더 쉬워진다. 

## 3.3 Decorator 

Decorator패턴은 런타임시 기능을 확장하기 위해 객체에 추가적인 책임을 동적으로 연결한다. 아래 예를 살펴 보도록 하자. 

```kotlin
//1
interface Salad {
  fun getIngredient(): String
}

//2
class PlainSalad : Salad {
  override fun getIngredient(): String {
    return "Arugula & Lettuce"
  }
}

//3
open class SaladDecorator(protected var salad: Salad) : Salad {
  override fun getIngredient(): String {
    return salad.getIngredient()
  }
}

//4
class Cucumber(salad: Salad) : SaladDecorator(salad) {
  override fun getIngredient(): String {
    return salad.getIngredient() + ", Cucumber"
  }
}

//5
class Carrot(salad: Salad) : SaladDecorator(salad) {
  override fun getIngredient(): String {
    return salad.getIngredient() + ", Carrot"
  }
}
```

위 예제에서 보여주는 내용은 아래와 같다. 

1. `Salad`인터페이스는 각종 재료들을 알게 해준다. 
2. 모든 샐러드에는 기반이 필요하다. 이 기반에는 `Argula & Lettuce`인 `PlainSalad`이다.
3. `SaladDecorator`는 `PlainSalad`에 더 많은 토핑을 추가하도록 해준다. 
4. `Cucumber`는 `SaladDecorator`를 상속한다. 
5. `Carrot`는 `SaladDecorator`를 상속 한다. 

`SaladDecorator`클래스를 사용하면 `PlainSalad`를 변경하지 않아도 쉽게 샐러드를 확장 할 수 있다. 런타임중 샐러드 데코레이터를 제거하거나 추가할 수도 있다. 사용 방법은 아래와 같다. 

```kotlin
val cucumberSalad = Cucumber(Carrot(PlainSalad()))
print(cucumberSalad.getIngredient()) // Arugula & Lettuce, Carrot, Cucumber

val carrotSalad = Carrot(PlainSalad())
print(carrotSalad.getIngredient()) // Arugula & Lettuce, Carrot
```

## 3.4 Composite

같은 개체들로 구성된 나무와 같은 구조들을 표현하려하는 경우 Composite패턴을 사용 한다. Composite패턴은 composite와 leaf두가지 유형의 객체가 있을 수 있다. composite 개체는 추가 개체를 가질 수 있는 반면 leaf개체는 마지막으로서 다른 개체를 가지지 않는다. 

아래 예제 코드를 보자. 

```kotlin
//1
interface Entity {
  fun getEntityName(): String
}

//2
class Team(private val name: String) : Entity {
  override fun getEntityName(): String {
    return name
  }
}

//3
class Raywenderlich(private val name: String) : Entity {
  private val teamList = arrayListOf<Entity>()

  override fun getEntityName(): String {
    return name + ", " + teamList.map { it.getEntityName() }.joinToString(", ")
  }

  fun addTeamMember(member: Entity) {
    teamList.add(member)
  }
}
```

코드에 대해서는 아래를 참고 하자. 

1. Composite패턴의 인터페이스 엔티티인 `Component`이다. 
2. `Team`클래스는 엔티티를 구현한다. 이 클래스는 Leaf가 된다. 
3. `Raywenderlich`는 엔티티 인터페읏도 구현한다. 이 클래스는 composite가 된다. 

논리적으로나 기술적으로 조직(이 경우에는 `Raywenderlich`)은 팀에 엔티티를 추가 한다. 사용 방법은 아래와 같다. 

```kotlin
val composite = Raywenderlich("Ray")
val ericTeamComposite = Raywenderlich("Eric")
val aaqib = Team("Aaqib")
val vijay = Team("Vijay")
ericTeamComposite.addTeamMember(aaqib)
ericTeamComposite.addTeamMember(vijay)
composite.addTeamMember(ericTeamComposite)
print(composite.getEntityName()) // Ray, Eric, Aaqib, Vijay
```

