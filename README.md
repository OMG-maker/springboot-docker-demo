# Spring Boot Docker Demo

## 프로젝트 개요
이 프로젝트는 Spring Boot를 기반으로 한 REST API 서버를 구축하는 데모 프로젝트입니다. Docker로 컨테이너화된 환경에서 MySQL 데이터베이스를 사용하며, Flyway로 데이터베이스 마이그레이션을 관리합니다. Spring Security로 Basic Authentication을 적용했고, 앞으로 JWT 인증을 추가할 예정입니다.

### 주요 기능
- **Docker**: MySQL과 Spring Boot 앱을 컨테이너로 실행.
- **JPA & Flyway**: 데이터베이스 ORM과 스키마 버전 관리.
- **Spring Security**: Basic Auth로 인증/인가.
- **REST API**: 사용자 CRUD API 제공 (`/users` 엔드포인트).

### 기술 스택
- **백엔드**: Spring Boot 3.4.4, Java 17
- **데이터베이스**: MySQL 8.0 (Docker)
- **마이그레이션**: Flyway
- **보안**: Spring Security (Basic Auth)
- **빌드**: Gradle
- **컨테이너**: Docker, Docker Compose

## 아키텍처
### 레이어 구조
- **컨트롤러 (`UserController`)**: REST API 엔드포인트 정의, 요청/응답 처리.
- **레포지토리 (`UserRepository`)**: 데이터베이스 접근 (JPA).
- **엔티티 (`User`)**: 데이터베이스 테이블 매핑.
- **DTO (`UserDto`)**: 요청/응답 데이터 전송 객체.

### 동작 흐름
1. 클라이언트 요청 → Spring Boot 애플리케이션 (Tomcat 8080 포트).
2. Spring Security → Basic Auth 인증 (`admin:password`).
3. 컨트롤러 → 요청 처리 (`/users` 등).
4. JPA → 데이터베이스 접근 (`UserRepository`).
5. Flyway → 데이터베이스 스키마 관리 (MySQL `mydb`).
6. 응답 → JSON 형식 (`UserDto`).

## 설치 및 실행
### Prerequisites
- Docker & Docker Compose
- Java 17
- Gradle

### 실행 방법
1. 저장소 클론:
   ```bash
   git clone https://github.com/OMG-maker/springboot-docker-demo.git
   cd springboot-docker-demo
   ```
2. 빌드:
   ```bash
   ./gradlew clean build -x test
   ```
3. Docker Compose로 실행:
   ```bash
   docker-compose up --build
   ```
4. API 테스트:
   - 사용자 생성:
     ```bash
     curl -u admin:password -X POST http://localhost:8080/users -H "Content-Type: application/json" -d '{"id":1,"name":"Test1","email":"test1@example.com"}'
     ```
   - 사용자 조회:
     ```bash
     curl -u admin:password http://localhost:8080/users
     ```

## 주요 기능별 설정 및 코드
### 1. Docker
Docker를 사용해 MySQL과 Spring Boot 앱을 컨테이너로 실행합니다.

- **`docker-compose.yml`**:
  ```yaml
  services:
    db:
      image: mysql:8.0
      environment:
        MYSQL_ROOT_PASSWORD: rootpassword
        MYSQL_DATABASE: mydb
      volumes:
        - db-data:/var/lib/mysql
    app:
      image: myspringboot-app-app
      build:
        context: .
        dockerfile: Dockerfile
      ports:
        - "8080:8080"
      depends_on:
        - db
  volumes:
    db-data:
  ```
- **`Dockerfile`**:
  ```dockerfile
  FROM openjdk:17-jdk-slim
  COPY build/libs/*.jar app.jar
  ENTRYPOINT ["java", "-jar", "/app.jar"]
  ```

### 2. JPA & Flyway
JPA로 데이터베이스와 연동하고, Flyway로 스키마를 관리합니다.

- **JPA 설정** (`application.yml`):
  ```yaml
  spring:
    datasource:
      url: jdbc:mysql://db:3306/mydb?useSSL=false&allowPublicKeyRetrieval=true
      username: root
      password: rootpassword
    jpa:
      hibernate:
        ddl-auto: validate
  ```
- **엔티티** (`User.java`):
  ```java
  @Entity
  public class User {
      @Id
      private Long id;
      private String name;
      private String email;
      public Long getId() { return id; }
      public void setId(Long id) { this.id = id; }
      public String getName() { return name; }
      public void setName(String name) { this.name = name; }
      public String getEmail() { return email; }
      public void setEmail(String email) { this.email = email; }
  }
  ```
- **Flyway 설정** (`application.yml` & `build.gradle`):
  ```yaml
  spring:
    flyway:
      baseline-on-migrate: true
      baseline-version: 0
  ```
  ```gradle
  implementation 'org.flywaydb:flyway-core'
  implementation 'org.flywaydb:flyway-mysql'
  ```
- **마이그레이션 스크립트** (`V1__add_email_column.sql`):
  ```sql
  ALTER TABLE user ADD COLUMN email VARCHAR(255);
  ```

### 3. Spring Security (Basic Auth)
Spring Security로 Basic Authentication을 적용했습니다.

- **설정** (`SecurityConfig.java`):
  ```java
  @Configuration
  @EnableWebSecurity
  public class SecurityConfig {
      @Bean
      public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
          http
              .authorizeHttpRequests(auth -> auth
                  .requestMatchers("/hello").permitAll()
                  .anyRequest().hasRole("ADMIN"))
              .httpBasic(Customizer.withDefaults())
              .csrf(AbstractHttpConfigurer::disable);
          return http.build();
      }

      @Bean
      public UserDetailsService userDetailsService() {
          UserDetails admin = User.withUsername("admin")
              .password("{noop}password")
              .roles("ADMIN")
              .build();
          return new InMemoryUserDetailsManager(admin);
      }
  }
  ```

### 4. REST API (CRUD)
사용자 관리 CRUD API를 제공합니다.

- **컨트롤러** (`UserController.java`):
  ```java
  @RestController
  public class UserController {
      @Autowired
      private UserRepository userRepository;

      @PostMapping("/users")
      public UserDto saveUser(@Valid @RequestBody UserDto userDto) {
          User user = new User();
          user.setId(userDto.getId());
          user.setName(userDto.getName());
          user.setEmail(userDto.getEmail());
          user = userRepository.save(user);
          UserDto response = new UserDto();
          response.setId(user.getId());
          response.setName(user.getName());
          response.setEmail(user.getEmail());
          return response;
      }

      @GetMapping("/users")
      public Page<UserDto> getUsers(Pageable pageable) {
          return userRepository.findAll(pageable).map(user -> {
              UserDto dto = new UserDto();
              dto.setId(user.getId());
              dto.setName(user.getName());
              dto.setEmail(user.getEmail());
              return dto;
          });
      }

      @GetMapping("/users/{id}")
      public UserDto getUser(@PathVariable Long id) { ... }

      @PutMapping("/users/{id}")
      public UserDto updateUser(@PathVariable Long id, @RequestBody UserDto userDto) { ... }

      @DeleteMapping("/users/{id}")
      public void deleteUser(@PathVariable Long id) { ... }
  }
  ```
- **DTO** (`UserDto.java`):
  ```java
  public class UserDto {
      @NotNull
      private Long id;
      @NotBlank
      private String name;
      private String email;
      public Long getId() { return id; }
      public void setId(Long id) { this.id = id; }
      public String getName() { return name; }
      public void setName(String name) { this.name = name; }
      public String getEmail() { return email; }
      public void setEmail(String email) { this.email = email; }
  }
  ```

## 진행 상황
- **완료**:
  - Docker 환경 설정.
  - Flyway로 데이터베이스 마이그레이션 (`email` 컬럼 추가).
  - Spring Security로 Basic Auth 적용.
  - 사용자 CRUD API 구현.
- **진행 중**:
  - JWT 인증 구현 예정 (`feature/jwt-auth` 브랜치).

## 기여
1. 브랜치 생성 (`feature/기능명`).
2. 변경 사항 커밋 및 푸시.
3. `develop` 브랜치로 PR 생성 및 병합.