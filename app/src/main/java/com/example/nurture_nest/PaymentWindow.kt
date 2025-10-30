package com.example.nurture_nest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PaymentWindow : AppCompatActivity() {

    private lateinit var btnProcessPayment: Button
    private lateinit var btnViewReceipt: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_payment_window)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize buttons only
        btnProcessPayment = findViewById(R.id.btnProcessPayment)
        btnViewReceipt = findViewById(R.id.btnViewReceipts)

        btnProcessPayment.setOnClickListener {
            startActivity(Intent(this, PaymentActivity::class.java))
        }

        btnViewReceipt.setOnClickListener {
            startActivity(Intent(this, ReceiptsActivity::class.java))
        }
    }
}
