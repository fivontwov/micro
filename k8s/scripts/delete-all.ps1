# Delete all Kubernetes resources

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Deleting Kubernetes Resources" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$confirmation = Read-Host "Are you sure you want to delete ALL resources? (yes/no)"
if ($confirmation -ne "yes") {
    Write-Host "❌ Cancelled" -ForegroundColor Yellow
    exit 0
}

$k8sDir = Split-Path -Parent $PSScriptRoot

Write-Host ""
Write-Host "🗑️  Deleting resources..." -ForegroundColor Red
Write-Host ""

# Delete in reverse order
Write-Host "[1/6] Deleting API Gateway..." -ForegroundColor Yellow
kubectl delete -f "$k8sDir\05-gateway\" --ignore-not-found=true
Start-Sleep -Seconds 2

Write-Host "[2/6] Deleting Application Services..." -ForegroundColor Yellow
kubectl delete -f "$k8sDir\04-services\" --ignore-not-found=true
Start-Sleep -Seconds 2

Write-Host "[3/6] Deleting Eureka Server..." -ForegroundColor Yellow
kubectl delete -f "$k8sDir\03-discovery\" --ignore-not-found=true
Start-Sleep -Seconds 2

Write-Host "[4/6] Deleting Infrastructure (forum-postgres, study-postgres, redis, zookeeper, kafka)..." -ForegroundColor Yellow
kubectl delete -f "$k8sDir\02-infrastructure\" --ignore-not-found=true
Start-Sleep -Seconds 5

Write-Host "[5/6] Deleting ConfigMaps and Secrets..." -ForegroundColor Yellow
kubectl delete -f "$k8sDir\01-config\" --ignore-not-found=true
Start-Sleep -Seconds 2

Write-Host "[6/6] Deleting Namespace..." -ForegroundColor Yellow
kubectl delete -f "$k8sDir\00-namespace\namespace.yaml" --ignore-not-found=true

Write-Host ""
Write-Host "⏳ Waiting for resources to terminate..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Cleanup Complete ✅" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "📊 Remaining resources:" -ForegroundColor Yellow
kubectl get all -n microservices 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "   (Namespace deleted - no resources remaining)" -ForegroundColor Green
}

Write-Host ""
Write-Host "💡 To redeploy: .\scripts\deploy-all.ps1" -ForegroundColor Cyan
