package com.example.proyectomoviles

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class DrawManager(private val context: Context, private val dbHelper: DatabaseHelper) {

    fun performDraw(exchangeId: String) {
        val participants = dbHelper.getAcceptedParticipants(exchangeId)

        if (participants.size < 2) {
            Toast.makeText(context, "No hay suficientes participantes para realizar el sorteo", Toast.LENGTH_SHORT).show()
            return
        }

        val shuffledParticipants = participants.shuffled()
        val assignments = mutableMapOf<Map<String, String>, Map<String, String>>()

        for (i in participants.indices) {
            val giver = shuffledParticipants[i]
            val receiver = shuffledParticipants[(i + 1) % shuffledParticipants.size]
            assignments[giver] = receiver
        }

        sendNotifications(assignments)
        Toast.makeText(context, "El sorteo se realizó correctamente", Toast.LENGTH_SHORT).show()
    }



    fun sendEmail(email: String, subject: String, body: String) {
        val sender = GmailSender("ulises21.uligom@gmail.com", "syuq fyri zgyv svba")
        Thread {
            val success = sender.sendEmail(email, subject, body)
            (context as? Activity)?.runOnUiThread {
                if (success) {
                    Toast.makeText(context, "Correo enviado a $email", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Error al enviar el correo a $email", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    fun sendSms(phoneNumber: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Toast.makeText(context, "SMS enviado a $phoneNumber", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al enviar SMS a $phoneNumber", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendNotifications(assignments: Map<Map<String, String>, Map<String, String>>) {
        for ((giver, receiver) in assignments) {
            val giverName = giver["name"] ?: "Sin Nombre"
            val giverEmailOrPhone = giver["email"] ?: giver["number"] ?: ""
            val receiverName = receiver["name"] ?: "Sin Nombre"
            val giverTheme = giver["selected_theme"] ?: "No seleccionado"

            val message = "¡Hola $giverName! Debes darle un regalo a $receiverName en el intercambio.\n" +
                    "¡No se lo digas a nadie!, Recuerda que elegiste regalar $giverTheme"

            if (giverEmailOrPhone.contains("@")) {
                sendEmail(giverEmailOrPhone, "Asignación de participante para intercambio", message)
            } else {
                sendSms(giverEmailOrPhone, message)
            }
        }
    }


}
