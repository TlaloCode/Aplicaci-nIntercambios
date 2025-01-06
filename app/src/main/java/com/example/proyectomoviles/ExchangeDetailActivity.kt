package com.example.proyectomoviles

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
            val drawManager = DrawManager(this, dbHelper)
            drawManager.performDraw(exchangeId)
        }

        findViewById<Button>(R.id.deleteButton).setOnClickListener {
            deleteExchange(exchangeId)
        }

        findViewById<Button>(R.id.addParticipantButton).setOnClickListener {
            if (verifyUserPermissions(exchangeId)) {
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle("Agregar Participante")

                val layout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(16, 16, 16, 16)
                }

                val nameInput = EditText(this).apply { hint = "Nombre" }
                val emailInput = EditText(this).apply { hint = "Correo Electrónico" }

                layout.addView(nameInput)
                layout.addView(emailInput)
                dialog.setView(layout)

                dialog.setPositiveButton("Agregar") { _, _ ->
                    val name = nameInput.text.toString().trim()
                    val email = emailInput.text.toString().trim()

                    if (name.isNotEmpty() && email.isNotEmpty()) {
                        dbHelper.addParticipant(exchangeId!!.toLong(), name, email)
                        Toast.makeText(this, "Participante agregado", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Completa los campos", Toast.LENGTH_SHORT).show()
                    }
                }

                dialog.setNegativeButton("Cancelar", null)
                dialog.show()
            } else {
                Toast.makeText(
                    this,
                    "Solo el creador puede agregar participantes",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        findViewById<Button>(R.id.deleteParticipantButton).setOnClickListener {
            if (verifyUserPermissions(exchangeId)) {
                val participants = dbHelper.getParticipantsForExchange(exchangeId!!.toLong())
                val participantNames = participants.map { it["name"]!! }.toTypedArray()

                val dialog = AlertDialog.Builder(this)
                dialog.setTitle("Eliminar Participante")
                dialog.setItems(participantNames) { _, which ->
                    val participant = participants[which]
                    dbHelper.deleteParticipant(participant["email"]!!)
                    Toast.makeText(this, "Participante eliminado", Toast.LENGTH_SHORT).show()
                }
                dialog.show()
            } else {
                Toast.makeText(this, "Solo el creador puede eliminar participantes", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.editExchangeButton).setOnClickListener {
            if (verifyUserPermissions(exchangeId)) {
                val intent = Intent(this, EditExchangeActivity::class.java)
                intent.putExtra("EXCHANGE_ID", exchangeId)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Solo el creador puede editar este intercambio", Toast.LENGTH_SHORT).show()
            }
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
        println(participants)
        val acceptedList = participants.filter { it["status"] == "aceptado" }
        val pendingList = participants.filter { it["status"] == "pendiente" }
        val rejectedList = participants.filter { it["status"] == "rechazado" }

        findViewById<TextView>(R.id.acceptedParticipantsTextView).text =
            "Aceptados:\n" + acceptedList.joinToString("\n") { it["name"] ?: "Sin Nombre" }

        findViewById<TextView>(R.id.pendingParticipantsTextView).text =
            "Pendientes:\n" + pendingList.joinToString("\n") { it["name"] ?: "Sin Nombre" }
        findViewById<TextView>(R.id.pendingRejectedTextView).text =
        "No participarán:\n" + rejectedList.joinToString("\n") { it["name"] ?: "Sin Nombre" }
    }

    private fun verifyUserPermissions(exchangeId: String): Boolean {
        val currentUserId = getCurrentUserId()
        if(currentUserId != null)
        {
            return dbHelper.isUserCreatorOfExchange(currentUserId, exchangeId!!)
        }
        else{
            return false
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
