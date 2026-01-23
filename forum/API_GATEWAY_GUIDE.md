# API Gateway - Kiến thức và Ứng dụng cho Project

## 📚 Tài liệu tham khảo chính thức

- **Spring Cloud Gateway Official**: https://spring.io/projects/spring-cloud-gateway
- **Documentation**: https://docs.spring.io/spring-cloud-gateway/reference/
- **GitHub**: https://github.com/spring-cloud/spring-cloud-gateway

---
micro/
├── api-gateway/          ← NEW
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── forum/
└── ssstudy_management/

## 🎯 API Gateway là gì?

API Gateway là một **điểm vào duy nhất (single entry point)** cho tất cả các client requests đến hệ thống microservices của bạn. Nó đóng vai trò như một "cổng" trung gian giữa client và các backend services.

```
┌─────────┐
│ Client  │
└────┬────┘
     │
     ▼
┌─────────────────────┐
│   API Gateway       │  ◄── Single Entry Point
└──┬──────────┬───────┘
   │          │
   ▼          ▼
┌──────┐  ┌──────────┐
│Forum │  │User Svc  │
│Service│  │(gRPC)    │
└──────┘  └──────────┘
```

---

## 🌟 Tại sao cần API Gateway?

### 1. **Routing & Load Balancing**
- Điều hướng request đến đúng service
- Phân tải traffic giữa nhiều instances

### 2. **Security (Bảo mật tập trung)**
- Authentication & Authorization tại một điểm
- API Key validation
- JWT token verification
- Rate limiting

### 3. **Cross-cutting Concerns**
- Logging
- Monitoring & Metrics
- Request/Response transformation
- Caching

### 4. **Protocol Translation**
- REST → gRPC
- HTTP → WebSocket
- Versioning APIs

### 5. **Simplified Client**
- Client chỉ cần biết 1 endpoint thay vì nhiều services
- Giảm số lượng round-trips

---

## 🏗️ Kiến trúc hiện tại của bạn

Dựa trên code của bạn, hiện tại:

```
Client → Forum Service (REST) → User Service (gRPC)
```

### Vấn đề:
1. ❌ Client phải biết địa chỉ Forum Service
2. ❌ Nếu thêm services khác (e.g., Auth, Notification), client phải quản lý nhiều endpoints
3. ❌ Không có centralized security/logging
4. ❌ Khó scale và monitor

---

## ✅ Kiến trúc với API Gateway

```
                    ┌──────────────────────────────┐
                    │      API Gateway             │
                    │  (Port: 8080)                │
Client → Request →  │  - Authentication            │
                    │  - Rate Limiting             │
                    │  - Logging                   │
                    │  - Load Balancing            │
                    └──┬───────────┬────────────┬──┘
                       │           │            │
                       ▼           ▼            ▼
                  ┌────────┐  ┌────────┐  ┌──────────┐
                  │ Forum  │  │  User  │  │  Auth    │
                  │Service │  │Service │  │ Service  │
                  │:8081   │  │:8082   │  │  :8083   │
                  └────────┘  └────────┘  └──────────┘
```

---

## 🚀 Ứng dụng cho Project của bạn

### Scenario 1: Basic Gateway với Routing

**Use case**: Tách Forum Service ra khỏi direct client access

```yaml
# application.yml cho Gateway
spring:
  cloud:
    gateway:
      routes:
        # Route cho Forum Service
        - id: forum-service
          uri: http://localhost:8081
          predicates:
            - Path=/api/topics/**
          filters:
            - StripPrefix=1  # Bỏ /api prefix
        
        # Route cho User Service (nếu expose REST)
        - id: user-service
          uri: http://localhost:8082
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=1
```

**Benefits**:
- Client gọi: `http://gateway:8080/api/topics` thay vì trực tiếp đến forum service
- Dễ dàng thay đổi backend URL mà không ảnh hưởng client

---

### Scenario 2: Add Authentication/Authorization

**Use case**: Bảo vệ tất cả APIs bằng JWT token

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .authorizeExchange()
                .pathMatchers("/api/topics/**").authenticated()
                .pathMatchers("/api/auth/**").permitAll()
            .and()
            .oauth2ResourceServer()
                .jwt()
            .and()
            .build();
    }
}
```

**Benefits**:
- Không cần implement authentication ở mỗi service
- Centralized security policy

---

### Scenario 3: Rate Limiting

**Use case**: Giới hạn 100 requests/phút cho mỗi user

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: forum-service
          uri: http://localhost:8081
          predicates:
            - Path=/api/topics/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200
                key-resolver: "#{@userKeyResolver}"
```

```java
@Bean
public KeyResolver userKeyResolver() {
    return exchange -> Mono.just(
        exchange.getRequest()
            .getHeaders()
            .getFirst("X-User-Id")
    );
}
```

**Benefits**:
- Bảo vệ backend khỏi bị overload
- Fair usage giữa các users

---

### Scenario 4: Circuit Breaker

**Use case**: Fallback khi User Service (gRPC) down

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: forum-with-users
          uri: http://localhost:8081
          predicates:
            - Path=/api/topics/**
          filters:
            - name: CircuitBreaker
              args:
                name: forumCircuitBreaker
                fallbackUri: forward:/fallback/topics
```

```java
@RestController
public class FallbackController {
    
    @GetMapping("/fallback/topics")
    public ResponseEntity<Map<String, Object>> topicsFallback() {
        return ResponseEntity.ok(Map.of(
            "message", "Service temporarily unavailable",
            "status", "fallback"
        ));
    }
}
```

**Benefits**:
- Graceful degradation khi service down
- Tránh cascade failures

---

### Scenario 5: Request/Response Logging & Monitoring

**Use case**: Log tất cả requests đi qua gateway

```java
@Component
public class LoggingFilter implements GlobalFilter, Ordered {
    
    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        String path = exchange.getRequest().getPath().toString();
        
        log.info("Request: {} {}", 
            exchange.getRequest().getMethod(), 
            path);
        
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            log.info("Response: {} - {}ms", 
                exchange.getResponse().getStatusCode(), 
                duration);
        }));
    }
    
    @Override
    public int getOrder() {
        return -1; // Highest priority
    }
}
```

**Benefits**:
- Centralized logging
- Performance monitoring
- Debugging dễ dàng

---

### Scenario 6: Protocol Translation (REST → gRPC)

**Use case**: Client gọi REST, Gateway chuyển sang gRPC cho User Service

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: grpc-user-service
          uri: grpc://localhost:9090
          predicates:
            - Path=/api/users/{id}
          filters:
            - name: JsonToGrpc
              args:
                protoDescriptor: user.proto
                service: UserService
                method: getUserById
```

**Benefits**:
- Client không cần biết backend protocol
- Flexibility trong việc chọn protocol cho services

---

## 📦 Implementation Steps cho Project của bạn

### Step 1: Tạo Gateway Service mới

```xml
<!-- pom.xml -->
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>
</dependencies>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>2024.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### Step 2: Configuration

```yaml
# application.yml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        # Forum Topics
        - id: forum-topics
          uri: http://localhost:8081
          predicates:
            - Path=/topics/**
          filters:
            - AddRequestHeader=X-Gateway-Request, true
        
        # Future services...
        # - id: auth-service
        #   uri: http://localhost:8083
        #   ...
      
      # Global CORS
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
            allowedHeaders: "*"

# Actuator để monitor
management:
  endpoints:
    web:
      exposure:
        include: gateway, health, metrics
```

### Step 3: Main Application

```java
@SpringBootApplication
public class ApiGatewayApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
    
    // Custom filter example
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("forum_route", r -> r
                .path("/topics/**")
                .filters(f -> f
                    .addRequestHeader("X-Source", "Gateway")
                    .retry(config -> config
                        .setRetries(3)
                        .setMethods(HttpMethod.GET)
                    )
                )
                .uri("http://localhost:8081")
            )
            .build();
    }
}
```

---

## 🔧 Advanced Features bạn có thể dùng

### 1. **Service Discovery với Eureka/Consul**

Thay vì hardcode URIs, dùng service discovery:

```yaml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        - id: forum-service
          uri: lb://forum-service  # Load-balanced
          predicates:
            - Path=/topics/**
```

### 2. **API Versioning**

```yaml
routes:
  # v1 API
  - id: forum-v1
    uri: http://localhost:8081
    predicates:
      - Path=/v1/topics/**
    filters:
      - StripPrefix=1
  
  # v2 API
  - id: forum-v2
    uri: http://localhost:8082
    predicates:
      - Path=/v2/topics/**
    filters:
      - StripPrefix=1
```

### 3. **Request Transformation**

```java
@Component
public class ModifyRequestBodyFilter implements GlobalFilter {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return DataBufferUtils.join(exchange.getRequest().getBody())
            .flatMap(dataBuffer -> {
                // Modify request body
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                
                String body = new String(bytes, StandardCharsets.UTF_8);
                // Transform body...
                
                // Continue with modified request
                return chain.filter(exchange);
            });
    }
}
```

### 4. **Caching**

```yaml
filters:
  - name: LocalResponseCache
    args:
      size: 100MB
      timeToLive: 1h
```

---

## 📊 Monitoring & Observability

### Actuator Endpoints

```bash
# Gateway routes info
GET http://localhost:8080/actuator/gateway/routes

# Refresh routes
POST http://localhost:8080/actuator/gateway/refresh

# Route filters
GET http://localhost:8080/actuator/gateway/routefilters

# Metrics
GET http://localhost:8080/actuator/metrics
```

### Integration với Prometheus/Grafana

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
```

---

## 🎁 Benefits cho Project của bạn

| Feature | Without Gateway | With Gateway |
|---------|----------------|--------------|
| **Endpoints** | Multiple (forum:8081, user:8082, ...) | Single (gateway:8080) |
| **Security** | Each service handles auth | Centralized at gateway |
| **Monitoring** | Monitor each service | Single dashboard |
| **Rate Limiting** | Implement per service | Centralized policy |
| **CORS** | Configure per service | Global config |
| **Protocol** | Client must know (REST/gRPC) | Gateway handles translation |
| **Versioning** | Hard to manage | Easy routing by version |

---

## 🚦 Khi nào NÊN dùng API Gateway?

✅ Khi có nhiều hơn 2-3 microservices  
✅ Cần centralized authentication/authorization  
✅ Cần rate limiting/throttling  
✅ Muốn hide internal architecture khỏi clients  
✅ Cần protocol translation (REST ↔ gRPC)  
✅ Cần aggregation từ nhiều services  

---

## ⚠️ Khi nào KHÔNG NÊN dùng?

❌ Chỉ có 1 service duy nhất  
❌ Application rất đơn giản  
❌ Team nhỏ, không có capacity maintain thêm service  
❌ Latency là critical (gateway adds overhead ~10-50ms)  

---

## 🔍 Alternatives

1. **Netflix Zuul** (older, less recommended)
2. **Kong Gateway** (feature-rich, enterprise)
3. **Nginx/Traefik** (infrastructure level)
4. **AWS API Gateway** (managed service)
5. **Azure API Management**

**Recommendation**: Với Spring Boot ecosystem, **Spring Cloud Gateway** là lựa chọn tốt nhất.

---

## 📝 Next Steps cho Project của bạn

### Phase 1: Basic Setup
1. ✅ Tạo Gateway service mới
2. ✅ Config routing cho Forum Service
3. ✅ Test basic request flow

### Phase 2: Security
1. ✅ Add JWT authentication
2. ✅ Implement rate limiting
3. ✅ Add CORS configuration

### Phase 3: Resilience
1. ✅ Add Circuit Breaker
2. ✅ Configure retry logic
3. ✅ Add timeout policies

### Phase 4: Observability
1. ✅ Add logging filter
2. ✅ Integrate Prometheus
3. ✅ Setup Grafana dashboard

---

## 📚 Tài liệu học thêm

### Official Docs
- [Spring Cloud Gateway Reference](https://docs.spring.io/spring-cloud-gateway/reference/)
- [Spring Cloud Gateway Samples](https://github.com/spring-cloud/spring-cloud-gateway/tree/main/spring-cloud-gateway-sample)

### Blog Posts
- [Baeldung: Spring Cloud Gateway](https://www.baeldung.com/spring-cloud-gateway)
- [Spring.io Guides](https://spring.io/guides)

### Videos
- [Spring Tips: Spring Cloud Gateway](https://www.youtube.com/watch?v=TwVtlNX-2Hs)

---

## 💡 Ví dụ thực tế cho bạn

Với architecture hiện tại của bạn:

**Current**:
```java
// Client code
fetch('http://forum-service:8081/topics')  // Direct call
```

**With Gateway**:
```java
// Client code
fetch('http://api-gateway:8080/api/topics')  // Through gateway

// Gateway routes to forum-service:8081/topics
// + Adds authentication
// + Logs request
// + Rate limits
// + Retries on failure
```

---

## ❓ Questions?

Nếu bạn muốn tôi:
1. ✅ Implement API Gateway cho project này
2. ✅ Setup specific features (auth, rate limiting, etc.)
3. ✅ Tạo Docker Compose với Gateway

Hãy cho tôi biết! 🚀
