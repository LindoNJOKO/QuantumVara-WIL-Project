package com.example.nurture_nest.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.nurture_nest.ApiClient
import com.example.nurture_nest.AppDatabase
import com.example.nurture_nest.PaymentRequest
import com.example.nurture_nest.PaymentResponse
import com.example.nurture_nest.R
import com.example.nurture_nest.ReceiptEntity
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PaymentFragment : Fragment() {

    private lateinit var payButton: Button
    private lateinit var etAmount: EditText
    private lateinit var paymentSheet: PaymentSheet
    private var paymentIntentClientSecret: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment, container, false)

        payButton = view.findViewById(R.id.btnPayNow)
        etAmount = view.findViewById(R.id.etAmount)

        // Stripe PaymentSheet init
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)

        // Fetch client secret from backend when button is clicked
        payButton.setOnClickListener {
            val amountText = etAmount.text.toString()
            if (amountText.isBlank()) {
                Toast.makeText(requireContext(), "Please enter an amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amountDouble = try {
                amountText.toDouble()
            } catch (e: NumberFormatException) {
                Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Convert to cents
            val amountCents = (amountDouble * 100).toInt()

            createPaymentIntent(amountCents)
        }

        return view
    }

    private fun createPaymentIntent(amount: Int) {
        val request = PaymentRequest(amount = amount)
        ApiClient.instance.createPaymentIntent(request).enqueue(object : retrofit2.Callback<PaymentResponse> {
            override fun onResponse(call: Call<PaymentResponse>, response: Response<PaymentResponse>) {
                if (response.isSuccessful) {
                    paymentIntentClientSecret = response.body()?.clientSecret
                    presentPaymentSheet()
                } else {
                    Toast.makeText(requireContext(), "Failed to create payment intent", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PaymentResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun presentPaymentSheet() {
        paymentIntentClientSecret?.let { secret ->
            val config = PaymentSheet.Configuration("NurtureNest Tuition")
            paymentSheet.presentWithPaymentIntent(secret, config)
        }
    }

    private fun onPaymentSheetResult(result: PaymentSheetResult) {
        when (result) {
            is PaymentSheetResult.Completed -> {
                Toast.makeText(requireContext(), " Payment successful!", Toast.LENGTH_LONG).show()
                saveReceipt("Tuition Fee", 10.0)
            }
            is PaymentSheetResult.Canceled -> {
                Toast.makeText(requireContext(), "⚠️ Payment canceled", Toast.LENGTH_SHORT).show()
            }
            is PaymentSheetResult.Failed -> {
                Toast.makeText(requireContext(), " Error: ${result.error.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveReceipt(item: String, amount: Double) {
        val db = AppDatabase.getDatabase(requireContext())
        val receipt = ReceiptEntity(
            item = item,
            amount = amount,
            date = System.currentTimeMillis()
        )

        // Insert on background thread
        lifecycleScope.launch {
            db.receiptDao().insertReceipt(receipt)
        }
    }

}
