## View Binding Android - 3 Major Benefits

> 이 글은 vlad sonkin의 [3 Major Benefits Of View Binding Android](https://vladsonkin.com/3-major-benefits-of-view-binding-android/?utm_source=feedly&utm_medium=rss&utm_campaign=3-major-benefits-of-view-binding-android)을 번역 하였다. 

안드로이드의 뷰 바인딩(View binding)을 사용하면 뷰 관련 작업이 더 쉬워지며 `findViewById()`및 기타 다른 솔루션들에 대해 잇점이 있다. 그동안 인기있었던 Kotlin Synthetics가 Kotlin 1.4에서 지원 중단 되었기 때문에 이제 뷰 바인딩은 더 중요하게 느껴질 수 있다. 

이 게시글에서는 Android View Binding을 살펴보고 다른 모든 솔루션과 비교 하여 어떻게 사용할 수 있는지 살펴 보도록 한다. 

### 1. What is View Binding in Android?

뷰 바인딩은 뷰와 더 나은 상호작용을 제공하는 기능이다. 뷰 바인딩은 레이아웃 xml에서 ID가 있는 뷰에 대한 모든 참조를 갖는 각 Binding클래스를 생성한다. 이 생성한 클래스의 이름은 Pascal Case의 xml이름과 끝에 Binding을 갖는 단어 이다.
예를 들어 `activity_main.xml`은 `ActivityMainBinding`클래스가 된다. 

#### 1.1 Why do we need it?

뷰에 대한 참조를 얻는 방법에 대해 많은 솔루션이 있었으며 이 들이 필요한 이유가 궁금할 수 있다. Null-Safety, 바인딩 속도, 컴파일시 안정성 에 따라 다양한 솔루션들을 비교해 보면 아래와 같다. 

||ButterKnife|Kotlin Synthetics|Data Binding|findViewById|ViewBinding|
|---|---|---|---|---|---|---|
|속도|X*|O|X*|O|O|
|Null-safe|X|X|O|X|O|
|컴파일시 안정성|X|X|O|O**|O|

- `*` : ButterKnife및 데이터 바인딩은 어노테이션 기반 접근 방식을 사용하므로 속도가 느리다. 
- `**` : `findViewById()`는 더이상 뷰의 타입을 캐스팅 할 필요가 없기 때문에 API26이후로 컴파일 시 안전 하다. 

뷰 바인딩은 다른 솔루션의 가장 좋은 방법을 갖고 단점을 피하고 있음을 확인 할 수 있다. 

### 2. How to add View Binding

뷰 바인딩은 Gradle플러그인에 포함되어 있기 때문에 라이브러리를 따로 필요로 하지 않으며 Android Studio 3.6이상을 사용하기만 하며 아래와 같이 `build.gradle`에 추가 해주면 된다:

```
android {
    // ...
    buildFeatures {
        viewBinding = true
    }
}
```

### 3. How to use View Binding in Activity

Activity에서의 사용법은 아래처럼 간단하다. 

```kotlin
class MainActivity: Activity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }
        binding.title.text = "View Binding"
    }
}
```

위 예제에 제공된 `inflate()`메소드를 이용하여 Binding클래스의 인스턴스를 만든 다음 뷰 관련 항목에 사용 한다. 

### 4. View Binding Android Fragment

Fragment의 경우 Fragment인스턴스가 View의 인스턴스보다 오래 지속될 수 있으므로 더 많은 주의가 필요 하다. 뷰의 라이프 사이클동안 바인딩을 생성 하고 이를 해제 했는지 확인후 해제해 주어야 한다. 만약 그렇지 않으면 뷰에 대한 참조가 지속되어 메모리 누수가 발생 한다. 

> Fragment의 View Binding시 메모리 누수에 관련된 내용은 [이 링크의 글](https://github.com/ksu3101/TIL/blob/master/Android/200828_android.md)을 참고 하면 된다. 

```kotlin
class MainFragment : Fragment() {
    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = MainFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

예제 코드에서 보다시피 뷰 바인딩 인스턴스의 초기화는 동일 하며, `onDestroyView()`콜백 메소드에서 바인딩 인스턴스를 해제함을 확인 할 수 있다. 

### 5. How to use View Binding in adapters

뷰 바인딩의 사용법은 Activity및 Fragment에 국한되지 않으며 XML레이아웃을 사용하는 모든 곳 에서 사용할 수 있다. 일반적인 예로는 RecyclerView의 Adapter이다. 아래 예제처럼 ViewHolder에 뷰 바인딩을 사용할 수 있다. 

```kotlin
override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = 
    HeaderViewHoler(
        LayoutInflater.from(
            parent.context,
            parent,
            false
        )
    )

class HeaderViewHolder(
    val binding: ItemHolderBinding
): RecyclerView.ViewHolder(binding.root) {
    fun bind(item: HeaderData) {
        binding.name.text = item.name
    }
}
```

### 6. Summary 

여기에서 보았듯이 뷰 바인딩은 안정성과 속도 측면에서 뷰의 인스턴스를 얻는 작업을 처리하는데 좋은 방법이다. 또한, 사용이 간단하며 라이브러리가 거의 모드 작업을 수행해 준다. 유일한 단점은 Fragment에서의 뷰 바인딩 인스턴스 해제 작업이다. Fragment에 뷰 바인딩 인스턴스를 생성했을 때 `onDestroyView()`콜백 메소드에서 바인딩 객체를 해제하는 것을 잊지 말도록 하자. 
