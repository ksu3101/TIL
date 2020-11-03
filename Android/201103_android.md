## DayNight - 다크모드를 간단하게 적용 하는 방법 

이 문서는 Jamshid Mamatkulov
의 [DayNight - Applying dark mode without recreating your app](https://proandroiddev.com/daynight-applying-dark-mode-without-recreating-your-app-c8a62d51092d)를 번역 하였다.

- 예제 프로젝트의 [gitHub](https://github.com/Jamshid-M/DayNight)

### 1. 어플리케이션을 다시 만들지 않고 다크 모드 적용 하려면? 

먼저 앱의 manifest파일의 액티비티에 `configChanges`를 추가 해야 한다. 

```xml
<application
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:supportsRtl="true"
    android:theme="@style/AppTheme">

    <activity android:name=".MainActivity"
        android:configChanges="uiMode"> // 이 라인이 추가 되었음
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>

</application>
```

추가된 라인은 알림 표시줄에서 수동으로 다크모드를 적용할 때 앱을 다시 만들지 않게 한다. 만약 `colors-night.xml`을 사용한 경우 앱이 다시 생성되지 않으므로 건너 뛰게 된다. 

### 2. 수동으로 색 지정 하려면? 

이 경우에는 `colors.xml`에 `night`라는 이름으로 끝나는 각 색상을 추가 하였다. 

```xml
<color name="colorPrimary">#fff</color>
<color name="colorPrimaryDark">#fff</color>
<color name="colorAccent">#D81B60</color>
<color name="colorText">#1A1A1A</color>


// 다크모드 컬러
<color name="colorPrimaryNight">#000</color>
<color name="colorPrimaryDarkNight">#000</color>
<color name="colorTextNight">#dcdcdc</color>
```

색상을 변경 한 후 각 액티비티의 `onConfigurationChanged()`메소드를 재정의 하고 `nightModeFlags`상태에 따라 색상을 적용 하면 된다. 

```kotlin
override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

    if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO){
        applyDayNight(OnDayNightStateChanged.DAY)
    }else{
        applyDayNight(OnDayNightStateChanged.NIGHT)
    }
}
private fun applyDayNight(state: Int){
    if (state == OnDayNightStateChanged.DAY){
        // 밝은모드 컬러를 뷰에 적용 한다 
    } else {
        // 다크모드 컬러를 뷰에 적용 한다
    }
}
```

`onConfigurationChanged()`메소드는 `DayNight`상태가 변경 될 때 마다 호출 된다. (예를 들면, 알림 표시줄에서 다크 모드를 활성화 혹은 비활성화 하였거나 코드에서 수동으로 적용 하였을때)

### 3. 액티비티의 프래그먼트에게는 어떻게 알리는가? 

간단하다. 인터페이스를 정의 하고 다크 모드를 적용하려는 모든 프래그먼트에서 구현해주기만 하면 된다. 

```kotlin
interface OnDayNightStateChanged {

    fun onDayNightApplied(state: Int)

    companion object{
        const val DAY = 1
        const val NIGHT = 2
    }
}
```

위에 정의된 인터페이스를 다크모드 적용할 프래그먼트에서 구현 한다. 

```kotlin
class YourFragment: Fragment(), OnDayNightStateChanged {

    override fun onDayNightApplied(state: Int) {
        if (state == OnDayNightStateChanged.DAY){
            // 밝은모드 컬러를 뷰에 적용 한다 
        } else {
            // 다크모드 컬러를 뷰에 적용 한다
        }
    }
}
```

액티비티에서 프래그먼트는 여러개 일 수 있으므로 DayNight 상태의 변화는 이터레이셔닝 하면서 알려주면 된다. 

```kotlin
private fun applyDayNight(state: Int){
    if (state == OnDayNightStateChanged.DAY){
       // 밝은모드 컬러를 뷰에 적용 한다 
    } else {
       // 다크모드 컬러를 뷰에 적용 한다
    }
    supportFragmentManager.fragments.forEach {
        if (it is OnDayNightStateChanged){
            it.onDayNightApplied(state)
        }
    }
}
```

### 4. 추가 : 상태 표시줄 및 네비게이션바에 다크 모드 적용 하기

상태표시줄과 네비게이션바 도 다크모드에 맞추어 어둡게 보여야 한다. 

```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO) {
        decorView.systemUiVisibility = decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    }
}

if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    var flags: Int = decorView.systemUiVisibility
    flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

    if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO) {
        decorView.systemUiVisibility = flags
    }
    window.statusBarColor = yourColorDay
    } else window.statusBarColor = yourColorNight
```

### 5. 다크 모드 적용 

```kotlin
AppCompatDelegate
.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) // 다크 모드 

AppCompatDelegate
.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) // 밝은 낮 모드

// 시스템 세팅에 따라 다크/밝은 모드를 적용 한다. 
AppCompatDelegate
.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
```