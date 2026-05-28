$ErrorActionPreference = 'Stop'

$jenkinsUrl = "http://localhost:8090"
$user = "AbdoJab"
$token = "11fd4070db4ce1ff5895f7a55556e6c186"
$jobName = "medicab-pipeline"

# Fix de la variable d'authentification pour eviter l'erreur PowerShell ":"
$authPair = $user + ":" + $token
$encodedAuth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes($authPair))
$headers = @{ "Authorization" = "Basic $encodedAuth" }

# Gestion de session (pour les cookies Jenkins + CSRF)
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession

Write-Host "Etape 1 - Recupere le CSRF crumb..." -ForegroundColor Cyan
$crumbResponse = Invoke-RestMethod -Uri "$jenkinsUrl/crumbIssuer/api/json" -Headers $headers -Method Get -WebSession $session
$crumbField = $crumbResponse.crumbRequestField
$crumbValue = $crumbResponse.crumb
$headers.Add($crumbField, $crumbValue)
Write-Host "[OK] Crumb recupere: $crumbValue" -ForegroundColor Green

# ----------------------------------------------------------------
# Etape 1.1 - Configuration de la variable globale SONAR_HOST_URL
# ----------------------------------------------------------------
Write-Host "Etape 1.1 - Configuration de la variable globale SONAR_HOST_URL..." -ForegroundColor Cyan
$groovyScript = @"
import hudson.slaves.EnvironmentVariablesNodeProperty
import jenkins.model.Jenkins
def jenkins = Jenkins.getInstance()
def globalNodeProperties = jenkins.getGlobalNodeProperties()
def envVarsNodePropertyList = globalNodeProperties.getAll(EnvironmentVariablesNodeProperty.class)
def envVars
if (envVarsNodePropertyList == null || envVarsNodePropertyList.size() == 0) {
    def newProperty = new EnvironmentVariablesNodeProperty()
    globalNodeProperties.add(newProperty)
    envVars = newProperty.getEnvVars()
} else {
    envVars = envVarsNodePropertyList.get(0).getEnvVars()
}
envVars.put("SONAR_HOST_URL", "http://host.docker.internal:9005")
jenkins.save()
"@

$scriptHeaders = $headers.Clone()
$scriptHeaders["Content-Type"] = "application/x-www-form-urlencoded"
$body = "script=" + [Uri]::EscapeDataString($groovyScript)

$scriptResponse = Invoke-WebRequest -Uri "$jenkinsUrl/scriptText" -Method Post -Headers $scriptHeaders -Body $body -UseBasicParsing -WebSession $session
if ($scriptResponse.StatusCode -eq 200) {
    Write-Host "[OK] Variable globale SONAR_HOST_URL configuree." -ForegroundColor Green
} else {
    Write-Warning "Echec de la configuration de la variable globale."
}

# ----------------------------------------------------------------
# Etape 2 - Creation/Mise a jour du job orchestrateur principal
# ----------------------------------------------------------------
Write-Host "Etape 2 - Creation du job principal $jobName via XML..." -ForegroundColor Cyan
$xmlBody = @"
<?xml version='1.1' encoding='UTF-8'?>
<flow-definition plugin="workflow-job">
  <description>Pipeline CI/CD Gestion Cabinet Medical (Orchestrateur GitOps)</description>
  <keepDependencies>false</keepDependencies>
  <properties/>
  <definition class="org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition" plugin="workflow-cps">
    <scm class="hudson.plugins.git.GitSCM" plugin="git">
      <configVersion>2</configVersion>
      <userRemoteConfigs>
        <hudson.plugins.git.UserRemoteConfig>
          <url>https://github.com/abdelkabirJabrane/Gestion_Cabinent_Medical_PFE.git</url>
          <credentialsId>github-creds</credentialsId>
        </hudson.plugins.git.UserRemoteConfig>
      </userRemoteConfigs>
      <branches>
        <hudson.plugins.git.BranchSpec>
          <name>*/master</name>
        </hudson.plugins.git.BranchSpec>
      </branches>
      <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
      <submoduleCfg class="empty-list"/>
      <extensions/>
    </scm>
    <scriptPath>Jenkinsfile</scriptPath>
    <lightweight>true</lightweight>
  </definition>
  <triggers/>
  <disabled>false</disabled>
</flow-definition>
"@

$createHeaders = $headers.Clone()
$createHeaders["Content-Type"] = "application/xml"

$mainJobExists = $false
try {
    $checkMain = Invoke-WebRequest -Uri "$jenkinsUrl/job/$jobName/config.xml" -Method Get -Headers $headers -UseBasicParsing -WebSession $session
    if ($checkMain.StatusCode -eq 200) { $mainJobExists = $true }
} catch {}

if ($mainJobExists) {
    $createResponse = Invoke-WebRequest -Uri "$jenkinsUrl/job/$jobName/config.xml" -Method Post -Headers $createHeaders -Body $xmlBody -UseBasicParsing -WebSession $session
    Write-Host "[OK] Status mise a jour ${jobName}: $($createResponse.StatusCode)" -ForegroundColor Green
} else {
    $createResponse = Invoke-WebRequest -Uri "$jenkinsUrl/createItem?name=$jobName" -Method Post -Headers $createHeaders -Body $xmlBody -UseBasicParsing -WebSession $session
    Write-Host "[OK] Status creation ${jobName}: $($createResponse.StatusCode)" -ForegroundColor Green
}

# ----------------------------------------------------------------
# Etape 2.1 - Creation/Mise a jour automatique des 10 jobs enfants
# ----------------------------------------------------------------
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
    $childJobName = "$svc-pipeline"
    Write-Host "Creation/Mise a jour du job enfant $childJobName via XML..." -ForegroundColor Cyan
    $childXmlBody = @"
<?xml version='1.1' encoding='UTF-8'?>
<flow-definition plugin="workflow-job">
  <description>Pipeline CI/CD pour $svc</description>
  <keepDependencies>false</keepDependencies>
  <properties/>
  <definition class="org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition" plugin="workflow-cps">
    <scm class="hudson.plugins.git.GitSCM" plugin="git">
      <configVersion>2</configVersion>
      <userRemoteConfigs>
        <hudson.plugins.git.UserRemoteConfig>
          <url>https://github.com/abdelkabirJabrane/Gestion_Cabinent_Medical_PFE.git</url>
          <credentialsId>github-creds</credentialsId>
        </hudson.plugins.git.UserRemoteConfig>
      </userRemoteConfigs>
      <branches>
        <hudson.plugins.git.BranchSpec>
          <name>*/master</name>
        </hudson.plugins.git.BranchSpec>
      </branches>
      <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
      <submoduleCfg class="empty-list"/>
      <extensions/>
    </scm>
    <scriptPath>$svc/Jenkinsfile</scriptPath>
    <lightweight>true</lightweight>
  </definition>
  <triggers/>
  <disabled>false</disabled>
</flow-definition>
"@

    $createChildHeaders = $headers.Clone()
    $createChildHeaders["Content-Type"] = "application/xml"
    
    $childJobExists = $false
    try {
        $checkChild = Invoke-WebRequest -Uri "$jenkinsUrl/job/$childJobName/config.xml" -Method Get -Headers $headers -UseBasicParsing -WebSession $session
        if ($checkChild.StatusCode -eq 200) { $childJobExists = $true }
    } catch {}

    if ($childJobExists) {
        $createChildResponse = Invoke-WebRequest -Uri "$jenkinsUrl/job/$childJobName/config.xml" -Method Post -Headers $createChildHeaders -Body $childXmlBody -UseBasicParsing -WebSession $session
        Write-Host "[OK] Status mise a jour ${childJobName}: $($createChildResponse.StatusCode)" -ForegroundColor Green
    } else {
        $createChildResponse = Invoke-WebRequest -Uri "$jenkinsUrl/createItem?name=$childJobName" -Method Post -Headers $createChildHeaders -Body $childXmlBody -UseBasicParsing -WebSession $session
        Write-Host "[OK] Status creation ${childJobName}: $($createChildResponse.StatusCode)" -ForegroundColor Green
    }
}

# ----------------------------------------------------------------
# Etape 3 - Verification et declenchement de l'orchestrateur
# ----------------------------------------------------------------
Write-Host "Etape 3 - Verification de l'existence du job orchestrateur..." -ForegroundColor Cyan
$verifyResponse = Invoke-RestMethod -Uri "$jenkinsUrl/job/$jobName/api/json" -Headers $headers -Method Get -WebSession $session
Write-Host "[OK] Job principal trouve: $($verifyResponse.name)" -ForegroundColor Green

Write-Host "Etape 4 - Declenchement du build..." -ForegroundColor Cyan
$buildResponse = Invoke-WebRequest -Uri "$jenkinsUrl/job/$jobName/build" -Method Post -Headers $headers -UseBasicParsing -WebSession $session
Write-Host "[OK] Status declenchement build orchestrateur: $($buildResponse.StatusCode) (Attendu: 201)" -ForegroundColor Green

Start-Sleep -Seconds 2
$jobInfo = Invoke-RestMethod -Uri "$jenkinsUrl/job/$jobName/api/json" -Headers $headers -Method Get -WebSession $session
$nextBuild = $jobInfo.nextBuildNumber
$currentBuild = $nextBuild - 1
Write-Host ">>> SUCCES ! Le build #$currentBuild a ete declenche pour $jobName et configurera les pipelines enfants. <<<" -ForegroundColor Magenta
