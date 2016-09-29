# LegoLibrary 프로젝트 이슈  
## API 비밀 키 관리  
 LegoLibrary의 경우 Naver API를 사용하기 위한 공개 키로 사용되는 ID와, 비밀키 한쌍을 가지고 있다.

 문제는 이 한쌍의 키 들은 외부에 공개 되면 안되는 '비밀키'를 갖고 있는 것 이다. gitHub에 소스를 오픈하기 위해선 이러한 키 들을 가릴 필요가 있다.

 우선 프로젝트의 앱내부`({project_name}/app/src/)`에 `gradle.properties`를 수정 한다. 만약 이 파일이 없다면 새로 만든다. 그리고 이 파일 내부에 키를 작성 할 것이다. 예로 들면 다음 과 같다.

```
NAVER_CLIENT_ID="발급받은_naver_api의_id값"
NAVER_CLIENT_SECRET="발급받은_naver_api의_비밀키"
```

 이제 추가한 키 값을 앱의 `build.gradle`에서 `BuildConfigs`를 통해서 이용할 수 있게 할 것이다. `{project_name}/app/src/build.gradle` 파일을 열고 다음과 같이 수정 하자. (몰론 `gradle.properties`에서 작성한 API키 값의 키값과 일치 해야 한다.)  

```gradle
...
byildTypes {
    buildTypes.each {
      it.buildConfigField('String', 'NAVER_CLIENT_ID', NAVER_CLIENT_ID)
      it.buildConfigField('String', 'NAVER_CLIENT_SECRET', NAVER_CLIENT_SECRET)
    }
}
...
```
내 프로젝트의 `BuildConfig.NAVER_CLIENT_ID`을 사용해보면 잘 되는것을 확인 할 수 있다.  
