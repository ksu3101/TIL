## gRPC 기초 개념 정리

이 문서에서는 gRPC에 대해서 짜집기 하여 정리한 문서로, 기본적인 개념을 잡기 위한 문서이다. 아무래도 짜집기 및 개인 번역을 통해서 정리한 만큼 틀린 내용이 있을수 있으므로 이에 대해서 유념하고, 틀린 내용에 대해서는 업데이트를 통해서 수정 하도록 하자. 

- 문서 
  - [Android with gRPC](https://developer.android.com/guide/topics/connectivity/grpc?hl=ko)
    - [Example](https://github.com/grpc/grpc-java/tree/v1.24.0/examples/android)
  - [gRPC.io](https://grpc.io/)
  - [gRPC Instroduction](https://grpc.io/docs/what-is-grpc/introduction/)
  - [Scheme of Protobuf](https://martin.kleppmann.com/2012/12/05/schema-evolution-in-avro-protocol-buffers-thrift.html)

- 참고하면 좋을 문서
  - [3 Crucial Concepts of gRPC in Android](https://vladsonkin.com/3-crucial-concepts-of-grpc-in-android/)
  - [뱅크샐러드 - 프로덕션 환경에서 사용하는 golang과 gRPC](https://blog.banksalad.com/tech/production-ready-grpc-in-golang/?gclid=CjwKCAiAn7L-BRBbEiwAl9UtkCuSD6XOnTrqzr6y-ZDUSnKDNhXT-gmHSiWeZYaVEnDKLVwk8LUenRoCxUgQAvD_BwE)

### 1. gRPC?

- gRPC는, google Remote Procedure Call. 
- HTTP/2 기반 Prodocol Buffers(protobuf)를 사용하여 직렬화된 바이트 스트림 통신 기술. 
- 기존 JSON기반의 네트워크 통신보다 더 많은 이점을 갖고 있음. (이점에 대해서는 하단 '1.1'항목 참고)
  - MSA를 갖는 서비스 구조에서 내부 콜이 빈번할 경우 성능에 이점을 갖는다. 
  - 꼭 MSA뿐만이 아니라 서비스, 컴포넌트, 모듈 간 통신에서 데이터간 통신을 갖을때 기존 객체를 json이나 xml등 직렬화된 데이터로 통신하는 일이 많은 경우 protobuf와 gRPC는 좋은 선택이 될 수 있다. 

#### 1.1 Prodocol buffer? 

- 직렬화된 데이터구조. (xml, JSON을 생각하면 쉽다)
- 다른 직렬화 데이터구조들에 비한 장, 단점
  - 장점
    - 데이터의 크기가 작아 통신 속도가 더 빠르다. 
    - 데이터 구조를 파싱할 필요가 없다. 
  - 단점
    - 인간이 읽기 불편하다. 
    - proto문법을 알아야 한다. 

- protocol buffer 문법 예제 
  
  ```json
  {
    "userName": "Martin",
    "favouriteNumber": 1337,
    "interests": ["daydreaming", "hacking"]
  }
  ```

  와 같은 json이 있을 경우 프로토콜 버퍼는 아래처럼 데이터 구조를 제공 한다. 

  ```
  message Person {
    required string user_name        = 1;
    optional int64  favourite_number = 2;
    repeated string interests        = 3;
  }
  ```

  ![protobuf_ex1](https://martin.kleppmann.com/2012/12/protobuf_small.png)

### 2. gRPC with Android 

- 프로젝트 레벨의 build.gradle에 아래 처럼 의존을 추가 한다. 

```
buildscript {
    dependencies {
        classpath "com.google.protobuf:protobuf-gradle-plugin:$protobuf_ver"
    }
}
```

- 그리고 프로토콜 버퍼를 사용할 모듈의 build.gradle에 아래처럼 gRPC의 의존을 추가 한다. 

```
plugins {
    id 'com.google.protobuf'
}

// ...

dependencies {
    implementation "io.grpc:grpc-okhttp:$okhttp_grpc_ver"
    implementation "io.grpc:grpc-protobuf-lite:$protobuf_lite_ver"
    implementation "io.grpc:grpc-stub:$grpc_ver"
    // ... 
}

protobuf {
    protoc { 
        artifact = 'com.google.protobuf:protoc:3.10.0'
    }
    plugins {
        javalite {
            artifact = 'com.google.protobuf:protoc-gen-javalite:3.0.0'
        }
        grpc {
            artifact = 'io.grpc:protoc-gen-grpc-java:1.33.1'
        }
    }
    generateProtoTasks {
        all().each { task ->
            task.plugins {
                javalite {}
                grpc { 
                    option 'lite'
                }
            }
        }
    }
}
```

- 추가적으로 Android studio에 Protocol buffer editor 플러그인을 추가 할 수 있다. 
