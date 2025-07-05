# Microsoft.PowerShell_profile.ps1 - Profil PowerShell principal
# Vérification du mode administrateur
if (-NOT ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")) {
    Write-Host "⚠️  PowerShell n'est PAS en mode administrateur" -ForegroundColor Yellow
    Write-Host "   Certaines commandes Android peuvent nécessiter des privilèges élevés" -ForegroundColor Yellow
} else {
    Write-Host "✅ PowerShell en mode administrateur" -ForegroundColor Green
}

# Message de bienvenue
Write-Host "🚀 Profil PowerShell principal chargé" -ForegroundColor Cyan
Write-Host "📱 Développement Android - Sweet Home Launcher" -ForegroundColor Cyan

# Alias pour les commandes Android courantes
Set-Alias -Name gradle -Value ".\gradlew"
Set-Alias -Name adb -Value "adb.exe"
Set-Alias -Name logcat -Value "adb.exe logcat"

# Fonction pour lancer l'émulateur
function Start-Emulator {
    param(
        [string]$AvdName = "Pixel_7_API_34"
    )
    Write-Host "📱 Lancement de l'émulateur: $AvdName" -ForegroundColor Green
    & "emulator.exe" -avd $AvdName
}

# Fonction pour nettoyer et compiler le projet
function Build-Project {
    Write-Host "🧹 Nettoyage du projet..." -ForegroundColor Yellow
    .\gradlew clean
    
    Write-Host "🔨 Compilation du projet..." -ForegroundColor Yellow
    .\gradlew assembleDebug
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Compilation réussie!" -ForegroundColor Green
    } else {
        Write-Host "❌ Erreur de compilation" -ForegroundColor Red
    }
}

# Fonction pour installer l'APK
function Install-APK {
    param(
        [string]$ApkPath = "app\build\outputs\apk\debug\app-debug.apk"
    )
    
    if (Test-Path $ApkPath) {
        Write-Host "   Installation de l'APK: $ApkPath" -ForegroundColor Green
        adb install -r $ApkPath
    } else {
        Write-Host "❌ APK non trouvé: $ApkPath" -ForegroundColor Red
        Write-Host "   Vérifiez que la compilation s'est bien passée" -ForegroundColor Yellow
    }
}

# Fonction pour afficher les logs
function Show-Logs {
    param(
        [string]$Filter = "*"
    )
    Write-Host "   Affichage des logs Android (filtre: $Filter)" -ForegroundColor Green
    Write-Host "   Appuyez sur Ctrl+C pour arrêter" -ForegroundColor Yellow
    adb logcat $Filter
}

# Fonction pour redémarrer le serveur ADB
function Restart-ADB {
    Write-Host "🔄 Redémarrage du serveur ADB..." -ForegroundColor Yellow
    adb kill-server
    Start-Sleep -Seconds 2
    adb start-server
    Write-Host "✅ Serveur ADB redémarré" -ForegroundColor Green
}

# Fonction pour lister les appareils connectés
function Get-Devices {
    Write-Host "📱 Appareils connectés:" -ForegroundColor Green
    adb devices
}

# Fonction pour nettoyer les logs
function Clear-Logs {
    Write-Host "   Nettoyage des logs Android..." -ForegroundColor Yellow
    adb logcat -c
    Write-Host "✅ Logs nettoyés" -ForegroundColor Green
}

# Fonction complète: build + install + logs
function Deploy-And-Monitor {
    Write-Host "🚀 Déploiement complet du projet..." -ForegroundColor Cyan
    
    # Build
    Build-Project
    
    if ($LASTEXITCODE -eq 0) {
        # Install
        Install-APK
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Déploiement réussi! Affichage des logs..." -ForegroundColor Green
            Start-Sleep -Seconds 2
            Show-Logs
        }
    }
}

# Affichage des commandes disponibles
Write-Host "`n📋 Commandes disponibles:" -ForegroundColor Cyan
Write-Host "   Start-Emulator [nom_avd]     - Lancer l'émulateur" -ForegroundColor White
Write-Host "   Build-Project               - Nettoyer et compiler" -ForegroundColor White
Write-Host "   Install-APK [chemin]        - Installer l'APK" -ForegroundColor White
Write-Host "   Show-Logs [filtre]          - Afficher les logs" -ForegroundColor White
Write-Host "   Restart-ADB                 - Redémarrer le serveur ADB" -ForegroundColor White
Write-Host "   Get-Devices                 - Lister les appareils" -ForegroundColor White
Write-Host "   Clear-Logs                  - Nettoyer les logs" -ForegroundColor White
Write-Host "   Deploy-And-Monitor          - Build + Install + Logs" -ForegroundColor White
Write-Host "`n   Alias disponibles: gradle, adb, logcat" -ForegroundColor White
Write-Host "`n🎯 Exemple: Deploy-And-Monitor" -ForegroundColor Green