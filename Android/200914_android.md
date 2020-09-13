## Moshi - 티끌같지만 도움이 될 수도 있는 팁 

Json의 직렬화 라이브러리로 Gson을 필두로 Jackson, Moshi등이 있다. Java에서는 모두 문제없이 잘 사용할 수 있지만 코틀린의 경우에는 조금 다르다. Gson의 경우 Kotlin의 Data클래스를 적용 할 때 `@SerializedName`을 사용해서 데이터 클래스의 필드 객체들의 이름을 정의해 주어야 한다. (과거엔 이 어노테이션을 작성해야만 했던것 으로 기억 하는데 지금은 다른지 확인 필요)

### 1. Gson의 `@SerializedName`

Gson을 이용해 kotlin의 data클래스에 직렬화 하여 매핑해주려면 아래처럼 해야 한다. 

```kotlin
data class Person(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val anme: String,
    @SerializedName("age") val age: Int,
    @SerializedName("phone_num") val phoneNumber: String
)
```

`phoneNumber`를 제외하고 entity와 필드들의 이름이 같음에도 불구 하고 어노테이션을 일일히 작성해주어야 하기 때문에 불편하다. 

### 2. [Moshi](https://github.com/square/moshi)에서는? 

```kotlin
data class Person(
    val id: Long,
    val anme: String,
    val age: Int,
    @field:Json(name = "phone_num") val phoneNumber: String
)
```

Moshi에서는 Gson과 다르게 필드들에 일일히 달아줄 필요가 없다. `phoneNumber`필드 처럼 서버와 네이밍을 다르게 가고 싶은 경우 에 한해서 어노테이션을 작성하는것 을 확인 할 수 있다. 

### 3. Moshi의 다른 지원 기능

#### 3.1 `@JsonQualifier`

특정 Json필드에 대해 사용자 정의 인코딩을 직접 제공 한다. 

예를 들어 아래와 같은 Json이 있을 떄, 

```json
{
  "width": 1024,
  "height": 768,
  "color": "#ff0000"
}
```

코틀린의 data class는 다음과 같이 매핑한다고 하면, 

```kotlin
data class Rectangle {
    val width: Int,
    val height: Int,
    val color: Int
}
```

Json의 `color`는 16진수 컬러 타입이고, 코틀린의 `color`는 10진수 정수로 이루어져 있다. 이 때 사용자 정의된 인코딩의 어노테이션을 정의 하여 어노테이션 프로세서에 필드의 인코딩 타입을 재정의 하게 할 수 있다. 

```kotlin
@Retention(RUNTIME)
@JsonQualifier
annotation class HexColor 
```

데이터 클래스는 아래처럼 사용자 정의될 필드에 대해 어노테이션을 추가 하면 된다. 

```kotlin
data class Rectangle {
    val width: Int,
    val height: Int,
    @HexColor val color: Int
}
```

마지막으로 이를 처리해줄 타입의 어뎁터(Type adapter)를 정의 해 준다. 

```kotlin
class ColorAdapter {
    @ToJson 
    fun toJson(@HexColor rgb: Int): String {
        return String.format("#%06x", rgb)
    }

    @FromJson
    @HexColor 
    fun fromJson(rgb: String): Int {
        return Integer.parseInt(rgb.subString(1), 16)
    }
}
```

Moshi에서 타입 어뎁터를 적용하려면 아래처럼 하면 된다. 

```kotlin
val moshi = Moshi.Builder()
                .add(ColorAdapter())
                .add(KotlinJsonAdapterFactory())
                .builder()
```

#### 3.2 Json중 필드를 무시하기 

Json의 필드 중 필요없는 필드를 무시하지 않으려면 `@Transient`어노테이션을 해당 Pojo클래스의 멤버에 추가 하면 된다. (자바에서는 `transient`키워드를 추가하면 된다)

```kotlin
data class Music(
    val id: Long,
    val title: Long,
    @Transient val uselessField: String
)
```

아니면 그냥 해당필드를 data클래스에서 선언하지 않아도 된다. 

```kotlin
data class Music(
    val id: Long,
    val title: Long
)
```

### 결론

[Danny Damsky의 게시물](https://medium.com/@dannydamsky99/heres-why-you-probably-shouldn-t-be-using-the-gson-library-in-2018-4bed5698b78b)을 보면 Gson을 비롯한 Moshi, Jackson등 여러 라이브러리를 같이 테스트 해보고 그 결과를 확인한 게시물이 있다. 게시물에서는 Moshi는 작은 라이브러리이며 속도도 괜찮은 라이브러리로 소개 하고 있다. 



