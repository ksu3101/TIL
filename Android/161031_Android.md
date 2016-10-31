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

JVM 에서 Heap은 런타임 시 동적으로 할당되어 사용되는 실제 인스턴스들이 생성되어 자리잡는 영역이라고 할 수 있다. 보통 `new`키워드를 통해서 생성되는 인스턴스 객체나 배열의 인스턴스 아이템들이 존재 한다. 이는 다른 영역인 스택 영역이나 static 참조 변수 에서 참조 할 수 있는 인스턴스 객체들도 저장 된다.

#### 4.4.1 Synchronized

JVM의 Heap은 모든 스레드에서 공유 된다. 그러므로 멀티 스레드 환경에선는 공유되는 자원(참조될 인스턴스) 에 대한 동기화가 필요 하다.

![jvm_heap](https://github.com/ksu3101/TIL/blob/master/Android/images/jvm_heap3.png)     

`synchronized` 키워드는 멀티 스레드 환경에서 공유 자원에 대한 읽기/쓰기 시 필요하다고 할 수 있다. 특히 이런 환경에서의 Critical Section상에서의 스레드 제어와 static 메소드 내부에서의 공유 자원에 대한 스레딩 제어지 필수라고 할 수 있다. 하지만 멀티 스레딩 환경에서 잦은 `synchronized`키워드의 남발은 오히려 프로세스의 성능을 낮춰버리는 문제가 존재 한다. 

자바에서 `synchronized` 키워드를 이용한 동기화 방법은 여러가지가 있다. 

 1. 공유 자원에 대해 접근 하는 메소드에 대한 `synchronized`. 
  
  ```java
  public synchronized void func() {
    // ... 
  }
  ```
  메소드 자체에 대한 동기화로 인하여 해당 메소드에서의 작업이 끝날때 까지 다른 스레드는 기다려야 한다. 당연히 성능이 가장 좋지 않다. 

  2. 공유 자원에 대한 작업을 할때만 단일 스레드의 접근만 허용하는 `block synchronized`.

  ```java
  public void func() {
    // ...
    synchronized(object) {
      // ...
    }
    // ...
  }
  ```
  공유된 자원에 접근하여 작업을 하는 구역에 대해서 동기화를 설정하는 block을 설정 한다. 필요한 영역에만 스레드의 접근을 제어할 수 있으므로 메소드전체에 대한 동기화 보다는 좋다. 

#### 4.4.2 Garbage Collection

![jvm_heap](https://github.com/ksu3101/TIL/blob/master/Android/images/jvm_heap2.png)

JVM의 Heap에서는 동적으로 물리 메모리에 올라가는 인스턴스들이 존재 한다. 그런데 계속 인스턴스를 생성 하기만 하면 당연히 Out of Memory 예외가 발생할 것 이다. 이를 막기 위해서 JVM에서는 특정한 알고리즘에 의하여 필요시 `Garbage Collection(GC)`라는 작업을 수행 하게 된다. 

GC는 메모리의 힙에 할당되어진 인스턴스 객체를 더이상 사용하지 않는다는 판단(특정 알고리즘)에 의해 참조를 제거 하고 메모리에서 나중에 제거 할 수 있게 해주는 것 이라고 할 수 있다. 

그리고 GC를 개발자가 임의적인 시점에 실행하게 할 수는 없다. 어떤 참조 객체에 대해 `null`을 설정 하는건 문제 없지만, `System.gc()`를 직접적으로 호출하는 것은 매우 지양해야 한다.   

GC할때에는 JVM이 GC를 수행하는 스레드를 제외한 나머지 스레드들을 모두 정지 시킨다. 이를 `stop the world`라고 한다. 그리고 Heap의 영역을 크게 3개로 나눈다. 

1. Young Generation area 
 - 새롭게 생성한 객체의 대부분이 이곳에 존재. 
 - 대부분 금방 접근 불가능 상태 (unreachable state)가 되기 때문에 보통 Young 영역에 왔다가 사라진다. 이를 `Minor GC`라고 한다.
 - 내부에서는 Eden, Survivor(2개) 으로 나뉘어 진다. 그 과정은 다음과 같다. 

 ``` 
  1. 보통 Eden에서 GC 후 살아남으면 Survivor 1로 이동 한다. 
  2. Survivor 1이 계속 차게 되면 Survivor 2로 이동 한다. 그러면 Survivor 1은 비어진다. (두개의 Survivor영역중 무조건 한개는 비어 있어야 한다.)
  3. 이 과정을 반복하다가 계속해서 살아남는 객체는 Old 영역으로 이동 한다.
 ``` 

2. Old Generation area 
 - 위 영역에서 접근 불가능 상태(unreachable state)가 되지 않아 살아남은 객체가 이곳으로 복사 된다. 
 - 대부분 young 영역 보다 크게 할당 되며, 크기가 큰 만큼 GC는 young 영역보다 적게 발생한다. 
 - 이 영역에서 객체가 사라질때 Major GC(혹은 Full GC)가 발생 한다.
 - 만약 Old 영역에서 Young 영역의 참조가 발생 하면 이를 512바이트의 chunk로 되어 있는 Card Table로 관리 한다. 

3. Permanent Generation area 
 - Method Area라고도 불리는 영역이다.
 - 객체나 억류(intern)된 문자열 정보를 저장 하는 곳 이다. 
 - 이 영역에서 GC가 발생하면 Major GC의 카운터에 포함 된다.

Old Generation Area에서 발생하는 Major GC 의 방식(알고리즘)은 여러가지가 있다. 일단 JDK7을 기준으로는 5가지가 있다. 

 1. Serial GC
 2. Parallen GC
 3. Parallel Old GC (Parallel Compacting GC)
 4. Concurrent Mark & Sweep GC 
 5. G1(Garbage First) GC  

- [참고하면 좋은글 - Naver D2 / Java Garbage Collection](http://d2.naver.com/helloworld/1329)

#### 4.4.3 Reference Objects

![jvm_heap](https://github.com/ksu3101/TIL/blob/master/Android/images/jvm_heap.png)

JVM의 Garbage Collection은 어떠한 객체에 대해 GC 여부를 판단 하는 작업은, 객체의 `Reachability`를 `Strongly`, `Softly`, `Weakly` 순서로 판별 하고 모두 아니라면 `Phantomly`의 Reachability 여부를 확인한다. 그리고 Phantomly 여부를 확인 하기 전에 `finalize()`를 진행 한다.   

GC의 Reference Object에 대한 GC순서는 다음과 같다고 생각 하면 된다. 

 1. Strongly reference
  - `new` 키워드를 이용하여 객체를 생성했을때의 참조.
  - Strongly reachable 하고 Strong reference에 의해 참조되고 있는 객체는 GC에서 일단 제외 된다. 이는 Memory leak에 유심해서 사용 해야 한다.  

 2. Softly reference 
  - `SoftReference` 래퍼 클래스를 이용하여 참조 변수를 래핑해서 사용 한다. 
  - Softly reference 는 GC에 의해서 수거 될 수 도 있고 되지 않을 수도 있다. 하지만 만약 OOME 가 발생할 수 있는 상황이 라면 SoftReference는 무조건 GC된다.  
  - GC되는 시점에 특별한 정책에 의해 GC여부가 결정 되게 할 수도 있다. 

 3. Weakly reference
  - `WeakReference` 래퍼 클래스를 이용하여 참조 변수를 래핑해서 사용 한다. 
  - Weakly reference는 GC가 발생하기 전 까지는 객체에 대한 참조를 유지하지만, 만약 GC가 발생하면 무조건 메모리를 수거 한다. 그래서 보통 Cache용도로 많이 사용 한다고 하지만 최근에는 VM에서의 Reference Object에 대한 GC가 공격적이라서 추천하지는 않는다고 한다. 

 4. finalize();
  - 일반적으로 인스턴스가 소멸되는 시점에 불리는 메소드로 알려져 있다. 
  - 하지만 이 메소드가 꼭 콜되는 일은 없다. 불릴수도 있고 안불릴 수도 있기 때문이다. 그래서 이 메소드를 재 정의해서 사용 하는것은 자제 하는것이 좋다. 

 5. Phantomly reference 
  - `PhantomReference` 래퍼 클래스를 이용하여 참조 변수를 래핑해서 사용 한다. 
  - Phantomly reference는 `finalize()` 메소드가 호출되고 난 뒤 그 객체와 관련된 작업을 수행할 필요가 있을때 주로 사용 된다. 
  - 이 reference의 특징은 다시는 이 객체를 참조할 수 없게 된다는 것 이다. 
 
#### 4.4.4 Deep copy & Shallow copy 

참조 변수의 얕은 복사는 하나의 참조를 두개 이상의 변수가 같이 참조 하고 있는 것 이다. 이런 경우 어느 참조 변수를 통해서 데이터를 변형 시켜도 모든 변수들은 같은 값을 보유 한다. 예를 들면 아래 소스와 같다. 

```java
private void f() {
  ArrayList<String> list = new ArrayList<>();
  anotherList = list;
}
```

위 메소드에서 list변수와 anotherList는 같은 메모리를 참조 하고 있다. 어느 변수를 통해서 원소를 add하거나, remove를 해도 같은 데이터를 보유 하게 된다. 

하지만 깊은 복사는 다르다. 참조된 원본 데이터에 대한 값 에 대한 복사를 하게 되는 것이다. 방법은 2가지가 존재 한다. 

`Cloneable`인터페이스를 구현하여 `clone()`메소드를 구현 하는 방법이 있는데,`clone()`메소드에서 primitive type은 `=`연산자를 이용하여 복사 하고,  내부에서 참조된 참조 변수들은 `new`연산자를 이용하여 값을 새로 복사 해야 한다. 예를 들면 `new String(buffer)`와 같은 형태가 될 것 이다. 내부에 참조된 변수들이 많다면 일일히 하나씩 다 `new`를 이용하여 생성 해 줘야 한다.

아니면 `new`연산자를 이용하여 생성자를 통해서 복사할 원본을 패러미터로 받아서 생성자 내부에서 값복사를 일일히 하는 방법도 있다. 

Java의 일부 클래스들은 이미 `clone()`이 구현되어진 클래스들이 존재 한다. 예를 들어 `ArrayList`나 `HashMap`같은 컬렉션 메소드들이 그 예 이다.   

### 4.5 Method Area

프로세스에서 유일하게 하나만 존재하는 Heap과 Method Area중 Method Area는 다른 스레드와도 공유 하는 공간이다. 이 곳 에서는 JVM이 `.class`파일을 로드 할 때 class 팡링나 포함된 바이트 코드 등의 type 정보, class 내 선언된 static 클래스와 변수들을 method area에 설정 한다. 

Method Area라는 것은 로드할 데이터 타입의 정보를 저장하고 static 클래스, 메소드, 변수 등에 대한 인스턴스의 참조를 저장하는 논리적 저장 공간이다.  

type(클래스, 인터페이스, 메소드, 변수 등...) 에 대해서 저장 하는 것은 다음과 같다. 

- type의 전체 이름
- type의 super class의 전체 이름. (interface나 java.lang.Object일 경우에는 제외)
- type이 class인지 인터페이스인지 여부
- public, abstract, final 과 같은 type의 키워드 수식어 
- type의 직접적인 super interface의 전체 이름에 대한 정렬된 리스트  

#### 4.5.1 Runtime Constant Pool 

Method Area에 포함된 정보중에는 `상수 풀`에 대한 정보 또한 저장 된다. 일반 상수와 type, field, method를 참조하는 reference와 같은 type들과 상수들의 정렬된 정보를 저장 한다. 

