## ConstraintLayout 으로 반응형 UI 만들기

[Android developer 문서](https://developer.android.com/training/constraint-layout?hl=ko) 를 참고 하여 작성. 

### 1. ConstraintLayout 에 대하여

ConstraintLayout 는 레이아웃들의 깊이가 사실상 없고 뷰 계층을 한눈에 볼 수 있어 RelativeLayout 과 비슷하지만 더 유연하고 안드로이드 스튜디오 에서 제공 하는 Layout editor 와 함께 사용 하기 편리하다. 이는 기존 xml 레이아웃을 직접 작성하고 프로퍼티를 직접 기입하여 작성 하는 방식에서 벗어나 GUI 환경에서 xml 레이아웃을 세팅 할 수 있게 해 주었다. 그리고 LinearLayout 에서만 제공 하던 뷰의 비율(weight) 속성또한 제공 하며 기존 레이아웃과 큰 충돌 없이 같이 사용 할 수도 있다. 

ConstraintLayout 에서 뷰를 정의하려면 컴포넌트를 추가 하고 난 뒤 제약조건을 설정 해야 한다. 제약조건을 설정하지 않으면 [0, 0] 위치에 보이는데 컴파일 오류는 딱히 없긴 하지만 누락된 제약조건을 오류로 알린다. 

#### 1.1 ConstraintLayout 를 프로젝트에 추가 하기

1. 모듈의 `build.gradle` 파일에 `google()` 리포지터리를 추가 한다. (이미 추가되어 있으면 추가하지 않아도 된다)

```
repositories {
  google()
}
```

2. 위 1번의 `build.gradle` 파일에 라이브러리를 추가 한다. 

```
dependencies {
  implementation 'com.android.support.constraint:constraint-layout:x.x.x'
}
```

#### 1.2 ConstraintLayout 레이아웃의 생성 

기존 xml 레이아웃 추가 방식과 유사하다. 다만 root 레이아웃 에 `android.support.constraint.ConstraintLayout` 으로 설정 하면 된다. 만약 `ViewModel` 을 사용 하고 있다면 기존과 동일하게 root를 `layout` 으로 갖고 자식 항목에 `ConstraintLayout` 을 추가 하면 된다. 

```xml
<layout>
  <data>
    <variable name="vm" type="...ViewModel" />    
  </data>
  
  <android.support.constraint.ConstraintLayout>
    <!-- child views... -->
  </android.support.constraint.ConstraintLayout>
</layout>
```

### 2. 제약조건의 추가, 삭제 

Layout editor 를 이용 해 컴포넌트를 배치 한 뒤(컴포넌트에 따라 해당 리소스를 할당 해야 할 수도 있다) 해당 컴포넌트가 선택된 상태 에서 뷰 컴포넌트의 사방에 놓여진 핸들의 앵커 포인트를 다른 뷰, 부모 뷰 등의 가장자리 혹은 앵커 포인트, 가이드라인 과 같은 비쥬얼 헬퍼 등에 드래그 하여 제약조건을 추가 할 수 있다. 추가된 제약조건에는 연결된 상대 뷰와의 여백또한 지정할 수 있다. 

제약조건을 만들땐 다음의 규칙이 존재 한다. 

- 모든 뷰 에는 가로, 세로 하나씩 두개 이상의 제약조건이 필요 하다. 
- 같은평면을 공유하는 제약조건 핸들과 앵커 포인트 사이에만 제약조건을 만들 수 있다. 뷰의 vertical 영역은 다른 vertical 영역으로만 제한될 수 있으며 이 기준은 다른 기준으로만 제한 될 수 있다. 
- 각 제약조건 핸들은 하나의 제약조건에만 사용할 수 있지만 동일한 앵커 포인트에 다른 뷰에서 여러개 의 제약조건을 만들 수 있다. 

추가된 제약조건을 삭제 하려면 추가된 제약조건을 클릭하여 선택 한 뒤 Delete 키를 이용 하거나 Control 키 (맥 에서는 Command) 길게 누른 다음 추가된 제약조건 앵커를 클릭 하면 빨간색이 되는데 이 때 삭제할 수 있다. 


### 3. 제약조건의 종류 


