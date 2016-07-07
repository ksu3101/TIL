### ViewPager Issue  
#### 1. Picasso   
- 배경   
a. `FragmentStatePagerAdapter`를 상속한 adapter를 사용하는 `ViewPager`를 통해 페이징 화는 화면들이 존재 하는 환경.    
b. `Fragment`의 내부에서는 라이브러리 [Picasso](http://square.github.io/picasso/)를 이용 해서 비동기로 이미지를 여러개 불러 오게 설정 함.     
c. `Picasso`에서는 `Target`인터페이스를 익명 클래스로 구현하고 callback 메소드를 통해서 `Bitmap`을 전달 받은 뒤 후 처리등을 따로 하였음. (파일의 캐시 저장, rounded rectangle 등의 이미지 수정 등)  

- 발생한 이슈 설명   
a. `ViewPager`의 페이징을 할때마다 새로 페이지를 불러오면서 `Picasso`를 통해서 이미지를 최소 0개에서 최대 21개 (프로필 사진같은 조그만 썸네일을 포함하면 더 많을 수도 있음)까지 비동기로 불러 오게 됨.  
b. `ViewPager`에서는 기본적으로 current index의 page에서 다음 불러올 페이지를 예측(index-n, index+n) 하여 다음 페이지를 미리 pre-loading하게 됨.(이는 `ViewPager`의 `setOffscreenPageLimit()`메소드를 통해서 설정 할 수 있으므로 바뀔 수 있음)  
c. pre-load된 이미지는 `Picasso`내에서 **디스크 캐시**만 사용하도록 설정을 변경 함.  
d. **문제는, 페이징을 한참 하다보면 destroy되어 하단 메소드를 통해서 gc 되어야 할 이미지 객체가 gc되지 않아 메모리를 계속 차지 하고 있는 문제 였음.**  
e. 그래서 한참 페이징을 하다 보면 메모리에 `Bitmap`이미지 가 제거되지 않고 계속 남아있어 결국 `OutOfMemory`예외가 발생하는 크리티컬한 이슈 엿음.   
f. 찾아본 바로는 `Picasso`뿐만 아니라 `AIUL`에서도 비슷한 오류가 있다고 함. (확실하지는 않음)
g. 안드로이드에서는 비트맵의 실제 데이터를 `Native heap`에 저장 하는데 이 데이터들에 대한 `reference`가 계속 유지되면서 앱에 할당된 heap을 gc하지 않고 계속 채우면서 발생한 버그로 예상된다.  
h. 그래서 `Context`를 받아서 처리 하는 `Picasso`를 사용하지 않고 비슷하게 `Builder`패턴과 재사용 가능 한 `Transformer`등을 적용 하여 비동기로 이미지를 웹에서 로드 하고 후처리 할 수 있게 [비동기 이미지 로더](https://github.com/ksu3101/TIL/blob/master/Android/java/160707_AsyncImageLoader.java)를 만들고 적용. 

- 문제 해결 방법  
a. `Fragment`에서 사용하고 있는 `Picasso`를 빼고 직접 만든 [비동기 이미지 로더](https://github.com/ksu3101/TIL/blob/master/Android/java/160707_AsyncImageLoader.java)를 사용 함.  
b. `Fragment`에서 `onDestroy()`메소드가 호출될 때 하단의 `unbindDrawables()`메소드를 호출 하여 view에 설정된 모든 drawable을 해지 하고 gc할 수 있도록 해 준다.   
```java
  public static void unbindDrawables(@NonNull View view, boolean skipGarbageCollection) {
    if (view.getBackground() != null) {
      view.getBackground()
          .setCallback(null);
    }
    if (view instanceof ViewGroup) {
      for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
        unbindDrawables(((ViewGroup) view).getChildAt(i), true);
      }
      if (!(view instanceof AbsSpinner) && !(view instanceof AbsListView)) {
        ((ViewGroup) view).removeAllViews();
      }
    }

    if (!skipGarbageCollection) System.gc();
  }
```

