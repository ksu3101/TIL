# [StateButton module](https://github.com/ksu3101/SnsTemplate/blob/master/app/src/main/java/kr/swkang/snstemplate/utils/widgets/StateButton.java)
---

## 1. Description
네트워크 통신을 비동기로 하다 보면 사용자에게 현재 (비동기)작업 중임을 사용자한테 알려야 할 경우가 생긴다. 사용자 입장에서는 앱이 죽거나 멈추는것을 기대 하지 않는다.

그래서 보통 `ProgressBar`나 계속 움직이는 어떠한 문구, 이미지를 배치하곤 한다.

로그인 버튼이나 어떠한 비동기 작업을 수행하는 버튼을 눌렀을 경우 다른 작업의 행동까지 방해하고 싶지 않을 경우가 있다.

![로그인 버튼 상태 1](https://github.com/ksu3101/TIL/blob/master/Android/images/statebutton_01.jpg)   
![로그인 버튼 상태 2](https://github.com/ksu3101/TIL/blob/master/Android/images/statebutton_02.jpg)  

`StateButton`에서는 개발자가 주어진 Selector drawable을 활용한 커스터마이징된 뷰에 State를 설정 하여 `Enable`, `Waiting`, `Disabled`플래그를 두고 그에 따라 알아서 내부 뷰들을 설정한다.

`Waiting`플래그를 제외 하고는 ProgressBar는 알아서 감춰질 것 이다.  
자세한 설정은 다음 예제를 참고 할 것. 

## 2. Example 
```xml
 <kr.swkang.snstemplate.utils.widgets.StateButton
    android:id="@+id/login_btn_login"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:sb_btn_paddingBottom="12dp"
    app:sb_btn_paddingLeft="34dp"
    app:sb_btn_paddingRight="34dp"
    app:sb_btn_paddingTop="12dp"
    app:sb_btn_selector="@drawable/btn_c_white"
    app:sb_btn_text_disabled="@string/c_login"
    app:sb_btn_text_enable="@string/c_login"
    app:sb_btn_text_wait="@string/c_login_w"
    app:sb_btn_textcolor="@drawable/btn_c_white_text"
    app:sb_btn_textsize="15sp"
    app:sb_progressbar_color="@color/white"
    app:sb_progressbar_visible="true"
    app:sb_state="enable"/>
```
  
