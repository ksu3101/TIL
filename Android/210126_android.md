## Android Parcelable: There's a better way

> 이 글은 Christopher Keenan의 [Android Parcelable: There's a better way](https://chrynan.codes/android-parcelable-theres-a-better-way/)을 번역 하였다. 

### TL;DR

`kotlinx.serialization`을 이용하여 데이터를 Android의 `Parcel`으로 직렬화 하여 서로 다른 안드로이드의 컴포넌트간 전달 할 수 있도록 하는 새로운 parcelable라이브러리를 소개 한다.

### Android Parcelable 

Android의 Parcelable은 `Parcel`을 구현하여 객체의 값을 쓰거나 읽을수 있도록 해준다. Android의 Parcel은 Activity나 Fragment와 같은 다양한 안드로이드의 컴포넌트간에 데이터를 전송할수 있게 해주는 컨테이너이다. 따라서 Parcelable 인터페이스를 구현하면 서로 다른 안드로이드의 컴포넌트간에 객체들을 전달할수 있게 된다. 

하지만, Parcelable인터페이스를 구현하는 방식은 구현방식이 장황하며 오류가 발생하기 쉽다. Parcelable인터페이스를 구현하기 위해서는 `describeContents()`와 `writeToParcel()`을 구현해야 하며 `Parcelable.Creator`인터페이스를 구현한 `CREATOR`라는 정적 필드도 구현 해 주어야 한다. 또, `Parcelable.Creator`인터페이스에는 구현해야 하는 두가지 메소드인 `createFromParcel()`과 `newArray()`가 있다. 그리고 인터페이스의 구현은 순서대로 진행 해야 하며 그렇지 않을 경우 작업은 더 복잡해 진다. 아래는 Parcelable의 적용 예를 Kotlin으로 전환한 예제 이다. 

```kotlin
class MyDataClass: Parcelable {
    private val mData: Int

    constructor(mData: Int) {
        this.mData = mData
    }

    private constructor(parcel: Parcel) {
        this.mData = parcel.readInt()
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeInt(mData)
    }

    companion object {
        val CREATOR: Parcelable.Creator<MyParcelable> = object: Parcelable.Creator<MyParcelable> {
            override fun createFromParcel(parcel: Parcel): MyParcelable = MyParcelable(parcel)

            override fun newArray(size: Int): Array<MyParcelable?> = arrayOfNulls(size)
        }
    }
}
```

보다시피 단일 객체를 다른 Activity나 Fragment등 컴포넌트에 전달 할 수 있으려면 많은 코드들이 필요하다. 그래서 객체를 "분할"하거나 "직렬화"하기 위한 더 쉬운 방법이 필요 한 것 이다. 

### Java Serializable

Java의 `Serializable`인터페이스는 안드로이드에서 사용할 수 있으며 다른 컴포넌트간에 전달 할 수 있도록 객체를 직렬화하는 다른 방법을 제공하고 있다. 이 접근 방법은 Parcelable을 구현하는 방법보다 훨씬 간단 하다. 필요한 것은 `Serializable`인터페이스를 구현하는 것 뿐이다.

```kotlin
class MyDataClass(val mData: Int): Serializable
```

`Serializable`을 구현하는 방식이 Parcelable을 구현하는 방법보다 훨씬 간단함은 분명하지만 그래도 비용이 발생하는 것 은 어쩔 수 없다. [이 StackOverflow의 답변](https://stackoverflow.com/a/23647471/1478764)에서 자세히 설명하듯이 `Serializable`은 리플렉션을 사용하여 객체를 직렬화 하므로 다른 방법들보다 더 느리다. 일반적으로 일반 데스크탑에 비해 처리 능력이 제한적인 안드로이드에서는 다른 것 보다도 성능이 먼저 우선시 된다. 따라서 이 경우에는 가독성을 위해 성능일 희생하는 방법은 최선의 방법이 아니다. 그렇다면 객체를 전달 하는 다른 방법에 대해서 더 알아보도록 하자. 

### Kotlin Android Extensions

Kotlin Android Extension Gradle 플러그인에서는 `@Parcelize`어노테이션을 이용하여 안드로이드에서 객체를 직렬화 하는 방법을 제공 한다. 이 플러그인은 사용자를 위해 자동으로 코드들을 생성해 주어 작성해야 하는 코드의 양을 크게 줄여준다. `@Parcelize`어노테이션을 데이터 클래스에 추가 하고 `Parcelable`인터페이스를 구현하기만 하면 설정이 끝난다. 

```kotlin
@Parcelize
class MyDataClass(val mData: Int): Parcelable
```

이 방법은 매우 단순해진 접근 방식이며 여전히 `Parcelable`인터페이스를 구현하고 있으므로 Java의 `Serializable`방식 보다 런타임 성능이 훨씬 더 좋다. `@Parcelize`어노테이션을 사용한 사례에서는 문제 없이 잘 작동하지만 사용자 특정화된 직렬 방식이 필요한 복잡한 객체가 있다면 어떻게 해야 할까? 이 경우에는 `Parcelable`인터페이스를 완전히 구현하는 것 보다 더 쉬운 방법으로 객체를 수동으로 직렬화할수 있는 `Parceler`인터페이스를 구현 하면 된다. 

```kotlin
object MyDataClassParceler: Parceler<MyDataClass> {
    override fun create(parcel: Parcel) = MyDataClass(parcel.readInt())

    override fun MyDataClass.write(parcel: Parcel, flags: Int) {
        parcel.writeInt(mData)
    }
}
```

이 방식을 사용하면 객체를 간단히 Parcel화 할 수 있으며 더 복잡한 상황에 대해 유연하게 처리 할 수 있게 해준다. 그러나 Kotlin Multi-platform의 인기가 높아지면서 데이터 클래스는 일반적으로 공통 코드로 생성 되어 `@Parcelize`어노테이션과 같이 안드로이드 컴포넌트 요소와는 분리 해야 할 필요가 있다. 이 경우 공통 모듈에서 parcel을 적용 하려는 모둔 데이터 클래스에 대해 안드로이드 모듈에서 Parceler를 만들어줘야 하므로 이 플러그인을 적용하려 했던 목적에서 벗어나게 된다. 

### moko-parcelize 

Kotlin Multi-Platform에 `@Parcelize`기능을 제공 하는 [moko-parcelize](https://github.com/icerockdev/moko-parcelize)라는 IceRock Development의 라이브러리가 있다. 이 라이브러리를 사용 하면 공통 모듈에 이전에 했던 방식처럼 데이터 클래스를 Parcel할 수 있다. 

```kotlin
@Parcelize
class MyDataClass(val mData: Int): Parcelable
```

`@Parcelize`방법의 남아 있는 문제는 복잡한 클래스의 경우 여전히 수동으로 `Parceler`를 작성해야 한다는 것 이다. 그래야만 데이터가 Parcel으로 직렬화 되기 때문이다. 이는 안드로이드 컴포넌트와 함께 사용할 데이터를 직렬화하는데 도움이 되지만 데이터를 JSON으로 만드는것 과 같은 다른 상황에 대해서는 데이터를 직렬화 하는데 도움이 되지 못한다. 

### kotlinx.serialization

`kotlinx.serialization`라이브러리는 Kotlin모델과 다른 타입간에 직렬화/역-직렬화 하는 방법들을 제공 한다. 이 라이브러리는 JSON, Protobuf, CBOR, Hocon등을 지원 한다. 그리고 Kotlinx Serialization에 XML지원을 추가하는 xmlutil라이브러리와 같은 다른 라이브러리도 있다. 

일반적으로 프로젝트에서는 `kotlinx.serialization`을 이용하여 HTTP요청을 처리 하기 위해 JSON과 Kotlin의 데이터 직렬화를 처리 한다. 복잡한 데이터의 경우 사용자 지정 `KSerializer`를 만들 수 있다. 여기에서 오는 문제는 복잡한 데이터의 경우 앱 전체에서 데이터를 직렬화 할 수 있도록 사용자 지정 `KSerializer`및 사용자 정의 `Parceler`를 만들어야 한다는 점 이다. 이러한 중복은 코드가 장황해지고 오류가 발생하기 쉬워진다. 그래서 데이터를 처리 하는 `kotlinx.serialization`를 위한 커스텀 인코더와 디코더가 있다면 어떨까? 이럴 경우 여러 사용자 정의 serializer의 중복이 제거 될 것이다. 

### chRyNaN/parcelable

원본 글 작성자는 `kotlinx.serialization`의 동료격으로 [`parcelable` 라이브러리](https://github.com/chRyNaN/parcelable)를 만들었다. Android Parcel에서 쓰기 및 읽기를 처리 하는 사용자 지정 인코더 및 디코더를 제공 한다. 즉, `@Serialiable`클래스는 안드로이드 컴포넌트와 함께 작동하며 모든 사용자 지정 `KSerializer`도 함께 작동 한다. 

라이브러리의 사용 방법은 매우 간단하다. 

- `kotlinx.serialization`라이브러리를 사용하여 `@Serializable`(외 필요한 `KSerializer`을 만든다.
  ```kotlin
  @Serializable
  data class MyDataClass(
      mData: Int
  )
  ```
- `Parcelable`객체를 만들거나 기본 값을 사용 한다. 
  ```kotlin
  val parcelable = Parcelable {
      serializersModule = mySerializersModule
  }

  // 혹은, 
  val parcelable = Parcelable.Default
  ```

- 그런 다음 평소처럼 `Intent`와 `Bundle`을 통해 데이터를 전달 하지만 `Parcelable`인스턴스를 추가 매개 변수로 제공 한다. 

  ```kotlin
  // Put
  intent.putExtra(key, myModel, parcelable)
  bundle.putParcelable(key, myModel, parcelable)

  // Get
  val myModel = intent.getParcelableExtra(key, parcelable)
  val myModel = bundle.getParcelable(key, parcelable)
  ```

그리고 이게 전부이다. 사용자 정의 직렬화가 필요한 경우 `kotlinx.serialization`라이브러리에 대한 사용자 지정 `KSerializer`를 만들고 특정 인코더/디코더를 사용하지 않는 한 안드로이드의 Parcel에서 작동 한다. 여기에서는 중복 직렬화 로직, 안드로이드의 특정 컴포넌트 및 추가적인 어노테이션 프로세서가 필요하지 않다. 

### Conclusion

서로 다른 안드로이드 컴포넌트간에 데이터를 전달 하기 위해 Parcel을 사용하는 방법에는 여러가지가 있다. 가장 간단한 방법은 새로운 parcelable과 `kotlinx.serialization`을 사용 하는 것 이다. 이러한 라이브러리를 함께 사용하면 JSON이나 Http응답, 안드로이드의 컴포넌트간 데이터 전달을 위한 Parcel으로의 데이터 직렬화를 쉽게 사용할 수 있다. 