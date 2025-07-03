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

class GameActivity : AppCompatActivity() {
    private lateinit var retroView: GLRetroView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val frame = findViewById<ConstraintLayout>(R.id.frame)
        val btnCloseMenu = findViewById<Button>(R.id.btnCloseMenu)
        val btnMenuIcon = findViewById<ImageButton>(R.id.btnMenuIcon)

        val abi = getDeviceAbi()
        val coreName = "fceumm_libretro_android.so" // ou choisis dynamiquement selon le core voulu
        val assetPath = "cores/$abi/$coreName"
        val coreFile = copyAssetToInternalStorage(assetPath)
        val romFile = copyAssetToInternalStorage("roms/sweet_home.nes")

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

        // Restauration automatique de l'état du jeu après un changement d'orientation
        savedInstanceState?.getByteArray("game_save_state")?.let { saveData ->
            retroView.post {
                retroView.unserializeState(saveData)
            }
        } ?: run {
            // Sinon, restauration auto depuis le fichier si présent
            val saveFile = File(filesDir, "auto_save_state.bin")
            if (saveFile.exists()) {
                val loadedData = saveFile.readBytes()
                retroView.post {
                    retroView.unserializeState(loadedData)
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
                if (drawerLayout.isDrawerOpen(Gravity.END)) {
                    drawerLayout.closeDrawer(Gravity.END)
                } else {
                    finish()
                }
            }
        })

        btnMenuIcon.setOnLongClickListener {
            drawerLayout.openDrawer(Gravity.END)
            true
        }
        btnMenuIcon.setOnClickListener { /* rien sur simple clic */ }

        btnCloseMenu.setOnClickListener {
            drawerLayout.closeDrawer(Gravity.END)
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
} 