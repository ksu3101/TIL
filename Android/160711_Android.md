### Daily development learning  
#### 1. [RecyclerView Generic Adapter](https://github.com/ksu3101/TIL/edit/master/Android/160709_Android.md) 적용해 보기  
- 기존에 만든 Generic data type을 지원하는 Adapter를 사용 하여 실제로 어떻게 구현 되어지는 지 알아보자. 
- 추가 할 Adapter의 클래스는 기존에 만든 `SwReccylerViewAdapter`를 상속한다. 그리고 class의 generic type에 내가 List를 통해서 구현할 데이터 타입을 명시 하자. 
- 구현해야 할 추상 메소드인 `createView()`와 `bindView()`를 구현 한다. 
 - `createView()` : `LayoutInflater`를 통해서 레이아웃을 인플레이팅 한 뒤 그 View객체를 반환 한다. 만약 `ViewType`에 의해서 다른 레이아웃을 제공 하게 되면 이곳에서 분기 하면 된다. 
 - `bindView()` : ViewHolder를 생성 할 것 없이 부모의 `ViewHolder`를 사용해서 레이아웃을 데이터에 따라서 갱신 한다. `SwRecyclerViewAdapter`의 `ViewHolder`의 메소드인 `getView()`에 view의 resource id를 전달 한다. 내부에서 root view의 `findViewById()`를 통해서 View를 얻고 map에 id를 기준으로 저장한다. 만약 뷰가 `isClickable()`이라면 `onClick`이벤트를 처리 할 수 있는 콜백으로 뷰의 정보와 아이템의 position정보를 콜백을 통해서 전달 할 것이다. 
- Adapter의 구현 예제
```java
public class RecentLegoItemListAdapter
    extends SwRecyclerViewAdapter<LegoModel> {
  private static final String TAG = RecentLegoItemListAdapter.class.getSimpleName();

  public RecentLegoItemListAdapter(Context context, ArrayList<LegoModel> list, OnViewClickListener clickListener) {
    super(context, list, clickListener);
  }

  @Override
  protected View createView(Context context, ViewGroup viewGroup, int viewType) {
    return LayoutInflater.from(context).inflate(R.layout.main_item_recently_lego, viewGroup, false);
  }

  @Override
  protected void bindView(LegoModel item, ViewHolder viewHolder) {
    if (item != null) {
      RelativeLayout container = (RelativeLayout) viewHolder.getView(R.id.main_recently_item_container);
      container.setClickable(true);

      ImageView ivLego = (ImageView) viewHolder.getView(R.id.main_recently_item_iv);
      TextView tvModelNum = (TextView) viewHolder.getView(R.id.main_recently_item_tv_modelnumber);

      final int cornerRadius = context.getResources().getDimensionPixelSize(R.dimen._3dp);
      final String imgLego = item.getImageUrl(false);
      ivLego.setBackgroundResource(R.drawable.shape_recent_def_bg);

      if (!TextUtils.isEmpty(imgLego)) {
        Picasso.with(context)
               .load(imgLego)
               .fit()
               .centerCrop()
               .transform(new RoundedCornersTransformation(
                   cornerRadius, 0,
                   RoundedCornersTransformation.CornerType.TOP))
               .error(R.drawable.image_not_found_tr)
               .into(ivLego, new Callback() {
                 @Override
                 public void onSuccess() {
                   ivLego.setBackgroundResource(R.color.transparent);
                 }

                 @Override
                 public void onError() {
                   ivLego.setBackgroundResource(R.drawable.shape_recent_def_bg);
                 }
               });
      }
      else {
        Picasso.with(context)
               .load(R.drawable.image_not_found_tr)
               .fit()
               .centerCrop()
               .error(R.drawable.image_not_found_tr)
               .into(ivLego);
      }
      tvModelNum.setText(String.valueOf(item.getModel_number()));
    }
  }
}
```
