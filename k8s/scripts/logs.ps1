# View logs of a specific service

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("forum-postgres", "study-postgres", "redis", "zookeeper", "kafka", "eureka", "forum", "study-management", "notification", "api-gateway")]
    [string]$Service,
    
    [Parameter(Mandatory=$false)]
    [int]$Lines = 100,
    
    [Parameter(Mandatory=$false)]
    [switch]$Follow
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Logs: $Service" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Set namespace
kubectl config set-context --current --namespace=microservices 2>$null

# Get pod name
$podName = ""
if ($Service -in @("forum-postgres", "study-postgres", "zookeeper", "kafka")) {
    $podName = "$Service-0"
} else {
    $pods = kubectl get pods -n microservices -l app=$Service -o name 2>$null
    if ($pods) {
        $podName = ($pods | Select-Object -First 1) -replace "pod/", ""
    }
}

if (-not $podName) {
    Write-Host "❌ Pod not found for service: $Service" -ForegroundColor Red
    exit 1
}

Write-Host "📋 Pod: $podName" -ForegroundColor Green
Write-Host ""

# Build kubectl command
$cmd = "kubectl logs $podName -n microservices --tail=$Lines"
if ($Follow) {
    $cmd += " -f"
}

Write-Host "Command: $cmd" -ForegroundColor Yellow
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Execute
Invoke-Expression $cmd
