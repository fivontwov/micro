# 🚀 Quick Start - Notification System

Hướng dẫn nhanh để chạy toàn bộ hệ thống notification.

## 🎯 Tổng quan

**Flow:** Comment mới → Kafka → Email thông báo

```
User tạo comment
    ↓
Forum Service → Kafka
    ↓
Notification Service → Email
    ↓
📧 Mailtrap inbox
```

## ⚡ Cách chạy nhanh nhất

### Option 1: Local Development (Recommended)

```powershell
# 1. Start infrastructure
docker-compose up -d zookeeper kafka postgres redis eureka-server

# Đợi 30 giây...

# 2. Start Study Management
cd ssstudy_management
mvn spring-boot:run

# 3. Start Forum Service (Terminal mới)
cd forum
mvn spring-boot:run

# 4. Start Notification Service (Terminal mới)
cd notification-service
mvn spring-boot:run
```

### Option 2: Full Docker

```powershell
# Build và start tất cả
docker-compose up --build

# Hoặc background mode
docker-compose up -d --build
```

## ✅ Kiểm tra services đã chạy

```powershell
# Check containers
docker ps

# Check Eureka (should see 3 services)
start http://localhost:8761

# Check health
curl http://localhost:8081/actuator/health  # Forum
curl http://localhost:8083/actuator/health  # Notification
```

## 🧪 Test ngay

### 1. Tạo topic

```powershell
curl -X POST http://localhost:8081/topics \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "title": "Test Topic",
    "body": "This is a test topic"
  }'
```

### 2. Thêm comment

```powershell
curl -X POST http://localhost:8081/topics/1/comments \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "body": "Great topic! Thanks for sharing."
  }'
```

### 3. Check logs

**Notification Service logs phải thấy:**
```
Received comment event from Kafka: commentId=1
Sending notification to topic creator: user1@example.com
Successfully sent email to: user1@example.com
```

### 4. Check email trong Mailtrap

1. Visit https://mailtrap.io/inboxes
2. Login với account của bạn
3. Xem email mới trong inbox!

**Email sẽ có:**
- Subject: "User Two commented on your topic 'Test Topic'"
- Beautiful HTML template với gradient header
- Comment preview
- Action button

## 📧 Mailtrap Configuration

Email được gửi qua **Mailtrap** (test SMTP server):

```
Host: sandbox.smtp.mailtrap.io
Port: 2525
Username: 109b298932e5e4
Password: f22f288279a5de
```

**Không có email thật nào được gửi!** Tất cả emails nằm trong Mailtrap inbox.

## 🎬 Demo Scenarios

### Scenario 1: Direct Comment

```powershell
# User 2 comments on User 1's topic
curl -X POST http://localhost:8081/topics/1/comments \
  -H "Content-Type: application/json" \
  -d '{"userId": 2, "body": "Nice post!"}'

# Result: 1 email to User 1 (topic creator)
```

### Scenario 2: Reply Comment

```powershell
# User 3 replies to User 2's comment
curl -X POST http://localhost:8081/topics/1/comments \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 3,
    "parentCommentId": 1,
    "body": "I agree!"
  }'

# Result: 2 emails
# - To User 1 (topic creator)
# - To User 2 (parent comment creator)
```

### Scenario 3: Self Comment (No notification)

```powershell
# User 1 comments on their own topic
curl -X POST http://localhost:8081/topics/1/comments \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "body": "Update!"}'

# Result: 0 emails (avoid self-notification)
```

## 🐛 Troubleshooting

### Kafka không connect được

```powershell
# Check Kafka running
docker ps | findstr kafka

# Restart Kafka
docker-compose restart kafka
```

### Không nhận được email

**Check 1:** Notification Service logs
```
✅ "Received comment event from Kafka"
✅ "Successfully sent email to..."
```

**Check 2:** Mailtrap credentials trong `application.yml`

**Check 3:** Refresh Mailtrap inbox page

### gRPC error - User not found

```sql
-- Check users exist
SELECT * FROM users;

-- Create test users nếu chưa có
INSERT INTO users (id, username, email, name, role, password) 
VALUES 
  (1, 'user1', 'user1@example.com', 'User One', 'STUDENT', 'password'),
  (2, 'user2', 'user2@example.com', 'User Two', 'STUDENT', 'password'),
  (3, 'user3', 'user3@example.com', 'User Three', 'STUDENT', 'password');
```

## 📊 Monitoring

### View Kafka messages

```powershell
docker exec -it micro-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic forum.comment.created \
  --from-beginning
```

### Check consumer lag

```powershell
docker exec micro-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group notification-service-group \
  --describe
```

**LAG should be 0** (consumer up-to-date)

## 🎯 Ports

| Service | Port | URL |
|---------|------|-----|
| Eureka | 8761 | http://localhost:8761 |
| Forum | 8081 | http://localhost:8081 |
| Study Management | 8082 | http://localhost:8082 |
| Notification | 8083 | http://localhost:8083 |
| API Gateway | 8080 | http://localhost:8080 |
| Kafka | 9093 | localhost:9093 (local) |
| Postgres | 5432 | localhost:5432 |
| Redis | 6379 | localhost:6379 |

## 📚 Tài liệu chi tiết

- [kafka/README.md](kafka/README.md) - Tổng quan Kafka integration
- [kafka/STEP_5_NOTIFICATION_SERVICE.md](kafka/STEP_5_NOTIFICATION_SERVICE.md) - Chi tiết Notification Service
- [kafka/STEP_6_TEST_END_TO_END.md](kafka/STEP_6_TEST_END_TO_END.md) - Hướng dẫn test chi tiết
- [notification-service/README.md](notification-service/README.md) - Notification Service docs

## 🎉 Demo nhanh (1 lệnh)

```powershell
# Tạo topic và comment trong 1 lệnh
curl -X POST http://localhost:8081/topics -H "Content-Type: application/json" -d '{"userId": 1, "title": "Demo", "body": "Test"}'; curl -X POST http://localhost:8081/topics/1/comments -H "Content-Type: application/json" -d '{"userId": 2, "body": "Nice!"}'

# Sau đó check Mailtrap inbox!
```

---

**Lưu ý:** Lần đầu chạy có thể mất 1-2 phút để download dependencies và start services. Hãy kiên nhẫn! ☕
