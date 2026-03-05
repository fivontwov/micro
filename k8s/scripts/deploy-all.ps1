# Deploy all Kubernetes resources

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Deploying to Kubernetes" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$ErrorActionPreference = "Stop"
$k8sDir = Split-Path -Parent $PSScriptRoot

# Check kubectl
try {
    kubectl version --client --short 2>$null | Out-Null
} catch {
    Write-Host "❌ kubectl not found. Please install kubectl first." -ForegroundColor Red
    exit 1
}

# Check cluster connection
try {
    kubectl cluster-info 2>$null | Out-Null
} catch {
    Write-Host "❌ Cannot connect to Kubernetes cluster. Is Docker Desktop Kubernetes running?" -ForegroundColor Red
    exit 1
}

Write-Host "✅ kubectl and cluster OK" -ForegroundColor Green
Write-Host ""

# Step 1: Namespace
Write-Host "📦 [1/6] Creating namespace..." -ForegroundColor Green
kubectl apply -f "$k8sDir\00-namespace\namespace.yaml"
Start-Sleep -Seconds 2
Write-Host ""

# Set default namespace
kubectl config set-context --current --namespace=microservices
Write-Host "✅ Default namespace set to 'microservices'" -ForegroundColor Green
Write-Host ""

# Step 2: ConfigMaps and Secrets
Write-Host "🔐 [2/6] Creating ConfigMaps and Secrets..." -ForegroundColor Green
kubectl apply -f "$k8sDir\01-config\"
Start-Sleep -Seconds 2
Write-Host ""

# Step 3: Infrastructure
Write-Host "🏗️  [3/6] Deploying Infrastructure (PostgreSQL, Redis, Zookeeper, Kafka)..." -ForegroundColor Green
Write-Host "   This may take 2-3 minutes..." -ForegroundColor Yellow

# Forum PostgreSQL (postgres:16)
kubectl apply -f "$k8sDir\02-infrastructure\forum-postgres-statefulset.yaml"
kubectl apply -f "$k8sDir\02-infrastructure\forum-postgres-service.yaml"
Write-Host "   ⏳ Waiting for Forum PostgreSQL (postgres:16)..."
kubectl wait --for=condition=ready pod -l app=forum-postgres --timeout=120s

# Study Management PostgreSQL (postgres:15)
kubectl apply -f "$k8sDir\02-infrastructure\study-postgres-statefulset.yaml"
kubectl apply -f "$k8sDir\02-infrastructure\study-postgres-service.yaml"
Write-Host "   ⏳ Waiting for Study PostgreSQL (postgres:15)..."
kubectl wait --for=condition=ready pod -l app=study-postgres --timeout=120s

# Redis
kubectl apply -f "$k8sDir\02-infrastructure\redis-deployment.yaml"
kubectl apply -f "$k8sDir\02-infrastructure\redis-service.yaml"
Write-Host "   ⏳ Waiting for Redis..."
kubectl wait --for=condition=ready pod -l app=redis --timeout=60s

# Zookeeper
kubectl apply -f "$k8sDir\02-infrastructure\zookeeper-statefulset.yaml"
kubectl apply -f "$k8sDir\02-infrastructure\zookeeper-service.yaml"
Write-Host "   ⏳ Waiting for Zookeeper..."
kubectl wait --for=condition=ready pod -l app=zookeeper --timeout=120s

# Kafka
kubectl apply -f "$k8sDir\02-infrastructure\kafka-statefulset.yaml"
kubectl apply -f "$k8sDir\02-infrastructure\kafka-service.yaml"
Write-Host "   ⏳ Waiting for Kafka (this takes 2-3 minutes)..."
kubectl wait --for=condition=ready pod -l app=kafka --timeout=180s

Write-Host "✅ Infrastructure deployed" -ForegroundColor Green
Write-Host ""

# Step 4: Eureka Server
Write-Host "🔍 [4/6] Deploying Eureka Server..." -ForegroundColor Green
kubectl apply -f "$k8sDir\03-discovery\"
Write-Host "   ⏳ Waiting for Eureka..."
kubectl wait --for=condition=ready pod -l app=eureka --timeout=120s
Write-Host "✅ Eureka Server deployed" -ForegroundColor Green
Write-Host ""

# Wait a bit for Eureka to be fully ready
Start-Sleep -Seconds 10

# Step 5: Application Services
Write-Host "🚀 [5/6] Deploying Application Services..." -ForegroundColor Green
Write-Host "   This may take 2-3 minutes..." -ForegroundColor Yellow

# Study Management (phải deploy TRƯỚC forum vì forum cần gRPC)
kubectl apply -f "$k8sDir\04-services\study-deployment.yaml"
kubectl apply -f "$k8sDir\04-services\study-service.yaml"
Write-Host "   ⏳ Waiting for Study Management..."
kubectl wait --for=condition=ready pod -l app=study-management --timeout=180s

# Forum Service
kubectl apply -f "$k8sDir\04-services\forum-deployment.yaml"
kubectl apply -f "$k8sDir\04-services\forum-service.yaml"
Write-Host "   ⏳ Waiting for Forum..."
kubectl wait --for=condition=ready pod -l app=forum --timeout=180s

# Notification Service
kubectl apply -f "$k8sDir\04-services\notification-deployment.yaml"
kubectl apply -f "$k8sDir\04-services\notification-service.yaml"
Write-Host "   ⏳ Waiting for Notification..."
kubectl wait --for=condition=ready pod -l app=notification --timeout=120s

Write-Host "✅ Application Services deployed" -ForegroundColor Green
Write-Host ""

# Step 6: API Gateway
Write-Host "🌐 [6/6] Deploying API Gateway..." -ForegroundColor Green
kubectl apply -f "$k8sDir\05-gateway\"
Write-Host "   ⏳ Waiting for API Gateway..."
kubectl wait --for=condition=ready pod -l app=api-gateway --timeout=120s
Write-Host "✅ API Gateway deployed" -ForegroundColor Green
Write-Host ""

# Summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Deployment Complete! ✅" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "📊 Pod Status:" -ForegroundColor Yellow
kubectl get pods
Write-Host ""

Write-Host "🌐 Services:" -ForegroundColor Yellow
kubectl get services
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Next Steps" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Check Eureka Dashboard:" -ForegroundColor Green
Write-Host "   kubectl port-forward service/eureka 8761:8761" -ForegroundColor White
Write-Host "   http://localhost:8761" -ForegroundColor Cyan
Write-Host ""
Write-Host "2. Test API Gateway:" -ForegroundColor Green
Write-Host "   kubectl port-forward service/api-gateway 8080:8080" -ForegroundColor White
Write-Host "   curl http://localhost:8080/actuator/health" -ForegroundColor Cyan
Write-Host ""
Write-Host "3. Run status check:" -ForegroundColor Green
Write-Host "   .\scripts\status.ps1" -ForegroundColor White
Write-Host ""
Write-Host "4. View logs:" -ForegroundColor Green
Write-Host "   .\scripts\logs.ps1 <service-name>" -ForegroundColor White
Write-Host ""
Write-Host "For detailed testing: See k8s\TESTING_GUIDE.md" -ForegroundColor Yellow
