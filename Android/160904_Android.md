# [CarouselViewPager](https://github.com/ksu3101/CarouselViewPager)

## 1. 설명
![sample image](https://github.com/ksu3101/TIL/blob/master/Android/images/carousel_s.gif)
- 자주 사용되는 UI형태인 Carousel패턴을 `ViewPager`와 `Transformer`의 재정의를 통해서 구현한 예. 
- 스와이프 액션 시 어떠한 값에 따라서 내부 레이아웃의 상대적 위치나 구성을 변경 한다. 상황에 따라서 parallax효과를 주거나 translate, alpha를 수정한 효과들이 주로 사용 된다. 

## 2. 구현
- [ViewPagerTransformer](https://github.com/ksu3101/CarouselViewPager/blob/master/app/src/main/java/kr/swkang/carouselviewpager/utils/ViewPagerTransformer.java)에서는 `ViewPager`의 `PageTransformer`인터페이스를 구현한다. 
- `PageTransformer`를 구현한 클래스 에서는 `transformPage()`메소드의 내부를 구현 한다. 주어진 position의 값에 따라 하위 뷰들의 pivot, scale, alpha를 변경 하며 translate메소드 들을 통해서 위치좌표를 변경 하고 업데이트 한다. 
```java
/**
 * @author KangSung-Woo
 * @since 2016/08/17
 */
public class ViewPagerTransformer
    implements ViewPager.PageTransformer {
  private static final String TAG = ViewPagerTransformer.class.getSimpleName();
  /**
   * ViewPager Child view의 최소 scale value.
   */
  private static final float MIN_SCALE = 0.72f;     // 0.8f
  /**
   * ViewPager Child view의 최소 alpha value.
   */
  private static final float MIN_ALPHA = 0.3f;      // 0.6f
  /**
   * ViewPager Child view의 최소 translation_y value.
   */
  private static final float MIN_TRANSLATION_Y = 0.08f;

  @Override
  public void transformPage(View page, float position) {
    page.setAlpha(position <= -1f || position >= 1f ? 0f : 1f);
    onTransform(page, position);
  }

  protected void onTransform(View view, float position) {
    int pageWidth = view.getWidth();
    int pageHeight = view.getHeight();

    if (position < -1) { // [-Infinity,-1)
      // This page is way off-screen to the left.
      view.setAlpha(0);
    }

    else if (position <= 1) { // [-1,1]
      // Modify the default slide transition to shrink the page as well
      float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));

      // 중심 좌표의 재 설정
      view.setPivotX(pageWidth / 2);
      view.setPivotY(pageHeight);

      // Scale the page down (between MIN_SCALE and 1)
      view.setScaleX(scaleFactor);
      view.setScaleY(scaleFactor);

      // Fade the page relative to its size.
      view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));

      // set Translation Y
      float translationY = pageHeight * -(1 - Math.abs(position)) * MIN_TRANSLATION_Y;
      view.setTranslationY(translationY);

    }

    else { // (1,+Infinity]
      // This page is way off-screen to the right.
      view.setAlpha(0);
    }
  }// onTransform()

}
```

