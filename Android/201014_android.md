## Firebase - Crashlytics 추가 하기

Crashlytics를 추가 하기 전에 Firebase console을 사용할 수 있도록 설정하는 것 을 잊지 말자. 

### 1. Firebase-Crashlytics에 대해 gradle 추가 

프로젝트 레벨의 `build.gradle`파일에 아래와 같은 의존 리포지터리와 라이브러리 의존 등을 추가 한다. 

```
buildscript {
    repositories {
        google()
    }

    dependencies {
        classpath 'com.google.gms:google-services:4.3.4'

        // Firebase의 Crashlytics를 제대로 사용하기 위해서는 gradle버전을 2.0.0 이후 
        // 그리고 안드로이드 스튜디오 4.1버전 이후 에서 사용 한다. 
        classpath 'com.google.firebase:firebase-crashlytics-gradle:2.3.0'
    }
}

allprojects {
    repositories {
        google()
    }
}
```

그리고 app레벨의 `build.gradle`에 아래와 같이 Crashlytics의 의존을 추가 한다. 

```
dependencies {
    // Import the BoM for the Firebase platform
    implementation platform('com.google.firebase:firebase-bom:25.12.0')

    // BoM을 위 처럼 의존을 정의했다면 아래 라이브러리의 버전은 작성하지 않아도 된다. 
    implementation 'com.google.firebase:firebase-crashlytics'
    implementation 'com.google.firebase:firebase-analytics'

    // 만약 NDK에 대해서도 Crashlytics를 사용하려면 위 의존에서
    // 'com.google.firebase:firebase-crashlytics'의존을 아래 의존으로 변경 한다. 
    implementation 'com.google.firebase:firebase-crashlytics-ndk'
}
```

### 2. NDK 추가 설정 

만약 앱 에서 NDK를 사용 하고 있다면 아래와 같이 app레벨의 `build.gradle`의 의존을 일부 변경해야 한다. 

```
dependencies {
    // 만약 NDK에 대해서도 Crashlytics를 사용하려면 
    // 'com.google.firebase:firebase-crashlytics'의존을 아래 의존으로 변경 한다. 
    implementation 'com.google.firebase:firebase-crashlytics-ndk'
}
```

그리고 Native symbol을 Firebase의 Crashlytics에 전송할지 여부에 대해 필요한경우 아래와 같이 설정한다. (앱 빌드 퍼포먼스를 위해서 사용하지 않는다.)

```
android {
    buildTypes {
        release {
            firebaseCrashlytics {
                nativeSymbolUploadEnabled true
                strippedNativeLibsDir ‘path/to/stripped/parent/dir’
                unstrippedNativeLibsDir ‘path/to/unstripped/parent/dir’
            }
        }
    }
}
```

### 3. 테스트 해 보기

Crashlytics에 정상적으로 Crash 리포트가 전달되는지 확인 하고 싶다면 아래와 같이 예외를 `throw`하면 된다. 

```java
crashButton.setOnClickListener(new View.OnClickListener() {
   public void onClick(View view) {
       throw new RuntimeException("Test Crash"); 
   }
});
```

위 처럼 강제로 발생한 예외는 Crashlytics에 전달되기 전까지 약간의 시간을 필요로 한다. 보통 5분 이내에 전달이 될 것 이다. 

### 4. Crashlytics의 디버깅 로그 보기 

예외가 발생하였는데도 Firebase의 Crashlytics에 전달되지 않을 경우 실제로 예외가 전달 되는지 여부를 디버깅 할 필요가 있다. 그러기 위해서는 terminal에서 아래와 같이 `adb`를 사용해서 출력되는 로그를 볼 수 있다. 

우선 로그캣에 전달될 로그를 얻기 위해 adb의 shell을 통해 아래와 같은 명령어를 통해 로그출력을 허용 한다. 

```
$ adb shell setprop log.tag.FirebaseCrashlytics DEBUG
```

그리고 해당 로그를 보려면 아래와 같은 명령어를 치면 된다. 

```
$ adb logcat -s FirebaseCrashlytics
```

그만 보고 싶다면 터미널에서 주로 사용되는 중지 단축키를 사용하면 된다. 

### 5. Firebase의 Crashlytics에서 사용되는 기본 메소드들 

기본적으로 이전 Fabric과는 다르게 `Application`을 상속받는 클래스에서 `Fabric.with()`정적 메소드를 통해 초기화를 할 필요 없이 Crashlytics는 바로 사용 할 수 있다. Crashlytics를 사용하기 위해 인스턴스를 얻는 방법은 아래처럼 싱글턴 패턴으로 얻을 수 있다. 

```kotlin
val firebaseCrashlytics = FirebaseCrashlytics.getInstance()
```

자주 사용되는 Crashlytics의 API 메소드들은 아래와 같다. 

```kotlin
// Crashlytics에 치명적이지 않은 예외 를 전송 한다. 보통 try-catch의 catch블럭 내 에서 사용 된다. 
FirebaseCrashlytics.getInstance().recordException(e)

// 사용자 정의 로그를 전송 한다. 
FirebaseCrashlytics.getInstance().log("My Log Message")

// 사용자에 대한 정의된 구분자를 설정 한다. 이는 유니크한 유저의 ID나 숫자, 해시코드 등이 있다. 
FirebaseCrashlytics.getInstance().setUserId(userId)

// Crashlytics에 전송될 로그의 키를 정의 한다. 
// value는 String, boolean, int, long, float, double등 이 가능 하다. 
FirebaseCrashlytics.getInstance().someCustomKey("key", value)   
```

### 6. 크래시 리포트를 전달 하지 않는 방법. 

예를 들어 디버깅 앱 에서 크래시 리포트를 전달하지 않아야 할 때 아래와 같은 2가지 방법을 통해 예외 전송을 회피 할 수 있다. 

#### 6.1 앱의 `manifest`파일에 meta-data를 정의하여 값을 적용 한다. 

안드로이드 앱 의 `AndroidManifest.xml`에 아래와 같이 meta-data를 정의하고 `value`를 `false`로 설정 한다. 

```xml
<meta-data
    android:name="firebase_crashlytics_collection_enabled"
    android:value="false" />
```

#### 6.2 런타임 코드에서 직접 메소드 호출 

런타임 코드에 직접 메소드를 호출하여 값을 적용 한다. 

```kotlin
FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
```