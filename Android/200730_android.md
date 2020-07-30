## Koin에서 Activity scope 적용기

Koin에서 Activity scope를 적용한 경험을 정리하였다. 이 내용을 잘 응용하면 custom scope를 적용하는 대 에도 무리가 없을것 으로 생각 된다. 

### 1. Module definition

`module()`DSL 함수를 이용하여 모듈을 정의 하는데 `scope()`를 이용해 Custom scope를 적용 한다. `scope()`함수 에 포함될 함수들의 구현들은 Custom scope의 라이프사이클에 동기화 되어 적용 된다. 

`scope()`함수에는 적용할 Custom scope에 대해 `named()`함수를 이용하여 이름을 적용해 퀄리파이어 처럼 사용할 수 있다. 이는 스코프를 구분하는데 사용 된다. 아래 예제코드의 경우 `named<T>()`와 같이 제네릭을 사용하여 클래스 자체를 이름으로 적용 하였다. 

```kotlin
val activityModules = module {
    scope(named<PokemonSearchActivity>()) {
        scoped<PokemonNavigationHelper> {
            PokemonNavigationHelperImpl(get<PokemonSearchActivity>())
        }
        viewModel { PokeSearchViewModel(get(), get(), get(), get()) }
        viewModel { PokeDetailViewModel(get(), get(), get()) }
    }
}
```

위 예제코드의 모듈 정의를 보면 다음과 같다. 

- `PokemonSearchActivity`의 라이프사이클을 갖는 커스텀 스코프를 적용한 모듈을 정의 한다. 
- `PokemonSearchActivity` scope에서는 `PokemonNavigationHelper`인스턴스가 해당 scope에만 사용 가능 하다. 
- `PokeSearchViewModel, PokeDetailViewModel` 또한 해당 scope에서만 사용 가능 하다. 

### 2. Scope declare

scope를 적용할 `Activity`에서 scope에 대한 라이프사이클을 적용해야 한다. 

```kotlin
class PokemonSearchActivity : AppCompatActivity() {
    // module에서 정의한 Activity scope를 얻는다. 
    private val activityScope =
        getKoin().getOrCreateScope(POKEMON_SEARCH_ACTIVITY_SCOPEID, named<PokemonSearchActivity>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pokesearch_activity)
        // 얻은 scope를 통해서 이 Activity를 사용할수 있음을 선언한다. 
        // 선언된 인스턴스가 이미 존재 할 경우 `overrdie = true`로 설정 하여 재정의 할 수 있도록 해준다. 
        activityScope.declare(this, override = true)
    }

    override fun onDestroy() {
        // Activity가 종료될 시점에 Acitivty의 scope도 같이 종료 시켜준다. 
        activityScope.close()
        super.onDestroy()
    }
}
```

정리하면, 모듈에서 정의한 custom scope를 특정 인스턴스의 create - destroy 각 시점에 맞추어 선언해주고, 종료시켜주는 것 이다. 

custom scope를 생성할때 사용되는 `POKEMON_SEARCH_ACTIVITY_SCOPEID`는 Scope에 대한 ID로서 예제에서는 문자열로 되어 있다. 

### 3. Examples

예를 들어 위 모듈에서 scope에 적용했던 ViewModel을 주입받는 방법을 보면 아래와 같다. 

```kotlin
class PokemonSearchFragment : BaseFragment() {
    private val activityScope = getKoin().getScope(POKEMON_SEARCH_ACTIVITY_SCOPEID)
    private val vm: PokeSearchViewModel by lazy {
        activityScope.getViewModel<PokeSearchViewModel>(requireParentFragment())
    }
    
    // ...
}
```

`Fragment`에서는 scope에 대한 인스턴스를 `POKEMON_SEARCH_ACTIVITY_SCOPEID`라는 scope id(문자열)을 통해 얻고 이를 이용해서 `getViewModel()`DSL 함수를 이용해 ViewModel인스턴스를 주입받음을 알수있다. 
