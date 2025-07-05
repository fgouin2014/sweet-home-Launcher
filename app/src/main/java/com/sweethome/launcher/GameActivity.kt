package com.sweethome.launcher

import android.os.Bundle
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.appcompat.app.AppCompatActivity
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.libretrodroid.GLRetroViewData
import com.swordfish.libretrodroid.ShaderConfig
import java.io.File
import java.io.FileOutputStream
import com.swordfish.libretrodroid.LibretroDroid
import android.os.Vibrator
import android.os.VibrationEffect
import android.content.Context
import androidx.drawerlayout.widget.DrawerLayout
import android.view.Gravity
import android.view.View
import android.widget.Switch
import android.content.SharedPreferences
import android.widget.ImageButton
import android.app.AlertDialog
import android.view.LayoutInflater
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.TextView
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.ImageView
import android.util.Log
import android.content.ContextWrapper
import androidx.appcompat.view.ContextThemeWrapper
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.BitmapFactory
import androidx.core.view.GravityCompat

class GameActivity : AppCompatActivity() {
    private lateinit var retroView: GLRetroView
    private val mainScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val isNewGame = intent.getBooleanExtra("new_game", false)
        val isTestRom = intent.getBooleanExtra("test_rom", false)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val frame = findViewById<ConstraintLayout>(R.id.frame)
        val btnCloseMenu = findViewById<Button>(R.id.btnCloseMenu)
        val btnMenuIcon = findViewById<Button>(R.id.btnMenuIcon)

        val abi = getDeviceAbi()
        val coreName = "fceumm_libretro_android.so" // ou choisis dynamiquement selon le core voulu
        val assetPath = "cores/$abi/$coreName"
        val coreFile = copyAssetToInternalStorage(assetPath)
        
        // Ancien comportement : charger toujours la ROM de test
        val romFile = copyAssetToInternalStorage("roms/240pee.nes")

        val data = GLRetroViewData(this).apply {
            coreFilePath = coreFile.absolutePath
            gameFilePath = romFile.absolutePath
            systemDirectory = filesDir.absolutePath
            savesDirectory = filesDir.absolutePath
            shader = ShaderConfig.Default
            rumbleEventsEnabled = true
            preferLowLatencyAudio = true
        }

        retroView = GLRetroView(this, data)
        // retroView.setBackgroundColor(android.graphics.Color.RED)

        // Ajout du GLRetroView avec contraintes si en portrait
        val isPortrait = resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT
        if (isPortrait) {
            val guideline = frame.findViewById<View>(R.id.guidelineHalf)
            val params = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_PARENT,
                0
            )
            params.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            params.bottomToTop = guideline.id
            params.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            params.setMargins(0, 0, 0, 0)
            retroView.layoutParams = params
            frame.addView(retroView, 0)
        } else {
            val params = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_PARENT,
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_PARENT
            )
            params.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            params.bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            params.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            params.setMargins(0, 0, 0, 0)
            retroView.layoutParams = params
            frame.addView(retroView, 0)
        }
        lifecycle.addObserver(retroView)

        if (!isNewGame) {
            // Chargement automatique de l'état du jeu après un changement d'orientation
            savedInstanceState?.getByteArray("game_save_state")?.let { saveData ->
                retroView.post { retroView.unserializeState(saveData) }
            } ?: run {
                // Sinon, restauration auto depuis le fichier si présent
                val saveFile = File(filesDir, "auto_save_state.bin")
                if (saveFile.exists()) {
                    val loadedData = saveFile.readBytes()
                    retroView.post { retroView.unserializeState(loadedData) }
                }
            }
        }

        // Chargement d'une sauvegarde spécifique si demandé
        val saveFileName = intent.getStringExtra("save_file")
        if (!saveFileName.isNullOrEmpty()) {
            mainScope.launch {
                retroView.getGLRetroEvents().collect { event ->
                    if (event is com.swordfish.libretrodroid.GLRetroView.GLRetroEvents.FrameRendered) {
                        val file = File(filesDir, saveFileName)
                        if (file.exists()) {
                            val data = file.readBytes()
                            val ok = retroView.unserializeState(data)
                            if (!ok) showDarkToast("Erreur lors du chargement de la sauvegarde")
                        } else {
                            showDarkToast("Sauvegarde introuvable")
                        }
                        this.cancel() // On ne veut charger qu'une seule fois
                    }
                }
            }
        }

        // Appliquer le style SVG par défaut (plus de switch)
        val btnA = findViewById<Button>(R.id.btnA)
        val btnB = findViewById<Button>(R.id.btnB)
        val btnUp = findViewById<Button>(R.id.btnUp)
        val btnDown = findViewById<Button>(R.id.btnDown)
        val btnLeft = findViewById<Button>(R.id.btnLeft)
        val btnRight = findViewById<Button>(R.id.btnRight)
        btnA.background = getDrawable(R.drawable.btn_round_a_selector)
        btnB.background = getDrawable(R.drawable.btn_round_b_selector)
        btnUp.background = getDrawable(R.drawable.btn_dpad_up_selector)
        btnDown.background = getDrawable(R.drawable.btn_dpad_down_selector)
        btnLeft.background = getDrawable(R.drawable.btn_dpad_left_selector)
        btnRight.background = getDrawable(R.drawable.btn_dpad_right_selector)
        btnA.text = ""
        btnB.text = ""
        btnUp.text = ""
        btnDown.text = ""
        btnLeft.text = ""
        btnRight.text = ""

        // Connexion des boutons de contrôle
        findViewById<Button>(R.id.btnUp).setOnTouchListener { v, event -> handleKeyEvent(event, android.view.KeyEvent.KEYCODE_DPAD_UP); false }
        findViewById<Button>(R.id.btnDown).setOnTouchListener { v, event -> handleKeyEvent(event, android.view.KeyEvent.KEYCODE_DPAD_DOWN); false }
        findViewById<Button>(R.id.btnLeft).setOnTouchListener { v, event -> handleKeyEvent(event, android.view.KeyEvent.KEYCODE_DPAD_LEFT); false }
        findViewById<Button>(R.id.btnRight).setOnTouchListener { v, event -> handleKeyEvent(event, android.view.KeyEvent.KEYCODE_DPAD_RIGHT); false }
        findViewById<Button>(R.id.btnA).setOnTouchListener { v, event -> handleKeyEvent(event, android.view.KeyEvent.KEYCODE_BUTTON_A); false }
        findViewById<Button>(R.id.btnB).setOnTouchListener { v, event -> handleKeyEvent(event, android.view.KeyEvent.KEYCODE_BUTTON_B); false }
        findViewById<Button>(R.id.btnStart).setOnTouchListener { v, event -> handleKeyEvent(event, android.view.KeyEvent.KEYCODE_BUTTON_START); false }
        findViewById<Button>(R.id.btnSelect).setOnTouchListener { v, event -> handleKeyEvent(event, android.view.KeyEvent.KEYCODE_BUTTON_SELECT); false }

        // Retour système : ferme le menu si ouvert
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END)
                } else {
                    finish()
                }
            }
        })

        btnMenuIcon.setOnLongClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
            true
        }
        btnMenuIcon.setOnClickListener { /* rien sur simple clic */ }

        btnCloseMenu.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
        }

        // Gestion des boutons du menu latéral
        val btnPowerOff = findViewById<Button>(R.id.btnPowerOff)
        val btnHardwareReset = findViewById<Button>(R.id.btnHardwareReset)
        val btnReturnToTitle = findViewById<Button>(R.id.btnReturnToTitle)

        btnPowerOff.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Power-Off")
                .setMessage("Voulez-vous vraiment éteindre l'émulateur NES ?")
                .setNegativeButton("Annuler", null)
                .setPositiveButton("Éteindre") { _, _ ->
                    // Arrêt propre de l'émulateur libretro
                    if (::retroView.isInitialized) {
                        // Sauvegarde automatique avant fermeture
                        try {
                            val saveData = retroView.serializeState()
                            val saveFile = File(filesDir, "auto_save_state.bin")
                            FileOutputStream(saveFile).use { it.write(saveData) }
                        } catch (e: Exception) {
                            // Ignore les erreurs de sauvegarde
                        }
                        
                        // Arrêt propre de l'émulateur
                        retroView.onPause()
                        retroView.onDestroy()
                    }
                    
                    // Fermeture complète de l'application
                    finishAffinity()
                    System.exit(0)
                }
                .show()
        }

        btnHardwareReset.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Hardware Reset")
                .setMessage("Voulez-vous vraiment redémarrer l'émulateur ? Cela effacera la partie en cours.")
                .setNegativeButton("Annuler", null)
                .setPositiveButton("Redémarrer") { _, _ ->
                    if (::retroView.isInitialized) {
                        retroView.reset()
                        showDarkToast("Émulateur redémarré")
                    }
                }
                .show()
        }

        btnReturnToTitle.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Retour à l'écran titre")
                .setMessage("Voulez-vous retourner à l'écran titre ? La partie en cours sera sauvegardée automatiquement.")
                .setNegativeButton("Annuler", null)
                .setPositiveButton("Retourner") { _, _ ->
                    if (::retroView.isInitialized) {
                        // Sauvegarde automatique avant de partir
                        val saveData = retroView.serializeState()
                        val saveFile = File(filesDir, "auto_save_state.bin")
                        FileOutputStream(saveFile).use { it.write(saveData) }
                    }
                    finish()
                }
                .show()
        }

        val btnSave = findViewById<Button>(R.id.btnSave)
        btnSave.setOnClickListener {
            // Quicksave dans le slot 0
            if (!::retroView.isInitialized) return@setOnClickListener
            val saveData = retroView.serializeState()
            val file = File(filesDir, "save_slot_0.sav")
            file.writeBytes(saveData)
            // Capture d'écran pour le quicksave
            retroView.captureScreenshotSafe { screenshotBytes ->
                if (screenshotBytes != null && screenshotBytes.isNotEmpty()) {
                    val screenshotFile = File(filesDir, "save_slot_0.png")
                    screenshotFile.writeBytes(screenshotBytes)
                }
                Toast.makeText(this, "Quicksave effectué !", Toast.LENGTH_SHORT).show()
            }
        }
        btnSave.setOnLongClickListener {
            showSaveDialog()
            true
        }

        val btnLoad = findViewById<Button>(R.id.btnLoad)
        btnLoad.setOnClickListener {
            // Chargement du quicksave (slot 0), sinon dernière sauvegarde slot 1-5
            if (!::retroView.isInitialized) return@setOnClickListener
            val quickFile = File(filesDir, "save_slot_0.sav")
            if (quickFile.exists()) {
                val data = quickFile.readBytes()
                val ok = retroView.unserializeState(data)
                if (ok) {
                    Toast.makeText(this, "Quicksave chargé !", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Erreur lors du chargement du quicksave", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Chercher la dernière sauvegarde parmi les slots 1 à 5
                val slots = (1..5).map { File(filesDir, "save_slot_${it}.sav") }
                val lastFile = slots.filter { it.exists() }.maxByOrNull { it.lastModified() }
                if (lastFile != null) {
                    val data = lastFile.readBytes()
                    val ok = retroView.unserializeState(data)
                    if (ok) {
                        Toast.makeText(this, "Dernière sauvegarde chargée (slot ${lastFile.name.filter { it.isDigit() }}) !", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Erreur lors du chargement de la sauvegarde", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Aucune sauvegarde trouvée.\nAppuyez sur le bouton de gauche pour créer un quicksave, ou faites un appui long pour accéder aux sauvegardes manuelles.", Toast.LENGTH_LONG).show()
                }
            }
        }
        btnLoad.setOnLongClickListener {
            showLoadDialog()
            true
        }

        // Ouvre le menu de chargement si demandé, même si new_game est présent
        if (intent.getBooleanExtra("open_load_menu", false)) {
            mainScope.launch {
                retroView.getGLRetroEvents().collect { event ->
                    if (event is com.swordfish.libretrodroid.GLRetroView.GLRetroEvents.FrameRendered) {
                        if (::retroView.isInitialized) {
                            retroView.reset()
                            retroView.onPause()
                        }
                        showLoadDialog()
                        this.cancel()
                    }
                }
            }
        }

        // Affiche le screenshot natif si dispo (debug)
        val debugScreenshot = findViewById<ImageView>(R.id.debugScreenshot)
        val density = resources.displayMetrics.density
        val widthPx = 256
        val heightPx = 240
        val widthDp = widthPx / density
        val heightDp = heightPx / density
        val params = debugScreenshot.layoutParams
        params.width = widthDp.toInt()
        params.height = heightDp.toInt()
        debugScreenshot.layoutParams = params

        // Connexion du bouton de capture d'écran
        val btnScreenshot = findViewById<Button>(R.id.btnScreenshot)
        btnScreenshot.visibility = View.VISIBLE
        btnScreenshot.isEnabled = true
        btnScreenshot.setOnClickListener {
            // Générer un nom de fichier unique avec timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            retroView.captureScreenshotSafe { screenshotBytes ->
                if (screenshotBytes != null && screenshotBytes.isNotEmpty()) {
                    // 1. Décoder le screenshot natif
                    val fullBitmap = BitmapFactory.decodeByteArray(screenshotBytes, 0, screenshotBytes.size)
                    if (fullBitmap != null) {
                        // 2. Détecter la zone du jeu (zone non-noire)
                        val gameZone = detectGameZone(fullBitmap)
                        
                        // 3. Créer le Bitmap recadré à la résolution native 256x240
                        val croppedBitmap = Bitmap.createBitmap(fullBitmap, gameZone.left, gameZone.top, gameZone.width(), gameZone.height())
                        val finalBitmap = Bitmap.createScaledBitmap(croppedBitmap, 256, 240, true)

                        // 4. Sauvegarder le Bitmap final
                        val screenshotFile = File(filesDir, "screenshot_${timestamp}.png")
                        FileOutputStream(screenshotFile).use { out ->
                            finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                        showScreenshotPreviewDialog(screenshotFile)
                    } else {
                        showScreenshotPreviewDialog(null)
                    }
                } else {
                    showScreenshotPreviewDialog(null)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::retroView.isInitialized) {
            val saveData = retroView.serializeState()
            outState.putByteArray("game_save_state", saveData)
        }
    }

    override fun onPause() {
        super.onPause()
        if (::retroView.isInitialized) {
            val saveData = retroView.serializeState()
            val saveFile = File(filesDir, "auto_save_state.bin")
            FileOutputStream(saveFile).use { it.write(saveData) }
        }
    }

    private fun getDeviceAbi(): String {
        // Retourne la première ABI supportée, ou armeabi-v7a par défaut
        return android.os.Build.SUPPORTED_ABIS.firstOrNull() ?: "armeabi-v7a"
    }

    private fun copyAssetToInternalStorage(assetName: String): File {
        val file = File(filesDir, File(assetName).name) // On copie sous le nom du fichier seul
        file.parentFile?.mkdirs()
        if (!file.exists()) {
            assets.open(assetName).use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            android.util.Log.i("ASSET_COPY", "Copié: ${file.absolutePath} (${file.length()} octets)")
        } else {
            android.util.Log.i("ASSET_COPY", "Déjà présent: ${file.absolutePath} (${file.length()} octets)")
        }
        return file
    }

    private fun handleKeyEvent(event: android.view.MotionEvent, keyCode: Int) {
        when (event.action) {
            android.view.MotionEvent.ACTION_DOWN -> {
                retroView.sendKeyEvent(android.view.KeyEvent.ACTION_DOWN, keyCode)
                // Feedback haptique
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(30)
                }
            }
            android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> retroView.sendKeyEvent(android.view.KeyEvent.ACTION_UP, keyCode)
        }
    }

    private fun showDarkToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        val view = toast.view
        view?.setBackgroundColor(Color.parseColor("#222222"))
        val textViewId = android.R.id.message
        val text = view?.findViewById<TextView?>(textViewId)
        if (text != null) {
            text.setTextColor(Color.WHITE)
        }
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    private fun captureViewBitmap(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun updateSlotDates(dialogView: View) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        // Quicksave
        val quickFile = File(filesDir, "save_slot_0.sav")
        val quickDate = dialogView.findViewById<TextView>(R.id.quick_date)
        val quickImg = dialogView.findViewById<View>(R.id.quick_img)
        val quickPng = File(filesDir, "save_slot_0.png")
        Log.d("DEBUG", "Affichage quicksave: PNG existe=${quickPng.exists()} taille=${quickPng.length()} date=${if (quickPng.exists()) Date(quickPng.lastModified()) else "n/a"}")
        if (quickFile.exists()) {
            val date = Date(quickFile.lastModified())
            quickDate.text = "Dernière sauvegarde : ${dateFormat.format(date)}"
            // Afficher screenshot natif PNG si dispo
            if (quickImg is ImageView) {
                if (quickPng.exists()) {
                    val bmp = BitmapFactory.decodeFile(quickPng.absolutePath)
                    quickImg.scaleType = ImageView.ScaleType.CENTER_INSIDE
                    quickImg.setImageBitmap(bmp)
                } else {
                    quickImg.setBackgroundColor(Color.parseColor("#222222"))
                }
            }
        } else {
            quickDate.text = "vide"
            if (quickImg is ImageView) {
                quickImg.setImageDrawable(null)
                quickImg.setBackgroundColor(Color.parseColor("#444444"))
            } else {
                quickImg.setBackgroundColor(Color.parseColor("#444444"))
            }
        }
        // Slots 1 à 5
        for (slot in 1..5) {
            val file = File(filesDir, "save_slot_${slot}.sav")
            val tv = dialogView.findViewById<TextView>(resources.getIdentifier("slot${slot}_date", "id", packageName))
            val img = dialogView.findViewById<View>(resources.getIdentifier("slot${slot}_img", "id", packageName))
            val emptyText = dialogView.findViewById<TextView>(resources.getIdentifier("slot${slot}_empty", "id", packageName))
            val screenshotFile = File(filesDir, "save_slot_${slot}.png")
            Log.d("DEBUG", "Affichage slot $slot: PNG existe=${screenshotFile.exists()} taille=${screenshotFile.length()} date=${if (screenshotFile.exists()) Date(screenshotFile.lastModified()) else "n/a"}")
            if (file.exists()) {
                val date = Date(file.lastModified())
                tv.text = "Dernière sauvegarde : ${dateFormat.format(date)}"
                // Afficher screenshot natif PNG si dispo
                if (img is ImageView) {
                    if (screenshotFile.exists()) {
                        val bmp = BitmapFactory.decodeFile(screenshotFile.absolutePath)
                        img.scaleType = ImageView.ScaleType.CENTER_INSIDE
                        img.setImageBitmap(bmp)
                    } else {
                        img.setBackgroundColor(Color.parseColor("#EEEEEE"))
                    }
                }
                emptyText.visibility = View.GONE
            } else {
                tv.text = "vide"
                if (img is ImageView) {
                    img.setImageDrawable(null)
                    img.setBackgroundColor(Color.parseColor("#333333"))
                } else {
                    img.setBackgroundColor(Color.parseColor("#333333"))
                }
                emptyText.visibility = View.VISIBLE
            }
        }
    }

    private fun showSaveDialog() {
        // Capturer l'écran AVANT d'ouvrir le dialog
        retroView.captureScreenshotSafe { screenshotBytes ->
            if (screenshotBytes != null && screenshotBytes.isNotEmpty()) {
                // Sauvegarder la capture dans un fichier temporaire
                val tempScreenshotFile = File(filesDir, "temp_screenshot.png")
                tempScreenshotFile.writeBytes(screenshotBytes)
            }
            
            // Maintenant ouvrir le dialog
            showSaveDialogInternal()
        }
    }
    
    private fun showSaveDialogInternal() {
        // Met l'émulation en pause à l'ouverture du menu
        if (::retroView.isInitialized) retroView.onPause()
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_save_slots, null)
        val darkContext = ContextThemeWrapper(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
        val dialog = AlertDialog.Builder(darkContext)
            .setView(dialogView)
            .setTitle("Sauvegarde rapide")
            .setNegativeButton("Annuler", null)
            .create()
        dialog.setOnDismissListener {
            if (::retroView.isInitialized) retroView.onResume()
        }

        // Met à jour dynamiquement le texte des boutons de slot
        for (slot in 1..5) {
            val saveBtn = dialogView.findViewById<Button>(resources.getIdentifier("slot${slot}_load", "id", packageName))
            if (saveBtn != null) {
                saveBtn.text = "Sauvegarder dans le slot $slot"
            }
        }
        val quickSaveBtn = dialogView.findViewById<Button>(R.id.quick_load)
        if (quickSaveBtn != null) {
            quickSaveBtn.text = "Sauvegarder dans le quicksave"
        }

        fun saveToSlot(slot: Int) {
            if (!::retroView.isInitialized) return
            val saveData = retroView.serializeState()
            val file = File(filesDir, "save_slot_${slot}.sav")
            file.writeBytes(saveData)
            
            // Copier la capture temporaire vers le slot
            val tempScreenshotFile = File(filesDir, "temp_screenshot.png")
            val screenshotFile = File(filesDir, "save_slot_${slot}.png")
            if (tempScreenshotFile.exists()) {
                tempScreenshotFile.copyTo(screenshotFile, overwrite = true)
            }
            
            updateSlotDates(dialogView)
            showDarkToast("Sauvegarde effectuée dans le slot $slot")
            dialog.dismiss()
        }

        updateSlotDates(dialogView)

        // Sauvegarde immédiate sur pression d'un bouton de slot
        for (slot in 1..5) {
            val saveBtn = dialogView.findViewById<Button>(resources.getIdentifier("slot${slot}_load", "id", packageName))
            if (saveBtn != null) {
                saveBtn.setOnClickListener {
                    saveToSlot(slot)
                }
            }
        }
        
        // Bouton de sauvegarde du quicksave
        val quickSaveBtn2 = dialogView.findViewById<Button>(R.id.quick_load)
        if (quickSaveBtn2 != null) {
            quickSaveBtn2.setOnClickListener {
                saveToSlot(0) // Slot 0 = quicksave
            }
        }

        // Suppression du quicksave (slot 0)
        val quickDeleteBtn = dialogView.findViewById<Button>(R.id.quick_delete)
        quickDeleteBtn.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Supprimer le quicksave ? Cette action est irréversible.")
                .setNegativeButton("Annuler", null)
                .setPositiveButton("Supprimer") { _, _ ->
                    val file = File(filesDir, "save_slot_0.sav")
                    val screenshotFile = File(filesDir, "save_slot_0.png")
                    if (file.exists()) {
                        file.delete()
                    }
                    if (screenshotFile.exists()) {
                        screenshotFile.delete()
                    }
                    showDarkToast("Quicksave supprimé")
                    updateSlotDates(dialogView)
                }
                .show()
        }

        dialog.show()
    }

    private fun showLoadDialog() {
        // Met l'émulation en pause à l'ouverture du menu
        if (::retroView.isInitialized) retroView.onPause()
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_save_slots, null)
        val darkContext = ContextThemeWrapper(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
        val dialog = AlertDialog.Builder(darkContext)
            .setView(dialogView)
            .setTitle("Charger une sauvegarde")
            .setNegativeButton("Annuler", null)
            .create()
        dialog.setOnDismissListener {
            if (::retroView.isInitialized) retroView.onResume()
        }

        // Met à jour dynamiquement le texte des boutons de slot
        for (slot in 1..5) {
            val loadBtn = dialogView.findViewById<Button>(resources.getIdentifier("slot${slot}_load", "id", packageName))
            if (loadBtn != null) {
                loadBtn.text = "Charger le slot $slot"
            }
        }
        val quickLoadBtn = dialogView.findViewById<Button>(R.id.quick_load)
        if (quickLoadBtn != null) {
            quickLoadBtn.text = "Charger le quicksave"
        }

        fun loadFromSlot(slot: Int) {
            if (!::retroView.isInitialized) return
            val file = File(filesDir, "save_slot_${slot}.sav")
            if (file.exists()) {
                val data = file.readBytes()
                val ok = retroView.unserializeState(data)
                if (ok) {
                    showDarkToast("Sauvegarde du slot $slot restaurée")
                } else {
                    showDarkToast("Erreur lors de la restauration du slot $slot")
                }
                dialog.dismiss()
            } else {
                showDarkToast("Aucune sauvegarde dans le slot $slot")
            }
        }

        updateSlotDates(dialogView)

        // Chargement immédiat sur pression d'un bouton de slot
        for (slot in 1..5) {
            val loadBtn = dialogView.findViewById<Button>(resources.getIdentifier("slot${slot}_load", "id", packageName))
            if (loadBtn != null) {
                loadBtn.setOnClickListener {
                    loadFromSlot(slot)
                }
            }
            // Ajout du bouton de suppression pour chaque slot
            val deleteBtn = dialogView.findViewById<Button>(resources.getIdentifier("slot${slot}_delete", "id", packageName))
            if (deleteBtn != null) {
                val file = File(filesDir, "save_slot_${slot}.sav")
                deleteBtn.isEnabled = file.exists()
                deleteBtn.setOnClickListener {
                    AlertDialog.Builder(this)
                        .setTitle("Confirmation")
                        .setMessage("Supprimer la sauvegarde du slot $slot ? Cette action est irréversible.")
                        .setNegativeButton("Annuler", null)
                        .setPositiveButton("Supprimer") { _, _ ->
                            if (file.exists()) {
                                // Animation de fondu avant suppression
                                val containerId = resources.getIdentifier("slot${slot}_container", "id", packageName)
                                val container = dialogView.findViewById<View>(containerId)
                                container?.animate()?.alpha(0f)?.setDuration(200)?.withEndAction {
                                    file.delete()
                                    showDarkToast("Sauvegarde du slot $slot supprimée")
                                    updateSlotDates(dialogView)
                                    // Animation de fondu pour l'état vide
                                    container.alpha = 0f
                                    container.animate().alpha(1f).setDuration(200).start()
                                }?.start()
                            } else {
                                showDarkToast("Aucune sauvegarde à supprimer dans le slot $slot")
                                updateSlotDates(dialogView)
                            }
                            deleteBtn.isEnabled = file.exists()
                        }
                        .show()
                }
            }
        }

        // Gestion du bouton de chargement du quicksave ou de la sauvegarde la plus récente
        val slots = (0..5).map { File(filesDir, "save_slot_${it}.sav") }
        val lastFile = slots.filter { it.exists() }.maxByOrNull { it.lastModified() }
        if (quickLoadBtn != null) {
            if (lastFile != null) {
                quickLoadBtn.isEnabled = true
                quickLoadBtn.setOnClickListener {
                    if (!::retroView.isInitialized) return@setOnClickListener
                    val data = lastFile.readBytes()
                    val ok = retroView.unserializeState(data)
                    if (ok) {
                        showDarkToast("Sauvegarde la plus récente restaurée (slot ${lastFile.name.filter { it.isDigit() }})")
                    } else {
                        showDarkToast("Erreur lors de la restauration de la sauvegarde")
                    }
                    dialog.dismiss()
                }
            } else {
                quickLoadBtn.isEnabled = false
            }
        }

        // Suppression du quicksave (slot 0)
        val quickDeleteBtn2 = dialogView.findViewById<Button>(R.id.quick_delete)
        if (quickDeleteBtn2 != null) {
            quickDeleteBtn2.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Confirmation")
                    .setMessage("Supprimer le quicksave ? Cette action est irréversible.")
                    .setNegativeButton("Annuler", null)
                    .setPositiveButton("Supprimer") { _, _ ->
                        val file = File(filesDir, "save_slot_0.sav")
                        if (file.exists()) {
                            file.delete()
                            showDarkToast("Quicksave supprimé")
                        } else {
                            showDarkToast("Aucun quicksave à supprimer")
                        }
                        updateSlotDates(dialogView)
                    }
                    .show()
            }
        }

        dialog.show()
    }

    private fun showScreenshotPreviewDialog(screenshotFile: File?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_screenshot_preview, null)
        val imageView = dialogView.findViewById<ImageView>(R.id.screenshotPreviewImage)
        val btnClose = dialogView.findViewById<Button>(R.id.btnCloseScreenshotPreview)

        if (screenshotFile != null && screenshotFile.exists()) {
            val bmp = BitmapFactory.decodeFile(screenshotFile.absolutePath)
            imageView.setImageBitmap(bmp)
        } else {
            imageView.setImageResource(android.R.drawable.ic_delete)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun detectGameZone(bitmap: Bitmap): android.graphics.Rect {
        val width = bitmap.width
        val height = bitmap.height
        val gameZone = android.graphics.Rect()

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = bitmap.getPixel(x, y)
                if (pixel != Color.TRANSPARENT) {
                    gameZone.left = Math.min(gameZone.left, x)
                    gameZone.top = Math.min(gameZone.top, y)
                    gameZone.right = Math.max(gameZone.right, x)
                    gameZone.bottom = Math.max(gameZone.bottom, y)
                }
            }
        }

        return gameZone
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }
} 