## Dagger vs Koin 

Android 개발 환경에서 DI(Dependency Injection)를 위해 사용 되는 도구로 [Dagger](https://dagger.dev/dev-guide/android.html) 와 [Koin](https://insert-koin.io/) 이 있다. 이 도구는 지향하는 목표는 비슷하지만 사용하는 방법과 내부 흐름은 전혀 다르다. 

이 두개의 도구를 간단히 비교 하면 아래와 같다. 

||Dagger|Koin|
|---|---|---|
|사용 언어, 패턴|JAVA|Kotlin, Service Locator pattern|
|DI 시점|Compile|Runtime|
|장점|<ul> <li>런타임 중 에러 없어 안정적</li> <li>퍼포먼스 오버헤드가 적음</li> </ul>|<ul> <li>Dagger 에 비해 컴파일 속도가 빠름</li> <li>코틀린의 장점들을 그대로 사용 가능</li> <li>학습이 빠르며 가독성 높은 코드</li> </ul>|
|단점|<ul> <li>컴파일 시 오버헤드 존재</li> <li>학습곡선이 높고 디버깅이 어려움</li> </ul>|<ul> <li>런타임중 에러가 발생 에러 핸들링 필수</li> <li>Dagger 에 비해 런타임 중 오버헤드 발생</li> </ul>|

[Android developer 의 Dependency Injection 문서](https://developer.android.com/training/dependency-injection#choosing-right-di-tool) 에서는 프로젝트의 크기(추가적으로 앱 내부에서 보여지게 될 화면의 갯수) 에 따라 크면 클 수록 Dagger 를, 그에 반해 작을수록 Service locator pattern 의 사용을 추천 하였다. 몰론 선택은 개발자의 자유 라고 생각 하긴 한다. 

Dagger 와 Service locator pattern 을 기반으로 한 Koin은 장단점이 너무나도 명확하다. 작성자는 Dagger2 를 처음 사용 한 계기가 2016년 말에 처음 적용 하고 계속 사용해 왔었다. 하지만 kotlin 이 등장하고 난 뒤 기존 Java와 Annotation processor 기반의 Dagger 에서 발생 하는 보일러 플레이트 코드와 수많은 모듈 component, ViewModel 그리고 Redux 구조에서 발생 할 수 있는 Reducer, middleware 등 이 도메인의 갯수만큼 증가 함으로 인한 문제는 계속 눈엣가시였었다. 

