# Test script for all microservices

Write-Host "Starting tests for all services..." -ForegroundColor Cyan

$services = @(
    "ai-service",
    "api-gateway",
    "appointment-service",
    "auth-service",
    "billing-service",
    "config-service",
    "discovery-service",
    "medical-record-service",
    "ordonnance-service",
    "patient-service"
)

foreach ($service in $services) {
    Write-Host "`n>>> Testing $service..." -ForegroundColor Yellow
    Push-Location $service
    try {
        if (Test-Path "pom.xml") {
            mvn test
        } elseif (Test-Path "requirements.txt") {
            # Assuming python is installed and venv is active or deps are in path
            pytest
        } else {
            Write-Host "No known build tool found in $service" -ForegroundColor Red
        }
    } catch {
        Write-Host "Tests failed for $service" -ForegroundColor Red
    }
    Pop-Location
}

Write-Host "`nAll service tests completed." -ForegroundColor Green
