package com.example.proyectomoviles

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
class DatabaseHelper(context:Context):
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "QueridoSanta.db"
        private const val DATABASE_VERSION = 3

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
        const val COLUMN_EXCHANGE_DATE = "date"
        const val COLUMN_EXCHANGE_LOCATION = "location"
        const val COLUMN_EXCHANGE_CODE  = "code" // Código único del participante
        const val COLUMN_USER_ID_FK = "user_id"

        // Tabla de participantes
        const val TABLE_PARTICIPANTS = "participants"
        const val COLUMN_PARTICIPANT_ID = "id"
        const val COLUMN_EXCHANGE_ID_FK = "exchange_id"
        const val COLUMN_PARTICIPANT_NAME = "name"
        const val COLUMN_PARTICIPANT_EMAIL = "email"
        const val COLUMN_PARTICIPANT_STATUS = "status"
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
                $COLUMN_EXCHANGE_DATE TEXT,
                $COLUMN_EXCHANGE_LOCATION TEXT,
                $COLUMN_USER_ID_FK INTEGER,
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
    fun addExchange(theme1: String, theme2: String, theme3: String, date: String, location: String, userId: Long): Long {
        val db = writableDatabase
        val exchangeCode = generateExchangeCode()
        val values = ContentValues().apply {
            put(COLUMN_EXCHANGE_THEME1, theme1)
            put(COLUMN_EXCHANGE_THEME2, theme2)
            put(COLUMN_EXCHANGE_THEME3, theme3)
            put(COLUMN_EXCHANGE_DATE, date)
            put(COLUMN_EXCHANGE_LOCATION, location)
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
    fun confirmParticipation(email: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PARTICIPANT_STATUS, "aceptado")
        }
        db.update(
            TABLE_PARTICIPANTS,
            values,
            "$COLUMN_PARTICIPANT_EMAIL = ?",
            arrayOf(email)
        )
        db.close()
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




}