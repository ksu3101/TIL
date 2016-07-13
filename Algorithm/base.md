### 1. 기본기
#### 1.1 아스키 코드
![최소한 A,Z,a,z정도는 기억해 둡시다](https://github.com/ksu3101/TIL/blob/master/Algorithm/Images/1275273992_asciitable.gif)

---

#### 1.2 자주 사용되는 코드 
##### 1.2.1 character to integer
 ```
char c ='1';
int number = Character.getNumericValue(c);
 ```
##### 1.2.2 숫자 역전 시키기 
```java
public static int reverse(int n) {
      int reverse = 0;
      while(n != 0) {
          reverse *= 10;
          reverse += (n % 10);
          n = n / 10;
      }
      return reverse;
  }
```

---
#### 1.3 Swap
```java
// 기본 스왑 
public void swap(int x, int y) {
  int tmp = x;
  x = y;
  y = tmp;
}

// XOR을 사용한 스왑 
// (피연산자의 수가 서로 다를 경우에만 1인 XOR의 성질을 이용해서 2진수로 풀어 계산 해보면 된다.)
public void swap(int x, int y) {
  x = x ^ y;
  y = x ^ y;
  x = x ^ y;
}
```
 - [참고 링크 1](https://betterexplained.com/articles/swap-two-variables-using-xor/)
 - [참고 링크 2](https://en.wikipedia.org/wiki/XOR_swap_algorithm)
 
---

### 2. 정렬
#### 2.1 Quick sort
- 시간 복잡도 : 최악 `O(n^2)`, 평균적으로 `O(n log n)`  
- 실행 설명  
  a. 리스트 가운데서 하나의 원소를 고른다. 이렇게 고른 원소를 피벗이라고 한다.  
  b. 피벗 앞에는 피벗보다 값이 작은 모든 원소들이 오고, 피벗 뒤에는 피벗보다 값이 큰 모든 원소들이 오도록 피벗을 기준으로 리스트를 둘로 나눈다. 이렇게 리스트를 둘로 나누는 것을 분할이라고 한다. 분할을 마친 뒤에 피벗은 더 이상 움직이지 않는다.  
  c. 분할된 두 개의 작은 리스트에 대해 재귀(Recursion)적으로 이 과정을 반복한다. 재귀는 리스트의 크기가 0이나 1이 될 때까지 반복된다.  
```java
    public static void quickSort(int[] arr, int low, int high) {
         if(low >= high) return;
         
         // pick the pivot
         int middle = low + (high - low) / 2;
         int pivot = arr[middle];
         
         // make left < pivot and right > pivot
         int i = low, j = high;
         while(i <= j) {
             while(arr[i] < pivot) {
                 i++;
             }
             while(arr[j] > pivot) {
                 j--;
             }
             if(i <= j) {
                 // swapping
                 int tmp = arr[i];
                 arr[i] = arr[j];
                 arr[k] = tmp;
                 i++;
                 j--;
             }
         }
         if(low < j) {
             quickSort(arr, low, j);
         }
         if(high > i) {
             quickSort(arr, i, high);
         }
     }
```
---
#### 2.2 Merge sort (병합, 합병 정렬)  
- 시간 복잡도 : `O(n long n)`
- 공간 복잡도 : `O(n)`  
- 실행 설명  
  a. 리스트의 길이가 0 또는 1이면 이미 정렬된 것으로 본다. 그렇지 않은 경우에는  
  b. 정렬되지 않은 리스트를 절반으로 잘라 비슷한 크기의 두 부분 리스트로 나눈다.  
  c. 각 부분 리스트를 재귀적으로 합병 정렬을 이용해 정렬한다.  
  d. 두 부분 리스트를 다시 하나의 정렬된 리스트로 합병한다.  

---
### 3. 탐색 
#### 3.1 BFS (너비 우선 탐색) 
- 너비 우선 탐색(Breadth-first search, BFS)은 맹목적 탐색방법의 하나로 시작 정점을 방문한 후 시작 정점에 인접한 모든 정점들을 우선 방문하는 방법이다. 더 이상 방문하지 않은 정점이 없을 때까지 방문하지 않은 모든 정점들에 대해서도 넓이 우선 검색을 적용한다. OPEN List 는 큐를 사용해야만 레벨 순서대로 접근이 가능하다.
- 장점
 - 출발노드에서 목표노드까지의 최단 길이 경로를 보장한다.
- 단점
 - 경로가 매우 길 경우에는 탐색 가지가 급격히 증가함에 따라 보다 많은 기억 공간을 필요로 하게 된다.
 - 해가 존재하지 않는다면 유한 그래프(finite graph)의 경우에는 모든 그래프를 탐색한 후에 실패로 끝난다.
 - 무한 그래프(infinite graph)의 경우에는 결코 해를 찾지도 못하고, 끝내지도 못한다.
 ```java
public static void bfs(int[][] mat, int startVertex) {
  Queue<Integer> queue = new LinkedList<>();
  boolean[] visited = new boolean[mat[startVertex].length];

  visited[startVertex] = true;
  queue.add(startVertex);

  int e = 0, i = 0;
  while(!queue.isEmpty()) {
    e = queue.remove();
    i = e;
    while(i <= mat[startVertex].length) {
      if(mat[e][i] == 1 && !visited[i]) {
        queue.add(i);
        visited[i] = true;
      }
      i++;
    }
  }
}
 ```
---
#### 3.2 DFS (깊이 우선 탐색)
- 맹목적 탐색방법의 하나로 탐색트리의 최근에 첨가된 노드를 선택하고, 이 노드에 적용 가능한 동작자 중 하나를 적용하여 트리에 다음 수준(level)의 한 개의 자식노드를 첨가하며, 첨가된 자식 노드가 목표노드일 때까지 앞의 자식 노드의 첨가 과정을 반복해 가는 방식이다.
- 장점
 - 단지 현 경로상의 노드들만을 기억하면 되므로 저장공간의 수요가 비교적 적다.
 - 목표노드가 깊은 단계에 있을 경우 해를 빨리 구할 수 있다.
- 단점
 - 해가 없는 경로에 깊이 빠질 가능성이 있다. 따라서 실제의 경우 미리 지정한 임의의 깊이까지만 탐색하고 목표노드를 발견하지 못하면 다음의 경로를 따라 탐색하는 방법이 유용할 수 있다.
 - 얻어진 해가 최단 경로가 된다는 보장이 없다. 이는 목표에 이르는 경로가 다수인 문제에 대해 깊이우선 탐색은 해에 다다르면 탐색을 끝내버리므로, 이때 얻어진 해는 최적이 아닐 수 있다는 의미이다.
 ```java
 int map[][], visit[];

void dfs(int vertexSize, int v) { 
  int i;
  visit[v] = 1; // root node
  for(i=1; i<=vertexSize; i++) {
    if(map[v][i] == 1 && !visit[i]) {
      dfs(i);
    }
  }
}
 ```
___
#### 3.3 Dijkstra algorithm
#### 3.4 소수 - 에라토스테네스의 체 
