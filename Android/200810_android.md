
안드로이드에서 많이 볼 수 있는 UI 디자인의 패턴 중 스크롤의 위치 에 따라 상단 ToolBar와 다른 뷰들에 대해 처리하는 방법을 정리 하였다. 기존에는 `ScrollView`에 listener를 추가 하여 콜백으로 전달 받는 스크롤 위치에 대해서 후처리를 하여 다른 뷰를 갱신하였지만 android x에서 제공 하는 방법은 더 접근하기 쉬운 방법으로 스크롤이벤트에 따른 뷰의 변화를 처리 해 줄 수있다. 

## 0. 예제 앱 

간단한 뷰에 대한 처리만 있으며 데이터를 따로 핸들링 할 필요가 없기 때문에 MVVM이나 다른 아키텍쳐는 적용하지 않고 `app`모듈 하나만 두었다. 그렇기 때문에 따로 ViewModel이나 다른 모듈등은 없다. 

예제앱의 동작 은 아래 gif를 참고 하도록 하자

![ex](./images/coodilayout_exp.gif)

### 0.1 Navigation graph

One Activity를 기준으로 1개의 예제 프래그먼트를 하나 두었으며 이들을 Navigation Graph 에서 Home으로 설정 하였다. 이 화면 외에 다른 화면은 없기 때문에 따로 정해진 Direction Action이나 Safe args는 없다. 

```xml
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto" android:id="@+id/nav_main"
    app:startDestination="@id/mainFragment">
    <fragment
        android:id="@+id/mainFragment"
        android:name="com.swkang.exmotioneditor.MainFragment"
        android:label="MainFragment" />
</navigation>
```

## 1. CoodinatorLayout

`CoodinatorLayout`을 사용하기에 앞서, app 모듈의 `build.gradle`파일에 아래와 같이 라이브러리 의존을 추가 해 준다. 

```
dependencies {
    implementation "androidx.coordinatorlayout:coordinatorlayout:1.1.0"
}
```

그리고, Activity의 레이아웃 xml파일에 `CoodinatorLayout`을 최상위 부모 레이아웃으로 변경 혹은 추가 해 준다. `CoodinatorLayout`은 앱 레이아웃에 최상위로 존재하는 decor view의 개념으로서, 자식뷰들간의 특정 인터렉션을 지원하는 컨테이너이다. 

```xml
<androidx.coordinatorlayout.widget.CoordinatorLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/main_parent_container"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fitsSystemWindows="true">

</androidx.coordinatorlayout.widget.CoordinatorLayout>    
```

### 1.1 CoodinatorLayout.Behavior

`CoodinatorLayout`의 하위뷰에 지정할 수 있는 Behavior등을 통해 여러 인터렉션등을 설정할 수 있다. 예를 들면 `DrawerLayout`의 제스쳐 애니메이션이나 상단 툴바의 collapse/expand 애니메이션등 이다. 

제공되는 Behavior는 아래와 같다. 

- [BottomSheetBehavior](https://www.youtube.com/watch?v=WeaylHAwIIk) : Bottom에서 Top으로 제스쳐 액션으로 올라오는 View
- [FloatingActionButton.Behavior](https://material.io/components/buttons-floating-action-button#behavior)
- [SwipeDismissBehavior](https://www.androhub.com/swipe-dismiss-cardview-with-coordinator-layout/) : 특정 뷰 를  스와이프 하는 액션의 애니메이션. 보통 swipe했을 때 뷰를 제거 하는 액션을 대상으로 적용 한다. 
- AppBarLayout.ScrollingViewBehavior : 이 문서에서 `NestedScrollView`에 적용된 Behavior. 
- AppBarLayout.Behavior : `AppBarLayout`의 자식들에 대한 Behavior.

Behavior을 xml에서 지정할 때 에는 `String`타입으로 지정할 `Behavior`의 패키지 + 클래스명을 적으면 된다.

## 2. AppBarLayout

`AppBarLayout`을 `CoordinatorLayout`의 자식으로 추가 한다.

```xml
<androidx.coordinatorlayout.widget.CoordinatorLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/main_parent_container"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fitsSystemWindows="true">
    <com.google.android.material.appbar.AppBarLayout
        android:id="@+id/main_appbarlayout"
        android:layout_width="match_parent"
        android:layout_height="256dp"
        android:fitsSystemWindows="true"
        android:theme="@style/ThemeOverlay.AppCompat.Dark.ActionBar">
    </com.google.android.material.appbar.AppBarLayout>        
</androidx.coordinatorlayout.widget.CoordinatorLayout>    
```

### 2.1 style.xml

앱의 `style.xml`에서 엡에 적용될 테마를 변경 해 주어야 한다. 기본적으로 안드로이드에서 제공되는 `ActionBar`테마가 아닌 `NoActionBar`계열의 테마를 적용 하면 된다. 

```xml
<style name="AppTheme" parent="Theme.AppCompat.DayNight.NoActionBar">
    <!-- ... -->
</style>    
```

만약 `NoActionBar`계열의 테마를 지정하지 않았다면 실행시 `IllegalStateException`예외가 발생하고 앱이 종료된다. 로그캣 내용을 보면 아래와 같다. 

```
java.lang.IllegalStateException: This Activity already has an action bar supplied by the window decor. Do not request Window.FEATURE_SUPPORT_ACTION_BAR and set windowActionBar to false in your theme to use a Toolbar instead.
```

이미 Activity에 `ActionBar`가 있으므로 테마에서 NoActionBar를 적용 하자. 

## 3. CollapsingToolbarLayout

`CollapsingToolbarLayout`을 `AppbarLayout`의 자식으로 추가 한다. 좀 의아한게 있다면 `CollapsingToolbarLayout`을 처음 보았을때 이 레이아웃이 `AppbarLayout`을 대체하여 사용되는 것 으로 생각 되었다. 하지만 `CollapsingToolbarLayout`만을 사용 해 보면 정상적으로 동작하지 않는다. 

그 이유는 `CollapsingToolbarLayout`는 부모인 `AppbarLayout`의 `offsetChangeListener`콜백을 기반으로 동작하고 있기 때문이다. `CollapsingToolbarLayout`의 `onAttachedToWindow()`메소드 내부 구현을 보면 parent view를 가져와 `AppbarLayout`으로 캐스팅 하여 `addOnOffsetChangedListener()`메소드를 통해 콜백을 추가함을 확인 할 수 있다. 

```xml
<androidx.coordinatorlayout.widget.CoordinatorLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/main_parent_container"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fitsSystemWindows="true">
    <com.google.android.material.appbar.AppBarLayout
        android:id="@+id/main_appbarlayout"
        android:layout_width="match_parent"
        android:layout_height="256dp"
        android:fitsSystemWindows="true"
        android:theme="@style/ThemeOverlay.AppCompat.Dark.ActionBar">
        <com.google.android.material.appbar.CollapsingToolbarLayout
            android:id="@+id/main_collapsingtoolbarlayout"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:fitsSystemWindows="true"
            app:contentScrim="?attr/colorPrimary"
            app:expandedTitleMarginEnd="64dp"
            app:expandedTitleMarginStart="48dp"
            app:layout_scrollFlags="scroll|exitUntilCollapsed">
        </com.google.android.material.appbar.CollapsingToolbarLayout>
    </com.google.android.material.appbar.AppBarLayout>        
</androidx.coordinatorlayout.widget.CoordinatorLayout>    
```

## 4. Toolbar (+ImageView)

`Toolbar`를 `CollapsingToolbarLayout`의 자식으로 추가 한다. 맨 위 상단 예제 gif이미지 처럼 이미지뷰를 설정해 보여주고 싶다면 `ImageView`를 함께 추가 한다. 

```xml
<androidx.coordinatorlayout.widget.CoordinatorLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/main_parent_container"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fitsSystemWindows="true">
    <com.google.android.material.appbar.AppBarLayout
        android:id="@+id/main_appbarlayout"
        android:layout_width="match_parent"
        android:layout_height="256dp"
        android:fitsSystemWindows="true"
        android:theme="@style/ThemeOverlay.AppCompat.Dark.ActionBar">
        <com.google.android.material.appbar.CollapsingToolbarLayout
            android:id="@+id/main_collapsingtoolbarlayout"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:fitsSystemWindows="true"
            app:contentScrim="?attr/colorPrimary"
            app:expandedTitleMarginEnd="64dp"
            app:expandedTitleMarginStart="48dp"
            app:layout_scrollFlags="scroll|exitUntilCollapsed">
            <ImageView
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:fitsSystemWindows="true"
                android:scaleType="centerCrop"
                android:src="@drawable/top_bg"
                app:layout_collapseMode="parallax" />
            <androidx.appcompat.widget.Toolbar
                android:id="@+id/main_toolbar"
                android:layout_width="match_parent"
                android:layout_height="?attr/actionBarSize"
                app:layout_collapseMode="pin"
                app:popupTheme="@style/ThemeOverlay.AppCompat.Light"
                app:title="@string/app_name"
                app:titleTextColor="@color/white" />
        </com.google.android.material.appbar.CollapsingToolbarLayout>
    </com.google.android.material.appbar.AppBarLayout>        
</androidx.coordinatorlayout.widget.CoordinatorLayout>    
```

### 4.1 MainActivity

`AppCompatActivity`를 상속한 `MainActivity`에서는 Toolbar에 대한 뷰의 인스턴스를 얻고 `setSupportActionBar()`메소드를 이용해 사용할 AppBar와 Toolbar를 설정 해 주어야 한다. 

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val toolbar: Toolbar = findViewById(R.id.main_toolbar)
        setSupportActionBar(toolbar)
    }
}
```

### 4.2 `app:toolbarId` 설정 

`CollapsingToolbarLayout`에서는 자식으로 추가될 `Toolbar`의 id를 `app:toolbarId`프로퍼티로 설정하여 약간의 퍼포먼스를 향상시킬 수 있다. 

`CollapsingToolbarLayout`내부에서는 자식으로 추가될 Toolbar를 찾아야 하는데 이 때 id가 없으면 자식뷰들을 일일히 이터레이션 하여 Toolbar를 찾는다. 하지만 위 프로퍼티를 이용 하여 적용 하면 `findViewById()`를 통해서 바로 찾으므로 필요없는 이터레이션을 하지 않는다. 

## 5. NestedScrollView with Contents

`CoordinatorLayout`에 추가될 컨텐츠의 영역을 추가 한다. 대부분의 뷰 컨테이너를 사용 할 수 있다. 하지만 위 예제 gif처럼 스크롤 되는 영역에 따라 `CollapsingToolbarLayout`과 함께 패럴랙스 스크롤 효과와 함께 사용 하려면 `NestedScrollView`를 추가 해 준다. 일반 ScrollView를 설정 하면 동작하지 않는다. 

추가된 `NestedScrollView`혹은 해당 뷰 에 `app:layout_behavior="@string/appbar_scrolling_view_behavior"`프로퍼티를 추가 하여 `CoordinatorLayout`와 함께 동작하게 될 뷰임을 알려준다. 

```xml
<androidx.coordinatorlayout.widget.CoordinatorLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/main_parent_container"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fitsSystemWindows="true">
    <com.google.android.material.appbar.AppBarLayout
        android:id="@+id/main_appbarlayout"
        android:layout_width="match_parent"
        android:layout_height="256dp"
        android:fitsSystemWindows="true"
        android:theme="@style/ThemeOverlay.AppCompat.Dark.ActionBar">
        <com.google.android.material.appbar.CollapsingToolbarLayout
            android:id="@+id/main_collapsingtoolbarlayout"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:fitsSystemWindows="true"
            app:contentScrim="?attr/colorPrimary"
            app:expandedTitleMarginEnd="64dp"
            app:expandedTitleMarginStart="48dp"
            app:layout_scrollFlags="scroll|exitUntilCollapsed">
            <ImageView
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:fitsSystemWindows="true"
                android:scaleType="centerCrop"
                android:src="@drawable/top_bg"
                app:layout_collapseMode="parallax" />
            <androidx.appcompat.widget.Toolbar
                android:id="@+id/main_toolbar"
                android:layout_width="match_parent"
                android:layout_height="?attr/actionBarSize"
                app:layout_collapseMode="pin"
                app:popupTheme="@style/ThemeOverlay.AppCompat.Light"
                app:title="@string/app_name"
                app:titleTextColor="@color/white" />
        </com.google.android.material.appbar.CollapsingToolbarLayout>
    </com.google.android.material.appbar.AppBarLayout>    
    
    <androidx.core.widget.NestedScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:clipToPadding="false"
        android:fitsSystemWindows="true"
        android:paddingTop="20dp"
        android:scrollbars="vertical"
        app:layout_behavior="@string/appbar_scrolling_view_behavior">
        <fragment
            android:id="@+id/fragmentContainer"
            android:name="androidx.navigation.fragment.NavHostFragment"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            app:defaultNavHost="true"
            app:navGraph="@navigation/nav_main" />
    </androidx.core.widget.NestedScrollView>
</androidx.coordinatorlayout.widget.CoordinatorLayout>    
```

## 6. FloatingActionButton

필요한 경우 Floating action button을 추가 해줄 경우도 있다. `app:layout_anchor`프로퍼티와 연계된 프로퍼티들의 속성 그리고 margin설정 값을 참고 하면 된다. 

```xml
<androidx.coordinatorlayout.widget.CoordinatorLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/main_parent_container"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fitsSystemWindows="true">
    <com.google.android.material.appbar.AppBarLayout
        android:id="@+id/main_appbarlayout"
        android:layout_width="match_parent"
        android:layout_height="256dp"
        android:fitsSystemWindows="true"
        android:theme="@style/ThemeOverlay.AppCompat.Dark.ActionBar">
        <com.google.android.material.appbar.CollapsingToolbarLayout
            android:id="@+id/main_collapsingtoolbarlayout"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:fitsSystemWindows="true"
            app:contentScrim="?attr/colorPrimary"
            app:expandedTitleMarginEnd="64dp"
            app:expandedTitleMarginStart="48dp"
            app:layout_scrollFlags="scroll|exitUntilCollapsed">
            <ImageView
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:fitsSystemWindows="true"
                android:scaleType="centerCrop"
                android:src="@drawable/top_bg"
                app:layout_collapseMode="parallax" />
            <androidx.appcompat.widget.Toolbar
                android:id="@+id/main_toolbar"
                android:layout_width="match_parent"
                android:layout_height="?attr/actionBarSize"
                app:layout_collapseMode="pin"
                app:popupTheme="@style/ThemeOverlay.AppCompat.Light"
                app:title="@string/app_name"
                app:titleTextColor="@color/white" />
        </com.google.android.material.appbar.CollapsingToolbarLayout>
    </com.google.android.material.appbar.AppBarLayout>    
    
    <androidx.core.widget.NestedScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:clipToPadding="false"
        android:fitsSystemWindows="true"
        android:paddingTop="20dp"
        android:scrollbars="vertical"
        app:layout_behavior="@string/appbar_scrolling_view_behavior">
        <fragment
            android:id="@+id/fragmentContainer"
            android:name="androidx.navigation.fragment.NavHostFragment"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            app:defaultNavHost="true"
            app:navGraph="@navigation/nav_main" />
    </androidx.core.widget.NestedScrollView>

    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginEnd="32dp"
        android:layout_marginRight="32dp"
        android:clickable="true"
        android:focusable="true"
        android:src="@android:drawable/ic_media_play"
        app:layout_anchor="@id/main_appbarlayout"
        app:layout_anchorGravity="bottom|right|end" />
</androidx.coordinatorlayout.widget.CoordinatorLayout>    
```

## 7. 더 복잡한 레이아웃 구성 

`DrawerLayout`이나 `NavigationView`, `ViewPager`등과 같이 사용 하는 경우 뷰의 구성은 아래처럼 구성 할 수 있다. 참고로 아래 의 코드들은 보기쉽게 긴 패키지와 내부 프로퍼티를 간소화하였다. 필요에 따라 추가적으로 프로퍼티를 사용 하면 될 것으로 생각 한다. 

### 7.1 DrawerLayout을 갖는 Activity 의 xml 

```xml
<DrawerLayout >
    <include layout=".../viewpager_container" />
    <NavigationView />
</DrawerLayout>
```

액티비티에서는 `DrawerLayout`를 배치 하고 실제 컨텐츠가 자리잡을 `CoordinatorLayout`와 하위 뷰들이 존재 하는 레이아웃의 xml을 `include`해서 적용 한다. 

### 7.2 `include` 대상 viewpager_container.xml

```xml
<CoordinatorLayout>
    <AppBarLayout>
        <Toolbar />
        <TabLayout />
    </AppBarLayout>
    <ViewPager />
    <FloatingActionButton />
</CoordinatorLayout >
```

레이아웃의 구성을 보면 `CollapsingToolbarLayout`이 아닌 `AppBarLayout`만 사용 되었을뿐 본문의 예제와 레이아웃의 구성은 같음을 확인 할 수 있다. 
