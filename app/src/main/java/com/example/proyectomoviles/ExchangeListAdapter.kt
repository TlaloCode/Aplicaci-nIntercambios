package com.example.proyectomoviles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExchangeListAdapter(
    private val exchangeList: List<Map<String, String>>,
    private val onItemClick: (exchangeId: String) -> Unit
) : RecyclerView.Adapter<ExchangeListAdapter.ExchangeViewHolder>() {

    inner class ExchangeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val button: Button = view.findViewById(R.id.exchangeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExchangeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exchange_button, parent, false)
        return ExchangeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExchangeViewHolder, position: Int) {
        val exchange = exchangeList[position]
        val id = exchange["id"] ?: "0"
        val code = exchange["code"] ?: "xxxxxx"
        val tematica1 = exchange["theme1"] ?: "Sin Nombre"
        val tematica2 = exchange["theme2"] ?: "Sin Nombre"
        val tematica3 = exchange["theme3"] ?: "Sin Nombre"
        val hora = exchange["hour"] ?: "Sin Hora"
        val date = exchange["date"] ?: "Sin Fecha"

        holder.button.text = "Intercambio #$code\n$tematica1, $tematica2, $tematica3\n$hora\n$date"
        holder.button.setOnClickListener {
            onItemClick(id) // Pasar el ID del intercambio al callback
        }
    }

    override fun getItemCount(): Int = exchangeList.size
}

