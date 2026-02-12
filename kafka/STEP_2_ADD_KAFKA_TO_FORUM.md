# BƯỚC 2: Thêm Kafka vào Forum Service

## 🎯 Mục tiêu bước này

Cài đặt Kafka library vào Forum Service để code Java có thể gửi/nhận messages.

## 📦 Thay đổi

### 1. Thêm dependency vào pom.xml

**File:** `forum/pom.xml`

```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

**Giải thích:**
- `spring-kafka`: Spring Boot wrapper cho Apache Kafka
- Không cần version, Spring Boot tự quản lý

### 2. Thêm configuration vào application.properties

**File:** `forum/src/main/resources/application.properties`

```properties
# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9093
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
spring.kafka.consumer.group-id=forum-service-group
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=*
```

**Giải thích từng dòng:**

| Property | Ý nghĩa |
|----------|---------|
| `bootstrap-servers=localhost:9093` | Địa chỉ Kafka server (port 9093 cho local) |
| `producer.key-serializer` | Cách chuyển key thành bytes (dùng String) |
| `producer.value-serializer` | Cách chuyển message thành bytes (dùng JSON) |
| `consumer.group-id` | Nhóm consumers (để Kafka biết ai đang đọc) |
| `consumer.key-deserializer` | Cách đọc key từ bytes |
| `consumer.value-deserializer` | Cách đọc message từ bytes (JSON → Object) |
| `trusted.packages=*` | Cho phép deserialize tất cả packages (dev only!) |

## 🔧 Download dependencies

```powershell
cd forum
mvn clean install
```

**Giải thích:**
- `mvn clean`: Xóa build cũ
- `mvn install`: Download dependencies mới

**Phải thấy:**
```
[INFO] BUILD SUCCESS
```

## ✅ Kiểm tra

### 1. Check dependency đã download chưa

```powershell
# Windows
dir target\*.jar | findstr kafka

# Hoặc check trong IDE (IntelliJ/Eclipse)
# Xem External Libraries → spring-kafka
```

### 2. Test Spring Boot vẫn chạy được

```powershell
cd forum
mvn spring-boot:run
```

**Phải thấy trong logs:**
```
Started ForumApplication in X.XXX seconds
```

**Không có error về Kafka!**

### 3. Check Kafka config được load

Trong logs Spring Boot, tìm:
```
KafkaAdmin : Kafka AdminClient configuration:
bootstrap.servers = [localhost:9093]
```

## 📊 Kiến trúc hiện tại

```
Forum Service (Java)
    │
    ├─ REST API (existing)
    ├─ gRPC Client (existing)
    ├─ Database (existing)
    └─ Kafka Producer/Consumer ← NEW!
           │
           ▼
       Kafka Server
       (localhost:9093)
```

## ⚠️ Troubleshooting

### Problem 1: Maven không download được

```powershell
# Clear cache
mvn dependency:purge-local-repository

# Re-download
mvn clean install -U
```

### Problem 2: Spring Boot không start vì Kafka

**Lỗi:** `Could not connect to Kafka`

**Giải pháp:**
1. Check Kafka đang chạy:
   ```powershell
   docker ps | findstr kafka
   ```

2. Tạm thời comment Kafka config để test:
   ```properties
   # spring.kafka.bootstrap-servers=localhost:9093
   ```

### Problem 3: Port 9093 sai

**Trong Docker:** Dùng `kafka:9092`
**Local development:** Dùng `localhost:9093`

## 📝 Configuration cho môi trường khác nhau

### Development (Local)
```properties
spring.kafka.bootstrap-servers=localhost:9093
```

### Docker
```properties
spring.kafka.bootstrap-servers=kafka:9092
```

**Tip:** Dùng Spring Profiles để tách config:

`application-docker.properties`:
```properties
spring.kafka.bootstrap-servers=kafka:9092
```

`application-local.properties`:
```properties
spring.kafka.bootstrap-servers=localhost:9093
```

## 🎓 Giải thích Serialization

### Producer (Gửi message)

**Java Object → JSON → Bytes → Kafka**

```java
CommentEvent event = new CommentEvent(...);
// JsonSerializer tự động convert:
// event → {"userId": 1, "topicId": 5, ...} → bytes
```

### Consumer (Nhận message)

**Kafka → Bytes → JSON → Java Object**

```java
// JsonDeserializer tự động convert:
// bytes → {"userId": 1, ...} → CommentEvent event
```

## 📚 Thuật ngữ mới

| Thuật ngữ | Tiếng Việt | Giải thích |
|-----------|------------|------------|
| **Serializer** | Bộ tuần tự hóa | Chuyển Object → Bytes để gửi |
| **Deserializer** | Bộ giải tuần tự | Chuyển Bytes → Object để đọc |
| **Bootstrap Server** | Server khởi động | Địa chỉ Kafka đầu tiên để kết nối |
| **Consumer Group** | Nhóm người nhận | Nhóm consumers cùng đọc từ 1 topic |

## ✅ Tóm tắt Bước 2

**Đã làm:**
1. ✅ Thêm `spring-kafka` dependency
2. ✅ Config Kafka connection
3. ✅ Config serializer/deserializer
4. ✅ Download dependencies

**Chưa làm (sẽ làm bước 3):**
- Tạo Kafka Producer class
- Gửi message khi có comment mới

## ⏭️ Bước tiếp theo

**BƯỚC 3:** Tạo Producer để gửi event khi có comment mới.

---

## 💡 Quick Reference

```properties
# Kafka URL patterns:
localhost:9093    # Local development
kafka:9092        # Docker containers

# Common serializers:
StringSerializer    # For String values
JsonSerializer      # For Objects (auto convert to JSON)
ByteArraySerializer # For byte[] values
```
