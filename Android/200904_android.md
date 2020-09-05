## Lottie for Android - Lottie란 무엇인가

Airbnb의 애니메이션 지운 라이브러리인 [Lottie](http://airbnb.io/lottie/#/README)에서 안드로이드 관련 문서를 번역하고 정리해보려 한다. 

1. [Lottie for Android - Lottie란 무엇인가](https://github.com/ksu3101/TIL/blob/master/Android/200904_android.md)
2. [Lottie for Android - Basic](https://github.com/ksu3101/TIL/blob/master/Android/200905_android.md)
3. [Lottie for Android - Advanced 1](https://github.com/ksu3101/TIL/blob/master/Android/200906_android.md)

### 1. Lottie 

Lottie는 json으로 내보내진 Adobe After effects애니메이션을 파싱한뒤 [Bodymovin](https://github.com/airbnb/lottie-web)과 렌더러를 사용하여 Android, iOS, 웹 및 Windows용 애니메이션 라이브러리 이다. 

Lottie를 사용 하면 디자이너는 개발자가 손으로 힘들게 아름다운 애니메이션을 다시 만들지 않고도 제공할 수 있다. Lottie에서 지원되는 애니메이션은 After Effects에서 생성되어 Bodymovin으로 내보내지고 추가 개발자들의 노력없이 기본적으로 렌더링 된다. 

Bodymovin은 Hernan Torrisi에서 만든 After Effects플러그인으로 After Effects에서 만든 파일을 josn으로 추출해준다. Bodymovin의 작업을 기반으로 Airbnb에서는 이를 Android, iOS, Reactive Native및 Windows에서 사용할 수 있게 API를 확장 하였다. 

#### 1.1 지원되는 다른 플랫폼 

- [Xamarin (martijn00)](https://github.com/martijn00/LottieXamarin)
- [NativeScript (bradmartin)](https://github.com/bradmartin/nativescript-lottie)
- [Axway Appcelerator (m1ga)](https://github.com/m1ga/ti.animation)
- [ReactXP (colmbrady)](https://github.com/colmbrady/lottie-reactxp)
- [Flutter (simolus3)](https://github.com/simolus3/fluttie)
- [Flutter (fabiomsr)](https://github.com/fabiomsr/lottie-flutter)

#### 1.2 샘플 앱 

Android용 샘플앱은 직접 빌드해보거나 [Play Store](https://play.google.com/store/apps/details?id=com.airbnb.lottie)에서 다운로드 하여 설치해 사용해볼 수 있다. 샘플앱 에서는 일부 내장 애니메이션이 포함되어 있지만 내부 저장소 또는 외부 URL에서 애니메이션을 불러올 수 있다. 

Windows의 경우 [Lottie Viewer app](https://aka.ms/lottieviewer)을 사용하여 Lottie애니메이션 및 codegen 클래스를 미리 확인 할 수 있으며, [Lottie Sample app](https://aka.ms/lottiesamples)을 사용하여 코드 샘플 및 간단한 튜토리얼을 시작해 볼 수 있다. 

#### 1.3 Lottie의 대체할 다른 방법

1. 손으로 애니메이션을 직접 만든다. 이는 Android와 iOS등 에서 애니메이션의 개발에 엄청난 시간을 투자 해야 한다. 애니메이션을 만들기 위해 너무 많은 시간을 소비하는 것은 좋지 않다. 

2. [Facebook Keyframes](https://github.com/facebookincubator/Keyframes)을 사용 한다. KeyFrames는 사용자 반응을 위해 만들어진 Facebook의 멋진 새 라이브러리 이다. 그러나 Keyframes는 마스크, Mattes, 트림 패스, 대시 패턴등과 같은 Lottie의 일부 기능을 지원하지 않는다. 

3. GIF를 사용 한다. 하지만 GIF는 bodymovin json파일 크기의 두배 이상이며 크고 고밀도 화면과 일치하려 하면 고정 크기로 렌더링 되어야 한다. 

4. Png 시퀀스를 사용 한다. Png 시퀀스는 파일 크기가 bodymovin json파일 크기의 30~50배인 경우가 많고 확장이 불가능하여 GIF보다 더 좋지 않다. 

5. Android의 경우 Animated Vector Drawable을 사용 한다. 메인 스레듣 대신 Render스레드 에서만 실행되기 떄문에 성능은 괜찮다. 하지만 애니메이션의 진행은 수동으로 설정할 수 없으며 텍스트 또는 동적 색상을 지원하지 않는다. 그리고 프로그래밍 방식 및 인터넷을 통한 불러오기를 사용할 수 없다. 

#### 1.4 왜 Lottie라고 부르는가? 

Lottie는 독일 영화 감독이자 실루엣 애니메이션의 선구자의 이름을 따서 명명 되었다. 그녀의 잘 알려진 영화는 The Adventures of Prince Achmed(1926)으로, 월트 디즈니의 장편인 백설공주와 일곱 난장이(1937)를 10년 넘게 앞선 가장 오래된 장편 애니메이션 영화이다. 

