### LegoLibrary 프로젝트 이슈  
#### 1. Retrofit   
- 기존에 사용하던 Volley보다 장점이라면 좀 더 빠르고 `Restful`한 API의 대응에 좋은거 같다. 하지만 내가 만든 php API는 Restful하지 못해서 문제. 단점이라면 내가 이해력이 딸려서 아직 제대로 장점을 더 확장해서 사용하지 못하는거 같다.      
- GSON의 파싱 문제. 이건 내가 잘못짠 서버 API의 문제라고 생각된다. 결국 `ResponseBody`를 직접 파싱하여 사용 하고 있지만 나중에 이 부분들도 다 수정 해야 겠다.  
- Retrofit의 Adapter클래스를 singleton으로 만들어서 사용했었는데 다른 서버 API를 사용하기 위해서 baseUrl과 API가 명시된 Interface의 class를 대응하기가 복잡 했음. 
- 그래서 일단 Adapter클래스는 그대로 두고 Builder클래스를 만들어서 static메소드를 두고 default paramter가 세팅된 인스턴스를 만들도록 설정. 
- 나중에 Builder패턴으로 구성해야 할거 같음..  

--- 
 
##### 기본적으로 세팅된 인스턴스를 만들어주는 static 메소드를 가진 Builder클래스.   
```java
public class DefaultParameterBuilder {
  public static final int DEFAULT_TIMEOUT_SEC       = 10;
  public static final int DEFAULT_WRITE_TIMEOUT_SEC = 10;
  public static final int DEFAULT_READ_TIMEOUT_SEC  = 10;

  public static OkHttpClient getHttpLoggingInterceptor() {
    HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
    interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

    return new OkHttpClient.Builder()
        .connectTimeout(DEFAULT_READ_TIMEOUT_SEC, TimeUnit.SECONDS)
        .writeTimeout(DEFAULT_WRITE_TIMEOUT_SEC, TimeUnit.SECONDS)
        .readTimeout(DEFAULT_READ_TIMEOUT_SEC, TimeUnit.SECONDS)
        .addInterceptor(interceptor)
        .build();
  }

  public static Retrofit getRetrofit(@NonNull OkHttpClient httpClient, @NonNull String baseUrl) {
    return new Retrofit.Builder()
        .client(httpClient)
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .build();
  }
}
```
---
  
##### 예제로 만들어본 Adapter클래스. 여기에서 API의 Interface와 baseUrl만 바뀐다.  
```java
public class RetrofitAdapter {
  private static NJobInterface netJobInterfaceInstance;

  public synchronized static NJobInterface getInstance() {
    if (netJobInterfaceInstance == null) {
      OkHttpClient httpClient = DefaultParameterBuilder.getHttpLoggingInterceptor();

      // retrofit configures
      Retrofit retrofit = DefaultParameterBuilder.getRetrofit(httpClient, ConstantParams.URL_PREFIX);
      netJobInterfaceInstance = retrofit.create(NJobInterface.class);
    }
    return netJobInterfaceInstance;
  }
}
```  
---
#### 2. Realm
- `RealmObject`를 상속한 data bean객체의 생성자에서 데이터를 초기화 해 주는 `init()`메소드를 콜 하고 있었는데 이 때문에 `Realm`에서 `io.realm.ProxyState.getRealm$realm()' on a null object reference`예외가 발생.  
- 그래서 해결 방법을 찾다 보니 구버전의 문서 에서 `RealmObject`를 상속하는 클래스에서는 비어있는 `public`생성자가 필요 한데 그 생성자의 내부 내용은 비어있어야 한다 라는 제한조건이 걸려 있었던 것. [참고](https://realm.io/kr/docs/java/latest/#section-12)  
- 생각지도 못한 제한 사항이라 일단 수정 하였음.
- 그리고 `Application`을 상속한 클래스의 `onCreate()`메소드에서 `Realm`의 설정을 하도록 추가 하였음. 
```java
public class LegoLibApplication
    extends Application {
  private Realm realm;
  @Override
  public void onCreate() {
    super.onCreate();

    RealmConfiguration config = new RealmConfiguration.Builder(this).build();
    //Realm.deleteRealm(config);
    Realm.setDefaultConfiguration(config);
  }
}
```
- `Fragment`의 라이프 사이클에 맞추어서 `onActivityCreated()`에서 `Realm`인스턴스를 생성 하고 `RealmChangeListner`를 등록 할 수 있게 했음. 
- 그리고 `onPause()`와 `onStop()`, `onResume()`등의 메소드를 통해서 `Realm`의 라이프사이클을 관리 할 수 있도록 부모 `Fragment`를 만들고 상속받아서 사용 하게 설정 함. 



