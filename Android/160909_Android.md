# Instagram의 Gradent transition effect의 구현

### 배경
- 인스타그램 앱 에서 로그인, 가입 화면에서는 배경에 화려한 그라데이션이 트랜지션 효과와 함께 계속 변화하는것을 볼 수 있다. 
- 디자이너에게 부탁하여 이미지를 만들어 리소스로 두고 관리하는 방법도 있지만 이 방법은 유지 보수가 불편하다. 컬러를 변경하거나 그라데이션 위치 값들을 변경 하려면 다시 이미지를 요청하고 수정한뒤에 적용 해야 한다. 
- 그라데이션의 이미지 리소스를 추가 하는게 아닌 `Drawable`리소스로 만들어서 관리하는 방법을 구현해 보았다.  

### 1. 그라데이션이 적용된 `Drawable`리소스 만들기. 
- 그라데이션 적용 방법의 예는 다음과 같다. 
```xml
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">

   <item>
      <shape android:shape="rectangle">
         <gradient
            android:angle="0"
            android:endColor="@color/flat_river_sel"
            android:startColor="@color/flat_river"/>
      </shape>
   </item>

   <item>
      <shape android:shape="rectangle">
         <gradient
            android:angle="135"
            android:endColor="#0d9b59b6"
            android:startColor="@color/flat_purple"/>
      </shape>
   </item>

</layer-list>
```
- `layer-list`를 사용하여 층간계층에 그라데이션을 새로 덮어씌우는 방식이다. base가 되는 녀석의 그라데이션을 `angle` 0으로 설정되어 그리고 그 위에 0d(5%)의 투명도를 가진 컬러와 primary 컬러를 설정한 그라데이션 객체를 추가 한다. 
- 그라데이션의 angle은 45의 배수로만 이루어 진다. 또 한 그 값은 0 부터 360까지만 지원함을 기억 해야 한다. 
- 아무튼 이런식으로 `Drawable`리소스를 몇개 더 만든다. 예제는 아래와 같이 4개를 만들었다.   
![gradient drawables](https://github.com/ksu3101/TIL/blob/master/Android/images/lgrs.png)

### 2. `animation-list` frame animation drawable 만들기 
- 위에서 만든 4개를 가지고 다시 `animation-list`를 이용한 frame animation Drawable을 만든다. 예제로 만든 `bg_transition1.xml`은 아래 소스를 참고 하자. 
```xml
<animation-list xmlns:android="http://schemas.android.com/apk/res/android">
   <item
      android:drawable="@drawable/bg_lgr01"
      android:duration="10000"/>

   <item
      android:drawable="@drawable/bg_lgr02"
      android:duration="10000"/>

   <item
      android:drawable="@drawable/bg_lgr03"
      android:duration="10000"/>

   <item
      android:drawable="@drawable/bg_lgr04"
      android:duration="10000"/>
</animation-list>
```

### 3. 특정 뷰에 적용 시키기 
- 지금까지 만든 animation-list drawable을 특정 뷰의 `background`속성에 적용 한다. 
```xml
<RelativeLayout
  android:id="@+id/login_container"
  android:layout_width="match_parent"
  android:layout_height="match_parent"
  android:background="@drawable/bg_transition1"
  android:orientation="vertical">
  
  ...
  
</Relativelayout>
```

### 4. Activity(혹은 Fragment등) 의 라이프사이클에 동기화 시키기
- animation-list drawable을 background속성으로 둔 특정 뷰의 인스턴스를 얻고 `getBackground()`메소드를 이용하여 Drawable을 얻는다. 이 때 인스턴스가 `AnimationDrawable`인지 체크 하고 캐스팅 해서 적용 시켜 준다. 
- 얻은 `AnimationDrawable`의 `setEnterFadeDuration`과 `setExitFadeDuration`을 설정 한다. 이 값이 길면 길 수록 트랜지션 할 시간이 길어진다. 
- 아래의 예제 코드에서는 Activity의 `onResume()`메소드와 `onPause()`에서 `AnimationDrawable`을 동기화 해서 사용하는 것을 알 수 있다. 
```java
public class LoginActivity
    extends BaseActivity
    implements LoginActivityPresenter.View {
    
  @BindView(R.id.login_container)
  RelativeLayout    container;
  
  AnimationDrawable animDrawable;
    
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.login_activity);
    ButterKnife.bind(this);

    animDrawable = (AnimationDrawable) (container.getBackground() != null && container.getBackground() instanceof AnimationDrawable ?
        container.getBackground() : null);
    if (animDrawable != null) {
      animDrawable.setEnterFadeDuration(10000);
      animDrawable.setExitFadeDuration(10000);
    }
      
    ...
  }
    
  @Override
  protected void onResume() {
    super.onResume();
    if (animDrawable != null && !animDrawable.isRunning()) {
      animDrawable.start();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (animDrawable != null && animDrawable.isRunning()) {
      animDrawable.stop();
    }
  }
}
```

### 결과물  
![result_captured_image](https://github.com/ksu3101/TIL/blob/master/Android/images/videotogif_2016.09.09_17.53.07.gif)
