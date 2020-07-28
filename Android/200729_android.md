## Rx로 처리하는 TextWatch 콜백 

`TextWatcher`는 안드로이드에서 `EditText`에서 입력되는 텍스트의 변화할 때 마다 콜백되는 API를 제공하는 인터페이스이다. `TextWatcher`의 경우 EditText인스턴스를 참조 하여 `addTextChangedListener()`함수를 이용해 `TextWatcher`인터페이스를 생성하여 등록 하고, 종료시점에 `removeTextChangedListener()`함수를 이용해 등록한 TextWatcher인스턴스를 제거 해야 한다. 

이번 정리내용에서는 Rx와 TextWatcher를 이용하여 텍스트입력에 대해 처리하는 방법을 정리 하려고 한다. 

### 1. 목적 

요구사항은 이렇다. 

1. EditText에 Rx의 cold 스트림을 생성 하여 구독 시 텍스트의 변화에 따라 `Observable` 소스로 변환하여 내린다. 
2. EditText의 텍스트 변화를 Rx를 이용해 버퍼처럼 처리한다. 
3. 버퍼에서는 일정 시간동안 TextWatcher콜백을 받으며 마지막으로 받았던 문자열을 배출한다. 
  - 검색하는 입력란에서 입력할때 모든 입력글자에 대응하는게 아닌 마지막으로 입력한 내용에 대응 하게 한다고 생각하면 될거 같다. 

EditText를 상속받는 커스텀 뷰를 만들고 내부에 `PublishSubject`와 같은 cold Observable을 배출하는 스트림 객체를 둔다. 

기존에 사용되던 `addTextChangedListener()`은 건드리지 않는다. 필요하다면 `removeTextChangedListener()`을 호출하지 않아도 외부에서 내부 레퍼런스들을 일괄 정리할 수 있는 함수를 만들면 좋을것 이다. 

### 2. 예제 코드 

```kotlin
class RxEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {
    private val textWatcherRef: TextWatcher
    private val textChanged = PublishSubject.create<String>()

    init {
        textWatcherRef = addTextChangedListener {
            textChanged.onNext(it?.toString() ?: "")
        }
    }

    fun getTextChangedListener(): Observable<String> {
        return textChanged.hide().observeOn(AndroidSchedulers.mainThread())
            .distinctUntilChanged()
            .debounce(500, TimeUnit.MILLISECONDS)
    }

    fun dispose() {
        textChanged.onComplete()
        removeTextChangedListener(textWatcherRef)
    }

    override fun onDetachedFromWindow() {
        dispose()
        super.onDetachedFromWindow()
    }
}
```

사용하기 위해서는 MVVM의 경우 `@BindingAdapter`함수 등 에서 처리한다. 

```kotlin
@BindingAdapter(value = ["disposer", "textChanged"])
fun addTextChangedListener(
    et: RxEditText,
    disposer: RxDisposer,
    textChangedListener: (String) -> Unit
) {
    disposer.addDisposer(
        et.getTextChangedListener()
            .subscribe {
                textChangedListener(it)
            }
    )
}
```

`dispoer`는 `RxDisposer`인터페이스를 구현한 객체로서, 안드로이드 컴포넌트(액티비티, 프래그먼트)의 라이프사이클에 맞춰 Rx에서 생성한 `Disposable`레퍼런스를 compiste 패턴으로 갖고 있다가 일괄 해제해주는 역활을 갖는다. 

위에서 `disposer`를 통해 생성된 Rx의 `Disposable`의 레퍼런스를 전달 해 준다. 참고로 위 코드에서 `RxDisposer`는 ViewModel이 된다. 

`textChangedListener`는 ViewModel에 Rx에서 생성된 Observable스트림에서 얻은 문자열을 최종적으로 전달 해 준다. ViewModel에서는 실제 `RxEditText`에서 뭘 하는지는 관심이 없다. ViewModel입장 에서는 콜백을 통해 전달받는 문자열을 처리 하기만 하면 되기 때문이다. 

위 예제는 개인적으로 궁금하여 만들어보았으며 더 좋은 레퍼런스를 가진 라이브러리가 있을 것 이다. 사실, 얼마전 면접 봤다가 TextWatcher에 대해서 이야기가 나왔는데 TextWatcher에 cold stream을 이용해 디바운스등을 사용한다고 하니까 이해를 못해서 직접 만든것 이기도 하다. 

목적에 부합하기 위해 작성되는 코드의 로직에는 정답이 없다고 생각 한다. 최근 면접을 여러곳 다니면서 여러 개발자들과 이야기를 나눌 수 있는 좋은 기회가 있었다. 그 중엔 선민사상으로 가득찬 위험한 개발자도 있었고, 정답만을 요구하는 개발자들도 보았었다. 뭐 그럴수도 있겠지만 많은 생각을 하게 만들어 주어 고맙다는 생각이 든다. 
