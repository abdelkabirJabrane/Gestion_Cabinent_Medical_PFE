# test-integration.ps1
# Script compatible ASCII pour eviter les erreurs d'encodage PowerShell

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " --- Lancement des TESTS D'INTEGRATION --- " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$services = @(
    "auth-service", 
    "appointment-service", 
    "billing-service", 
    "medical-record-service", 
    "ordonnance-service", 
    "patient-service",
    "ai-service"
)

$results = @{}

foreach ($service in $services) {
    Write-Host "`n>>> [Integration] Service: $service" -ForegroundColor Yellow
    
    if (Test-Path $service) {
        Push-Location $service
        try {
            if (Test-Path "pom.xml") {
                mvn test -Dtest="*IntegrationTest"
                if ($LASTEXITCODE -eq 0) { $results[$service] = "SUCCESS" } else { $results[$service] = "FAILED" }
            } elseif ($service -eq "ai-service") {
                $env:PYTHONPATH="."
                python -m pytest tests/test_api.py
                if ($LASTEXITCODE -eq 0) { $results[$service] = "SUCCESS" } else { $results[$service] = "FAILED" }
            }
        } catch {
            $results[$service] = "ERROR"
        }
        Pop-Location
    } else {
        Write-Host "Service $service non trouve." -ForegroundColor Red
        $results[$service] = "NOT FOUND"
    }
}

Write-Host "`n==========================================================" -ForegroundColor Cyan
Write-Host " --- RESUME DES TESTS D'INTEGRATION --- " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

foreach ($s in $services) {
    $status = $results[$s]
    $color = "White"
    if ($status -eq "SUCCESS") { $color = "Green" }
    elseif ($status -eq "FAILED") { $color = "Red" }
    elseif ($status -eq "ERROR") { $color = "Yellow" }
    
    Write-Host "$($s.PadRight(25)) : $status" -ForegroundColor $color
}

Write-Host "`nTermine." -ForegroundColor Green
