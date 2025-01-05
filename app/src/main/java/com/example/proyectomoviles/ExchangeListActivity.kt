package com.example.proyectomoviles

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ExchangeListActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var exchangeListView: RecyclerView
    private val exchangeList = mutableListOf<Map<String, String>>() // Lista temporal para mostrar intercambios

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exchange_list)

        dbHelper = DatabaseHelper(this)
        exchangeListView = findViewById(R.id.exchangeListView)

        // Configurar RecyclerView
        exchangeListView.layoutManager = LinearLayoutManager(this)
        val adapter = ExchangeListAdapter(exchangeList) { exchangeId ->
            // Redirigir a la actividad de detalles
            val intent = Intent(this, ExchangeDetailActivity::class.java)
            intent.putExtra("EXCHANGE_ID", exchangeId)
            startActivity(intent)
        }
        exchangeListView.adapter = adapter

        // Cargar la lista de intercambios desde la base de datos
        loadExchanges(adapter)
    }


    private fun loadExchanges(adapter: ExchangeListAdapter) {
        val cursor = dbHelper.getAllExchanges()
        exchangeList.clear()
        while (cursor.moveToNext()) {
            val id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
            val theme1 = cursor.getString(cursor.getColumnIndexOrThrow("theme1"))
            val theme2 = cursor.getString(cursor.getColumnIndexOrThrow("theme2"))
            val theme3 = cursor.getString(cursor.getColumnIndexOrThrow("theme3"))
            val amount = cursor.getString(cursor.getColumnIndexOrThrow("amount"))
            val deadline = cursor.getString(cursor.getColumnIndexOrThrow("deadline"))
            val hour = cursor.getString(cursor.getColumnIndexOrThrow("hour"))
            val date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
            val data = cursor.getString(cursor.getColumnIndexOrThrow("data"))
            val code = cursor.getString(cursor.getColumnIndexOrThrow("code"))

            exchangeList.add(mapOf("id" to id, "theme1" to theme1,"theme2" to theme2,
                "theme3" to theme3, "amount" to amount, "deadline" to deadline,
                "hour" to hour,"date" to date,"data" to data,"code" to code))
        }
        cursor.close()
        adapter.notifyDataSetChanged()
    }
}