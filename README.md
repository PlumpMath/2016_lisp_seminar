# 2016 Lisp 세미나

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

- CPU의 CAS 연산 소개
- java.util.Concurrent.AtomicReference 소개
- non-blocking 방식의 Counter 예제

## 락 방식과 단일 연산 변수 사용의 성능 비교

- 자바병렬프로그래밍에 있는 성능 비교 자료 인용

## 단순한 clojure.lang.Atom.java 코드 설명

- Atom.java 코드 설명

## 쓰레드를 이용하지 않는 병렬처리

- CSP 간단한 소개
- core.async 소개

## channel

- 채널 소개
- 채널 예제

## go 블럭

- go 블럭 소개
- go 블럭 예제

## 콜백 보다 더 보기 좋은 방식

- http.kit를 이용한 순차 콜백 처리 예제
- core.async로 바꿔본 예제

## non-blocking i/o vs 쓰레드

- 현대 리눅스는 쓰레드를 많이 생성할 수 있다 - 비동기 의미가 있는가?
- 비교 자료

## 웹에는 사용할 수 있는가?

- ring 스팩에 handler(Servlet)가 비동기를 제공하지 않기 때문에 의미 없음
- ring과 http.kit non-blocking 함수를 이용한 예제의 무의미함

## ring 1.6.0 소개

- ring 프로젝트에 진행중인 비동기 ring spec 소개 (Servlet 3.0 스팩 적용)
- 비동기 ring 예제
- 비동기 스타일 ring과 http.kit, core.aync를 활용한 예제

## 아쉽지만 다루지 못한 내용

- ref, agent
- pmap, p시리즈 함수들
- future, promise
- netflix pigpen
