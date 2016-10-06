# Flat Color resource
flat 디자인에 잘 어울리는 컬러셋 들이 있다. 이 컬러셋을 리소스에 저장하여 활용 하고 selector등에 유용하게 사용할 수 있는 컬러셋을 만들어 놓았다. 

## 1. xml 예제
 
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>

   <!-- ALPHA HEX : 00 ~ FF
   100% — FF / 95% — F2 / 90% — E6 / 85% — D9 / 80% — CC / 75% — BF / 70% — B3
   65%  — A6 / 60% — 99 / 55% — 8C / 50% — 80 / 45% — 73 / 40% — 66 / 35% — 59
   30%  — 4D / 25% — 40 / 20% — 33 / 15% — 26 / 10% — 1A / 5%  — 0D -->

   <color name="black">#000000</color>
   <color name="white">#ffffff</color>

   <color name="red">#F00</color>
   <color name="blue">#00F</color>
   <color name="green">#0F0</color>
   <color name="transparent">@android:color/transparent</color>

   <color name="lightlightGray">#F2F2F2</color>
   <color name="lightGray">#bbb</color>
   <color name="gray">#888</color>
   <color name="darkGray">#333</color>

   <color name="transparent_light_white">#26ffffff</color>
   <color name="transparent_medium_white">#40ffffff</color>
   <color name="transparent_heavy_white">#B3ffffff</color>
   <color name="transparent_ultra_heavy_white">#F2ffffff</color>

   <color name="transparent_light_black">#26000000</color>
   <color name="transparent_medium_black">#40000000</color>
   <color name="transparent_heavy_black">#B3000000</color>
   <color name="transparent_ultra_heavy_black">#F2000000</color>

   <!-- Flat colors -->
   <color name="flat_emerald">#1abc9c</color>
   <color name="flat_emerald_sel">#16a085</color>
   <color name="flat_emerald_dis">#261abc9c</color>
   <color name="flat_green">#2ecc71</color>
   <color name="flat_green_sel">#27ae60</color>
   <color name="flat_green_dis">#262ecc71</color>
   <color name="flat_river">#3498db</color>
   <color name="flat_river_sel">#2980b9</color>
   <color name="flat_river_dis">#263498db</color>
   <color name="flat_purple">#9b59b6</color>
   <color name="flat_purple_sel">#8e44ad</color>
   <color name="flat_purple_dis">#269b59b6</color>
   <color name="flat_stone">#607487</color>
   <color name="flat_stone_sel">#34495e</color>
   <color name="flat_stone_dis">#26607487</color>
   <color name="flat_asphalt">#34495e</color>
   <color name="flat_asphalt_sel">#2c3e50</color>
   <color name="flat_asphalt_dis">#2634495e</color>
   <color name="flat_midnight">#2c3e50</color>
   <color name="flat_midnight_sel">#1c2e40</color>
   <color name="flat_midnight_dis">#262c3e50</color>
   <color name="flat_sun">#f1c40f</color>
   <color name="flat_sun_sel">#f39c12</color>
   <color name="flat_sun_dis">#26f1c40f</color>
   <color name="flat_orange">#e67e22</color>
   <color name="flat_orange_sel">#d35400</color>
   <color name="flat_orange_dis">#26e67e22</color>
   <color name="flat_red">#e74c3c</color>
   <color name="flat_red_sel">#c0392b</color>
   <color name="flat_red_dis">#26e74c3c</color>
   <color name="flat_razzmatazz">#db0a5b</color>
   <color name="flat_razzmatazz_sel">#9e0841</color>
   <color name="flat_razzmatazz_dis">#26db0a5b</color>
   <color name="flat_clouds">#ecf0f1</color>
   <color name="flat_clouds_sel">#bdc3c7</color>
   <color name="flat_clouds_dis">#26ecf0f1</color>
   <color name="flat_concrete">#95a5a6</color>
   <color name="flat_concrete_sel">#7f8c8d</color>
   <color name="flat_concrete_dis">#2695a5a6</color>
   <color name="flat_asbestos">#7f8c8d</color>
   <color name="flat_asbestos_sel">#717a7a</color>
   <color name="flat_asbestos_dis">#26717a7a</color>

   <color name="flat_def_white">#ffffff</color>
   <color name="flat_def_white_sel">@color/gray</color>
   <color name="flat_def_white_dis">#26ffffff</color>

   <color name="flat_def_black">#000000</color>
   <color name="flat_def_black_sel">@color/darkGray</color>
   <color name="flat_def_black_dis">#26000000</color>

</resources>
```

## 2. selector 예제

```xml
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android"
          android:enterFadeDuration="@integer/def_enter_fade_duration"
          android:exitFadeDuration="@integer/def_exit_fade_duration">

   <!-- Selected Status -->
   <item android:state_selected="true">
      <shape android:shape="rectangle">
         <solid android:color="@color/flat_orange_sel"/>
      </shape>
   </item>

   <!-- Pressed Status -->
   <item android:state_pressed="true">
      <shape android:shape="rectangle">
         <solid android:color="@color/flat_orange_sel"/>
      </shape>
   </item>

   <!-- Focused Status -->
   <item android:state_focused="true">
      <shape android:shape="rectangle">
         <solid android:color="@color/flat_orange_sel"/>
      </shape>
   </item>

   <!-- Activated Status -->
   <item android:state_activated="true">
      <shape android:shape="rectangle">
         <solid android:color="@color/flat_orange_sel"/>
      </shape>
   </item>

   <!-- Disabled Status -->
   <item android:state_enabled="false">
      <shape android:shape="rectangle">
         <solid android:color="@color/flat_orange_dis"/>
      </shape>
   </item>

   <!-- Default Status -->
   <item>
      <shape android:shape="rectangle">
         <solid android:color="@color/flat_orange"/>
      </shape>
   </item>

</selector>
``` 

## 3. [Color Manager plug in](https://github.com/shiraji/color-manager)
  
![plug in screen shot](https://github.com/ksu3101/TIL/blob/master/Android/images/as_color_mng.png)

안드로이드 스튜디오에서 colors resource에 등록한 컬러셋의 목록을 도식화 하여 볼수 있는 플러그인이다. 초기엔 버그가 좀 있었는데 최근에는 그럭저럭 잘 동작 하는 것 같다. 

개인적인 느낌으로는 리소스를 많이 등록하였을 경우 매우 유용한 플러그인 이다.    
원하는 컬러를 드래그 앤 드롭으로 가져다 쓸 수 있으며 마우스 오른쪽 버튼을 눌러 리소스 id를 복사하거나 수정 할 수도 있다. 
잘 사용한다면 개발에 들어갈 삽질 시간을 조금이라도 단축 시켜줄 수 있을 것 이다. 



