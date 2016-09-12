# Lollipop 이상에서 Transparent StatusBar 적용 하기 

## StatusBar의 상태  

![Normal StatusBar](https://github.com/ksu3101/TIL/blob/master/Android/images/layout_structure_system_color3.png)  
### 1. 기본 StatusBar 상태  
- `AndroidManifest.xml`에서 Application에 적용된 `android:theme`속성의 테마에 적용된 컬러들의 기본 컬러가 설정 될 것이다. 
- `color.xml`의 `colorPrimaryDark`속성의 컬러 값이 상단 status bar의 컬러가 된다.   
   
--- 
![Colored StatusBar](https://github.com/ksu3101/TIL/blob/master/Android/images/layout_structure_system_color1.png)     
### 2. 컬러가 설정된 StatusBar 상태   
- `color.xml`의 `colorPrimaryDark`속성 컬러 값을 사용자가 지정했을 경우 그 컬러가 보여 진다.  
- 혹은 runtime에서도 Status Bar의 컬러 값을 지정 할 수 있다. 아래 메소드를 통해서 지정 할 수 있다.  
```java
protected void setStatusBarBackgroundColor(@ColorInt int color) {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
    Window window = getWindow();
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    window.setStatusBarColor(color);
  }
}
```  
   
--- 
![Transparent StatusBar](https://github.com/ksu3101/TIL/blob/master/Android/images/layout_structure_system_color2.png)     
### 3. alpha + 컬러가 설정된 StatusBar 상태    
- 지정된 컬러에 alpha값을 적용 했을 경우 투명한 상태바가 된다.    
- 만약 `value-21`폴더를 resource내에 만들고, `styles.xml`의 테마 재정의 부분에서 `<item name="android:windowTranslucentStatus">true</item>` 항목을 추가 했다면 StatusBar와 구현된 액티비티의 레이아웃이 곂친다. 
- 곂쳤을 경우 상단의 padding혹은 margin에 유의해서 사용 한다. 


