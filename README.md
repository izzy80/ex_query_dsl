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
[강의 보러가기](https://www.inflearn.com/course/querydsl-%EC%8B%A4%EC%A0%84/dashboard)   
[코드 보러가기](https://github.com/izzy80/ex_query_dsl/blob/main/src/test/java/study/ex_query_dsl/QuerydslBasicTest.java)

## 0. QnA
1. Q파일은 git에서 관리하지 말자
   <details>
   <summary>자세히 보기</summary>
   충돌이 일어날 가능성이 있다. </br>
   [Link 1](https://www.inflearn.com/questions/875369)
   </details>

## Tip
1. Build, Execution, Deployment > Build Tools > Gradle 설정 Intellj가 아닌 Gradle로 냅두기.  
    Intellij로 바꿀 시, QEntity를 imort하지 못하는 오류가 생김
2. git pull 받을 때 한글 경로 포함시키지 말기-> 오류남   
    [Link1](https://www.inflearn.com/questions/1116732)
3. jpql과는 달리 compile 시점에 오류 발견 할 수 있다. 
4. JPA의 서브쿼리는 select, where는 가능하지만, from은 안 된다.  
   이는 QueryDsl도 안 된다. QueryDsl은 JPQL의 빌더 역할과 같기때문
   
    from절의 서브쿼리 해결방안
   1. 서브쿼리를 join으로 변경한다.
   2. 애플리케이션에서 쿼리를 2번 분리해서 실행한다.
   3. nativeSQL을 사용한다.   
   -> DB는 데이터를 최소화하는 역할
5. case문 역시, DB는 원본데이터를 그대로 두고,   
    보여주고 그런 경우 애플리케이션 단에서 다루기.   
    꼭 써야한다면 쓰지만 그렇지 않다면 쓰지말자
