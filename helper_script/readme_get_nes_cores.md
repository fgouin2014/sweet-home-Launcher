# Script de téléchargement des cores NES LibRetro pour Android

[![PowerShell](https://img.shields.io/badge/PowerShell-5.1%2B-blue.svg)](https://docs.microsoft.com/en-us/powershell/)
[![Platform](https://img.shields.io/badge/Platform-Windows-lightgrey.svg)](https://www.microsoft.com/windows)
[![LibRetro](https://img.shields.io/badge/LibRetro-Buildbot-orange.svg)](https://buildbot.libretro.com/)

Script PowerShell automatisé pour télécharger et organiser les cores Nintendo Entertainment System (NES) depuis le buildbot LibRetro, spécifiquement optimisé pour Android.

## 🎯 Fonctionnalités

- **Téléchargement automatique** des 4 principaux cores NES
- **Organisation par architecture** (ARM64, ARMv7, x86_64, x86)
- **Extraction automatique** des archives ZIP
- **Mode découverte** pour trouver les fichiers disponibles
- **Test de connectivité** intégré
- **Gestion des erreurs** avec retry automatique
- **Rapport détaillé** des téléchargements

## 🎮 Cores NES inclus

| Core | Description | Spécialité |
|------|-------------|------------|
| **FCEUmm** | Core principal | Compatibilité générale excellente |
| **Nestopia** | Haute précision | Émulation cycle-accurate |
| **Mesen** | Ultra précis | Debugging et développement |
| **QuickNES** | Performances | Optimisé pour vitesse |

## 📁 Structure générée

```
cores/
├── arm64-v8a/
│   ├── fceumm_libretro_android.so
│   ├── nestopia_libretro_android.so
│   ├── mesen_libretro_android.so
│   └── quicknes_libretro_android.so
├── armeabi-v7a/
│   ├── fceumm_libretro_android.so
│   ├── nestopia_libretro_android.so
│   ├── mesen_libretro_android.so
│   └── quicknes_libretro_android.so
├── x86_64/
│   ├── fceumm_libretro_android.so
│   ├── nestopia_libretro_android.so
│   ├── mesen_libretro_android.so
│   └── quicknes_libretro_android.so
└── x86/
    ├── fceumm_libretro_android.so
    ├── nestopia_libretro_android.so
    ├── mesen_libretro_android.so
    └── quicknes_libretro_android.so
```

## 🚀 Installation et utilisation

### Prérequis

- Windows 10/11
- PowerShell 5.1 ou supérieur
- Connexion Internet

### Téléchargement

1. Téléchargez le script `download-nes-cores.ps1`
2. Placez-le dans un dossier de votre choix
3. Ouvrez PowerShell en tant qu'administrateur

### Première utilisation (recommandée)

```powershell
# 1. Tester la connectivité
.\download-nes-cores.ps1 -Test

# 2. Télécharger avec découverte automatique
.\download-nes-cores.ps1 -DiscoverMode

# 3. Vérifier la structure
.\download-nes-cores.ps1 -Structure
```

## 📖 Guide d'utilisation

### Commandes principales

| Commande | Description |
|----------|-------------|
| `.\script.ps1` | Téléchargement et extraction standard |
| `.\script.ps1 -DiscoverMode` | Mode découverte (recommandé) |
| `.\script.ps1 -Test` | Test de connectivité buildbot |
| `.\script.ps1 -KeepZip` | Garde les fichiers ZIP après extraction |
| `.\script.ps1 -ExtractOnly` | Extrait uniquement les ZIP existants |

### Commandes d'information

| Commande | Description |
|----------|-------------|
| `.\script.ps1 -Structure` | Affiche l'arborescence des fichiers |
| `.\script.ps1 -Cores` | Liste les cores par architecture |
| `.\script.ps1 -Report` | Rapport détaillé des téléchargements |
| `.\script.ps1 -Help` | Aide complète |

### Commandes de maintenance

| Commande | Description |
|----------|-------------|
| `.\script.ps1 -Extract` | Extrait toutes les archives existantes |
| `.\script.ps1 -CleanArchives` | Supprime uniquement les ZIP |
| `.\script.ps1 -Clean` | Supprime tout le dossier cores/ |

## 🔧 Exemples d'utilisation

### Utilisation basique
```powershell
# Téléchargement complet avec extraction automatique
.\download-nes-cores.ps1
```

### Première fois / Résolution de problèmes
```powershell
# Mode découverte pour trouver les fichiers disponibles
.\download-nes-cores.ps1 -DiscoverMode
```

### Conservation des archives
```powershell
# Garder les ZIP pour backup
.\download-nes-cores.ps1 -KeepZip
```

### Vérification des résultats
```powershell
# Voir la structure générée
.\download-nes-cores.ps1 -Structure

# Rapport détaillé
.\download-nes-cores.ps1 -Report
```

## 📊 Exemple de sortie

```
=== Téléchargement cores NES Android (Version corrigée) ===
Destination: .\cores
Source: https://buildbot.libretro.com/nightly/android/latest

Architecture: arm64-v8a
[+] Créé: .\cores\arm64-v8a
[FOUND] fceumm_libretro_android.so.zip
[DL] fceumm_libretro_android.so.zip (tentative 1/3)
[OK] fceumm_libretro_android.so.zip (856.23 KB)
[EXT] Extrait: fceumm_libretro_android.so.zip
[DEL] ZIP supprimé: fceumm_libretro_android.so.zip

=== Processus terminé ===
Téléchargements: 16/16 fichiers
Extractions: 16 archives
```

## 🏗️ Architectures supportées

| Architecture | Description | Appareils |
|--------------|-------------|-----------|
| **arm64-v8a** | ARM 64-bit | Appareils Android modernes |
| **armeabi-v7a** | ARM 32-bit | Appareils Android plus anciens |
| **x86_64** | Intel/AMD 64-bit | Émulateurs Android x64 |
| **x86** | Intel/AMD 32-bit | Émulateurs Android x86 |

## 🛠️ Fonctionnalités avancées

### Mode découverte automatique
Le script peut automatiquement détecter les noms de fichiers disponibles :
```powershell
.\download-nes-cores.ps1 -DiscoverMode
```

### Test de connectivité
Vérifiez si le buildbot LibRetro est accessible :
```powershell
.\download-nes-cores.ps1 -Test
```

### Gestion des erreurs
- **Retry automatique** : 3 tentatives par fichier
- **Gestion des 404** : Skip automatique des fichiers inexistants
- **Validation des téléchargements** : Vérification de la taille des fichiers

## 📋 Configuration

### Variables principales
```powershell
$baseUrl = "https://buildbot.libretro.com/nightly/android/latest"
$nesCores = @("fceumm", "nestopia", "mesen", "quicknes")
$downloadPath = ".\cores"
```

### Personnalisation
Pour ajouter d'autres cores, modifiez la variable `$nesCores` :
```powershell
$nesCores = @("fceumm", "nestopia", "mesen", "quicknes", "bnes")
```

## 🐛 Résolution de problèmes

### Erreurs 404
- Utilisez le mode découverte : `.\script.ps1 -DiscoverMode`
- Testez la connectivité : `.\script.ps1 -Test`

### Erreurs de téléchargement
- Le script effectue 3 tentatives automatiquement
- Vérifiez votre connexion Internet
- Certains cores peuvent ne pas être disponibles pour toutes les architectures

### Erreurs d'extraction
- Vérifiez que PowerShell peut écrire dans le dossier
- Exécutez en tant qu'administrateur si nécessaire

### Fichiers manquants
```powershell
# Vérifier quels fichiers sont disponibles
.\script.ps1 -DiscoverMode

# Voir le rapport détaillé
.\script.ps1 -Report
```

## 🔄 Mises à jour

Le buildbot LibRetro est mis à jour quotidiennement. Pour obtenir les dernières versions :
```powershell
# Nettoyer et retélécharger
.\script.ps1 -Clean
.\script.ps1 -DiscoverMode
```

## 📝 Notes techniques

### Changements de structure (2025)
Le script a été mis à jour pour supporter la nouvelle structure du buildbot LibRetro :
- **Ancienne URL** : `/android/latest/fichier.zip`
- **Nouvelle URL** : `/android/latest/architecture/fichier.zip`

### Performances
- Téléchargement séquentiel avec délai de 500ms entre chaque fichier
- Extraction automatique pour économiser l'espace disque
- Suppression automatique des ZIP (sauf avec `-KeepZip`)

## 🎯 Utilisation avec les émulateurs

### RetroArch Android
1. Copiez les fichiers `.so` dans le dossier cores de RetroArch
2. Chemin typique : `/storage/emulated/0/Android/data/com.retroarch/files/cores/`

### Frontend Android
Les cores peuvent être utilisés avec :
- RetroArch
- Lemuroid  
- ClassicBoy
- Autres frontends compatibles LibRetro

## 📄 Licence

Ce script est fourni "tel quel" sans garantie. Les cores LibRetro sont soumis à leurs propres licences respectives.

## 🤝 Contribution

Pour signaler des bugs ou proposer des améliorations :
1. Testez avec `.\script.ps1 -Test`
2. Essayez le mode découverte `.\script.ps1 -DiscoverMode`
3. Fournissez le rapport `.\script.ps1 -Report`

---

*Script créé pour faciliter le téléchargement des cores NES LibRetro pour Android*