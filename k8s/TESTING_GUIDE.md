# 🧪 Hướng dẫn Test Kubernetes Chi Tiết

## 📋 Tổng quan

File này hướng dẫn test từng phần của hệ thống microservices trên Kubernetes.

## 🎯 Test Flow

```
1. Infrastructure (PostgreSQL, Redis, Kafka, Zookeeper)
   ↓
2. Service Discovery (Eureka)
   ↓
3. Application Services (Study Management, Forum, Notification)
   ↓
4. API Gateway
   ↓
5. End-to-End Integration Test
```

## 1️⃣ Test Infrastructure

### PostgreSQL

```powershell
# 1. Check pod running
kubectl get pods -l app=postgres
# STATUS phải là Running

# 2. Check logs
kubectl logs postgres-0 --tail=20
# Phải thấy: "database system is ready to accept connections"

# 3. Connect và test
kubectl exec -it postgres-0 -- psql -U postgres

# Trong psql, chạy:
\l                              # List databases
CREATE DATABASE study_management;
\l                              # Phải thấy 2 databases: forum, study_management
\c forum                        # Connect to forum
\dt                             # List tables (sẽ trống nếu chưa run migration)
SELECT version();               # Check PostgreSQL version
\q                              # Exit

# 4. Test connection từ ngoài cluster (optional)
kubectl port-forward postgres-0 5432:5432
# Terminal khác:
psql -h localhost -U postgres -d forum
# Password: 123456
```

**✅ Expected Output:**
```
\l
                                 List of databases
       Name        |  Owner   | Encoding | Collate |  Ctype  
-------------------+----------+----------+---------+---------
 forum             | postgres | UTF8     | C       | C
 study_management  | postgres | UTF8     | C       | C
```

### Redis

```powershell
# 1. Check pod
kubectl get pods -l app=redis

# 2. Connect to Redis
kubectl exec -it deployment/redis -- redis-cli

# Test commands:
PING                            # PONG
SET kubernetes "test value"     # OK
GET kubernetes                  # "test value"
KEYS *                          # List all keys
INFO server                     # Redis server info
DBSIZE                          # Number of keys
exit

# 3. Test persistence
kubectl exec -it deployment/redis -- redis-cli SET persist "data"
kubectl delete pod -l app=redis  # Delete pod
# Đợi pod restart
kubectl exec -it deployment/redis -- redis-cli GET persist
# Output: "data" (data vẫn còn sau khi pod restart)
```

**✅ Expected Output:**
```
127.0.0.1:6379> PING
PONG
127.0.0.1:6379> INFO server
# Server
redis_version:7.0.x
```

### Zookeeper

```powershell
# 1. Check pod
kubectl get pods -l app=zookeeper

# 2. Check Zookeeper status
kubectl exec zookeeper-0 -- zkServer.sh status
# Mode: standalone

# 3. Connect to Zookeeper CLI
kubectl exec -it zookeeper-0 -- zkCli.sh

# Trong zkCli:
ls /                            # List root znodes
create /test "test data"        # Create znode
get /test                       # Get data
delete /test                    # Delete znode
quit

# 4. Check Zookeeper logs
kubectl logs zookeeper-0 --tail=20
```

**✅ Expected Output:**
```
Mode: standalone
```

### Kafka

```powershell
# 1. Check pod
kubectl get pods -l app=kafka

# 2. Wait for Kafka to be ready (2-3 phút)
kubectl wait --for=condition=ready pod -l app=kafka --timeout=180s

# 3. List topics
kubectl exec kafka-0 -- kafka-topics --bootstrap-server localhost:9092 --list
# Có thể trống nếu chưa có topic nào

# 4. Create test topic
kubectl exec kafka-0 -- kafka-topics `
  --bootstrap-server localhost:9092 `
  --create --topic test-topic `
  --partitions 3 `
  --replication-factor 1

# 5. List topics again
kubectl exec kafka-0 -- kafka-topics --bootstrap-server localhost:9092 --list
# Output: test-topic

# 6. Describe topic
kubectl exec kafka-0 -- kafka-topics `
  --bootstrap-server localhost:9092 `
  --describe --topic test-topic

# 7. Test Producer-Consumer
# Terminal 1: Consumer
kubectl exec -it kafka-0 -- kafka-console-consumer `
  --bootstrap-server localhost:9092 `
  --topic test-topic `
  --from-beginning

# Terminal 2: Producer
kubectl exec -it kafka-0 -- kafka-console-producer `
  --bootstrap-server localhost:9092 `
  --topic test-topic

# Type messages in Terminal 2, see them in Terminal 1
```

**✅ Expected Output:**
```
Topic: test-topic
PartitionCount: 3
ReplicationFactor: 1
```

## 2️⃣ Test Eureka Server

```powershell
# 1. Check pod
kubectl get pods -l app=eureka

# 2. Check logs
kubectl logs -l app=eureka --tail=50
# Phải thấy: "Started EurekaServerApplication"

# 3. Port forward
kubectl port-forward service/eureka 8761:8761

# 4. Open browser
start http://localhost:8761

# 5. Check health
curl http://localhost:8761/actuator/health
# {"status":"UP"}

# 6. List registered apps (API)
curl http://localhost:8761/eureka/apps
# XML response với danh sách services
```

**✅ Expected on Dashboard:**
- Eureka server hiển thị
- "Instances currently registered with Eureka" section
- Sau khi deploy services, phải thấy 4 services: API-GATEWAY, FORUM-SERVICE, STUDY-MANAGEMENT-SERVICE, NOTIFICATION-SERVICE

## 3️⃣ Test Application Services

### Study Management Service

```powershell
# 1. Check pod
kubectl get pods -l app=study-management

# 2. Check logs
kubectl logs -l app=study-management --tail=100
# Phải thấy:
# - "Started StudyManagementApplication"
# - "Registered with Eureka"
# - "gRPC Server started"

# 3. Port forward
kubectl port-forward service/study-management 8082:8082 9090:9090

# 4. Test health
curl http://localhost:8082/actuator/health
# {"status":"UP"}

# 5. Test API endpoints
# Register user
curl -X POST http://localhost:8082/auth/register `
  -H "Content-Type: application/json" `
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "name": "Test User",
    "role": "STUDENT"
  }'

# Get users
curl http://localhost:8082/users

# 6. Check gRPC port
netstat -an | Select-String "9090"
# Phải thấy port 9090 LISTENING
```

**✅ Expected Output:**
```json
{
  "id": 1,
  "username": "testuser",
  "email": "test@example.com",
  "name": "Test User",
  "role": "STUDENT"
}
```

### Forum Service

```powershell
# 1. Check pod
kubectl get pods -l app=forum

# 2. Check logs
kubectl logs -l app=forum --tail=100 -f
# Phải thấy:
# - "Started ForumApplication"
# - "Kafka producer initialized"
# - "gRPC client initialized"

# 3. Port forward
kubectl port-forward service/forum 8081:8081

# 4. Test health
curl http://localhost:8081/actuator/health

# 5. Test API
# Create topic
curl -X POST http://localhost:8081/topics `
  -H "Content-Type: application/json" `
  -d '{
    "userId": 1,
    "title": "Kubernetes Test",
    "body": "Testing from K8s cluster"
  }'

# Get topics
curl http://localhost:8081/topics
# Output: [{"id":1,"userId":1,"title":"Kubernetes Test",...}]

# Get specific topic
curl http://localhost:8081/topics/1

# 6. Test gRPC integration
# Topic detail phải include user info từ Study Management via gRPC
curl http://localhost:8081/topics/1
# Response phải có user.name, user.email
```

**✅ Expected Output:**
```json
{
  "id": 1,
  "userId": 1,
  "title": "Kubernetes Test",
  "body": "Testing from K8s cluster",
  "user": {
    "id": 1,
    "name": "Test User",
    "email": "test@example.com"
  },
  "createdAt": "2024-01-15T10:30:00"
}
```

### Notification Service

```powershell
# 1. Check pod
kubectl get pods -l app=notification

# 2. Check logs (important!)
kubectl logs -l app=notification --tail=100 -f
# Phải thấy:
# - "Started NotificationServiceApplication"
# - "Kafka consumer group: notification-service-group"
# - "Listening to topic: forum.comment.created"

# 3. Port forward
kubectl port-forward service/notification 8083:8083

# 4. Test health
curl http://localhost:8083/actuator/health

# 5. Test Kafka consumer
# Create comment (sẽ trigger notification)
curl -X POST http://localhost:8081/topics/1/comments `
  -H "Content-Type: application/json" `
  -d '{
    "userId": 2,
    "body": "Nice post! Testing notification."
  }'

# 6. Check notification logs
kubectl logs -l app=notification --tail=20
# Phải thấy:
# "Received comment event from Kafka: commentId=1"
# "Sending notification email to: test@example.com"
# "Successfully sent email"
```

**✅ Expected Log Output:**
```
Received comment event from Kafka: CommentCreatedEvent(commentId=1, topicId=1, userId=2, ...)
Fetching topic details: topicId=1
Sending notification to topic creator: test@example.com
Email sent successfully to: test@example.com
```

## 4️⃣ Test API Gateway

```powershell
# 1. Check pod
kubectl get pods -l app=api-gateway

# 2. Check logs
kubectl logs -l app=api-gateway --tail=100
# Phải thấy:
# - "Started ApiGatewayApplication"
# - Route configurations loaded

# 3. Port forward
kubectl port-forward service/api-gateway 8080:8080

# 4. Test health
curl http://localhost:8080/actuator/health

# 5. Test routes
# List all routes
curl http://localhost:8080/actuator/gateway/routes | ConvertFrom-Json | Format-List

# 6. Test routing to Forum Service
curl http://localhost:8080/api/forum/topics
# Phải return list topics

# 7. Test routing to Study Management
curl http://localhost:8080/api/study/users
# Phải return list users

# 8. Test path stripping
# Request: /api/forum/topics
# Gateway strips /api/forum
# Routes to: forum-service/topics
# Verify in logs:
kubectl logs -l app=forum --tail=5
# Phải thấy: "GET /topics" (không phải /api/forum/topics)
```

**✅ Expected Routes:**
```
route_id: forum-service
uri: lb://forum-service
predicates: [Path=/api/forum/**]
filters: [StripPrefix=2]

route_id: study-management-service
uri: lb://study-management-service
predicates: [Path=/api/study/**]
filters: [StripPrefix=2]
```

## 5️⃣ End-to-End Integration Test

### Full User Flow

```powershell
# Port forward API Gateway
kubectl port-forward service/api-gateway 8080:8080

# Step 1: Register User
$user = curl -X POST http://localhost:8080/api/study/auth/register `
  -H "Content-Type: application/json" `
  -d '{
    "username": "john",
    "email": "john@example.com",
    "password": "pass123",
    "name": "John Doe",
    "role": "STUDENT"
  }' | ConvertFrom-Json

Write-Host "User ID: $($user.id)"

# Step 2: Create Topic
$topic = curl -X POST http://localhost:8080/api/forum/topics `
  -H "Content-Type: application/json" `
  -d "{
    `"userId`": $($user.id),
    `"title`": `"E2E Test Topic`",
    `"body`": `"Full integration test`"
  }" | ConvertFrom-Json

Write-Host "Topic ID: $($topic.id)"

# Step 3: Add Comment (triggers notification)
curl -X POST "http://localhost:8080/api/forum/topics/$($topic.id)/comments" `
  -H "Content-Type: application/json" `
  -d '{
    "userId": 2,
    "body": "Great topic!"
  }'

# Step 4: Verify Kafka message
kubectl logs -l app=notification --tail=10
# Phải thấy notification được gửi

# Step 5: Check Mailtrap inbox
Write-Host "Check your Mailtrap inbox for email!"
```

### Test Service Discovery

```powershell
# 1. Scale forum service to 3 replicas
kubectl scale deployment forum --replicas=3

# 2. Wait for all pods ready
kubectl wait --for=condition=ready pod -l app=forum --timeout=180s

# 3. Check all pods
kubectl get pods -l app=forum
# forum-xxx   1/1   Running
# forum-yyy   1/1   Running
# forum-zzz   1/1   Running

# 4. Check Eureka shows 3 instances
# Browser: http://localhost:8761
# forum-service phải show 3 instances

# 5. Test load balancing
# Send multiple requests, check which pod handles
for ($i=1; $i -le 10; $i++) {
    curl http://localhost:8080/api/forum/topics
    Start-Sleep -Milliseconds 500
}

# 6. Check logs of all forum pods
kubectl logs -l app=forum --all-containers=true --tail=5
# Phải thấy requests được distribute đến các pods khác nhau

# 7. Scale back
kubectl scale deployment forum --replicas=1
```

### Test Fault Tolerance

```powershell
# 1. Delete forum pod
kubectl delete pod -l app=forum

# 2. Immediately test API
curl http://localhost:8080/api/forum/topics
# Có thể failed hoặc slow

# 3. Wait for pod to restart
kubectl wait --for=condition=ready pod -l app=forum --timeout=120s

# 4. Test again
curl http://localhost:8080/api/forum/topics
# Phải work bình thường

# 5. Verify pod auto-restarted
kubectl get pods -l app=forum
# RESTARTS phải > 0
```

### Test Database Persistence

```powershell
# 1. Create data
curl -X POST http://localhost:8080/api/forum/topics `
  -H "Content-Type: application/json" `
  -d '{"userId": 1, "title": "Persistence Test", "body": "Test"}'

# 2. Get topic ID
$topics = curl http://localhost:8080/api/forum/topics | ConvertFrom-Json
$topicId = $topics[0].id

# 3. Delete forum pod
kubectl delete pod -l app=forum

# 4. Wait for restart
kubectl wait --for=condition=ready pod -l app=forum --timeout=120s

# 5. Check data still exists
curl "http://localhost:8080/api/forum/topics/$topicId"
# Phải vẫn thấy topic (data persisted in PostgreSQL)
```

## 6️⃣ Performance Testing

### Load Test với Apache Bench

```powershell
# Install Apache Bench (if not installed)
# Download from: https://httpd.apache.org/docs/2.4/programs/ab.html

# Test 1: Simple GET requests
ab -n 1000 -c 10 http://localhost:8080/api/forum/topics
# -n: total requests
# -c: concurrent requests

# Test 2: POST requests
ab -n 100 -c 5 -p topic.json -T application/json http://localhost:8080/api/forum/topics

# topic.json content:
# {"userId": 1, "title": "Load Test", "body": "Testing"}

# Monitor pods during load test
kubectl top pods
```

### Resource Monitoring

```powershell
# Enable metrics server (if not enabled)
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

# Wait 1-2 minutes for metrics to be available

# Check pod resource usage
kubectl top pods

# Check node usage
kubectl top nodes

# Watch resources in real-time
kubectl top pods --watch
```

## 7️⃣ Troubleshooting Commands

```powershell
# 1. Pod not starting
kubectl describe pod <pod-name>
kubectl logs <pod-name>
kubectl get events --sort-by='.lastTimestamp'

# 2. Service not accessible
kubectl get endpoints
kubectl describe service <service-name>

# 3. Database connection issues
kubectl exec -it postgres-0 -- psql -U postgres -c "SELECT * FROM pg_stat_activity;"

# 4. Kafka issues
kubectl exec kafka-0 -- kafka-consumer-groups --bootstrap-server localhost:9092 --list
kubectl exec kafka-0 -- kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group notification-service-group

# 5. Network debugging
kubectl run debug --rm -it --image=nicolaka/netshoot -- /bin/bash
# Trong container:
nslookup postgres
nslookup kafka
curl http://forum:8081/actuator/health
```

## ✅ Test Checklist

Sau khi deploy, check tất cả:

- [ ] PostgreSQL: Connected, databases created
- [ ] Redis: PING returns PONG
- [ ] Zookeeper: Status is standalone
- [ ] Kafka: Topics can be created
- [ ] Eureka: Dashboard accessible, shows all services
- [ ] Study Management: Health OK, gRPC port open
- [ ] Forum: Health OK, can create topics
- [ ] Notification: Kafka consumer connected
- [ ] API Gateway: Routes working, can access all services
- [ ] E2E: Create user → Create topic → Add comment → Email sent
- [ ] Scaling: Can scale up/down, load balancing works
- [ ] Fault Tolerance: Pods auto-restart, data persists
- [ ] Monitoring: Metrics available, logs accessible

---

**🎉 Nếu tất cả đều PASS, hệ thống của bạn đã ready!**
