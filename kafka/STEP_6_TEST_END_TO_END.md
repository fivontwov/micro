# BƯỚC 6: Test End-to-End - Comment → Kafka → Email

## 🎯 Mục tiêu

Test toàn bộ flow từ khi user tạo comment cho đến khi nhận được email thông báo.

## 🚀 Setup

### Option 1: Local (Recommended for testing)

```powershell
# Terminal 1: Infrastructure (Kafka, Postgres, Redis)
docker-compose up -d zookeeper kafka postgres redis eureka-server

# Đợi 30s cho services khởi động

# Terminal 2: Study Management Service
cd ssstudy_management
mvn spring-boot:run

# Terminal 3: Forum Service
cd forum
mvn spring-boot:run

# Terminal 4: Notification Service
cd notification-service
mvn spring-boot:run
```

### Option 2: Full Docker

```powershell
# Start tất cả services
docker-compose up --build

# Hoặc background mode
docker-compose up -d --build
```

## ✅ Pre-flight checks

### 1. Check all services running

```powershell
# Check Docker containers
docker ps

# Phải thấy:
# - micro-zookeeper
# - micro-kafka
# - micro-postgres
# - micro-redis
# - micro-eureka-server
# (+ study-management, forum, notification nếu dùng Docker)
```

### 2. Check Eureka Dashboard

Visit http://localhost:8761

**Phải thấy:**
- FORUM-SERVICE (1 instance)
- STUDY-MANAGEMENT-SERVICE (1 instance)
- NOTIFICATION-SERVICE (1 instance)

### 3. Check Kafka topics

```powershell
docker exec micro-kafka kafka-topics \
  --list --bootstrap-server localhost:9092
```

**Có thể có hoặc không có:** `forum.comment.created` (auto-create)

### 4. Check services health

```powershell
# Forum Service
curl http://localhost:8081/actuator/health

# Notification Service
curl http://localhost:8083/actuator/health

# Study Management
curl http://localhost:8082/actuator/health
```

**All phải return:** `{"status":"UP"}`

## 🧪 Test Scenarios

### Test Case 1: Direct Comment → Notify Topic Creator

**Setup:**
- Topic ID: 1, Creator: User 1
- Commenter: User 2

**Step 1: Create topic**

```powershell
curl -X POST http://localhost:8081/topics \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "title": "Spring Boot Best Practices",
    "body": "What are the best practices for Spring Boot?"
  }'
```

**Response:**
```json
{
  "id": 1,
  "userId": 1,
  "title": "Spring Boot Best Practices",
  "body": "What are the best practices for Spring Boot?",
  "createdAt": "2026-01-22T..."
}
```

**Step 2: Add comment (different user)**

```powershell
curl -X POST http://localhost:8081/topics/1/comments \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "body": "Always use @Transactional for database operations!"
  }'
```

**Response:**
```json
{
  "id": 1,
  "topicId": 1,
  "userId": 2,
  "parentCommentId": null,
  "body": "Always use @Transactional for database operations!",
  "createdAt": "2026-01-22T..."
}
```

**Step 3: Check Forum Service logs**

```
Sending comment created event to Kafka: commentId=1, topicId=1
Successfully sent comment event to Kafka: topic=forum.comment.created, partition=0, offset=0
```

**Step 4: Check Notification Service logs**

```
Received comment event from Kafka: commentId=1, topicId=1, commenterId=2
Sending notification to topic creator: user1@example.com
Preparing email to: user1@example.com, commenter: User Two, topic: Spring Boot Best Practices
Successfully sent email to: user1@example.com
Successfully processed comment event: commentId=1
```

**Step 5: Check Mailtrap inbox**

Visit https://mailtrap.io/inboxes

**Expected email:**
- **To:** user1@example.com
- **Subject:** "User Two commented on your topic 'Spring Boot Best Practices'"
- **Badge:** NEW COMMENT
- **Commenter:** User Two
- **Comment:** "Always use @Transactional for database operations!"

**✅ Pass criteria:**
- [x] Comment saved to database
- [x] Event sent to Kafka
- [x] Consumer received event
- [x] Email sent successfully
- [x] Email visible in Mailtrap

---

### Test Case 2: Reply Comment → Notify Topic Creator + Parent Comment Creator

**Setup:**
- Topic ID: 1, Creator: User 1
- Comment #1: by User 2
- Reply to Comment #1: by User 3

**Step 1: Add reply comment**

```powershell
curl -X POST http://localhost:8081/topics/1/comments \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 3,
    "parentCommentId": 1,
    "body": "Great tip! I also recommend using DTOs for API responses."
  }'
```

**Response:**
```json
{
  "id": 2,
  "topicId": 1,
  "userId": 3,
  "parentCommentId": 1,
  "body": "Great tip! I also recommend using DTOs for API responses.",
  "createdAt": "2026-01-22T..."
}
```

**Step 2: Check Notification Service logs**

```
Received comment event from Kafka: commentId=2, topicId=1, commenterId=3
Sending notification to topic creator: user1@example.com
Sending reply notification to parent comment creator: user2@example.com
Successfully sent email to: user1@example.com
Successfully sent email to: user2@example.com
Successfully processed comment event: commentId=2
```

**Step 3: Check Mailtrap inbox**

**Email 1 (to topic creator):**
- **To:** user1@example.com
- **Subject:** "User Three commented on your topic 'Spring Boot Best Practices'"
- **Badge:** NEW COMMENT

**Email 2 (to parent comment creator):**
- **To:** user2@example.com
- **Subject:** "User Three replied to your comment on 'Spring Boot Best Practices'"
- **Badge:** REPLY TO YOUR COMMENT

**✅ Pass criteria:**
- [x] Reply comment saved with parentCommentId
- [x] Event sent to Kafka
- [x] 2 emails sent (topic creator + parent comment creator)
- [x] Both emails visible in Mailtrap

---

### Test Case 3: Self Comment → No Notification

**Setup:**
- Topic ID: 1, Creator: User 1
- Commenter: User 1 (same person)

**Step 1: Add self-comment**

```powershell
curl -X POST http://localhost:8081/topics/1/comments \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "body": "Update: I found a better solution!"
  }'
```

**Step 2: Check Notification Service logs**

```
Received comment event from Kafka: commentId=3, topicId=1, commenterId=1
Successfully processed comment event: commentId=3
```

**Notice:** NO "Sending notification..." log

**Step 3: Check Mailtrap inbox**

**Expected:** No new email for this comment

**✅ Pass criteria:**
- [x] Comment saved
- [x] Event sent to Kafka
- [x] Consumer received event
- [x] NO email sent (avoid self-notification)

---

### Test Case 4: Multiple Comments in Quick Succession

**Step 1: Send 5 comments rapidly**

```powershell
# Comment 1
curl -X POST http://localhost:8081/topics/1/comments -H "Content-Type: application/json" -d '{"userId": 2, "body": "Comment 1"}'

# Comment 2
curl -X POST http://localhost:8081/topics/1/comments -H "Content-Type: application/json" -d '{"userId": 3, "body": "Comment 2"}'

# Comment 3
curl -X POST http://localhost:8081/topics/1/comments -H "Content-Type: application/json" -d '{"userId": 2, "body": "Comment 3"}'

# Comment 4
curl -X POST http://localhost:8081/topics/1/comments -H "Content-Type: application/json" -d '{"userId": 3, "body": "Comment 4"}'

# Comment 5
curl -X POST http://localhost:8081/topics/1/comments -H "Content-Type: application/json" -d '{"userId": 1, "body": "Thanks all!"}'
```

**Step 2: Check all processed**

```powershell
# Check Kafka messages count
docker exec micro-kafka kafka-run-class kafka.tools.GetOffsetShell \
  --broker-list localhost:9092 \
  --topic forum.comment.created
```

**Output:** `forum.comment.created:0:X` (X = số messages)

**Step 3: Check Mailtrap inbox**

**Expected:** 4 emails (Comment 1, 2, 3, 4 - NOT Comment 5 vì self-comment)

**✅ Pass criteria:**
- [x] All 5 events sent to Kafka
- [x] All 5 events consumed
- [x] 4 emails sent (excluding self-comment)
- [x] All emails in correct order

---

## 📊 Monitoring

### Check Kafka consumer lag

```powershell
docker exec micro-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group notification-service-group \
  --describe
```

**Output:**
```
GROUP                       TOPIC                 PARTITION  CURRENT-OFFSET  LAG
notification-service-group  forum.comment.created  0          10              0
```

**LAG = 0** → Consumer đang up-to-date ✅

### Check Kafka messages

```powershell
# View all messages
docker exec -it micro-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic forum.comment.created \
  --from-beginning
```

**Output:** JSON objects của tất cả CommentCreatedEvent

### Check email delivery logs

**Notification Service logs:**
```
Successfully sent email to: user1@example.com
Successfully sent email to: user2@example.com
...
```

## 🐛 Troubleshooting

### Problem 1: Comment saved but no Kafka event

**Check Forum Service logs:**
```
✅ Should see: "Sending comment created event to Kafka"
✅ Should see: "Successfully sent comment event to Kafka"
```

**If not:**
```powershell
# Check Kafka connection
# Forum Service application.properties
spring.kafka.bootstrap-servers=localhost:9093  # Correct?
```

### Problem 2: Kafka event but no email

**Check Notification Service logs:**
```
✅ Should see: "Received comment event from Kafka"
✅ Should see: "Sending notification to..."
❌ Error: "Failed to send email"
```

**If error:**
```yaml
# Check email config
spring.mail:
  host: sandbox.smtp.mailtrap.io
  username: 109b298932e5e4  # Correct?
  password: f22f288279a5de  # Correct?
```

### Problem 3: Consumer not receiving events

**Check consumer group:**
```powershell
docker exec micro-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --list
```

**Phải thấy:** `notification-service-group`

**Reset offset if stuck:**
```powershell
# Stop Notification Service first!

docker exec micro-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group notification-service-group \
  --reset-offsets \
  --to-earliest \
  --topic forum.comment.created \
  --execute

# Start Notification Service again
```

### Problem 4: Email sent but not in Mailtrap

**Check:**
1. Refresh Mailtrap inbox page
2. Check "All Messages" tab (not just Inbox)
3. Check spam folder (shouldn't have spam in Mailtrap)
4. Verify Mailtrap account credentials

**Logs to check:**
```
Successfully sent email to: xxx@example.com  ← This means email sent to SMTP
```

## 📈 Performance Testing

### Test 100 comments

```powershell
# PowerShell loop
for ($i=1; $i -le 100; $i++) {
  curl -X POST http://localhost:8081/topics/1/comments `
    -H "Content-Type: application/json" `
    -d "{`"userId`": 2, `"body`": `"Comment $i`"}"
}
```

**Monitor:**
- Kafka consumer lag
- Email sending rate
- Memory usage

**Expected:**
- All 100 events consumed
- All 100 emails sent
- Lag returns to 0
- No errors

## ✅ Acceptance Criteria

### Functional

- [x] Direct comment → email to topic creator
- [x] Reply comment → email to topic creator + parent creator
- [x] Self-comment → no email
- [x] Multiple comments → all processed
- [x] Email content matches template
- [x] Email subject correct

### Technical

- [x] Kafka Producer sends events
- [x] Kafka Consumer receives events
- [x] No consumer lag
- [x] No duplicate emails
- [x] Emails sent via Mailtrap SMTP
- [x] Error handling (logs errors)

### Performance

- [x] Events processed in order
- [x] Low latency (< 5s from comment to email)
- [x] Handles concurrent comments
- [x] No memory leaks

## 📝 Test Report Template

```
Date: 2026-01-22
Tester: Your Name

Test Case 1: Direct Comment
- Status: ✅ PASS
- Comments saved: 1
- Events sent: 1
- Emails received: 1
- Issues: None

Test Case 2: Reply Comment
- Status: ✅ PASS
- Comments saved: 1
- Events sent: 1
- Emails received: 2 (topic + parent)
- Issues: None

Test Case 3: Self Comment
- Status: ✅ PASS
- Comments saved: 1
- Events sent: 1
- Emails received: 0 (expected)
- Issues: None

Test Case 4: Bulk Comments
- Status: ✅ PASS
- Comments saved: 100
- Events sent: 100
- Emails received: 100
- Issues: None

Overall: ✅ ALL TESTS PASSED
```

## ⏭️ Next Steps

**Tính năng mở rộng:**

1. **Batch Notifications**
   - Gộp nhiều comments → 1 email digest
   - "You have 5 new comments"

2. **Email Preferences**
   - User opt-out notifications
   - Notification frequency settings

3. **Notification History**
   - Store notifications trong database
   - User xem lịch sử notifications

4. **Retry Logic**
   - Retry failed emails (3 lần)
   - Dead letter queue cho failed events

5. **Real-time Notifications**
   - WebSocket notifications
   - In-app notification center

---

## 🎉 Kết luận

Bạn đã hoàn thành **toàn bộ Kafka notification system**!

**Flow hoàn chỉnh:**
```
User creates comment
    ↓
Forum Service saves to DB
    ↓
Forum Service → Kafka Producer
    ↓
Kafka Topic: forum.comment.created
    ↓
Notification Service ← Kafka Consumer
    ↓
Email Service → Mailtrap SMTP
    ↓
📧 Email delivered!
```

**Chúc mừng!** 🎊
