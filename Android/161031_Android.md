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

 저장되는 내용들의 설명은 다음과 같다. [참고](https://source.android.com/devices/tech/dalvik/dex-format.html#file-layout) 

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

### 2.3.1 MultiDex 
 
 `.dex`파일에는 모든 클래스와 메소드, 멤버 등 모든 정보가 1개의 파일에 저장된다. 문제는 이 필드의 제한이 존재 한다는 것 이다. dex파일 포맷 에서는 

### 2.4  APK 파일 생성


## 3. Install

![img](https://github.com/ksu3101/TIL/blob/master/Android/images/jvm_install.png)

### 3.1  Dalvik VM

#### 3.1.1 Odex 파일 

#### 3.1.2 JIT / Just-In-Time

### 3.2 ART VM

#### 3.2.1 ELF 파일

![img](https://github.com/ksu3101/TIL/blob/master/Android/images/jvm_elf.png)

#### 3.2.2 ART


## 4. Runtime Data Areas

![img](https://github.com/ksu3101/TIL/blob/master/Android/images/jvm_rda.png)

### 4.1 Pc Register

#### 4.1.1 volatile 

### 4.2 Stack

#### 4.2.1 Stack Frame 

#### 4.2.2 Local Variable Array

#### 4.2.3 Operand Stack

#### 4.2.4 Reference to Constant Pool

### 4.3 Native Method Stack 

### 4.4 Heap 

#### 4.4.1 Reference Objects

#### 4.4.2 Garbage Collection

#### 4.4.3 Deep copy & Shallow copy 

### 4.5 Method Area

#### 4.5.1 Runtime Constant Pool 



