## NestedRecyclerView - Empty View

## [SwRecyclerView](https://github.com/ksu3101/NestedRecyclerView/blob/master/app/src/main/java/kr/swkang/nestedrecyclerview/utils/SwRecyclerView.java)
`RecyclerView`의 데이터들이 비어 있을 경우 보여지게 되는 Empty View를 보여지게 하거나 감출 수 있게 토글 처리 한다.

기본적인 쉬운 방법으로는 `RecyclerView`보다 크거나 덮는 레이아웃을 추가 하고 `Adapter`의 데이터존재 유무에 따라서 레이아웃의 Visibility를 `VISIBLE`, `GONE`처리 하는 방법 이 있다. 하지만 이 방법은 재사용이 불가능 하며 비슷한 코드가 반복 됨을 알 수 있다. 

`RecyclerView`를 상속한 [SwRecyclerView](https://github.com/ksu3101/NestedRecyclerView/blob/master/app/src/main/java/kr/swkang/nestedrecyclerview/utils/SwRecyclerView.java)를 보면 내부에 `AdapterDataObserver`를 두고 데이터 셋의 변경 여부를 콜백(`onChanged()`) 받아서 데이터존재 여부 체크 한 뒤에 empty view를 visible처리 한다. 
- [이 페이지를 참고 하였음.](http://stackoverflow.com/questions/28217436/how-to-show-an-empty-view-with-a-recyclerview)

```java
public class SwRecyclerView
    extends RecyclerView {
  private View emptyView;
  private AdapterDataObserver observer = new AdapterDataObserver() {
    @Override
    public void onChanged() {
      Adapter<?> adapter = getAdapter();
      if (adapter != null && emptyView != null) {
        if (adapter.getItemCount() == 0) {
          emptyView.setVisibility(View.VISIBLE);
          SwRecyclerView.this.setVisibility(View.GONE);
        }
        else {
          emptyView.setVisibility(View.GONE);
          SwRecyclerView.this.setVisibility(View.VISIBLE);
        }
      }
    }
  };

  public SwRecyclerView(Context context) {
    super(context);
  }

  public SwRecyclerView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public SwRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  public void setAdapter(Adapter adapter) {
    super.setAdapter(adapter);
    adapter.registerAdapterDataObserver(observer);
    observer.onChanged();
  }

  public void setEmptyView(@NonNull View view) {
    this.emptyView = view;
    if (observer != null) {
      observer.onChanged();
    }
  }
}
```
