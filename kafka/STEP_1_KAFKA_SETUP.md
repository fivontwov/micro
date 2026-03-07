# BƯỚC 1: Cài Kafka Server - Hướng dẫn cho người mới

## 🎯 Mục tiêu bước này

Cài đặt Kafka server bằng Docker để sẵn sàng gửi/nhận messages.

## 📚 Kafka là gì? (Giải thích đơn giản)

**Kafka** giống như một "bưu điện" cho các services:
- **Producer** (người gửi): Service gửi message vào Kafka
- **Topic** (hộp thư): Nơi lưu messages theo chủ đề
- **Consumer** (người nhận): Service đọc messages từ topic

**Ví dụ thực tế:**
```
Forum Service → Kafka Topic "comments" → Notification Service
     (gửi)           (lưu trữ)                  (nhận)
```

## 🏗️ Components cần cài

### 1. Zookeeper
- **Là gì?** Quản lý Kafka cluster
- **Port:** 2181
- **Cần thiết:** Kafka phụ thuộc vào Zookeeper

### 2. Kafka
- **Là gì?** Message broker chính
- **Port:** 9092 (internal), 9093 (localhost)
- **Làm gì:** Nhận, lưu, gửi messages

## 🚀 Cách chạy

### Option 1: Docker Compose (Khuyến nghị)

```powershell
# Từ project root
docker-compose up -d zookeeper kafka
```

**Giải thích:**
- `up` = khởi động services
- `-d` = chạy background (detached mode)
- `zookeeper kafka` = chỉ chạy 2 services này

### Option 2: Chạy tất cả services

```powershell
docker-compose up --build
```

## ✅ Kiểm tra Kafka đã chạy chưa

### 1. Check containers
```powershell
docker ps | findstr kafka
docker ps | findstr zookeeper
```

**Phải thấy:**
```
micro-zookeeper   Up
micro-kafka       Up
```

### 2. Check Kafka topics
```powershell
# Vào Kafka container
docker exec -it micro-kafka bash

# List topics (hiện tại chưa có topic nào)
kafka-topics --bootstrap-server localhost:9092 --list

# Exit
exit
```

### 3. Test tạo topic thủ công
```powershell
# Tạo topic test
docker exec micro-kafka kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic test-topic \
  --partitions 1 \
  --replication-factor 1

# Xem danh sách topics
docker exec micro-kafka kafka-topics --list \
  --bootstrap-server localhost:9092
```

**Phải thấy:** `test-topic`

### 4. Test gửi message
```powershell
# Producer: Gửi message
docker exec -it micro-kafka kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic test-topic

# Gõ message, ví dụ:
> Hello Kafka!
> Test message
# Nhấn Ctrl+C để thoát
```

### 5. Test nhận message
```powershell
# Consumer: Đọc messages (terminal mới)
docker exec -it micro-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic test-topic \
  --from-beginning

# Phải thấy:
# Hello Kafka!
# Test message
```

## 📊 Ports được dùng

| Service | Port | Mô tả |
|---------|------|-------|
| Zookeeper | 2181 | Kafka management |
| Kafka (internal) | 9092 | Dùng bởi services trong Docker network |
| Kafka (localhost) | 9093 | Dùng từ máy local (development) |

## 🔧 Configuration quan trọng

### Trong docker-compose.yml:

```yaml
KAFKA_ADVERTISED_LISTENERS: 
  PLAINTEXT://kafka:9092          # Cho services trong Docker
  PLAINTEXT_HOST://localhost:9093  # Cho local development

KAFKA_AUTO_CREATE_TOPICS_ENABLE: 'true'  # Tự động tạo topic
```

**Giải thích:**
- `ADVERTISED_LISTENERS`: Kafka lắng nghe ở 2 địa chỉ
- `AUTO_CREATE_TOPICS`: Không cần tạo topic thủ công, Kafka tự tạo khi có message

## ⚠️ Troubleshooting

### Problem 1: Zookeeper không start
```powershell
# Check logs
docker logs micro-zookeeper

# Restart
docker-compose restart zookeeper
```

### Problem 2: Kafka không connect Zookeeper
```powershell
# Check Zookeeper health
docker exec micro-zookeeper nc -z localhost 2181

# Phải return: Connection to localhost 2181 port [tcp/*] succeeded!
```

### Problem 3: Port 9092/9093 bị chiếm
```powershell
# Check port
netstat -ano | findstr "9092"

# Kill process hoặc đổi port trong docker-compose.yml
```

## 📝 Tóm tắt Bước 1

✅ **Đã làm:**
1. Thêm Zookeeper vào docker-compose.yml
2. Thêm Kafka vào docker-compose.yml
3. Configure ports và settings

✅ **Có thể test:**
- Kafka chạy được
- Tạo topic
- Gửi message
- Nhận message

## ⏭️ Bước tiếp theo

**BƯỚC 2:** Thêm Kafka dependency vào Forum Service để code Java có thể gửi messages.

---

## 📚 Thuật ngữ cần nhớ

| Thuật ngữ | Tiếng Việt | Giải thích |
|-----------|------------|------------|
| **Broker** | Máy chủ Kafka | Server lưu trữ messages |
| **Topic** | Chủ đề | Kênh phân loại messages |
| **Producer** | Người gửi | Service gửi message vào topic |
| **Consumer** | Người nhận | Service đọc message từ topic |
| **Partition** | Phân vùng | Chia topic thành nhiều phần (để scale) |
| **Offset** | Vị trí | Số thứ tự của message trong partition |

## 💡 Tips

1. **Development:** Dùng port 9093 (localhost)
2. **Docker services:** Dùng port 9092 (kafka hostname)
3. **Topic naming:** Dùng format `{service}.{entity}.{action}` 
   - Ví dụ: `forum.comment.created`
4. **Auto create topics:** Đã bật, không cần tạo thủ công
