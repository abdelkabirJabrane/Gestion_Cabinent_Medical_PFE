$ErrorActionPreference = 'Stop'

$baseUrl = "http://localhost:8090"
$authPair = "admin:b5c5133091994310b0d9c850032f34af"
$encodedAuth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes($authPair))
$headers = @{
    "Authorization" = "Basic $encodedAuth"
}

Write-Host "Etape 1 - Recupere le CSRF crumb"
$crumbResponse = Invoke-RestMethod -Uri "$baseUrl/crumbIssuer/api/json" -Headers $headers -Method Get
$crumbField = $crumbResponse.crumbRequestField
$crumbValue = $crumbResponse.crumb
$headers.Add($crumbField, $crumbValue)
Write-Host "Crumb recupere: $crumbValue"
Write-Host ""

Write-Host "Etape 2 - Cree le job via le fichier XML"
$xmlPath = "c:\Users\ajabrane2\IdeaProjects\microservice\Gestion_Cabinent_Medical_PFE\medicab-pipeline.xml"
$xmlContent = Get-Content $xmlPath -Raw

$createJobHeaders = $headers.Clone()
$createJobHeaders["Content-Type"] = "application/xml"

$createResponse = Invoke-WebRequest -Uri "$baseUrl/createItem?name=medicab-pipeline" -Method Post -Headers $createJobHeaders -Body $xmlContent -UseBasicParsing
Write-Host "Status creation job: $($createResponse.StatusCode)"
Write-Host ""

Write-Host "Etape 3 - Verifie que le job existe"
$verifyResponse = Invoke-RestMethod -Uri "$baseUrl/job/medicab-pipeline/api/json" -Method Get -Headers $headers
Write-Host "Nom du job trouve: $($verifyResponse.name)"
Write-Host ""

Write-Host "Etape 4 - Declenche un premier build"
$buildResponse = Invoke-WebRequest -Uri "$baseUrl/job/medicab-pipeline/build" -Method Post -Headers $headers -UseBasicParsing
Write-Host "Status declenchement build: $($buildResponse.StatusCode)"

# Get the build queue to find the build number (optional, but requested to confirm build number)
Start-Sleep -Seconds 2
$jobInfo = Invoke-RestMethod -Uri "$baseUrl/job/medicab-pipeline/api/json" -Method Get -Headers $headers
$nextBuild = $jobInfo.nextBuildNumber
$currentBuild = $nextBuild - 1
Write-Host "Build declenche avec succes ! (Numero estime: $currentBuild)"
