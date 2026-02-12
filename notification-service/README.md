# Notification Service

Email notification service sử dụng Kafka Consumer để gửi thông báo khi có comment mới.

## 🎯 Chức năng

- **Kafka Consumer:** Lắng nghe events từ topic `forum.comment.created`
- **Email Service:** Gửi email qua Mailtrap SMTP
- **Notification Logic:**
  - Comment trực tiếp → Gửi email cho topic creator
  - Reply comment → Gửi email cho topic creator + parent comment creator
  - Tránh self-notification (không gửi cho chính người comment)

## 🚀 Quick Start

### Local Development

```powershell
# 1. Start Kafka
docker-compose up -d zookeeper kafka

# 2. Start Notification Service
cd notification-service
mvn spring-boot:run
```

### Docker

```powershell
# Start all services
docker-compose up --build
```

## 📧 Email Configuration

Service sử dụng **Mailtrap** để test email:

```yaml
spring.mail:
  host: sandbox.smtp.mailtrap.io
  port: 2525
  username: 109b298932e5e4
  password: f22f288279a5de
```

**Check emails:** https://mailtrap.io/inboxes (login với account đã config)

## 📊 Architecture

```
Forum Service
    ↓ (Producer)
Kafka Topic: forum.comment.created
    ↓ (Consumer)
Notification Service
    ↓
Email Service (Mailtrap)
    ↓
📧 Email sent!
```

## 🔧 Configuration

### Kafka Consumer

```yaml
spring.kafka:
  bootstrap-servers: localhost:9093  # local
  consumer:
    group-id: notification-service-group
    auto-offset-reset: earliest
```

### Email Templates

Template location: `src/main/resources/templates/comment-notification.html`

**Features:**
- Responsive design
- Beautiful gradient header
- Avatar placeholder
- Comment preview
- Action button

## 🧪 Testing

### 1. Start services

```powershell
# Kafka
docker-compose up -d zookeeper kafka

# Study Management (gRPC user info)
cd ssstudy_management
mvn spring-boot:run

# Forum Service (Terminal 2)
cd forum
mvn spring-boot:run

# Notification Service (Terminal 3)
cd notification-service
mvn spring-boot:run
```

### 2. Create comment

```powershell
curl -X POST http://localhost:8081/topics/1/comments \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "body": "Great post!"
  }'
```

### 3. Check logs

**Notification Service logs:**
```
Received comment event from Kafka: commentId=1, topicId=1
Sending notification to topic creator: user1@example.com
Successfully sent email to: user1@example.com
Successfully processed comment event: commentId=1
```

### 4. Check Mailtrap inbox

Visit https://mailtrap.io/inboxes and see the email!

## 📝 Event Schema

```json
{
  "commentId": 1,
  "topicId": 5,
  "commenterId": 2,
  "commenterEmail": "commenter@example.com",
  "commenterName": "Alice",
  "topicCreatorId": 1,
  "topicCreatorEmail": "creator@example.com",
  "topicTitle": "Spring Boot Tutorial",
  "parentCommentId": null,
  "parentCommentCreatorId": null,
  "parentCommentCreatorEmail": null,
  "commentBody": "Great tutorial!",
  "createdAt": "2026-01-22T21:00:00"
}
```

## 🐛 Troubleshooting

### No emails received

**Check:**
1. Notification Service logs - "Successfully sent email"
2. Mailtrap inbox - emails appear with ~1-2s delay
3. Email configuration - correct username/password

### Kafka connection error

```powershell
# Check Kafka running
docker ps | findstr kafka

# Restart Kafka
docker-compose restart kafka
```

### Consumer not receiving messages

```powershell
# Check consumer group
docker exec micro-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group notification-service-group \
  --describe
```

## 📚 Dependencies

- `spring-boot-starter-web` - REST API
- `spring-boot-starter-mail` - Email sending
- `spring-kafka` - Kafka consumer
- `spring-cloud-starter-netflix-eureka-client` - Service discovery
- `spring-boot-starter-thymeleaf` - Email templates
- `spring-boot-starter-actuator` - Health checks

## 🔗 Endpoints

- Health: http://localhost:8083/actuator/health
- Metrics: http://localhost:8083/actuator/metrics
- Info: http://localhost:8083/actuator/info

## 💡 Tips

1. **Development:** Use Mailtrap to avoid sending real emails
2. **Production:** Replace with real SMTP (Gmail, SendGrid, AWS SES)
3. **Email Templates:** Edit `comment-notification.html` to customize
4. **Error Handling:** Check logs if emails fail
5. **Retry Logic:** TODO - implement retry for failed emails

## ⚙️ Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | localhost:9093 | Kafka server address |
| `SPRING_MAIL_HOST` | sandbox.smtp.mailtrap.io | SMTP host |
| `SPRING_MAIL_PORT` | 2525 | SMTP port |
| `SPRING_MAIL_USERNAME` | 109b298932e5e4 | SMTP username |
| `SPRING_MAIL_PASSWORD` | f22f288279a5de | SMTP password |

## 📖 Documentation

- [STEP_5_NOTIFICATION_SERVICE.md](../kafka/STEP_5_NOTIFICATION_SERVICE.md) - Detailed setup guide
- [STEP_6_TEST_END_TO_END.md](../kafka/STEP_6_TEST_END_TO_END.md) - End-to-end testing

## ✅ Status

- [x] Kafka Consumer setup
- [x] Email service with Mailtrap
- [x] Thymeleaf templates
- [x] Eureka registration
- [x] Docker support
- [ ] Retry logic
- [ ] Dead letter queue
- [ ] Email preferences
- [ ] Notification history
