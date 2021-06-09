# Gradle 오류 "Minimum supported Gradle version is N.M. Current version is N.O." 

안드로이드 스튜디오에서 gradle버전을 업데이트 한 뒤 빌드를 시도 할 때 gradle에서 아래와 같은 오류메시지를 출력하며 빌드가 되지 않는 상황이 발생 했었다.

```
Minimum supported Gradle version is 6.7.1. Current version is 6.5.

Please fix the project's Gradle settings.
```

현재 gradle버전에 맞는 gradle바이너리를 설정 해 주어야 한다. 

`프로젝트\gradle\wrapper\gradle-wrapper.properties`파일을 열어 아래 `distributionUrl`항목의 zip파일 바이너리 버전을 오류 메시지에서의 최소 버전으로 변경 하면 된다. 

```
# 변경 전
distributionUrl=https\://services.gradle.org/distributions/gradle-6.5-bin.zip

# 변경 후
distributionUrl=https\://services.gradle.org/distributions/gradle-6.7.1-bin.zip
```

해당 zip파일 바이너리들의 목록은 [이 링크](https://services.gradle.org/distributions/)에서 확인 할 수 있다. 
