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
