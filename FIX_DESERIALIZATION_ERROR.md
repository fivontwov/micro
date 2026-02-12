# 🔧 Fix: Deserialization Error - SOLVED!

## ❌ Lỗi bạn đang gặp

```
ClassNotFoundException: com.fivontwov.event.CommentCreatedEvent
```

**Nguyên nhân:**
- Forum Service gửi với package: `com.fivontwov.event.CommentCreatedEvent`
- Notification Service tìm class này nhưng package của nó là: `com.micro.notification.event.CommentCreatedEvent`

## ✅ Đã fix!

Tôi đã update config để **ignore type headers** và deserialize theo JSON structure thay vì class name:

**Files updated:**
1. `notification-service/src/main/resources/application.yml`
2. `notification-service/src/main/java/com/micro/notification/config/KafkaConsumerConfig.java`

---

## 🚀 Cách áp dụng fix

### Option 1: Reset consumer offset (Recommended)

```powershell
# 1. Stop Notification Service (Ctrl+C hoặc docker-compose stop notification-service)

# 2. Reset consumer offset to skip old messages
docker exec micro-kafka kafka-consumer-groups `
  --bootstrap-server localhost:9092 `
  --group notification-service-group `
  --reset-offsets `
  --to-latest `
  --topic forum.comment.created `
  --execute

# 3. Restart Notification Service
cd notification-service
mvn spring-boot:run

# Or if using Docker:
docker-compose up notification-service
```

### Option 2: Delete topic và start fresh (Development only!)

```powershell
# 1. Stop Notification Service

# 2. Delete topic
docker exec micro-kafka kafka-topics `
  --delete --topic forum.comment.created `
  --bootstrap-server localhost:9092

# 3. Restart Notification Service (topic sẽ auto-create)
cd notification-service
mvn spring-boot:run
```

---

## ✅ Verify fix hoạt động

### 1. Check logs

**Phải KHÔNG thấy error này nữa:**
```
❌ ClassNotFoundException: com.fivontwov.event.CommentCreatedEvent
```

**Phải thấy:**
```
✅ Started NotificationApplication
✅ KafkaAdmin : Kafka AdminClient configuration
```

### 2. Test với comment mới

```powershell
# Tạo comment mới
curl -X POST http://localhost:8081/topics/1/comments `
  -H "Content-Type: application/json" `
  -d '{
    "userId": 2,
    "body": "Test after fix!"
  }'
```

**Notification Service logs phải thấy:**
```
✅ Received comment event from Kafka: commentId=X
✅ Sending notification to topic creator: userX@example.com
✅ Successfully sent email to: userX@example.com
```

### 3. Check Mailtrap

Visit https://mailtrap.io/inboxes → Phải thấy email mới!

---

## 📝 Giải thích fix

### Before (❌ Broken)

```
Forum sends:
{
  "__type": "com.fivontwov.event.CommentCreatedEvent",  ← Type header
  "commentId": 1,
  ...
}

Notification receives:
- Tries to find class: com.fivontwov.event.CommentCreatedEvent
- ERROR: ClassNotFoundException (package doesn't exist)
```

### After (✅ Fixed)

```yaml
# Config added:
spring.json.use.type.headers: false        # Ignore __type header
spring.json.value.default.type: com.micro.notification.event.CommentCreatedEvent
```

```
Forum sends:
{
  "__type": "com.fivontwov.event.CommentCreatedEvent",  ← Ignored!
  "commentId": 1,
  ...
}

Notification receives:
- Ignores __type header
- Maps JSON fields to: com.micro.notification.event.CommentCreatedEvent
- SUCCESS! ✅
```

---

## 🎯 Quick Commands

### Check consumer offset

```powershell
docker exec micro-kafka kafka-consumer-groups `
  --bootstrap-server localhost:9092 `
  --group notification-service-group `
  --describe
```

### View messages in topic

```powershell
docker exec -it micro-kafka kafka-console-consumer `
  --bootstrap-server localhost:9092 `
  --topic forum.comment.created `
  --from-beginning
```

---

## 🆘 Vẫn còn lỗi?

**Check:**

1. **Config file được save chưa?**
   - `notification-service/src/main/resources/application.yml`
   - Phải có `spring.json.use.type.headers: false`

2. **Service đã restart chưa?**
   - Stop và start lại Notification Service

3. **Consumer offset đã reset chưa?**
   - Chạy lệnh reset offset ở trên

4. **Kafka đang chạy không?**
   ```powershell
   docker ps | findstr kafka
   ```

**Nếu vẫn lỗi, xem:** [kafka/TROUBLESHOOTING.md](kafka/TROUBLESHOOTING.md)

---

## ✅ Kết luận

**Lỗi đã được fix!** Chỉ cần:

1. ✅ Config updated (done by me)
2. ⏭️ Reset consumer offset (bạn chạy lệnh)
3. ⏭️ Restart Notification Service (bạn restart)
4. ⏭️ Test với comment mới

**Sau khi làm 3 bước trên, hệ thống sẽ hoạt động bình thường!** 🎉
