package com.example.proyectomoviles
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class CreateExchangeActivity: AppCompatActivity() {

    private lateinit var participantsListLayout: LinearLayout
    private lateinit var dbHelper: DatabaseHelper
    private var exchangeId: Long = -1L
    private var participantCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_exchange)
        dbHelper = DatabaseHelper(this)
        // Referencias a los elementos
        participantsListLayout = findViewById(R.id.participantsListLayout)
        val addParticipantButton: Button = findViewById(R.id.addParticipantButton)
        val saveExchangeButton: Button = findViewById(R.id.saveExchangeButton)

        // Agregar nuevo participante
        addParticipantButton.setOnClickListener {
            //addParticipant()
            showAddParticipantDialog()
        }

        // Guardar intercambio
        saveExchangeButton.setOnClickListener {
            saveExchange()
        }
    }

    private fun showAddParticipantDialog() {
        val options = arrayOf("Agregar manualmente", "Seleccionar desde contactos")

        AlertDialog.Builder(this)
            .setTitle("Agregar Participante")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> addParticipantManually()
                    1 -> checkContactPermission()
                }
            }
            .show()
    }

    private fun addParticipantManually() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10) // Padding opcional para mejorar la presentación
        }

        val nameInput = EditText(this).apply {
            hint = "Nombre del participante"
        }

        val emailInput = EditText(this).apply {
            hint = "Correo Electronico"
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }

        layout.addView(nameInput)
        layout.addView(emailInput)

        AlertDialog.Builder(this)
            .setTitle("Agregar Participante")
            .setView(layout)
            .setPositiveButton("Agregar") { _, _ ->
                val name = nameInput.text.toString()
                val email = emailInput.text.toString()

                if (name.isNotEmpty() && email.isNotEmpty()) {
                    addParticipantToList(name)
                    addParticipantToDB(name,email)
                } else {
                    Toast.makeText(this, "Debe llenar ambos campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun checkContactPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), 100)
        } else {
            selectFromContacts()
        }
    }

    private fun selectFromContacts() {
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, null
        )

        val contactList = mutableListOf<String>()
        while (cursor?.moveToNext() == true) {
            val name =
                cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            contactList.add(name)
        }
        cursor?.close()

        val contactsArray = contactList.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Seleccionar Contacto")
            .setItems(contactsArray) { _, which ->
                addParticipantToList(contactsArray[which])
            }
            .show()
    }

    // Agregar participante a la base de datos y a la lista visual
    private fun addParticipantToDB(name: String, email: String) {
        if (exchangeId != -1L) {
            dbHelper.addParticipant(exchangeId, name, email)
            sendEmailInvitation(email, "Intercambio Navideño", "https://example.com/confirm?email=$email")
        } else {
            Toast.makeText(this, "Primero guarda el intercambio", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addParticipantToList(name: String) {
        participantCount++

        val participantTextView = TextView(this).apply {
            text = "$name"
            textSize = 16f
            setPadding(8, 8, 8, 8)
        }
        participantsListLayout.addView(participantTextView)
    }

    private fun saveExchange() {
        val theme1 = findViewById<EditText>(R.id.themeEditText1).text.toString()
        val theme2 = findViewById<EditText>(R.id.themeEditText2).text.toString()
        val theme3 = findViewById<EditText>(R.id.themeEditText3).text.toString()
        val date = findViewById<EditText>(R.id.exchangeDateEditText).text.toString()
        val location = findViewById<EditText>(R.id.exchangeLocationEditText).text.toString()

        if (theme1.isNotEmpty() && date.isNotEmpty() && location.isNotEmpty()) {
            val exchangeId = dbHelper.addExchange(
                "Intercambio Navideño", theme1, theme2, theme3, date, location, 1
            )
            if (exchangeId != -1L) {
                Toast.makeText(this, "Intercambio creado exitosamente", Toast.LENGTH_SHORT).show()
                val code = dbHelper.getExchangeCode(exchangeId)
                sendInvitationsToParticipants(exchangeId, code)
                Toast.makeText(this, "Código del intercambio: $code", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    // Método para enviar correos electrónicos a todos los participantes
    private fun sendInvitationsToParticipants(exchangeId: Long, code: String) {
        val participants = dbHelper.getParticipantsForExchange(exchangeId)
        val exchangeName = "Intercambio Navideño"

        for (participant in participants) {
            val email = participant["email"] ?: continue
            sendEmailInvitation(email, exchangeName, code)
        }
    }

    // Método para enviar el correo electrónico
    private fun sendEmailInvitation(email: String, exchangeName: String, code: String) {
        val subject = "Invitación al intercambio: $exchangeName"
        val body = "¡Has sido invitado al intercambio '$exchangeName'!\n\n" +
                "Código de acceso: $code\n" +
                "Ingresa este código en la app para confirmar tu participación.\n\n" +
                "¡Esperamos verte pronto!"

        // Configura el emisor (una cuenta de Gmail)
        val sender = GmailSender("ulises21.uligom@gmail.com", "syuq fyri zgyv svba")

        // Enviar el correo en un hilo de background
        Thread {
            val success = sender.sendEmail(email, subject, body)
            runOnUiThread {
                if (success) {
                    Toast.makeText(this, "Correo enviado a $email", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al enviar el correo a $email", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }



    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            selectFromContacts()
        } else {
            Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
        }
    }

}