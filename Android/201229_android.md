## Android FileProvider를 이용한 파일 공유 하는 방법 

> 이 문서는 Vlad Sonkin의 [How To Share Files With Android FileProvider](https://vladsonkin.com/how-to-share-files-with-android-fileprovider/)을 번역 하였다. 

앱에서 공유는 필수적인 기능이라고 할 수 있다. 간단한 데이터외 에도 파일을 공유 하려 할 때 Android FileProvider는 훌륭한 도우미가 될 것 이다. 이 글에서는 FileProvider가 필요한 이유와 파일을 다른 앱과 공유 하는 방법에 대해 간단하게 설명 하려 한다. 

### 1. Why we need Android FileProvider 

어떤 가계부앱이 있다고 가정해보자. 사용자는 수입과 지출을 입력하면 앱에 이를 멋진 그래프로 보여주고 앱 디렉토리에 CSV파일로 저장할수 있게 한다. 

여기에 필수적인 기능은 사용자에게 원시 CSV데이터를 공유 할 수 있는 기능을 제공하여 사용자가 Google스프레드 시트 또는 다른 앱 에서 열수 있도록 하는 것 이다. 그에대한 첫번째 시도로 암시적인 Android Intent를 사용하여 파일을 내보내는 것 이다. 

```kotlin
val shareIntent = Intent().apply {
    action = Intent.ACTION_SEND
    putExtra(Intent.EXTRA_STREAM, csvUri)
    type = "text/csv"
}

startActivity(Intent.createChoose(shareIntent, resources.getText(R.string.send_to)))
```

하지만 수신 어플리케이션(예를 들면 Google스프레드 시트)에서 가계부 앱 디렉토리 내 에 있는 이 파일에 접근할 수 있는 권한이 없기 떄문에 정상적으로 작동하지 않는다. 

이 경우, 두가지 옵션을 사용할 수 있다. 

1. 일반 [`MediaStore`](https://developer.android.com/reference/android/provider/MediaStore)에 CSV파일을 추가 한다. 단점으로는 파일을 모든 앱 에서 사용할 수 있다. 이 가계부 앱의 경우 민감한 데이터가 있을 수 있기 때문에 이 방식은 사용하지 않을 것 이다. 
2. `ContentProvider`를 사용하여 수신 어플리케이션에서 필요한 파일에 접근할 수 있는 올바른 권한의 보유 여부를 확인 한다. 이 ContentProvider를 만드는 쉬운 방법은 Android FileProvider를 사용 하는 것 이다. 

### 2. Android FileProvider Example

Android FileProvider는 ContentProvider의 하위 클래스이며 `file://Uri`eotls `content://Uri`를 만들어 파일을 더 안전하게 공유 할 수 있다. 이는, 파일의 컨텐츠만 노출하고 파일의 실제 위치는 숨겨져 있기 때문에 더 안전하다. 

FileProvider는 실제로 ContentProvider와 같으며 `AndroidManifest.xml`에 추가하여 사용한다. 

```xml
<application>
    <provider
        android:name="androidx.core.content.FileProvider"
        android:authorities="com.vladsonkin.fileprovider"
        android:exported="false"
        android:grantUriPermissions="true">
        <meta-data
            android:name="android.support.FILE_PROVIDER_PATHS"
            android:resource="@xml/provider_paths" />
    </provider>
</application>
```

각 속성들과 라인들에 대해 설명하자면 아래와 같다. 

1. `android:name`은 AndroidX의 FileProvider클래스를 참조 한다. 
2. `android:authorities`는 Android시스템이 다른 Provider를 구분하기 위한 고유한 값이다. 일반적으로 앱의 도메인 이름을 추가하고 끝에 "fileprovider"를 붙여 사용 한다.
3. `android:exported`플래그는 이 Provider가 외부에 공개적인지 여부를 설정 한다. 공개된 경우 다른 모든 앱 에서 권한없이 이 content provider에 접근 할 수 있다. 
4. `android:grantUriPermissions`속성을 사용하면 파일에 대한 임시 엑세스 권한을 부여 할 수 있다. 
5. `<meta-data>`요소는 이 provider의 공유 경로를 설정 한다. 

이제 `app/res/xml`디렉토리에 `provider_paths.xml`을 만들고 공유 경로를 지정할 수 있다. 

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <files-path name="share" path="external_files" />
</paths>
```

`<patns>`의 요소는 상이할 수 있으며 사용법은 파일 위치에 따라 다르다. 

1. `<files-path>`는 앱의 내부 저장소에 있는 `files/` 디렉토리 이다. 
2. `<cache-path`는 앱의 내부 저장소에 있는 캐시 디렉토리 이다. 
3. `<external-path>`는 외부 저장소의 최상위 위치(root) 이다. 
4. `<external-files-path>`는 앱 외부 저장소의 최상위 위치(root) 이다. 
5. `<external-cache-path`는 앱 외부 캐시 저장소의 최상위 위치(root) 이다. 
6. `<external-media-path>`는 앱 외부 미디어 저장소의 최상위 위치(root) 이다. 

이 예제에서 CSV파일은 내부 저장소에 있으므로 `<files-path>`를 사용 하였다. 이 요소에는 두가지 속성을 갖고 있다. 

1. `name`은 URI하위 디렉토리로 주어진 값 으로서 이를 대체하여 보안을 강화할 수도 있다. 
2. `path`는 공유할 하위 디렉토리를 지정 한다. 

> 참고 : 개별 파일이 아닌 디렉터리만 공유 할 수 있다. 

이 시점에서 FileProvider가 설정 완료 되었고, 드디어 파일을 공유할 수 있다. 

```kotlin
val csvUri = FileProvider.getUriForFile(this, getPackageName(), csvFile)

val intent = ShareCompat.IntentBuilder.from(this)
    .setStream(csvUri)
    .setType("text/csv")
    .intent
    .setAction(Intent.ACTION_SEND)
    .setDataAndType(csvUri, "text/csv")
    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

startActivity(intent)
```

여기에서 FileProvider의 도움으로 Uri파일을 가져와 공유Intent를 생성하고 실행 한다. 선택한 수신자 앱은 이제 파일 작업권한을 갖게 되었지만 실제 파일의 위치는 모른다. 

### 3. Summary

파일이 앱 디렉토리에 있는 경우에도 파일을 공유 할 수 있다. Android FileProvider를 사용하면 쉽게 공유 할 수 있으며, 이를 위해선 앱 Manifest에서만 지정하면 된다. 

또한 파일의 위치가 숨겨져 있고 파일을 공유할 시기와 위치는 사용자가 결정하기만 하면 되기 때문에 더 안전한 접근 방식으로 간주된다. 