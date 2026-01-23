# API Gateway Routing Guide

## 🎯 Tóm tắt

API Gateway hoạt động như **single entry point** cho tất cả requests từ client tới microservices.

## 📊 Kiến trúc

```
Client
  ↓
API Gateway (localhost:8080)
  ├── /api/forum/** → Forum Service (localhost:8081)
  └── /api/study/** → Study Management Service (localhost:8082)
```

## 🔀 Routing Rules

### 1. Forum Service

**Backend:** `http://localhost:8081`  
**Prefix:** `/api/forum`  
**StripPrefix:** 2 (bỏ `/api/forum`)

| Client Request | Gateway Strips | Backend Receives |
|---|---|---|
| `GET /api/forum/topics` | `/api/forum` | `GET /topics` |
| `GET /api/forum/topics/1` | `/api/forum` | `GET /topics/1` |
| `POST /api/forum/topics` | `/api/forum` | `POST /topics` |
| `GET /api/forum/topics/1/comments` | `/api/forum` | `GET /topics/1/comments` |
| `POST /api/forum/topics/1/votes` | `/api/forum` | `POST /topics/1/votes` |

### 2. Study Management Service

**Backend:** `http://localhost:8082`  
**Prefix:** `/api/study`  
**StripPrefix:** 2 (bỏ `/api/study`)

| Client Request | Gateway Strips | Backend Receives |
|---|---|---|
| `POST /api/study/auth/login` | `/api/study` | `POST /auth/login` |
| `POST /api/study/auth/register` | `/api/study` | `POST /auth/register` |
| `GET /api/study/users` | `/api/study` | `GET /users` |
| `GET /api/study/subjects` | `/api/study` | `GET /subjects` |
| `POST /api/study/subject-registrations` | `/api/study` | `POST /subject-registrations` |

## 🚀 Testing Commands

### Forum Service via Gateway

```bash
# Get all topics
curl http://localhost:8080/api/forum/topics

# Get specific topic
curl http://localhost:8080/api/forum/topics/1

# Create topic
curl -X POST http://localhost:8080/api/forum/topics \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "title": "Test Topic",
    "body": "This is a test"
  }'

# Add comment
curl -X POST http://localhost:8080/api/forum/topics/1/comments \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "body": "Great topic!"
  }'

# Vote
curl -X POST http://localhost:8080/api/forum/topics/1/votes \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "value": 1
  }'
```

### Study Management Service via Gateway

```bash
# Login
curl -X POST http://localhost:8080/api/study/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user@example.com",
    "password": "password123"
  }'

# Get users
curl http://localhost:8080/api/study/users

# Get subjects
curl http://localhost:8080/api/study/subjects
```

## 🔍 Monitoring

### Check Gateway Routes
```bash
curl http://localhost:8080/actuator/gateway/routes | jq
```

### Check Health
```bash
curl http://localhost:8080/actuator/health
```

## ⚙️ Configuration

Routes được định nghĩa trong `application.yml`:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: forum-service
          uri: http://localhost:8081
          predicates:
            - Path=/api/forum/**
          filters:
            - StripPrefix=2  # Strip /api/forum
            
        - id: study-management-service
          uri: http://localhost:8082
          predicates:
            - Path=/api/study/**
          filters:
            - StripPrefix=2  # Strip /api/study
```

## 🔐 Security (Currently Disabled)

Security hiện tại **tắt** để dễ test. Tất cả requests đều được `permitAll()`.

Để bật JWT authentication, uncomment code trong `SecurityConfig.java`.

## 🐛 Troubleshooting

### Problem: 404 Not Found

**Nguyên nhân:** StripPrefix sai hoặc backend service không chạy

**Giải pháp:**
1. Check backend services đang chạy:
   ```bash
   # Forum service on 8081
   curl http://localhost:8081/topics
   
   # Study service on 8082
   curl http://localhost:8082/users
   ```

2. Check gateway logs:
   ```bash
   # Xem logs để biết request được route đi đâu
   ```

### Problem: Connection Refused

**Nguyên nhân:** Backend service không chạy hoặc sai port

**Giải pháp:** Start backend services trước khi start gateway

### Problem: Routes không hoạt động

**Nguyên nhân:** Có duplicate routes trong code và config

**Giải pháp:** Xóa programmatic routes trong `ApiGatewayApplication.java`, chỉ dùng `application.yml`

## 📝 Port Summary

| Service | Port | Description |
|---------|------|-------------|
| **API Gateway** | 8080 | Single entry point |
| **Forum Service** | 8081 | Forum microservice |
| **Study Management** | 8082 | Study management microservice |
| **PostgreSQL** | 5432 | Database |
| **Redis** | 6379 | Cache (optional) |

## ✅ Quick Start

1. Start backend services:
   ```bash
   # Terminal 1: Forum Service
   cd forum
   mvn spring-boot:run
   
   # Terminal 2: Study Management
   cd ssstudy_management
   mvn spring-boot:run
   ```

2. Start API Gateway:
   ```bash
   # Terminal 3: API Gateway
   cd api-gateway
   mvn spring-boot:run
   ```

3. Test:
   ```bash
   curl http://localhost:8080/api/forum/topics
   ```

## 🎯 Best Practices

1. **Always use Gateway URL** từ client:
   - ✅ `http://localhost:8080/api/forum/topics`
   - ❌ `http://localhost:8081/topics` (không nên access trực tiếp)

2. **Keep StripPrefix consistent:**
   - Format: `/api/{service-name}/**`
   - StripPrefix: 2 (để strip cả `/api` và `/{service-name}`)

3. **Add request headers** để tracking:
   - Gateway tự động add `X-Source: Gateway` vào mọi request
   - Backend services có thể check header này để biết request từ đâu

4. **Monitor via Actuator:**
   - `/actuator/gateway/routes` - Xem tất cả routes
   - `/actuator/health` - Health check
   - `/actuator/metrics` - Performance metrics
