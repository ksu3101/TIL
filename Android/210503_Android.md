# Flow/LiveData….What Are They? Best Use Case. (Lets Build a Login System)

> 이 글은 Inuwa Ibrahim의 [Flow/LiveData….What Are They? Best Use Case. (Lets Build a Login System)](https://proandroiddev.com/flow-livedata-what-are-they-best-use-case-lets-build-a-login-system-39315510666d)을 번역 하였다. 

현대의 안드로이드 개발자로서, `LiveData`와 `Flow`라는 단어들을 여러번 접해본적 있을 것 이다. 이 것들에 대한 글들은 많지만 대부분은 초보자들을 돕기 위한 사례들과 간단한 예제들에 대해서 알려주지는 않는다. 

이 글에서는 간단한 예제를 통해서 `LiveData`와 `Flow`에 대해서 알아볼 것 이다. 

# LiveData

LiveData는 기본적으로 견고하고 테스트할 수 있으며, 유지 관리에 좋은 앱을 설계 하는데 도움되는 라이브러리 모음인 안드로이드 아키텍쳐의 컴포넌트 중 하나 이다. 

이러한 라이브러리에서는 앱에서 사용할 수 있게 클래스를 제공 하고 있다. 이 클래스 중 하나는 `LiveData`이다. 

LiveData는 `Observable`데이터 클래스(다른 컴포넌트에서 관찰 할 수 있도록 하는 클래스)이다. 이는 UI(Activity, Fragment) 에 대한 컨트롤러이다. 따라서 ViewModel(메모리 누출로 인해 없어야 하는)에 Acitivty/Fragment에 대한 참조를 갖는 대신 이제 Activity/Fragment에서 ViewModel에 대한 참조를 갖고 있다. 

이는 수명주기를 인식하고 있다. 즉 View가 활성 상태일때에 UI(Activity/Fragment)에 업데이트를 하라고 알린다. (이 경우 메모리 누수 없음)

LiveData를 사용하면 다음과 같은 이점이 있다. 

- 메모리 누수가 없다
- UI가 데이터 상태와 일치하게 해준다
- Activity가 중지되었을 때 앱의 크래시를 방지 한다
- 데이터는 항상 최신 상태를 유지한다
- 구성 변경(configuration changes)에 유용하다 
- 자원들을 공유 한다

## Live Data Use Case: Let’s Build a simple login system.

MVVM을 사용하여 API와 통신하는 간단한 로그인 시스템을 만들어 보도록 하자. 실제 앱 에서 LiveData가 어떻게 작동하는지 살펴보도록 하자. 

사용자가 자신의 이메일과 비밀번호를 입력 하고 Login버튼을 클릭 하면 아래와 같이 작동 한다. 

- 클릭 이벤트는 ViewModel에 알린다
- Activity의 데이터를 관찰(Observe)한다
- 받은 로그인 응답의 상태에 따라 적절한 메시지를 화면에 보여 준다 

이를 위해 아래를 사용 한다.

- 코루틴, Retrofit

# Steps

## 1. Set up Retrofit for network calls

- API호출에 필요한 설정들에 대한 [전체 코드](https://github.com/ibrajix/FlowLiveData)를 확인 한다. 또는 원글 게시자가 작성한 [이 글](https://medium.com/swlh/how-i-built-a-simple-currency-converter-app-using-recommended-android-pattern-and-architecture-204a3bbfc142)을 확인 해보도록 하자. 

## 2. Set up Repository 

- 새로운 클래스인 `LoginRepoLiveData`를 만든다. 

```kotlin
class LoginRepo(private val apiHelper: ApiHelper) : BaseDataSource() {
   suspend fun login(userModel: UserModel) =  safeApiCall { apiHelper.login(userModel) }
}
```

참고 - Repo는 네트워크의 상태를 파악하고 적절하게 처리 할 수 있도록 도움이 되는 `BaseDataSource`클래스를 구현/확장 해야 한다. 

## 3. Set up our ViewModel

- 새로운 클래스인 `LoginViewModelLiveData`를 만든다. 

```kotlin
class LoginViewModel(private val loginRepo: LoginRepo) : ViewModel() {

    //cached
    private val _login = MutableLiveData<Resource<LoginResponse>>()

    //public
    val login : LiveData<Resource<LoginResponse>> get() =  _login
    
    //do login
    fun doLogin(userModel: UserModel) =
        viewModelScope.launch {
            try {
                _login.value = loginRepo.login(userModel)
            }
            catch (exception: Exception){

            }
        }
}
```

- LiveData가 ViewModel에 있는 이유 중 하나는 구성 변경(Configuration changes)를 유지하기 위한 것 이므로, 기기가 회전 해도 상태가 손실되지 않고 UI가 다시 생성되지 않게 해 준다. 
- LiveData에는 데이터를 수정할 수 있는 공개된 메소드가 존재하지 않는다. 그래서 ViewModel클래스와 같이 외부에서 LiveData를 수정할 수 있도록 `MutableLiveData`를 사용 한다. 
- 코루틴을 사용하여 네트워크 저장소에 요청을 하여 값들을 가져오는 함수가 존재 한다. 

## 4. Set up View

`LiveDataActivity`에서는 아래와 같은 코드를 갖는다. 

```kotlin
class LiveDataActivity : AppCompatActivity() {
    private lateinit var viewModel: LoginViewModel
    private lateinit var binding: ActivityLiveDataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiveDataBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initView()
    }

    private fun initView(){
        setupViewModel()
        handleClick()
    }

    private fun handleClick(){

        binding.btnLogin.setOnClickListener {            
            binding.btnLogin.visibility = View.GONE
            binding.loading.visibility = View.VISIBLE
            setupObservers()
        }
    }

    private fun setupViewModel(){

        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactoryLiveData(ApiHelper(ApiClient.apiService))
        ).get(LoginViewModel::class.java)

    }

    private fun setupObservers(){

        //get email and password
        val email = binding.editText.text.toString()
        val password = binding.editText2.text.toString()
        val userModel = UserModel(email = email, password = password)
        viewModel.doLogin(userModel)
        viewModel.login.observe(this, Observer {
            when(it.status){
                Resource.Status.SUCCESS -> {
                    if(it.data?.status == "fail"){
                        binding.loading.visibility = View.GONE
                        binding.btnLogin.visibility = View.VISIBLE
                        Utility.displaySnackBar(binding.root, it.data.message, this)
                    }
                    else if (it.data?.status == "success"){
                        Toast.makeText(this, "Login was successful", Toast.LENGTH_LONG).show()
                        binding.loading.visibility = View.GONE
                        binding.btnLogin.visibility = View.VISIBLE
                    }
                }

                Resource.Status.ERROR -> {
                    binding.loading.visibility = View.GONE
                    binding.btnLogin.visibility = View.VISIBLE
                    Utility.displaySnackBar(
                        binding.root,
                        it.message?:"",
                        applicationContext
                    )
                }
                Resource.Status.LOADING -> {
                    binding.loading.visibility = View.VISIBLE
                    binding.btnLogin.visibility = View.GONE
                }
                Resource.Status.FAILURE -> {
                    binding.loading.visibility = View.GONE
                    binding.btnLogin.visibility = View.VISIBLE
                    Utility.displaySnackBar(
                        binding.root,
                        it.message?:"",
                        applicationContext
                    )
                }
            }
        })
    }
}
```

- 참고로 의존성 주입(Dependency Injection)-Hilt를 사용하지 않았다. 그렇기 때문에 `ViewModelFactory`에 대한 확인이 필요하다.
- 위의 Activity 코드에서는 ViewModel을 설정하고 ViewModel의 변화를 관찰하고 변경에 대한 View를 갱신하게 해 준다. 

실행 결과는 아래와 같다. 

- 잘못된 자격 증명(이메일, 비밀번호)을 입력하였다면 Snackbar를 통해 "잘못된 이메일 또는 비밀번호"라는 오류 메시지가 표시된다.
- 문제없이 로그인을 성공 했다면 "로그인 성공"과 같은 토스트 메시지를 보여준다. 

## LiveData의 단점

- 실행 컨텍스트들에 대한 제어가 부족하다
- 저장소에서 사용될 때 스레드 문제가 발생 한다
- 코루틴 및 Kotlin을 사용하여 만들어진 라이브러리가 아니다
- Room과 같은 데이터베이스, UI간의 원활한 데이터 통합이 부족 하다
- 변환을 사용하는 동안 많은 보일러 플레이트 코드들이 생성된다 

# Flows 

코루틴에서 Flow는 단일값만 반환하는 함수를 suspend하는 것과 달리 여러 값들을 순차적으로 내보낼수 있도록 해 준다. 예를 들면, Flow를 이용하여 데이터베이스에서 순차적으로 실시간 업데이트를 받을 수 있다. 

Flow는 값의 스트림을 처리 하고 복잡한 다중 스레드 방식으로 데이터들을 변환할 수 있다. 

## StateFlow와 SharedFlow

`StateFlow`와 `SharedFlow`는 Flow의 상태 업데이트를 최적으로 실행하고 여러 소비자들에게 값을 내보낼 수 있도록 하는 Flow API이다. 

이에 대해 [이 글](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow)을 확인 하도록 하자. 

StateFlow와 LiveData는 비슷한 특성을 가지고 있으므로 이 글에서는 StateFlow를 사용하려 한다. 

# StateFlow

StateFlow는 업데이트 된 값만 얻는다. 이 StateFlow는 RxJava의 Observer와 개념적으로 매우 유사 하다. 

StateFlow에서 값을 수집할 때 항상 StateFlow에 값이 존재 하므로 읽기에 안전하며 항상 값이 설정되어 있기 때문에 최신값을 얻을 수 있다. 그 이유는, StateFlow는 무조건 초기화된 초기값이 필요하기 때문이다. 

## State Flow Use Case: Refactoring our existing login application

- `FlowActivity`라는 새로운 액티비티를 생성한다
- 기존의 `activity_live_data.xml` 을 `activity_flow.xml`로 복사 한다
- 그리고 `LoginRepoFlow`라는 새로운 클래스를 생성 한다. 이 클래스는 기본적으로 Flow를 사용하고 코루틴을 이용해 네트워크 API를 호출 할 것 이다. 

```kotlin
class LoginRepoFlow(private val apiHelper: ApiHelper) : BaseDataSource() {

    //login user with flow
    suspend fun loginUserFlow(userModel: UserModel) : Flow<Resource<LoginResponse>> {

        return flow {
            val result = safeApiCall { apiHelper.login(userModel) }
            emit(result)

        }.flowOn(Dispatchers.IO)
    }
}
```

- Flow를 사용하는 ViewModel인 `LoginViewModelFlow`를 만든다

```kotlin
class LoginViewModelFlow(private val loginRepoFlow: LoginRepoFlow) : ViewModel() {

        private val _loginUserFlow = MutableStateFlow<Resource<LoginResponse>>(Resource.loading(null))
        val loginUserFlow : StateFlow<Resource<LoginResponse>> =  _loginUserFlow

        fun doLoginUserFlow(userModel: UserModel){
            viewModelScope.launch {
                loginRepoFlow.loginUserFlow(userModel)
                        .catch { e ->
                            _loginUserFlow.value = Resource.error(e.toString())
                        }
                        .collect {
                            _loginUserFlow.value = it
                        }
               }
          }
}
```

- `MutableLiveData`대신 `MutableStateFlow`를 사용하고 `LiveData`대신 `StateFlow`를 사용하는 방법에 주목 하도록 하자. API는 비슷하지만 `StateFlow`에는 null데이터의 초기값을 허용하지 않으므로 초기값을 항상 설정 해야 한다. 
 
`FlowActivity`에서는 Flow를 관찰하여 View를 업데이트 시켜 준다. 

```kotlin
class FlowActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModelFlow
    private lateinit var binding: ActivityFlowBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flow)
        binding = ActivityFlowBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initView()
    }

    private fun initView(){
        setupViewModel()
        handleClick()
    }

    private fun setupViewModel(){
       viewModel = ViewModelProviders.of(
                this,
                LoginViewModelFactoryFlow(ApiHelper(ApiClient.apiService))
        ).get(LoginViewModelFlow::class.java)
    }

    private fun handleClick(){
        //on click login button
        binding.btnLogin.setOnClickListener {
            binding.btnLogin.visibility = View.GONE
            binding.loading.visibility = View.VISIBLE
            setupObservers()
        }

        //on click use flow text
        binding.txtUseLiveDate.setOnClickListener {
            val intent = Intent(this, FlowActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupObservers(){

        //get email and password
        val email = binding.editText.text.toString()
        val password = binding.editText2.text.toString()

        val userModel = UserModel(email = email, password = password)
        viewModel.doLoginUserFlow(userModel)
        lifecycleScope.launchWhenStarted {

            // Triggers the flow and starts listening for values
            viewModel.loginUserFlow.collect {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        if (it.data?.status == "fail") {
                            binding.loading.visibility = View.GONE
                            binding.btnLogin.visibility = View.VISIBLE
                            Utility.displaySnackBar(binding.root, it.data.message, this@FlowActivity)

                        } else if (it.data?.status == "success") {
                            Toast.makeText(this@FlowActivity, "Login was successful", Toast.LENGTH_LONG).show()
                            binding.loading.visibility = View.GONE
                            binding.btnLogin.visibility = View.VISIBLE
                        }
                    }

                    Resource.Status.ERROR -> {
                        binding.loading.visibility = View.GONE
                        binding.btnLogin.visibility = View.VISIBLE
                        Utility.displaySnackBar(
                                binding.root,
                                it.message ?: "",
                                applicationContext
                        )
                    }
                    Resource.Status.LOADING -> {
                        binding.loading.visibility = View.VISIBLE
                        binding.btnLogin.visibility = View.GONE
                    }
                    Resource.Status.FAILURE -> {
                        binding.loading.visibility = View.GONE
                        binding.btnLogin.visibility = View.VISIBLE
                        Utility.displaySnackBar(
                                binding.root,
                                it.message ?: "",
                                applicationContext
                        )
                    }
                }
            }
        }
    }
}
```

- 이제 앱을 실행하여 테스트 해 보자. 잘 작동할 것 이다. 이에 대해 차이점은 딱히 없을 것 이다. 

## Flow의 단점 

- 스낵바(SnackBar)를 사용한 작업에 대한 지원이 부족 : `Channels`를 사용 한다
- `LiveData.observe()`는 View가 `STOPPED`상태가 될 때 자동으로 등록된 소비자들을 취소 하지만, `StateFlow`는 그렇지 않다. 
  - 수동으로 구독을 중지 한다. (이 경우 반복 코드가 발생 한다)
  - `asLiveData()`를 이용하여 Flow를 LiveData로 변환하여 사용 한다. (이는 LiveData를 쓰는 것 과 다를게 있을까?)
  - 안전한 방법으로 Flow를 구독 중지 하기 위해 최신 API를 사용 한다. 

단점에 대한 수정으로 아래를 참고 해 보자. 

- `LoginViewModelFlow` : `Channel`로 변경 한다. 
    ```kotlin
    class LoginViewModelFlow(private val loginRepoFlow: LoginRepoFlow) : ViewModel() {
        private val _loginUserFlow = Channel<Resource<LoginResponse>>(Channel.BUFFERED)
        val loginUserFlow = _loginUserFlow.receiveAsFlow()

        fun doLoginUserFlow(userModel: UserModel){
            viewModelScope.launch {
                loginRepoFlow.loginUserFlow(userModel)
                    .catch { e ->
                        _loginUserFlow.send(Resource.error(e.toString()))
                    }
                    .collect {
                        _loginUserFlow.send(it)
                    }
            }
        }
    }
    ```

- `FlowActivity` : `lifecycle-runtime-ktx`라이브러리에서 `lifecycleOwner.addRepeatingjob()`확장 함수를 이용하여 안전한 방법으로 ViewModel의 Flow를 구독 중지 시킨다. 
  ```kotlin
  viewModel.doLoginUserFlow(userModel)
        this.addRepeatingJob(Lifecycle.State.STARTED){
            viewModel.loginUserFlow.collect {
                when (it.status) {
                    Resource.Status.SUCCESS -> {

                        if (it.data?.status == "fail") {
                            binding.loading.visibility = View.GONE
                            binding.btnLogin.visibility = View.VISIBLE
                            Utility.displaySnackBar(binding.root, it.data.message, this@FlowActivity)

                        } else if (it.data?.status == "success") {
                            Toast.makeText(this@FlowActivity, "Login was successful", Toast.LENGTH_LONG).show()
                            binding.loading.visibility = View.GONE
                            binding.btnLogin.visibility = View.VISIBLE
                        }
                    }

                    Resource.Status.ERROR -> {
                        binding.loading.visibility = View.GONE
                        binding.btnLogin.visibility = View.VISIBLE
                        Utility.displaySnackBar(
                            binding.root,
                            it.message ?: "",
                            applicationContext
                        )
                    }
                    Resource.Status.LOADING -> {
                        binding.loading.visibility = View.VISIBLE
                        binding.btnLogin.visibility = View.GONE
                    }
                    Resource.Status.FAILURE -> {
                        binding.loading.visibility = View.GONE
                        binding.btnLogin.visibility = View.VISIBLE
                        Utility.displaySnackBar(
                            binding.root,
                            it.message ?: "",
                            applicationContext
                        )
                   }
                }
            }
        }
  ```

  결론적으로 말하자면, Flow(StateFlow)와 LiveData에서는 큰 차이가 없어 보이지만 구글에서는 (코틀린을 중심으로 한)Flow의 사용을 추천하고 있다. 

  이는 결국 LiveData가 더이상 오래 사용되지 않을 것 임을 말해준다. 