# Gradle error : `processReleaseResource` issue 
---
## 1. 이 에러는 무엇 인가? 
개인적으로 필요에 의해서 만들던 [SwImageView](https://github.com/ksu3101/SwImageView)라이브러리 모듈이 있었다. 이 모듈을 만들다가 Attribute set을 사용 해야 할 필요성이 있었다. 그 중에서 enum값을 가진 attribute를 만들었다. 이 attribute는 이미지뷰를 통해서 보여줄 이미지 drawable의 형태를 설정하는 enum값으로서 기본적인 사각형 혹은 곡진 사각형인 기본값과 타원형 혹은 계란형이라 할 수 있는 `OVAL`, 그리고 완전한 원인 `CIRCLE`, 이렇게 3개를 가지고 있었다. 
   
문제는 첫번째 기본 enum값을 `DEFAULT`로 만들었었다. 그리고 `attrs.xml`에서는 등록될 스타일 attribute enum의 네임에 `default`를 설정 했 었다.

`attrs.xml`에 등록한 Attribute enum은 다음과 같았다. 

```xml
  <attr name="siv_shapetype" format="enum">
     <enum name="default" value="0" />
     <enum name="oval" value="1"/>
     <enum name="circle" value="2"/>
  </attr>
```
    
문제 없어보였지만 이 상태에서 클린혹은 빌드를 하려고 하면 에러가 발생한다. 에러는 아래와 같았다. 

```
Execution failed for task ':swimageview:processReleaseResources'.  
> com.android.ide.common.ProcessException: org.gradle.process 'command .../sdk/build-tools/23.0.3/aapt'' finished with non-zero exit value 1
```
  
## 2. 정보 찾기  
발생한 예외를 기준으로 구글에서 검색해서 찾아본 것 중에 [눈길이 가는 글](http://stackoverflow.com/questions/19294663/cannot-build-android-project-using-android-studio-gradle-1-7)이 있었다.  
 
이 글에 따르면 위 예외의 이유에 대해서 이렇게 설명 한다.  

  - 잘못된 리소스이름들. (빠져있는 attribute들, 잘못된 태그 기타 등등)
  - 리소스 id를 style에서 작성. 
  - 중복된 리소스나 라이브러리. 
  - `attrs.xml`에서 작성한 attribute중 이름이 존재하지 않음. 
  - 정의된 리소스의 아이디가 맞지 않음. 다른 AAPT버전을 사용 하는 중. 
  - 그외 모르는 이유 등등.. 
  
이 내용을 참고해서 다시 `attrs.xml`을 검토 해 보기 시작했다. 뭐가 잘못 된 것일까? 
 
## 3. 해결 및 결과 
한참 보던 중 `siv_shapetype`의 enum 값 내부를 주석 처리 하고 clean해 보았다. 이떄는 잘 된다. 그래서 이번인 enum값 중 `default`의 이름을 `rectangle`로 변경하고 clean해 보았다. 이럴수가 잘 된다? 
    
아마 안드로이드의 리소스 이름 중 사용 할 수 없는 이름들이 존재 하는것을 본 거 같은 기억이 난다. 그 중에 attribute set 이름에 `default`가 포함 되어 있는걸까? 정확한 이유는 확신이 가지 않지만 그렇지 않을까 생각 된다. 
    
아무튼 아래와 같이 enum의 이름을 변경 하고 clean - build해 보니 잘 되는 것을 확인 하였다. 

```xml
<attr name="siv_shapetype" format="enum">
   <enum name="rectangle" value="0"/>
   <enum name="oval" value="1"/>
   <enum name="circle" value="2"/>
</attr>
  ```
  
  
  
  

