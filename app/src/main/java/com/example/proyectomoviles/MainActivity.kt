package com.example.proyectomoviles

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DatabaseHelper(this)
        // Verificar y realizar el sorteo automáticamente para todos los intercambios
        val exchanges = dbHelper.getAllExchangesWithDates()
        exchanges.forEach { exchange ->
            val exchangeId = exchange["id"] ?: return@forEach
            val exchangeDate = exchange["date"] ?: return@forEach

            checkAndPerformAutoDraw(exchangeId, exchangeDate)
        }
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun checkAndPerformAutoDraw(exchangeId: String, exchangeDate: String) {
        val currentDate = Calendar.getInstance()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val parsedExchangeDate = Calendar.getInstance().apply {
            time = sdf.parse(exchangeDate)!!
        }

        if (currentDate >= parsedExchangeDate) {
            val drawManager = DrawManager(this, dbHelper)
            drawManager.performDraw(exchangeId)
        }
    }

}