package com.example.proyectomoviles

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ConfirmExchangeActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var exchangeId: String? = null
    private var selectedTheme: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_exchange)

        dbHelper = DatabaseHelper(this)
        exchangeId = intent.getStringExtra("EXCHANGE_ID")

        loadExchangeDetails()
        setupThemeSpinner()
        setupButtons()
    }

    private fun loadExchangeDetails() {
        if (exchangeId == null) return

        val cursor = dbHelper.getExchangeById(exchangeId!!)
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

            findViewById<TextView>(R.id.exchangeDetailsTextView).text =
                "Intercambio #$code\nTemática 1: $theme1\nTemática 2: $theme2\nTemática 3: $theme3\n" +
                        "Monto maximo por regalo: $amount\nFecha Limite para registrarse: $deadline\n" +
                        "Hora del intermcabio: $hour\nFecha del intercambio: $date\nLugar: $location\n" +
                        "Datos adicionales: $data"
        }
        cursor.close()

        val participants = dbHelper.getParticipantsByExchangeId(exchangeId!!)
        findViewById<TextView>(R.id.participantsTextView).text =
            "Participantes:\n" + participants.joinToString("\n") { it["name"] ?: "Sin Nombre" }
    }

    private fun setupThemeSpinner() {
        if (exchangeId == null) return

        // Obtener los temas de regalo desde la base de datos
        val themes = dbHelper.getThemesByExchangeId(exchangeId!!)
        val spinner = findViewById<Spinner>(R.id.themeSpinner)


        // Configurar el adaptador del Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, themes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Obtener la selección del usuario
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedTheme = themes[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedTheme = null
            }
        }
    }


    private fun setupButtons() {
        findViewById<Button>(R.id.confirmParticipationButton).setOnClickListener {
            val currentUserEmail = getCurrentUserId() // Recupera el correo del usuario actual
            println("El usuario actual es: $currentUserEmail")
            if (currentUserEmail != null) {
                val email = dbHelper.getParticipantEmailById(currentUserEmail)
                println("El email recuperado es: $email")
                dbHelper.confirmParticipation(email.toString())
                Toast.makeText(this, "Has aceptado el intercambio", Toast.LENGTH_SHORT).show()
                dbHelper.close() // Cierra la conexión a la base de datos
                finish()
            } else {
                Toast.makeText(this, "Error al confirmar participación", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.declineParticipationButton).setOnClickListener {
            declineParticipation()
        }
    }

    private fun declineParticipation() {
        val currentUserEmail = getCurrentUserId() // Recupera el correo del usuario actual
        if (currentUserEmail != null) {
            val email = dbHelper.getParticipantEmailById(currentUserEmail)
            println("El email recuperado es: $email")
            dbHelper.declineParticipation(email.toString())
            Toast.makeText(this, "Has declinado el intercambio", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error al declinar participación", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentUserId(): Long? {
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        return if (sharedPreferences.contains("loggedInUserId")) {
            sharedPreferences.getLong("loggedInUserId", -1L).takeIf { it != -1L }
        } else {
            null
        }
    }


}
