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
 - `Realm`의 데이터가 변경 되었을때의 콜백 리스너, 비동기 트랜젝션, 멀티 스레딩을 지원 한다.
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
  
#### 1.2.2 단점 들  
1. **제약 조건**  
 - `Realm`에 저장될 모든 데이터들은 `RealmObject`를 상속 해야 한다.   
 - `2.0` 이전 버전에서는 객체의 기본 생성자를 지원하지 않았다. [참고](https://github.com/ksu3101/TIL/blob/master/Android/160705_Android.md#2-realm)

## 2. 사용 예제 

## 3.  
