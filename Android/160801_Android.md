# NestedRecyclerView - Scroll state에 따른 Picasso의 resume / pause

## [SwOnScrollListener](https://github.com/ksu3101/NestedRecyclerView/blob/master/app/src/main/java/kr/swkang/nestedrecyclerview/utils/rvs/SwOnScrollListener.java) 
`RecyclerView`의 스크롤 상태에 따른 콜백을 구현 할 수 있는 추상 클래스인 `OnScrollListener`을 상속 받아 만든 클래스로서 내부에서 `onScrollStateChanged()`을 구현 하여 스크롤의 상태에 따라서 `Picasso`의 resume, pause를 제어 한다.

`Picasso`에서 resume, pause를 하기 위해서는 tag로 사용될 Object가 필요 하다. [여기](https://nullpointer.wtf/android/image-loading-with-picasso/)와 같이 내부적으로 `final Object`를 하나 만들어서 tag로 사용 해도 좋다. 하지만 `SwOnScrollListener`에서는 전달받은 `Context`객체를 사용 하였다.

`RecyclerView`의 스크롤 상태가 `SCROLL_STATE_IDLE`일 때 `Picasso`에 어떠한 태그로 등록된 큐들을 모두 resume시킨다.

`RecyclerView`의 스크롤 상태가 `SCROLL_STATE_SETTLING`일 때 `RecyclerView`에 `postDelayed()`를 하게 되는데, 세틀링 되는 약 0.5sec의 시간의 딜레이 후 `Picasso`에 어떠한 태그로 등록된 큐들을 모두 resume시킨다.

`RecyclerView`의 스크롤 상태가 그 외 모든 경우(스크롤 되고 있을 때) `Picasso`에 어떠한 태그로 등록된 큐 들을 모두 pause시킨다.

```java
public abstract class SwOnScrollListener
    extends RecyclerView.OnScrollListener {
  private Picasso  picasso;
  private Context  context;
  private Runnable settlingResumeRunnable;

  public SwOnScrollListener(@NonNull Context context) {
    this.context = context;
    this.picasso = Picasso.with(context);
    this.settlingResumeRunnable = null;
  }

  @Override
  public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
    if (this.picasso != null) {
      if (newState == RecyclerView.SCROLL_STATE_IDLE) {
        if (settlingResumeRunnable != null) recyclerView.removeCallbacks(settlingResumeRunnable);
        picasso.resumeTag(context);
      }
      else if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
        settlingResumeRunnable = new Runnable() {
          @Override
          public void run() {
            picasso.resumeTag(context);
          }
        };
        recyclerView.postDelayed(settlingResumeRunnable, 500);
      }
      else {
        picasso.pauseTag(context);
      }
    }
  }
}
```

액티비티에서의 구현은 다음과 같다.

```java
rv.addOnScrollListener(
    new SwOnScrollListener(this) {
      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        // do something. ex) LOAD MORE NEXT PAGE. 
      }
    }
);
```


