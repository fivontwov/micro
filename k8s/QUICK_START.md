# ⚡ Quick Start - Kubernetes

Hướng dẫn nhanh để deploy microservices lên Kubernetes trong 5 phút!

## 🎯 Prerequisites

1. **Docker Desktop** đã cài và đang chạy
2. **Kubernetes** đã enable trong Docker Desktop
3. **kubectl** đã cài đặt (đi kèm với Docker Desktop)

## 🚀 Quick Deploy (3 bước)

### Bước 1: Build Docker Images

```powershell
cd k8s\scripts
.\build-images.ps1
```

Script này sẽ build tất cả 5 images:
- micro-eureka-server
- micro-forum-service
- micro-study-management
- micro-api-gateway
- micro-notification-service

⏱️ **Thời gian:** ~5-10 phút

### Bước 2: Deploy to Kubernetes

```powershell
.\deploy-all.ps1
```

Script này sẽ deploy theo thứ tự:
1. Namespace & Config
2. Infrastructure (PostgreSQL, Redis, Zookeeper, Kafka)
3. Eureka Server
4. Application Services (Study, Forum, Notification)
5. API Gateway

⏱️ **Thời gian:** ~3-5 phút

### Bước 3: Test

```powershell
.\quick-test.ps1
```

Script này sẽ:
- Test API Gateway health
- Tạo topic
- Tạo comment (trigger notification)
- Check Kafka event
- Verify end-to-end flow

⏱️ **Thời gian:** ~30 giây

## ✅ Verify Deployment

### Check Pod Status

```powershell
kubectl get pods -n microservices
```

**Expected output:**
```
NAME                              READY   STATUS    RESTARTS   AGE
api-gateway-xxx                   1/1     Running   0          2m
eureka-xxx                        1/1     Running   0          3m
forum-xxx                         1/1     Running   0          2m
forum-postgres-0                  1/1     Running   0          5m
kafka-0                           1/1     Running   0          4m
notification-xxx                  1/1     Running   0          2m
redis-xxx                         1/1     Running   0          4m
study-management-xxx              1/1     Running   0          2m
study-postgres-0                  1/1     Running   0          5m
zookeeper-0                       1/1     Running   0          4m
```

Tất cả phải **STATUS = Running** và **READY = 1/1**

### Check Services

```powershell
kubectl get services -n microservices
```

### Check Eureka Dashboard

```powershell
kubectl port-forward -n microservices service/eureka 8761:8761
```

Mở browser: http://localhost:8761

**Phải thấy 4 services registered:**
- API-GATEWAY
- FORUM-SERVICE
- STUDY-MANAGEMENT-SERVICE
- NOTIFICATION-SERVICE

## 🧪 Test APIs

### Port Forward API Gateway

```powershell
kubectl port-forward -n microservices service/api-gateway 8080:8080
```

### Test Endpoints

```powershell
# Health check
curl http://localhost:8080/actuator/health

# Create topic
curl -X POST http://localhost:8080/api/forum/topics `
  -H "Content-Type: application/json" `
  -d '{"userId": 1, "title": "Test", "body": "Hello K8s"}'

# Get topics
curl http://localhost:8080/api/forum/topics

# Create comment (triggers notification)
curl -X POST http://localhost:8080/api/forum/topics/1/comments `
  -H "Content-Type: application/json" `
  -d '{"userId": 2, "body": "Great post!"}'
```

### Check Notification Logs

```powershell
.\logs.ps1 notification -Lines 20 -Follow
```

Phải thấy:
```
Received comment event from Kafka: commentId=1
Sending notification to: user@example.com
Successfully sent email
```

## 🔧 Useful Commands

```powershell
# Check status
.\status.ps1

# View logs
.\logs.ps1 forum
.\logs.ps1 notification -Follow

# Port forward all services
.\port-forward.ps1 all

# Delete everything
.\delete-all.ps1
```

## 📊 Resource Usage

Default resource allocations:

| Service | CPU Request | Memory Request | CPU Limit | Memory Limit |
|---------|-------------|----------------|-----------|--------------|
| PostgreSQL | 250m | 256Mi | 500m | 512Mi |
| Redis | 100m | 128Mi | 200m | 256Mi |
| Zookeeper | 250m | 256Mi | 500m | 512Mi |
| Kafka | 500m | 512Mi | 1000m | 1Gi |
| Eureka | 500m | 512Mi | 1000m | 1Gi |
| Forum | 500m | 512Mi | 1000m | 1Gi |
| Study Mgmt | 500m | 512Mi | 1000m | 1Gi |
| Notification | 500m | 512Mi | 1000m | 1Gi |
| API Gateway | 500m | 512Mi | 1000m | 1Gi |

**Total:** ~3.5 CPU cores, ~4.5 GB RAM

**Khuyến nghị:** Docker Desktop nên có **tối thiểu 8 GB RAM** và **4 CPU cores**

## 🐛 Troubleshooting

### Pod không start

```powershell
# Check pod details
kubectl describe pod <pod-name> -n microservices

# Check logs
kubectl logs <pod-name> -n microservices --tail=50
```

### ImagePullBackOff error

```
Error: Failed to pull image
```

**Fix:** Build lại images
```powershell
.\build-images.ps1
kubectl delete pod <pod-name> -n microservices
```

### Database connection error

```
Error: Connection to postgres refused
```

**Fix:** PostgreSQL phải start trước
```powershell
kubectl wait --for=condition=ready pod -l app=postgres -n microservices --timeout=120s
kubectl rollout restart deployment/forum -n microservices
```

### Eureka registration failed

```
Error: Connection refused to eureka:8761
```

**Fix:** Eureka phải start trước
```powershell
kubectl wait --for=condition=ready pod -l app=eureka -n microservices --timeout=120s
kubectl rollout restart deployment/forum -n microservices
```

## 📚 Chi tiết hơn

- [README.md](README.md) - Hướng dẫn đầy đủ từng bước
- [TESTING_GUIDE.md](TESTING_GUIDE.md) - Hướng dẫn test chi tiết
- [TROUBLESHOOTING.md](TROUBLESHOOTING.md) - Xử lý lỗi

## 🎯 Next Steps

1. **Scale services:**
```powershell
kubectl scale deployment forum -n microservices --replicas=3
```

2. **Monitor resources:**
```powershell
kubectl top pods -n microservices
```

3. **View Kafka messages:**
```powershell
kubectl exec kafka-0 -n microservices -- kafka-console-consumer `
  --bootstrap-server localhost:9092 `
  --topic forum.comment.created `
  --from-beginning
```

4. **Access databases:**
```powershell
kubectl exec -it postgres-0 -n microservices -- psql -U postgres
kubectl exec -it deployment/redis -n microservices -- redis-cli
```

---

**🎉 Chúc mừng! Bạn đã deploy microservices lên Kubernetes thành công!**

Nếu có thắc mắc, xem [TROUBLESHOOTING.md](TROUBLESHOOTING.md) hoặc check logs:
```powershell
.\logs.ps1 <service-name>
```
