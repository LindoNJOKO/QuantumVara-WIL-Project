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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nurture_nest.adapters.ReceiptsAdapter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class ReceiptsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReceiptsAdapter
    private lateinit var emptyView: TextView
    private lateinit var totalAmountView: TextView

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipts)

        recyclerView = findViewById(R.id.receiptsRecyclerView)
        emptyView = findViewById(R.id.emptyView)
        totalAmountView = findViewById(R.id.totalAmount)

        adapter = ReceiptsAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadReceipts()
    }

    private fun loadReceipts() {
        val db = AppDatabase.getDatabase(this)

        lifecycleScope.launch {
            //Get only current users receipts
            val receipts = db.receiptDao().getReceiptsByUser(currentUserId)

            withContext(Dispatchers.Main) {
                if (receipts.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    emptyView.visibility = View.VISIBLE
                    totalAmountView.text = "Total: R0.00"
                } else {
                    recyclerView.visibility = View.VISIBLE
                    emptyView.visibility = View.GONE
                    adapter.submitList(receipts)

                    // Calculate total
                    val total = receipts.sumOf { it.amount }
                    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
                    totalAmountView.text = "Total: ${formatter.format(total)}"
                }
            }
        }
    }
}
