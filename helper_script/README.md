# Scripts de Développement - Sweet Home Launcher

## 🚀 Commandes Rapides

### Option 1 : Script local (recommandé)
```powershell
# Charger les commandes (une fois par session)
. .\helper_script\dev-commands.ps1

# Utiliser les commandes
build
install
logcat
start-emulator
```

### Option 2 : Profil global
```powershell
# Copier le contenu de dev-commands.ps1 dans :
# C:\Users\<VotreNom>\Documents\WindowsPowerShell\Microsoft.PowerShell_profile.ps1
```

## 📋 Commandes Disponibles

| Commande | Description |
|----------|-------------|
| `build` | Compile le projet |
| `clean` | Nettoie le projet |
| `install` | Installe l'APK sur l'émulateur |
| `logcat` | Affiche les logs filtrés |
| `start-emulator` | Lance l'émulateur |
| `build-and-install` | Compile puis installe |
| `Get-Devices` | Liste les appareils connectés |

## 🔧 Après Réinstallation Windows/Cursor

1. **Copier** `helper_script/dev-commands.ps1` dans votre nouveau projet
2. **Charger** : `. .\helper_script\dev-commands.ps1`
3. **Utiliser** les commandes normalement

## 💡 Avantages du Script Local

- ✅ **Lié au projet** (versionné dans Git)
- ✅ **Pas de pollution globale** 
- ✅ **Facile à partager** avec l'équipe
- ✅ **Rechargement manuel** quand nécessaire 