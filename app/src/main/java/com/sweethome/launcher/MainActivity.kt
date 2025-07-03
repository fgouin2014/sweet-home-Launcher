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

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Affichage de l'image depuis les assets
        val imageView = findViewById<ImageView>(R.id.coverImage)
        try {
            assets.open("sweet_home_logo.png").use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imageView.setImageBitmap(bitmap)
            }
        } catch (e: Exception) {
            // Si l'image n'est pas trouvée, rien n'est affiché
        }

        // Bouton pour lancer GameActivity
        val btnPlay = findViewById<Button>(R.id.btnPlay)
        val btnLoad = findViewById<Button>(R.id.btnLoad)

        // Vérifie la présence d'une sauvegarde (quicksave ou slot 1-5)
        val hasSave = (File(filesDir, "save_slot_0.sav").exists() ||
            (1..5).any { File(filesDir, "save_slot_${'$'}it.sav").exists() })

        if (hasSave) {
            btnLoad.visibility = View.VISIBLE
            btnPlay.text = "Jouer à Sweet Home (Nouvelle partie)"
        } else {
            btnLoad.visibility = View.GONE
            btnPlay.text = "Jouer à Sweet Home"
        }

        btnPlay.setOnClickListener {
            if (hasSave) {
                AlertDialog.Builder(this)
                    .setTitle("Nouvelle partie")
                    .setMessage("Toutes les sauvegardes existantes seront supprimées. Voulez-vous vraiment commencer une nouvelle partie ?")
                    .setNegativeButton("Annuler", null)
                    .setPositiveButton("Confirmer") { _, _ ->
                        val intent = Intent(this, GameActivity::class.java)
                        startActivity(intent)
                    }
                    .show()
            } else {
                val intent = Intent(this, GameActivity::class.java)
                startActivity(intent)
            }
        }
        btnLoad.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("open_load_menu", true)
            android.util.Log.d("MainActivity", "Lancement GameActivity avec open_load_menu=true")
            startActivity(intent)
        }
    }
}
