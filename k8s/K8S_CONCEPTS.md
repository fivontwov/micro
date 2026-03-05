# 📚 Kubernetes Concepts Explained (Giải thích Khái niệm K8s)

## Dành cho người mới bắt đầu

### 🎯 Kubernetes là gì?

**Kubernetes (K8s)** là một hệ thống mã nguồn mở để tự động hóa việc triển khai, mở rộng và quản lý các ứng dụng được đóng gói trong container.

**Tương tự:**
- **Docker** = Ngôi nhà (container)
- **Kubernetes** = Thành phố quản lý nhiều ngôi nhà (cluster)

---

## 🏗️ Các thành phần chính

### 1. **Cluster**

**Là gì:** Một cụm gồm nhiều máy (nodes) chạy Kubernetes

```
Cluster
├── Master Node (Control Plane)
│   ├── API Server
│   ├── Scheduler
│   └── Controller Manager
└── Worker Nodes
    ├── Node 1
    ├── Node 2
    └── Node 3
```

**Trong môi trường development:**
- Docker Desktop tạo **1 node cluster** trên máy bạn
- Node này vừa là master vừa là worker

---

### 2. **Pod**

**Là gì:** Đơn vị nhỏ nhất trong Kubernetes. Một Pod chứa 1 hoặc nhiều containers.

**Ví dụ:**
```yaml
Pod: forum-service-abc123
  └── Container: forum-service (image: micro-forum-service:latest)
```

**Đặc điểm:**
- Mỗi Pod có IP riêng
- Containers trong cùng Pod share network và storage
- Pod có thể bị xóa và tạo lại bất kỳ lúc nào (ephemeral)

**Lệnh:**
```powershell
# List pods
kubectl get pods

# Pod details
kubectl describe pod forum-xxx

# Pod logs
kubectl logs forum-xxx
```

---

### 3. **Deployment**

**Là gì:** Quản lý việc tạo và update Pods

**Ví dụ:**
```yaml
Deployment: forum
  ReplicaSet: forum-abc
    ├── Pod: forum-abc-1
    ├── Pod: forum-abc-2
    └── Pod: forum-abc-3
```

**Deployment làm gì:**
- ✅ Tạo và quản lý Pods
- ✅ Scale pods (tăng/giảm số lượng)
- ✅ Rolling updates (update không downtime)
- ✅ Rollback khi có lỗi
- ✅ Self-healing (tự restart pod bị lỗi)

**Lệnh:**
```powershell
# List deployments
kubectl get deployments

# Scale
kubectl scale deployment forum --replicas=3

# Update image
kubectl set image deployment/forum forum=micro-forum-service:v2

# Rollback
kubectl rollout undo deployment/forum
```

---

### 4. **StatefulSet**

**Là gì:** Giống Deployment nhưng cho các ứng dụng có **state** (như database)

**Khác biệt với Deployment:**

| Deployment | StatefulSet |
|------------|-------------|
| Pods có tên random (forum-abc-1) | Pods có tên cố định (postgres-0, postgres-1) |
| Không đảm bảo thứ tự start | Start theo thứ tự (0 → 1 → 2) |
| Không có persistent identity | Mỗi pod có identity riêng |
| Dùng cho stateless apps | Dùng cho stateful apps (DB) |

**Ví dụ:**
```yaml
StatefulSet: postgres
  ├── Pod: postgres-0 (always postgres-0)
  ├── Pod: postgres-1 (always postgres-1)
  └── Pod: postgres-2 (always postgres-2)
```

**Trong project này:**
- PostgreSQL: StatefulSet (có data)
- Zookeeper: StatefulSet (có state)
- Kafka: StatefulSet (có data)
- Forum Service: Deployment (stateless)

---

### 5. **Service**

**Là gì:** Expose Pods ra ngoài và provide load balancing

**Tại sao cần Service?**
- Pod IP thay đổi khi pod restart
- Service có IP cố định
- Service tự động route traffic đến pods healthy

**Các loại Service:**

#### a. **ClusterIP** (default)
- Chỉ accessible trong cluster
- Không thể access từ bên ngoài

```yaml
Service: forum (ClusterIP)
  IP: 10.96.123.45 (fixed)
  ↓
  Pods: forum-abc-1, forum-abc-2 (IPs thay đổi)
```

**Ví dụ trong project:**
- PostgreSQL: ClusterIP (chỉ services khác connect)
- Redis: ClusterIP
- Eureka: ClusterIP
- Forum: ClusterIP

#### b. **LoadBalancer**
- Expose ra ngoài internet
- Docker Desktop tạo load balancer trên localhost

```yaml
Service: api-gateway (LoadBalancer)
  External IP: localhost
  Port: 8080
```

**Ví dụ trong project:**
- API Gateway: LoadBalancer (để client access)

#### c. **NodePort**
- Expose qua port trên mỗi node
- Port range: 30000-32767

#### d. **Headless Service** (ClusterIP = None)
- Không có load balancing
- Dùng cho StatefulSet
- Trực tiếp trỏ đến Pod IPs

```yaml
Service: postgres (Headless)
  ↓
  Direct access: postgres-0, postgres-1, postgres-2
```

**Lệnh:**
```powershell
# List services
kubectl get services

# Service details
kubectl describe service forum

# Endpoints (actual pod IPs)
kubectl get endpoints forum
```

---

### 6. **ConfigMap**

**Là gì:** Lưu configuration không nhạy cảm (non-sensitive)

**Ví dụ:**
```yaml
ConfigMap: app-config
  POSTGRES_HOST: postgres
  POSTGRES_PORT: 5432
  REDIS_HOST: redis
  KAFKA_BOOTSTRAP_SERVERS: kafka:9092
```

**Dùng trong Pod:**
```yaml
env:
- name: POSTGRES_HOST
  valueFrom:
    configMapKeyRef:
      name: app-config
      key: POSTGRES_HOST
```

**Lợi ích:**
- ✅ Tách config ra khỏi code
- ✅ Dễ update config mà không rebuild image
- ✅ Share config giữa nhiều services

**Lệnh:**
```powershell
# View ConfigMap
kubectl get configmap app-config -o yaml

# Edit ConfigMap
kubectl edit configmap app-config
```

---

### 7. **Secret**

**Là gì:** Lưu thông tin nhạy cảm (passwords, API keys)

**Khác ConfigMap:**
- Data được **encode base64**
- Có thể **encrypt at rest**
- Permissions riêng biệt

**Ví dụ:**
```yaml
Secret: app-secrets
  postgres-password: MTIzNDU2 (base64 của "123456")
  mail-password: ZjIy...
```

**Dùng trong Pod:**
```yaml
env:
- name: POSTGRES_PASSWORD
  valueFrom:
    secretKeyRef:
      name: app-secrets
      key: postgres-password
```

**Lệnh:**
```powershell
# View Secret (encoded)
kubectl get secret app-secrets -o yaml

# Decode password
kubectl get secret app-secrets -o jsonpath='{.data.postgres-password}' | base64 -d
```

**⚠️ Lưu ý:**
- Base64 **KHÔNG phải encryption**, chỉ là encoding!
- Trong production, dùng thêm encryption hoặc external secret managers (Vault, AWS Secrets Manager)

---

### 8. **Namespace**

**Là gì:** Phân vùng logic trong cluster

**Tương tự:**
- Namespace = Folders
- Resources = Files

```
Cluster
├── Namespace: default
│   └── (empty)
├── Namespace: kube-system
│   └── Kubernetes system pods
└── Namespace: microservices
    ├── PostgreSQL
    ├── Redis
    ├── Kafka
    ├── Forum Service
    └── API Gateway
```

**Lợi ích:**
- ✅ Tách biệt resources
- ✅ Permissions khác nhau
- ✅ Resource quotas
- ✅ Dễ cleanup (delete namespace = delete all)

**Lệnh:**
```powershell
# List namespaces
kubectl get namespaces

# Set default namespace
kubectl config set-context --current --namespace=microservices

# Resources in namespace
kubectl get all -n microservices

# Delete namespace (delete ALL resources!)
kubectl delete namespace microservices
```

---

### 9. **Volume & PersistentVolumeClaim (PVC)**

**Là gì:** Storage cho Pods

**Vấn đề:**
- Container data bị mất khi container restart
- Cần persistent storage cho databases

**Giải pháp:**

#### **Volume Types:**

**a. emptyDir** - Temporary storage
```yaml
volumes:
- name: temp-storage
  emptyDir: {}
```
- Tạo khi Pod start
- Xóa khi Pod xóa
- Dùng cho cache, temp files

**b. PersistentVolume (PV)** - Durable storage
```yaml
PersistentVolume (PV) - Physical storage
  ↓
PersistentVolumeClaim (PVC) - Request for storage
  ↓
Pod - Uses PVC
```

**Ví dụ trong project:**
```yaml
StatefulSet: postgres-0
  ↓
  PVC: postgres-storage-postgres-0 (2Gi)
  ↓
  PV: pvc-abc123 (actual disk)
  ↓
  Physical storage on host machine
```

**Đặc điểm:**
- ✅ Data persists khi pod restart
- ✅ Data persists khi pod delete (có thể config)
- ✅ Có thể move giữa pods

**Lệnh:**
```powershell
# List PVCs
kubectl get pvc

# PVC details
kubectl describe pvc postgres-storage-postgres-0

# List PVs
kubectl get pv
```

---

### 10. **Ingress**

**Là gì:** HTTP/HTTPS routing từ ngoài vào cluster

**So sánh:**

| Service (LoadBalancer) | Ingress |
|------------------------|---------|
| Layer 4 (TCP/UDP) | Layer 7 (HTTP/HTTPS) |
| Một service = Một IP | Nhiều services = Một IP |
| Không có routing rules | Path-based routing |
| Không SSL termination | SSL termination |

**Ví dụ:**
```yaml
Ingress: api.example.com
  ↓
  /api/forum/** → forum-service
  /api/study/** → study-management-service
  /api/notification/** → notification-service
```

**Lợi ích:**
- ✅ Một IP cho nhiều services
- ✅ Path-based routing
- ✅ SSL termination
- ✅ Load balancing

**Lệnh:**
```powershell
# List ingress
kubectl get ingress

# Ingress details
kubectl describe ingress api-gateway-ingress
```

---

### 11. **Probes**

**Là gì:** Health checks cho Pods

#### **a. Liveness Probe**
- Kiểm tra pod còn sống không
- Nếu failed → restart pod

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health
    port: 8081
  initialDelaySeconds: 60
  periodSeconds: 10
```

#### **b. Readiness Probe**
- Kiểm tra pod sẵn sàng nhận traffic chưa
- Nếu failed → remove from service endpoints

```yaml
readinessProbe:
  httpGet:
    path: /actuator/health
    port: 8081
  initialDelaySeconds: 30
  periodSeconds: 5
```

**Flow:**
```
Pod starts
  ↓
Wait initialDelaySeconds (30s)
  ↓
Readiness Probe checks
  ↓ (if pass)
Pod added to Service endpoints (receives traffic)
  ↓
Every periodSeconds (5s): check readiness
  ↓
Every periodSeconds (10s): check liveness
```

---

### 12. **Labels & Selectors**

**Là gì:** Key-value pairs để organize và select resources

**Ví dụ:**
```yaml
# Pod có labels
Pod: forum-abc-123
  labels:
    app: forum
    version: v1
    environment: production

# Service select pods bằng labels
Service: forum
  selector:
    app: forum  # Match tất cả pods có label app=forum
```

**Lệnh:**
```powershell
# Get pods with label
kubectl get pods -l app=forum

# Get pods with multiple labels
kubectl get pods -l app=forum,version=v1

# Add label
kubectl label pod forum-abc app=forum-service

# Remove label
kubectl label pod forum-abc version-
```

---

## 🔄 Request Flow trong K8s

### Flow 1: External Request

```
User Browser
  ↓
http://localhost:8080/api/forum/topics
  ↓
Service: api-gateway (LoadBalancer)
  IP: localhost:8080
  ↓
Endpoints: api-gateway-abc-1 (10.1.2.3:8080)
  ↓
Pod: api-gateway-abc-1
  ↓
Container: API Gateway application
  ↓
Gateway routes to: lb://forum-service
  ↓
Eureka: lookup forum-service instances
  ↓
Service: forum (ClusterIP)
  IP: 10.96.50.10:8081
  ↓
Endpoints: forum-xyz-1 (10.1.2.5:8081), forum-xyz-2 (10.1.2.6:8081)
  ↓
Load balanced to one of:
  - Pod: forum-xyz-1
  - Pod: forum-xyz-2
  ↓
Container: Forum Service application
  ↓
Response back through same path
```

### Flow 2: Inter-Service Communication (gRPC)

```
Forum Service (Pod)
  ↓
Call: study-management:9090 (gRPC)
  ↓
Service: study-management (ClusterIP)
  IP: 10.96.50.20:9090
  ↓
Endpoints: study-management-aaa-1 (10.1.2.7:9090)
  ↓
Pod: study-management-aaa-1
  ↓
Container: Study Management gRPC Server
  ↓
Response: User data
```

### Flow 3: Database Access

```
Forum Service (Pod)
  ↓
Connection: postgres:5432
  ↓
Service: postgres (Headless ClusterIP)
  ↓
Direct to: postgres-0 (10.1.2.8:5432)
  ↓
Pod: postgres-0
  ↓
Container: PostgreSQL
  ↓
PVC: postgres-storage-postgres-0
  ↓
PV: Physical disk
```

---

## 🎓 So sánh với Docker Compose

| Docker Compose | Kubernetes |
|----------------|-----------|
| `docker-compose.yml` | Multiple YAML files |
| `services:` | `Deployment` / `StatefulSet` |
| `image:` | `spec.containers.image` |
| `ports:` | `Service` |
| `environment:` | `ConfigMap` / `Secret` |
| `volumes:` | `PersistentVolumeClaim` |
| `depends_on:` | Health checks + Probes |
| `networks:` | Automatic (all pods can talk) |
| `docker-compose up` | `kubectl apply` |
| `docker-compose down` | `kubectl delete` |

---

## 📊 Resource Hierarchy

```
Cluster
  └── Namespace: microservices
      ├── ConfigMap: app-config
      ├── Secret: app-secrets
      ├── StatefulSet: postgres
      │   ├── Pod: postgres-0
      │   │   └── Container: postgres
      │   └── PVC: postgres-storage-postgres-0
      │       └── PV: pvc-abc123
      ├── Service: postgres (ClusterIP)
      ├── Deployment: forum
      │   └── ReplicaSet: forum-abc
      │       ├── Pod: forum-abc-1
      │       └── Pod: forum-abc-2
      ├── Service: forum (ClusterIP)
      ├── Deployment: api-gateway
      │   └── Pod: api-gateway-xyz-1
      ├── Service: api-gateway (LoadBalancer)
      └── Ingress: api-gateway-ingress
```

---

## 💡 Tips cho người mới

### 1. **Luôn luôn check logs khi có lỗi**
```powershell
kubectl logs <pod-name>
kubectl describe pod <pod-name>
```

### 2. **Hiểu lifecycle của Pod**
```
Pending → Creating → Running → Succeeded/Failed
```

### 3. **Labels là best friend**
```powershell
kubectl get pods -l app=forum
kubectl logs -l app=forum --tail=20
```

### 4. **Port forward để test**
```powershell
kubectl port-forward service/forum 8081:8081
```

### 5. **Namespace giúp tổ chức**
```powershell
kubectl get all -n microservices
```

---

**🎉 Giờ bạn đã hiểu các khái niệm cơ bản của Kubernetes!**

Để practice, thử:
1. Scale deployment: `kubectl scale deployment forum --replicas=3`
2. Xem pods được tạo: `kubectl get pods -l app=forum`
3. Delete một pod: `kubectl delete pod <pod-name>`
4. Xem K8s tự tạo pod mới (self-healing)
