# Data object mapper

코틀린의 immutable data class 를 사용 하다 보면 치명적인 실수를 할 수 있다. 예를 들면 다음의 예제 data class를 보도록 하자. 

```kotlin
data class Products(
    val id: Long,
    val name: String,
    val desc: String,
    val price: Long
)
```

위 data class 는 딱히 문제는 보이지 않는다. 하지만 다음과 같이 이미 생성된 data class 인스턴스를 새로 만들 때 치명적인 문제가 발생할 수 있다. 

```kotlin
val products = Products(101L, "상품이름", "상품정보", 15000L)

fun createUpdatedPriceOfProducts(oldProducts: Products, newPrice: Long): Products {
    return Products(
        oldProducts.id,
        oldProducts.desc,   // human error!! 원래 여긴 name
        oldProducts.name,   // 원래 여긴 desc
        newPrice
    )
}
```

기존에 인스턴스로 갖고 있던 `products` 는 immutable data class 이다. 이 인스턴스 내 의 정보를 업데이트 하기 위해서는 새로운 인스턴스를 생성 해서 필요한 값을 변경해야 한다. 

그래서 함수를 통해서 새로운 인스턴스를 만드려 하는데 위 처럼 각 필드 패러미터를 잘못 적을 수 있다. 컴파일러에서는 둘다 같은 type 인 `String` 이기 때문에 아무런 컴파일 오류도 발생하지 않는다. 

문제는 이런 휴먼 에러가 생각보다 코틀린에서 발생할 가능성이 높다는 것 이다. 위의 예제는 그나마 `desc`, `name` 이라는 서로 다른 글자로 시작 되기 때문에 자동 완성 될 때 헷갈리는 경우는 없지만 만약 같은 글자로 시작 하는데다가 비슷한 이름이며 같은 타입일 경우 게다가 코드 리뷰 때 검출되지 못한 경우 라면 이 문제가 무조건 발생 할 수 없는 크리티컬한 코드가 실제로 배포 될 수 있는 것 이다. 

## 해결방법들

### 1. named argument 를 이용한 데이터 삽입 

가장 간단하면서 쉬운 방법이며 어떻게 보면 가장 추천 할 수 있는 방법이다. 

```kotlin
val products = Products(101L, "상품이름", "상품정보", 15000L)

fun createUpdatedPriceOfProducts(oldProducts: Products, newPrice: Long): Products {
    return Products(
        id = oldProducts.id,
        desc = oldProducts.desc,
        name = oldProducts.name,
        price = newPrice
    )
}
```

named argument 를 각각 작성 하면 입력 순서에 상관없이 named 정의된 패러미터에 알아서 값이 설정 되기 때문에 가장 심플 하면서 다른 수작업 이 존재 하지 않는 다. 
다만 named argument 를 무조건 항상 작성 해 주어야 하는 것 과 이 named argument 또한 잘못 작성하면 같은 휴먼 에러가 발생 할 수 있다. 
하지만 이 이슈를 해결할 수 있는 방법중 가장 빠르게 대응 할 수는 있다. 

그렇다면 다른 방법으로 이 휴먼에러를 대비한 시스템적인 이슈 대응 을 할 수 있을순 없을까? 

### 2. reflection 을 이용 한 paramenter name 비교 

이 방법은 리플렉션을 이용 하여 패러미터의 이름을 비교 하고 문제가 있을 경우 예외를 내뱉는 방법이다. 하지만 리플렉션 자체 가 성능에 영향을 크게 주기에 이 해결방법은 논외로 하려한다. 

### 3. map 을 이용한 data mapping

kotlin 의 `by` 키워드를 이용 하여 data class 에 map 을 사용 하여 패러미터를 유니크 한 패러미터 이름을 통해 저장 하는 방식이다. 

```kotlin
data class Products(val map: Map<String, Any>) {
    val id: Long by map
    val name: String by map
    val desc: String by map
    val price: Long by map
}

fun createUpdatedPriceOfProducts(oldProducts: Products, newPrice: Long): Products {
    return Products(
        mapOf(
            "id" to oldProducts.id,
        	"desc" to oldProducts.desc,
        	"name" to oldProducts.name,
        	"price" to newPrice
        )        
    )
}

fun main() {
    val products = Products(mapOf("id" to 101L, "name" to "상품이름", "desc" to "상품정보", "price" to 15000L))
    val newProducts = createUpdatedPriceOfProducts(products, 200L)
    print(newProducts.price)
}
```

immutable map 으로 생성된 map 에 패러미터의 이름으로 key, 패러미터의 값이 value 가 되어 map 에 저장 되는 형태이다. map 의 특성으로 인하여 패러미터에 대한 값의 저장시 휴먼 에러를 어느정도 방지 하기는 하지만 map 인스턴스를 만들어야 하는 과정이 있고 위 처럼 사용할 바 에는 그냥 named argument 가 나을 것 이다. 

## 결론

결국 위 크리티컬할 수도 있는 data class 에 대한 이슈는 역시 휴먼 에러에서 비롯 된다. 대부분의 에러가 휴먼에러인 이상 시스템적으로 보완하기에는 어려운 것 도 사실이다.
그렇기 때문에 유닛 테스트 코드를 작성 하고 assertion value 를 하여 정상적으로 data mapping 이 되었는지 확인 할 수 있어야 한다. 

위 방법중 가장 좋은 방법은 일단 named argument 를 사용 하고 또한 mutable data 에 대하여 mapping 하는 함수에 대한 유닛테스트 코드를 만들어 값의 유효성을 검사 하는 과정이 필요 할 것 이라고 생각 된다. 이는 최대한 휴먼 에러를 코드 배포 전에 막는 방법이 되는 것 이지 휴먼 에러에 대하여 자동으로 검증 하고 확인 시켜주는 건 아니긴 하다. (CI 에 물려서 apk 빌드 및 배포를 하지 못하게 하면 되긴 하다.)

하지만 이러한 data mapping 에 대하여 함수의 생성, 테스트 코드의 제작을 시스템 적으로 어떻게 할 수 있는 방법도 찾아보는것도 나쁘지 않을 것 같다. 
