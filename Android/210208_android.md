## ViewBinding delegate functions

Fragment을 대상으로 ViewBinding해서 사용하려 할 때 메모리 누수를 방지 하기 위해 사용하는 필수적인 코드들이 있다. 

```kotlin
class SomeFragment: Fragment() {
    private var binding: SomeFragmentBinding? = null
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // 레이아웃을 인플레이트 하고 뷰의 인스턴스를 반환 한다. 
        binding = SomeFragmentBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 프래그먼트가 파괴 될 때 바인딩 인스턴스를 null로 처리 해 준다. 
        binding = null
    }
}
```

문제는 이 코드가 ViewBinding을 계속 사용 하게 될 경우 보일러 플레이트코드화 되어 계속 반복 된다는 것 이다. 

그렇다면 이 불편함을 아래처럼 해결해 보는것은 어떨까? 

```kotlin
class SomeFragment: Fragment() {
    private val binding by viewBinding(SomeFragmentBinding::bind)
}
```

Fragment의 콜백 메소드와 nullable 객체로 인한 불편들이 한줄로 모두 해결되었음을 확인 할 수 있다. 그렇다면 이 `viewBinding()`이라는 delegate function은 어떻게 구현 해야 한다. 

`viewBinding()`델리게이트 함수는 코틀린의 `ReadOnlyProperty`를 구현해야 한다. 아래의 예제를 보자. 

```kotlin
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class FragmentViewBindingDelegate<T: ViewBinding>(
    val fragment: Fragment,
    val viewBinder: (View) -> T
): ReadOnlyProperty<Fragment, T> {
    private var binding: T? = null

    init {
        fragment.lifecycle.addObserver(object: DefaultLifecycleObserver {
            val viewLifecycleOwnerLiveData =
                Observer<LifecycleOwner?> {
                    val viewLifecycleOwner = it ?: return@Observer
                    viewLifecycleOwner.lifecycle.addObserver(object: DefaultLifecycleObserver {
                        override fun onDestroy(owner: LifecycleOwner) {
                            binding = null
                        }
                    })
                }

            override fun onCreate(owner: LifecycleOwner) {
                fragment.viewLifecycleOwnerLiveData.observeForever(viewLifecycleOwnerLiveData)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                fragment.viewLifecycleOwnerLiveData.removeObserver(viewLifecycleOwnerLiveData)
            }
        })
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        val binding = binding
        if (binding != null) return binding

        val lifecycle = fragment.viewLifecycleOwner.lifecycle
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            // Fragment가 이미 destory상태인 경우,
            throw IllegalStateException("Should not attempt to get bindings when Fragment views are destroyed.")
        }
        return viewBinder(thisRef.requireView()).also { this.binding = it }
    }
}

// 확장 함수
fun <T : ViewBinding> Fragment.viewBinding(viewBinder: (View) -> T) =
    FragmentViewBindingDelegate(this, viewBinder)
```

위 확장함수를 이용하면 보일러 플레이트 없이 편하게 ViewBinding객체를 위임 받아 not nullable 인스턴스로 활용 할 수 있다. 

참고로 `DefaultLifecycleObserver`을 사용하려면 해당 모듈의 `build.gradle`에 `implementation "androidx.lifecycle:lifecycle-common-java8:$<version_number>"`와 같은 의존을 추가 해야 한다. 이 라이브러리의 최신 버전은 (여기)[https://mvnrepository.com/artifact/androidx.lifecycle/lifecycle-common-java8] 에서 확인 할 수 있다.
