# BƯỚC 3: Tạo Producer gửi event khi comment

## 🎯 Mục tiêu bước này

Tạo code Java để gửi event qua Kafka mỗi khi có comment mới.

## 📦 Files tạo mới

### 1. CommentCreatedEvent.java

**File:** `forum/src/main/java/com/fivontwov/event/CommentCreatedEvent.java`

**Là gì?** Event object chứa thông tin về comment mới

**Chứa thông tin gì?**
- Comment mới: `commentId`, `commentBody`, `commenterId`, `commenterEmail`
- Topic: `topicId`, `topicTitle`, `topicCreatorId`, `topicCreatorEmail`
- Parent comment (nếu là reply): `parentCommentId`, `parentCommentCreatorId`, `parentCommentCreatorEmail`

**Tại sao cần?**
- Để Notification Service biết gửi email cho ai
- Chứa đủ thông tin không cần query thêm

### 2. KafkaProducerService.java

**File:** `forum/src/main/java/com/fivontwov/kafka/KafkaProducerService.java`

**Là gì?** Service gửi messages vào Kafka

**Làm gì?**
```java
sendCommentCreatedEvent(event) {
    1. Log thông tin
    2. Gửi vào topic "forum.comment.created"
    3. Key = commentId (để partition)
    4. Value = event object (auto convert to JSON)
    5. Log kết quả (success/error)
}
```

**Topic name:** `forum.comment.created`

### 3. TopicService.java - Updated

**Thay đổi:** Method `addComment()` giờ gửi Kafka event

**Luồng xử lý:**
```java
addComment() {
    1. Validate topic exists
    2. Validate commenter exists (gRPC)
    3. Save comment vào database
    4. Get topic creator info (gRPC)
    5. Get parent comment creator info (nếu reply, gRPC)
    6. Build CommentCreatedEvent
    7. Send event to Kafka  ← NEW!
    8. Return comment
}
```

## 🔄 Data Flow

```
Client → POST /topics/1/comments
    │
    ▼
TopicService.addComment()
    │
    ├─ Save comment → Database ✅
    │
    └─ kafkaProducerService.sendCommentCreatedEvent()
           │
           ▼
       Kafka Topic: "forum.comment.created"
       Message: {
           "commentId": 123,
           "topicId": 1,
           "commenterId": 5,
           "commenterEmail": "user@example.com",
           "topicCreatorEmail": "creator@example.com",
           ...
       }
```

## 💡 Giải thích Code

### Event structure

```java
@Data
public class CommentCreatedEvent {
    // Ai comment?
    private Long commenterId;
    private String commenterEmail;
    private String commenterName;
    
    // Comment ở đâu?
    private Long topicId;
    private String topicTitle;
    
    // Ai cần nhận thông báo?
    private Long topicCreatorId;
    private String topicCreatorEmail;  // → Gửi email
    
    // Nếu là reply comment:
    private Long parentCommentId;
    private Long parentCommentCreatorId;
    private String parentCommentCreatorEmail;  // → Gửi email
    
    // Nội dung
    private String commentBody;
    private LocalDateTime createdAt;
}
```

### Kafka send

```java
kafkaTemplate.send(
    "forum.comment.created",    // Topic name
    event.getCommentId().toString(),  // Key (for partitioning)
    event                        // Value (auto JSON)
);
```

**Giải thích:**
- **Topic:** Kênh chứa messages
- **Key:** commentId - giúp Kafka quyết định partition nào lưu message
- **Value:** Event object - Spring tự động convert thành JSON

### Async callback

```java
future.whenComplete((result, ex) -> {
    if (ex == null) {
        log.info("Success!");
    } else {
        log.error("Failed!", ex);
    }
});
```

**Giải thích:**
- Kafka send là **async** (không đợi)
- `whenComplete()` chạy khi send xong (success hoặc error)
- Log để debug

## ✅ Test

### 1. Start Kafka
```powershell
docker-compose up -d zookeeper kafka
```

### 2. Start Forum Service
```powershell
cd forum
mvn spring-boot:run
```

**Phải thấy trong logs:**
```
KafkaAdmin : Kafka AdminClient configuration:
bootstrap.servers = [localhost:9093]
```

### 3. Tạo comment qua API

```powershell
curl -X POST http://localhost:8081/topics/1/comments \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "body": "Great topic!"
  }'
```

**Hoặc nếu dùng qua Gateway:**
```powershell
curl -X POST http://localhost:8080/api/forum/topics/1/comments \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "body": "Great topic!"
  }'
```

### 4. Check logs

**Phải thấy:**
```
Sending comment created event to Kafka: commentId=X, topicId=1
Successfully sent comment event to Kafka: topic=forum.comment.created, partition=0, offset=0
```

### 5. Verify message trong Kafka

```powershell
# Consume messages từ topic
docker exec -it micro-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic forum.comment.created \
  --from-beginning
```

**Phải thấy JSON:**
```json
{
  "commentId": 1,
  "topicId": 1,
  "commenterId": 2,
  "commenterEmail": "user@example.com",
  "commenterName": "User Name",
  "topicCreatorId": 1,
  "topicCreatorEmail": "creator@example.com",
  "commentBody": "Great topic!",
  "topicTitle": "Spring Boot là gì?",
  "createdAt": "2026-01-22T21:30:00"
}
```

## 📊 Kiến trúc sau Bước 3

```
POST /topics/1/comments
    │
    ▼
TopicService.addComment()
    │
    ├─ Database ← Comment saved
    │
    └─ Kafka ← Event sent
           │
           ▼
    Topic: forum.comment.created
    Partition: 0
    Offset: 0
    Message: {...}
           │
           ▼
    (Consumer sẽ đọc - Bước 5)
```

## ⚠️ Troubleshooting

### Problem 1: Cannot connect to Kafka

**Error:** `Connection to node -1 (localhost:9093) could not be established`

**Solution:**
```powershell
# 1. Check Kafka running
docker ps | findstr kafka

# 2. Check port
netstat -ano | findstr "9093"

# 3. Restart Kafka
docker-compose restart kafka
```

### Problem 2: Event gửi nhưng không thấy logs

**Check:**
```properties
# application.properties
logging.level.com.fivontwov.kafka=DEBUG
```

### Problem 3: JSON serialization error

**Error:** `Could not serialize object`

**Check:**
- All fields trong `CommentCreatedEvent` phải có getter/setter
- Lombok `@Data` đã có chưa?
- `LocalDateTime` serialize được (Spring Boot auto hỗ trợ)

### Problem 4: gRPC call fail

**Error:** `User not found`

**Solution:**
- Start Study Management Service trước
- Check gRPC port 9090

## 📝 Message format

### Ví dụ 1: Comment trực tiếp vào topic

```json
{
  "commentId": 1,
  "topicId": 5,
  "commenterId": 3,
  "commenterEmail": "alice@example.com",
  "commenterName": "Alice",
  "topicCreatorId": 1,
  "topicCreatorEmail": "bob@example.com",
  "parentCommentId": null,  ← Không có parent
  "parentCommentCreatorId": null,
  "parentCommentCreatorEmail": null,
  "commentBody": "Nice post!",
  "topicTitle": "Java Tutorial",
  "createdAt": "2026-01-22T21:00:00"
}
```

**Notification logic (Bước 5):**
- Gửi email cho Bob (topic creator): "Alice commented on your topic 'Java Tutorial'"

### Ví dụ 2: Reply comment

```json
{
  "commentId": 2,
  "topicId": 5,
  "commenterId": 4,
  "commenterEmail": "charlie@example.com",
  "commenterName": "Charlie",
  "topicCreatorId": 1,
  "topicCreatorEmail": "bob@example.com",
  "parentCommentId": 1,  ← Có parent comment
  "parentCommentCreatorId": 3,
  "parentCommentCreatorEmail": "alice@example.com",
  "commentBody": "I agree!",
  "topicTitle": "Java Tutorial",
  "createdAt": "2026-01-22T21:05:00"
}
```

**Notification logic (Bước 5):**
- Gửi email cho Bob (topic creator): "Charlie commented on your topic 'Java Tutorial'"
- Gửi email cho Alice (parent comment creator): "Charlie replied to your comment"

## 🎓 Kafka Concepts

### Topic
- Giống "hộp thư" chứa messages
- Tên: `forum.comment.created`
- Auto create khi có message đầu tiên

### Partition
- Mỗi topic có nhiều partitions (default: 1)
- Messages phân bổ theo key
- Cùng key → cùng partition

### Offset
- Số thứ tự message trong partition
- Partition 0: offset 0, 1, 2, ...
- Consumer track offset để biết đã đọc đến đâu

### Key
- `commentId` trong trường hợp này
- Giúp Kafka quyết định partition
- Messages cùng key → cùng partition → đảm bảo thứ tự

## ✅ Tóm tắt Bước 3

**Đã làm:**
1. ✅ Tạo `CommentCreatedEvent` class
2. ✅ Tạo `KafkaProducerService`
3. ✅ Update `TopicService.addComment()` để gửi event
4. ✅ Test gửi message thành công

**Chưa làm (sẽ làm bước 4):**
- Test tất cả scenarios
- Test với reply comment
- Verify message format

## ⏭️ Bước tiếp theo

**BƯỚC 4:** Test Producer với nhiều scenarios khác nhau.

---

## 💡 Quick Reference

```bash
# View Kafka topic
docker exec micro-kafka kafka-topics \
  --list --bootstrap-server localhost:9092

# Consume messages
docker exec -it micro-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic forum.comment.created \
  --from-beginning

# Delete topic (để test lại)
docker exec micro-kafka kafka-topics \
  --delete --topic forum.comment.created \
  --bootstrap-server localhost:9092
```
