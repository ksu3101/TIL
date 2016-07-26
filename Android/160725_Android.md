## NestedRecyclerView - RecyclerView Item Decoration 

### 1. [Horizontal RecyclerView Item Decoration](https://github.com/ksu3101/NestedRecyclerView/blob/master/app/src/main/java/kr/swkang/nestedrecyclerview/main/list/SubHorRvItemDecoration.java)
- Horizontal RecyclerView의 Item view의 왼쪽, 오른쪽에 공간(Space)를 추가 한다. 이는 각 아이템 간 간격을 추가 하기 위해서 이다. 
- 하지만 맨 왼쪽(`0`)과 맨 오른쪽 (`itemCount() - 1`)은 각 왼쪽과 오른쪽에 공간을 추가 하면 안 된다. 예를 들면 다음과 같다.   

|   | ITEM 0 | 6dp | ITEM 1 | 6dp | ITEM ... | 6dp | ITEM n-1 |   |
|---|--------|-----|--------|-----|----------|-----|----------|---|
- `ITEM 0`부터 `ITEM n-1`까지 보여 주게 되는데 중간에 `6dp`사이즈의 공간이 있는 것을 알 수 있다. 이 공간을 위해서 각각 아이템뷰들이 `3dp`씩 left, right를 설정 하는 것 이다. 
```java
public class SubHorRvItemDecoration
    extends RecyclerView.ItemDecoration {
  private int gapSize;

  public SubHorRvItemDecoration(@NonNull Context context) {
    this.gapSize = context.getResources().getDimensionPixelSize(R.dimen.subrv_hor_gap_size);
  }

  @Override
  public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
    int position = parent.getChildAdapterPosition(view);

    outRect.left = (position > 0 ? gapSize / 2 : 0);
    outRect.right = (position < parent.getAdapter().getItemCount() - 1 ? gapSize / 2 : 0);
  }
}
```

---
### 2. [Multiple ViewType Grid RecyclerView Item Decoration](https://github.com/ksu3101/NestedRecyclerView/blob/master/app/src/main/java/kr/swkang/nestedrecyclerview/main/list/MainRvItemDecoration.java)
- 크게 나누어 `Header`와 `BODY_FULL`, `BODY_HALF`, `SECTION_HEADER`, `FOOTER`로 나뉘어 진 Multiple ViewType RecyclerView에서 각 `ViewType`에 따라 분기 하여 아이템 간 공간을 계산하고 적용 하는 Item Decoration 이다. 
- 구현 될 RecyclerView는 `GridLayoutManager`를 사용 하며, 이 레이아웃 매니저의 `setSpanSizeLookup()`의 구현을 통해서 각 `ViewType`에 따라 `Span`사이즈를 설정 한다. 기본적으로 Column수가 2개 이므로 Span사이즈가 2 이면 Full width를 사용 하며, Span 사이즈가 1 이면 Half width를 사용 하게 된다. 
- 각 ViewType에 대한 정보와 보여지는 순서는 다음과 같다.  
 - `HEADER` : 내부에 ViewPager를 보유. Span 2. 
 - `BODY_FULL` : 내부에 Horizontal RecyclerView를 보유. Span 2. RecyclerView에 Padding left, right가 설정 되어 있으며, clip to padding 설정이 `false`로 설정 되어 있다. 
 - `BODY_HALF` : CardView. Span 1. 
 - `SECTION_HEADER` : TextView. Span 2. (섹션 헤더는 어디에서든 등장 할 수 있다)
 - `FOOTER` : Span 2. 
- Footer이전에 Half body의 하단 간격도 잘 설정 되어야 한다. 
```java
public class MainRvItemDecoration
    extends RecyclerView.ItemDecoration {
  private int defaultLeftMargin;
  private int defaultRightMargin;
  private int defaultTopMargin;
  private int defaultBottomMargin;

  public MainRvItemDecoration(@NonNull Context context) {
    this.defaultLeftMargin = context.getResources().getDimensionPixelSize(R.dimen.rv_def_left_margin);
    this.defaultRightMargin = context.getResources().getDimensionPixelSize(R.dimen.rv_def_right_margin);
    this.defaultTopMargin = context.getResources().getDimensionPixelSize(R.dimen.rv_def_top_margin);
    this.defaultBottomMargin = context.getResources().getDimensionPixelSize(R.dimen.rv_def_bottom_margin);
  }

  @Override
  public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
    int position = parent.getChildAdapterPosition(view);
    int viewType = parent.getAdapter().getItemViewType(position);

    if (viewType == HeaderContents.VIEWTYPE_VALUE) {
      // 0 == Header contents
    }
    else if (viewType == BodySection.FULL_VIEWTYPE_VALUE) {
      // BODY contents (Span 2)
      // top, bottom margin (paddin on inside Horizontal-RecyclerView)
      outRect.top = defaultTopMargin;
      outRect.bottom = defaultBottomMargin;
    }
    else if (viewType == BodySection.HALF_VIEWTYPE_VALUE) {
      // BODY contents (Span 1)
      // top, bottom margin
      outRect.top = (defaultTopMargin / 2);
      outRect.bottom = (defaultBottomMargin / 2);

      RecyclerView.Adapter a = parent.getAdapter();
      if (a instanceof MainRvAdapter) {
        MainRvAdapter adapter = (MainRvAdapter) a;
        // if it last item
        int count = adapter.getItemCount();
        if (adapter.isFooter(count - 1)) {
          // has Footer
          count = count - 2;
        }
        if (position > count - 2) {
          outRect.bottom = defaultBottomMargin;
        }

        final int firstHalfBodyPosition = adapter.getFirstHalfBodyContentsPosition();
        if (firstHalfBodyPosition <= position) {
          if (position % 2 == 0) {
            // on Left item (left margin, and half right margin)
            outRect.left = defaultLeftMargin;
            outRect.right = (defaultRightMargin / 2);
          }
          else {
            // on Right item (half left margin, and right margin)
            outRect.left = (defaultLeftMargin / 2);
            outRect.right = defaultRightMargin;
          }
        }
      }
    }
    else if (viewType == MainRvAdapter.FOOTER_LOADMORE) {
      // list.size() + 1 == Footer contents
    }
    else {
      // Section Headers
      // left, right, top, bottom margin
      outRect.left = defaultLeftMargin;
      outRect.right = defaultRightMargin;
      outRect.top = defaultTopMargin;
      outRect.bottom = defaultBottomMargin;
    }
  }
}
```

