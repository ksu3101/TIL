# JVM의 Dalvik 과 안드로이드의 ART, 그리고 JAVA의 Runtime Data Area 정리

JVM(Java Virtual Machine)은 아키텍쳐와 플랫폼에 상관없이 실행할 수 있는 환경을 제공하는 언어라고 할 수 있다. 안드로이드에서는 JAVA를 사용 하지만 JVM은 라이센스 문제로 인하여 Dalvik이라는 VM을 만들어서 사용 했었다. Dalvik VM은 JVM이기는 하지만 JVM의 명세를 따르지는 않는다. Stack머신인 다른 JVM과는 달리 Dalvik VM은 레지스터 머신이며, 독자적인 툴을 이요하여 자바 바이트 코드를 Dalvik VM용 레지스터 기반 명령어 코드로 변환 한다. 

안드로이드에서는 버전별로 사용되는 VM이 약간 다르다. 
 
  1. 안드로이드 2.2 프로요 버전 이전의 DalvikVM 에서는 앱이 구동되는 중에 실시간으로 Java소스를 CPU에 맞추어 네이티브로 변환 한다.
  2. 안드로이드 2.2 프로요 버전 이후의 DalvikVM 에서는 JIT(Just-In-Time) 컴파일러가 추가 되어 앱 최초 실행 시 자바 코드가 일정 부분을 한꺼번에 네이티브로 변환되어 변환된 내용을 메모리에 올리고 작업 하게 된다. 이전 버전보다는 퍼포먼스가 많이 좋아졌다. 

안드로이드 킷캣 버전에서 처음 등장했으며 5.0 롤리팝 이후 적용된 ART(Android RunTime) VM 에서는 JIT와 AOT(Ahead-Of-Time)을 같이 사용 한다.   
 
자세한 내용은 아래에서 다루도록 하겠다. 

## 1. 패키징-설치-실행 정리     

![img](https://github.com/ksu3101/TIL/blob/master/Android/images/jvm_dalvik_art_rda.png)

## 2. Package

![img](https://github.com/ksu3101/TIL/blob/master/Android/images/jvm_package.png)

### 2.1 class 파일 생성

 JVM은 자바소스를 변환한 자바 바이트 코드를 실행 한다. 자바의 컴파일러는 C/C++의 컴파잉러처럼 고수준 언어를 기계어로 변환하는 것 이아니라 개발자가 이해 하는 자바 언어를 JVM이 이해하는 바이트코드로 번역 한다. 

 따라서 변환되어진 바이트 코드는 플랫폼에 상관없이 JVM(JRE)이 설치되었다면 어디에서나 실행 할 수 있다. 자바 컴파일러인 `javac`를 이용하여 원본 `.java`파일을 컴파일후 바이트 코드로 변환 하면 `.class`파일이 생성 된다. 

 `.class`파일 자체는 바이너리 파일이다. 그래서 일반적인 개발자(사람) 이 이해하기 힘들다. 이를 보완하기 위해서 역 어셈블러(disassembler)인 `javap`을 사용 하기도 한다.   

### 2.2 proguard / 난독화 프로세스

 `Proguard`는 코드의 난독화(Code Obfuscation)을 위한 툴 로 변수나 메소드, 클래스 등의 이름들을 의미없는 이름으로 짧게 바꾸어 사람으로 보기에 어렵고 복잡한 소스로 보이게 만들어 준다. 그와 동시에 불필요한 코드를 찾고 제거 하며 최적화 하는 기능도 갖고 있다.

 ![proguard](https://www.guardsquare.com/files/media/javac_proguard_dex.png)  

 `javac` 컴파일러를 통해 만들어진 `.class` 자바 바이트 코드와 라이브러리들의 `.class`코드를 `Proguard`의 설정이 명시된 파일들을 적용 하면 코드단독화가 적용된 `.class`파일이 생성 된다.  

### 2.3 dex 파일 생성

생성된 `.class`파일들을 `dxtool`등으로 `.dex`파일로 변환 한다. `.class`파일은 JVM에서는 사용 할 수 있지만 Dalvik VM 에서는 `.dex` 바이트 코드 파일로 변환해서 사용 해야 하기 때문이다. `.dex`파일의 내부에는 모든 클래스, 메소드, 내부 멤버변수등의 정보가 저장 된다.  

저장되는 내용들의 설명은 다음과 같다. 
  - [Dalvik byteCode by Google](https://source.android.com/devices/tech/dalvik/dalvik-bytecode.html)
  - [Dalvik Executable format by Google](https://source.android.com/devices/tech/dalvik/dex-format.html#file-layout) 

 Name | Format | Description | size 
 --- | --- | --- | ---
 header | heade_item | Dex 파일 헤더 정보 | 4 bytes 
 string_ids | string_id_item[] | string identifiers list. | 4 bytes  
 type_ids | type_id_item[] | type identifiers list. | 4 bytes 
 proto_ids | proto_id_item[] | method prototype identifiers list. | 4 bytes 
 field_ids | field_id_item[] | field identifiers list. | 4 bytes 
 method_ids | method_id_item[] | method identifers list. | 4 bytes 
 class_defs | class_def_item[] | class definition list. | 4 bytes 
 data | ubyte[] | data area, containing all the support data for the tables listed above. | -  
 link_data | ubyte[] | data used in statically linked files. | - 

 이 외에도 `Annotation`등 기타 정보들도 저장 된다. 자세한건 위 google의 링크를 참고 할 것. 

### 2.3.1 MultiDex 
 
 - [Configure Apps with Over 64k Methods by Google](https://developer.android.com/studio/build/multidex.html)

`.dex`파일에는 모든 클래스와 메소드, 멤버 등 모든 정보가 저장 된다. 이 중에 `method_ids`에는 메소드들의 인덱스가 배열로 저장된다. 문제는 Dalvik VM의 DEX파일 포맷에서는 이 필드에서 16비트만 사용 가능 한 것 이다. 그렇기 때문에 최대 메소드의 정보가 **65,535**개 만 저장 된다. 

만약 최대 64k 메소드 제한을 넘겨서 사용 해야 할 경우에는 다음과 같이 사용 하면 된다. 

 - 타겟 디바이스가 5.0 버전 미만이면`multidex support library`를 가져와서 적용 하면 된다. 기타 정보는 따로 찾아보면 알 수 있다. 
 - 타겟 디바이스가 5.0 버전 이상 이라면 `ART` VM을 사용 하기 때문에 신경쓰지 않아도 된다. 

### 2.4  APK 파일 생성
 
 생성된 `.dex`파일들과 `AndroidManifest.xml` 그리고 각종 자원 파일들(이미지, 레이아웃, asset 들 등..)을 zip파일로 압축 한다. 그리고 이름을 `apk`파일로 변경 한다. 최초로 만들어진 이 `apk`파일은 `unsigned apk`라고 하는데 `jarsigner`등으로 debug key store로 사이닝 된 파일이다. 보통 흔히 말하는 개발자용 output 파일인 것 이다. 

 만약 이 파일을 발급받은 key store를 이용하여 `jarsigner`등으로 사이닝 하면 `signed apk`파일을 생성 할 수 있다. 이 `apk`파일은 공식적으로 google play store등을 통해서 퍼블리싱 할 수 있는 파일 이다. 

## 3. Install

![img](https://github.com/ksu3101/TIL/blob/master/Android/images/jvm_install.png)

### 3.1  Dalvik VM
 
 - [ART and Dalvik by Google](https://source.android.com/devices/tech/dalvik/index.html)

어플리케이션을 설치 후 내부에 존재하는 `.dex`는 dalvik VM 에서 `dexopt`에 의해 `Odex`파일로 optimized 된다. 이렇게 만들어진 `Odex`파일은 Dalvik VM에서 로드 되어 사용 된다. 

dalvik 에서는 **JIT(Just-In-Time)** 컴파일러를 사용 한다. 이는 `.dex` 바이트 코드 를 번역해서 네이티브 코드로 번역 하여 실행 하게 되는데 실행 시점에 번역하고 메모리에 올리는 것 이다. 문제는 번역 할때의 정보들도 메모리에 올리기 때문에 퍼포먼스가 좋지 않다. 

### 3.2 ART VM

안드로이드 킷캣 버전에서 처음 적용 되었으며 추후 롤리팝버전에서 완전히 적용된 `ART` VM 은 `Android RunTime`의 줄임말 으로서 안드로이드를 위해서 새로 만든 VM 이라고 할 수 있다.  

ART 에서는 앱을 설치 할 때 완전히 네이티브 앱으로 변환해서 설치 하게 된다. 이를 **Ahead-Of-Time (AOT)** 컴파일 이라고 한다. 앱의 설치 시점에 바이트 코드를 미리 번역해서 저장해 놓기 때문에 JIT를 사용한 것에 비하면 빠르다. 하지만 앱의 설치 후 혹은 업데이트 후에 앱의 바이트 코드를 번역하기 위한 과정이 오래 걸릴 수 도 있는 단점이 존재 한다. 

안드로이드 N(누가) 버전에서는 ART에 JIT도 추가 된다고 한다. JIT컴파일러는 ART의 AOT컴파일러를 보완하고 런타임 성능을 개선 하며 저장공간을 절약 하고 앱의 업데이트와 실행 속도를 빠르게 해 준다고 한다.   

dalvikVM 에서는 `.odex`파일을 생성하여 VM에서 실행 하였지만, ART에서는 `dex2oat`을 이용하여 이미 생성된 `ELF`(Executable and Linkable Format)파일을 실행 한다. 이 파일에는  기존의 `dex`정보와 native code가 같이 존재 한다. `ELF`파일에 대한 정보는 아래와 같다. 

 ![img](https://github.com/ksu3101/TIL/blob/master/Android/images/jvm_elf.png)

 - ELF 헤더 : 전체 파일 구성의 이미지의 정보가 담겨 있음. 
 - Section (.text, .rodata) : Linking에 필요한 명령어, 데이터, 심볼 테이블, 재배치 정보등 목적 파일 정보가 담겨 있음. 
 - Program Header Table : 시스템이 어떻게 이 프로그램을 실행시키는데 필요한 정보.
 - Section Header Table : 목적 파일에 들어있는 섹션들이 어떻게 구성되어 있는지에 대한 정보들.   

## 4. Runtime Data Areas

![img](https://github.com/ksu3101/TIL/blob/master/Android/images/jvm_rda.png)

### 4.1 PC Register

스레드가 생성될 때 마다 생성되는 메모리 공간 으로서, Thread가 어떠한 명령을 실행하게 될지에 대한 부분을 기록 한다.

이 는 CPU 에서 소스의 명령들을 처리 하는 과정에서 필요한 데이터들을 Stack에서 Operand(피연산자)를 얻어 PC(Program Counter) Register라는 CPU내 기억장치에 저장 한다. 이러한 CPU의 Register의 역할을 JVM의 메모리영역으로 구현된 형태 이다.

PC Register는 각 Thread마다 하나씩 존재 하며, Native Pointer와 Return Address를 갖고 있다. 

#### 4.1.1 volatile 

자바의 `volatile` 키워드는 선언되어진 해당 변수의 암묵적인 동기화 와 멀티 프로세서 환경에서 스레드간 공유 변수의 메모리 가시화를 보장 하는 변수 이다. 이 키워드의 핵심 역할은 다음과 같다.

![volatile_cpu](https://github.com/ksu3101/TIL/blob/master/Android/images/java-volatile-2.png) 

일반적으로 메인메모리의 힙에 자리잡은 인스턴스는 CPU에서 처리 할때 CPU내의 캐시(레지스터)에 저장하고 빠르게 처리 한다. 문제는 서로 다른 스레드 환경에서 서로 멀티 프로세서(여러개의 CPU를 지원하는 환경)로 인해 서로 다른 CPU에서 공유된 인스턴스를 복사해 왔을 때 이다. 

이때, 이 서로 복사된 공유된 변수를 읽기만 한다면 문제는 없지만, 두개 이상의 스레드에서 단 하나라도 수정을 하게 되면 서로 다른 값을 가지게 되므로 이때 공유된 변수는 서로 다른 값이 된다. 보통 이런상황을 메모리 가시성을 확보 해 줘야 한다고 한다. 

이러한 상황을 방지하기 위해서 `volatile`키워드를 사용 하게 되면 공유된 변수를 CPU로 복사하지 않고 메인 메모리에서 사용 할 수 있게 한다. 문제는 여러개의 스레드에서 메인 메모리에 존재하는 인스턴스에 접근 하기 때문에 단 한개 스레드만 접근 할 수 있는 동기화를 지원 한다. 

결론적으로 멀티 스레딩 환경에서 어떤 공용 변수의 값에 대한 쓰기가 생긴 다면 `volatile`을 사용하는게 좋다. 하지만 해당 변수에 대해 읽기만 한다면 `volatile`을 사용할 이유는 없다. 

### 4.2 Stack

JVM의 Stack은 스레드가 생성 될 때 마다 생성되는 스레드의 정보들을 기록하는 Frame을 저장 하는 메모리 영역이다. 보통 JVM에서는 Stack Frame을 JVM Stack에 넣고(Push) 빼는(Pop) 하는 작업만 수행 한다.  

#### 4.2.1 Stack Frame 

Stack Frame은 해당 Thread가 수행하고 있는 Method단위로 기록 되는 정보들 이다. 어떤 Method가 실행 되면 Class의 메타 정보를 이용하여 적절한 크기로 생성 되어 JVM Stack에 Push하고 Method의 작업을 수행한다. 

Stack Frame을 이루는 내용은 다음과 같다. 

#### 4.2.2 Local Variable Array (지역 변수 배열)
 - Method의 Parameter Variable과 Local Variable을 저장 한다. 
 - [0] 번째 index에는 Class의 인스턴스에 대한 참조가 저장 된다.

#### 4.2.3 Operand Stack (피연산자 스택)
 - 실제로 JVM이 Method에 대한 연산을 수행하는 작업 공간. 
 - Stack내부의 Stack 으로서 데이터를 push하고 연산 한 뒤 pop하는 과정들이 진행 된다.

#### 4.2.4. Reference to Constant Pool (상수 풀 에 대한 참조 배열)
 - Method Area에 존재 하는 상수들의 참조 목록들. Constant Pool의 Pointer의 정보를 저장 한다. 

#### 4.2.5. Exception Stack (예외 스택)  
 - Method에서 예외가 발생시 Exception을 Frame Data에 저장 한다. 
 - Exception이 발생 하면 바이트 코드를 참고 하여 catch영역을 참고 하여 점프 한다. 

### 4.3 Native Method Stack 

Java언어 외에 작성된 프로그램, 대표적으로 JNI(Java Native Interface)로 알려진 Native Code로 되어 있는 Function들의 호출들에 대한 정보를 저장하는 곳 이다. 
Native Function의 Parameter Variable과 Local Variable등을 저장 한다. 

### 4.4 Heap 

#### 4.4.1 Reference Objects

#### 4.4.2 Garbage Collection

#### 4.4.3 Deep copy & Shallow copy 

### 4.5 Method Area

#### 4.5.1 Runtime Constant Pool 



