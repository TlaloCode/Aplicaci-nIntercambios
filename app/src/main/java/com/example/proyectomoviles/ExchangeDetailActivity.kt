package com.example.proyectomoviles

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ExchangeDetailActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exchange_detail)
        dbHelper = DatabaseHelper(this)
        val exchangeId = intent.getStringExtra("EXCHANGE_ID") ?: "0"

        // Cargar los detalles del intercambio
        println("El id del intercambio es: $exchangeId")
        loadExchangeDetails(exchangeId)
        // Configurar botones
        findViewById<Button>(R.id.acceptButton).setOnClickListener {
            finish() // Regresar a la lista de intercambios
        }

        findViewById<Button>(R.id.advanceDateButton).setOnClickListener {
            Toast.makeText(this, "Funcionalidad pendiente de implementar", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.deleteButton).setOnClickListener {
            deleteExchange(exchangeId)
        }
        loadParticipants(exchangeId)
    }

    private fun loadExchangeDetails(exchangeId: String) {
        val cursor = dbHelper.getExchangeById(exchangeId)
        if (cursor.moveToFirst()) {
            val code = cursor.getString(cursor.getColumnIndexOrThrow("code"))
            println("El codigo es $code")
            val theme1 = cursor.getString(cursor.getColumnIndexOrThrow("theme1"))
            val theme2 = cursor.getString(cursor.getColumnIndexOrThrow("theme2"))
            val theme3 = cursor.getString(cursor.getColumnIndexOrThrow("theme3"))
            val amount = cursor.getString(cursor.getColumnIndexOrThrow("amount"))
            val deadline = cursor.getString(cursor.getColumnIndexOrThrow("deadline"))
            val hour = cursor.getString(cursor.getColumnIndexOrThrow("hour"))
            val date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
            val data = cursor.getString(cursor.getColumnIndexOrThrow("data"))
            val location = cursor.getString(cursor.getColumnIndexOrThrow("location"))

            findViewById<TextView>(R.id.exchangeCodeTextView).text = "Código del intercambio: $code"
            findViewById<TextView>(R.id.exchangeTheme1TextView).text = "Temática 1: $theme1"
            findViewById<TextView>(R.id.exchangeTheme2TextView).text = "Temática 2: $theme2"
            findViewById<TextView>(R.id.exchangeTheme3TextView).text = "Temática 3: $theme3"
            findViewById<TextView>(R.id.exchangeAmountTextView).text = "Monto máximo por regalo: $amount"
            findViewById<TextView>(R.id.exchangeDeadlineTextView).text = "Fecha limite de registro: $deadline"
            findViewById<TextView>(R.id.exchangeHourTextView).text = "Hora: $hour"
            findViewById<TextView>(R.id.exchangeDateTextView).text = "Fecha: $date"
            findViewById<TextView>(R.id.exchangeDataTextView).text = "Datos adicionales: $data"
            findViewById<TextView>(R.id.exchangeLocationTextView).text = "Lugar: $location"
        }
        cursor.close()
    }

    private fun loadParticipants(exchangeId: String) {
        val participants = dbHelper.getParticipantsByExchangeId(exchangeId)

        val acceptedList = participants.filter { it["status"] == "aceptado" }
        val pendingList = participants.filter { it["status"] == "pendiente" }

        findViewById<TextView>(R.id.acceptedParticipantsTextView).text =
            "Aceptados:\n" + acceptedList.joinToString("\n") { it["name"] ?: "Sin Nombre" }

        findViewById<TextView>(R.id.pendingParticipantsTextView).text =
            "Pendientes:\n" + pendingList.joinToString("\n") { it["name"] ?: "Sin Nombre" }
    }


    private fun deleteExchange(exchangeId: String) {
        if (exchangeId != null) {
            dbHelper.deleteExchange(exchangeId!!)
            Toast.makeText(this, "Intercambio eliminado", Toast.LENGTH_SHORT).show()
            finish() // Regresar a la lista después de eliminar
        } else {
            Toast.makeText(this, "Error al eliminar el intercambio", Toast.LENGTH_SHORT).show()
        }
    }
}
