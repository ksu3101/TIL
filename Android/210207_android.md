## Helpful ADB commands

> 이 글은 Alex Zhukovich의 [Helpful ADB commands](https://alexzh.com/helpful-adb-commands/)을 번역 하였다.

Android Debug Bridge는 **ADB**로 잘 알려져 있다. 이는 Android SDK에 포함 된 명령줄(Command line) 유틸리티로 USB연결을 통해 안드로이드 기기 또는 에뮬레이터와 파일 전송, 앱 설치, 앱 권한 변경, 스크린 샷 촬영 등 상호작용을 할 수 있게 해준다. 

ADB에 익숙하지 않다면 [공식 ADB 웹 페이지](https://developer.android.com/studio/command-line/adb)부터 시작하는 것 이 좋다. 

이 문서에서는 안드로이드 기기와 상호작용을 하면서 앱 프로그램들을 보다 효율적으로 테스트 할 수 있는 몇가지 ADB명령어에 대해 정리 하였다. 

USB또는 Wi-Fi를 통해 연결하면 모든 안드로이드 기기 또는 에뮬레이터와 상호 작용할 수 있다. 

### ADB on Wi-Fi

Wi-Fi를 이용해 안드로이드 기기에 연결하기 위해서는 `adb connect <ip-address>`명령어를 사용 하면 된다. 

안드로이드 기기의 현재 IP주소는 "설정 > 장치 정보 > 상태" 화면으로 이동하여 확인 할 수 있다. 

예제로 안드로이드 기기의 주소가 "192.168.1.42"라고 하였을 때:

```
# 안드로이드 기기에 Wi-Fi의 IP 주소를 통해 접속 한다
> adb connect 192.168.1.42

# 기기 IP및 특정 포트를 통해 접속 한다 
> adb connect 192.168.1.42:5554

# 접속된 대상 IP로부터 연결을 종료 한다 
> adb disconnect 192.168.1.42
```

참고로 `adb connect`및 `adb disconnect`명령어는 기본적으로 포트 5555를 사용 한다. 기기 IP에 장치를 연결할 수 없는 경우 기기의 5555포트를 사용할 수 없어 TCP/IP연결을 허용하지 않아서 일 수 도 있다. 이 문제를 해결하려면 USB를 통해 기기를 연결한 뒤 다음 명령어를 실행해 보자. 

```
adb tcpip 5555
```

### Helpful ADB commands

ADB는 Android기기 및 에뮬레이터와 상호 작용할 수 있는 많은 명령어를 지원 한다. [여기](https://developer.android.com/studio/command-line/adb)에서 그 명령어에 대한 설명들을 찾을 수 있다. 

그러나 안드로이드 앱을 테스트 할 때 특정 시나리오에서 이점을 얻을 수 있는 몇가지 명령어에 대해 정리 하였다. 

#### Send input event

앱을 수동으로 확인하는 동안 바우처코드등과 같은 특정 텍스트를 직업 입력해야 하는 상황이 발생할 수 있다. 이는 장치에서 코드로 리소스에 접근할 수 없을 때 사용할 수 있는 사례가 될 수 있다. 

`adb shell input text "<text>"`명령을 통해 선택한 입력필드에 텍스트를 입력할 수 있다. 

```
# 공백을 입력하려면 "%s"을 작성하면 된다.
adb shell input text "insert%syour%stext"
```

`adb shell input keyevent <key\action-name>`명령을 통해 하드웨어 버튼에 대한 이벤트를 에뮬레이션 할 수 있다. [여기](https://developer.android.com/reference/android/view/KeyEvent.html) 에서 하드웨어 버튼의 키 종류를 확인 할 수 있다. 

```
adb shell input keyevent 26
adb shell input keyevent POWER
```

#### Testing app with Monkey testing approach

Monkey테스트는 사용자가 임의 입력(텍스트, 클릭, 하드웨어 버튼 등)을 증명하고 앱 동작을 확인 하며 앱이 크래시 되지 않는지 확인 하는 소프트웨어 테스트 기술이다. 

ADB에서는 Monkey테스트가 가능 하다. `adb shell monkey -p <package> -v <event-count> -s <seed>`를 사용할 수 있다. 테스트 세션을 재현 하려면 `<seed>`매개 변수가 필요 하다. 이 seed값이 동일하게 적용하여 monkey테스트를 재 실행하여 보면 유사한 이벤트가 발생할 것 이다. 

```
adb shell monkey -p com.alexzh.mapnotes -v 10000 -s 100
```

#### Changing permissions

`adb shell pm grant <package> <permission>`또는 `adb shell pm revoke <package> <permission>`명령어들을 통해 설치된 앱의 권한을 변경할 수 있다. 

권한을 부여하기 위해서는 `adb shell pm grant <package> <permission>`을 사용 한다. 

```
adb shell pm grant com.alexzh.mapnotes android.permission.ACCESS_FINE_LOCATION
adb shell pm grant com.alexzh.mapnotes android.permission.ACCESS_COARSE_LOCATION
```

권한을 제거 하기 위해서는 `adb shell pm revoke <package> <permission>`을 사용 한다. 

```
adb shell pm revoke com.alexzh.mapnotes android.permission.ACCESS_FINE_LOCATION
adb shell pm revoke com.alexzh.mapnotes android.permission.ACCESS_COARSE_LOCATION
```

그리고 앱의 권한을 확인할 수 도 있다. 

```
adb shell dumpsys package com.alexzh.mapnotes | grep permission
```

#### Simulate process death

Android의 OS는 앱이 백그라운드에서 실행중이면서 기기의 메모리가 부족할 때 프로세스를 종료하도록 할 수 있다. 이는 메모리를 확보하고 우선 순위가 더 높은 다른 앱이나 서비스에 할당 하기 위해서 이다. 

이러한 상황에 대비하고 OS에 의해 앱이 종료될 때 잠재적인 문제를 확인해야 할 필요가 있다. 

`adb shell am kill <package>`명령어를 이용해 이러한 상황을 시뮬레이션 할 수 있다. 

```
adb shell am kill com.alexzh.mapnotes
```

### Summary 

ADB에는 기기와 상호작용하고, 앱을 설치하고 다양한 사용 사례를 확인 하는데 많은 기능을 제공 한다. 추가적인 도움될만한 명령어는 아래와 같다. 

- `adb shell input`은 텍스트 입력, 하드웨어 버튼 키 이벤트 입력, 스와이프 제스쳐 등 의 기능을 제공 한다. 
  - `adb shell input text "insert%skyour%stext"`
  - `adb shell input keyevent 26`
  - `adb shell input keyevent POWER`
- `adb shell monkey -p <package> -v <event-count> -s <seed>`
- `adb shell pm grant <package> <permission>`, `adb shell pm revoke <package>`
- `adb shell am kill <package`

ADB명령어에 대한 추가 사항은 [이 문서](https://developer.android.com/studio/command-line/adb)를 통해 확인 해 보자. 