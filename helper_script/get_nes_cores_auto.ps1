# Script pour organiser les cores NES par architecture avec extraction automatique
# Structure CORRIGÉE: cores/architecture/fichier.dll ou .so
# Exemple: cores/x86_64/quicknes_libretro_android.so
# Auteur: Assistant Claude - Version corrigée 2025
# Plateforme: Windows 11

# Configuration
$baseUrl = "https://buildbot.libretro.com/nightly/android/latest"
$nesCores = @("fceumm", "nestopia", "mesen", "quicknes")
$downloadPath = ".\cores"

# Configuration des architectures Android avec nouvelle structure URL
$architectures = @{
    "arm64-v8a" = @(
        @{
            "url_path" = "arm64-v8a"
            "files" = @{
                "fceumm" = "fceumm_libretro_android.so.zip"
                "nestopia" = "nestopia_libretro_android.so.zip"
                "mesen" = "mesen_libretro_android.so.zip"
                "quicknes" = "quicknes_libretro_android.so.zip"
            }
        }
    )
    "armeabi-v7a" = @(
        @{
            "url_path" = "armeabi-v7a"
            "files" = @{
                "fceumm" = "fceumm_libretro_android.so.zip"
                "nestopia" = "nestopia_libretro_android.so.zip"
                "mesen" = "mesen_libretro_android.so.zip"
                "quicknes" = "quicknes_libretro_android.so.zip"
            }
        }
    )
    "x86_64" = @(
        @{
            "url_path" = "x86_64"
            "files" = @{
                "fceumm" = "fceumm_libretro_android.so.zip"
                "nestopia" = "nestopia_libretro_android.so.zip"
                "mesen" = "mesen_libretro_android.so.zip"
                "quicknes" = "quicknes_libretro_android.so.zip"
            }
        }
    )
    "x86" = @(
        @{
            "url_path" = "x86"
            "files" = @{
                "fceumm" = "fceumm_libretro_android.so.zip"
                "nestopia" = "nestopia_libretro_android.so.zip"
                "mesen" = "mesen_libretro_android.so.zip"
                "quicknes" = "quicknes_libretro_android.so.zip"
            }
        }
    )
}

# Fonction pour tester la disponibilité d'un fichier
function Test-FileExists {
    param([string]$url)
    
    try {
        $response = Invoke-WebRequest -Uri $url -Method Head -ErrorAction Stop
        return ($response.StatusCode -eq 200)
    }
    catch {
        return $false
    }
}

# Fonction pour découvrir les fichiers disponibles
function Discover-AvailableFiles {
    param([string]$architecture)
    
    Write-Host "[INFO] Découverte des fichiers pour $architecture..." -ForegroundColor Yellow
    $archUrl = "$baseUrl/$architecture/"
    $availableFiles = @()
    
    foreach ($core in $nesCores) {
        # Tester différents formats de noms de fichiers
        $possibleNames = @(
            "$core`_libretro_android.so.zip",
            "$core`_libretro.so.zip",
            "$core.so.zip"
        )
        
        foreach ($fileName in $possibleNames) {
            $testUrl = "$archUrl$fileName"
            if (Test-FileExists $testUrl) {
                $availableFiles += @{
                    "core" = $core
                    "filename" = $fileName
                    "url" = $testUrl
                }
                Write-Host "[FOUND] $fileName" -ForegroundColor Green
                break
            }
        }
    }
    
    return $availableFiles
}

# Fonction pour créer les répertoires
function Create-Directory {
    param([string]$path)
    if (!(Test-Path $path)) {
        New-Item -ItemType Directory -Path $path -Force | Out-Null
        Write-Host "[+] Créé: $path" -ForegroundColor Green
    }
}

# Fonction pour télécharger un fichier avec retry
function Download-File {
    param(
        [string]$url,
        [string]$destination,
        [string]$fileName,
        [int]$maxRetries = 3
    )
    
    for ($i = 1; $i -le $maxRetries; $i++) {
        try {
            Write-Host "[DL] $fileName (tentative $i/$maxRetries)" -ForegroundColor Yellow
            
            # Utiliser ProgressPreference pour éviter les erreurs avec Invoke-WebRequest
            $originalPref = $ProgressPreference
            $ProgressPreference = 'SilentlyContinue'
            
            Invoke-WebRequest -Uri $url -OutFile $destination -ErrorAction Stop -UseBasicParsing
            
            $ProgressPreference = $originalPref
            
            $fileSize = [math]::Round((Get-Item $destination).Length / 1KB, 2)
            Write-Host "[OK] $fileName ($fileSize KB)" -ForegroundColor Green
            return $true
        }
        catch {
            Write-Host "[ERR] Tentative $i - $($_.Exception.Message)" -ForegroundColor Red
            if ($i -lt $maxRetries) {
                Start-Sleep -Seconds 2
            }
        }
    }
    return $false
}

# Fonction pour extraire une archive
function Extract-Archive {
    param(
        [string]$zipPath,
        [string]$extractPath,
        [string]$fileName
    )
    
    try {
        Expand-Archive -Path $zipPath -DestinationPath $extractPath -Force
        Write-Host "[EXT] Extrait: $fileName" -ForegroundColor Cyan
        return $true
    }
    catch {
        Write-Host "[ERR] Extraction: $fileName - $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Fonction principale avec découverte automatique
function Download-And-Extract-NESCores {
    param(
        [switch]$ExtractOnly, 
        [switch]$KeepZip,
        [switch]$DiscoverMode
    )
    
    Write-Host "=== Téléchargement cores NES Android (Version corrigée) ===" -ForegroundColor Cyan
    Write-Host "Destination: $downloadPath" -ForegroundColor White
    Write-Host "Source: $baseUrl" -ForegroundColor White
    
    $totalDownloads = 0
    $successfulDownloads = 0
    $extractedFiles = 0
    
    # Parcourir chaque architecture
    foreach ($archName in $architectures.Keys) {
        Write-Host "`nArchitecture: $archName" -ForegroundColor Magenta
        
        # Créer le répertoire de l'architecture
        $archPath = Join-Path $downloadPath $archName
        Create-Directory $archPath
        
        if ($DiscoverMode) {
            # Mode découverte - trouver les fichiers réellement disponibles
            $availableFiles = Discover-AvailableFiles $archName
            
            foreach ($fileInfo in $availableFiles) {
                $totalDownloads++
                $fileName = $fileInfo.filename
                $coreUrl = $fileInfo.url
                $zipFile = Join-Path $archPath $fileName
                
                # Télécharger si pas déjà présent
                $needsDownload = !(Test-Path $zipFile)
                if ($needsDownload -and !$ExtractOnly) {
                    $downloaded = Download-File $coreUrl $zipFile $fileName
                    if ($downloaded) {
                        $successfulDownloads++
                    } else {
                        continue
                    }
                    Start-Sleep -Milliseconds 500
                } elseif (!$ExtractOnly) {
                    Write-Host "[SKIP] $fileName (déjà présent)" -ForegroundColor Gray
                    $successfulDownloads++
                }
                
                # Extraire l'archive si elle existe
                if (Test-Path $zipFile) {
                    $extracted = Extract-Archive $zipFile $archPath $fileName
                    if ($extracted) {
                        $extractedFiles++
                        
                        # Supprimer le ZIP si demandé
                        if (!$KeepZip) {
                            Remove-Item $zipFile -Force
                            Write-Host "[DEL] ZIP supprimé: $fileName" -ForegroundColor Gray
                        }
                    }
                }
            }
        } else {
            # Mode classique avec noms prédéfinis
            foreach ($source in $architectures[$archName]) {
                Write-Host "Source: $($source.url_path)" -ForegroundColor Blue
                
                # Télécharger chaque core
                foreach ($core in $nesCores) {
                    if ($source.files.ContainsKey($core)) {
                        $totalDownloads++
                        $fileName = $source.files[$core]
                        $coreUrl = "$baseUrl/$($source.url_path)/$fileName"
                        $zipFile = Join-Path $archPath $fileName
                        
                        # Télécharger si pas déjà présent
                        $needsDownload = !(Test-Path $zipFile)
                        if ($needsDownload -and !$ExtractOnly) {
                            $downloaded = Download-File $coreUrl $zipFile $fileName
                            if ($downloaded) {
                                $successfulDownloads++
                            } else {
                                continue
                            }
                            Start-Sleep -Milliseconds 500
                        } elseif (!$ExtractOnly) {
                            Write-Host "[SKIP] $fileName (déjà présent)" -ForegroundColor Gray
                            $successfulDownloads++
                        }
                        
                        # Extraire l'archive si elle existe
                        if (Test-Path $zipFile) {
                            $extracted = Extract-Archive $zipFile $archPath $fileName
                            if ($extracted) {
                                $extractedFiles++
                                
                                # Supprimer le ZIP si demandé
                                if (!$KeepZip) {
                                    Remove-Item $zipFile -Force
                                    Write-Host "[DEL] ZIP supprimé: $fileName" -ForegroundColor Gray
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    Write-Host "`n=== Processus terminé ===" -ForegroundColor Green
    if (!$ExtractOnly) {
        Write-Host "Téléchargements: $successfulDownloads/$totalDownloads fichiers" -ForegroundColor White
    }
    Write-Host "Extractions: $extractedFiles archives" -ForegroundColor White
}

# Fonction pour tester la connectivité
function Test-BuildbotConnectivity {
    Write-Host "=== Test de connectivité buildbot ===" -ForegroundColor Cyan
    
    # Tester la base
    try {
        $response = Invoke-WebRequest -Uri $baseUrl -Method Head -UseBasicParsing -ErrorAction Stop
        Write-Host "[OK] Buildbot accessible" -ForegroundColor Green
    }
    catch {
        Write-Host "[ERR] Buildbot inaccessible: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
    
    # Tester chaque architecture
    foreach ($archName in $architectures.Keys) {
        $archUrl = "$baseUrl/$archName/"
        try {
            $response = Invoke-WebRequest -Uri $archUrl -Method Head -UseBasicParsing -ErrorAction Stop
            Write-Host "[OK] Architecture $archName accessible" -ForegroundColor Green
        }
        catch {
            Write-Host "[ERR] Architecture $archName inaccessible" -ForegroundColor Red
        }
    }
    
    return $true
}

# Fonction pour extraire toutes les archives existantes
function Extract-AllArchives {
    Write-Host "Extraction des archives existantes..." -ForegroundColor Yellow
    
    if (!(Test-Path $downloadPath)) {
        Write-Host "[ERR] Répertoire cores/ non trouvé" -ForegroundColor Red
        return
    }
    
    $zipFiles = Get-ChildItem -Path $downloadPath -Recurse -Filter "*.zip"
    $extractedCount = 0
    
    foreach ($zipFile in $zipFiles) {
        try {
            $extractPath = $zipFile.Directory.FullName
            Expand-Archive -Path $zipFile.FullName -DestinationPath $extractPath -Force
            Write-Host "[OK] $($zipFile.Name)" -ForegroundColor Green
            $extractedCount++
        }
        catch {
            Write-Host "[ERR] $($zipFile.Name)" -ForegroundColor Red
        }
    }
    
    Write-Host "Total: $extractedCount archives extraites" -ForegroundColor Cyan
}

# Fonction pour afficher la structure
function Show-Structure {
    Write-Host "`n=== Structure cores/ ===" -ForegroundColor Cyan
    
    if (Test-Path $downloadPath) {
        Write-Host "cores/" -ForegroundColor Yellow
        
        foreach ($archName in (Get-ChildItem $downloadPath -Directory | Sort-Object Name)) {
            Write-Host "+-- $($archName.Name)/" -ForegroundColor Blue
            
            $files = Get-ChildItem $archName.FullName -File | Sort-Object Name
            foreach ($file in $files) {
                $fileSize = [math]::Round($file.Length / 1KB, 2)
                $isLast = ($file -eq $files[-1])
                $prefix = if ($isLast) { "    +--" } else { "    |--" }
                
                # Différencier ZIP et fichiers extraits
                $type = if ($file.Extension -eq ".zip") { "[ZIP]" } else { "[CORE]" }
                Write-Host "$prefix $type $($file.Name) ($fileSize KB)" -ForegroundColor White
            }
        }
    } else {
        Write-Host "[ERR] Répertoire cores/ non trouvé" -ForegroundColor Red
    }
}

# Fonction pour lister les fichiers par core
function Show-CoresList {
    Write-Host "`n=== Cores par architecture ===" -ForegroundColor Cyan
    
    if (Test-Path $downloadPath) {
        foreach ($core in $nesCores) {
            Write-Host "`n${core}:" -ForegroundColor Yellow
            
            foreach ($archName in (Get-ChildItem $downloadPath -Directory | Sort-Object Name)) {
                $coreFiles = Get-ChildItem $archName.FullName -File | Where-Object { $_.Name -like "$core*" }
                
                foreach ($file in $coreFiles) {
                    $type = if ($file.Extension -eq ".zip") { "(ZIP)" } else { "(Extrait)" }
                    Write-Host "  * cores/$($archName.Name)/$($file.Name) $type" -ForegroundColor White
                }
            }
        }
    }
}

# Fonction pour générer un rapport
function Create-Report {
    if (!(Test-Path $downloadPath)) {
        Write-Host "[ERR] Aucun téléchargement trouvé" -ForegroundColor Red
        return
    }
    
    Write-Host "`n=== Rapport des téléchargements ===" -ForegroundColor Cyan
    
    $totalSize = 0
    $totalFiles = 0
    $totalZips = 0
    $totalExtracted = 0
    $architectures = Get-ChildItem $downloadPath -Directory | Sort-Object Name
    
    foreach ($arch in $architectures) {
        $files = Get-ChildItem $arch.FullName -File
        $zipFiles = $files | Where-Object { $_.Extension -eq ".zip" }
        $extractedFiles = $files | Where-Object { $_.Extension -ne ".zip" }
        
        $archSize = ($files | Measure-Object Length -Sum).Sum
        $totalSize += $archSize
        $totalFiles += $files.Count
        $totalZips += $zipFiles.Count
        $totalExtracted += $extractedFiles.Count
        
        Write-Host "`n$($arch.Name):" -ForegroundColor Blue
        Write-Host "  * $($files.Count) fichiers total" -ForegroundColor Gray
        Write-Host "  * $($zipFiles.Count) archives ZIP" -ForegroundColor Gray
        Write-Host "  * $($extractedFiles.Count) fichiers extraits" -ForegroundColor Gray
        Write-Host "  * $([math]::Round($archSize / 1MB, 2)) MB" -ForegroundColor Gray
    }
    
    Write-Host "`nTotal:" -ForegroundColor White
    Write-Host "  * $($architectures.Count) architectures" -ForegroundColor Gray
    Write-Host "  * $totalFiles fichiers ($totalZips ZIP + $totalExtracted extraits)" -ForegroundColor Gray
    Write-Host "  * $([math]::Round($totalSize / 1MB, 2)) MB" -ForegroundColor Gray
}

# Fonction de nettoyage
function Clean-Downloads {
    param([switch]$ArchivesOnly)
    
    if ($ArchivesOnly) {
        Write-Host "Suppression des ZIP..." -ForegroundColor Yellow
        $zipFiles = Get-ChildItem -Path $downloadPath -Recurse -Filter "*.zip"
        $zipFiles | Remove-Item -Force
        Write-Host "[OK] $($zipFiles.Count) archives supprimées" -ForegroundColor Green
    } else {
        Write-Host "Suppression du répertoire cores/..." -ForegroundColor Yellow
        if (Test-Path $downloadPath) {
            Remove-Item -Path $downloadPath -Recurse -Force
            Write-Host "[OK] Répertoire cores/ supprimé" -ForegroundColor Green
        }
    }
}

# Fonction d'aide mise à jour
function Show-Help {
    Write-Host @"
=== Script cores NES par architecture - VERSION CORRIGÉE 2025 ===

CORRECTIONS APPORTÉES:
  ✓ Structure URL mise à jour pour le nouveau buildbot LibRetro
  ✓ Mode découverte automatique des fichiers disponibles
  ✓ Gestion des erreurs 404 améliorée
  ✓ Test de connectivité intégré

UTILISATION:
  .\script.ps1                    - Télécharge ET extrait automatiquement
  .\script.ps1 -KeepZip           - Télécharge, extrait et garde les ZIP
  .\script.ps1 -DiscoverMode      - Mode découverte (recommandé pour la première fois)
  .\script.ps1 -ExtractOnly       - Extrait seulement les ZIP existants
  .\script.ps1 -Extract           - Extrait les ZIP existants (legacy)
  .\script.ps1 -Structure         - Affiche la structure
  .\script.ps1 -Cores             - Liste par cores
  .\script.ps1 -Report            - Rapport détaillé
  .\script.ps1 -Test              - Test de connectivité buildbot
  .\script.ps1 -Clean             - Supprime tout
  .\script.ps1 -CleanArchives     - Supprime les ZIP seulement
  .\script.ps1 -Help              - Cette aide

NOUVEAUTÉS VERSION CORRIGÉE:
  + URL de base corrigée: $baseUrl
  + Mode découverte automatique des fichiers disponibles
  + Test de connectivité au buildbot
  + Gestion des retry en cas d'échec
  + Meilleure gestion des erreurs HTTP

STRUCTURE GÉNÉRÉE (Android uniquement):
  cores/
  +-- arm64-v8a/
  |   +-- fceumm_libretro_android.so
  |   +-- nestopia_libretro_android.so
  |   +-- mesen_libretro_android.so
  |   +-- quicknes_libretro_android.so
  +-- armeabi-v7a/
  |   +-- fceumm_libretro_android.so
  |   +-- etc...
  +-- x86_64/
  |   +-- fceumm_libretro_android.so
  |   +-- etc...
  +-- x86/
      +-- fceumm_libretro_android.so
      +-- etc...

CORES NES:
  * FCEUmm   - Core principal (Nintendo Entertainment System)
  * Nestopia - Haute précision (Nintendo Entertainment System) 
  * Mesen    - Cycle-accurate (Nintendo Entertainment System)
  * QuickNES - Performances optimisées (Nintendo Entertainment System)

RECOMMANDATION:
  Utilisez d'abord: .\script.ps1 -Test
  Puis: .\script.ps1 -DiscoverMode
"@ -ForegroundColor White
}

# Gestion des paramètres
param(
    [switch]$Structure,
    [switch]$Cores,
    [switch]$Extract,
    [switch]$ExtractOnly,
    [switch]$KeepZip,
    [switch]$DiscoverMode,
    [switch]$Test,
    [switch]$Report,
    [switch]$Clean,
    [switch]$CleanArchives,
    [switch]$Help
)

# Execution
switch ($true) {
    $Help { Show-Help }
    $Test { Test-BuildbotConnectivity }
    $Structure { Show-Structure }
    $Cores { Show-CoresList }
    $Extract { Extract-AllArchives }
    $ExtractOnly { Download-And-Extract-NESCores -ExtractOnly }
    $Report { Create-Report }
    $Clean { Clean-Downloads }
    $CleanArchives { Clean-Downloads -ArchivesOnly }
    $DiscoverMode { 
        Download-And-Extract-NESCores -DiscoverMode
        Write-Host "`nUtilisez -Structure pour voir le résultat" -ForegroundColor Cyan
    }
    default { 
        if ($KeepZip) {
            Download-And-Extract-NESCores -KeepZip
        } else {
            Download-And-Extract-NESCores
        }
        Write-Host "`nUtilisez -Structure pour voir le résultat" -ForegroundColor Cyan
    }
}