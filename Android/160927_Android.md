# Generic RecyclerView Adapter - 3

## 1. 배경
기존에 만든 [RecyclerView Generic Adapter](https://github.com/ksu3101/TIL/blob/master/Android/160709_Android.md)에서 일부 변경점이 있어 업데이트를 하게 되었다. 현재 작업중인 모든 RecyclerView는 이 문서를 기반으로 작업 하고 있다.  
기존 틀과 거의 동일 하긴 하지만 `NestedRecyclerView`의 지원 이나 몇가지 이슈로 인하여 내부 수정된 내용이 있어 이를 공유 하기 위해서 글을 작성 하였다.    

## 2. 변경된 내용
- [구현된 소스 ](https://github.com/ksu3101/NestedRecyclerView/blob/master/app/src/main/java/kr/swkang/nestedrecyclerview/utils/rvs/SwRecyclerViewAdapter.java)
- 자세한 내용은 다음을 참고 할 것.   

### 2.1 NestedRecyclerView의 지원
 RecyclerView내부 에 RecyclerView이 있을 수 있다. 예를 들어 Vertical scroll만 하는 RecyclerView에 Horizontal scroll을 하는 RecyclerView가 존재 할 수 있는 것 이다. 이런 UI의 형태는 생각보다 많다. 간단한 예를 들면 Google Play 메인 화면 처럼 말이다.   

 Header, Item, Footer(User, Loadmore), PTR 등 여러가지의 UX구현을 위해서는 Multiple View Type을 지원하는 Adapter의 구현이 필수적이다. 

```java
public abstract class SwRecyclerViewAdapter<T>
    extends RecyclerView.Adapter<SwRecyclerViewAdapter.ViewHolder> {
  ...
  private   Object              tagObj;
  ...

  public SwRecyclerViewAdapter(@NonNull Context context, @NonNull List<T> list, Object tag, OnViewClickListener clickListener) {
    super();
    this.context = context;
    this.clickListener = clickListener;
    this.list = new ArrayList<>();
    setTag(tag);
    setItem(list, false);
  }

  public Object getTag() {
    return tagObj;
  }

  public void setTag(Object tagObj) {
    this.tagObj = tagObj;
  }

  ...
}
```
1. `Object`객체 변수는 구현되어진 `RecyclerView`와 내부 아이템으로 추가 된 `NestedRecyclerView`를 구분하기 위한 `TAG`객체 이다. 추후 구현 할 `OnViewClickListener`의 `onClicked()`메소드에서 이 태그를 활용하여 `NestedRecyclerView`의 RecyclerView를 구분 할 수 있다. 
2. Adapter에 태깅할 `TAG`객체의 setter, getter메소드가 추가 되었다.  

### 2.2 아이템 클릭 리스너의 구현 메소드 변경   
```java
public interface OnViewClickListener {
  void onClicked(@NonNull SwRecyclerViewAdapter.ViewHolder viewHolder, int position);
}
```
기존에는 `View`객체와 `position`를 콜백 받는 메소드 였지만 이제는 `ViewHolder`의 구현 객체를 직접적으로 받는다. 
이 인터페이스의 구현 예는 아래와 같다.  

```java
  @Override
  public void onClicked(@NonNull SwRecyclerViewAdapter.ViewHolder viewHolder, int position) {
    if (viewHolder.getTag() != null) {
      Object viewHolderTag = viewHolder.getTag();
      if (viewHolderTag instanceof String) {
        String tagStr = (String) viewHolderTag;
        if (tagStr.equals(MainRvAdapter.TAG)) {
          MainAdapterItemModel m = mainAdapter.getItem(position);
          ...
        }
        else if (tagStr.equals(SectionRvAdapter.TAG)) {
          SectionAdapterItemModel m = sectionAdapter.getItem(posotion);
          ...
        }
      }
    }
  }
```
만약 `NestedRecyclerView`의 RecyclerView이 아니라면 위 처럼 tag를 이용한 구분을 하지 않아도 된다. 하지만, `NestedRecyclerView`의 경우 `SwRecyclerViewAdapter`의 구현 클래스에서 TAG를 생성자 혹은 setter를 통해서 지정 한 뒤 `OnViewClickListener`의 구현을 통해서 아이템 클릭의 콜백을 재정의 해야 한다. 

1. ViewHolder 객체에서 `getTag()`메소드를 통해서 태깅된 태그 오브젝트를 얻고 내가 원하는 것 인지 인스턴스를 체크 한다. (null 체크, instanceof 체크)  
2. 내가 원하는 인스턴스라면 해당 `ViewHolder`에 해당하는 `Adapter`의 객체를 통해서 item을 `getItem(int position)`메소드를 통해서 모델 객체를 얻는다.   
참 쉽죠?  

## 3. 사용 예 
- [적절한 예시 Adapter의 구현 클래스](https://github.com/ksu3101/NestedRecyclerView/blob/master/app/src/main/java/kr/swkang/nestedrecyclerview/main/list/MainRvAdapter.java)



