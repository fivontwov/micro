# 🔧 Troubleshooting Guide - Kubernetes

## 📋 Mục lục
1. [Pod Issues](#pod-issues)
2. [Database Issues](#database-issues)
3. [Kafka Issues](#kafka-issues)
4. [Service Discovery Issues](#service-discovery-issues)
5. [Network Issues](#network-issues)
6. [Resource Issues](#resource-issues)
7. [Common Errors](#common-errors)

---

## 🔴 Pod Issues

### Pod không start (CrashLoopBackOff)

**Triệu chứng:**
```powershell
kubectl get pods
# NAME          READY   STATUS             RESTARTS   AGE
# forum-xxx     0/1     CrashLoopBackOff   5          3m
```

**Nguyên nhân & Giải pháp:**

1. **Check logs:**
```powershell
kubectl logs forum-xxx
kubectl logs forum-xxx --previous  # Logs của pod trước khi crash
```

2. **Lỗi thường gặp:**

**a) Database connection failed**
```
Error: Could not connect to PostgreSQL
```
**Fix:**
```powershell
# Check PostgreSQL running
kubectl get pods -l app=postgres
# Nếu chưa ready, đợi thêm

# Test connection
kubectl exec -it postgres-0 -- psql -U postgres -c "SELECT 1"
```

**b) Eureka connection failed**
```
Error: Connection refused to eureka:8761
```
**Fix:**
```powershell
# Check Eureka running
kubectl get pods -l app=eureka

# Eureka phải start TRƯỚC các services khác
# Nếu cần, restart service:
kubectl rollout restart deployment/forum
```

**c) Image not found**
```
Error: ErrImagePull or ImagePullBackOff
```
**Fix:**
```powershell
# Build image locally
cd forum
docker build -t micro-forum-service:latest .

# Verify image exists
docker images | Select-String "micro-forum"

# Redeploy
kubectl delete pod -l app=forum
```

### Pod pending (không start)

**Triệu chứng:**
```powershell
kubectl get pods
# NAME          READY   STATUS    RESTARTS   AGE
# kafka-0       0/1     Pending   0          5m
```

**Nguyên nhân & Giải pháp:**

1. **Check events:**
```powershell
kubectl describe pod kafka-0
# Look for: "FailedScheduling" or "Insufficient resources"
```

2. **Không đủ resources:**
```
Error: Insufficient cpu/memory
```
**Fix:**
```powershell
# Giảm resource requests trong deployment
# Edit kafka-statefulset.yaml:
# resources:
#   requests:
#     memory: "256Mi"  # Giảm từ 512Mi
#     cpu: "250m"      # Giảm từ 500m
```

3. **PVC không bind:**
```powershell
kubectl get pvc
# STATUS phải là Bound

# Nếu Pending:
kubectl describe pvc postgres-storage-postgres-0
# Check events
```

### Pod ready nhưng không health check pass

**Triệu chứng:**
```powershell
kubectl get pods
# NAME          READY   STATUS    RESTARTS   AGE
# forum-xxx     0/1     Running   0          2m
```

**Fix:**
```powershell
# 1. Check readiness probe
kubectl describe pod forum-xxx | Select-String -Pattern "Readiness:" -Context 0,5

# 2. Test endpoint manually
kubectl exec -it forum-xxx -- curl http://localhost:8081/actuator/health

# 3. Nếu failed, check logs
kubectl logs forum-xxx --tail=50

# 4. Temporary fix: Tăng initialDelaySeconds
# Edit forum-deployment.yaml:
# readinessProbe:
#   initialDelaySeconds: 90  # Tăng từ 60
```

---

## 🗄️ Database Issues

### PostgreSQL pod không start

**Triệu chứng:**
```
Error: initdb: error: directory "/var/lib/postgresql/data" exists but is not empty
```

**Fix:**
```powershell
# Xóa PVC và tạo lại
kubectl delete pvc postgres-storage-postgres-0
kubectl delete pod postgres-0

# Pod sẽ tự restart và tạo PVC mới
```

### Cannot create database

**Triệu chứng:**
```
ERROR:  database "study_management" does not exist
```

**Fix:**
```powershell
# Connect và tạo database
kubectl exec -it postgres-0 -- psql -U postgres

# Trong psql:
CREATE DATABASE study_management;
\l  # Verify
\q
```

### Database connection refused

**Triệu chứng:**
```
Connection to postgres:5432 refused
```

**Fix:**
```powershell
# 1. Check PostgreSQL service
kubectl get service postgres
# PORT phải là 5432

# 2. Check pod running
kubectl get pods -l app=postgres
# STATUS = Running

# 3. Check logs
kubectl logs postgres-0 --tail=20
# Phải thấy: "database system is ready to accept connections"

# 4. Test từ một pod khác
kubectl run pg-test --rm -it --image=postgres:15-alpine -- psql -h postgres -U postgres
# Password: 123456
```

---

## 📬 Kafka Issues

### Kafka pod không start

**Triệu chứng:**
```
Error: Connection to zookeeper:2181 failed
```

**Fix:**
```powershell
# 1. Check Zookeeper running TRƯỚC
kubectl get pods -l app=zookeeper
# Phải Running

# 2. Wait for Zookeeper ready
kubectl wait --for=condition=ready pod -l app=zookeeper --timeout=120s

# 3. Restart Kafka
kubectl delete pod kafka-0
```

### Kafka consumer không nhận messages

**Triệu chứng:**
- Notification service không log "Received comment event"
- Kafka lag > 0

**Fix:**

**1. Check topic exists:**
```powershell
kubectl exec kafka-0 -- kafka-topics --bootstrap-server localhost:9092 --list
# Phải thấy: forum.comment.created
```

**2. Check consumer group:**
```powershell
kubectl exec kafka-0 -- kafka-consumer-groups `
  --bootstrap-server localhost:9092 `
  --describe --group notification-service-group

# LAG phải là 0 (consumer up-to-date)
```

**3. Check messages in topic:**
```powershell
kubectl exec -it kafka-0 -- kafka-console-consumer `
  --bootstrap-server localhost:9092 `
  --topic forum.comment.created `
  --from-beginning

# Phải thấy messages
```

**4. Check notification service logs:**
```powershell
kubectl logs -l app=notification --tail=100
# Phải thấy: "Kafka consumer initialized"
```

**5. Reset consumer offset (nếu cần):**
```powershell
# Stop notification service
kubectl scale deployment notification --replicas=0

# Reset offset
kubectl exec kafka-0 -- kafka-consumer-groups `
  --bootstrap-server localhost:9092 `
  --group notification-service-group `
  --reset-offsets --to-earliest `
  --topic forum.comment.created --execute

# Start notification service
kubectl scale deployment notification --replicas=1
```

### Kafka topic auto-delete

**Triệu chứng:**
- Topic bị xóa sau một thời gian

**Fix:**
```powershell
# Set retention policy
kubectl exec kafka-0 -- kafka-configs `
  --bootstrap-server localhost:9092 `
  --entity-type topics `
  --entity-name forum.comment.created `
  --alter --add-config retention.ms=604800000
# 604800000 ms = 7 days
```

---

## 🔍 Service Discovery Issues

### Services không register với Eureka

**Triệu chứng:**
- Eureka dashboard không show services
- API Gateway không route được

**Fix:**

**1. Check Eureka server:**
```powershell
kubectl port-forward service/eureka 8761:8761
# Open: http://localhost:8761
# Phải thấy Eureka dashboard
```

**2. Check service logs:**
```powershell
kubectl logs -l app=forum --tail=50 | Select-String "Eureka"
# Phải thấy: "Registered with Eureka" hoặc "DiscoveryClient_FORUM-SERVICE"
```

**3. Check environment variables:**
```powershell
kubectl exec -it deployment/forum -- env | Select-String "EUREKA"
# EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka:8761/eureka/
```

**4. Test connection to Eureka:**
```powershell
kubectl exec -it deployment/forum -- curl http://eureka:8761/actuator/health
# {"status":"UP"}
```

**5. Restart service:**
```powershell
kubectl rollout restart deployment/forum
kubectl rollout status deployment/forum
```

### API Gateway không tìm thấy services

**Triệu chứng:**
```
503 Service Unavailable
No available servers for: lb://forum-service
```

**Fix:**

**1. Check Eureka registration:**
```powershell
curl http://localhost:8761/eureka/apps | Select-String "FORUM-SERVICE"
# Phải thấy FORUM-SERVICE
```

**2. Check Gateway có fetch registry không:**
```powershell
kubectl logs -l app=api-gateway --tail=100 | Select-String "Eureka"
# Phải thấy: "Fetched registry successfully"
```

**3. Check Gateway routes:**
```powershell
kubectl port-forward service/api-gateway 8080:8080
curl http://localhost:8080/actuator/gateway/routes
# Phải thấy route với uri: lb://forum-service
```

**4. Restart Gateway:**
```powershell
kubectl rollout restart deployment/api-gateway
```

---

## 🌐 Network Issues

### Service không accessible từ pod khác

**Triệu chứng:**
```
curl: (6) Could not resolve host: postgres
```

**Fix:**

**1. Check DNS:**
```powershell
kubectl run dnstest --rm -it --image=busybox -- nslookup postgres
# Phải resolve được IP
```

**2. Check service exists:**
```powershell
kubectl get service postgres
# NAME       TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)
# postgres   ClusterIP   10.96.123.456   <none>        5432/TCP
```

**3. Check endpoints:**
```powershell
kubectl get endpoints postgres
# ENDPOINTS phải có IP:PORT
```

**4. Test connectivity:**
```powershell
kubectl run nettest --rm -it --image=nicolaka/netshoot -- /bin/bash
# Trong container:
ping postgres
telnet postgres 5432
curl http://forum:8081/actuator/health
```

### Port forward không work

**Triệu chứng:**
```
Error: unable to forward port
```

**Fix:**

**1. Check pod running:**
```powershell
kubectl get pods -l app=forum
# STATUS phải là Running, READY = 1/1
```

**2. Kill existing port-forward:**
```powershell
# Find process
Get-Process | Where-Object {$_.ProcessName -eq "kubectl"}

# Kill
Stop-Process -Name kubectl -Force

# Retry port-forward
kubectl port-forward service/forum 8081:8081
```

**3. Use pod name instead of service:**
```powershell
kubectl port-forward deployment/forum 8081:8081
```

---

## 💾 Resource Issues

### Out of memory (OOMKilled)

**Triệu chứng:**
```powershell
kubectl get pods
# NAME          READY   STATUS      RESTARTS   AGE
# forum-xxx     0/1     OOMKilled   3          5m
```

**Fix:**

**1. Check resource limits:**
```powershell
kubectl describe pod forum-xxx | Select-String -Pattern "Limits:" -Context 0,3
```

**2. Increase memory limit:**
```yaml
# Edit forum-deployment.yaml:
resources:
  limits:
    memory: "2Gi"  # Tăng từ 1Gi
```

**3. Apply changes:**
```powershell
kubectl apply -f k8s/04-services/forum-deployment.yaml
kubectl rollout status deployment/forum
```

### Insufficient CPU

**Triệu chứng:**
```
Warning: Insufficient cpu
```

**Fix:**
```yaml
# Giảm CPU request trong deployment:
resources:
  requests:
    cpu: "250m"  # Giảm từ 500m
```

### Disk full

**Triệu chứng:**
```
Error: No space left on device
```

**Fix:**

**1. Check disk usage:**
```powershell
kubectl exec postgres-0 -- df -h
```

**2. Clean up:**
```powershell
# Xóa unused images
docker system prune -a --volumes

# Xóa old pods
kubectl delete pods --field-selector status.phase=Succeeded
kubectl delete pods --field-selector status.phase=Failed
```

**3. Increase PVC size:**
```yaml
# Edit postgres-statefulset.yaml:
volumeClaimTemplates:
  - metadata:
      name: postgres-storage
    spec:
      resources:
        requests:
          storage: 5Gi  # Tăng từ 2Gi
```

---

## ❌ Common Errors

### Error 1: ImagePullBackOff

```
Error: Failed to pull image "micro-forum-service:latest": not found
```

**Fix:**
```powershell
# Build image
cd forum
docker build -t micro-forum-service:latest .

# Verify
docker images | Select-String "micro-forum"

# Redeploy
kubectl delete pod -l app=forum
```

### Error 2: CreateContainerConfigError

```
Error: couldn't find key postgres-password in Secret
```

**Fix:**
```powershell
# Check secret exists
kubectl get secret app-secrets

# Recreate secret
kubectl delete secret app-secrets
kubectl apply -f k8s/01-config/secrets.yaml

# Restart pods
kubectl rollout restart deployment/forum
```

### Error 3: Liveness probe failed

```
Liveness probe failed: HTTP probe failed with statuscode: 503
```

**Fix:**
```powershell
# Tăng timeout và initial delay
# Edit deployment:
livenessProbe:
  initialDelaySeconds: 120  # Tăng từ 90
  timeoutSeconds: 10        # Tăng từ 5
```

### Error 4: Unable to connect to database

```
Error: Connection to postgres:5432 refused
```

**Fix:**
```powershell
# 1. Check PostgreSQL ready
kubectl get pods -l app=postgres

# 2. Check service
kubectl get service postgres

# 3. Test connection
kubectl run pgtest --rm -it --image=postgres:15-alpine -- psql -h postgres -U postgres

# 4. Check credentials
kubectl get secret app-secrets -o yaml
# Decode password:
kubectl get secret app-secrets -o jsonpath='{.data.postgres-password}' | base64 -d
```

### Error 5: gRPC connection failed

```
Error: UNAVAILABLE: io exception
Channel ManagedChannelImpl{logId=1, target=study-management:9090}
```

**Fix:**
```powershell
# 1. Check Study Management running
kubectl get pods -l app=study-management

# 2. Check gRPC port exposed
kubectl get service study-management
# Phải có port 9090

# 3. Test gRPC port
kubectl exec -it deployment/forum -- nc -zv study-management 9090
# Connection to study-management 9090 port [tcp/*] succeeded!

# 4. Check logs
kubectl logs -l app=study-management | Select-String "gRPC"
```

---

## 🛠️ Debug Commands Cheat Sheet

```powershell
# Pod debugging
kubectl get pods -A                          # All pods in all namespaces
kubectl describe pod <pod-name>              # Pod details
kubectl logs <pod-name> --tail=100 -f        # Follow logs
kubectl logs <pod-name> --previous           # Previous container logs
kubectl exec -it <pod-name> -- /bin/bash     # SSH into pod

# Service debugging
kubectl get services                         # List services
kubectl get endpoints                        # Service endpoints
kubectl describe service <service-name>      # Service details

# Network debugging
kubectl run netdebug --rm -it --image=nicolaka/netshoot -- /bin/bash
# Inside: ping, curl, nslookup, telnet, traceroute

# Events
kubectl get events --sort-by='.lastTimestamp' --all-namespaces
kubectl get events -w                        # Watch events

# Resources
kubectl top nodes                            # Node resources
kubectl top pods                             # Pod resources
kubectl describe node docker-desktop         # Node details

# ConfigMaps & Secrets
kubectl get configmaps                       # List configmaps
kubectl describe configmap app-config        # ConfigMap details
kubectl get secret app-secrets -o yaml       # Secret (base64 encoded)

# Rollouts
kubectl rollout history deployment/forum     # Deployment history
kubectl rollout status deployment/forum      # Rollout status
kubectl rollout undo deployment/forum        # Rollback
kubectl rollout restart deployment/forum     # Restart deployment

# Scaling
kubectl scale deployment forum --replicas=3  # Scale up
kubectl scale deployment forum --replicas=1  # Scale down
kubectl autoscale deployment forum --min=2 --max=5 --cpu-percent=70

# Port forwarding
kubectl port-forward service/forum 8081:8081
kubectl port-forward pod/forum-xxx 8081:8081
```

---

## 📞 Need Help?

Nếu vẫn gặp vấn đề:

1. **Collect logs:**
```powershell
kubectl logs -l app=<service-name> > logs.txt
```

2. **Describe resources:**
```powershell
kubectl describe pod <pod-name> > pod-description.txt
```

3. **Get events:**
```powershell
kubectl get events --sort-by='.lastTimestamp' > events.txt
```

4. **Export all resources:**
```powershell
kubectl get all -n microservices -o yaml > all-resources.yaml
```

Đính kèm các files này khi hỏi trên Stack Overflow hoặc GitHub Issues!

---

**💡 Pro Tip:** Thường xuyên check logs và events. 90% lỗi Kubernetes có thể debug qua logs!
