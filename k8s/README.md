# Hướng dẫn Deploy Microservices lên Kubernetes (K8s)

## 📚 Mục lục
1. [Kubernetes là gì?](#kubernetes-là-gì)
2. [Cài đặt môi trường](#cài-đặt-môi-trường)
3. [Cấu trúc thư mục](#cấu-trúc-thư-mục)
4. [Deploy từng bước](#deploy-từng-bước)
5. [Kiểm tra từng phần](#kiểm-tra-từng-phần)
6. [Troubleshooting](#troubleshooting)

## 🎓 Kubernetes là gì?

**Kubernetes (K8s)** là một hệ thống tự động hóa việc triển khai, mở rộng và quản lý các ứng dụng được đóng gói trong container (Docker).

### Tại sao dùng Kubernetes?
- ✅ **Tự động scale**: Tự động tăng/giảm số lượng container khi cần
- ✅ **Self-healing**: Tự động khởi động lại container bị lỗi
- ✅ **Load balancing**: Tự động phân phối traffic
- ✅ **Rolling updates**: Cập nhật ứng dụng không downtime
- ✅ **Service discovery**: Các service tự tìm thấy nhau

### So sánh Docker Compose vs Kubernetes

| Docker Compose | Kubernetes |
|----------------|-----------|
| Chạy trên 1 máy | Chạy trên nhiều máy (cluster) |
| Phù hợp development | Phù hợp production |
| Đơn giản, dễ học | Phức tạp, mạnh mẽ |
| Không tự scale | Tự động scale |
| Không self-healing | Tự động recovery |

## 🛠️ Cài đặt môi trường

### Bước 1: Cài Docker Desktop (Windows)

1. Download Docker Desktop: https://www.docker.com/products/docker-desktop/
2. Cài đặt và khởi động Docker Desktop
3. Kiểm tra:
```powershell
docker --version
# Docker version 24.0.0 hoặc mới hơn
```

### Bước 2: Enable Kubernetes trong Docker Desktop

1. Mở **Docker Desktop**
2. Click vào **Settings** (biểu tượng bánh răng)
3. Chọn **Kubernetes** ở menu bên trái
4. Tick vào **Enable Kubernetes**
5. Click **Apply & Restart**
6. Đợi 3-5 phút cho Kubernetes khởi động

### Bước 3: Kiểm tra Kubernetes

```powershell
# Check kubectl installed
kubectl version --client
# Client Version: v1.28.0 hoặc mới hơn

# Check cluster running
kubectl cluster-info
# Kubernetes control plane is running at https://kubernetes.docker.internal:6443

# Check nodes
kubectl get nodes
# NAME             STATUS   ROLES           AGE   VERSION
# docker-desktop   Ready    control-plane   1d    v1.28.0
```

**✅ Nếu thấy output như trên là OK!**

### Bước 4: Cài kubectl (Optional - nếu chưa có)

```powershell
# Download kubectl
curl.exe -LO "https://dl.k8s.io/release/v1.28.0/bin/windows/amd64/kubectl.exe"

# Add to PATH hoặc copy to C:\Windows\System32\
```

## 📁 Cấu trúc thư mục

```
k8s/
├── README.md                          # File này
├── TESTING_GUIDE.md                   # Hướng dẫn test chi tiết
├── TROUBLESHOOTING.md                 # Xử lý lỗi
│
├── 00-namespace/
│   └── namespace.yaml                 # Namespace "microservices"
│
├── 01-config/
│   ├── configmap.yaml                # Config chung
│   └── secrets.yaml                  # Passwords, credentials
│
├── 02-infrastructure/
│   ├── postgres-statefulset.yaml     # PostgreSQL database
│   ├── postgres-service.yaml
│   ├── redis-deployment.yaml         # Redis cache
│   ├── redis-service.yaml
│   ├── zookeeper-statefulset.yaml    # Zookeeper (for Kafka)
│   ├── zookeeper-service.yaml
│   ├── kafka-statefulset.yaml        # Kafka message broker
│   └── kafka-service.yaml
│
├── 03-discovery/
│   ├── eureka-deployment.yaml        # Eureka service registry
│   └── eureka-service.yaml
│
├── 04-services/
│   ├── forum-deployment.yaml         # Forum service
│   ├── forum-service.yaml
│   ├── study-deployment.yaml         # Study Management service
│   ├── study-service.yaml
│   ├── notification-deployment.yaml  # Notification service
│   └── notification-service.yaml
│
├── 05-gateway/
│   ├── gateway-deployment.yaml       # API Gateway
│   ├── gateway-service.yaml
│   └── gateway-ingress.yaml          # External access
│
└── scripts/
    ├── deploy-all.ps1                # Deploy tất cả
    ├── delete-all.ps1                # Xóa tất cả
    ├── status.ps1                    # Check status
    └── logs.ps1                      # Xem logs
```

## 🚀 Deploy từng bước

### Bước 1: Tạo Namespace

**Namespace** là cách phân chia resources trong Kubernetes cluster.

```powershell
# Deploy namespace
kubectl apply -f k8s/00-namespace/namespace.yaml

# Kiểm tra
kubectl get namespaces
# NAME              STATUS   AGE
# microservices     Active   10s
```

**Test:**
```powershell
# Set default namespace (để không phải gõ -n microservices mỗi lần)
kubectl config set-context --current --namespace=microservices

# Verify
kubectl config view --minify | Select-String namespace
```

### Bước 2: Tạo ConfigMaps và Secrets

**ConfigMap**: Lưu configuration không nhạy cảm  
**Secret**: Lưu passwords, credentials (được encode base64)

```powershell
# Deploy configs
kubectl apply -f k8s/01-config/

# Kiểm tra
kubectl get configmaps
kubectl get secrets
```

**Test:**
```powershell
# Xem nội dung ConfigMap
kubectl describe configmap app-config

# Xem Secret (đã encode)
kubectl get secret app-secrets -o yaml

# Decode password
kubectl get secret app-secrets -o jsonpath='{.data.postgres-password}' | base64 -d
# Output: 123456
```

### Bước 3: Deploy Infrastructure Services

Deploy theo thứ tự: **PostgreSQL → Redis → Zookeeper → Kafka**

#### 3.1. PostgreSQL

```powershell
# Deploy PostgreSQL
kubectl apply -f k8s/02-infrastructure/postgres-statefulset.yaml
kubectl apply -f k8s/02-infrastructure/postgres-service.yaml

# Đợi pod ready (30-60 giây)
kubectl wait --for=condition=ready pod -l app=postgres --timeout=120s

# Kiểm tra
kubectl get pods -l app=postgres
# NAME         READY   STATUS    RESTARTS   AGE
# postgres-0   1/1     Running   0          1m
```

**Test PostgreSQL:**
```powershell
# Connect vào PostgreSQL pod
kubectl exec -it postgres-0 -- psql -U postgres

# Trong psql:
\l                              # List databases
\c forum                        # Connect to forum database
\dt                             # List tables
\q                              # Quit

# Hoặc test từ ngoài
kubectl run pg-test --rm -it --image=postgres:15-alpine -- psql -h postgres -U postgres -d forum
```

#### 3.2. Redis

```powershell
# Deploy Redis
kubectl apply -f k8s/02-infrastructure/redis-deployment.yaml
kubectl apply -f k8s/02-infrastructure/redis-service.yaml

# Wait
kubectl wait --for=condition=ready pod -l app=redis --timeout=60s

# Check
kubectl get pods -l app=redis
```

**Test Redis:**
```powershell
# Connect to Redis
kubectl exec -it deployment/redis -- redis-cli

# Test commands:
PING                    # PONG
SET test "Hello K8s"    # OK
GET test                # "Hello K8s"
exit
```

#### 3.3. Zookeeper

```powershell
# Deploy Zookeeper
kubectl apply -f k8s/02-infrastructure/zookeeper-statefulset.yaml
kubectl apply -f k8s/02-infrastructure/zookeeper-service.yaml

# Wait
kubectl wait --for=condition=ready pod -l app=zookeeper --timeout=120s

# Check
kubectl get pods -l app=zookeeper
```

**Test Zookeeper:**
```powershell
# Check Zookeeper status
kubectl exec zookeeper-0 -- zkServer.sh status
# Mode: standalone
```

#### 3.4. Kafka

```powershell
# Deploy Kafka
kubectl apply -f k8s/02-infrastructure/kafka-statefulset.yaml
kubectl apply -f k8s/02-infrastructure/kafka-service.yaml

# Wait (Kafka mất 2-3 phút để start)
kubectl wait --for=condition=ready pod -l app=kafka --timeout=180s

# Check
kubectl get pods -l app=kafka
```

**Test Kafka:**
```powershell
# List topics
kubectl exec kafka-0 -- kafka-topics --bootstrap-server localhost:9092 --list

# Create test topic
kubectl exec kafka-0 -- kafka-topics --bootstrap-server localhost:9092 --create --topic test --partitions 1 --replication-factor 1

# List again
kubectl exec kafka-0 -- kafka-topics --bootstrap-server localhost:9092 --list
# Output: test
```

### Bước 4: Deploy Eureka Server

```powershell
# Deploy Eureka
kubectl apply -f k8s/03-discovery/

# Wait
kubectl wait --for=condition=ready pod -l app=eureka --timeout=120s

# Check
kubectl get pods -l app=eureka
```

**Test Eureka:**
```powershell
# Port forward để access từ browser
kubectl port-forward service/eureka 8761:8761

# Mở browser: http://localhost:8761
# Phải thấy Eureka Dashboard
```

### Bước 5: Deploy Application Services

Deploy theo thứ tự: **Study Management → Forum → Notification**

#### 5.1. Study Management Service

```powershell
# Deploy
kubectl apply -f k8s/04-services/study-deployment.yaml
kubectl apply -f k8s/04-services/study-service.yaml

# Wait
kubectl wait --for=condition=ready pod -l app=study-management --timeout=180s

# Check
kubectl get pods -l app=study-management
```

**Test:**
```powershell
# Port forward
kubectl port-forward service/study-management 8082:8082

# Test API
curl http://localhost:8082/actuator/health
# {"status":"UP"}

# Check Eureka registration
# Refresh http://localhost:8761 - phải thấy STUDY-MANAGEMENT-SERVICE
```

#### 5.2. Forum Service

```powershell
# Deploy
kubectl apply -f k8s/04-services/forum-deployment.yaml
kubectl apply -f k8s/04-services/forum-service.yaml

# Wait
kubectl wait --for=condition=ready pod -l app=forum --timeout=180s

# Check
kubectl get pods -l app=forum
```

**Test:**
```powershell
# Port forward
kubectl port-forward service/forum 8081:8081

# Test API
curl http://localhost:8081/actuator/health

# Test topics endpoint
curl http://localhost:8081/topics
# []

# Check Eureka
# Refresh http://localhost:8761 - phải thấy FORUM-SERVICE
```

#### 5.3. Notification Service

```powershell
# Deploy
kubectl apply -f k8s/04-services/notification-deployment.yaml
kubectl apply -f k8s/04-services/notification-service.yaml

# Wait
kubectl wait --for=condition=ready pod -l app=notification --timeout=120s

# Check
kubectl get pods -l app=notification
```

**Test:**
```powershell
# Check logs
kubectl logs -l app=notification --tail=50

# Phải thấy:
# "Started NotificationServiceApplication"
# "Kafka consumer initialized"
```

### Bước 6: Deploy API Gateway

```powershell
# Deploy Gateway
kubectl apply -f k8s/05-gateway/

# Wait
kubectl wait --for=condition=ready pod -l app=api-gateway --timeout=120s

# Check
kubectl get pods -l app=api-gateway
```

**Test:**
```powershell
# Port forward
kubectl port-forward service/api-gateway 8080:8080

# Test routing
curl http://localhost:8080/api/forum/topics
curl http://localhost:8080/api/study/users

# Check routes
curl http://localhost:8080/actuator/gateway/routes
```

### Bước 7: Verify tất cả services

```powershell
# List all pods
kubectl get pods

# Tất cả phải STATUS = Running, READY = 1/1 hoặc 2/2

# Check services
kubectl get services

# Check Eureka Dashboard
# http://localhost:8761 phải thấy 4 services:
# - API-GATEWAY
# - FORUM-SERVICE
# - STUDY-MANAGEMENT-SERVICE
# - NOTIFICATION-SERVICE
```

## ✅ Kiểm tra từng phần

### Test 1: Infrastructure

```powershell
# PostgreSQL
kubectl exec postgres-0 -- psql -U postgres -c "\l"

# Redis
kubectl exec -it deployment/redis -- redis-cli PING

# Kafka
kubectl exec kafka-0 -- kafka-topics --bootstrap-server localhost:9092 --list

# Zookeeper
kubectl exec zookeeper-0 -- zkServer.sh status
```

### Test 2: Services Health

```powershell
# Port forward all services (mở nhiều terminal)
kubectl port-forward service/eureka 8761:8761
kubectl port-forward service/api-gateway 8080:8080
kubectl port-forward service/forum 8081:8081
kubectl port-forward service/study-management 8082:8082
kubectl port-forward service/notification 8083:8083

# Test health endpoints
curl http://localhost:8761/actuator/health
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
```

### Test 3: End-to-End Flow

```powershell
# 1. Port forward API Gateway
kubectl port-forward service/api-gateway 8080:8080

# 2. Tạo topic
curl -X POST http://localhost:8080/api/forum/topics `
  -H "Content-Type: application/json" `
  -d '{"userId": 1, "title": "K8s Test", "body": "Testing from Kubernetes"}'

# 3. Thêm comment
curl -X POST http://localhost:8080/api/forum/topics/1/comments `
  -H "Content-Type: application/json" `
  -d '{"userId": 2, "body": "Great post!"}'

# 4. Check notification logs
kubectl logs -l app=notification --tail=20

# Phải thấy:
# "Received comment event from Kafka"
# "Successfully sent email"
```

### Test 4: Kafka Integration

```powershell
# Terminal 1: Listen to Kafka topic
kubectl exec -it kafka-0 -- kafka-console-consumer `
  --bootstrap-server localhost:9092 `
  --topic forum.comment.created `
  --from-beginning

# Terminal 2: Create comment (như Test 3)

# Terminal 1 phải nhận được message JSON
```

## 🎯 Deploy tất cả một lúc

Sau khi đã hiểu từng bước, bạn có thể deploy tất cả:

```powershell
# Deploy all
cd k8s
.\scripts\deploy-all.ps1

# Check status
.\scripts\status.ps1

# View logs
.\scripts\logs.ps1 forum

# Delete all
.\scripts\delete-all.ps1
```

## 📊 Monitoring

### View Logs

```powershell
# Logs của 1 service
kubectl logs -l app=forum --tail=100 -f

# Logs của tất cả pods
kubectl logs -l app=forum --all-containers=true

# Logs previous crashed pod
kubectl logs -l app=forum --previous
```

### Describe Resources

```powershell
# Chi tiết pod
kubectl describe pod <pod-name>

# Chi tiết service
kubectl describe service forum

# Chi tiết deployment
kubectl describe deployment forum
```

### Events

```powershell
# Xem events
kubectl get events --sort-by='.lastTimestamp'

# Events của 1 pod
kubectl describe pod <pod-name> | Select-String -Pattern "Events:" -Context 0,20
```

## 🔧 Scaling

### Manual Scaling

```powershell
# Scale forum service to 3 replicas
kubectl scale deployment forum --replicas=3

# Check
kubectl get pods -l app=forum
# forum-xxx   1/1   Running
# forum-yyy   1/1   Running
# forum-zzz   1/1   Running

# Scale back to 1
kubectl scale deployment forum --replicas=1
```

### Auto Scaling (HPA)

```powershell
# Enable metrics server (nếu chưa có)
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

# Create HPA for forum
kubectl autoscale deployment forum --min=2 --max=5 --cpu-percent=70

# Check HPA
kubectl get hpa
```

## 🔄 Updates

### Rolling Update

```powershell
# Update image
kubectl set image deployment/forum forum=your-repo/forum:v2

# Check rollout status
kubectl rollout status deployment/forum

# Rollback nếu có lỗi
kubectl rollout undo deployment/forum
```

## 🗑️ Cleanup

```powershell
# Xóa tất cả resources
kubectl delete namespace microservices

# Hoặc xóa từng loại
kubectl delete -f k8s/05-gateway/
kubectl delete -f k8s/04-services/
kubectl delete -f k8s/03-discovery/
kubectl delete -f k8s/02-infrastructure/
kubectl delete -f k8s/01-config/
kubectl delete -f k8s/00-namespace/
```

## 📚 Tài liệu tham khảo

- [Kubernetes Official Docs](https://kubernetes.io/docs/)
- [kubectl Cheat Sheet](https://kubernetes.io/docs/reference/kubectl/cheatsheet/)
- [Docker Desktop Kubernetes](https://docs.docker.com/desktop/kubernetes/)

## 🎓 Khái niệm quan trọng

### Pod
- Đơn vị nhỏ nhất trong K8s
- Chứa 1 hoặc nhiều containers
- Có IP riêng trong cluster

### Deployment
- Quản lý ReplicaSet và Pods
- Cho phép rolling updates
- Self-healing

### Service
- Expose pods ra ngoài
- Load balancing
- Service discovery

### StatefulSet
- Cho databases cần persistent storage
- Pods có tên cố định (postgres-0, postgres-1...)
- Volumes được giữ lại khi pod restart

### ConfigMap & Secret
- ConfigMap: Config không nhạy cảm
- Secret: Passwords, API keys (base64 encoded)

### Ingress
- HTTP/HTTPS routing từ ngoài vào cluster
- Load balancer layer 7

---

**🎉 Chúc mừng! Bạn đã deploy microservices lên Kubernetes thành công!**

Xem thêm:
- [TESTING_GUIDE.md](TESTING_GUIDE.md) - Hướng dẫn test chi tiết
- [TROUBLESHOOTING.md](TROUBLESHOOTING.md) - Xử lý lỗi thường gặp
