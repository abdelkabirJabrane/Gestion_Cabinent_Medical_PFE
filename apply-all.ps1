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
    kubectl apply -f $secretsPath
    Write-Ok "Secrets K8s appliques avec succes."
} else {
    Write-Err "Secrets K8s introuvables a : $secretsPath"
    exit 1
}

# 2. Application de l'infrastructure globale
Write-Info "2. Application de l'infrastructure globale (PostgreSQL, Redis, RabbitMQ)..."
$infraPath = Join-Path $projRoot "k8s\infrastructure.yml"
if (Test-Path $infraPath) {
    kubectl apply -f $infraPath
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
        kubectl apply -f $svcPath
    } else {
        Write-Warn "Manifest $svcPath introuvable, etape ignoree."
    }
}
Write-Ok "Tous les manifests de deploiement individuels ont ete appliques."

# 4. Application des applications Argo CD (GitOps)
Write-Info "4. Application des applications Argo CD (GitOps)..."
$argocdPath = Join-Path $projRoot "k8s\argocd"
if (Test-Path $argocdPath) {
    kubectl apply -f $argocdPath
    Write-Ok "Applications Argo CD appliquees avec succes."
} else {
    Write-Warn "Dossier $argocdPath introuvable, etape ignoree."
}

# 5. Lancement du script de configuration Jenkins
Write-Info "5. Execution du script de configuration Jenkins (fixed-jenkins-script.ps1)..."
$jenkinsScript = Join-Path $projRoot "fixed-jenkins-script.ps1"
if (Test-Path $jenkinsScript) {
    & $jenkinsScript
    Write-Ok "Script Jenkins execute avec succes."
} else {
    Write-Warn "Script Jenkins $jenkinsScript introuvable, etape ignoree."
}

# 6. Verification de l'etat final
Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host "           VERIFICATION DE L'ETAT FINAL                     " -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green

Write-Info "Checking pods in namespace 'medicab'..."
kubectl get pods -n medicab

Write-Info "Checking applications in namespace 'argocd'..."
kubectl get applications -n argocd 2>$null

Write-Host "============================================================" -ForegroundColor Green
