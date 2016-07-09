### Daily development learning  
#### 1. RecyclerView generic Adapter  
- 안드로이드의 UI를 개발 하다보면 비슷비슷한 형태를 가진 UI를 구현 하는 경우가 많다. 그 중에서도 자주 사용되는 것 이 있다면 목록을 표현하는 `RecyclerView`가 아닐까 싶다. 
- 최근에는 한가지 형태의 아이템의 목록을 보여주는 목록 뷰가 아닌 여러가지 형태의 목록을 보여줘야 하는 경우가 많다. 예를 들면, 구글 플레이 스토어의 메인 화면등이다. 
- 보통 일반적으로 구현되어진 형태로는 최 상단에서 뭔가를 보여주는 `Header`와 중간의 메인 아이템들인 `Body`, 그리고 최 하단에서 작동 하게 되는 `Footer`라는 컨셉으로 구현 된다. (몰론 이는 개발자나 기획자에 따라 다를 수 있다. 게다가 여러 디자인 패턴에 의해 여러개로 중첩 될 수도 있다)  
- 최소 1개 이상의 레이아웃(`ViewType`)을 지원 하며 서로 다른 UX의 지원(pull to refresh, load more, swipe-dismiss 등)하기 위해서 비슷 한 구현을 가진 `RecyclerView`의 `Adapter`를 개발 하는 일이 잦아짐을 느낄 수 있다. 사실 구현은 비슷하지만 실제 다루는 데이터나 `RecyclerView`의 `Adapter`에 Binding되는 `ViewType`에 따른 `ViewHolder`의 구현과 갯수가 다를 뿐 이니까 말이다. 
- 그렇다면 이러한 형태를 가진 `RecyclerView`의 `Adapter`구현에 Generic한 형태를 만들고 이를 상속받아서 자주 사용되며 반복되는 코드를 줄이고 추후 유지, 보수에도 쓸만한 `Adapter`를 만들고 다뤄 보자. 
- 


