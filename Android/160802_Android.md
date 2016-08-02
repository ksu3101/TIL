### RxAndroid Lifecycle management  

#### 개요  
- `RxAndroid`를 사용 하면 내부에서 `Context`객체를 복사해서 가지고 있기 때문에 메모리 누수가 발생 한다. 이를 막기 위해서는 액티비티나 프래그먼트의 라이프 사이클과 구동하는 `Subscription`객체들을 `onDestory()`메소드 등에서 `unsubscribe()`하면 된다. 이는 [이 문서](https://github.com/ksu3101/TIL/blob/master/Android/160708_Android.md)를 참고 하면 된다. 
- 일반적인 상황에서는 액티비티 내부에서 `CompositeSubscription`객체를 두고 생성되는 `Subscription`을 bind 하고 `onDestroy()`메소드 에서 unbind해주면 된다. 하지만 MVP패턴을 사용 하는 경우에는 사용이 까다롭다. 
- 그래서 `Presenter`에서 부모 presenter인 [BasePresenter](https://github.com/ksu3101/NestedRecyclerView/blob/master/app/src/main/java/kr/swkang/nestedrecyclerview/utils/mvp/BasePresenter.java)를 두고 내부에서 `addSubscriber()`메소드를 콜하여 등록하는 `Subscription`을 bind하고 외부 view를 구현한 액티비티나 프래그먼트에서 `onDestroy()`할 경우 presenter의 `destroy()`메소드를 콜 하여 등록되어 대기 중인 `Subscription`을 unbind하도록 하였다. 
- 하지만 이는 매우 불편 하다. 매번 `Subscription`을 등록 할 때마다 presenter클래스에서 `addSubscriber()`메소드를 콜 해줘야 한다. 만약 개발자가 이를 빼먹게 되면 메모리 누수가 발생하는 중대한 결함이 될 것이다. 

#### 이제 어떻게 할 것인가? 
- 개발자가 최소한 손을 댈수 있도록 하고 알아서 액티비티나 프래그먼트의 라이프 사이클에 맞춰 동작 하게 하고 싶었다. 그래서 아래와 같이 생각해 보았다. 
 1. `Observable`을 상속받아서 `subscribe()`메소드를 콜 할때마다 알아서 내부에서 `CompositeSubscription`에 binding처리 하게 하기. 
 2. 어떤 클래스를 만들고 `Observable`인스턴스를 내부에 두고 래핑 해서 사용 하기. 이 클래스의 생성자에서는 `BasePresenter`를 무조건 받게 한다. 그리고 `subscribe()`메소드를 만들고 이 메소트를 콜해서 `Subscription`을 등록 시 내부에서 `addSubscriber()`를 사용해서 `CompositeSubscription`에 binding처리 해 준다. 
 
- 처음엔 (1)번을 해봤으나 내부 구조가 복잡하고 제네릭으로 얽혀있어 섣불리 건드릴 수 없었다. 시간적 여유도 없었고. 그래서 (2)를 시도 했는데 생각보다 간단하다. [SwObservable / 링크](https://github.com/ksu3101/NestedRecyclerView/blob/master/app/src/main/java/kr/swkang/nestedrecyclerview/utils/SwObservable.java)
```java
public class SwObservable {
  private BasePresenter presenter;
  private Observable    observable;

  public SwObservable(@NonNull BasePresenter p, @NonNull Observable o) {
    this.presenter = p;
    this.observable = o;
  }

  @SuppressWarnings("unchecked")
  public final void subscribe(@NonNull Subscriber subscriber) {
    if (presenter == null) {
      throw new NullPointerException("BasePresenter object is null.");
    }
    presenter.addSubscriber(subscriber);

    if (observable == null) {
      throw new NullPointerException("Observable object is null.");
    }
    observable.subscribe(subscriber);
  }
}
```

- 사용법은 다음과 같다. 
```java
public class MainActivityPresenter
    extends BasePresenter {
    
  public void retrieveSomeDatasMethod() {
    SwObservable observable = new SwObservable(
          this,       // extends `BasePresenter`
          Observable.create(
              new Observable.OnSubscribe<ArrayList<Model>>() {
                // ...
              }
          ).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
    );        
    
    observable.subscribe(
          new Subscriber<ArrayList<Model>>() {
            @Override
            public void onNext(ArrayList<Model> resultList) {
              // do something..
            }
          }
    );
  }
}  
```
- `SwObservable`인스턴스를 만들면서 `BasePresenter`를 상속한 `Presenter`자기 자신 인스턴스를 넘기고, 기존 `Observable`을 만들때와 동일 하게 `Observable.create`을 한다. 그리고 어떤 스레드에서 어떤 작업을할지 명시 해주는 것도 전과 동일 하다. 
- 그리고 만들어진 `SwObservable`인스턴스를 통해서 `subscribe()`를 한다. 내부에서는 알아서 `CompositeSubscription`에 binding해 줄 것이다. 

