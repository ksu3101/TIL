### Daily development learning
#### 1. Android method counting 
- (Dexcount Gradle Plugin)[https://github.com/KeepSafe/dexcount-gradle-plugin]을 이용해서 쉽게 앱에서 사용중인 메소드의 갯수를 파악 할 수 있다. 
- 기능은 비슷 하지만 메소드의 수를 줄여서 앱의 사이즈를 줄이는데 도움을 줄 수 있다고 한다. [링크참고](http://jeroenmols.com/blog/2016/05/06/methodcount/)
- 예를 들어 비동기 이미지로더 라이브러리의 경우 자체적으로 사용되는 메소드의 숫자는 다음과 같다. 이런식으로 메소드 카운팅이 되면 적합한 라이브러리를 선택 하는데 더 도움이 될 것이다. 

Library | Method count  
--- | ---  
Picasso 2.5.2 | 849  
Universal Image Loader 1.9.5 | 1206  
Glide 3.7.0 | 2879   
Fresco 0.9.0 | 12984  
- 앱의 프로젝트 root `build.gradle`파일의 `dependencies`항목에 내용을 추가 한다. 
```
classpath 'com.getkeepsafe.dexcount:dexcount-gradle-plugin:0.5.0'
```
- 그리고 앱의 `build.gradle`내용의 최 상단에 다음과 같은 내용을 추가 한다. (`com.android.application`아래에 추가 한다)
```
apply plugin: 'com.getkeepsafe.dexcount'
```
- 앱이 실제 존재 하는 폴더로 이동하여 아래와 같은 메시지를 콘솔등에서 입력 한다. 
```
./gradlew assembleDebug
```
- OSX등에서 `Permission deined`메시지가 bash로부터 출력 되면 `gradlew`에 x(실행 권한)을 추가 한다. 명령어는 `chmod +x gradlew`이다.
- 성공적으로 빌드를 완료 하고 나면 `BUILD SUCCESSFUL`을 포함한 메시지들이 출력 될 것이다. 그리고 `{PROJECT_LOCATION}/build/outputs/dexcount/debugChart`에서 그래픽 리포트가 생성됨을 볼 수 있다. 
- 그리고 기본적으로 빌드를 완료 하고 나면 콘솔 메시지에 아래와 같이 사용되는 메소드의 갯수를 알려 준다. 
```
Total methods in app-debug.apk: 33717 (51.45% used)
Total fields in app-debug.apk:  15843 (24.17% used)
Methods remaining in app-debug.apk: 31818
Fields remaining in app-debug.apk:  49692
```

