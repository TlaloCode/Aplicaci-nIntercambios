package com.example.proyectomoviles

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
class DatabaseHelper(context:Context):
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        companion object {
            private const val DATABASE_NAME = "QueridoSanta.db"
            private const val DATABASE_VERSION = 1

            // Tabla de usuarios
            const val TABLE_USERS = "users"
            const val COLUMN_USER_ID = "id"
            const val COLUMN_USER_NAME = "name"
            const val COLUMN_USER_ALIAS = "alias"
            const val COLUMN_USER_EMAIL = "email"
            const val COLUMN_USER_PASSWORD = "password"
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
            db.execSQL(CREATE_USERS_TABLE)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            // Si la base de datos cambia de versión, eliminamos y recreamos las tablas
            db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
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

        // Método para validar usuario
        fun validateUser(email: String, password: String): Boolean {
            val db = readableDatabase
            val query = """
            SELECT * FROM $TABLE_USERS
            WHERE $COLUMN_USER_EMAIL = ? AND $COLUMN_USER_PASSWORD = ?
        """
            val cursor = db.rawQuery(query, arrayOf(email, password))
            val isValid = cursor.count > 0
            cursor.close()
            db.close()
            return isValid
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


}