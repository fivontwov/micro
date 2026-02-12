# Kafka Troubleshooting Guide

## 🐛 Common Issues

### Issue 1: ClassNotFoundException - Package mismatch

**Error:**
```
ClassNotFoundException: com.fivontwov.event.CommentCreatedEvent
```

**Cause:**
- Producer (Forum Service) uses package: `com.fivontwov.event`
- Consumer (Notification Service) uses package: `com.micro.notification.event`
- JsonDeserializer tries to find exact class name from type headers

**Solution:**
Config đã được fix để ignore type headers và map theo JSON structure.

**After fix, reset consumer offset:**

```powershell
# Stop Notification Service

# Reset offset to skip old messages
docker exec micro-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group notification-service-group \
  --reset-offsets \
  --to-latest \
  --topic forum.comment.created \
  --execute

# Or delete and recreate topic (for development only!)
docker exec micro-kafka kafka-topics \
  --delete --topic forum.comment.created \
  --bootstrap-server localhost:9092

# Restart Notification Service
```

---

### Issue 2: Kafka connection error

**Error:**
```
Failed to construct kafka consumer
Connection to node -1 (localhost:9093) could not be established
```

**Check:**
```powershell
# 1. Kafka running?
docker ps | findstr kafka

# 2. Port correct?
# Local: localhost:9093
# Docker: kafka:9092

# 3. Restart Kafka
docker-compose restart kafka
```

---

### Issue 3: Consumer not receiving messages

**Check consumer group:**
```powershell
docker exec micro-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group notification-service-group \
  --describe
```

**Check LAG column:**
- LAG = 0 → Up to date ✅
- LAG > 0 → Messages pending

**Reset if stuck:**
```powershell
# Stop Notification Service first!

docker exec micro-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group notification-service-group \
  --reset-offsets \
  --to-earliest \
  --topic forum.comment.created \
  --execute
```

---

### Issue 4: Email not sending

**Check Notification Service logs:**
```
✅ "Received comment event from Kafka"
✅ "Sending notification to..."
✅ "Successfully sent email to..."
```

**If error:**
```yaml
# Check application.yml
spring.mail:
  host: sandbox.smtp.mailtrap.io  # Correct?
  port: 2525                       # Correct?
  username: 109b298932e5e4         # Correct?
  password: f22f288279a5de         # Correct?
```

**Test SMTP connection:**
```powershell
# From command line
telnet sandbox.smtp.mailtrap.io 2525

# Or check logs
logging.level.org.springframework.mail: DEBUG
```

---

### Issue 5: Mailtrap not showing emails

**Check:**
1. Refresh inbox page
2. Check "All Messages" tab
3. Check correct Mailtrap account (username matches config)
4. Wait 5-10 seconds after "Successfully sent email" log

---

### Issue 6: Multiple services can't start

**Port conflicts:**

| Service | Port |
|---------|------|
| Eureka | 8761 |
| Forum | 8081 |
| Study | 8082 |
| Notification | 8083 |
| Gateway | 8080 |

**Check ports:**
```powershell
netstat -ano | findstr "8081"
netstat -ano | findstr "8083"
```

---

## 🔧 Useful Commands

### View Kafka messages

```powershell
docker exec -it micro-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic forum.comment.created \
  --from-beginning
```

### Count messages in topic

```powershell
docker exec micro-kafka kafka-run-class kafka.tools.GetOffsetShell \
  --broker-list localhost:9092 \
  --topic forum.comment.created
```

**Output:** `forum.comment.created:0:10` means 10 messages

### Delete topic (development only!)

```powershell
docker exec micro-kafka kafka-topics \
  --delete --topic forum.comment.created \
  --bootstrap-server localhost:9092
```

### List all topics

```powershell
docker exec micro-kafka kafka-topics \
  --list --bootstrap-server localhost:9092
```

### List consumer groups

```powershell
docker exec micro-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --list
```

### Describe consumer group

```powershell
docker exec micro-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group notification-service-group \
  --describe
```

---

## 🚨 Emergency Reset

**If everything is broken:**

```powershell
# 1. Stop all services
docker-compose down

# 2. Remove volumes (CAUTION: deletes all data!)
docker volume rm micro_postgres-data

# 3. Start fresh
docker-compose up --build

# Or just reset Kafka:
docker-compose restart kafka
```

---

## 📊 Health Checks

```powershell
# Forum Service
curl http://localhost:8081/actuator/health

# Notification Service
curl http://localhost:8083/actuator/health

# Study Management
curl http://localhost:8082/actuator/health

# Eureka
start http://localhost:8761
```

---

## 🎯 Debug Workflow

1. **Check services running**
   ```powershell
   docker ps
   ```

2. **Check Eureka** - All services registered?
   ```
   http://localhost:8761
   ```

3. **Check Kafka topics**
   ```powershell
   docker exec micro-kafka kafka-topics --list --bootstrap-server localhost:9092
   ```

4. **Check consumer lag**
   ```powershell
   docker exec micro-kafka kafka-consumer-groups \
     --bootstrap-server localhost:9092 \
     --group notification-service-group \
     --describe
   ```

5. **Check logs**
   ```powershell
   # Docker
   docker logs micro-notification-service
   
   # Local
   # Check terminal output
   ```

6. **Test end-to-end**
   ```powershell
   curl -X POST http://localhost:8081/topics/1/comments \
     -H "Content-Type: application/json" \
     -d '{"userId": 2, "body": "Test"}'
   ```

---

## 💡 Tips

1. **Start services in order:**
   - Kafka → Study Management → Forum → Notification
   
2. **Wait for services to be ready:**
   - Check health endpoints before testing
   
3. **Use Docker for Kafka:**
   - Don't install Kafka locally, use Docker
   
4. **Development vs Docker:**
   - Local: `localhost:9093`
   - Docker: `kafka:9092`

5. **Reset often in development:**
   - Delete topic to start fresh
   - Reset consumer offset when changing code

---

## 📚 Log Locations

### Local Development
- Forum Service: Terminal output
- Notification Service: Terminal output
- Study Management: Terminal output

### Docker
```powershell
docker logs micro-forum-service
docker logs micro-notification-service
docker logs micro-study-management
docker logs micro-kafka
```

### Log levels
```yaml
logging:
  level:
    com.micro.notification: DEBUG
    org.springframework.kafka: INFO
    org.springframework.mail: DEBUG
```
