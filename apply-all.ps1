# ================================================================
# apply-all.ps1  -  Script de déploiement et validation DevOps
# Applique tous les manifests Kubernetes et lance la configuration Jenkins
# ================================================================

$ErrorActionPreference = 'Stop'

function Write-Info([string]$msg) { Write-Host "[INFO]  $msg" -ForegroundColor Cyan }
function Write-Ok([string]$msg)   { Write-Host "[ OK ]  $msg" -ForegroundColor Green }
function Write-Warn([string]$msg) { Write-Host "[WARN]  $msg" -ForegroundColor Yellow }
function Write-Err([string]$msg)  { Write-Host "[ERROR] $msg" -ForegroundColor Red }

$projRoot = $PSScriptRoot
if (-not $projRoot) { $projRoot = (Get-Location).Path }

# 1. Application des Secrets
Write-Info "1. Application des secrets..."
$secretsPath = Join-Path $projRoot "k8s\secrets.yml"
if (Test-Path $secretsPath) {
    kubectl apply -f $secretsPath --validate=false
    Write-Ok "Secrets K8s appliques avec succes."
} else {
    Write-Err "Secrets K8s introuvables a : $secretsPath"
    exit 1
}

# 2. Application de l'infrastructure globale
Write-Info "2. Application de l'infrastructure globale (PostgreSQL, Redis, RabbitMQ)..."
$infraPath = Join-Path $projRoot "k8s\infrastructure.yml"
if (Test-Path $infraPath) {
    kubectl apply -f $infraPath --validate=false
    Write-Ok "Infrastructure appliquee avec succes."
} else {
    Write-Err "Infrastructure introuvable a : $infraPath"
    exit 1
}

# 3. Application des déploiements individuels des microservices
Write-Info "3. Application des deploiements individuels (10 microservices)..."
$servicesList = @(
    "config-service",
    "discovery-service",
    "api-gateway",
    "auth-service",
    "patient-service",
    "appointment-service",
    "billing-service",
    "medical-record-service",
    "ordonnance-service",
    "ai-service"
)

foreach ($svc in $servicesList) {
    $svcPath = Join-Path $projRoot "k8s\$svc\deployment.yml"
    if (Test-Path $svcPath) {
        Write-Info "Application du manifest pour $svc..."
        kubectl apply -f $svcPath --validate=false
    } else {
        Write-Warn "Manifest $svcPath introuvable, etape ignoree."
    }
}
Write-Ok "Tous les manifests de deploiement individuels ont ete appliques."

# 4. Application des applications Argo CD (GitOps)
Write-Info "4. Application des applications Argo CD (GitOps)..."
$argocdPath = Join-Path $projRoot "k8s\argocd"
if (Test-Path $argocdPath) {
    kubectl apply -f $argocdPath --validate=false
    Write-Ok "Applications Argo CD appliquees avec succes."
} else {
    Write-Warn "Dossier $argocdPath introuvable, etape ignoree."
}

# 5. Application du Monitoring (Prometheus + Grafana)
Write-Info "5. Application du Monitoring (Prometheus + Grafana)..."
$monitoringPath = Join-Path $projRoot "k8s\monitoring"
if (Test-Path $monitoringPath) {
    # Apply namespace first to avoid alphabetical ordering race condition
    kubectl apply -f "$monitoringPath\namespace.yml" --validate=false
    kubectl apply -f "$monitoringPath\prometheus-config.yml" --validate=false
    kubectl apply -f "$monitoringPath\prometheus-deployment.yml" --validate=false
    kubectl apply -f "$monitoringPath\grafana-datasource.yml" --validate=false
    kubectl apply -f "$monitoringPath\grafana-dashboard-configmap.yml" --validate=false
    kubectl apply -f "$monitoringPath\grafana-deployment.yml" --validate=false
    Write-Ok "Manifests de monitoring appliques avec succes."
} else {
    Write-Err "Dossier $monitoringPath introuvable."
    exit 1
}

$monitoringAppPath = Join-Path $projRoot "k8s\argocd\monitoring-app.yml"
if (Test-Path $monitoringAppPath) {
    kubectl apply -f $monitoringAppPath --validate=false
    Write-Ok "Application Argo CD Monitoring appliquee avec succes."
}

Write-Info "Attente du deploiement complet de Prometheus et Grafana..."
try {
    kubectl rollout status deployment/prometheus -n monitoring --timeout=120s
    kubectl rollout status deployment/grafana -n monitoring --timeout=120s
    Write-Ok "Prometheus et Grafana sont prets et fonctionnels !"
} catch {
    Write-Warn "Delai d'attente depasse ou erreur lors de l'attente du deploiement du monitoring."
}

# 6. Lancement du script de configuration Jenkins
Write-Info "6. Execution du script de configuration Jenkins (fixed-jenkins-script.ps1)..."
$jenkinsScript = Join-Path $projRoot "fixed-jenkins-script.ps1"
if (Test-Path $jenkinsScript) {
    & $jenkinsScript
    Write-Ok "Script Jenkins execute avec succes."
} else {
    Write-Warn "Script Jenkins $jenkinsScript introuvable, etape ignoree."
}

# 7. Verification de l'etat final
Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host "           VERIFICATION DE L'ETAT FINAL                     " -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green

Write-Info "Checking pods in namespace 'medicab'..."
kubectl get pods -n medicab

Write-Info "Checking pods in namespace 'monitoring'..."
kubectl get pods -n monitoring

Write-Info "Checking applications in namespace 'argocd'..."
kubectl get applications -n argocd 2>$null

# Resolution de l'IP Minikube
$minikubeIp = "localhost"
try {
    $minikubeIp = (minikube ip).Trim()
} catch {
    Write-Warn "Impossible de recuperer l'IP de Minikube, utilisation de localhost."
}

Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host "           MONITORING ACCESS CHANNELS                       " -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green
Write-Host " Prometheus : http://$($minikubeIp):30090" -ForegroundColor Cyan
Write-Host " Grafana    : http://$($minikubeIp):30030 (admin / admin)" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Green
