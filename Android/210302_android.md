## Activity Result Contract – Outside the Activity

> 이 글은 Mark Allison의 [Activity Result Contract – Outside the Activity](https://blog.stylingandroid.com/activity-result-contract-outside-the-activity/?utm_source=feedburner&utm_medium=feed&utm_campaign=Feed%3A+StylingAndroid+%28Styling+Android%29)을 번역 하였다. 

[이전 글](https://github.com/ksu3101/TIL/blob/master/Android/210223_android.md)에서 우리는 새로운 Activity의 Result Contract패턴에 대해서 살펴 보았다. Result Contract는 코드를 좀 더 쉽게 이해할 수 있게 해 주었고 보일러플레이트 코드를 제거 해 주었다. Result Contract는 예를 들어 Activity에서 권한을 요청 할 때 좋은 예가 될 것 이다. 그러나 종종 우리는 Activity에 너무 많은 로직을 추가 하는 것을 피하고 싶다. 이 게시물에서는 이를 위한 다양한 기술들에 대해 알아볼 것 이다. 

### Background 

이 글 에서는 런타임 권한에 중점을 둘 것 이지만 `startActivityForResult()`를 사용 할 수 있다면 문제 없이 사용 할 수 있다. 

런타임 권한은 두가지 범주로 나뉘어 진다. 앱 운영에서의 핵심과 부차적인 것 이다. 예를 들어 카메라 앱 에는 `CAMERA`권한이 필요 할 것 이다. 이 권한 없이는 카메라의 기본 기능들을 사용 할 수 없다. 그리고 이 앱에서는 촬영 한 사진과 함께 위치 정보를 저장하는 선택적 기능이 있을 수 있다. 위치에 대한 권한이 부여되지 않은 경우 당연히 위치 정보를 사진에 포함 할 수 없다. 그러나 사진 촬영 자체를 방해하지는 않는다. 따라서, 이 앱의 경우 `CAMERA`권한은 필수적인 요구 사항 이지만 위치에 대한 권한은 앱의 주요 기능에 대해 부차적이라고 할 수 있다. 

사용자가 앱을 본격적으로 사용 하기 전에 필요한 권한을 미리 요청 해야 한다. 그러나 우리는 앱 내 에서 보조적인 권한을 더 많이 포함할 수 있다. 그런 다음 사용자가 인식할 수 있는 부분에서 권한을 요청할 수 있다. 이 예제에서는 사진을 찍고난 뒤 화면에 메모를 추가하는 형태가 될 수 있다. 이 메모에는 사진에 위치 정보를 추가할 수 있음을 사용자에게 알릴 수 있다. 그런 다음 위치 권한을 요청 하는 작업을 진행 할 것 이다. 

### Fragments

런타임 권한 요청 로직을 Fragment에서 진행 하려면 항상 코드 복잡성이 발생 하였다. 부모 Activity의 메소드를 호출 하는 Fragment의 코드가 존재 하거나 이를 처리 할 인터페이스등을 구현 해야 하는 것 등이다. 

AndroidX Fragment 1.3.0-alpha02에서 부터는 Fragment에서 직접 `ActivityResult`를 사용할 수 있도록 지원 해준다. 

```kotlin
class GrantedPermissionsFragment: Fragment(R.layout.fragment_granted_permissions) {
    private val getPermission = registerForActivityResult(RequestPermission()) { granted ->
        if (granted) {
            binding.secondaryPermission.setText(R.string.permission_granted)
        } else {
            binding.secondaryPermission.setText(R.string.permission_denied)
        }
    }

    lateinit var binding: FragmentGrantedPermissionBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstaceState)

        binding = FragmentGrantedPermissionsBinding.bind(view)

        binding.requestSecondary.setOnClickListener {
            getPermission.launch(Masnifest.Permission.READ_CONTACTS)
        }
    }
}
``` 

내부적으로 이것은 부모 Activity를 통해 동작을 마샬링 하여 처리 하지만 확실히 Fragment는 코드가 깔끔해졌음을 알 수 있다. 

### External Logic

아직 가야할 길이 남아 있다. 이번에는 다른 클래스의 권한을 요청하려는 경우가 있을 수 있다. 다시 말하지만, Activity의 Result Contract는 이를 깔끔하고 쉽게 만들어 준다. 

```kotlin
class MyLogic(registry: ActivityResultRegistry) {
    companion object {
        private const val REGISTRY_KEY = "Read Calendar perm"
    }

    private val enabled = MutableLiveData(false)

    private val getPermission = registry.register(REGISTRY_KEY, RequestPermission()) { granted ->
        enabled.value = granted
    }

    fun doSomething(): LiveData<Boolean> {
        getPermission.launch(Manifest.Permission.READ_CALENDAR)
        return enabled
    }
}
```

이 방법은 권한을 요청 하는 것 외에는 아무것도 하지 않기 떄문에 간단한 예제이다. 그러나 로직을 쉽게 분리할 수 있는 방법에 대해서 정확하게 보여주고 있다. 이 경우 `doSomething()`이 호출 되면 필요한 권한이 요청 될 것 이고, `LiveData<Boolean>`의 값이 변경 되어 요청된 권한 상태를 받아서 처리 하면 된다. 

이전 예제와 다른 점은 현재 컨텍스트에서 사용할 수 없기 떄문에 `registerForActivityResult()`를 호출할 수 없다는 점 이다. 그러나 생성자는 `ActivityResultRegistry`인스턴스를 사용 한다. 이 `ActivityResultRegistry`인스턴스는 우리에게 필요한 모든것을 제공 한다. `ActivityResultRegistry`인스턴스에서 `register()`를 호출 하는 것은 `registerForActivityResult()`를 호출 하는 것과 동일 하다. 유일한 차이점은 등록중인 Contract를 식별하는 고유 키를 제공 해 주어야 한다는 것 이다. 이것은 Contract를 소비자(consumer)와 연결 시켜 준다. 나머지는 이전과 거의 동일하게 작동 된다. 

`Fragment`에 대한 `registerForActivityResult()`의 구현은 비록 숨겨져 있지만 실제로는 내부적으로 `ActivityResultRegistry`을 사용한다는 점을 언급할 만한 가치가 있다. 

Fragment에서의 사용 방법은 아래와 같다. 

```kotlin
class GrantedPermissionsFragment: Fragment(R.layout.fragment_granted_permissions) {
    lateinit var myLogic: MyLogic
    lateinit var binding: FragmentGrantedPermissionBinding

    private val getPermission = registerForActivityResult(RequestPermission()) { granted ->
        if (granted) {
            binding.secondaryPermission.setText(R.string.permission_granted)
        } else {
            binding.secondaryPermission.setText(R.string.permission_denied)
        }
    }  

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstaceState)

        binding = FragmentGrantedPermissionsBinding.bind(view)

        binding.requestSecondary.setOnClickListener {
            getPermission.launch(Masnifest.Permission.READ_CONTACTS)
        }

        myLogic = MyLogic(requireActivity().activityResultRegistry)

        binding.requestExternal.setOnClickListener {
            myLogic.doSomething().observe(viewLifecycleOwner) { granted ->
                if (granted) {
                    binding.externalPermission.setText(R.string.permission_granted)
                } else {
                    binding.externalPermission.setText(R.string.permission_denied)
                }
            }
        }
    }
}
```

매우 간단하지만 `ActivityResultRegistry`를 가져 올 때 `Activity`인스턴스가 필요 하다. 

### Hilt 

Hilt또는 Dagger와 같은 의존성 주입을 사용 하는 경우 `Fragment`에서 이 부모 Activity의 참조를 쉽게 제거 할 수 있다. 먼저 현재 Activity에서 `ActivityResultRegistry`를 제공 하는 모듈을 만들어야 한다. 

```kotlin
@Module
@InstallIn(ActivityComponent::class)
class ActivityModule {
    @Provides
    fun provideActivityResultRegistry(@ActivityContext activity: Context): ActivityResultRegistry {
        (activity as? AppCompatActivity)?.activityResultRegistry
            ?: throw IllegalArgumentException("you must use AppCompatActivity")
    }
}
```

그리고 `MyLogic`클래스에 `@Inject`를 사용하여 생성자를 통해 `ActivityResultRegistry`인스턴스를 주입 한다.

```kotlin
class MyLogic @Inject constructor(retisgry: ActivityResultRegistry) {
    // 이전 코드와 동일
}
```

이제 `MyLogic`을 이용하여 `Fragment`에서도 주입 받아 사용할 수 있다. 

```kotlin
@AndroidEntryPoint
class GrantedPermissionsFragment: Fragment(R.layout.fragment_granted_permissions) {
    @Inject
    lateinit var myLogic: MyLogic

    // 이전 코드와 동일 
}
```

이제 더 이상 Fragment의 코드 에서 Activity를 참조할 필요가 없어졌다. 

### Conclusion

이 것들은 Activity및 Fragment내 에서 거의 완벽하게 잘 설계된 API이다. 그리고 외부 컨텍스트 에서도 사용 하기 매우 쉬우며 hilt와 같이 사용 하면 더욱 더 코드가 깔끔해지고 간결해짐을 확인 할 수 있다. 