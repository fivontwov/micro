# 🚀 Quick Start - All Services

## 📝 Thứ tự khởi động (QUAN TRỌNG!)

Phải chạy theo đúng thứ tự sau:

```
1. Eureka Server (8761)
2. Forum Service (8081) + Study Management (8082)  
3. API Gateway (8080)
```

## 💻 Lệnh khởi động

### Windows PowerShell

```powershell
# Terminal 1: Eureka Server
cd eureka-server
mvn spring-boot:run

# Đợi message: "Started Eureka Server"
# Kiểm tra: http://localhost:8761

# Terminal 2: Forum Service
cd forum
mvn spring-boot:run

# Đợi message: "DiscoveryClient_FORUM-SERVICE - registration status: 204"

# Terminal 3: Study Management Service
cd ssstudy_management
mvn spring-boot:run

# Đợi message: "DiscoveryClient_STUDY-MANAGEMENT-SERVICE - registration status: 204"

# Terminal 4: API Gateway
cd api-gateway
mvn spring-boot:run

# Đợi message: "Started ApiGatewayApplication"
```

## 🐳 Docker Compose (Tự động)

```powershell
# Từ project root
docker-compose up --build
```

Tất cả services sẽ tự động start theo đúng thứ tự!

## ✅ Kiểm tra

### 1. Eureka Dashboard
```
http://localhost:8761
```

Phải thấy 3 services:
- API-GATEWAY (UP)
- FORUM-SERVICE (UP)
- STUDY-MANAGEMENT-SERVICE (UP)

### 2. Test APIs

```bash
# Forum Service via Gateway
curl http://localhost:8080/api/forum/topics

# Study Management via Gateway
curl http://localhost:8080/api/study/users

# Gateway Health
curl http://localhost:8080/actuator/health
```

### 3. Check Ports

```powershell
# Windows
netstat -ano | findstr "8761"  # Eureka
netstat -ano | findstr "8080"  # Gateway
netstat -ano | findstr "8081"  # Forum
netstat -ano | findstr "8082"  # Study
```

## 🛑 Dừng Services

### Individual
```powershell
# Ctrl+C trong mỗi terminal
```

### Docker Compose
```powershell
docker-compose down
```

## ⚠️ Troubleshooting

### Eureka Server không start
```powershell
# Check port 8761
netstat -ano | findstr "8761"

# Nếu bị chiếm, kill process hoặc đổi port
```

### Services không register
```bash
# Check logs - phải thấy:
# "Registering application FORUM-SERVICE with eureka"
# "DiscoveryClient_FORUM-SERVICE - registration status: 204"
```

### Gateway không tìm thấy services
```bash
# 1. Check Eureka Dashboard - services phải UP
# 2. Wait 30 seconds sau khi services start
# 3. Restart Gateway
```

## 📊 Service Status

| Service | Port | Health Check |
|---------|------|--------------|
| Eureka Server | 8761 | http://localhost:8761/actuator/health |
| API Gateway | 8080 | http://localhost:8080/actuator/health |
| Forum Service | 8081 | http://localhost:8081/actuator/health |
| Study Management | 8082 | http://localhost:8082/actuator/health |

## 🎯 Development Workflow

1. **Start Eureka** → Đợi 30s
2. **Start Backend Services** → Check Eureka Dashboard
3. **Start Gateway** → Test APIs
4. **Code changes** → Restart individual service (không cần restart Eureka)
