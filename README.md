# ex_query_dsl
## 1. 프로젝트 생성
- java 21
- springboot 3.x
- 의존성
  - Spring Web, jpa, h2, lombok
- gradle
```
    plugins {
  id 'java'
  id 'org.springframework.boot' version '3.4.0'
  id 'io.spring.dependency-management' version '1.1.6'
  }

  group = 'study'
  version = '0.0.1-SNAPSHOT'

  java {
  toolchain {
  languageVersion = JavaLanguageVersion.of(21)
  }
  }
  
  configurations {
  compileOnly {
  extendsFrom annotationProcessor
  }
  }
  
  repositories {
  mavenCentral()
  }
  
  dependencies {
  implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
  implementation 'org.springframework.boot:spring-boot-starter-web'
  compileOnly 'org.projectlombok:lombok'
  runtimeOnly 'com.h2database:h2'
  annotationProcessor 'org.projectlombok:lombok'
  testImplementation 'org.springframework.boot:spring-boot-starter-test'
  testRuntimeOnly 'org.junit.platform:junit-platform-launcher'

    //test 롬복 사용
    testCompileOnly 'org.projectlombok:lombok'
    testAnnotationProcessor 'org.projectlombok:lombok'
	
    //Querydsl 추가
    implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'
    annotationProcessor "com.querydsl:querydsl-apt:${dependencyManagement.importedProperties['querydsl.version']}:jakarta"
    annotationProcessor "jakarta.annotation:jakarta.annotation-api"
    annotationProcessor "jakarta.persistence:jakarta.persistence-api"
  }
  
  // gradle clean 시에 QClass 디렉토리 삭제
  clean {
  delete file('src/main/generated')
  }
  
  
  tasks.named('test') {
  useJUnitPlatform()
  }
```
- yml
```
spring:
    datasource:
        url: jdbc:h2:tcp://localhost/~/ex_query_dsl
        username: sa
        password:
        driver-class-name: org.h2.Driver

    jpa:
      hibernate:
        ddl-auto: create
      properties:
        hibernate:
          show_sql: true
          format_sql: true
          use_sql_comments: true #querydsl로 작성하면 jqpl을 볼 수 없는데, 이걸 작성하면 볼 수 있음
logging.level:
    org.hibernate.SQL: debug
    org.hibernate.type: trace
```

## 2. 링크
- [강의 보러가기](https://www.inflearn.com/course/querydsl-%EC%8B%A4%EC%A0%84/dashboard)   
- [코드 보러가기 - Basic](https://github.com/izzy80/ex_query_dsl/blob/main/src/test/java/study/ex_query_dsl/QuerydslBasicTest.java)
- [코드 보러가기 - middle](https://github.com/izzy80/ex_query_dsl/blob/main/src/test/java/study/ex_query_dsl/QuerydslMiddleTest.java)
- [코드 보러가기 - 실무 활용 : 순수 JPA와 Querydsl](https://github.com/izzy80/ex_query_dsl/blob/main/src/test/java/study/ex_query_dsl/repository/MemberJpaRepositoryTest.java)
- [코드 보러가기 - 실무 활용 : 스프링 데이터 JPA와 Querydsl](https://github.com/izzy80/ex_query_dsl/blob/main/src/test/java/study/ex_query_dsl/repository/MemberRepositoryTest.java)
- [코드 보러가기 - 스프링 데이터 JPA가 제공하는 Querydsl 기능](https://github.com/izzy80/ex_query_dsl/blob/main/src/test/java/study/ex_query_dsl/repository/MemberRepositoryTest.java)


## 0. QnA
1. Q파일은 git에서 관리하지 말자
   <details>
   <summary>자세히 보기</summary>
   이유: Q파일은 QueryDSL에 의해 자동 생성되며, 이를 Git에서 관리하면 충돌이 발생할 가능성이 높습니다.</br>
   참고 링크: <a href="https://www.inflearn.com/questions/875369" target="_blank">Inflearn Q&A</a>
   </details>
2. fetchjoin? 여러 쿼리?
    <details>
   <summary>자세히 보기</summary>
   이유 : 웹서버와 데이터베이스 서버가 서로 다른 원격지에 존재한다면 쿼리를 여러번 날리는 것보다 join 을 통해 한번에 조회하는 것이 효율적일 확률이 높습니다.</br>   
   애플리케이션 처리속도가 원격 통신속도보다 월등히 빠르기 때문입니다.
   참고 링크 : <a href="https://www.inflearn.com/questions/1213023" target="_blank">Inflearn Q&A</a>
   </details>
3. @PostConstruct와 @Transactional 분리   
    <details>
   <summary>자세히 보기</summary>
   참고 링크: <a href="https://www.inflearn.com/community/questions/26902" target="_blank">Inflearn Q&A</a>
   </details>

## 개발 팁 (QueryDSL 및 JPA 관련)
### 1. Gradle 설정
- **경로**: `Build, Execution, Deployment > Build Tools > Gradle`
- **설정**: Intellij 대신 **Gradle**로 설정 유지.
    - Intellij로 변경 시, `QEntity`를 import하지 못하는 오류 발생 가능.
---
### 2. Git 사용 시 주의점
- **한글 경로 포함 금지**: 한글 경로가 포함된 파일을 `git pull` 받을 경우 오류가 발생할 수 있음.
    - 참고 링크: [Inflearn Q&A](https://www.inflearn.com/questions/1116732)
---
### 3. JPQL과 QueryDSL 비교
- JPQL과 달리 **QueryDSL은 컴파일 시점에 오류를 발견 가능**.
    - QueryDSL은 JPQL의 빌더 역할을 수행.
---
### 4. JPA 및 QueryDSL 서브쿼리 제한
- **제한 사항**:
    - **`SELECT`** 및 **`WHERE`** 절에서 서브쿼리 가능.
    - **`FROM`** 절에서는 서브쿼리 불가. (QueryDSL도 동일)
- **해결 방안**:
    1. 서브쿼리를 **JOIN**으로 변경.
    2. 애플리케이션에서 쿼리를 **2번 분리**하여 실행.
    3. **Native SQL** 사용.
- **원칙**: 데이터베이스는 데이터를 최소화하여 처리하는 역할을 담당.
---
### 5. CASE문 사용 지침
- **DB 역할**:
    - 원본 데이터를 그대로 유지하고 가공은 애플리케이션 단에서 처리.
- **권장 사항**:
    - 가능한 **CASE문은 지양**.
    - 꼭 필요한 경우에만 사용.
---
### 6. Tuple 사용 지침
- **문제점**:
    - **`Tuple` 객체**를 Repository에서 Service, Controller로 넘기는 것은 바람직하지 않음.
- **대안**:
    - **DTO**를 활용하여 데이터를 처리하도록 권장.
---
### 7. Projection 반환 결과
- tuple
- dto
- Dto에 생성자 위에 @QueryProjection
  - 컴파일 오류를 알려줘서 좋지만 queryDsl의 의존성이 있어서 실무에서는 안 씀
  - dto가 여기저기 레이어에서 사용되기 때문
---
### 8. 벌크 연산   
영속성컨텍스트를 거치지 않고 바로 DB에 저장된다. 
em.flush(), em.clear() 하지 않으면 영속성컨텍스트와 DB와 데이터에 차이가 발생한다. 
---
### 9. select vs selectFrom
- select() : 어떤 필드만 가지고 오고 싶을때
- selectFrom() : 특정 필드가 아니라 그냥 전체 조회할 때 편하게 쓴다. 
---
### 10. 동적쿼리
- 기본값이 있는 것이 좋다. condition 값이 아예 없으면 오류 발생
- BooleanExpression을 쓰면 조합이 가능함 
---
### 11. JPA에서 데이터 조회 방법
1. 엔티티로 조회하기
   - fetch join 사용가능
   - 특정 엔티티에 종속될 수 있음
2. DTO로 조회하기
   - 일반 join 사용가능
   - 특정 엔티티에 종속X => 여러 테이블 조인 => 각 테이블에 있는 원하는 값만 별도로 조회 가능

- 참고 링크: [Inflearn Q&A](https://www.inflearn.com/questions/1120713)
---
### 12. join도 chain하는 방법
- 참고 링크: [Inflearn Q&A](https://www.inflearn.com/questions/899972)
---
### 13. 페이징
1. 전체 카운트를 한 번에 조회하는 단순한 방법
    - .fetchResults()를 사용하여 내용과 전체 카운트를 한 번에 조회할 수 있다. (실제 쿼리는 2번 호출)
    - .fetchResults()는 카운트 쿼리 실행시 필요없는 order by는 제거하여 최적화한다. 
2. 데이터 내용과 전체 카운트를 별도로 조회하는 방법
   - 전체 카운트를 조회 하는 방법을 최적화 할 수 있으면 이렇게 분리하면 된다.
   => 최적화하고 싶으면 사용해라 
---
### 14. 정렬
정렬( Sort )은 조건이 조금만 복잡해져도 Pageable의 Sort기능을 사용하기 어렵다.   
루트 엔티티 범위를 넘어가는 동적 정렬 기능이 필요하면 스프링 데이터 페이징이 제공하는 Sort를 사용하기 보다는   
파라미터를 받아서 직접 처리하는 것을 권장한다
---
### 15. 스프링 데이터 JPA가 제공하는 Querydsl 기능
- QuerydslPredicateExecutor -> 실무에서는 권장X
- Querydsl Web -> 정말 권장 안 함