## 구글맵 이슈 처리

구글맵을 이용 해서 특정 좌표들을 화면에 여러개 보여줄때 발생한 이슈와 해결방법들에 대해 정리 해 보았다. 

### 1. 발생한 이슈

문제는 이렇다. 구글맵에 마커를 최소 1개에서 n개까지 보여주고 여러개의 맵 마커들을 한번에 보여줄 수 있도록 카메라의 위치, 줌을 재조정 해주는 게 목표 였다. 

일단 구글맵을 사용한 화면의 경우 안드로이드 스튜디오를 통해서 간단하게 auth인증과 키를 쉽게 얻을 수 있다. 게다가 생성되는 액티비티를 사용 해서 수정을 할 수 있다. 

생성된 액티비티를 수정한 결과는 아래와 같다. 

```kotlin
class PokeMapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var gMap: GoogleMap
    private val coordinate by navArgs<PokeMapsActivityArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap

        val markerBounds = LatLngBounds.Builder()
        val pokeInfos = coordinate.pokeMapInfos
        for (info in pokeInfos) {
            val targetCoordinates = LatLng(info.latitude, info.longitude)
            gMap.addMarker(MarkerOptions().position(targetCoordinates).title(info.name))
            markerBounds.include(targetCoordinates)
        }
        val bounds = markerBounds.build()
        gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50))
    }
}
```

딱히 별다른 문제는 없어보인다. 좌표를 네비게이션을 통해 Safe args로 받고 `OnMapReadyCallback`의 `onMapReady()`함수 에서 마커들을 설정하고 카메라에 대한 설정을 추가 하였다. 

하지만 위를 실행 하였을 때 문제가 있을떄도 있고 없을때도 있었지만 발생한 문제는 아래와 같은 로그를 발생시켰다. 

> com.google.maps.api.android.lib6.common.apiexception.c: Error using newLatLngBounds(LatLngBounds, int): Map size can't be 0. Most likely, layout has not yet occured for the map view.  Either wait until layout has occurred or use newLatLngBounds(LatLngBounds, int, int, int) which allows you to specify the map's dimensions.

### 2. 이슈가 발생한 이유

발생한 이유는 이렇다. 구글맵이 완전히 그려지기 전에 `newLatLngBounds`을 이용해서 bounds를 계산하려 했기 때문에 발생한 예외이다. 

예외를 처리 하는 방법은 아래와 같다. 

### 3. 해결 방법

완벽한 해결방법은 아니지만 구글맵 인스턴스에 `setOnMapLoadedCallback()`을 이용해 맵이 완전히 로드된 뒤 콜백을 설정해 마커를 설정할 수 있다. 

```kotlin
gMap.setOnMapLoadedCallback {
    gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50))
}
```
