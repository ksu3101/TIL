# SoftKeyboard상태 Callback을 구현한 Activity 

## 1. SoftKeyboard와 EditText의 문제. 
키보드의 입력을 받기 위한 EditText의 위치가 하단에 존재 할때 보통 Activity 내부에 `ScrollView`로 자식 뷰들을 래핑 하고 난 뒤에 `AndroidManifest.xml`파일에서 해당 액티비티의 속성 중 `windowSoftInputMode`을 지정 하여 내부에서 뷰들의 크기를 다시 계산 하는 `adjustResize` 플래그를 쓰거나 혹은 화면 전체가 스크롤 되는 `adjustPan`을 사용 한다. 

만약 `adjustResize`플래그를 사용 했을 경우 뷰들의 재 계산된 크기는 `onSizeChanged()`메소드를 통해서 확인 할 수 있다. 

하지만 이런 방법엔 문제가 있다. 하단에 위치한 EditText의 위치를 SoftKeyboard가 완전히 가려 버리는 일도 발생 한다. 예를 들어 액티비티에 `FLAG_FULLSCREEN`등의 layout parameter등을 설정 했을 경우 제대로 스크롤 되지 않는 문제가 발생 한다. 
 
이런 경우 SoftKeyboard의 상태에 대해서 알아야 할 것이다. 하지만 안타깝게도 안드로이드에서는 키보드 상태에 대한 API를 제공 하지 않는다.

아래의 소스는 액티비티 윈도우의 `getWindowVisibleDisplayFrame()`메소드를 통해 보이는 뷰의 사이즈를 구하고 사이즈의 높이에 대해 계산 하여 키보드의 등장 여부를 판단 하는 소스 이다. 
 
```java
 public abstract class BaseActivity
    extends AppCompatActivity
    implements BaseView {
  protected static final int MIN_KEYBOARD_HEIGHT = 150;

  private BasePresenter                           basePresenter;
  private View                                    decorView;
  private ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener;

  // - - Abstract methods - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  public abstract BasePresenter attachPresenter();

  // - - Callback Methods - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  public void onKeyboardShown(int keyboardHeight) {
  }

  public void onKeyboardHidden() {
  }

  // - - Life cycle methods  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.basePresenter = attachPresenter();
  }

  @Override
  protected void onDestroy() {
    if (basePresenter != null) {
      // unscribe registered Subscriptions
      basePresenter.destroy();
    }
    removeLayoutListener();
    super.onDestroy();
  }

  // - - Implements methods - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  @CallSuper
  @Override
  public void onError(String tag, String message) {
    Log.e(tag != null ? tag : "BaseActivity", message != null ? message : "Message is null.");
  }

  // - - Common methods - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  private void removeLayoutListener() {
    if (decorView != null && globalLayoutListener != null) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        decorView.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
      }
      else {
        decorView.getViewTreeObserver().removeGlobalOnLayoutListener(globalLayoutListener);
      }
    }
  }

  /**
   * SoftKeyboard가 보이는 경우 `onKeyboardShown()`메소드가 호출 된다.
   * 보이지 않는 경우나 가려진 경우에는 `onKeyboardHidden()`메소드가 호출 되어 진다.
   * 필요에 따라서 두 메소드를 재정의 하고 `checkSoftKeyboardOnActivity()`메소드를 호출 하면 된다.
   */
  public void checkSoftKeyboardOnActivity() {
    removeLayoutListener();
    
    decorView = getWindow().getDecorView();
    globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
      private final Rect windowVisibleDisplayFrame = new Rect();
      private int lastVisibleDecorViewHeight;

      @Override
      public void onGlobalLayout() {
        // 보여지고 있는 window의 크기를 사각형 Rect객체로 가져 온다.
        decorView.getWindowVisibleDisplayFrame(windowVisibleDisplayFrame);
        final int visibleDecorViewHeight = windowVisibleDisplayFrame.height();

        // 보여지고 있는 높이의 계산 결과에 따라 키보드의 등장 유무를 확인 한다.
        if (lastVisibleDecorViewHeight != 0) {
          if (lastVisibleDecorViewHeight > visibleDecorViewHeight + MIN_KEYBOARD_HEIGHT) {
            final int currentKeyboardHeight = decorView.getHeight() - windowVisibleDisplayFrame.bottom;
            // 키보드가 등장중인 상태
            onKeyboardShown(currentKeyboardHeight);
          }
          else if (lastVisibleDecorViewHeight + MIN_KEYBOARD_HEIGHT < visibleDecorViewHeight) {
            // 키보드가 사라진 상태
            onKeyboardHidden();
          }
        }
        lastVisibleDecorViewHeight = visibleDecorViewHeight;
      }
    };
  }
}
```
   
## 2. 설명   
`BaseActivity`를 상속한 액티비티 내부에서 키보드의 등장 유무를 콜백으로 받고 싶을 때엔, 키보드 등장 유무에 따른 콜백 메소드들인 `onKeyboardShown()`과 `onKeyboardHidden()`를 재정의 하고 `checkSoftKeyboardOnActivity()`메소드를 콜 하면 된다. 만약 키보드가 보이고 있다면 `onKeyboardShown()`메소드가 불림과 동시에 parameter값으로 등장한 키보드의 높이를 얻는다. 하지만 만약 키보드가 보이지 않게 된다면 `onKeyboardHidden()`메소드가 불려지게 될 것이다. 

키보드의 등장에 따라서 화면의 위치를 재 조정 하고 싶을때 래핑 하는 어떠한 뷰 그룹의 하단에 패딩을 추가 하면 된다. 하단 예제 소스를 보면 알 수 있을 것이다.

```java
@Override
public void onKeyboardShown(int keyboardHeight) {
  if (rv != null) {
    // add padding to bottom
    rv.setPadding(0, 0, 0, KEYBOARD_VISIBILITY_VALUE);
  }
}

@Override
public void onKeyboardHidden() {
  if (rv != null) {
    // remove paddings
    rv.setPadding(0, 0, 0, 0);
  }
}
```
몰론 이렇게 하단 패딩을 추가 한경우 최 하단으로 스크롤 하면 추가된 패딩이 그대로 보이기 때문에 좋은 해결 방법은 아니다. 다른 방법들이 더 있으니 방법을 강구 해 봐야겠다. :) 
 
- 참고 : [링크](https://pspdfkit.com/blog/2016/keyboard-handling-on-android/)
