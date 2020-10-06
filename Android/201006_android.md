## 안드로이드에서 사용자 모듈 추가 하기

clean-architecture를 기반으로 MVVM과 같은 패턴을 적용 하였을때에 사용자 모듈을 추가 하고 어떻게 사용 할지에 대해 간단하게 정리하였다. 

### 1. 추가하려는 모듈은 무엇인가? 

예제로 추가되는 모듈은 간단한 repository로서 원격 서버 혹은 로컬에 접근하여 api콜 혹은 db를 통해 데이터를 얻는 모듈이다. 그리고 해당 모듈에서는 `Retrofit`이나 `Room`, `Datastore`와 같은 인스턴스들을 필요로 할 것 이다. 

우선 추가될 모듈에 대해서 유즈케이스가 분석되었다면, 추상화를 진행하여 인터페이스화 한다. 아래는 예제로 사용될 모듈의 추상화된 인터페이스 예제 이다. 

```kotlin
interface MyMusicRepository {
    fun retrieveFavoritesMusics(id: String): Single<List<Music>>

    fun addFavorites(music: Music): Completable

    fun removeFavorites(musicId: Long): Completable
}
```

예제인 대상 인터페이스에서는 사용자가 등록한 즐겨찾기 음악에 대한 제어를 책임지고 있다. 그 책임에 따라 기능을 나누어 각 추상 메소드로 정의 한다. 기능에 따라 나뉘어진 각 메소드들은 자신의 책임이 뭔지 명확하게 설명할 수 있는 메소드 이름을 갖게 되며 구현 또한 그에 대한 코드만을 가져야 한다. 

- `retrieveFavoritesMusics()`은 사용자가 등록한 즐겨찾기 음악의 목록을 가져온다. 대상은 서버 api콜을 통해서 가져온뒤 이를 캐싱할 수도 있다. 만약 음악 목록이 없다면 비어있는 목록을 반환한다. 
- `addFavorites(Music)`은 사용자의 즐겨찾기 음악 목록에 새로운 음악을 추가한다. 
- `removeFavorites(Long)`은 사용자의 즐겨찾기 음악 목록에 등록된 음악을 제거 한다. 
- 추상화된 Repository는 Rx를 기반으로 비동기작업을 수행하게 될 것 이며, 그에 따라 반환 타입은 Rx의 타입에 의존한다. 

추가적으로 필요한 경우 생성될 모듈의 라이프 사이클에 대해서 정의 한다. 이 모듈이 어느 상황에서 언제부터 인스턴스가 생성되어 어느때까지 유지되어야 하는지 정의 해 주어야 한다. 예를 들면, 로그인과 같은 유저의 세션 이나 Application단위의 싱글톤 혹은 특정 Activity가 보여지고 있는 동안 인스턴스가 유지되어야 하는지 등이 있다. 

Dagger나 Koin과 같은 Dependency Injection 도구를 사용 하고 있다면 Scope를 정의 하고 적용하여 쉽게 해당모듈의 라이프 사이클을 안드로이드 컴포넌트의 라이프 사이클에 동기화 하여 사용 할 수 있다. 하지만 그렇지 않다면 해당 인스턴스의 시작과 끝을 직접 안드로이드 컴포넌트의 라이프사이클 콜백 메소드에 추가해 주어야 한다. (예를 들면 시작은 Activity의 `onCreate()`메소드 에서 시작되어 끝은 `onDestroy()`등의 콜백에서 해주면 된다) 

### 2. 모듈의 구현은 어떻게 하는가? 

예제 모듈인 `MyMusicRepository`은 추상화된 인터페이스이므로 직접 사용할 수 없다. 그러므로 이를 상속받은 클래스를 구현하여 추상화된 함수들을 구현해 주어야 한다. 그리고 원격 서버 api콜을 위한 retrofit과 같은 api혹은 Room과 같은 인스턴스를 필요로 할 수 있다. 만약 Dagger나 Koin과 같은 DI tool을 사용 중 이라면 해당 도구를 이용해 인스턴스를 주입해 주기만 하면 된다. 

아래는 `MyMusicRepository`의 구현 예제이다. 복잡한 코드를 추가하지 않고 간단히 서버에서 api콜을 통해서 작업을 수행하도록 하였다. 추가적으로 Room 혹은 Datastore등을 통해서 추가적인 비즈니스 로직을 수행할 수 있음을 간과 해서는 안된다. (하지만 추상화된 메소드에서는 자신의 책임에 맞게 해당하는 작업만 수행해야 한다. 책임에서 벗어난 코드가 추가되면 미래에서의 유지, 보수에 큰 악영향을 줄 수 있다)

```kotlin
class MyMusicRepositoryImpl(
    private val api: MyMusicApi
) : MyMusicRepository {
    override fun retrieveFavoritesMusics(id: String): Single<List<Music>> {
        return api.retrieveFavoritesMusics(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun addFavorites(music: Music): Completable {
        return api.addFavorites(music)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun removeFavorites(musicId: Long): Completable {
        return api.removeFavorites(musicId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}
```

위 예제에서는 구현된 클래스에서 `MyMusicApi`을 클래스의 생성자를 통해 주입받아 사용함을 알 수 있다. 아니 사실, 이 클래스에서는 `MyMusicApi`가 DI도구를 통해서 진짜로 주입받고 있는지 몰라야 한다. 외부에서 인스턴스를 어떻게 생성해서 생성자에 넘기는지 이 클래스에서는 몰라야 하기 떄문이다. (만약 그에 대해서 알게되면 외부 클래스에 대한 의존이 생기기 때문에 유지, 보수에 좋지 않다)

### 3. 모듈은 어떻게 사용 되는가? 

모듈은 ViewModel이나 Presenter등 에서 사용 될 것 이다. DI를 사용 하고 있다면 해당 모듈을 주입받아 그대로 사용 하면 되며 그렇지 않다면 ViewModel이나 Presenter를 생성할때 생성자를 통해 모듈 인스턴스를 생성해서 전달 하면 된다. ViewModel과 Dagger를 사용한다면 아래와 같이 사용할 것 이다.

```kotlin
class @Inject MyMusicListViewModel(
    private val repository: MyMusicRepository
): MyViewModel() {
    private val _musics = MutableLiveData<List<Music>>(listOf())
    val musics: LiveData<List<Music>>
        get() = _musics

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    // ... 

    fun retrieveMyMusics(id: String) {
        addDisposer(
            repository.retrieveFavoritesMusics(id)
                .doOnSubscribe { _isLoading.postValue(true) }
                .doOnFinally { _isLoading.postValue(false) }
                .subscribe(
                    { 
                        _musics.value = it
                    }, 
                    { e -> 
                        // .. error handling
                    }
                )
        )
    }

    // ... 
}
```

### 4. 결론

위처럼 구현되어진 모듈은 특정 ViewModel이나 Presenter, View등에 의존을 갖지 않아 다른 안드로이드 컴포넌트를 대상으로 재사용 할 수 있다는 장점이 있다. 그리고 모듈을 확장하여 사용하려면 추가될 기능들에 대한 모듈에 대한 인터페이스를 다시 추상화 하고 이미 만들어진 모듈을 상속받거나 인스턴스를 내부에 갖고 api메소드를 만들어 호출 하는 형태로 유연성이 좋다고 할 수 있다. 

추가적으로. 정의된 모듈에대해서는 이미 요구사항 분석과 유즈 케이스정의가 끝났다면 TDD를 통해서 테스트코드부터 작성하는 방법을 적용할 수 있다. 이는 개발중 요구사항과 유즈-케이스의 변경이 많이 없을경우 가능하다 할 수 있겠다. 
