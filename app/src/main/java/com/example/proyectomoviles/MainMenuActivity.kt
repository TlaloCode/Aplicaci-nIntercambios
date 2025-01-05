package com.example.proyectomoviles

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainMenuActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        val createExchangeButton: Button = findViewById(R.id.createExchangeButton)
        val listExchangesButton: Button = findViewById(R.id.listExchangesButton)
        val advanceDateButton: Button = findViewById(R.id.advanceDateButton)
        val joinExchangeButton: Button = findViewById(R.id.enterExchangeButton)

        // Navegar a la actividad para crear intercambio
        createExchangeButton.setOnClickListener {
            val intent = Intent(this, CreateExchangeActivity::class.java)
            startActivity(intent)
        }

        // Navegar a la actividad para listar intercambios
        listExchangesButton.setOnClickListener {
            val intent = Intent(this, ExchangeListActivity::class.java)
            startActivity(intent)
        }

        joinExchangeButton.setOnClickListener {
            val intent = Intent(this, JoinExchangeActivity::class.java)
            startActivity(intent)
        }
/*
        // Navegar a la actividad para adelantar fecha del sorteo
        advanceDateButton.setOnClickListener {
            val intent = Intent(this, AdvanceDateActivity::class.java)
            startActivity(intent)
        }*/
    }
}