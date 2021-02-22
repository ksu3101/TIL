## Webview data directory Android 9

안드로이드 9부터 앱 안정성과 데이터 무결성을 개선하기 위해 앱은 여러개의 프로세스 사이에서 단일 WebView데이터 디렉토리를 공유할 수 없도록 변경 되었다. 참고로 이 data디렉토리에서는 쿠키, Http캐시 및 웹 검색 과 관련된 영구 저장 및 임시 저장을 담당 하였다. 

그래서 앱에서 두개 이상의 프로세스에서 WebView의 인스턴스를 사용 하는 경우 각 프로세스의 WebView 인스턴스를 사용 하기 전에 `WebView.setDataDirectorySuffix()`을 이용 하여 해당 프로세스에 고유한 data디렉토리 접미사를 지정 해 줘야 한다. 

- [참고 문서](https://developer.android.com/about/versions/pie/android-9.0-changes-28?hl=ko#web-data-dirs)

적용 예제는 아래와 같다. 

```kotlin
@HiltAndroidApp
class PlayGroundApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val processName = getProcessName(this)
            val packageName = this.packageName
            if (!packageName.equals(processName)) {
                WebView.setDataDirectorySuffix(processName)
            }
        }
    }

    fun getProcessName(context: Context): String? {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (p in manager.runningAppProcesses) {
            if (p.pid == android.os.Process.myPid()) {
                return p.processName
            }
        }
        return null
    }

}
```