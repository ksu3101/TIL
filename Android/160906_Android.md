# [Parallax ViewPager Transition effect]()

### 설명
- Parallax효과란 2D 횡스크롤 게임에서 배경과 전면에 위치한 sprite들과 케릭터들과의 이동속도(translate dx coordinate)가 서로 다른 효과 이다.
- ViewPager의 Page transition효과에 ViewPager의 `Transformer`를 재정의하고 적용 하여 Parallax효과를 줄 수 있다. 그 예로 Yahoo 앱의 Parallax효과가 있다.   
![Yahoo](https://cdn-images-1.medium.com/max/800/1*FGCTHL82sXl_vewHFAcS3Q.gif)
- 이러한 효과와 비슷하게 아래 이미지 처럼 적용 할 수 있다.   
![screenshot](https://github.com/ksu3101/TIL/blob/master/Android/images/videotogif_2016.09.06_11.22.14.gif)

### 구현 
- [Transformer 구현](https://github.com/ksu3101/SnsTemplate/blob/master/app/src/main/java/kr/swkang/snstemplate/showcase/sub/CaseTransformer.java)
```java
public class CaseTransformer
    implements ViewPager.PageTransformer {
  @Override
  public void transformPage(View page, float position) {
    final int pageWidth = page.getWidth();

    if (position < -1) {          // [-Infinity, -1]
      page.setAlpha(1);
    }
    else if (position <= 1) {     // [-1, 1]
      ImageView bg = (ImageView) page.findViewById(R.id.showcase_sub_f_ivBg);
      if (bg != null) {
        // parallax effect on Background imageview.
        bg.setTranslationX(-position * (pageWidth / 2));
      }
      // some more transform effect here..
    }
    else {                        // [1, +Infinity]
      page.setAlpha(1);
    }
  }
}
```
- [ViewPager와 Activity의 구현](https://github.com/ksu3101/SnsTemplate/blob/master/app/src/main/java/kr/swkang/snstemplate/showcase/ShowCaseActivity.java)
