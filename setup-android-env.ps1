# Script pour configurer les variables d'environnement Android
# Usage: .\setup-android-env.ps1

Write-Host "Configuration des variables d'environnement Android..." -ForegroundColor Green

# Chemin typique pour Android SDK
$androidSdkPath = "C:\Users\Quentin\AppData\Local\Android\Sdk"

# Vérifier si le chemin existe
if (Test-Path $androidSdkPath) {
    Write-Host "Android SDK trouvé: $androidSdkPath" -ForegroundColor Green
} else {
    Write-Host "Android SDK non trouvé dans le chemin par défaut" -ForegroundColor Yellow
    Write-Host "Recherche d'autres emplacements possibles..." -ForegroundColor Yellow
    
    # Autres chemins possibles
    $possiblePaths = @(
        "C:\Android\Sdk",
        "C:\Program Files\Android\Sdk",
        "C:\Users\Quentin\Android\Sdk"
    )
    
    foreach ($path in $possiblePaths) {
        if (Test-Path $path) {
            $androidSdkPath = $path
            Write-Host "Android SDK trouvé: $androidSdkPath" -ForegroundColor Green
            break
        }
    }
}

# Configuration temporaire (pour cette session)
Write-Host "`nConfiguration temporaire (session actuelle)..." -ForegroundColor Cyan
$env:ANDROID_HOME = $androidSdkPath
$env:ANDROID_SDK_ROOT = $androidSdkPath
$env:PATH += ";$androidSdkPath\platform-tools;$androidSdkPath\emulator"

Write-Host "ANDROID_HOME = $env:ANDROID_HOME" -ForegroundColor White
Write-Host "ANDROID_SDK_ROOT = $env:ANDROID_SDK_ROOT" -ForegroundColor White

# Test des commandes
Write-Host "`nTest des commandes Android..." -ForegroundColor Cyan
try {
    $adbVersion = adb version 2>&1
    Write-Host "ADB fonctionne: $($adbVersion[0])" -ForegroundColor Green
} catch {
    Write-Host "ADB non trouvé ou ne fonctionne pas" -ForegroundColor Red
}

# Configuration permanente (optionnelle)
Write-Host "`nVoulez-vous configurer les variables de façon permanente ? (O/N)" -ForegroundColor Yellow
$response = Read-Host

if ($response -eq "O" -or $response -eq "o" -or $response -eq "Y" -or $response -eq "y") {
    Write-Host "Configuration permanente..." -ForegroundColor Cyan
    
    # Ajouter ANDROID_HOME
    [Environment]::SetEnvironmentVariable("ANDROID_HOME", $androidSdkPath, "User")
    
    # Ajouter ANDROID_SDK_ROOT
    [Environment]::SetEnvironmentVariable("ANDROID_SDK_ROOT", $androidSdkPath, "User")
    
    # Ajouter au PATH
    $currentPath = [Environment]::GetEnvironmentVariable("PATH", "User")
    $newPath = "$androidSdkPath\platform-tools;$androidSdkPath\emulator;$currentPath"
    [Environment]::SetEnvironmentVariable("PATH", $newPath, "User")
    
    Write-Host "Variables d'environnement configurées de façon permanente!" -ForegroundColor Green
    Write-Host "Redémarrez PowerShell pour que les changements prennent effet." -ForegroundColor Yellow
} else {
    Write-Host "Configuration temporaire uniquement." -ForegroundColor Yellow
    Write-Host "Les variables seront perdues à la fermeture de PowerShell." -ForegroundColor Yellow
}

Write-Host "`nConfiguration terminée!" -ForegroundColor Green
Write-Host "Vous pouvez maintenant utiliser: . .\android-dev-commands.ps1" -ForegroundColor Cyan 