package com.example.proyectomoviles

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditExchangeActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var exchangeId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_exchange)

        dbHelper = DatabaseHelper(this)
        exchangeId = intent.getStringExtra("EXCHANGE_ID")

        if (exchangeId != null) {
            loadExchangeDetails(exchangeId!!)
        }

        findViewById<Button>(R.id.saveChangesButton).setOnClickListener {
            saveChanges()
        }
    }

    private fun loadExchangeDetails(exchangeId: String) {
        val cursor = dbHelper.getExchangeById(exchangeId)
        if (cursor.moveToFirst()) {
            findViewById<EditText>(R.id.theme1EditText).setText(
                cursor.getString(cursor.getColumnIndexOrThrow("theme1"))
            )
            findViewById<EditText>(R.id.theme2EditText).setText(
                cursor.getString(cursor.getColumnIndexOrThrow("theme2"))
            )
            findViewById<EditText>(R.id.theme3EditText).setText(
                cursor.getString(cursor.getColumnIndexOrThrow("theme3"))
            )
            findViewById<EditText>(R.id.dateEditText).setText(
                cursor.getString(cursor.getColumnIndexOrThrow("date"))
            )
            findViewById<EditText>(R.id.locationEditText).setText(
                cursor.getString(cursor.getColumnIndexOrThrow("location"))
            )
        }
        cursor.close()
    }

    private fun saveChanges() {
        val theme1 = findViewById<EditText>(R.id.theme1EditText).text.toString()
        val theme2 = findViewById<EditText>(R.id.theme2EditText).text.toString()
        val theme3 = findViewById<EditText>(R.id.theme3EditText).text.toString()
        val date = findViewById<EditText>(R.id.dateEditText).text.toString()
        val location = findViewById<EditText>(R.id.locationEditText).text.toString()

        if (theme1.isNotEmpty() && date.isNotEmpty() && location.isNotEmpty()) {
            dbHelper.updateExchangeDetails(exchangeId!!, theme1, theme2, theme3, date, location)
            Toast.makeText(this, "Cambios guardados", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
        }
    }
}
