### RxAndroid Issue  
#### 1. Rx의 LifeCycle 관리  
- 비동기로 작업 중인 Rx의 Subscribe는 Activity나 Fragment가 종료된 뒤에도 동작 하고 있기 때문에 Memory leak이 발생할 수 있다.  
- 메모리의 누수를 막기 위해서는 Rx의 LifeCycle을 Activity나 Fragment의 LifeCycle에 맞추어 동작하게 해주면 된다.  

