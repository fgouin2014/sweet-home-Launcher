# Script de developpement Android - Sweet Home Launcher
# Usage: . .\android-dev-commands.ps1

# Alias pour les commandes Android
Set-Alias -Name adb -Value "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
Set-Alias -Name emulator -Value "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe"

# Fonction pour lancer l'emulateur
function Start-Emulator {
    param([string]$AvdName = "Medium_Phone_API_36.0")
    Write-Host "Lancement de l'emulateur: $AvdName" -ForegroundColor Green
    emulator -avd $AvdName
}

# Fonction pour compiler le projet
function Build-Project {
    Write-Host "Compilation du projet..." -ForegroundColor Green
    ./gradlew assembleDebug
}

# Fonction pour nettoyer le projet
function Clean-Project {
    Write-Host "Nettoyage du projet..." -ForegroundColor Yellow
    ./gradlew clean
}

# Fonction pour installer l'APK
function Install-APK {
    Write-Host "Installation de l'APK..." -ForegroundColor Cyan
    ./gradlew installDebug
}

# Fonction pour build + install
function Build-And-Install {
    Write-Host "Compilation et installation..." -ForegroundColor Green
    Build-Project
    if ($LASTEXITCODE -eq 0) {
        Install-APK
    } else {
        Write-Host "Erreur de compilation, installation annulee" -ForegroundColor Red
    }
}

# Fonction pour afficher les logs
function Show-Logs {
    param([string]$Filter = "SweetHome|libretro|DEBUG|SCREENSHOT|ERROR")
    Write-Host "Affichage des logs Android (filtre: $Filter)" -ForegroundColor Magenta
    Write-Host "Appuyez sur Ctrl+C pour arreter" -ForegroundColor Yellow
    adb logcat | Select-String -Pattern $Filter
}

# Fonction pour lister les appareils
function Get-Devices {
    Write-Host "Appareils connectes:" -ForegroundColor Cyan
    adb devices
}

# Fonction pour redemarrer ADB
function Restart-ADB {
    Write-Host "Redemarrage du serveur ADB..." -ForegroundColor Yellow
    adb kill-server
    Start-Sleep -Seconds 2
    adb start-server
    Write-Host "Serveur ADB redemarre" -ForegroundColor Green
}

# Fonction pour nettoyer les logs
function Clear-Logs {
    Write-Host "Nettoyage des logs Android..." -ForegroundColor Yellow
    adb logcat -c
    Write-Host "Logs nettoyes" -ForegroundColor Green
}

# Fonction pour surveiller les captures d'ecran
function Watch-Screenshots {
    Write-Host "Surveillance des captures d'ecran..." -ForegroundColor Magenta
    adb logcat | Select-String -Pattern "SCREENSHOT|DEBUG|ERROR" -Context 2
}

# Fonction complete: build + install + logs
function Deploy-And-Monitor {
    Write-Host "Deploiement complet du projet..." -ForegroundColor Cyan
    Build-And-Install
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Deploiement reussi! Affichage des logs..." -ForegroundColor Green
        Start-Sleep -Seconds 2
        Show-Logs
    }
}

# Message de bienvenue
Write-Host "Script de developpement Android charge!" -ForegroundColor Green
Write-Host "Commandes disponibles:" -ForegroundColor Cyan
Write-Host "  Start-Emulator [nom]     - Lancer l'emulateur" -ForegroundColor White
Write-Host "  Build-Project           - Compiler le projet" -ForegroundColor White
Write-Host "  Clean-Project           - Nettoyer le projet" -ForegroundColor White
Write-Host "  Install-APK             - Installer l'APK" -ForegroundColor White
Write-Host "  Build-And-Install       - Build + Install" -ForegroundColor White
Write-Host "  Show-Logs [filtre]      - Afficher les logs" -ForegroundColor White
Write-Host "  Watch-Screenshots       - Surveiller les captures" -ForegroundColor White
Write-Host "  Get-Devices             - Lister les appareils" -ForegroundColor White
Write-Host "  Restart-ADB             - Redemarrer ADB" -ForegroundColor White
Write-Host "  Clear-Logs              - Nettoyer les logs" -ForegroundColor White
Write-Host "  Deploy-And-Monitor      - Build + Install + Logs" -ForegroundColor White
Write-Host "`nExemple: Watch-Screenshots" -ForegroundColor Green 