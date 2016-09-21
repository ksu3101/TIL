# Android Studio의 Gradle 빌드가 너무 느려졌을 경우

과거에 Android Studio를 이용한 gradle빌드 과정중에 발생한 크리티컬 이슈를 다시 정리해 보았다. 당시 이슈에 해당하는 개발 환경은 다음과 같다.
- Windows 7 + Android Studio 2버전 이하 (버전에 상관 없을듯)
- 낮은 퍼포먼스의 개발 PC
- 65k 메소드 제한옵션을 풀기 위한 `multiDexEnable`속성의 `true`세팅. 
- 높은 타 라이브러리 모듈의 의존성 

## 1. 배경 
 전 회사에서 근무 하면서 작업 했던 안드로이드 앱 프로젝트에 라이브러리 모듈의 의존성 추가가 많아지면서 여러가지 설정을 하게 되었다. 
 그 중에서도 65k메소드 제한을 푸는 옵션인 `multiDexEnable`속성을 gradle에서 `true`로 하였다. 아무래도 메소드 카운팅이 제대로 되지 않은 라이브러리들을 많이 가져다 의존시켜 버리니 걱정이 되어서 추가 한 것이다. 앞으로 발생할 이슈도 방지 할겸.. 

 설정 한 뒤 시간이 좀 지나서 언제부턴가 gradle의 build속도가 너무나도 느려지기 시작 했다. 원래 1분 내로 컴파일, 빌드까지 끝 내던 프로젝트가 2015년 11월 18일 당시 10~20분을 소모 하기 시작했다. 그러다가 결국 빌드 도중에 `Out of memory`를 내뱉고 빌드를 실패하는 케이스도 생기기 시작했다. 

 처음에는 이러한 느리고 크래시 까지 발생하는 빌드 런타임 의 원인에 대해서 낮은 퍼포먼스의 회사 컴퓨터 문제라고 생각 되었다. 실제로 그다지 좋은 성능의 데스크 탑은 아니었으니 이로 인한 문제라고 생각 했었다. 

 그래서 회사가 아닌 집 컴퓨터(훨씬 성능이 좋음)에서 프로젝트를 가져와 컴파일, 빌드 해 보았는데 여전히 동일한 문제가 발생했다. 정확한 이유가 파악되지 않는 상황이었다.  
 
 다음 방법으로는 아예 새로운 프로젝트를 생성 하고 프로젝트앱의 의존성을 하나씩 추가해 나가면서 컴파일, 빌드를 해보는 것 이었다. 이 방법 에서는 정상적으로 1분내에 컴파일, 빌드를 완료 했었다.

 비정상적인 케이스와 정상적인 케이스를 비교해 보았다. 의존성은 완전히 동일했으며 gradle버전, 내부 sdk버전, 타겟 sdk등등 모두 동일했다. 다만 다른게 하나 있었다면 **65k 메소드 제한을 푸는 옵션**인 `multiDexEnable`속성의 `true`옵션 뿐 이었다.     

## 2. Profiling  
 gradle에서는 컴파일, 빌드 도중에 프로파일링을 할 수 있다. 
 ![프로파일링 속성 추가](http://burkdog.cafe24.com/wp/wp-content/uploads/2015/11/asprofile.png)
 Android studio의 `File -> Settings -> Build, Execution, Deployment -> Compiler` 항목에 가서 `Command-line Options`의 내용에 `--profile`을 입력 하면 된다. 

 프로파일링 패러미터를 추가 하여 build를 하게 되면 프로젝트의 `app`폴더 내 `build`폴더에서 `reports`라는 폴더가 생성된다. 그리고 내부에 빌드 결과들과 시간 정보들이 저장된 profile파일들이 생성된다. 내용은 아래 이미지와 같다. 
 ![프로파일링 결과물]()http://burkdog.cafe24.com/wp/wp-content/uploads/2015/11/gradle_profiling.png 

## 3. 해결
 profiling build를 시작하고 결과는 다음과 같았다.  
 ```
 Task                                                       Duration Result
:app                                                        4m26.89s (total)
:app:transformClassesWithDexForDebug 4m19.59s
 ```
 위 내용은 4분 26초 가량이 걸린 빌드의 profile내용이다. `app:transformClassesWithDexForDebug`를 하는 동안에만 4분 19초를 사용함을 알 수 있다. 그 외에는 10초 이내로 다 하는데 말이다. 
 아마도 자바 컴파일러에서 앱, 라이브러리 모듈 등의 클래스 파일들을 dex파일로 패키징 하는 과정으로 추정되었다. 하지만 이는 정상적인 케이스 보다 너무나도 느렸다! [참고](https://developer.android.com/studio/build/index.html?hl=ko)

 찾다가 `dexOptions`에 힙 사이즈를 설정 할 수 있는 옵션을 찾게 되었다. 프로젝트의 `build.gradle`에 다음과 같은 항목을 추가 하였다. [참고](http://kevinpelgrims.com/blog/2015/06/11/speeding-up-your-gradle-builds/) 
 ```gradle
 defaultConfig {
    ...
    multiDexEnabled true
}

dexOptions {
    incremental true
    javaMaxHeapSize "2048M"
}
 ```
클래스 파일을 dex파일로 패키징 하면서 사용되는 자바 힙 메모리의 사이즈가 생각보다 작은거 같다. 적은 메모리를 가지고 수많은 라이브러리 모듈들의 class들 까지 같이 패키징 하다가 OOM도 발생하고 그런것 같았다. 

옵션을 적용 후 다시 빌드 한뒤에 프로파일링 된 내용을 확인 하니,
```
Task                                                        Duration Result
:app                                                        33.766s (total)
:app:transformClassesWithDexForDebug 27.566s

Task                                                        Duration Result
:app                                                       44.171s (total)
:app:transformClassesWithDexForDebug 26.768s
```
확연히 빨라진 컴파일, 빌드 타임을 확인 할 수 있었다. 


