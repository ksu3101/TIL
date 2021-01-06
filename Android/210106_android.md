## Files, Boilerplate, and Testability

> 이 글은 Jesse Wilson의 [Files, Boilerplate, and Testability](https://publicobject.com/2020/12/29/files-boilerplate-testability/)을 번역 하였다. 

원본글의 작성자는 Okio의 다중 플랫폼 파일 시스템 API를 작업 중 이다. 이 작업에서 주요 유형을 소개하면 아래 3개와 같다. 

- `Path` : 파일 또는 디렉토리를 식별하는 값 객체. 
- `FileMetadata` : 유형 및 크기를 포함하여 파일 또는 디렉토리를 설명하는 값 객체. 
- `Filesystem` : 파일과 디렉토리를 읽고 쓰는 서비스 객체. 

추가적으로 2개의 도우미 클래스들이 있다. 

- `FakeFilesystem` : 빠른 테스트를 위한 in-memory `Filesystem`이다. 그리고 아무것도 file을 close하지 않았는지 여부등을 쉽게 알 수 있다. 
- `ForwardingFilesystem` : `Filesystem`을 데코레이터 패턴으로 적용 하였으며 `Filesystem`의 관측성, 오류의 주입 또는 변환을 위해 사용 된다. 

### Implicit and Explicit Dependencies

그렇지만 Kotlin의 JVM전용 `kotlin.io` API와 비교할 때 걱정이 된다. 예를 들어 파일에 백만줄을 사용할때, 

```kotlin
fun writeHellos(name: String) {
    File("hello1M.txt").bufferedWriter().use {
        for (i in 0 until 1_000_000) {
            it.write("$i hello, $name!\n")
        }
    }
}
```

Okio에서 동일한 작업을 수행하는 것은 비슷하지만 `Filesystem.SYSTEM`객체도 필요 해진다. 그 것은 코드를 복잡하게 만든다. 

```kotlin
fun writeHellos(name: String) {
    Filesystem.SYSTEM.sink("hello1M.txt".toPath()).buffer().use {
        for (i in 0 until 1_000_000) {
            it.writeUtf8("$i hello, $name!\n")
        }
    }
}
```

이것을 제거 하기 위해 짧은 확장 함수를 추가할 수 있다. 

```kotlin
fun Path.sink() = Filesystem.SYSTEM.sink(this)
```

그러나 이렇게 하면 `Filesystem.SYSTEM`종속성이 숨겨지게 되므로 테스트 가능성이 떨어진다. 

### ‘Injecting’ the Filesystem

테스트 가능성을 위해 `Filesystem.SYSTEM`을 직접 사용하지 말자. `writeHellos()`함수로 가져 오려면 래퍼 클래스의 생성자 매개 변수로 만들어줘야 한다. 

```kotlin
class HelloFileWriter(
    private val filesystem: Filesystem
) {
    fun writeHellos(name: String) {
        filesystem.sink("hello1M.txt".toPath()).buffer().use {
            for (i in 0 until 1_000_000) {
                it.writeUtf8("$i hello, $name!\n")
            }
        }
    }
}
```

이제 main함수에서 실제 파일시스템을 사용 한다. 


```kotlin
fun main(vararg args: String) [
    val helloFileWriter = HelloFileWriter(Filesystem.SYSTEM)
    helloFileWriter.writeHellos(args.single())
]
```

이제 테스트에서는 가짜 객체를 제공한다. 

```kotlin
@Test
fun happyPath() {
    val filesystem = FakeFilesystem()
    val helloFileWriter = HelloFileWriter(filesystem)
    helloFileWriter.writeHellos("jesse")
    assertThat(filesystem.metadata("hello1M.txt".toPath()).size)
        .isEqualTo(20_888_888L)
}
```

### Testability Traps

거의 모든 Kotlin및 Java I/O코드가 실제 파일시스템에 대해서 테스트가 될 것으로 예상 된다. 파일 시스템에 대한 직접 호출을 시작하는 것은 쉬우며 실행 취소하는 것은 어색 하다. 

그러나 실제 파일 시스템에 대한 테스트는 매우 엉망이다. 

- 매우 느리다. 
- 테스트가 불안정해 진다. 테스트는 서로 격리되어 있지 않으며 동시 실행성은 떨어진다. 
- 개발 환경이 손상될 위험이 있다. 테스트가 천천히 하드 디스크를 채우는가? 아니면 빨리 이를 지우는가? 
- 오류 주입(fault injection)을 할 수 없다. 파일 쓰기가 실패했을 때 어떻게 되는지 테스트를 할 수 있는가? 
- 누출(leak)을 감지 할 수 없다. 열려있는 모든 파일이 닫혔는지 여부를 확인할 수 있는가? 

