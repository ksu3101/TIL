## Firebase Realtime Database Basic

Google에서 제공하는 NoSQL 클라우드 데이터베이스, Firebase의 Realtime Database를 사용 하는 예제를 정리 하였음. 

- 이 예제에 앞서 Firebase Console에서 Realtime database에 대해 기본 규칙을 설정 해야 함. 
  - 보안 규칙에 대해서는 [이 문서](https://firebase.google.com/docs/database/security?hl=ko)를 참고 할 것. 

### 1. `build.gradle`에 의존 추가

FB의 RTDB를 사용하기에 앞서 모듈을 추가 한다. 

```gradle
dependencies {
    // Firebase 플랫폼 BoM의 설정 
    implementation platform('com.google.firebase:firebase-bom:26.2.0')

    // Realtime database 라이브러리의 의존의 추가
    // BoM을 사용하고 있을 경우, 라이브러리의 버전을 명시하지 않아도 된다.
    implementation 'com.google.firebase:firebase-database'

    // BoM을 사용하지 않을 경우 아래처럼 라이브러리 버전과 함께 의존을 추가 해야 한다. 
    implementation 'com.google.firebase:firebase-database:19.6.0'
}
```

### 2. DB에 저장될 데이터의 구조

FB의 RTDB에 사용 되는 데이터 구조는 테이블이나 레코드가 없는 JSON과 같은 형태이다. 이번 예제인 안드로이드에서 사용가능한 데이터 타입은 아래와 같다. 

- `String`
- `Long`
- `Double`
- `Boolean`
- `Map<String, Object>`
- `List<Object>`
- 커스텀 객체의 경우 매개변수를 받지 않는 기본 생성자, 각 속성의 getter가 필요. 

데이터 객체의 경우 아래처럼 사용할 수 있다. 

```kotlin
@IgnoreExtraProperties
data class User(
    var userName: String? = "",
    var email: String? = ""
)
```

### 3. 기본 사용 방법

데이터베이스에 데이터를 읽거나 쓰기 위해서는 `DatabaseReference`인스턴스가 필요 하다. 

```kotlin
private lateinit var database: DatabaseReference
// ...
database = Firebase.database.reference
```

데이터베이스에 저장할 데이터의 JSON트리에 따라 데이터를 사용 하게 되는데 예를 들어 위 `User`데이터 클래스의 경우에는 아래 처럼 저장 될 수 있다. 

```json
{
  "users": {
    "alovelace": {
      "name": "Ada Lovelace",
      "contacts": { "ghopper": true },
    },
    "ghopper": { ... },
    "eclarke": { ... }
  }
}
```

### 4. 데이터의 읽기/쓰기

#### 4.1 데이터 쓰기

데이터베이스에 데이털르 저장 하려면 `setValue()`를 사용 하여 저장 하면 된다. 

```kotlin
val user = User(name, email)
database.child("users").child(userId).setValue(user)
```

위 예제의 경우 `users`라는 노드에 하위 노드로 `userId`로 키를 가진 `User()`객체를 서브 노드로 저장 하게 된다. 

위 예제의 `setValue()`를 하게 되면 동일한 `userId`에 대해 `setValue()`될 값을 덮어쓰게 된다. 만약 이 값을 모두 덮어쓰는게 아니라 일부 필드만 갱신하고 싶다면 아래 처럼 하면 된다. 

```kotlin
val newName = "Rick"
database.child("users").child(userId).child("userName").setValue(newName)
```

#### 4.2 데이터 읽기

데이터를 읽기 위해서는 `addValueEventListener()`또는 `addListenerForSingleValueEvent()`두 메소드를 이용 하여 `ValueEventListener`을 구현 하면 된다. 

```kotlin
val postListener = object: ValueEventListener {
    override fun onDataChange(dataSnapshot: DataSnapshot) {
        // post 객체에서 필요한 값을 얻고 뷰를 업데이트 한다. 
        val post = dtaSnapshot.getValue<Post>()        
    }

    override fun onCancelled(databaseError: DatabaseError) {
        Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
    }
}
postReference.addValueEventListener(postListener)
```

위 방법과 달리 단 한번만 호출 하고 더이상 하지 않는 작업이 있을 수 있다. 이 경우에는 `addListenerForSingleValueEvent()`을 구현하면 되며 한번 콜백이 호출되면 다시 호출 되지 않는다. 

#### 4.3 데이터 지우기

데이터를 삭제 하는 방법은 해당 데이터의 위치 참조에 대해 `removeValue()`를 사용 하면 된다. 혹은 `setValue()`나 `updateChilderen()`의 데이터를 `null`로 하여 삭제할 수도 있다. 

#### 4.4 완료 콜백의 추가 

데이터가 커밋(commit)되는 시점에 다른 후처리를 하려면 완료 리스너를 추가 하면 된다. 

```kotlin
database.child("users").child(userId).setValue(user)
    .addOnSuccessListener {
        // 데이터베이스의 작업 성공시 콜백
    }
    .addOnFailureListener {
        // 데이터베이스에 작업 시도 중 실패 
    }
```