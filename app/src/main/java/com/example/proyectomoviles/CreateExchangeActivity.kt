package com.example.proyectomoviles
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CreateExchangeActivity: AppCompatActivity() {

    private lateinit var participantsListLayout: LinearLayout
    private var participantCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_exchange)

        // Referencias a los elementos
        participantsListLayout = findViewById(R.id.participantsListLayout)
        val addParticipantButton: Button = findViewById(R.id.addParticipantButton)
        val saveExchangeButton: Button = findViewById(R.id.saveExchangeButton)

        // Agregar nuevo participante
        addParticipantButton.setOnClickListener {
            addParticipant()
        }

        // Guardar intercambio
        saveExchangeButton.setOnClickListener {
            saveExchange()
        }
    }

    private fun addParticipant() {
        participantCount++

        // Crear un TextView para el nuevo participante
        val participantTextView = TextView(this).apply {
            text = "Participante $participantCount"
            textSize = 16f
            setPadding(8, 8, 8, 8)
        }

        // Agregarlo al LinearLayout
        participantsListLayout.addView(participantTextView)
    }

    private fun saveExchange() {
        Toast.makeText(this, "Intercambio guardado correctamente", Toast.LENGTH_SHORT).show()
        // Aquí puedes agregar la lógica para guardar en la base de datos
        finish() // Cierra la actividad
    }
}