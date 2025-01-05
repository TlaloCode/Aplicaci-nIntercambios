package com.example.proyectomoviles

import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class JoinExchangeActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_exchange)

        dbHelper = DatabaseHelper(this)

        val codeEditText = findViewById<EditText>(R.id.codeEditText)
        val confirmButton = findViewById<Button>(R.id.confirmButton)

        confirmButton.setOnClickListener {
            val code = codeEditText.text.toString().trim()

            if (code.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa un código", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val exchangeId = dbHelper.getExchangeIdByCode(code)
            if (exchangeId != null) {
                val intent = Intent(this, ConfirmExchangeActivity::class.java)
                intent.putExtra("EXCHANGE_ID", exchangeId)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Código inválido", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
