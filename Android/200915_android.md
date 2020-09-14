## SharedPreference에서 DataStore Preference으로 마이그래이션 

[Bevan Steele의 'DataStore Preferences and migrating from SharedPreferences'](https://www.rockandnull.com/jetpack-datastore/)을 번역해보았다. 

### 1. 시작 하며

최근 안드로이드 개발 시점에 있어 `SharedPreferences`를 사용한 개발자는 API에 업데이트가 절실히 필요함을 알고 있다. 동기화 되어 있을떄에만 데이터를 읽을 수 있고 오류를 알려주는 기능도 없었으며 트랜잭션 커밋(Tranjaction commit)을 수행할 수 있는 방법또한 없었다. 

대부분의 경우 비동기 계층을 추가하고 가능한 오류를 처리하기 위해서는 `SharedPreferences`을 사용할때 추상화하여 사용 한다. Jetpack은 최근에 이런 문제들을 해결하는 것 을 목표로 하는 [`DataSource`](https://android-developers.googleblog.com/2020/09/prefer-storing-data-with-jetpack.html)를 도입 하였다. 즉, 작은 데이터 비트를 저장하는 편리하고 현대적인 방법을 제시한 것 이다.

`DataSource`라이브러리는 DataSource Preferences및 Proto DataSource, 이렇게 두 부분으로 나뉘어져 있다. 저장된 값에 대해 타입 안정성을 제공하지 않는 DataSource Preferences만 다루며 SharedPreferences를 사용중인 경우 마이그레이션이 더 쉽다. 

### 2. 설정 

DataStore를 사용할 모듈의 `build.gradle`파일에 아래와 같이 의존을 추가 한다. 아래를 복사-붙여넣기 할 경우, 의존 라이브러리의 마지막 버전을 확인 하도록 한다. 이는 큰 작업이 아니긴 하였지만 공용 라이브러리로 추출할 가치가 없기 때문에 계속 반복할수밖에 없었다. 

```
implementation "androidx.datastore:datastore-preferences:1.0.0-alpha01"
```

### 3. 환경 설정 읽기

먼저 `DataStore`인스턴스를 생성한다. 이때 필요한 것은 오로지 단 한개의 유니크한 이름으로 문자열로 된 식별자 뿐이다. `Preferences`는 `SharedPreferences`와 같은 키-값으로 이루어진 저장소를 구현하기 위해 라이브러리에서 제공 하는 클래스 이다. 마지막으로 `context`의 `createDataStore()`라는 확장 함수를 이용해 `DataStore`의 인스턴스를 생성한다. 

```kotlin
private val dataStore: DataStore<Preferences> = context.createDataSotre(name = "movies")
```

이제 사용하려는 각 키에 대해 `preferencesKey`를 이용해 생성한다. `preferencesKey`을 이용해 만들수 있는 키의 타입은 `Int`, `Long`, `Boolean`, `Float`, `String`중 하나만 사용할 수 있다. 그리고, `Set`에 대해서도 사용할 수 있는 `preferencesSetKey`가 있다.

```kotlin
val LAST_MOVIE_ID = preferencesKey<Int>("last_movie_id")
```

요청한 키에 대해서 값을 `Flow`를 통해서 읽게된다. 값 데이터를 읽는동안 `IOException`이 발생할 수 있으므로 이 예외를 적절하게 처리하는 코드가 있으면 좋을것이다. 아직 값이 존재하지 않을 경우(즉, `null`이 반환될 경우)에 대해 처리하는 것도 잊지 말아야 한다. 

```kotlin
val lastMovieIdFlow: Flow<Int> = dataStore.data
    .catch { exception ->
        if (exception is IOException) {
            emit(-1)
        } else {
            throw exception
        }
    .map { preferences ->
        preferences[LAST_MOVIE_ID] ?: -1
    }
```

`ViewModel`에서 이 값을 사용하려면 `lastMovieIdFlow.asLiveData()`또는, `lastMovieIdFlow.collect()`를 사용할 수 있다. `Flow`에 대해서 알고 싶다면 [이 링크](https://www.rockandnull.com/kotlin-flow/)를 참고 하자. 

- `Flow`에 대해서 간단하게 말하자면 kotlin에서 제공하는 coroutine builder라고 한다. 기본적으로 비동기로 동작하며 Cold stream으로 lazy하게 동작 한다. 

### 4. 환경 설정 쓰기

쓰기를 할때엔 suspend함수인 `edit()`를 사용해서 쓰기를 하면 된다. 역시 `IOException`이 발생할 수 있으므로 핸딜랑 하기 위한 코드가 있으면 좋다. 

```kotlin
dataStore.edit { preferences ->
    preferences[LAST_MOVIE_ID] = currentMovieId
}
```

- `suspend`함수는 코루틴에서 사용되는 키워드이며, 해당 함수는 현재 사용되는 스레드를 차단하지 않고 코루틴의 실행을 일시 중단할 수 있다는 것 이다. 

### 5. 마이그레이션 

이미 `SharedPreferences`를 사용하고 있을 경우 `DataStore`으로 마이그레이션 하기 위해서는 `DataStore`인스턴스를 생성 할 때 이전 `SharedPreferences`이름을 전달 하기만 하면 마이그레이션이 자동으로 수행 된다. 마이그레이션을 진행했다면(예를 들어 마이그레이션 코드 작성 후 실행했을때 예외가 발생하지 않음) `SharedPreferences`사용을 중단하고 `DataStore`를 이용해 기존 키를 이용해 읽기/쓰기를 할 수 있다. 

```kotlin
private val dataStore: DataStore<Preferences> =
    context.createDataStore(
        name = "movies",
        migrations = 
          listOf(SharedPreferencesMigration(context, "shared_preferences_name"))
    )
```

