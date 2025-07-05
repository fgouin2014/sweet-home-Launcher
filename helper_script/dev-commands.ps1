# Script de développement pour Sweet Home Launcher
# Usage : . .\helper_script\dev-commands.ps1

Write-Host "Chargement des commandes de développement Sweet Home Launcher..." -ForegroundColor Green

# Alias pour les commandes Android
Set-Alias -Name adb -Value "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
Set-Alias -Name emulator -Value "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe"

# Fonctions pour le développement
function build {
    Write-Host "Compilation du projet Sweet Home Launcher..." -ForegroundColor Green
    ./gradlew assembleDebug
}

function clean {
    Write-Host "Nettoyage du projet..." -ForegroundColor Yellow
    ./gradlew clean
}

function install {
    Write-Host "Installation de l'APK sur l'émulateur..." -ForegroundColor Cyan
    ./gradlew installDebug
}

function logcat {
    Write-Host "Affichage des logs Android..." -ForegroundColor Magenta
    adb logcat | Select-String -Pattern "SweetHome|libretro|GameActivity"
}

function start-emulator {
    Write-Host "Démarrage de l'émulateur..." -ForegroundColor Blue
    emulator -avd Medium_Phone_API_36.0
}

function build-and-install {
    Write-Host "Compilation et installation..." -ForegroundColor Green
    build
    if ($LASTEXITCODE -eq 0) {
        install
    }
}

function Get-Devices {
    Write-Host "Appareils connectés :" -ForegroundColor Cyan
    adb devices
}

Write-Host "Commandes disponibles : build, clean, install, logcat, start-emulator, build-and-install, Get-Devices" -ForegroundColor Cyan
Write-Host "Pour recharger : . .\helper_script\dev-commands.ps1" -ForegroundColor Yellow 