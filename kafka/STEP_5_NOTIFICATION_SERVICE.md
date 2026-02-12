# BƯỚC 5: Notification Service - Email thông báo khi có comment

## 🎯 Mục tiêu

Tạo Notification Service để:
1. Đọc events từ Kafka topic `forum.comment.created`
2. Gửi email thông báo cho người cần nhận
3. Sử dụng Mailtrap để test email

## 📦 Files đã tạo

### 1. Project structure

```
notification-service/
├── src/
│   ├── main/
│   │   ├── java/com/micro/notification/
│   │   │   ├── NotificationApplication.java
│   │   │   ├── config/
│   │   │   │   └── KafkaConsumerConfig.java
│   │   │   ├── event/
│   │   │   │   └── CommentCreatedEvent.java
│   │   │   ├── kafka/
│   │   │   │   └── CommentEventConsumer.java
│   │   │   └── service/
│   │   │       └── EmailService.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-docker.yml
│   │       └── templates/
│   │           └── comment-notification.html
├── Dockerfile
├── .dockerignore
└── pom.xml
```

### 2. Dependencies (pom.xml)

**Key dependencies:**
- `spring-boot-starter-mail` - Gửi email
- `spring-kafka` - Kafka consumer
- `spring-boot-starter-thymeleaf` - Email templates
- `spring-cloud-starter-netflix-eureka-client` - Service discovery

### 3. KafkaConsumerConfig.java

**Là gì?** Configuration cho Kafka Consumer

**Key settings:**
```java
- bootstrap-servers: localhost:9093
- group-id: notification-service-group
- auto-offset-reset: earliest  // Đọc từ đầu nếu chưa có offset
- JsonDeserializer: Tự động convert JSON → CommentCreatedEvent
```

### 4. CommentEventConsumer.java

**Là gì?** Kafka Listener - Lắng nghe events từ topic

**Workflow:**
```
@KafkaListener
    ↓
1. Receive CommentCreatedEvent
2. Log event info
3. sendNotifications()
    ├─ Notify topic creator (if not self)
    └─ Notify parent comment creator (if reply & not self)
4. Log success/error
```

**Notification logic:**
```java
// Case 1: Direct comment
if (commenterId != topicCreatorId) {
    sendEmail(topicCreatorEmail, "X commented on your topic");
}

// Case 2: Reply comment
if (parentCommentId != null && commenterId != parentCommentCreatorId) {
    // Avoid duplicate: skip if parent creator == topic creator
    if (parentCommentCreatorId != topicCreatorId) {
        sendEmail(parentCommentCreatorEmail, "X replied to your comment");
    }
}
```

### 5. EmailService.java

**Là gì?** Service gửi email qua SMTP

**Methods:**
- `sendCommentNotification()` - Main method
- `buildSubject()` - Tạo email subject
- `buildHtmlContent()` - Render HTML template với Thymeleaf

**Email metadata:**
```java
From: noreply@forumapp.com
To: recipient@example.com
Subject: "Alice commented on your topic 'Spring Boot'"
Content-Type: text/html
```

### 6. comment-notification.html

**Là gì?** Thymeleaf HTML template cho email

**Features:**
- ✅ Gradient header
- ✅ Avatar placeholder (first letter of name)
- ✅ Badge (Reply / New Comment)
- ✅ Topic title
- ✅ Comment body preview
- ✅ Action button
- ✅ Footer với unsubscribe link
- ✅ Responsive design

**Thymeleaf variables:**
```html
${commenterName}  - "Alice"
${topicTitle}     - "Spring Boot Tutorial"
${commentBody}    - "Great post!"
${isReply}        - true/false
${year}           - 2024
```

### 7. application.yml

**Kafka config:**
```yaml
spring.kafka:
  bootstrap-servers: localhost:9093
  consumer:
    group-id: notification-service-group
```

**Email config (Mailtrap):**
```yaml
spring.mail:
  host: sandbox.smtp.mailtrap.io
  port: 2525
  username: 109b298932e5e4
  password: f22f288279a5de
```

**Eureka config:**
```yaml
eureka.client:
  service-url:
    defaultZone: http://localhost:8761/eureka/
```

## 🔄 Data Flow

```
1. User creates comment
    ↓
2. Forum Service → Kafka (Producer)
    Topic: forum.comment.created
    Message: {commentId, commenterId, topicCreatorEmail, ...}
    ↓
3. Notification Service ← Kafka (Consumer)
    @KafkaListener triggers
    ↓
4. CommentEventConsumer.consumeCommentCreatedEvent()
    ↓
5. sendNotifications()
    ├─ Determine recipients
    │   ├─ Topic creator (if not self)
    │   └─ Parent comment creator (if reply & not self)
    │
    └─ For each recipient:
        EmailService.sendCommentNotification()
        ↓
6. Build email
    ├─ Subject
    ├─ HTML content (Thymeleaf template)
    └─ Metadata
    ↓
7. JavaMailSender.send() → Mailtrap SMTP
    ↓
8. 📧 Email delivered to Mailtrap inbox
```

## 🚀 Cách chạy

### Option 1: Local Development (từng service)

```powershell
# Terminal 1: Kafka
docker-compose up -d zookeeper kafka

# Terminal 2: Study Management (gRPC user service)
cd ssstudy_management
mvn spring-boot:run

# Terminal 3: Forum Service
cd forum
mvn spring-boot:run

# Terminal 4: Notification Service
cd notification-service
mvn spring-boot:run
```

**Phải thấy trong logs:**
```
Notification Service:
- KafkaAdmin : Kafka AdminClient configuration: bootstrap.servers=[localhost:9093]
- Started NotificationApplication in X.XXX seconds
```

### Option 2: Docker Compose (tất cả services)

```powershell
# Build và start tất cả
docker-compose up --build

# Hoặc chỉ start (không build lại)
docker-compose up -d
```

**Services sẽ start theo thứ tự:**
1. Zookeeper
2. Kafka
3. Postgres
4. Redis
5. Eureka
6. Study Management
7. Forum Service
8. Notification Service
9. API Gateway

## ✅ Kiểm tra Notification Service đã chạy

### 1. Check logs

```powershell
# Local
# Phải thấy: "Started NotificationApplication"

# Docker
docker logs micro-notification-service
```

### 2. Health check

```powershell
curl http://localhost:8083/actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "kafka": {"status": "UP"},
    "mail": {"status": "UP"}
  }
}
```

### 3. Check Eureka

Visit http://localhost:8761

**Phải thấy:**
- NOTIFICATION-SERVICE (1 instance)

### 4. Check Kafka consumer group

```powershell
docker exec micro-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group notification-service-group \
  --describe
```

**Output:**
```
GROUP                          TOPIC                   PARTITION  CURRENT-OFFSET
notification-service-group     forum.comment.created   0          0
```

## 🧪 Test gửi email

### 1. Tạo comment

```powershell
curl -X POST http://localhost:8081/topics/1/comments \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "body": "This is a test comment!"
  }'
```

### 2. Check Notification Service logs

**Phải thấy:**
```
Received comment event from Kafka: commentId=1, topicId=1, commenterId=2
Sending notification to topic creator: user1@example.com
Preparing email to: user1@example.com, commenter: User Two, topic: Spring Boot Tutorial
Successfully sent email to: user1@example.com
Successfully processed comment event: commentId=1
```

### 3. Check Mailtrap inbox

**Truy cập:** https://mailtrap.io/inboxes

**Login với:**
- Email: (email đã register Mailtrap)
- Password: (password của Mailtrap account)

**Phải thấy email:**
- **From:** noreply@forumapp.com
- **To:** user1@example.com
- **Subject:** "User Two commented on your topic 'Spring Boot Tutorial'"
- **Body:** Beautiful HTML email với comment preview

### 4. Test reply comment

```powershell
# Reply vào comment #1
curl -X POST http://localhost:8081/topics/1/comments \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 3,
    "parentCommentId": 1,
    "body": "I agree with this!"
  }'
```

**Expected emails:**
1. **To topic creator (user1@example.com):**
   - Subject: "User Three commented on your topic..."
2. **To parent comment creator (user2@example.com):**
   - Subject: "User Three replied to your comment..."

## 📊 Email Template Preview

```html
┌────────────────────────────────────┐
│   🔔 Forum Notification            │  ← Gradient header
├────────────────────────────────────┤
│                                    │
│  [REPLY TO YOUR COMMENT]           │  ← Badge
│                                    │
│  Someone replied to your comment   │  ← Title
│  on "Spring Boot Tutorial"         │
│                                    │
│  ┌──────────────────────────────┐ │
│  │ [A] Alice                    │ │  ← Commenter
│  │     commented just now       │ │
│  └──────────────────────────────┘ │
│                                    │
│  ┌──────────────────────────────┐ │
│  │ I agree with this comment!   │ │  ← Comment body
│  └──────────────────────────────┘ │
│                                    │
│       [View Comment]               │  ← Action button
│                                    │
├────────────────────────────────────┤
│  You received this email because   │  ← Footer
│  you're subscribed...              │
│  Unsubscribe | Settings            │
└────────────────────────────────────┘
```

## 📝 Notification Logic Examples

### Example 1: Direct comment

```
Topic: "Java Tutorial" (by User 1)
Commenter: User 2
Comment: "Great post!"

→ Send email to:
   - User 1 (topic creator) ✅
```

### Example 2: Reply comment

```
Topic: "Java Tutorial" (by User 1)
Comment #1: by User 2
Reply to Comment #1: by User 3

→ Send emails to:
   - User 1 (topic creator) ✅
   - User 2 (parent comment creator) ✅
```

### Example 3: Self-comment (avoid)

```
Topic: "Java Tutorial" (by User 1)
Commenter: User 1 (same person!)

→ Send email to:
   - Nobody (don't notify yourself) ❌
```

### Example 4: Reply to own comment

```
Topic: "Java Tutorial" (by User 1)
Comment #1: by User 2
Reply to Comment #1: by User 2 (same person!)

→ Send email to:
   - User 1 (topic creator) ✅
   - User 2 (parent comment creator) ❌ (skip self)
```

## ⚠️ Troubleshooting

### Problem 1: No emails received

**Check 1:** Notification Service logs
```
✅ "Received comment event from Kafka"
✅ "Sending notification to..."
✅ "Successfully sent email to..."
```

**Check 2:** Mailtrap inbox (refresh page)

**Check 3:** Email config
```yaml
# application.yml
spring.mail:
  host: sandbox.smtp.mailtrap.io  ← Correct?
  username: 109b298932e5e4         ← Correct?
  password: f22f288279a5de         ← Correct?
```

### Problem 2: Kafka connection error

**Error:** `Failed to construct kafka consumer`

**Solution:**
```powershell
# Check Kafka running
docker ps | findstr kafka

# Restart Kafka
docker-compose restart kafka

# Check bootstrap servers
# Local: localhost:9093
# Docker: kafka:9092
```

### Problem 3: Consumer not receiving events

**Check consumer group:**
```powershell
docker exec micro-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --list
```

**Reset consumer group (if stuck):**
```powershell
docker exec micro-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group notification-service-group \
  --reset-offsets \
  --to-earliest \
  --topic forum.comment.created \
  --execute
```

### Problem 4: Email sending fails

**Error:** `Failed to send email`

**Check SMTP logs:**
```yaml
logging.level.org.springframework.mail: DEBUG
```

**Common issues:**
- Wrong Mailtrap credentials
- Network firewall blocking port 2525
- SMTP timeout

## 🎓 Concepts giải thích

### Kafka Consumer Group

**Là gì?** Nhóm consumers cùng đọc từ 1 topic

**Tại sao cần?**
- Load balancing: Nhiều consumers chia sẻ partitions
- Fault tolerance: Consumer fail → consumer khác tiếp quản

**Group ID:** `notification-service-group`
- Kafka track offset cho mỗi group
- Mỗi message chỉ được 1 consumer trong group xử lý

### Auto Offset Reset

```yaml
auto-offset-reset: earliest
```

**Là gì?** Quyết định vị trí bắt đầu đọc khi chưa có offset

**Options:**
- `earliest` - Đọc từ đầu topic (message cũ nhất)
- `latest` - Đọc từ cuối topic (chỉ message mới)

**Use case:**
- Development: `earliest` (để test với data cũ)
- Production: `latest` (chỉ xử lý events mới)

### JsonDeserializer

**Là gì?** Chuyển JSON string → Java object

**Workflow:**
```
Kafka message (bytes)
    ↓
String: {"commentId": 1, ...}
    ↓
JsonDeserializer
    ↓
CommentCreatedEvent object
```

**Config:**
```java
JsonDeserializer.VALUE_DEFAULT_TYPE = CommentCreatedEvent.class
JsonDeserializer.TRUSTED_PACKAGES = "*"
```

### JavaMailSender

**Là gì?** Spring abstraction cho sending emails

**Supports:**
- Plain text emails
- HTML emails (với MimeMessage)
- Attachments
- Inline images

**SMTP protocols:**
- Port 25 - Plain SMTP
- Port 465 - SMTP over SSL
- Port 587 - SMTP with STARTTLS (recommended)
- Port 2525 - Alternative port (Mailtrap)

## ✅ Tóm tắt Bước 5

**Đã làm:**
1. ✅ Tạo Notification Service module
2. ✅ Config Kafka Consumer
3. ✅ Config Spring Mail với Mailtrap
4. ✅ Tạo Kafka Listener
5. ✅ Tạo Email Service
6. ✅ Tạo HTML email template
7. ✅ Register với Eureka
8. ✅ Docker support

**Có thể test:**
- ✅ Consumer nhận events từ Kafka
- ✅ Emails gửi thành công qua Mailtrap
- ✅ HTML templates render đẹp
- ✅ Notification logic đúng

## ⏭️ Bước tiếp theo

**BƯỚC 6:** Test end-to-end toàn bộ flow từ comment → Kafka → email

---

## 💡 Production Checklist

**Khi deploy production:**

- [ ] Replace Mailtrap với real SMTP (Gmail, SendGrid, AWS SES)
- [ ] Implement retry logic cho failed emails
- [ ] Add dead letter queue cho failed events
- [ ] Store notification history trong database
- [ ] Add user email preferences (opt-out)
- [ ] Batch notifications (gộp nhiều comments)
- [ ] Add email rate limiting
- [ ] Monitor email delivery rate
- [ ] Add email templates cho nhiều ngôn ngữ
- [ ] Secure SMTP credentials (env variables, secrets manager)
