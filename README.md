# 클로저와 동시성

## pmap

## Ref

## Agent

## Atom

- CAS
- java.util.concurrent.atomic.AtomicReference
- lock vs CAS 성능 비교

## future & promise

## core.async

- 비동기
- CPS
- CSP vs Callbcak 형태 비교

## pigpen
=======
# 2016 Lisp 세미나 - 클로저와 동시성

## 개요

클로저에서 병렬 처리시 데이터 안정성을 보장하기 위해 ref, agent, atom 등 다양한 기능을 제공한다.
이 중 가장 많이 사용하는 atom에 대해 알아본다. 또 Go 언어로 유명해진 CSP(Communicating sequential processes)의
클로저 구현 라이브러리인 core.async에 대해 알아본다.

## Concurrency와 Parallelism

- Concurrency는 각각 다른 흐름을 갖는 여러개의 작업 (실제 같은 시간이 동작하지 않아도 됨)
- Parallelism은 같은 시간에 작업이 실행되는 것

## 쓰레드를 이용한 클로저의 병렬 처리 예

- 여러 쓰레드가 각각의 카운트를 하는 예

## 쓰레드간 데이터 공유

- atom을 이용해 카운트에 값을 증가 시킴

## 안전하지 않은 Counter 자바 예제

- 동기화 되지 않은 increase와 getCount 메서드

## synchronized (모니터)를 이용한 락 방식 동기화

- increase와 getCount에 synchronized 적용

## 단일 연산 변수(non-blocking)를 이용한 동기화

- CPU의 CAS 연산 소개 https://en.wikipedia.org/wiki/Compare-and-swap
- java.util.Concurrent.AtomicReference 소개 http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/atomic/AtomicReference.html
- non-blocking 방식의 Counter 예제

## 락 방식과 단일 연산 변수 사용의 성능 비교

- 자바병렬프로그래밍에 있는 성능 비교 자료 인용

http://www.studfiles.ru/preview/1584090/page:26/

## 단순한 clojure.lang.Atom.java 코드 설명

- Atom.java 코드 설명

https://github.com/clojure/clojure/blob/master/src/jvm/clojure/lang/Atom.java

## 쓰레드를 이용하지 않는 병렬처리

- CSP 간단한 소개
- core.async 소개

## channel

- 채널 소개
- 채널 예제

## go 블럭

- go 블럭 소개
- go 블럭 예제
- 쓰레드 보다 값싼 go 블럭

## 병렬 처리 작업와 I/O

- non-blocking i/o vs blocking 쓰레드
- 단순 쓰레드 갯수 현대 리눅스는 쓰레드를 많이 생성할 수 있다 - 비동기 의미가 있는가?
- 논의 http://stackoverflow.com/questions/8546273/is-non-blocking-i-o-really-faster-than-multi-threaded-blocking-i-o-how
- 비교 자료

## 콜백 보다 더 보기 좋은 방식

- http.kit를 이용한 순차 콜백 처리 예제
- core.async로 바꿔본 예제

## 웹에는 사용할 수 있는가?

- ring 스팩에 handler(Servlet)가 비동기를 제공하지 않기 때문에 의미 없음
- ring과 http.kit non-blocking 함수를 이용한 예제의 무의미함

## ring 1.6.0 소개

- ring 프로젝트에 진행중인 비동기 ring spec 소개 (Servlet 3.0 스팩 적용)
- 비동기 ring 예제
- 비동기 스타일 ring과 http.kit, core.aync를 활용한 예제

https://groups.google.com/forum/#!topic/ring-clojure/58CkHMiJZtg

## 아쉽지만 다루지 못한 내용

- ref, agent
- pmap, p시리즈 함수들
- future, promise
- netflix pigpen

# 클로저와 동시성

## 독립적인 동시 코드

- 동시에 여러개의 파일에서 단어 수를 세서 각각의 파일로 출력하는 예제
- 클로저는 자바를 사용할 수 있기 때문에 Thread 를 설명한다.
  - 클로저의 함수는 Runnable을 구현하고 있기 때문에 Thread 생성자의 인자로 넘길 수 있다.

## 동시 코드 간 커뮤니케이션

- 하나의 일을 빠르게 수행하기 위해 동시에 처리하면 좋을 때가 있다. 이때는 하나의 일을 하기 위해 동시 코드 간에
  커뮤니케이션이 필요하다.

- 여러가지 커뮤니케이션 방법이 있지만 대체로 공유 데이터를 이용해서 커뮤니케이션을 한다.

## 공유 데이터를 이용한 커뮤니케이션

- 클로저 데이터 타입은 기본적으로 변경 불가능 하기 때문에 동시 코드 간에 데이터를 변경하면서 커뮤니케이션 하는데
  적합하지 않다.

- 클로저에 가장 기본적인 Var는 동적으로 다시 바인딩 될 수 있는데 동적 바인딩으로 커뮤니케이션이 가능하다.
  - 여러 스레드에서 카운트 하는 예제
  - Var를 변경하는 alter-var-root! 는 lock 방식으로 동작하기 때문에 쓰레드 경합에 안전하다.
  - Var 데이터는 volatile 이기 때문에 메모리 가시성 문제가 생기지 않는다.
  - 하지만 적합하지 않다 (왜 인지는 좀 더 찾아봐야 함)

## non-blocking 방식

- 스레드 경합 문제는 lock 방식이 아닌 CAS 방식과 retry 해결 할 수 도 있다.
- java.concurrent.AtomXXX 클래스는 CAS 오퍼레이션을 지원원한다.
  - java 카운트 예제
- 클로저는 java.conconrrent.AtomReference와 retry 로직을 wrapping 한 atom 함수를 제공한다.
  - atom 코드
- atom 을 이용한 카운트 예제

## 결과만 취합하려면
- A B 합쳐서 C 결과를 도출하는 그림 추가
- java.concurrent.FutureTask 는 리턴 값을 전달할 수 있는 스레드
- 단어수 세기 예제

## 작업 흐름이 제어가 필요할 때
- A B 결과인 C와 D의 결과를 합쳐서 결과를 내야할 때 D가 끝나도 C 결과를 기다려야한다.
  - 예제를 고민해봐야한다.
- C가 준비되기를 기다리기 위한 Blocking
  - 카운트 다운 린치를 이용한 방식
    - 카운트 다운 린치를 이용한 자바 예제
    - Future 를 이용한 예제
    - clojure future를 이용한 예제

## 좀 더 복잡한 흐름 제어
- promise 를 이용한 동시 작업 간 커뮤니케이션
  - 예제

## core.async
- CPS 설명
- 쓰레드  
