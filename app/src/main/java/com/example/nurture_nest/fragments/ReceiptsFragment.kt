package com.example.nurture_nest.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.lifecycle.lifecycleScope
import com.example.nurture_nest.AppDatabase
import com.example.nurture_nest.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class ReceiptsFragment : Fragment() {

    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_receipts, container, false)
        listView = view.findViewById(R.id.receiptsList)
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, mutableListOf())
        listView.adapter = adapter

        loadReceipts()

        return view
    }

    private fun loadReceipts() {
        val db = AppDatabase.getDatabase(requireContext())

        lifecycleScope.launch {
            val receipts = db.receiptDao().getAllReceipts()
            val formatted = receipts.map {
                "📄 ${it.item} - $${it.amount} on ${Date(it.date)}"
            }

            withContext(Dispatchers.Main) {
                adapter.clear()
                adapter.addAll(formatted)
            }
        }
    }
}
