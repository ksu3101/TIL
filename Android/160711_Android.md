### Daily development learning  
#### 1. [RecyclerView Generic Adapter](https://github.com/ksu3101/TIL/edit/master/Android/160709_Android.md) 적용해 보기  
- 기존에 만든 Generic data type을 지원하는 Adapter를 사용 하여 실제로 어떻게 구현 되어지는 지 알아보자. 
- Adapter의 구현 
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
 - LegoLibrary프로젝트에 사용된 Adapter. 
 - 
