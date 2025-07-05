package com.sweethome.launcher

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import android.view.View
import android.app.AlertDialog
import android.view.LayoutInflater
import android.widget.TextView
import android.util.Log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Affichage de l'image depuis les assets
        val imageView = findViewById<ImageView>(R.id.coverImage)
        assets.open("sweet_home_logo.png").use { inputStream ->
            val bitmap = BitmapFactory.decodeStream(inputStream)
            imageView.setImageBitmap(bitmap)
        }

        // Bouton pour lancer GameActivity
        val btnNewGame = findViewById<Button>(R.id.btnNewGame)
        val btnContinue = findViewById<Button>(R.id.btnContinue)
        val btnLoad = findViewById<Button>(R.id.btnLoad)
        val btnTestRom = findViewById<Button>(R.id.btnTestRom)

        // Vérifie la présence d'une auto-save (partie en cours)
        val hasAutoSave = File(filesDir, "auto_save_state.bin").exists()
        // Vérifie la présence d'une sauvegarde manuelle
        val hasSave = (File(filesDir, "save_slot_0.sav").exists() || (1..5).any { File(filesDir, "save_slot_${'$'}it.sav").exists() })

        btnContinue.isEnabled = true
        btnLoad.isEnabled = true

        btnNewGame.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Nouvelle partie")
                .setMessage("La partie en cours sera fermée. Les sauvegardes existantes ne seront pas supprimées. Voulez-vous vraiment commencer une nouvelle partie ?")
                .setNegativeButton("Annuler", null)
                .setPositiveButton("Commencer") { _, _ ->
                    val intent = Intent(this, GameActivity::class.java)
                    intent.putExtra("new_game", true)
                    startActivity(intent)
                }
                .show()
        }

        btnContinue.setOnClickListener {
            // Charge la dernière session (auto-save)
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("load_auto_save", true)
            startActivity(intent)
        }

        btnLoad.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("open_load_menu", true)
            startActivity(intent)
        }

        btnTestRom.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("test_rom", true)
            startActivity(intent)
        }

        // TEST: création d'un fichier pour vérifier le dossier filesDir
        val testFile = File(filesDir, "test_hello.txt")
        testFile.writeText("coucou")
        Log.d("DEBUG", "Test file écrit : ${testFile.absolutePath}")
    }
}
