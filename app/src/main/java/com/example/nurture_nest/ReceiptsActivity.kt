package com.example.nurture_nest

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class ReceiptsActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipts)

        listView = findViewById(R.id.receiptsList)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        listView.adapter = adapter

        loadReceipts()
    }

    private fun loadReceipts() {
        val db = AppDatabase.getDatabase(this)

        lifecycleScope.launch {
            val receipts = db.receiptDao().getAllReceipts()
            val formatted = receipts.map {
                "📄 ${it.item} - R${it.amount} on ${Date(it.date)}"
            }

            withContext(Dispatchers.Main) {
                adapter.clear()
                adapter.addAll(formatted)
            }
        }
    }
}
