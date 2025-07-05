# Profil PowerShell pour le développement Android
# Placez ce fichier dans : $env:USERPROFILE\Documents\WindowsPowerShell\

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
    Write-Host "Installation de l'APK sur l'emulateur..." -ForegroundColor Cyan
    ./gradlew installDebug
}

function logcat {
    Write-Host "Affichage des logs Android..." -ForegroundColor Magenta
    adb logcat | Select-String -Pattern "SweetHome|libretro|DEBUG|SCREENSHOT|ERROR"
}

function start-emulator {
    Write-Host "Demarrage de l'emulateur..." -ForegroundColor Blue
    emulator -avd Medium_Phone_API_36.0
}

function build-and-install {
    Write-Host "Compilation et installation..." -ForegroundColor Green
    build
    if ($LASTEXITCODE -eq 0) {
        install
    } else {
        Write-Host "Erreur de compilation, installation annulee" -ForegroundColor Red
    }
}

function Get-Devices {
    Write-Host "Appareils connectes :" -ForegroundColor Cyan
    adb devices
}

# Message de bienvenue
Write-Host "Profil Android charge !" -ForegroundColor Green
Write-Host "Commandes disponibles : build, clean, install, logcat, start-emulator, build-and-install, Get-Devices" -ForegroundColor Cyan 