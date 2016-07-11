### Daily development learning  
#### 1. RecyclerView generic Adapter  
- 안드로이드의 UI를 개발 하다보면 비슷비슷한 형태를 가진 UI를 구현 하는 경우가 많다. 그 중에서도 자주 사용되는 것 이 있다면 목록을 표현하는 `RecyclerView`가 아닐까 싶다. 
- `RecyclerView`를 사용 하다보면 비슷한 코드를 `Adapter`나 `ViewHolder`에서 작성하는 것을 많이 경험 했다. 
- 반복되는 내용을 정리 하고 `RecyclerView`의 `Adapter`구현에 Generic한 형태를 만들어 이를 상속받아서 자주 사용되며 반복되는 코드를 줄이고 추후 유지, 보수에도 쓸만한 `Adapter`이다.  
```java
public abstract class SwRecyclerViewAdapter<T>
    extends RecyclerView.Adapter<SwRecyclerViewAdapter.ViewHolder> {
  protected List<T>             list;
  protected Context             context;
  private   OnViewClickListener clickListener;

  public SwRecyclerViewAdapter(@NonNull Context context, @NonNull List<T> list) {
    this(context, list, null);
  }

  public SwRecyclerViewAdapter(@NonNull Context context, @NonNull List<T> list, OnViewClickListener clickListener) {
    super();
    this.context = context;
    this.clickListener = clickListener;
    this.list = new ArrayList<>();
    setItem(list, false);
  }

  /**
   * viewType에 따라서 ViewHolder를만들고 bind할 View의 인스턴스를 반환 한다.
   */
  protected abstract View createView(Context context, ViewGroup viewGroup, int viewType);

  /**
   * createView()에서 생성한 View와 position의 Data를 기반으로 뷰를 업데이트 한다.
   */
  protected abstract void bindView(T item, ViewHolder viewHolder);

  public void clearItems() {
    if (list != null && !list.isEmpty()) {
      list.clear();
      notifyDataSetChanged();
    }
  }

  public void setItem(@NonNull List<T> list, boolean isClaearList) {
    if (this.list != null) {
      if (isClaearList) {
        clearItems();
      }
      this.list = list;
      notifyDataSetChanged();
    }
  }

  public void addItems(@NonNull List<T> addItems) {
    if (list != null) {
      final int startPos = list.size();
      list.addAll(addItems);
      notifyItemRangeInserted(startPos, addItems.size());
    }
  }

  public void addItem(@NonNull T addItem) {
    if (list != null) {
      list.add(addItem);
      notifyItemInserted(list.size());
    }
  }

  public T removeItem(int position) {
    if (list != null) {
      if (position >= 0 && position < list.size()) {
        T removedItem = list.remove(position);
        notifyItemRemoved(position);
        return removedItem;
      }
    }
    return null;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ViewHolder(createView(context, parent, viewType), clickListener);
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    bindView(getItem(position), holder);
  }

  @Override
  public int getItemCount() {
    return (list != null ? list.size() : 0);
  }

  public T getItem(@IntRange(from = 0) int position) {
    return ((list != null && position < list.size()) ? list.get(position) : null);
  }

  public static class ViewHolder
      extends RecyclerView.ViewHolder
      implements View.OnClickListener {
    private Map<Integer, View>  views;
    private OnViewClickListener clickListener;

    public ViewHolder(View view, OnViewClickListener clickListener) {
      super(view);
      this.clickListener = clickListener;
      if (this.clickListener != null) {
        view.setOnClickListener(this);
      }
      views = new HashMap<>();
      // insert RootView
      views.put(0, view);
    }

    public View getView(@IdRes int id) {
      if (!views.containsKey(id)) {
        initViewById(id);
      }
      return views.get(id);
    }

    public void initViewById(@IdRes int id) {
      // get RootView
      View view = (getView(0) != null ? getView(0).findViewById(id) : null);
      if (view != null) {
        if (view.isClickable()) {
          view.setOnClickListener(this);
        }
        views.put(id, view);
      }
    }

    @Override
    public void onClick(View v) {
      if (clickListener != null) {
        clickListener.onClick(v, getAdapterPosition());
      }
    }
  }// end of ViewHolder class
  
}
```
----

