### RxAndroid Issue  
#### 1. Rx의 LifeCycle 관리  
- 비동기로 작업 중인 Rx의 Subscribe는 Activity나 Fragment가 종료된 뒤에도 동작 하고 있기 때문에 **Memory leak**이 발생할 수 있다. (만약 정상적으로 `onComplete()`나 `onError()`콜백으로 진입 하게 되면 알아서 `unsubscribe()`된다.)   
- 메모리의 누수를 막기 위해서는 Rx의 LifeCycle을 Activity나 Fragment의 LifeCycle에 맞추어 동작하게 해주면 된다. 하지만 어떻게? 해야 하는가 하는 고민이 생긴다.   
- RxJava에는 [CompositeSubscription](http://reactivex.io/RxJava/javadoc/rx/subscriptions/CompositeSubscription.html)이라는 클래스를 제공 한다. 이 클래스는 생성된 `Subscription`인스턴스를 하나로 관리 해주는 클래스 이다.  
- 다른 방법이 또 있다면, `Subscriber`클래스를 상속한 클래스를 만들고 `WeakReference`로 래핑 해서 사용 하는 것이다. ([참고](http://www.philosophicalhacker.com/2015/03/24/how-to-keep-your-rxjava-subscribers-from-leaking/))  
- `CompositeSubscription`클래스의 `remove()`메소드를 보면, `Removes a Subscription from this CompositeSubscription, and unsubscribes the Subscription.`이라고 되어 있다. 이 `CompositeSubscription`에 `add()`된 `Subscription`에 `remove()`메소드를 콜하면 `CompositeSubscripnt`에서 제거 되면 동시에 `unsubscribe`된다고 명시 되어 있다.  
- `CompositeSubscription`과 `remove()`메소드를 활용하여 이를 Activity나 Fragment의 라이프 사이클에 연동 해서 사용 하면 된다. 
- Activity나 Fragment등의 `onCreate()`메소드 에서는 멤버변수로 존재 하는 `CompositeSubscription`인스턴스를 생성 하는 기능이 들어 간다.  
- 어떠한 작업을 위해서 `subscribe()`를 하게 되면 `Subscription`인스턴스를 변수로 정의 한 다음 `CompositeSubscription`에 `add()`해 준다.  
- Activity나 Fragment등의 `onDestroy()`메소등에서는 생성된 인스턴스로 존재 하는 `CompositeSubscription`인스턴스를 `unsubscribe()`한다. Activity나 Fragment에서 생성되어지고 `add()`된 모든 Rx의 subscribe들은 이제 `unubcribe()`될 것 이다.  
```java
public class MainActivity
    extends Activity {
  private CompositeSubscription compositeSubscription;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    compositeSubscription = new CompositeSubscription();
    
    Observable<String> observable = Observable.create(
        (Observable.OnSubscribe<String>) subscriber -> {
          ...
        }
    );
    Subscription subscription = observable
        .observeOn(Schedulers.computation())
        .subscribeOn(AndroidSchedulers.mainThread())
        .subscribe(
            s -> {
              ...
            }
        );
    compositeSubscription.add(subscription);
  }

  @Override
  protected void onDestroy() {
    if (compositeSubscription != null) {
      compositeSubscription.unsubscribe();
    }
    super.onDestroy();
  }
}
```

----  
#### 2. MVP 패턴에서의 `CompositeSubscription`   
 - (1)의 방법에서는 Activity나 Fragment에 바로 `Subscription`을 생성하고 멤버변수로 `CompositeSubscription`을 생성하여 `add()`, `unsubscribe()`하는 것을 알 수 있다. 
 - 하지만, MVP패턴에서는 `Subscription`을 실제로 생성하고 비동기 작업을 요청 하는곳이 Activity나 Fragment가 아닌 **Presenter**에서 하게 된다. 
 - Presenter에 `CompositeSubscription`을 멤버로 두고 관리하게 하는건 무리가 없을것이다. 하지만 Activity나 Fragment의 Lifecycle에 맞추어 `Subscription`을 관리 하려 하는 목적에 어긋난다.
 - 아래 소스는 Presenter의 부모 클래스로서 `CompositeSubscription`에 `Subscriber`를 등록 하고 `destroy()`메소드를 액티비티나 프래그먼트의 라이프 사이클에 맞추어 콜 하는 그 예 이다. 
 ```java
 public class BasePresenter {
  private CompositeSubscription compositeSubscriptionl;

  public BasePresenter() {
    this.compositeSubscriptionl = new CompositeSubscription();
  }

  public <T> void addSubscriber(@NonNull Subscriber<T> subscriber) {
    if (compositeSubscriptionl != null) {
      compositeSubscriptionl.add(subscriber);
    }
  }

  public void destroy() {
    if (compositeSubscriptionl != null) {
      compositeSubscriptionl.unsubscribe();
    }
  }
}
```  
- Presenter의 부모 클래스를 만들고 `CompositeSubscription`의 멤버변수를 추가 한다.  
- 부모 클래스의 생성자에서는 `CompositeSubscription`의 인스턴스를 생성 한다.  
- 부모 클래스에는 만들게 될 `Subscriber`의 인스턴스를 `add()`하는 메소드와 lifecycle의 `onDestroy()`에 맞춰 `unsubscribe()`하는 메소드인 `destroy()`메소드를 추가 한다. 
- 앞으로 만들게 되는 모든 Presenter들은 부모 Presenter를 상속해서 만든다.  
- 그리고 Activity나 Fragment를 상속한 부모 클래스들을 또 만들고, `onCreate()`메소드 군 에서는 presenter의 인스턴스를 생성 한다.  
- 또한 `onDestroy()`에서는 presenter의 `destroy()`메소드를 꼭 호출 하여 생성된 모든 `Subscription`을 `unsubscribe()`하게 해 준다.  

----
#### 3. RxLifeCycle  
- 다 귀찮다면 그냥 Trello에서 개발 한 [RxLifecycle](https://github.com/trello/RxLifecycle)을 사용 하면 된다.  
