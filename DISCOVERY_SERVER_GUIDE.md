# Service Discovery với Eureka Server - Hướng dẫn đầy đủ

## 📋 Tổng quan

Tôi đã thêm **Eureka Service Discovery Server** vào kiến trúc microservices của bạn. Bây giờ các services tự động register và discover lẫn nhau.

## 🏗️ Kiến trúc mới

### Trước (Hardcoded URLs):
```
API Gateway → http://localhost:8081 (Forum)
API Gateway → http://localhost:8082 (Study Management)
```

### Sau (Service Discovery):
```
                ┌─────────────────────┐
                │  Eureka Server      │
                │    Port: 8761       │
                └──────────┬──────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
        ▼                  ▼                  ▼
   ┌─────────┐      ┌──────────┐      ┌────────────┐
   │ Gateway │      │  Forum   │      │   Study    │
   │  :8080  │      │  :8081   │      │   :8082    │
   └─────────┘      └──────────┘      └────────────┘
        │                  │                  │
        └──────────────────┴──────────────────┘
               All register with Eureka
```

## ✅ Thay đổi đã thực hiện

### 1. **Tạo Eureka Server mới**
- Port: `8761`
- Dashboard: `http://localhost:8761`
- Location: `eureka-server/`

### 2. **API Gateway - Sử dụng Service Discovery**

**pom.xml** - Added dependency:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

**application.yml** - Changed routing:
```yaml
# Trước: Hardcoded URLs
uri: http://localhost:8081

# Sau: Service Discovery
uri: lb://forum-service  # lb = load balanced
```

**ApiGatewayApplication.java** - Added annotation:
```java
@EnableDiscoveryClient
```

### 3. **Forum Service - Register với Eureka**

**pom.xml** - Added:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

**application.properties** - Added:
```properties
spring.application.name=forum-service  # Changed from 'forum'
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

**ForumApplication.java** - Added:
```java
@EnableDiscoveryClient
```

### 4. **Study Management - Register với Eureka**

**pom.xml** - Added:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

**application.yaml** - Changed:
```yaml
spring:
  application:
    name: study-management-service  # Changed from 'ddp_study_management'

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

**DdpStudyManagementApplication.java** - Added:
```java
@EnableDiscoveryClient
```

### 5. **Docker Compose - Tất cả services**

Created `docker-compose.yml` với:
- PostgreSQL
- Redis
- Eureka Server
- Forum Service
- Study Management Service
- API Gateway

## 🚀 Cách chạy

### Option 1: Chạy riêng lẻ (Development)

```powershell
# Terminal 1: Eureka Server (CHẠY ĐẦU TIÊN!)
cd eureka-server
mvn spring-boot:run

# Đợi Eureka Server khởi động (khoảng 30 giây)
# Kiểm tra: http://localhost:8761

# Terminal 2: Forum Service
cd forum
mvn spring-boot:run

# Terminal 3: Study Management Service
cd ssstudy_management
mvn spring-boot:run

# Terminal 4: API Gateway (CHẠY SAU CÙNG!)
cd api-gateway
mvn spring-boot:run
```

### Option 2: Docker Compose (Production-like)

```powershell
# Từ project root
docker-compose up --build
```

## 🔍 Kiểm tra Service Discovery

### 1. Mở Eureka Dashboard
```
http://localhost:8761
```

Bạn sẽ thấy:
```
Instances currently registered with Eureka
─────────────────────────────────────────
Application           AMIs      Availability Zones    Status
───────────────────────────────────────────────────────────
API-GATEWAY           1         default (1)           UP (1) - api-gateway:8080
FORUM-SERVICE         1         default (1)           UP (1) - forum-service:8081
STUDY-MANAGEMENT-     1         default (1)           UP (1) - study-management-service:8082
SERVICE
```

### 2. Test API Gateway routes

```bash
# Gateway tự động tìm Forum Service qua Eureka
curl http://localhost:8080/api/forum/topics

# Gateway tự động tìm Study Management qua Eureka
curl http://localhost:8080/api/study/users
```

### 3. Check Eureka API

```bash
# Xem tất cả registered instances
curl http://localhost:8761/eureka/apps | jq

# Xem specific service
curl http://localhost:8761/eureka/apps/FORUM-SERVICE | jq
```

## 💡 Lợi ích của Service Discovery

### 1. **Dynamic Service Location**
- Services không cần biết IP/port của nhau
- API Gateway tự động tìm services qua tên
- Easy to scale (multiple instances)

### 2. **Load Balancing**
```yaml
uri: lb://forum-service  # Automatically load balances
```
Nếu có 3 instances của Forum Service, requests sẽ được phân tán đều.

### 3. **Health Monitoring**
Eureka tự động remove unhealthy instances khỏi registry.

### 4. **Zero-downtime Deployment**
Deploy version mới → Register với Eureka → Old version graceful shutdown

## 🔧 Configuration Chi tiết

### Eureka Server (Port 8761)
```yaml
eureka:
  client:
    register-with-eureka: false  # Don't register itself
    fetch-registry: false
  server:
    enable-self-preservation: false  # Disable in dev
```

### Client Configuration (All Services)
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${server.port}
```

## 🐛 Troubleshooting

### Problem 1: Services không xuất hiện trong Eureka Dashboard

**Nguyên nhân:**
1. Eureka Server chưa chạy
2. Service chưa có dependency `spring-cloud-starter-netflix-eureka-client`
3. Sai URL trong `eureka.client.service-url.defaultZone`

**Giải pháp:**
```bash
# 1. Check Eureka Server
curl http://localhost:8761

# 2. Check service logs
# Phải thấy: "DiscoveryClient_XXX - registration status: 204"

# 3. Check application.yml/properties
# Phải có: eureka.client.service-url.defaultZone
```

### Problem 2: API Gateway không tìm thấy services

**Nguyên nhân:**
- Service name sai trong routing config
- Services chưa register với Eureka

**Giải pháp:**
```yaml
# application.yml của Gateway
routes:
  - id: forum-service
    uri: lb://forum-service  # Phải match với spring.application.name
```

### Problem 3: 503 Service Unavailable

**Nguyên nhân:**
- Backend service down
- Service chưa healthy

**Giải pháp:**
```bash
# Check Eureka Dashboard - service phải status UP
# Check service logs
# Restart service
```

## 📊 Port Summary

| Service | Port | URL | Description |
|---------|------|-----|-------------|
| **Eureka Server** | 8761 | http://localhost:8761 | Service Discovery Dashboard |
| **API Gateway** | 8080 | http://localhost:8080 | Single Entry Point |
| Forum Service | 8081 | http://localhost:8081 | Forum REST API |
| Study Management | 8082 | http://localhost:8082 | Study REST API |
| Study Management (gRPC) | 9090 | grpc://localhost:9090 | gRPC Server |
| PostgreSQL | 5432 | localhost:5432 | Database |
| Redis | 6379 | localhost:6379 | Cache |

## 🎯 Testing Workflow

### 1. Start all services
```bash
# Eureka Server → Forum → Study Management → Gateway
```

### 2. Verify registration
```bash
# Open browser: http://localhost:8761
# All 3 services should show UP status
```

### 3. Test routing
```bash
# Through Gateway (Recommended)
curl http://localhost:8080/api/forum/topics
curl http://localhost:8080/api/study/users

# Direct access (Still works but not recommended)
curl http://localhost:8081/topics
curl http://localhost:8082/users
```

## 🔐 Production Considerations

### 1. Security
```yaml
# Secure Eureka Dashboard
spring:
  security:
    user:
      name: admin
      password: ${EUREKA_PASSWORD}
```

### 2. High Availability
Run multiple Eureka Server instances:
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://eureka1:8761/eureka/,http://eureka2:8762/eureka/
```

### 3. Enable Self Preservation
```yaml
eureka:
  server:
    enable-self-preservation: true  # Enable in production
```

## 📚 Next Steps

1. ✅ Service Discovery hoạt động
2. ⏭️ Add multiple instances để test load balancing
3. ⏭️ Configure Circuit Breaker với Resilience4j
4. ⏭️ Add Distributed Tracing (Sleuth + Zipkin)
5. ⏭️ Implement API Rate Limiting
6. ⏭️ Add Centralized Configuration (Spring Cloud Config)

## 🎉 Kết luận

Bây giờ microservices của bạn đã có:
- ✅ Service Discovery với Eureka
- ✅ Dynamic routing trong API Gateway
- ✅ Load balancing capability
- ✅ Health monitoring
- ✅ Docker Compose setup

Hệ thống sẵn sàng scale! 🚀
