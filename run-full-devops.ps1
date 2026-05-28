#================================================================
# run-full-devops.ps1  -  Version 2.2 (corrigee et GitOps-ready)
# Lance Docker Desktop, Minikube, Jenkins, SonarQube et Argo CD
# proprement apres un redemarrage PC.
#================================================================

Set-StrictMode -Off
$ErrorActionPreference = "Continue"

# ----------------------------------------------------------------
# 0. Helpers
# ----------------------------------------------------------------
function Write-Info([string]$msg)  { Write-Host "[INFO]  $msg" -ForegroundColor Cyan }
function Write-Ok([string]$msg)    { Write-Host "[ OK ]  $msg" -ForegroundColor Green }
function Write-Warn([string]$msg)  { Write-Host "[WARN]  $msg" -ForegroundColor Yellow }
function Write-Err([string]$msg)   { Write-Host "[ERROR] $msg" -ForegroundColor Red }

function Wait-Until {
    param(
        [scriptblock]$Condition,
        [int]$TimeoutSec = 120,
        [int]$IntervalSec = 5,
        [string]$Label = "condition"
    )
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    Write-Info "Attente : $Label (max ${TimeoutSec}s)..."
    while ((Get-Date) -lt $deadline) {
        if (& $Condition) { Write-Ok "$Label OK"; return $true }
        Start-Sleep -Seconds $IntervalSec
    }
    Write-Warn "Timeout atteint pour : $Label"
    return $false
}

# ----------------------------------------------------------------
# 1. Docker Desktop
# ----------------------------------------------------------------
Write-Info "Demarrage de Docker Desktop..."
$dockerExe = "C:\Program Files\Docker\Docker\Docker Desktop.exe"
if (Test-Path $dockerExe) {
    Start-Process $dockerExe -ErrorAction SilentlyContinue
}

Wait-Until -Label "Docker Engine" -TimeoutSec 120 -Condition {
    $out = docker info 2>&1
    $LASTEXITCODE -eq 0
} | Out-Null

Write-Ok "Docker est pret."

# ----------------------------------------------------------------
# 2. Verification de l'etat de Minikube
# ----------------------------------------------------------------
Write-Info "Verification de l'etat de Minikube..."
$mkStatus = minikube status --format '{{.Host}}' 2>$null

# ----------------------------------------------------------------
# 3. Demarrage Minikube (sans destruction)
# ----------------------------------------------------------------
if ($mkStatus -eq "Running") {
    Write-Ok "Minikube est deja en cours d'execution."
} else {
    Write-Info "Demarrage de Minikube..."
    minikube start `
        --driver=docker `
        --memory=6144 `
        --cpus=2 `
        --preload=false `
        --addons=dashboard,metrics-server

    if ($LASTEXITCODE -ne 0) {
        Write-Err "Minikube n'a pas demarre. Verifiez que Docker Desktop tourne et que vous avez 6 GB RAM libres."
        exit 1
    }
}

Wait-Until -Label "API Server Kubernetes" -TimeoutSec 60 -Condition {
    $nodes = kubectl get nodes 2>&1
    $LASTEXITCODE -eq 0
} | Out-Null

Write-Ok "Minikube et kubectl sont operationnels."

# ----------------------------------------------------------------
# 3.1. Correction RBAC PVC Minikube
# ----------------------------------------------------------------
Write-Info "Application du correctif RBAC PVC pour Minikube..."
$bindingExists = kubectl get clusterrolebinding minikube-node-pvc-admin --ignore-not-found 2>$null
if (-not $bindingExists) {
    kubectl create clusterrolebinding minikube-node-pvc-admin --clusterrole=cluster-admin --user=system:node:minikube 2>$null | Out-Null
    Write-Ok "ClusterRoleBinding pour system:node:minikube cree avec succes."
} else {
    Write-Ok "ClusterRoleBinding pour system:node:minikube existe deja."
}

# ----------------------------------------------------------------
# 4. Nettoyage conteneurs Docker precedents (supprime)
# ----------------------------------------------------------------
# Les conteneurs jenkins et sonarqube ne sont plus supprimes pour preserver leurs donnees.

# ----------------------------------------------------------------
# 5. Namespaces K8s + manifests
# ----------------------------------------------------------------
$projRoot = $PSScriptRoot
if (-not $projRoot) { $projRoot = (Get-Location).Path }

$secrets    = Join-Path $projRoot "k8s\secrets.yml"
$infra      = Join-Path $projRoot "k8s\infrastructure.yml"
$argocdApps = Join-Path $projRoot "k8s\argocd"

if (Test-Path $secrets) {
    Write-Info "Application du manifest secrets..."
    kubectl apply -f $secrets
} else {
    Write-Warn "k8s\secrets.yml introuvable, etape ignoree."
}

if (Test-Path $infra) {
    Write-Info "Application du manifest infrastructure..."
    kubectl apply -f $infra
} else {
    Write-Warn "k8s\infrastructure.yml introuvable, etape ignoree."
}

if (Test-Path $argocdApps) {
    Write-Info "Application des applications Argo CD (GitOps)..."
    kubectl apply -f $argocdApps
} else {
    Write-Warn "Dossier k8s\argocd introuvable, initialisation GitOps ignoree."
}

# ----------------------------------------------------------------
# 6. Attente des pods medicab
# ----------------------------------------------------------------
$nsExists = kubectl get namespace medicab 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Info "Attente des pods medicab (max 3 min)..."
    $deadline = (Get-Date).AddMinutes(3)
    do {
        Start-Sleep -Seconds 8
        $notReady = kubectl get pods -n medicab --no-headers 2>$null |
                    Where-Object { $_ -notmatch "Running|Completed" }
    } while ($notReady -and (Get-Date) -lt $deadline)

    if ($notReady) {
        Write-Warn "Certains pods medicab ne sont pas encore Running :"
        $notReady | ForEach-Object { Write-Host "  $_" -ForegroundColor Yellow }
    } else {
        Write-Ok "Tous les pods medicab sont Running."
    }
    kubectl get pods -n medicab
}

# ----------------------------------------------------------------
# 7. Jenkins (port 8090)
# ----------------------------------------------------------------
$jenkinsExists = docker ps -a --filter "name=^/jenkins$" --format "{{.Names}}"
if ($jenkinsExists -eq "jenkins") {
    $jenkinsRunning = docker ps --filter "name=^/jenkins$" --format "{{.Names}}"
    if ($jenkinsRunning -eq "jenkins") {
        Write-Ok "Jenkins est deja en cours d'execution."
    } else {
        Write-Info "Demarrage du conteneur Jenkins existant..."
        docker start jenkins | Out-Null
    }
} else {
    Write-Info "Lancement d'un nouveau conteneur Jenkins sur le port 8090..."
    $jenkinsHome = "C:\jenkins_home"
    New-Item -ItemType Directory -Force $jenkinsHome | Out-Null

    docker run -d `
        --name jenkins `
        --restart unless-stopped `
        -p 8090:8080 -p 50000:50000 `
        -v "${jenkinsHome}:/var/jenkins_home" `
        -v "//var/run/docker.sock:/var/run/docker.sock" `
        jenkins/jenkins:lts | Out-Null
}

Wait-Until -Label "Jenkins HTTP" -TimeoutSec 120 -IntervalSec 8 -Condition {
    try {
        $r = Invoke-WebRequest -Uri "http://localhost:8090" -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
        $r.StatusCode -in @(200, 403)
    } catch { $false }
} | Out-Null

$jenkinsPwd = ""
$pwdFile = "$jenkinsHome\secrets\initialAdminPassword"
if (Test-Path $pwdFile) {
    $jenkinsPwd = (Get-Content $pwdFile -Raw).Trim()
} else {
    $jenkinsPwd = docker logs jenkins 2>&1 |
                  Select-String -Pattern "^\s*[a-f0-9]{32}\s*$" |
                  Select-Object -Last 1 |
                  ForEach-Object { $_.Line.Trim() }
}
if (-not $jenkinsPwd) {
    $jenkinsPwd = "Deja configure (consultez vos identifiants Jenkins habituels)"
}
Write-Ok "Jenkins disponible -> http://localhost:8090  (mot de passe : $jenkinsPwd)"

# ----------------------------------------------------------------
# 8. SonarQube (port 9005)
# ----------------------------------------------------------------
$sonarExists = docker ps -a --filter "name=^/sonarqube$" --format "{{.Names}}"
if ($sonarExists -eq "sonarqube") {
    $sonarRunning = docker ps --filter "name=^/sonarqube$" --format "{{.Names}}"
    if ($sonarRunning -eq "sonarqube") {
        Write-Ok "SonarQube est deja en cours d'execution."
    } else {
        Write-Info "Demarrage du conteneur SonarQube existant..."
        docker start sonarqube | Out-Null
    }
} else {
    Write-Info "Lancement d'un nouveau conteneur SonarQube sur le port 9005..."
    $sonarData = "C:\sonarqube_data"
    New-Item -ItemType Directory -Force $sonarData | Out-Null

    docker run -d `
        --name sonarqube `
        --restart unless-stopped `
        -p 9005:9000 `
        -e SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true `
        -e SONAR_SEARCH_JAVAADDITIONALOPTS="-Xmx512m -Xms512m" `
        -v "${sonarData}:/opt/sonarqube/data" `
        sonarqube:latest | Out-Null
}

Wait-Until -Label "SonarQube HTTP" -TimeoutSec 180 -IntervalSec 10 -Condition {
    try {
        $r = Invoke-WebRequest -Uri "http://localhost:9005" -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
        $r.StatusCode -eq 200
    } catch { $false }
} | Out-Null

Write-Ok "SonarQube disponible -> http://localhost:9005  (admin / admin)"

# ----------------------------------------------------------------
# 9. Argo CD via Helm
# ----------------------------------------------------------------
$argoRunning = $false
try {
    $phase = kubectl -n argocd get pod -l "app.kubernetes.io/name=argocd-server" `
             -o jsonpath="{.items[0].status.phase}" 2>$null
    if ($phase -eq "Running") { $argoRunning = $true }
} catch {}

if ($argoRunning) {
    Write-Ok "Argo CD est deja installe et en cours d'execution."
} else {
    Write-Info "Installation d'Argo CD via Helm..."
    helm repo add argo https://argoproj.github.io/argo-helm 2>$null | Out-Null
    helm repo update 2>$null | Out-Null

    helm upgrade --install argo-cd argo/argo-cd `
        --namespace argocd --create-namespace `
        --set server.service.type=NodePort `
        --set "server.service.nodePorts.http=30081" `
        --timeout 300s `
        --wait

    if ($LASTEXITCODE -ne 0) {
        Write-Warn "Helm a rencontre un probleme. Nouvelle tentative sans --wait..."
        helm upgrade --install argo-cd argo/argo-cd `
            --namespace argocd --create-namespace `
            --set server.service.type=NodePort `
            --set "server.service.nodePorts.http=30081" `
            --timeout 300s
    }
}

Wait-Until -Label "Argo CD server pod" -TimeoutSec 300 -IntervalSec 10 -Condition {
    $phase = kubectl -n argocd get pod -l "app.kubernetes.io/name=argocd-server" `
             -o jsonpath="{.items[0].status.phase}" 2>$null
    $phase -eq "Running"
} | Out-Null

$argoPwd = "Non disponible"
$secretCheck = kubectl -n argocd get secret argocd-initial-admin-secret --ignore-not-found 2>$null
if ($secretCheck -match "argocd-initial-admin-secret") {
    $b64 = kubectl -n argocd get secret argocd-initial-admin-secret `
           -o jsonpath="{.data.password}" 2>$null
    if ($b64) {
        $argoPwd = [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($b64))
    }
}

# Resolution dynamique du service Argo CD et conversion en NodePort si necessaire
$argoSvc = "argo-cd-argocd-server"
$detectedSvc = kubectl get svc -n argocd --no-headers 2>$null |
               Where-Object { $_ -match "argocd-server" } |
               ForEach-Object { ($_ -split '\s+')[0] } |
               Select-Object -First 1
if ($detectedSvc) { $argoSvc = $detectedSvc }

Write-Info "Configuration du service Argo CD ($argoSvc) en NodePort sur le port 30081..."
# Patch precise pour spec.type et spec.ports[http].nodePort
kubectl patch svc $argoSvc -n argocd --type='json' -p='[{"op": "replace", "path": "/spec/type", "value": "NodePort"}]' 2>$null | Out-Null
kubectl patch svc $argoSvc -n argocd -p '{"spec": {"ports": [{"name": "http", "port": 80, "nodePort": 30081}]}}' 2>$null | Out-Null

$minikubeIp = ""
try {
    $minikubeIp = (minikube ip 2>$null).Trim()
} catch {}
if (-not $minikubeIp) {
    $minikubeIp = "localhost"
}
$argoCdUrl = "http://${minikubeIp}:30081"

Write-Ok "Argo CD disponible -> $argoCdUrl  (admin / $argoPwd)"

# ----------------------------------------------------------------
# 10. Dashboard Minikube (non bloquant)
# ----------------------------------------------------------------
Write-Info "Ouverture du dashboard Minikube en arriere-plan..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "minikube dashboard" -WindowStyle Minimized
Start-Sleep -Seconds 5

# ----------------------------------------------------------------
# 11. Ouverture automatique des navigateurs
# ----------------------------------------------------------------
Write-Info "Ouverture des dashboards dans le navigateur..."
Start-Process "http://localhost:8090"
Start-Sleep -Seconds 1
Start-Process "http://localhost:9005"
Start-Sleep -Seconds 1
Start-Process $argoCdUrl

# ----------------------------------------------------------------
# 12. Resume final
# ----------------------------------------------------------------
Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host "              DEVOPS STACK PRET !                           " -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green
Write-Host "  Jenkins    -> http://localhost:8090" -ForegroundColor Cyan
Write-Host "              mot de passe : $jenkinsPwd" -ForegroundColor White
Write-Host "  SonarQube  -> http://localhost:9005  (admin / admin)" -ForegroundColor Cyan
Write-Host "  Argo CD    -> $argoCdUrl" -ForegroundColor Cyan
Write-Host "              mot de passe : $argoPwd" -ForegroundColor White
Write-Host "  Minikube   -> fenetre PowerShell minimisee" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Green
Write-Host "  Pour tout arreter :" -ForegroundColor Yellow
Write-Host "    docker rm -f jenkins sonarqube" -ForegroundColor Yellow
Write-Host "    minikube delete" -ForegroundColor Yellow
Write-Host "============================================================" -ForegroundColor Green