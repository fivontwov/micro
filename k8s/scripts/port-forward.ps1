# Port forward to access services locally

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("all", "eureka", "api-gateway", "forum", "study-management", "notification", "postgres", "redis", "kafka")]
    [string]$Service = "all"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Port Forwarding" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

kubectl config set-context --current --namespace=microservices 2>$null

if ($Service -eq "all") {
    Write-Host "Starting port forwarding for all services..." -ForegroundColor Green
    Write-Host "Press Ctrl+C to stop" -ForegroundColor Yellow
    Write-Host ""
    
    Write-Host "📍 Access URLs:" -ForegroundColor Cyan
    Write-Host "   Eureka:            http://localhost:8761" -ForegroundColor White
    Write-Host "   API Gateway:       http://localhost:8080" -ForegroundColor White
    Write-Host "   Forum:             http://localhost:8081" -ForegroundColor White
    Write-Host "   Study Management:  http://localhost:8082" -ForegroundColor White
    Write-Host "   Notification:      http://localhost:8083" -ForegroundColor White
    Write-Host ""
    
    # Start all port forwards in background jobs
    Start-Job -ScriptBlock { kubectl port-forward -n microservices service/eureka 8761:8761 } | Out-Null
    Start-Sleep -Milliseconds 500
    Start-Job -ScriptBlock { kubectl port-forward -n microservices service/api-gateway 8080:8080 } | Out-Null
    Start-Sleep -Milliseconds 500
    Start-Job -ScriptBlock { kubectl port-forward -n microservices service/forum 8081:8081 } | Out-Null
    Start-Sleep -Milliseconds 500
    Start-Job -ScriptBlock { kubectl port-forward -n microservices service/study-management 8082:8082 } | Out-Null
    Start-Sleep -Milliseconds 500
    Start-Job -ScriptBlock { kubectl port-forward -n microservices service/notification 8083:8083 } | Out-Null
    
    Write-Host "✅ All port forwards started" -ForegroundColor Green
    Write-Host ""
    Write-Host "To stop all: Get-Job | Stop-Job; Get-Job | Remove-Job" -ForegroundColor Yellow
    
    # Keep script running
    while ($true) {
        Start-Sleep -Seconds 10
        $jobs = Get-Job | Where-Object { $_.State -eq "Running" }
        if ($jobs.Count -eq 0) {
            Write-Host "All port forwards stopped" -ForegroundColor Yellow
            break
        }
    }
} else {
    # Port forward single service
    $ports = @{
        "eureka" = "8761:8761"
        "api-gateway" = "8080:8080"
        "forum" = "8081:8081"
        "study-management" = "8082:8082"
        "notification" = "8083:8083"
        "postgres" = "5432:5432"
        "redis" = "6379:6379"
        "kafka" = "9092:9092"
    }
    
    $port = $ports[$Service]
    Write-Host "Port forwarding: $Service ($port)" -ForegroundColor Green
    Write-Host "Press Ctrl+C to stop" -ForegroundColor Yellow
    Write-Host ""
    
    kubectl port-forward -n microservices service/$Service $port
}
