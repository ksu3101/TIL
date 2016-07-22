### NestedRecyclerView - ViewPager infinite looping, with Indicators 

#### 1. Infinite loop ViewPager 
- [Source Link](https://github.com/ksu3101/NestedRecyclerView/tree/master/app/src/main/java/kr/swkang/nestedrecyclerview/utils/viewpagers)
- `NestedRecyclerView`의 예제를 한참 만들다가 `Header`에 inifite loop이 가능한 ViewPager을 설정 하였다. 
- `ViewPager`를 상속 하였으며 내부에서 position을 설정 하려 할때 실제 값(`Adapter`에서 `getCount()`하였을 때 가져올 실제 데이터 셋의 아이템 갯수)을 `%`나머지 연산자를 활용 하여 계산된 포지션을 가지고 내부 포지셔닝을 한다.  
- [InfinitePagerAdapter](https://github.com/ksu3101/NestedRecyclerView/blob/master/app/src/main/java/kr/swkang/nestedrecyclerview/utils/viewpagers/InfiniteViewPager.java)클래스는 `PagerAdapter`를 상속 받은 클래스인데 내부에 `PagerAdapter`의 구현체 인스턴스를 갖는 **래퍼 클래스** 이다. 
- 래퍼 클래스인 `InfinitePagerAdapter`에서는 내부 `PagerAdapter`를 위한 아이템 포지션을 계산 한다. 
```java
    @Override
    public int getCount() {
        if (getRealCount() == 0) {
            return 0;
        }
        // warning: scrolling to very high values (1,000,000+) results in
        // strange drawing behaviour
        return Integer.MAX_VALUE;
    }
```
- `getCount()`메소드에서 실제 카운터 값을 체크 하고 최대값인 `Integer.MAX_VALUE (2147483647)`를 설정 한다. 최대 스크롤 가능한 position을 설정 하는 것 이다. 
- 현제 `ViewPager`의 포지션을 알기 위해서는 `ViewPager`의 `position`과 `adapter`의 `getRealCount()`메소드를 이용하여 `%`나머지 연산을 통해서 계산된 포지션을 활용 하면 된다. 

### 2. ViewPager indicator  
- [Source Link](https://github.com/ksu3101/NestedRecyclerView/blob/master/app/src/main/java/kr/swkang/nestedrecyclerview/utils/viewpagers/pagerindicator/ViewPagerIndicator.java)
- [Jake Wharton의 ViewPagerIndicator](https://github.com/JakeWharton/ViewPagerIndicator)를 참고 하였다. 
- `ViewPager`의 현재 위치와 아이템 갯수를 카운팅 해서 보여주는 UI패턴 중 하나인 `ViewPager Indicator`를 사용 한다. indicator의 구현 자체는 어렵지 않다. 
- `ViewPager Indicator`의 구현은, `LinearLayout`을 상속한 뷰 그룹에 이미지뷰에 drawable을 세팅 후 추가적으로 layout parameter등을 세팅 한 뒤에 `addView()`한다. 몰론 `ViewPager`의 데이터 셋 전체 사이즈와 현재 위치 포지션을 알아야 할 것이다.
- 하지만, 위 1번의 `Infinite loop ViewPager`와 같이 사용하기 위해선 오류가 존재 한다. `ViewPager`에서 반환하는 포지션이 실제로 가진 데이터셋 보다 큰(최대 `Integer.MAX_VALUE`) 것이다. 당연히 오류가 발생 할 수밖에 없다. 
- 그래서 indicator의 내부에서 `ViewPager`의 페이지 가 변경될 때 마다 불리는 리스너인 `ViewPager.OnPageChangeListener`을 구현 한다. 
```java
  @Override
  public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    this.currentPosition = position;
  }

  @Override
  public void onPageSelected(int position) {
    this.currentPosition = position;
    for (int i = 0; i < indicators.size(); i++) {
      indicators.get(i).setImageResource(i == (currentPosition % size) ? selectedItemDrawable : normalItemDrawable);
    }
  }
```
- `onPageSelected()`메소드를 보면 for반복문 에서 현재 포지션(`currentPosition`)과 데이터셋의 실제 전체 사이즈(`size`)를 나누고 그 나머지를 통해서 포지션을 체크 함을 확인 할 수 있다. 


