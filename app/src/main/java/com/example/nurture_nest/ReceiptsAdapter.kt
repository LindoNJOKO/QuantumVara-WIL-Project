package com.example.nurture_nest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReceiptsAdapter : RecyclerView.Adapter<ReceiptsAdapter.ReceiptViewHolder>() {

    private var receipts = listOf<ReceiptEntity>()
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    fun submitList(newReceipts: List<ReceiptEntity>) {
        receipts = newReceipts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_receipt, parent, false)
        return ReceiptViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReceiptViewHolder, position: Int) {
        holder.bind(receipts[position])
    }

    override fun getItemCount() = receipts.size

    inner class ReceiptViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemName: TextView = itemView.findViewById(R.id.receiptItem)
        private val amount: TextView = itemView.findViewById(R.id.receiptAmount)
        private val date: TextView = itemView.findViewById(R.id.receiptDate)
        private val badge: TextView = itemView.findViewById(R.id.receiptBadge)

        fun bind(receipt: ReceiptEntity) {
            itemName.text = receipt.item
            amount.text = currencyFormatter.format(receipt.amount)
            date.text = dateFormatter.format(Date(receipt.date))
            badge.text = "✓"
        }
    }
}