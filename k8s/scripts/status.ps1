# Check status of all Kubernetes resources

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Kubernetes Status Check" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check cluster
Write-Host "🔍 Cluster Info:" -ForegroundColor Yellow
kubectl cluster-info
Write-Host ""

# Set namespace
kubectl config set-context --current --namespace=microservices 2>$null

# Namespaces
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "📦 Namespaces" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
kubectl get namespaces | Select-String "microservices"
Write-Host ""

# ConfigMaps
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "🔧 ConfigMaps" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
kubectl get configmaps -n microservices
Write-Host ""

# Secrets
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "🔐 Secrets" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
kubectl get secrets -n microservices
Write-Host ""

# Pods
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "🚀 Pods" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
kubectl get pods -n microservices -o wide
Write-Host ""

# Check pod status
$pods = kubectl get pods -n microservices -o json | ConvertFrom-Json
$totalPods = $pods.items.Count
$runningPods = ($pods.items | Where-Object { $_.status.phase -eq "Running" }).Count
$readyPods = ($pods.items | Where-Object { 
    $_.status.containerStatuses | Where-Object { $_.ready -eq $true }
}).Count

Write-Host "Total Pods: $totalPods" -ForegroundColor White
Write-Host "Running: $runningPods" -ForegroundColor Green
Write-Host "Ready: $readyPods" -ForegroundColor Green
Write-Host ""

# Services
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "🌐 Services" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
kubectl get services -n microservices
Write-Host ""

# Deployments
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "📋 Deployments" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
kubectl get deployments -n microservices
Write-Host ""

# StatefulSets
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "💾 StatefulSets" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
kubectl get statefulsets -n microservices
Write-Host ""

# PVCs
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "💿 Persistent Volume Claims" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
kubectl get pvc -n microservices
Write-Host ""

# Events (recent)
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "📰 Recent Events" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
kubectl get events -n microservices --sort-by='.lastTimestamp' | Select-Object -Last 10
Write-Host ""

# Health check summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "💚 Health Check Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$services = @("forum-postgres", "study-postgres", "redis", "zookeeper", "kafka", "eureka", "study-management", "forum", "notification", "api-gateway")

foreach ($service in $services) {
    $pod = kubectl get pods -n microservices -l app=$service -o name 2>$null | Select-Object -First 1
    if ($pod) {
        $podName = $pod -replace "pod/", ""
        $status = kubectl get pod $podName -n microservices -o jsonpath='{.status.phase}' 2>$null
        $ready = kubectl get pod $podName -n microservices -o jsonpath='{.status.containerStatuses[0].ready}' 2>$null
        
        if ($status -eq "Running" -and $ready -eq "true") {
            Write-Host "✅ $service" -ForegroundColor Green
        } else {
            Write-Host "❌ $service (Status: $status, Ready: $ready)" -ForegroundColor Red
        }
    } else {
        Write-Host "⚠️  $service (Not found)" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Status Check Complete" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "💡 To view logs: .\scripts\logs.ps1 <service-name>" -ForegroundColor Cyan
Write-Host "💡 To test: See k8s\TESTING_GUIDE.md" -ForegroundColor Cyan
