package com.example.proyectomoviles
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import android.telephony.SmsManager


class CreateExchangeActivity: AppCompatActivity() {

    private lateinit var participantsListLayout: LinearLayout
    private lateinit var dbHelper: DatabaseHelper
    private var exchangeId: Long = -1L
    private var participantCount = 0
    private val participantList = mutableListOf<Map<String, String>>() // Lista temporal de participantes
    private val participantListSMS = mutableListOf<Map<String, String>>()
    val SMS_PERMISSION_REQUEST_CODE = 101


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
                    participantList.add(mapOf("name" to name, "email" to email))
                    //addParticipantToDB(name,email)
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
        // Consultar los contactos
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ), // Solo seleccionamos las columnas necesarias
            null, null, null
        )

        val contactList = mutableListOf<Map<String, String>>() // Lista de mapas para almacenar nombre y número

        // Iterar sobre los contactos
        while (cursor?.moveToNext() == true) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))

            contactList.add(mapOf("name" to name, "number" to number)) // Guardar en la lista
        }
        cursor?.close()

        // Crear un arreglo con los nombres para mostrar en el diálogo
        val contactNames = contactList.map { it["name"] ?: "Desconocido" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Seleccionar Contacto")
            .setItems(contactNames) { _, which ->
                val selectedContact = contactList[which]
                val selectedName = selectedContact["name"] ?: "Desconocido"
                val selectedNumber = selectedContact["number"] ?: ""

                // Agregar el contacto seleccionado a la lista temporal local
                addParticipantToList(selectedName)
                participantListSMS.add(mapOf("name" to selectedName, "number" to selectedNumber))
            }
            .show()
    }

    // Agregar participante a la base de datos y a la lista visual
    private fun addParticipantToDB(id: Long, name: String, email: String) {
        if (id != -1L) {
            dbHelper.addParticipant(id, name, email)
            //sendEmailInvitation(email, "Intercambio Navideño", "https://example.com/confirm?email=$email")
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
            setTextColor(getColor(R.color.black)) // Define el color
        }
        participantsListLayout.addView(participantTextView)
    }

    private fun saveExchange() {
        println("Hola mundo")
        val theme1 = findViewById<EditText>(R.id.themeEditText1).text.toString()
        val theme2 = findViewById<EditText>(R.id.themeEditText2).text.toString()
        val theme3 = findViewById<EditText>(R.id.themeEditText3).text.toString()
        val date = findViewById<EditText>(R.id.exchangeDateEditText).text.toString()
        val max_amount = findViewById<EditText>(R.id.amountEditText).text.toString()
        val hour = findViewById<EditText>(R.id.exchangeTimeEditText).text.toString()
        val deadlinedate = findViewById<EditText>(R.id.registrationDeadlineEditText).text.toString()
        val location = findViewById<EditText>(R.id.exchangeLocationEditText).text.toString()
        val additional = findViewById<EditText>(R.id.additionalDetailsEditText).text.toString()


        if (theme1.isNotEmpty() && date.isNotEmpty() && location.isNotEmpty()) {
            val userId = getLoggedInUserId()
            if (userId == -1L) {
                Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
                return
            }
            val exchangeId = dbHelper.addExchange(theme1, theme2, theme3,max_amount,deadlinedate,
                hour,date, location, additional,userId)
            if (exchangeId != -1L) {
                for (participant in participantList) {
                    val name = participant["name"] ?: continue
                    val email = participant["email"] ?: continue
                    addParticipantToDB(exchangeId, name, email)
                }
                for (participant in participantListSMS){
                    val name = participant["name"] ?: continue
                    val phoneNumber = participant["number"] ?: continue
                    addParticipantToDB(exchangeId,name,phoneNumber)
                }
                Toast.makeText(this, "Intercambio creado exitosamente", Toast.LENGTH_SHORT).show()
                val code = dbHelper.getExchangeCode(exchangeId)
                sendInvitationsToParticipants(exchangeId, code)
                Toast.makeText(this, "Código del intercambio: $code", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, ExchangeListActivity::class.java))
                finish()
            }
        } else {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    // Método para enviar correos electrónicos a todos los participantes
    private fun sendInvitationsToParticipants(exchangeId: Long, code: String) {
        val participants = dbHelper.getParticipantsForExchange(exchangeId)
        for (participant in participants) {
            println(participant)
            val contacto = participant["email"] ?: continue
            if (contacto.contains("@")) {
                sendEmailInvitation(contacto, code)
            } else {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    sendSmsInvitation(contacto, code)
                } else {
                    checkAndRequestSmsPermission()
                }
            }
            }
    }

    // Método para enviar el correo electrónico
    private fun sendEmailInvitation(email: String, code: String) {
        val subject = "Invitación al intercambio:"
        val body = "¡Has sido invitado al intercambio de !\n\n" +
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

    fun sendSmsInvitation(phoneNumber: String, code: String) {
        val message = "¡Has sido invitado a un intercambio!, el código de acceso es $code"
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Toast.makeText(this, "SMS enviado a $phoneNumber", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al enviar SMS a $phoneNumber", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getLoggedInUserId(): Long {
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        return sharedPreferences.getLong("loggedInUserId", -1) // -1 si no está definido
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

    private fun checkAndRequestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS),
                101
            )
        }
    }

}