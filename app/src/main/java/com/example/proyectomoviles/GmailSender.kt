package com.example.proyectomoviles

import java.util.Properties
import javax.activation.DataHandler
import javax.activation.DataSource
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class GmailSender(private val username: String, private val password: String) {

    fun sendEmail(to: String, subject: String, body: String): Boolean {
        return try {
            val props = Properties()
            props["mail.smtp.host"] = "smtp.gmail.com"
            props["mail.smtp.socketFactory.port"] = "465"
            props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
            props["mail.smtp.auth"] = "true"
            props["mail.smtp.port"] = "465"

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(username, password)
                }
            })

            val message = MimeMessage(session)
            message.setFrom(InternetAddress(username)) // Emisor
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to)) // Receptor
            message.subject = subject
            message.setText(body)

            Transport.send(message)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
