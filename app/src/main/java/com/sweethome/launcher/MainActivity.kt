package com.sweethome.launcher

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

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
        findViewById<Button>(R.id.btnPlay).setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }
    }
}
