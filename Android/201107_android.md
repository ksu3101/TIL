## Goobye Gson, Hello Moshi

> 이 글은 Reza의 [Goodbye Gson👋, Hello Moshi🤗](https://proandroiddev.com/goodbye-gson-hello-moshi-4e591116231e)을 번역 하였다. 

이 글에서는 Android와 Kotlin을 위해 새로운 직렬화 라이브러리인 Moshi로 마이그레이션 하는 것 에 대한 글 이다. 따라서 Moshi가 왜 Gson보다 나은지 이유에 대해 알아보며, 마이그레이션 하는 방법들에 대해 살펴보도록 한다. 

### 1. Gson, What? 

[Gson의 Github 리포지터리](https://github.com/google/gson)를 살펴보면 아래와 같은 설명들이 있다. 

> Gson은, Java객체를 JSON으로 직렬화/역직렬화 하는 라이브러리 이다. 

- 2008년 이후로 출시되어 12년동안 사용 되었고, 더이상 업데이트는 되지 않고 있다. 사실상 죽은 프로젝트이며 Kotlin으로 당연히 작성되어있지 않아 더이상 현대적인 라이브러리 라고 할 수 는 이젠 없다. 

- 최근에는 업데이트 되지 않았으며 마지막으로 진행 된 커밋은 대규모 업데이트나 버그 수정처럼 보이지 않는다. 이미 수많은 프로젝트에서 잘 작동하고 있는 라이브러리를 건드리지 않는 것이 실제로 의미가 있기는 하지만 Gson의 현재 상황은 죽은 프로젝트 인것 으로 보인다. 

- Gson은 Moshi(약 500개)보다 거의 두배인(약 1036개) 메소드를 갖고 있다. 

- Footprint 측면에서 Gson은 APK파일에 약 300kB의 사이즈가 추가되며, Moshi는 약 120kB가 추가 된다. 

- Gson은 리플랙션(Reflection)을 사용하여 JSON문자열을 직렬화/역직렬화 한다. 

- Gson은 필드들에 대한 기본값을 지원하지 않는다. 네트워크의 응답 Json문자열에 필드가 없어 기본값을 null이 아닌 다른 값으로 설정하려 할 수가 없다. 

이제 Gson이 실제로 죽은 프로젝트라고 가정해 보자. 이제 어떻게 해야 할까? 몰론 Gson을 계속 영원히 사용하는 방법도 있다. 하지만 호기심 많은 개발자와 Kotlin애호가를 위해 Moshi를 사용해 볼 수 있다. 이제 Moshi가 무엇인지 그리고 왜 사용해야 하는지 알아보도록 하자. 

### 2. Moshi, Who? 

> Kotlin과 Java를 지원하는 최근에 만들어진 JSON 라이브러리. 

[Moshi의 Github 리포지터리](https://github.com/square/moshi)에서는 다음과 같이 설명하고 있다. Moshi는 현대적이고 Kotlin친화적이며 빠르고, 안정적이며 다양한 기능을 제공하고 있다고 한다. 그렇다면 Moshi의 장점에 대해 살펴 보도록 하자. 

#### 2.1 Better API: 

Moshi는 이 강력한 API들을 활용하여 깔끔하게 작업할 수 있는 더 좋은 API들을 제공 하고 있다. 

어노테이션(Annotation)을 통해 더 세밀하고 상황에 맞는 역 직렬화로 이어지는 데이터들을 어노테이션을 통해 제공 할 수 있다. 

- JSON데이터 에서 필드의 구문 분석을 사용자 지정하기 위해 고유한 주석을 만들어 사용할 수 있다. (`@ColorInt, @FromHexColor`와 같은 integer colour)

- [Moshi는 사람이 읽을 수 있는 더 나은 직렬화의 실패 로그](https://github.com/square/moshi#fails-gracefully)를 갖고 있다. 이는 앱이 실제 구동중 직렬화 예외가 발생하였을때 스택 추적(Stack traces)를 할때 큰 장점이 된다. 

- Moshi에서는 업 스트림 어뎁터(upstream adapter)에서 새로운 어뎁터를 만드는데 사용 할 수 있는 `newBuilder()`API를 제공 한다. 이는 `OkHttp`또는 `Okio`빌더와 유사한 개념이다. 별도의 어뎁터를 만들 수 있으므로 1K+모델을 구문 분석하는 방법을 알고 있는 새로운 어뎁터를 갖지 않기 때문에 이 부분에서 유용 하게 쓸 수 있다. 

- Moshi는 알 수 없는 데이터 타입에 대한 폴백(fallback)지원 및 다형성 데이터타입(polymorphic datatypes)에 대한 지원을 기본으로 제공 한다. 

- Moshi는 Kotlin용 Code-gen어뎁터를 제공한다. 어노테이션의 도움으로 직렬화/역직렬화를 훨씬 더 빠르게 만들수 있으며 Gson이 사용하는 오래된 리플랙션 방식을 사용하지 않는다. 

#### 2.2 Performance 

[Moshi는 Gson보다 빠르고 적은 메모리를 사용하며](https://zacsweers.github.io/json-serialization-benchmarking/), 스트림을 분석 하는 동안 알 수 없거나 원하지 않는 필드를 무시 하는데 도움이 되는 키를 미리 예측하거나 예상할 수 있는 Okio를 사용하고 있기 때문이다. ([참고할만한 좋은 기사](https://medium.com/@BladeCoder/advanced-json-parsing-techniques-using-moshi-and-kotlin-daf56a7b963d)) Retrofit에서도 Okio를 사용하고 있다. JSON직렬화 라이브러리(Moshi)와 네트워킹 라이브러리(Retrofit)은 버퍼를 공유 하기 때문에 네트워크를 호출 하고 응답을 직렬화 하는 동안 메모리 소비를 크게 낮춰준다. 

이제 마이그레이션이 실제로 어떻게 진행되는지 확인해 보도록 하자. 

1. `buid.gradle`에 아래와 같이 라이브러리 디펜던시를 추가 한다. 추가되는 디펜던시의 텍스트는 개발 환경에 따라 다를 수 있다. 

```
/*Moshi*/
def moshiVersion = "1.10.0"
implementation("com.squareup.moshi:moshi:$moshiVersion")
kapt("com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion")
implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
```

2. 모든 `@SerializedName`어노테이션을 .`@Json`으로 변경 한다. 

```kotlin
data class MediaCandidateData(
    @SerializedName("width")
    val width: Int,

    @SerializedName("height")
    val height: Int,

    @SerializedName("url")
    val url: String
)
```

위와 같은 data 클래스의 `@SerializedName`을, 아래처럼 바꾼다. 

```kotlin
data class MediaCandidateData(
    @Json(name = "width")
    val width: Int,

    @Json(name = "height")
    val height: Int,

    @Json(name = "url")
    val url: String
)
```

> Gson과 다르게 각 필드의 이름과 실제 response JSON의 각 항목과 이름이 일치한다면 `@Json`필드를 사용하지 않아도 된다. 만약 response와 이름이 다르게 하고 싶다면 ` @field:Json(name = "date_time") val dateTime: String,`과 같이 사용하면 된다. 

3. data 클래스에 `@JsonClass(generateAdapter = true)`주석을 추가 한다. 

직렬화/역직렬화 JSON프로세스를 적용할 모든 data class에 주석을 추가 한다. 이렇게 할 경우 Moshi가 코드를 생성(code-gen)하고 리플렉션을 사용하지 않아 프로세스 속도가 더 빨라 진다. 

```kotlin
@JsonClass(generateAdapter = true)
data class MediaCandidateData(
    @Json(name = "width")
    val width: Int,

    @Json(name = "height")
    val height: Int,

    @Json(name = "url")
    val url: String
)
```

4. Gson인스턴스를 Moshi인스턴스로 변경 해 준다. 

예를 들어 Dagger와 같은 DI도구를 통해 JSON라이브러리의 인스턴스를 제공 하고 있다면 Moshi로 변경 해 준다. 

```kotlin
@Singleton
@Provides
fun provideGson() = GsonBuilder().setLenient().create()
```

을 아래와 같이 변경 한다. 

```kotlin
@Singleton
@Provides
fun providesMoshi() = Moshi.Builder().build()
```

5. Retrofit에서 사용되는 JSON컨버터를 Moshi로 변경 해 준다. 

Dagger를 사용할 경우 아래처럼 변경 한다. 

```kotlin
@Provides
@Singleton
fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson) = Retrofit.Builder()
  .client(okHttpClient)
  .addConverterFactory(GsonConverterFactory.create(gson))
  .baseUrl(BASE_ENDPOINT)
  .build()
```

를, 아래처럼 변경 한다. 

```kotlin
@Provides
@Singleton
fun provideRetrofit(okHttpClient: OkHttpClient, mosh: Moshi) = Retrofit.Builder()
  .client(okHttpClient)
  .addConverterFactory(MoshiConverterFactory.create(mosh))
  .baseUrl(BASE_ENDPOINT)
  .build()
```

이것으로 끝 이다. 이는 Gson을 Moshi로 리팩토링 하는 간단한 예제 이다. Moshi를 약간 다르게 처리하는 경우가 있긴 하지만 일반적인 예는 위와 같을 것 이다. 

이제 Moshi에서 Gson과 다르게 처리되는 몇가지 사례들을 살펴 보도록 하자. 이는 프로젝트에서 Gson을 제거 하고 Moshi로 교체 하고 난 뒤 직면할 수 있는 사례들 이다. 

##### 2.3.1 Case - Alternate keys: 

JSON response 필드에 다른 키 들이 있을 수 있다. 이 경우 Gson을 이용하면 아래처럼 처리 했었다. 

```kotlin
@SerializedName(value = "id", alternate = ["pk"]) var userId: Long
```

그래서 대체될 배열의 필드에 다른 키를 추가하기만 되었지만 Moshi에서는 조금 다르다. 

```kotlin
@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class IdQualifier
```

대체키로 사용 될 어노테이션 클래스를 만들고 난 뒤,

```kotlin
@JsonClass(generateAdapter = true)
data class IdIntermediate(
  @Json(name = "id") val id: Long?,
  @Json(name = "pk") val pk: Long?
)
```

그리고 이 id필드를 보유 할 `Id`라는 다른 클래스를 만들고, 

```kotlin
@JsonClass(generateAdapter = true)
data class Id(val id: Long?)
```

대체키 주석을 해당 필드에 추가 한다. 

```kotlin
@IdQualifier val userId: Id?
```

이제 커스텀 JSON어뎁터를 정의 하고,

```kotlin
object JsonAdapter {
        @FromJson
        @IdQualifier
        fun fromJson(idIndeterminate: IdIntermediate): Id {
            return idIndeterminate.id?.let { Id(idIndeterminate.id) }
                ?: idIndeterminate.pk?.let { Id(idIndeterminate.pk) } ?: Id(null)
        }
        @ToJson
        fun toJson(@IdQualifier id: Id): IdIntermediate {
            return IdIntermediate(id = id.id, pk = null)
        }
    }
```

Moshi 인스턴스에 추가하면 된다. 

```kotlin
@Singleton
@Provides
fun providesMoshi() = Moshi.Builder().add(User.JsonAdapter).build()
```

추 후에는 아래와 같은 사례들을 추가로 업데이트 할 예정이다. (아직 업데이트 안한듯?)

Case #2: Serializing enums:  

Case #3: Manual Serializing an object  

Case #4: Manual Serializing list of objects  

Case #5: Parsing Polymorphic JSON list  
