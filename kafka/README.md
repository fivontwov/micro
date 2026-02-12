# Kafka Integration - Comment Notification System

## 📚 Tổng quan

Hệ thống thông báo khi có comment mới sử dụng Apache Kafka.

**Mục tiêu:** 
- Khi có comment mới → Gửi thông báo cho topic creator và parent comment creator (nếu là reply)

## 🎯 Kiến trúc

```
User comments
    ↓
Forum Service
    ├─ Save comment → Database
    └─ Send event → Kafka
                      ↓
              Topic: forum.comment.created
                      ↓
              Notification Service (Consumer)
                      ↓
              Send emails to:
                - Topic creator
                - Parent comment creator (if reply)
```

## 📋 Các bước đã hoàn thành

### ✅ BƯỚC 1: Setup Kafka Server
- Kafka & Zookeeper trong Docker
- Port: 9092 (Docker), 9093 (localhost)
- [Chi tiết](./STEP_1_KAFKA_SETUP.md)

### ✅ BƯỚC 2: Add Kafka vào Forum Service
- Dependency: `spring-kafka`
- Configuration: bootstrap servers, serializers
- [Chi tiết](./STEP_2_ADD_KAFKA_TO_FORUM.md)

### ✅ BƯỚC 3: Tạo Producer
- Event: `CommentCreatedEvent`
- Service: `KafkaProducerService`
- Integration: `TopicService.addComment()`
- [Chi tiết](./STEP_3_CREATE_PRODUCER.md)

### ✅ BƯỚC 4: Test Producer
- Test scenarios: direct comment, reply comment
- Verification: Kafka messages, notification recipients
- [Chi tiết](./STEP_4_TEST_PRODUCER.md)

## ⏭️ Các bước đã hoàn thành

### ✅ BƯỚC 5: Notification Service
- Created Notification Service (Spring Boot module)
- Kafka Consumer đọc từ topic `forum.comment.created`
- Email Service với Mailtrap SMTP
- Beautiful HTML email templates
- [Chi tiết](./STEP_5_NOTIFICATION_SERVICE.md)

### ✅ BƯỚC 6: Test End-to-End
- Test direct comment → email
- Test reply comment → multiple emails
- Test self-notification (avoid)
- Performance testing
- [Chi tiết](./STEP_6_TEST_END_TO_END.md)

### 🔲 BƯỚC 7: Advanced Features (TODO)
- Batch notifications (gộp nhiều comments)
- User preferences (opt-out)
- Notification history (database)
- Retry logic + Dead letter queue
- Real-time WebSocket notifications

## 🚀 Quick Start

### Start Kafka
```powershell
docker-compose up -d zookeeper kafka
```

### Start Forum Service
```powershell
cd forum
mvn spring-boot:run
```

### Test
```powershell
# Create comment
curl -X POST http://localhost:8081/topics/1/comments \
  -H "Content-Type: application/json" \
  -d '{"userId": 2, "body": "Great post!"}'

# Check Kafka message
docker exec -it micro-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic forum.comment.created \
  --from-beginning
```

## 📊 Kafka Topics

| Topic | Description | Producer | Consumer |
|-------|-------------|----------|----------|
| `forum.comment.created` | Comment events | Forum Service | Notification Service (TODO) |

## 🔧 Configuration

### Forum Service (application.properties)

```properties
# Kafka
spring.kafka.bootstrap-servers=localhost:9093
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
```

### Docker Compose

```yaml
kafka:
  image: confluentinc/cp-kafka:7.5.0
  ports:
    - "9092:9092"  # Docker network
    - "9093:9093"  # Localhost
  environment:
    KAFKA_AUTO_CREATE_TOPICS_ENABLE: 'true'
```

## 📝 Event Schema

```json
{
  "commentId": 1,
  "topicId": 5,
  "commenterId": 2,
  "commenterEmail": "user@example.com",
  "commenterName": "User Name",
  "topicCreatorId": 1,
  "topicCreatorEmail": "creator@example.com",
  "topicTitle": "Topic Title",
  "parentCommentId": null,
  "parentCommentCreatorId": null,
  "parentCommentCreatorEmail": null,
  "commentBody": "Comment text",
  "createdAt": "2026-01-22T21:00:00"
}
```

## 🐛 Troubleshooting

### Kafka không connect được
```powershell
# Check Kafka running
docker ps | findstr kafka

# Restart
docker-compose restart kafka
```

### Không thấy messages
```powershell
# List topics
docker exec micro-kafka kafka-topics --list --bootstrap-server localhost:9092

# Check logs
docker logs micro-kafka
```

### Forum Service không gửi được
```powershell
# Check Forum Service logs
# Phải thấy: "Successfully sent comment event to Kafka"

# Check Kafka config
# spring.kafka.bootstrap-servers=localhost:9093  # For local
# spring.kafka.bootstrap-servers=kafka:9092      # For Docker
```

## 📚 Tài liệu

- [STEP_1_KAFKA_SETUP.md](./STEP_1_KAFKA_SETUP.md) - Cài Kafka
- [STEP_2_ADD_KAFKA_TO_FORUM.md](./STEP_2_ADD_KAFKA_TO_FORUM.md) - Add dependency
- [STEP_3_CREATE_PRODUCER.md](./STEP_3_CREATE_PRODUCER.md) - Tạo Producer
- [STEP_4_TEST_PRODUCER.md](./STEP_4_TEST_PRODUCER.md) - Test Producer

## 💡 Tips

1. **Development:** Dùng localhost:9093
2. **Docker:** Dùng kafka:9092
3. **Debug:** Check logs của cả Kafka và Forum Service
4. **Reset:** Delete topic để test lại từ đầu

## ✅ Status

- [x] Kafka Server setup
- [x] Producer implementation
- [x] Producer testing
- [x] Consumer implementation (Notification Service)
- [x] Email service (Mailtrap SMTP)
- [x] End-to-end testing
- [ ] Advanced features (batch, preferences, history)

**Current Phase:** ✅ **HOÀN THÀNH! Email notifications đang hoạt động!**
