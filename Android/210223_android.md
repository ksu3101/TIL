## Activity Result Contract – The Basics

> 이 글은 Mark Allison의 [Activity Result Contract – The Basics](https://blog.stylingandroid.com/activity-result-contract-basics/)을 번역 하였다. 

Styling Android의 구독자라면 알겠지만, 나(원본 글 작성자)는 일반적으로 각 게시글과 함께 샘플 코드를 같이 게시한다. 샘플 코드에서 권한이 필요할 때 마다 보일러플레이트 코드가 추가되어야 함이 맘에 들지 않았다. 이는 나에게 추가 작업을 의미할 뿐만 아니라 샘플 코드를 더 어렵게 만들 수 있다. 그러나 이러한 문제들을 단순화 시켜주기 위핸 새로운 기능이 AndroidX Activity에 추가 되었다. 그것은 `ActivityResultContracts`이다. 

### Background 

런타임 중 권한 요청은 2015년에 출시 된 API 23(마시멜로우) 이후 추가된 요구 사항이다. 사용자에게 권한을 요청 하려면 요청을 수행하기 위한 제어권을 OS에 전달해야 한다. 일반적으로 `startActivityForResult()`를 사용 하므로 (직접적이지 않은 경우 간접적으로)제어가 반환 될 때 특정 메소드가 호출 된다. 

[Jetpack Activity Library 1.2.0-alpha02](https://developer.android.com/jetpack/androidx/releases/activity)에 도입 된 새로운 패턴으로는 `startActivityForResult()`대신 `onActivityResult()`를 호출 하게 된다. 이전에 `startActivityForResult()`를 사용 했던 모든 곳 에 새로운 패턴을 적용할 수 있다. 이는 런타임 권한으로 제한되지 않으며, 예를들어 사진을 찍거나 문서를 여는데 사용할 수 있다. 

### Contracts

새로운 API는 각 사용 사례에 따라 다른 계약(contracts)를 기반으로 한다. 예를 들어 런타임 권한을 요청 할 때 권한이 부여되었는지 여부를 알아야 할 것 이다. 그러나 사진 촬영을 요청 하면 촬영한 사진에 대한 결과는 Uri가 되어야 한다. 예를 들면 다음과 같다. 

```kotlin
private val requestPermissions = 
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            navController.navigate(R.id.grantedPermissionFragment)
        }
    }
```

여기에서 contract는 `ActivityResultsContracts.RequestPermission()`이다. 입력 및 출력 유형을 나타내는 두가지 유형 매개 변수가 있는 일반 클래스인 `ActivityResultContract`를 상속 받고 있다. 권한 요청의 경우 입력은 권한을 나타내는 문자열이고, 권한이 부여되었는지 여부를 나타내는 boolean이다. 

내부적으로 이 contract는 필요한 권한을 요청하는 논리를 구현 한다. 

`registerForActivityResult()`함수는 두개의 인수를 사용 한다. 첫번째는 contract이고, 두번째는 완료 시 호출 될 람다가 된다. contract의 출력 타입은 람다의 인수 유형을 나타낸다. 이 예제에서는 권한이 부여되었는지 여부를 나타내는 `boolean`이다. 

`registerForActivityResult()`함수는 이 작업을 수행하려고 할 때 호출 할 수 있는 `ActivityResultLauncher`를 반환 한다. 

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    val permission = Manifext.permission.READ_PHONE_STATE

    when {
        ContextCompat.checkSelfPermission(this, permission) == PERMISSION_GRANTED ->
            navController.navigate(R.id.grantedPermissionsFragment)
        shouldShowRequestPermissionRationale(permission) -> showRationale()
        else -> requestPermissions.launch(permission)
    }
}

private fun showRationale() {
    MaterialAlertDialogBuilder(this)
        .setTitle(R.string.dialog_title)
        .setMessage(R.string.dialog_message)
        .setPositiveButton(R.string.button_ok) { _, _ ->
            requestPermissions.launch(permission)
        }
        .setNegativeButton(R.string.button_cancel) { _, _ -> }
        .show()
}
```

여기에서 먼저 필요한 권한이 있는지 먼저 화인 한다. 그렇다면 적절한 목적지로 이동하게 될 것 이다. 필요한 권한이 없는 경우 요청 권한 근거를 표시 해야 하는지 출력 한다. 그렇지 않다면 이전에 생성 한 `ActivityResultLauncher` 인스턴스를 시작 한다. contract의 입력 타입은 시작 방법에 대한 인수를 지정 한다. 이 경우 필요한 권한을 나타내는 문자열이 된다. 

단일 작업으로 여러 권한을 요청하는 데 사용할 수 있는 별도의 contract가 있다. 

### Benefits

이 접근 방식을 사용하면 코드를 훨씬 쉽게 이해할 수 있다. 따라서 처음 보는 사람이 로직을 이해하는데 어려움이 적기 때문에 유지 관리가 더 쉽다. 

`startActivityForResult()` / `onActivityResult()`의 이전 패턴에서는 이 매커니즘의 작동 방식을 이해하지 못했었다. 그러나 `requestPermission.launch(...)`를 보면 완료시 호출되는 람다로 직접 연결 되어야 한다. 

또한 이것이 실행되는 동안 제어가 다른곳으로 전달됨을 알 필요가 없다. 이는 불투명한 상자를 통한 작업이며 우리는 세부 사항을 알 필요가 없다. 우리가 알아야 할 것은 contract을 시작 하고 완료 되었을 때 람다가 결과와 함께 호출 된다는 것 이다. 

다른 ready-made contract도 많이 있다. 목록을 보고 싶다면 `ActivityResultContract`의 알려진 하위 클래스를 살펴 보면 된다. 또한 사용자 로직을 만들기 위해 `ActivityResultContract`의 자식 클래스로 만든느 것은 생각보다 간단할 것 이다. 

### Conclusion

`ActivityResultContracts`는 정말 유용한 추가 기능이다. 이미 언급하였듯이 코드의 이해도를 크게 향상 시킨다고 생각 한다. 즉, 이것은 하나의 특정 사례이며 다음 글 에서 더 자세히 살펴 보도록 하자. 