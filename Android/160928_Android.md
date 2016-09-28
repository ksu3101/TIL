# Realm java 2.0 

이 글은 Realm Java(안드로이드) 환경을 기반을 작성 되었다. 그러므로 여기에서 나오는 개발자 경험과 소스들은 Java(안드로이드)를 기준으로 작성 되어 있는 것 이다. 

## 1. Realm ?

### 1.1 Local database 
안드로이드 내부 Local database로 선택하는 것 은 `SQLite`였었다. 개인적으로 DB를 사용함에 있어 `DB-테이블` 구조를 설계 하는 시간이나 쿼리를 작성하는 시간들이 아깝게 느껴졌던건 사실이다. 개인적으로 SQLite를 기반으로 로컬 데이터들을 관리 해 왔었지만 그것만으로는 매우 부족했다. 

그래서 보통 `SQLite`기반의 Local database API들을 사용 한다. [GreenDao](https://github.com/greenrobot/greenDAO)같은 ORM프레임 워크 를 사용 하거나 다른 API들을 사용 한다. 그러던 중 얼마전 부터 주목받기 시작한 `Realm`에 대해서 알게 되었다.  

### 1.2  Realm  Mobile Database  
최근 Mobile database API중 가장 핫 한 [Realm](https://realm.io/)이 업데이트를 단행 했다. `1.2`버전에서 자그만치 `2.0`으로 업데이트를 진행 했다.

직접 사용해보면서 느낀 점과 잘 알려진 장,단점을 기반으로 왜 `Realm`이 핫 해졌고 최근 Local database로 선택되어지는지 살펴 보았다. 

#### 1.2.1 장점 들
1. **성능** 
 - 다른 `SQLite`기반 API보다 빠른 속도를 장점으로 내세우고 있다. 특히 많은 수의 데이터를 로컬에 저장 하고 CRUD를 해야 할때 빠른 속도를 자랑 한다.
 - [Realm에서 제공하는 Benchmark 결과](https://realm.io/news/realm-for-android/)
 - [Realm java의 벤치마크 소스 및 결과](https://github.com/realm/realm-java-benchmarks)
 - [Realm vs snappydb(SQLite)](https://medium.com/@hesam.kamalan/database-benchmark-realm-vs-snappydb-f4b89711f424#.9y5ctvj58)  

2. **접근, 사용성**   

 ```java
// Query Realm for all dogs younger than 2 years old
final RealmResults<Dog> puppies = realm.where(Dog.class).lessThan("age", 2).findAll();
puppies.size(); // => 0 because no dogs have been added to the Realm yet

 // Persist your data in a transaction
realm.beginTransaction();
final Dog managedDog = realm.copyToRealm(dog); // Persist unmanaged objects
Person person = realm.createObject(Person.class); // Create managed objects directly
person.getDogs().add(managedDog);
realm.commitTransaction();

// Listeners will be notified when data changes
puppies.addChangeListener(new RealmChangeListener<RealmResults<Dog>>() {
  @Override
  public void onChange(RealmResults<Dog> results) {
    puppies.size(); // => 1
  }
});
 ```   
 - `SQLite`을 사용 하게 되면 테이블의 관계를 기반 하여 스키마를 구성 하고 조인등 쿼리를 사용 해야 한다. 객체를 기반으로 한 Database가 아니어서 불편하게 느껴진다. 간단한 데이터를 넣더라도 Database와 Table, 그리고 내부 컬럼들과 데이터 양식등 까지 모두 정의해주고 쿼리를 작성하여 CRUD를 해야 한다. 이것은 시간 낭비다.   
하지만 `Realm`에서는 그럴 필요가 없다. **객체 기반 데이터베이스**로서 `RealmObject`를 상속한 데이터 모델을 기반으로 CRUD를 한다. 또한 테이블 간의 관계를 조인 쿼리가 아니라 `has`등의 문법을 통해서 표현 한다. 이는 설계에 들어가는 시간, 쿼리를 작성 하는 시간을 크게 감축 시켜 준 다.
 - `Realm`의 데이터가 변경 되었을때의 콜백 리스너, 비동기 트랜젝션을 지원 한다.
 - `MVCC`(MultiVersion Concurrency Contrl)데이터 베이스로서 **원자성, 일관성, 고립성, 지속성**을 제대로 지원 한다. 멀티 스레딩 환경에서 데이터의 I/O에 대해서 고민할 필요가 많이 줄어 든 다.
 - Java외 에도 Object-c등 다른 플랫폼들을 지원 한다.   
 - `Rx`또한 지원 한다.  
  ```java
// Combining Realm, Retrofit and RxJava (Using Retrolambda syntax for brevity)
// Load all persons and merge them with their latest stats from GitHub (if they have any)
Realm realm = Realm.getDefaultInstance();
GitHubService api = retrofit.create(GitHubService.class);
realm.where(Person.class).isNotNull("username").findAllAsync().asObservable()
    .filter(persons.isLoaded)
    .flatMap(persons -> Observable.from(persons))
    .flatMap(person -> api.user(person.getGithubUserName())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(user -> showUser(user));
  ```

3. 다른 장점들 
 - 빠른 업데이트 : [Realm](https://realm.io/)사이트에서의 업데이트는 지속적인 업데이트를 해왔었다. 그 예로 최근 큰 이슈가 된 `Rx`의 지원 에 따른 업데이트 들이 있겠다. 이는 기민한 이슈, 버그 및 개선사항들의 업데이트가 빠르게 진행 되어 왔다는 것 이다.     
 - 문서들 : 한글화 된 문서들이 존재하는 페이지가 있으며, 문서의 수준도 높은 편 이다. `Realm`의 study cost가 높기는 하지만 그런 단점들을 상쇄시켜줄 문서들이 존재 한다. 
  
#### 1.2.2 단점 들  
1. **제약 조건**  
 - `Realm`에 저장될 모든 데이터들은 `RealmObject`를 상속 해야 한다.
 - model의 `final`, `transient`, `volatile`를 지원 하지 않는다.   
 - `2.0` 이전 버전에서는 객체의 기본 생성자를 지원하지 않았다. [참고](https://github.com/ksu3101/TIL/blob/master/Android/160705_Android.md#2-realm)  

## 2. *2.0* 버전에서의 변경 사항
### 2.1  [Realm Mobile Platform](https://realm.io/kr/news/realm-java-2-0-mobile-platform-support/)
`Realm`이 2.0으로 버전업 하면서 모바일 플랫폼을 [지원](https://realm.io/news/introducing-realm-mobile-platform/)하기 시작 했다.   

![Realm mobile platform](https://images.contentful.com/emmiduwd41v7/4BZWBXxaP6oA2OKYymCwiW/74d169bec3e0774dc00b0f723a6c88a0/Screen_Shot_2016-09-26_at_8.56.48_PM.png)  
Realm을 사용 하는 서로 다른 모바일 플랫폼에서의 동기화를 제공 하는 플랫폼 이다. 구글의 파이어베이스가 생각 나는 녀석이기도 하다.   

로컬의 Realm에 저장된 어떠한 값이 변경 되었을 경우 `syncEnabled`가 `true`인 상태 라면, 변경되어진 값을 모든 등록된 장비에서 동기화 하는 형태로 보인다. 다른 사용자와의 연결된 상태에서 작업 하는 채팅이나 게임 등에서 큰 역활을 할 수 있을것으로 기대 된다.   
게다가 다른 API플랫폼을 가져와서 사용 하는게 아닌 `Realm`을 그대로 사용 하고 있는 상태에서 하고 있다는 것은 큰 메리트로 보인다.  

다만 아직 이 문서 작성날짜를 기반으로 `Realm Mobile Platform`의 지원은 베타이다. 아직은 조금 더 기다려 볼 필요도 있을 거 같다. 
 - `Realm`상의 모든 베타 API는 `@Beta`어노테이션이 지정되어있으니 유심 할 것.   

### 2.2 Realm 2.0
#### 2.2.1 전역 초기화
`Context`의 인스턴스를 초기화 할때에만 전역으로 초기화를 하게 한다. 초기화를 하는 곳은 `Application`을 상속한 클래스의 `onCreate()`에서 하는 것을 추천 한다.  
 ```java
public class MyApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    Realm.init(this);
    // 다른 초기화 루틴
  }
}
 ```   
그리고 다른 API에서 `Context`인스턴스를 받지 않게 했다. 이는 기존 인스턴스와 나중에 받게 되는 인스턴스가 달라서 생길 수 있는 문제(특히 라이프사이클 동기화)를 방지 할 수 있다.   

#### 2.2.2 기본 값 
`RealmObject`를 상속한 데이터 모델은 생성자를 통한 데이터의 초기 값 세팅을 지원하지 않았다. 하지만 2.1 부터는 데이터 모델 객체를 인스턴스화 할때 기본값을 설정 할 수 있다. 기본 필드에 값을 바로 지정 하거나, 생성자를 통해서 기본 초기 값을 지정 할 수 있는 것 이다.  

#### 2.3.3 `RealmLog`
`Realm`에서 로그를 지원하는 클래스를 제공 한다. 
```java
RealmLog.clear(); // 모든 로거를 제거합니다
RealmLog.add(new AndroidLogger(Log.DEBUG)); // 커스텀 로그를 설정합니다
```
  
#### 2.3.3 기본키들과 객체  
여러 단말의 객체를 통합하기 위해 객체의 기본 키에 대한 새로운 두가지 제약이 추가 되었다고 한다. 
- 기본 키는 객체를 생성할 때 제공 되어야 한다. 
- 한번 설정된 기본 키는 바꿀 수 없다. 
이는 조금 더 살펴 보아야 할 거 같다. 



