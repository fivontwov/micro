# 🚀 API Gateway - Quick Start Guide

## ✅ ĐÃ SỬA CÁC LỖI

### 1. **StripPrefix sai** 
- ❌ Trước: `StripPrefix=1` → gửi `/forum/topics` tới service (WRONG!)
- ✅ Sau: `StripPrefix=2` → gửi `/topics` tới service (CORRECT!)

### 2. **Duplicate Routes**
- ❌ Trước: Routes trong cả `application.yml` VÀ `ApiGatewayApplication.java`
- ✅ Sau: Chỉ dùng `application.yml`, xóa code routes trong Java

### 3. **Port Conflicts**
- ❌ Trước: Gateway (8083), Study Management (8080) → confusing!
- ✅ Sau: Gateway (8080), Study Management (8082), Forum (8081)

### 4. **Security Block All**
- ❌ Trước: `anyExchange().authenticated()` → block tất cả requests
- ✅ Sau: `anyExchange().permitAll()` → allow all (easy testing)

## 🎯 CÁCH CHẠY

### Bước 1: Start Backend Services

```powershell
# Terminal 1: Forum Service
cd forum
mvn spring-boot:run
# Chạy trên port 8081

# Terminal 2: Study Management Service  
cd ssstudy_management
mvn spring-boot:run
# Chạy trên port 8082 (đã sửa từ 8080)
```

### Bước 2: Start API Gateway

```powershell
# Terminal 3: API Gateway
cd api-gateway
mvn spring-boot:run
# Chạy trên port 8080
```

### Bước 3: Test Routing

```bash
# Test Forum Service
curl http://localhost:8080/api/forum/topics

# Test Study Management Service
curl http://localhost:8080/api/study/users
```

## 📝 ROUTING TABLE

| Client Request | Gateway Strips | Gửi tới Backend | Backend Endpoint |
|---|---|---|---|
| `GET /api/forum/topics` | `/api/forum` | `localhost:8081` | `GET /topics` |
| `POST /api/forum/topics` | `/api/forum` | `localhost:8081` | `POST /topics` |
| `GET /api/forum/topics/1/comments` | `/api/forum` | `localhost:8081` | `GET /topics/1/comments` |
| `POST /api/study/auth/login` | `/api/study` | `localhost:8082` | `POST /auth/login` |
| `GET /api/study/users` | `/api/study` | `localhost:8082` | `GET /users` |
| `GET /api/study/subjects` | `/api/study` | `localhost:8082` | `GET /subjects` |

## 🔍 VERIFY

### 1. Check Services Running

```bash
# Forum Service (should return topics)
curl http://localhost:8081/topics

# Study Management (should return users or auth endpoints)
curl http://localhost:8082/users

# API Gateway (should return topics via gateway)
curl http://localhost:8080/api/forum/topics
```

### 2. Check Gateway Routes

```bash
curl http://localhost:8080/actuator/gateway/routes | jq
```

Expected output:
```json
[
  {
    "route_id": "forum-service",
    "uri": "http://localhost:8081",
    "predicate": "Paths: [/api/forum/**], match trailing slash: true"
  },
  {
    "route_id": "study-management-service", 
    "uri": "http://localhost:8082",
    "predicate": "Paths: [/api/study/**], match trailing slash: true"
  }
]
```

## ⚠️ TROUBLESHOOTING

### Problem 1: "Connection refused" 
**Nguyên nhân:** Backend service chưa chạy

**Giải pháp:**
```bash
# Check services:
curl http://localhost:8081/topics  # Forum
curl http://localhost:8082/users   # Study Management
```

### Problem 2: "404 Not Found"
**Nguyên nhân:** Routing sai

**Giải pháp:** Check logs của Gateway để xem request được gửi đi đâu

### Problem 3: Study Management vẫn dùng port 8080
**Nguyên nhân:** Chưa restart service sau khi đổi port

**Giải pháp:**
```bash
# Stop service (Ctrl+C)
# Check file ssstudy_management/src/main/resources/application.yaml
# Phải có: server.port: 8082
# Restart service
```

## 📊 PORT SUMMARY

| Service | Port | URL |
|---------|------|-----|
| **API Gateway** | 8080 | http://localhost:8080 |
| Forum Service | 8081 | http://localhost:8081 |
| Study Management | 8082 | http://localhost:8082 |
| PostgreSQL | 5432 | localhost:5432 |

## ✨ NEXT STEPS

1. ✅ Test tất cả endpoints qua Gateway
2. ⏭️ Enable JWT authentication (uncomment code trong SecurityConfig.java)
3. ⏭️ Add rate limiting với Redis
4. ⏭️ Setup Docker Compose cho tất cả services
5. ⏭️ Add Circuit Breaker pattern

## 📖 CHI TIẾT

- Xem [ROUTING_GUIDE.md](./ROUTING_GUIDE.md) để hiểu routing chi tiết
- Xem [README.md](./README.md) để biết full documentation
- Xem [TODO.md](../TODO.md) để biết what's done và what's next
