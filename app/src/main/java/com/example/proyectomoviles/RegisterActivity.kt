package com.example.proyectomoviles

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity: AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        dbHelper = DatabaseHelper(this)
        val nameEditText: EditText = findViewById(R.id.nameEditText)
        val aliasEditText: EditText = findViewById(R.id.aliasEditText)
        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val registerButton: Button = findViewById(R.id.registerButton)

        registerButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val alias = aliasEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (name.isNotEmpty() && alias.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                val success = dbHelper.registerUser(name, alias, email, password)
                if (success) {
                    val users = dbHelper.getAllUsersDebug()
                    users.forEach {Log.d("DB_DEBUG", it) }
                    Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()
                    finish() // Regresar al login
                } else {
                    Toast.makeText(this, "Error al registrar el usuario", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }


}