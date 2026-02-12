# BƯỚC 4: Test Producer - Kiểm tra tất cả hoạt động

## 🎯 Mục tiêu

Test Kafka Producer với các scenarios:
1. Comment trực tiếp vào topic
2. Reply vào comment (parent comment)
3. Verify message format
4. Check notification recipients

## 🚀 Chuẩn bị

### 1. Start tất cả services

```powershell
# Terminal 1: Kafka & Zookeeper
docker-compose up -d zookeeper kafka

# Đợi 30s cho Kafka khởi động

# Terminal 2: Study Management (gRPC server cho user info)
cd ssstudy_management
mvn spring-boot:run

# Terminal 3: Forum Service
cd forum
mvn spring-boot:run
```

### 2. Tạo test data

**Cần có:**
- Topic ID: 1 (hoặc tạo mới)
- User ID: 1, 2, 3 (trong Study Management database)

**Tạo topic test:**
```powershell
curl -X POST http://localhost:8081/topics \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "title": "Spring Boot Tutorial",
    "body": "Learn Spring Boot basics"
  }'
```

## 📝 Test Cases

### Test Case 1: Comment trực tiếp vào topic

**Mô tả:** User 2 comment vào topic của User 1

**Request:**
```powershell
curl -X POST http://localhost:8081/topics/1/comments \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "body": "Great tutorial! Thanks for sharing."
  }'
```

**Expected Response:**
```json
{
  "id": 1,
  "topicId": 1,
  "userId": 2,
  "parentCommentId": null,
  "body": "Great tutorial! Thanks for sharing.",
  "createdAt": "2026-01-22T..."
}
```

**Check Logs:**
```
Sending comment created event to Kafka: commentId=1, topicId=1
Successfully sent comment event to Kafka: topic=forum.comment.created, partition=0, offset=0
```

**Check Kafka message:**
```powershell
docker exec -it micro-kafka kafka-console-consumer 
  --bootstrap-server localhost:9092 
  --topic forum.comment.created 
  --from-beginning
```

**Expected message:**
```json
{
  "commentId": 1,
  "topicId": 1,
  "commenterId": 2,
  "commenterEmail": "user2@example.com",
  "commenterName": "User Two",
  "topicCreatorId": 1,
  "topicCreatorEmail": "user1@example.com",
  "parentCommentId": null,
  "parentCommentCreatorId": null,
  "parentCommentCreatorEmail": null,
  "commentBody": "Great tutorial! Thanks for sharing.",
  "topicTitle": "Spring Boot Tutorial",
  "createdAt": "2026-01-22T..."
}
```

**Notification recipients:**
- ✅ User 1 (topic creator) - email: user1@example.com

---

### Test Case 2: Reply vào comment

**Mô tả:** User 3 reply vào comment của User 2

**Request:**
```powershell
curl -X POST http://localhost:8081/topics/1/comments \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 3,
    "parentCommentId": 1,
    "body": "I agree with this comment!"
  }'
```

**Expected Response:**
```json
{
  "id": 2,
  "topicId": 1,
  "userId": 3,
  "parentCommentId": 1,
  "body": "I agree with this comment!",
  "createdAt": "2026-01-22T..."
}
```

**Expected Kafka message:**
```json
{
  "commentId": 2,
  "topicId": 1,
  "commenterId": 3,
  "commenterEmail": "user3@example.com",
  "commenterName": "User Three",
  "topicCreatorId": 1,
  "topicCreatorEmail": "user1@example.com",
  "parentCommentId": 1,
  "parentCommentCreatorId": 2,
  "parentCommentCreatorEmail": "user2@example.com",
  "commentBody": "I agree with this comment!",
  "topicTitle": "Spring Boot Tutorial",
  "createdAt": "2026-01-22T..."
}
```

**Notification recipients:**
- ✅ User 1 (topic creator) - email: user1@example.com
- ✅ User 2 (parent comment creator) - email: user2@example.com

---

### Test Case 3: Multiple comments

**Mô tả:** Test nhiều comments liên tiếp

```powershell
# Comment 3
curl -X POST http://localhost:8081/topics/1/comments \
  -H "Content-Type: application/json" \
  -d '{"userId": 2, "body": "Another thought..."}'

# Comment 4
curl -X POST http://localhost:8081/topics/1/comments \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "body": "Thanks everyone!"}'
```

**Check Kafka:**
```powershell
docker exec -it micro-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic forum.comment.created \
  --from-beginning
```

**Phải thấy:** 4 messages (offset 0, 1, 2, 3)

---

## ✅ Verification Checklist

### 1. Logs check

```
✅ Kafka connection established
✅ "Sending comment created event to Kafka"
✅ "Successfully sent comment event to Kafka"
✅ Topic, partition, offset logged
❌ No errors about serialization
❌ No connection errors
```

### 2. Message format check

```json
✅ commentId present
✅ topicId present
✅ commenterId, commenterEmail, commenterName present
✅ topicCreatorId, topicCreatorEmail present
✅ commentBody present
✅ topicTitle present
✅ createdAt present

For replies:
✅ parentCommentId present
✅ parentCommentCreatorId, parentCommentCreatorEmail present
```

### 3. Notification recipients check

**Scenario 1: Direct comment**
```
Topic Creator: User 1
Commenter: User 2
→ Notify: User 1
```

**Scenario 2: Reply comment**
```
Topic Creator: User 1
Parent Comment Creator: User 2
Commenter: User 3
→ Notify: User 1, User 2
```

**Special case: Topic creator comments on own topic**
```
Topic Creator: User 1
Commenter: User 1
→ Notify: Nobody (don't notify yourself)
```

## 🐛 Troubleshooting

### Problem 1: No messages in Kafka

**Check:**
```powershell
# 1. Kafka topic exists?
docker exec micro-kafka kafka-topics \
  --list --bootstrap-server localhost:9092

# 2. Check Forum Service logs
# Phải thấy: "Successfully sent comment event"

# 3. Consume from beginning
docker exec -it micro-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic forum.comment.created \
  --from-beginning
```

### Problem 2: gRPC error - User not found

**Error:** `User not found with id: X`

**Solution:**
```sql
-- Check users exist in Study Management database
SELECT * FROM users;

-- Create test users nếu chưa có
INSERT INTO users (username, email, name, role, password) 
VALUES 
  ('user1', 'user1@example.com', 'User One', 'STUDENT', 'password'),
  ('user2', 'user2@example.com', 'User Two', 'STUDENT', 'password'),
  ('user3', 'user3@example.com', 'User Three', 'STUDENT', 'password');
```

### Problem 3: Kafka serialization error

**Error:** `Failed to serialize object`

**Check:**
```java
// CommentCreatedEvent.java
@Data  ← Phải có annotation này
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreatedEvent {
    // All fields have getters/setters
}
```

### Problem 4: Topic auto-create không hoạt động

**Manual create:**
```powershell
docker exec micro-kafka kafka-topics \
  --create \
  --topic forum.comment.created \
  --partitions 1 \
  --replication-factor 1 \
  --bootstrap-server localhost:9092
```

## 📊 Message Statistics

```powershell
# Count messages in topic
docker exec micro-kafka kafka-run-class kafka.tools.GetOffsetShell \
  --broker-list localhost:9092 \
  --topic forum.comment.created

# Output: forum.comment.created:0:5
#         (topic:partition:offset)
#         → 5 messages
```

## 🎯 Success Criteria

✅ **Comments được lưu vào database**
```sql
SELECT * FROM comments;
```

✅ **Events được gửi vào Kafka**
```
Successfully sent comment event to Kafka: offset=X
```

✅ **Messages có đầy đủ thông tin**
```json
{
  "commentId": ...,
  "commenterEmail": "...",
  "topicCreatorEmail": "...",
  ...
}
```

✅ **Notification recipients đúng**
- Direct comment → notify topic creator
- Reply comment → notify topic creator + parent comment creator

## 📝 Test Script

**File:** `test-kafka-producer.sh` (PowerShell)

```powershell
# Test Kafka Producer

# 1. Create topic
Write-Host "Creating test topic..."
curl -X POST http://localhost:8081/topics `
  -H "Content-Type: application/json" `
  -d '{"userId": 1, "title": "Test Topic", "body": "Test body"}'

# 2. Direct comment
Write-Host "`nTest 1: Direct comment..."
curl -X POST http://localhost:8081/topics/1/comments `
  -H "Content-Type: application/json" `
  -d '{"userId": 2, "body": "Test comment"}'

Start-Sleep -Seconds 2

# 3. Reply comment
Write-Host "`nTest 2: Reply comment..."
curl -X POST http://localhost:8081/topics/1/comments `
  -H "Content-Type: application/json" `
  -d '{"userId": 3, "parentCommentId": 1, "body": "Test reply"}'

Start-Sleep -Seconds 2

# 4. Check Kafka
Write-Host "`nChecking Kafka messages..."
docker exec -it micro-kafka kafka-console-consumer `
  --bootstrap-server localhost:9092 `
  --topic forum.comment.created `
  --from-beginning `
  --timeout-ms 5000
```

## ✅ Tóm tắt Bước 4

**Đã test:**
1. ✅ Comment trực tiếp vào topic
2. ✅ Reply vào comment
3. ✅ Multiple comments
4. ✅ Message format verification
5. ✅ Notification recipients logic

**Confirmed working:**
- ✅ Kafka Producer sends messages
- ✅ Events contain correct data
- ✅ Topic auto-created
- ✅ Messages persisted in Kafka

## ⏭️ Các bước tiếp theo (LÀM SAU)

### **BƯỚC 5:** Tạo Kafka Consumer
- Đọc messages từ topic `forum.comment.created`
- Process events
- Trigger notifications

### **BƯỚC 6:** Email Notification Service
- Gửi email cho topic creator
- Gửi email cho parent comment creator
- Email templates

### **BƯỚC 7:** Advanced Features
- Avoid self-notification
- Batch notifications
- Email preferences (user opt-out)
- Notification history

---

## 💡 Monitoring Commands

```powershell
# View all topics
docker exec micro-kafka kafka-topics --list --bootstrap-server localhost:9092

# Describe topic
docker exec micro-kafka kafka-topics \
  --describe --topic forum.comment.created \
  --bootstrap-server localhost:9092

# Consumer groups
docker exec micro-kafka kafka-consumer-groups \
  --list --bootstrap-server localhost:9092

# Delete topic (reset test)
docker exec micro-kafka kafka-topics \
  --delete --topic forum.comment.created \
  --bootstrap-server localhost:9092
```

## 🎉 Kết luận Bước 4

Bạn đã hoàn thành **Producer** phần! 

**Những gì đã làm được:**
- ✅ Kafka server running
- ✅ Forum Service connects to Kafka
- ✅ Events gửi thành công khi có comment
- ✅ Message format đúng
- ✅ Ready cho Consumer (Bước 5)

**Tạm dừng ở đây!** Làm Bước 5 (Consumer + Notification) sau khi đã test kỹ Bước 1-4.
