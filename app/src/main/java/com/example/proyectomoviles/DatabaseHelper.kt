package com.example.proyectomoviles

import android.content.ContentValues
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
class DatabaseHelper(context:Context):
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "QueridoSanta.db"
        private const val DATABASE_VERSION = 5

        // Tabla de usuarios
        const val TABLE_USERS = "users"
        const val COLUMN_USER_ID = "id"
        const val COLUMN_USER_NAME = "name"
        const val COLUMN_USER_ALIAS = "alias"
        const val COLUMN_USER_EMAIL = "email"
        const val COLUMN_USER_PASSWORD = "password"

        // Tabla de intercambios
        const val TABLE_EXCHANGES = "exchanges"
        const val COLUMN_EXCHANGE_ID = "id"
        const val COLUMN_EXCHANGE_THEME1 = "theme1"
        const val COLUMN_EXCHANGE_THEME2 = "theme2"
        const val COLUMN_EXCHANGE_THEME3 = "theme3"
        const val COLUMN_EXCHANGE_AMOUNT = "amount"
        const val COLUMN_EXCHANGE_DEADLINE = "deadline"
        const val COLUMN_EXCHANGE_HOUR = "hour"
        const val COLUMN_EXCHANGE_DATE = "date"
        const val COLUMN_EXCHANGE_LOCATION = "location"
        const val COLUMN_EXCHANGE_ADDITIONAL = "data"
        const val COLUMN_EXCHANGE_CODE  = "code" // Código único del participante
        const val COLUMN_EXCHANGE_DRAW_DONE = "draw_done"
        const val COLUMN_USER_ID_FK = "user_id"

        // Tabla de participantes
        const val TABLE_PARTICIPANTS = "participants"
        const val COLUMN_PARTICIPANT_ID = "id"
        const val COLUMN_EXCHANGE_ID_FK = "exchange_id"
        const val COLUMN_PARTICIPANT_NAME = "name"
        const val COLUMN_PARTICIPANT_EMAIL = "email"
        const val COLUMN_PARTICIPANT_STATUS = "status"
        const val COLUMN_PARTICIPANT_SELECTED_THEME = "selected_theme"
    }


    override fun onCreate(db: SQLiteDatabase) {
        // Crear tabla de usuarios
        val CREATE_USERS_TABLE = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_NAME TEXT NOT NULL,
                $COLUMN_USER_ALIAS TEXT NOT NULL,
                $COLUMN_USER_EMAIL TEXT NOT NULL UNIQUE,
                $COLUMN_USER_PASSWORD TEXT NOT NULL
            )
        """
        val CREATE_EXCHANGES_TABLE = """
            CREATE TABLE $TABLE_EXCHANGES (
                $COLUMN_EXCHANGE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_EXCHANGE_THEME1 TEXT,
                $COLUMN_EXCHANGE_THEME2 TEXT,
                $COLUMN_EXCHANGE_THEME3 TEXT,
                $COLUMN_EXCHANGE_AMOUNT TEXT,
                $COLUMN_EXCHANGE_DEADLINE TEXT,
                $COLUMN_EXCHANGE_HOUR TEXT,
                $COLUMN_EXCHANGE_DATE TEXT,
                $COLUMN_EXCHANGE_LOCATION TEXT,
                $COLUMN_EXCHANGE_ADDITIONAL TEXT,
                $COLUMN_USER_ID_FK INTEGER,
                $COLUMN_EXCHANGE_DRAW_DONE INTEGER DEFAULT 0,
                $COLUMN_EXCHANGE_CODE TEXT UNIQUE NOT NULL,
                FOREIGN KEY ($COLUMN_USER_ID_FK) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
            )
        """
        val CREATE_PARTICIPANTS_TABLE = """
            CREATE TABLE $TABLE_PARTICIPANTS (
                $COLUMN_PARTICIPANT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_EXCHANGE_ID_FK INTEGER,
                $COLUMN_PARTICIPANT_NAME TEXT,
                $COLUMN_PARTICIPANT_EMAIL TEXT,
                $COLUMN_PARTICIPANT_STATUS TEXT DEFAULT 'pendiente',
                $COLUMN_PARTICIPANT_SELECTED_THEME TEXT DEFAULT 'nada',
                FOREIGN KEY ($COLUMN_EXCHANGE_ID_FK) REFERENCES $TABLE_EXCHANGES($COLUMN_EXCHANGE_ID)
            )
        """
        db.execSQL(CREATE_USERS_TABLE)
        db.execSQL(CREATE_EXCHANGES_TABLE)
        db.execSQL(CREATE_PARTICIPANTS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Si la base de datos cambia de versión, eliminamos y recreamos las tablas
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EXCHANGES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PARTICIPANTS")
        onCreate(db)
    }

    // Método para registrar un usuario
    fun registerUser(name: String, alias: String, email: String, password: String): Boolean {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_USER_NAME, name)
        values.put(COLUMN_USER_ALIAS, alias)
        values.put(COLUMN_USER_EMAIL, email)
        values.put(COLUMN_USER_PASSWORD, password)

        val result = db.insert(TABLE_USERS, null, values)
        println("Se ingresó usuario $values")
        db.close()
        return result != -1L
    }

    // Método para crear un intercambio
    fun addExchange(theme1: String, theme2: String, theme3: String, amount: String,deadline:String,
                    hour:String, date: String, location: String, data: String, userId: Long): Long {
        val db = writableDatabase
        val exchangeCode = generateExchangeCode()
        val values = ContentValues().apply {
            put(COLUMN_EXCHANGE_THEME1, theme1)
            put(COLUMN_EXCHANGE_THEME2, theme2)
            put(COLUMN_EXCHANGE_THEME3, theme3)
            put(COLUMN_EXCHANGE_AMOUNT,amount)
            put(COLUMN_EXCHANGE_DEADLINE,deadline)
            put(COLUMN_EXCHANGE_HOUR,hour)
            put(COLUMN_EXCHANGE_DATE, date)
            put(COLUMN_EXCHANGE_LOCATION, location)
            put(COLUMN_EXCHANGE_ADDITIONAL,data)
            put(COLUMN_EXCHANGE_CODE, exchangeCode)
            put(COLUMN_USER_ID_FK, userId)
        }
        val id = db.insert(TABLE_EXCHANGES, null, values)
        db.close()
        return id
    }

    // Método para validar usuario
    fun validateUser(email: String, password: String): Long {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_USER_ID FROM $TABLE_USERS WHERE $COLUMN_USER_EMAIL = ? AND $COLUMN_USER_PASSWORD = ?",
            arrayOf(email, password)
        )

        val userId = if (cursor.moveToFirst()) {
            cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)) // Obtener el ID del usuario
        } else {
            -1L // Usuario no encontrado
        }

        cursor.close()
        db.close()
        return userId
    }

    fun addParticipant(exchangeId: Long, name: String, email: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EXCHANGE_ID_FK, exchangeId)
            put(COLUMN_PARTICIPANT_NAME, name)
            put(COLUMN_PARTICIPANT_EMAIL, email)
            put(COLUMN_PARTICIPANT_STATUS, "pendiente")
        }
        val id = db.insert(TABLE_PARTICIPANTS, null, values)
        db.close()
        return id
    }

    // Método para confirmar la participación
    fun confirmParticipation(email: String, selected_theme: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PARTICIPANT_STATUS, "aceptado")
            put(COLUMN_PARTICIPANT_SELECTED_THEME, selected_theme)
        }
        db.update(
            TABLE_PARTICIPANTS,
            values,
            "$COLUMN_PARTICIPANT_EMAIL = ?",
            arrayOf(email)
        )
        db.close()
    }

    // Método para declinar la participación
    fun declineParticipation(email: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PARTICIPANT_STATUS, "rechazado")
        }
        db.update(
            TABLE_PARTICIPANTS,
            values,
            "$COLUMN_PARTICIPANT_EMAIL = ?",
            arrayOf(email)
        )
        db.close()
    }

    fun getParticipantEmailById(participantId: Long): String? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_PARTICIPANT_EMAIL FROM $TABLE_USERS WHERE id = ?",
            arrayOf(participantId.toString())
        )

        val email = if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow("email"))
        } else {
            null // Retorna null si no se encuentra el participante
        }
        cursor.close()
        db.close()
        return email
    }



    fun getAllUsers(): List<String> {
        val users = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS", null)

        if (cursor.moveToFirst()) {
            do {
                val user = "ID: ${cursor.getInt(0)}, Nombre: ${cursor.getString(1)}, Alias: ${cursor.getString(2)}, Correo: ${cursor.getString(3)}"
                users.add(user)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return users
    }

    fun getAllUsersDebug(): List<String> {
        val users = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS", null)

        if (cursor.moveToFirst()) {
            do {
                val user = "ID: ${cursor.getInt(0)}, Nombre: ${cursor.getString(1)}, Correo: ${cursor.getString(3)}"
                users.add(user)
            } while (cursor.moveToNext())
        } else {
            users.add("No se encontraron usuarios")
        }

        cursor.close()
        db.close()
        return users
    }

    private fun generateExchangeCode(): String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..8)
            .map { charset.random() }
            .joinToString("")
    }

    fun getExchangeCode(exchangeId: Long): String {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_EXCHANGE_CODE FROM $TABLE_EXCHANGES WHERE $COLUMN_EXCHANGE_ID = ?",
            arrayOf(exchangeId.toString())
        )
        var code = ""
        if (cursor.moveToFirst()) {
            code = cursor.getString(0)
        }
        cursor.close()
        db.close()
        return code
    }

    fun getParticipantsForExchange(exchangeId: Long): List<Map<String, String>> {
        val db = readableDatabase
        val participants = mutableListOf<Map<String, String>>()

        val cursor = db.rawQuery(
            "SELECT $COLUMN_PARTICIPANT_NAME, $COLUMN_PARTICIPANT_EMAIL FROM $TABLE_PARTICIPANTS WHERE $COLUMN_EXCHANGE_ID_FK = ?",
            arrayOf(exchangeId.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PARTICIPANT_NAME))
                val email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PARTICIPANT_EMAIL))
                participants.add(mapOf("name" to name, "email" to email))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return participants
    }

    fun getAllExchanges(): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_EXCHANGES", null)
    }

    fun getExchangeById(exchangeId: String): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_EXCHANGES WHERE id = ?", arrayOf(exchangeId))
    }

    fun getParticipantsByExchangeId(exchangeId: String): List<Map<String, String>> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_PARTICIPANT_NAME, $COLUMN_PARTICIPANT_STATUS FROM $TABLE_PARTICIPANTS WHERE $COLUMN_EXCHANGE_ID_FK = ?",
            arrayOf(exchangeId)
        )

        val participants = mutableListOf<Map<String, String>>()
        while (cursor.moveToNext()) {
            participants.add(
                mapOf(
                    "name" to cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    "status" to cursor.getString(cursor.getColumnIndexOrThrow("status"))
                )
            )
        }
        cursor.close()
        return participants
    }

    fun deleteExchange(exchangeId: String) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            // Eliminar participantes asociados
            db.delete("participants", "exchange_id = ?", arrayOf(exchangeId))

            // Eliminar el intercambio
            db.delete("exchanges", "id = ?", arrayOf(exchangeId))

            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    fun deleteParticipant(email: String) {
        val db = writableDatabase
        db.delete(TABLE_PARTICIPANTS, "$COLUMN_PARTICIPANT_EMAIL = ?", arrayOf(email))
        db.close()
    }


    fun getExchangeIdByCode(code: String): String? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id FROM exchanges WHERE code = ?", arrayOf(code))
        return if (cursor.moveToFirst()) {
            val exchangeId = cursor.getString(cursor.getColumnIndexOrThrow("id"))
            cursor.close()
            exchangeId
        } else {
            cursor.close()
            null
        }
    }

    fun getThemesByExchangeId(exchangeId: String): List<String> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT theme1, theme2, theme3 FROM exchanges WHERE id = ?",
            arrayOf(exchangeId)
        )

        val themes = mutableListOf<String>()
        if (cursor.moveToFirst()) {
            val theme1 = cursor.getString(cursor.getColumnIndexOrThrow("theme1"))
            val theme2 = cursor.getString(cursor.getColumnIndexOrThrow("theme2"))
            val theme3 = cursor.getString(cursor.getColumnIndexOrThrow("theme3"))

            if (!theme1.isNullOrEmpty()) themes.add(theme1)
            if (!theme2.isNullOrEmpty()) themes.add(theme2)
            if (!theme3.isNullOrEmpty()) themes.add(theme3)
        }
        cursor.close()
        return themes
    }

    fun getAcceptedParticipants(exchangeId: String): List<Map<String, String>> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT name, email FROM participants WHERE exchange_id = ? AND status = ?",
            arrayOf(exchangeId, "aceptado")
        )

        val participants = mutableListOf<Map<String, String>>()
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val email = cursor.getString(cursor.getColumnIndexOrThrow("email"))
            val selectedTheme = cursor.getString(cursor.getColumnIndexOrThrow("selected_theme"))
            participants.add(mapOf("name" to name, "email" to email, "selected_theme" to selectedTheme))
        }
        cursor.close()
        return participants
    }

    fun getAllExchangesWithDates(): List<Map<String, String>> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id, date FROM exchanges WHERE draw_done = 0", null)

        val exchanges = mutableListOf<Map<String, String>>()
        while (cursor.moveToNext()) {
            exchanges.add(
                mapOf(
                    "id" to cursor.getString(cursor.getColumnIndexOrThrow("id")),
                    "date" to cursor.getString(cursor.getColumnIndexOrThrow("date"))
                )
            )
        }
        cursor.close()
        return exchanges
    }

    fun markDrawAsDone(exchangeId: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("draw_done", 1)
        }
        db.update("exchanges", values, "id = ?", arrayOf(exchangeId))
        db.close()
    }


    fun updateExchangeDetails(exchangeId: String, theme1: String, theme2: String, theme3: String, amount:String,
                              deadline: String, hour: String, date: String, location: String, data: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("theme1", theme1)
            put("theme2", theme2)
            put("theme3", theme3)
            put("amount", amount)
            put("deadline", deadline)
            put("hour", hour)
            put("date", date)
            put("location", location)
            put("data",data)
        }
        db.update("exchanges", values, "id = ?", arrayOf(exchangeId))
        db.close()
    }



    fun isUserCreatorOfExchange(userId: Long, exchangeId: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_EXCHANGES WHERE $COLUMN_EXCHANGE_ID = ? AND $COLUMN_USER_ID_FK = ?",
            arrayOf(exchangeId, userId.toString())
        )

        val isCreator = if (cursor.moveToFirst()) {
            cursor.getInt(0) > 0
        } else {
            false
        }
        cursor.close()
        db.close()
        return isCreator
    }











}