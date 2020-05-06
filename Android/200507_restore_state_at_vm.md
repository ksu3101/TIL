## ViewModel 의 데이터 상태 관리

ViewModel 에서는 ViewModel 을 가져올때 `ViewModelProvider`를 통해 `LifeCycle` 을 지정 하여 Activity 나 Fragment 의 생명주기와 동기화된 활동을 보여준다. 아래 그 생명주기를 참조하면 알 수 있다. 

![](https://github.com/ksu3101/TIL/blob/master/imgs/viewmodel-lifecycle.png)

Activity 혹은 Fragment 에서 ViewModel 의 인스턴스를 생성 하고 사용 할 때 ViewModel 내부의 `LiveData` 등은 Finished 상태가 되기 전 까지는 유효한 것 이다. 

하지만 앱 화면의 회전과 같은 destroy - re create 의 과정을 밟게 된다면 어떻게 될 것인가? 일반적으로 이 경우에는 `onSaveInstanceState()` 등의 콜백을 구현 해서 `Bundle` 객체에 키-값 형태로 다시 화면에 그려져야 할 데이터들을 임시로 저장했다가 onCreate 등 콜백 시점에 다시 세팅하여 화면을 업데이트 하는 방식을 사용 했었다.

하지만 이 방법은 기존 아키텍쳐 에서는 유효했지만 MVVM 등과 같은 아키텍쳐에서는 유효하지 않다. 몰론 Activity 나 Fragment 등 에서 해당 콜백을 ViewModel 의 인스턴스에 직접 알리는 방식이 있겠지만 안드로이드 에서 제공 하는 방법을 사용 해 보도록 하자. 

### 설정 및 사용 방법

`ViewModelProvider` 에서 ViewModel 을 가져올 때 `AbstractSavedStateVMFactory` 를 상속, 구현한 인스턴스를 설정 해 주면 해당 팩토리에 의해 ViewModel 에서 `SavedStateHandle` 을 사용 할 수 있다. 

```kotlin
class SavedStateViewModel(
  private val state: SavedStatehandle
): ViewModel() {
  
  fun getText(): LiveData<String> {
    return state.getLiveData("KEY_TEXT")
  }
  
  fun saveText(text: String) {
    state["KEY_TEXT"] = text
  }
}

class SomeFragment: Fragment() {
  private lateinit var vm: SavedStateViewModel
  
  override fun onCreate() {
    // ...
    vm = ViewModelProvider(this, SavedStateVMFactory(this)).get(SavedStateViewModel::class.java)    
  }
}

```

### SavedStateHandle

`SavedStateHandle` 에는 다음과 같은 함수들을 제공한다. 

- `get(key: String)`
- `contains(key: String)`
- `remove(key: String)`
- `set(key: String, value: T)`
- `keys()`

위 함수들 외 에도 `LiveData` 로 래핑된 `getLiveDate(key: String)` 도 사용 할 수 있다. 함수들을 보면 알겠지만 외부에서 이 클래스를 사용 할 때 Map 처럼 사용됨을 알 수 있다. 

#### 허용되는 클래스들 

`SavedStateHandle` 에서 저장되는 데이터들은 `Bundle` 로 저장되므로 기존 `Bundle` 사용법과 동일 하다. 비트맵 처럼 대용량보다는 작은 데이터들을 임시적으로 사용한다고 생각 하는게 좋을 것 같다. 

### 기타

[구글 예제](https://codelabs.developers.google.com/codelabs/android-lifecycles/#6)를 보면 실제 테스트 예를 볼 수 있는데 `adb kill` 명령어를 이용하여 프로세스를 죽이고 다시 앱을 열어 RUNNING 시점 으로 다시 시작해보면 저장한 데이터들이 잘 복구됨을 확인 할 수 있다. 

