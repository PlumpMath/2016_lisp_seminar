# 클로저와 동시성

- 부제 : 클로저가 (자바 보다) 동시성 프로그래밍하기 좋은 점
- 자바병렬프로그래밍 (에이콘) - 브라이언 게츠 외 5명 지음 (강철구 옮김)
- 7가지 동시서 모델 (한빛미디어) - 폴 부처 지음 (임백준 옮김)

- 단일 연산 변수
- 스레드 풀

## 스레드 안전하지 않은 Counter 코드

```java
public final class Counter {
  private long count = 0;

  public long getValue() {
    return count;
  }

  public void increment() {
    ++count;
  }
}
```

- ++count는 저수준에서 읽기-수정하기-쓰기 연산으로 되어 있다.
```
getfiled #2
iconst_1
iadd
putfield #2
```

## 스레드 안전한 Counter 코드?

```java
public final class Counter {
  private long count = 0;

  public long getValue() {
    return count;
  }

  public synchronized void increment() {
    ++count;
  }
}
```

- 메모리 가시성 문제로 읽는 스레드도 동기화되어야 한다.

## 스레드 안전한 Counter 코드

```java
public final class Counter {
  private long count = 0;

  public synchronized long getValue() {
    return count;
  }

  public synchronized void increment() {
    ++count;
  }
}
```

- synchronized는 Tony Hoare가 고안한 Monitor라는 방식의 동기화 방법이다.
- 자바의 synchronized는 한번에 하나의 스레드가 접근 하기 위해 락을 사용한다.

## 락 방식의 문제점

- 스레드 경합이 없는 단일 스레드에서 락을 얻고 반납하는 일은 리소스가 들어가는 일이다.
- 스레드가 락을 얻지 못하면 대기 해야하며 대기하는 스레드가 많아지면 컨텍스트 스위칭에 들어가는 비용이 많아진다.
- volatile 변수는 메모리 가시성 문제는 해결해 주지만 복합 연산을 단일 연산으로 처리할 수 있는 기능은 없다.
- 락을 직접 사용하는 경우 데드락, 라이브락등 다양한 활동성(liveness) 문제가 일어나지 않도록 신경써야한다.
  (* 자바병렬프로그래밍 10장 활동성을 최대로 높이기 참조)

## 병렬 연산을 위한 하드웨어적인 지원

- 현대 프로세서는 CAS(Compare-and-swap) 연산을 제공한다.
- CAS 연산은 메모리 위치 / 기존 값 / 새 값을 하나의 명령어로 넘겨 하나의 연산으로 수행한다.
  - 메모리 위치의 값이 기존 값과 같은 경우에 새 값으로 변경한다.
- 스레드 간 경쟁이 없는 상태에서 CAS 연산은 락을 사용하는 연산 보다 2배 빠르다.
- 자바는 java.util.Concurrent.atomic 패키지에 AtomicXxx 클래스로 CAS 연산을 지원한다.

## Atomic 클래스를 사용한 논블럭킹 Counter

```java
public class CasCounter {
  private AtomicReference<Long> count = new AtomicReference<Long>(new Long(0));

  public long getValue() {
    return count.get();
  }

  public void increment() {
    for(;;) {
      long oldValue = count.get();
      long newValue = oldValue + 1;
      if(count.compareAndSet(oldValue, newValue)) {
        return;
      }
    }
  }
}
```

- AtomicReference는 자바 객체를 CAS 값으로 감싼다.
- compareAndSet 메서드는 기존 값과 새 값을 넣고 기존 값이 같을 때만 새로운 값으로 설정되고 true를 리턴한다.
- 경합이 발생하면 기존 값이 변경될 수 있고 그래서 새 값을 설정하지 못하면 재시도 한다.

## 락과 논블럭킹 성능 비교

- 자바 병렬프로그래밍 15장
- 경쟁이 많은 상황
  - 그림1
- 보통 수준의 경쟁 상황
  - 그림2

## 클로저의 atom을 이용한 논블럭킹 Counter

```clojure
(ns counter)

(def count (atom 0))

(defn get-value []
  @count)

(defn increment []
  (swap! count inc))
```

## atom의 내부

```clojure
;; clojure.core

(defn atom
  ([x] (new clojure.lang.Atom x))
  ...)

(defn swap!
  ([^clojure.lang.Atom atom f] (.swap atom f)))
```

```java
// clojure.lang.Atom

public Atom(Object state) {
  this.state = new AtomicReference(state);
}
```

```java
// clojure.lang.Atom

public Object swap(IFn f) {
  for(;;) {
		Object v = deref();
		Object newv = f.invoke(v);
		validate(newv);
		if(state.compareAndSet(v, newv)) {
			notifyWatches(v, newv);
			return newv;
    }
  }
}
```

## 클로저 스레드 만들기

```clojure
(.start (Thread. #(println "Hello, World!")))
```

- 클로저 함수는 Runnable을 구현하고 있다.

```java
// clojure.lang.IFn

public interface IFn extends Callable, Runnable { ... }
```

```java
// clojure.lang.AFn

public void run() {
  invoke();
}
```

## STM

- 두개의 값을 하나 처럼 바꾸기

```clojure
(def checking (ref 10000))
(def savings (ref 20000))

(defn transfer [from to amount]
  (dosync
    (alter from - amount)
    (alter to + amount)))

(defn stress-thread [from to iterations amount]
  (Thread. #(dotimes [_ iterations]
              (transfer from to amount))))

(defn start []
  (println "Before: checking=" @checking " Savings=" @savings)
  (let [t1 (stress-thread checking savings 100 100)
        t2 (stress-thread savings checking 200 100)]
    (.start t1)
    (.start t2)
    (.join t1)
    (.join t2)
    (println "After: checking=" @checking " Savings=" @savings)))
```

## STM의 스레드 경합 처리

- dosync를 벗어나는 순간 진입 시점과 원래 값이 다르다면 누군가 변경한 것으로 간주하고 재시도

```clojure
(def checking (ref 10000))
(def savings (ref 20000))
(def attempts (atom 0))

(defn transfer [from to amount]
  (dosync
    (swap! attempts inc)
    (alter from - amount)
    (alter to + amount)))

(defn stress-thread [from to iterations amount]
  (Thread. #(dotimes [_ iterations]
              (transfer from to amount))))

(defn start []
  (println "Before: checking=" @checking " Savings=" @savings)
  (let [t1 (stress-thread checking savings 100 100)
        t2 (stress-thread savings checking 200 100)]
    (.start t1)
    (.start t2)
    (.join t1)
    (.join t2)
    (println "Attempts:" @attempts)
    (println "After: checking=" @checking " Savings=" @savings)))
```

## 동기화 클래스 (synchronizer)

- 스레드와 스레드는 공유 데이터로 커뮤니케이션 할 수 있다.
- 한 스레드가 다른 스레드의 결과를 기다려야 한다면?
- 스레드 간의 작업 흐름을 조절 할 수 있도록 만들어 진 클래스를 동기화 클래스라고 한다.
- java.util.concurrent.CountDownLatch
- java.util.concurrent.FutureTask

## FutureTask

- 두 스레드가 각자의 계산을 한 후 합치는 예제
- 스레드 풀을 사용하기 때문에 쓰레드 생성 / 소멸에 대한 성능저하나 OutOfMemory에 대해 안전하다.

## 클로저 future

- 클로저의 future는 블럭킹 되지 않는다.
- 두 스레드가 각자의 계산을 한 후 합치는 예제

## 클로저 promise를 이용한 정교한 작업 흐름 제어

- CountDownLatch를 이용해서 값이 설정 될때 까지 블럭 킹
- 두 스레드가 서로 결과를 주고 받으며 최종 결과를 도출 하는 예제

## 다시 논블럭킹

- future와 promise는 블럭킹 방식을 사용해서 스레드 간 작업 흐름을 조절한다.
- 클로저의 core.async를 사용하면 논블럭킹 방식으로 작업 흐름을 조절 할 수 있다.

## core.async

- 채널
- go 블럭

## core.async를 이용한 넌블럭킹 작업 흐름 제어

## CSP

- 더 심오한 함수적 의미 Tony Hoare

## 다루지 않은 내용

- 함수형 프로그래밍의 병렬 처리 (pmap, reducer, transducer)
- 람다 아키텍처 라이브러리들 (netflix/pigpen)

## 결론

- 현대 프로그래밍 언어들은 효과적으로 동시성 프로그램을 지원하고 있다.
- 현대 프로그래밍 언어들을 사용해서 쉽게 동시성 프로그래밍을 하자.
- 클로저로 하면 더 좋다. :)
