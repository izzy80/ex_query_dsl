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
## 0. QnA
1. Q파일은 git에서 관리하지 말자
   <details>
   <summary>자세히 보기</summary>
   충돌이 일어날 가능성이 있다.  
   [Link 1](https://www.inflearn.com/questions/875369)
   </details>

## Tip
1. Build, Execution, Deployment > Build Tools > Gradle 설정 Intellj가 아닌 Gradle로 냅두기. 
    Intellij로 바꿀 시, QEntity를 imort하지 못하는 오류가 생김
2. git pull 받을 때 한글 경로 포함시키지 말기. -> 오류남
    [Link1](https://www.inflearn.com/questions/1116732)
3. 