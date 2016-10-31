# JVM의 Dalvik 과 안드로이드의 ART, 그리고 JAVA의 Runtime Data Area 정리

 JVM(Java Virtual Machine)은 아키텍쳐와 플랫폼에 상관없이 실행할 수 있는 환경을 제공하는 언어라고 할 수 있다. 안드로이드에서는 JAVA를 사용 하지만 JVM은 라이센스 문제로 인하여 Dalvik이라는 VM을 만들어서 사용 했었다. 

 안드로이드에서는 버전별로 사용되는 VM이 약간 다르다. 
 
  1. 안드로이드 2.2 프로요 버전 이전의 DalvikVM 에서는 앱이 구동되는 중에 실시간으로 Java소스를 CPU에 맞추어 네이티브로 변환 한다.
  2. 안드로이드 2.2 프로요 버전 이후의 DalvikVM 에서는 JIT(Just-In-Time) 컴파일러가 추가 되어 앱 최초 실행 시 자바 코드가 일정 부분을 한꺼번에 네이티브로 변환되어 변환된 내용을 메모리에 올리고 작업 하게 된다. 이전 버전보다는 퍼포먼스가 많이 좋아졌다. 

 안드로이드 킷캣 버전에서 처음 등장했으며 5.0 롤리팝 이후 적용된 ART(Android RunTime) VM 에서는 JIT와 AOT(Ahead-Of-Time)을 같이 사용 한다.   
 
 자세한 내용은 아래에서 다루도록 하겠다. 

## 1. 상세   

![img](https://github.com/ksu3101/TIL/blob/master/Android/images/jvm_dalvik_art_rda.png)

## 2. Package

### 2.1 class 파일 생성

### 2.2 proguard / 난독화 프로세스

### 2.3 dex 파일 생성

### 2.4  APK 파일 생성


## 3. Install

### 3.1  Dalvik VM

#### 3.1.1 Odex 파일 

#### 3.1.2 JIT / Just-In-Time

### 3.2 ART VM

#### 3.2.1 ELF 파일

#### 3.2.2 ART


## 4. Runtime Data Areas

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




