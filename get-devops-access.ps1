# ================================================================
# get-devops-access.ps1
# ================================================================

function Write-Info([string]$msg) { Write-Host "[INFO] $msg" -ForegroundColor Cyan }
function Write-Ok([string]$msg)   { Write-Host "[OK]   $msg" -ForegroundColor Green }
function Write-Warn([string]$msg) { Write-Host "[WARN] $msg" -ForegroundColor Yellow }

Write-Host "`n========================================" -ForegroundColor Magenta
Write-Host "   DEVOPS ACCESS DASHBOARD RETRIEVER   " -ForegroundColor Magenta
Write-Host "========================================`n" -ForegroundColor Magenta

# ----------------------------------------------------------------
# 1. JENKINS
# ----------------------------------------------------------------
Write-Info "Jenkins password..."
$jenkinsPwd = ""
try {
    $jenkinsPwd = docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword 2>$null
} catch {}

if (-not $jenkinsPwd) {
    $jenkinsPwd = "NOT READY - run: docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword"
}

# ----------------------------------------------------------------
# 2. SONARQUBE
# ----------------------------------------------------------------
Write-Info "SonarQube status..."
$sonarStatus = "DOWN"
try {
    $r = Invoke-WebRequest -Uri http://localhost:9005 -UseBasicParsing -ErrorAction Stop -TimeoutSec 5
    if ($r.StatusCode -eq 200) { $sonarStatus = "UP" }
} catch {}

if ($sonarStatus -eq "DOWN") {
    Write-Warn "SonarQube is DOWN - attempting to restart container..."
    $sonarExists = docker ps -a --filter "name=^/sonarqube$" --format "{{.Names}}"
    if ($sonarExists -eq "sonarqube") {
        docker start sonarqube | Out-Null
    } else {
        docker run -d `
          --name sonarqube `
          -p 9005:9000 `
          -e SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true `
          -v "C:\sonarqube_data:/opt/sonarqube/data" `
          sonarqube:latest | Out-Null
    }
    Write-Info "SonarQube starting... wait 60s"
    Start-Sleep -Seconds 60
    $sonarStatus = "STARTING - check http://localhost:9005 in 1 min"
}

# ----------------------------------------------------------------
# 3. ARGO CD
# ----------------------------------------------------------------
Write-Info "ArgoCD password..."
$argoPwd = ""
try {
    $b64 = kubectl -n argocd get secret argocd-initial-admin-secret `
             -o jsonpath="{.data.password}" 2>$null
    if ($b64) {
        $argoPwd = [System.Text.Encoding]::UTF8.GetString(
                       [System.Convert]::FromBase64String($b64))
    }
} catch {}

if (-not $argoPwd) {
    $argoPwd = "SECRET NOT FOUND - wait 1 min and retry"
}

# Resolution dynamique du service Argo CD
$argoSvc = "argo-cd-argocd-server"
$detectedSvc = kubectl get svc -n argocd --no-headers 2>$null |
               Where-Object { $_ -match "argocd-server" } |
               ForEach-Object { ($_ -split '\s+')[0] } |
               Select-Object -First 1
if ($detectedSvc) { $argoSvc = $detectedSvc }

# Port-forward ArgoCD
Write-Info "Starting port-forward ArgoCD 8888->443 on svc/$argoSvc..."
$argoJob = Start-Job -ScriptBlock {
    param($svcName)
    kubectl port-forward svc/$svcName -n argocd 8888:443 2>$null
} -ArgumentList $argoSvc
Start-Sleep -Seconds 4

# ----------------------------------------------------------------
# 4. MINIKUBE DASHBOARD
# ----------------------------------------------------------------
Write-Info "Enabling Minikube dashboard..."
minikube addons enable dashboard 2>$null | Out-Null
minikube addons enable metrics-server 2>$null | Out-Null

$dashJob = Start-Job -ScriptBlock {
    minikube dashboard --url 2>$null
}
Start-Sleep -Seconds 8
$dashUrl = Receive-Job $dashJob | Select-Object -Last 1
if (-not $dashUrl) {
    $dashUrl = "http://127.0.0.1:43185/api/v1/namespaces/kubernetes-dashboard/services/http:kubernetes-dashboard:/proxy/"
}

# ----------------------------------------------------------------
# 5. AFFICHAGE FINAL
# ----------------------------------------------------------------
Write-Host "`n"
Write-Host "================================================================" -ForegroundColor Magenta
Write-Host "                  DEVOPS DASHBOARD ACCESS                      " -ForegroundColor Magenta
Write-Host "================================================================" -ForegroundColor Magenta

Write-Host ""
Write-Host " JENKINS" -ForegroundColor Green
Write-Host " URL      : http://localhost:8090" -ForegroundColor Cyan
Write-Host " Login    : admin" -ForegroundColor White
Write-Host " Password : $jenkinsPwd" -ForegroundColor Yellow

Write-Host ""
Write-Host " SONARQUBE [$sonarStatus]" -ForegroundColor Green
Write-Host " URL      : http://localhost:9005" -ForegroundColor Cyan
Write-Host " Login    : admin" -ForegroundColor White
Write-Host " Password : admin" -ForegroundColor Yellow

Write-Host ""
Write-Host " ARGO CD" -ForegroundColor Green
Write-Host " URL      : https://localhost:8888" -ForegroundColor Cyan
Write-Host " Login    : admin" -ForegroundColor White
Write-Host " Password : $argoPwd" -ForegroundColor Yellow

Write-Host ""
Write-Host " MINIKUBE DASHBOARD" -ForegroundColor Green
Write-Host " URL      : $dashUrl" -ForegroundColor Cyan
Write-Host " Login    : none (direct access)" -ForegroundColor White

Write-Host ""
Write-Host "================================================================" -ForegroundColor Magenta

# ----------------------------------------------------------------
# 6. OPEN BROWSERS
# ----------------------------------------------------------------
Write-Info "Opening dashboards in browser..."
Start-Sleep -Seconds 2

Start-Process "http://localhost:8090"
Start-Sleep -Seconds 1
Start-Process "http://localhost:9005"
Start-Sleep -Seconds 1
Start-Process "https://localhost:8888"
Start-Sleep -Seconds 1
if ($dashUrl) { Start-Process $dashUrl }

# ----------------------------------------------------------------
# 7. DEBUG COMMANDS
# ----------------------------------------------------------------
Write-Host ""
Write-Host "--- DEBUG COMMANDS ---" -ForegroundColor Yellow

Write-Host ""
Write-Host "Jenkins password:" -ForegroundColor Yellow
Write-Host "  docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword" -ForegroundColor Gray

Write-Host ""
Write-Host "ArgoCD password:" -ForegroundColor Yellow
Write-Host '  kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | ForEach-Object { [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($_)) }' -ForegroundColor Gray

Write-Host ""
Write-Host "Pods status:" -ForegroundColor Yellow
Write-Host "  kubectl get pods -n medicab" -ForegroundColor Gray

Write-Host ""
Write-Host "Pod logs:" -ForegroundColor Yellow
Write-Host "  kubectl logs <pod-name> -n medicab" -ForegroundColor Gray

Write-Host ""
Write-Host "[INFO] ArgoCD port-forward running (Job ID: $($argoJob.Id))" -ForegroundColor Cyan
Write-Host "[INFO] To stop: Stop-Job $($argoJob.Id); Remove-Job $($argoJob.Id)" -ForegroundColor Cyan