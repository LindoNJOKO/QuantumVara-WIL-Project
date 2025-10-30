package com.example.nurture_nest

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.example.nurture_nest.model.Meal
import java.text.SimpleDateFormat
import java.util.*
import androidx.viewpager2.widget.ViewPager2
import com.example.nurture_nest.adapters.WeeklyMenuPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class LunchOrdering : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var btnViewCart: Button
    private lateinit var tvCartTotal: TextView
    private lateinit var etChildName: EditText

    private val weeklyMeals = mutableMapOf<String, List<Meal>>()
    private val cart = mutableMapOf<String, Meal>()
    private val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lunch_ordering)

        supportActionBar?.title = "Order Lunch"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initializeViews()
        loadMeals()
    }

    private fun initializeViews() {
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        btnViewCart = findViewById(R.id.btnViewCart)
        tvCartTotal = findViewById(R.id.tvCartTotal)
        etChildName = findViewById(R.id.etChildName)

        btnViewCart.setOnClickListener {
            if (cart.isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show()
            } else {
                showCartDialog()
            }
        }
    }

    private fun loadMeals() {
        db.collection("meals")
            .whereEqualTo("isAvailable", true)
            .get()
            .addOnSuccessListener { documents ->
                weeklyMeals.clear()

                days.forEach { day ->
                    val mealsForDay = documents.mapNotNull { doc ->
                        val meal = doc.toObject(Meal::class.java).copy(id = doc.id)
                        if (meal.dayOfWeek == day) meal else null
                    }
                    weeklyMeals[day] = mealsForDay
                }

                setupViewPager()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading meals: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupViewPager() {
        val adapter = WeeklyMenuPagerAdapter(
            this,
            days,
            weeklyMeals,
            cart
        ) { day, meal ->
            addToCart(day, meal)
        }

        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = days[position]
        }.attach()
    }

    private fun addToCart(day: String, meal: Meal) {
        cart[day] = meal
        updateCartDisplay()
        Toast.makeText(this, "${meal.name} added for $day", Toast.LENGTH_SHORT).show()
    }

    fun removeFromCart(day: String) {
        cart.remove(day)
        updateCartDisplay()
        viewPager.adapter?.notifyDataSetChanged()
    }

    private fun updateCartDisplay() {
        val total = cart.values.sumOf { it.price }
        tvCartTotal.text = "Cart (${cart.size}): R ${String.format("%.2f", total)}"

        if (cart.isEmpty()) {
            btnViewCart.isEnabled = false
            btnViewCart.alpha = 0.5f
        } else {
            btnViewCart.isEnabled = true
            btnViewCart.alpha = 1.0f
        }
    }

    private fun showCartDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_lunch_cart, null)

        val llCartItems = dialogView.findViewById<LinearLayout>(R.id.llCartItems)
        val tvDialogTotal = dialogView.findViewById<TextView>(R.id.tvDialogTotal)
        val etSpecialNotes = dialogView.findViewById<EditText>(R.id.etSpecialNotes)

        // Build cart items
        var total = 0.0
        cart.forEach { (day, meal) ->
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_cart_meal, llCartItems, false)

            val tvDay = itemView.findViewById<TextView>(R.id.tvCartDay)
            val tvMealName = itemView.findViewById<TextView>(R.id.tvCartMealName)
            val tvMealPrice = itemView.findViewById<TextView>(R.id.tvCartMealPrice)
            val btnRemove = itemView.findViewById<ImageButton>(R.id.btnRemoveFromCart)

            tvDay.text = day
            tvMealName.text = meal.name
            tvMealPrice.text = "R ${String.format("%.2f", meal.price)}"

            btnRemove.setOnClickListener {
                removeFromCart(day)
                showCartDialog()
            }

            total += meal.price
            llCartItems.addView(itemView)
        }

        tvDialogTotal.text = "Total: R ${String.format("%.2f", total)}"

        AlertDialog.Builder(this)
            .setTitle("Your Order")
            .setView(dialogView)
            .setPositiveButton("Place Order") { _, _ ->
                val childName = etChildName.text.toString().trim()

                if (childName.isEmpty()) {
                    Toast.makeText(this, "Please enter child's name", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val notes = etSpecialNotes.text.toString()
                placeOrder(childName, notes, total)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun placeOrder(childName: String, notes: String, total: Double) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val weekStartDate = getNextMonday()

        val orderData = hashMapOf(
            "userId" to userId,
            "childName" to childName,
            "weekStartDate" to dateFormat.format(weekStartDate),
            "meals" to cart.mapValues {
                hashMapOf(
                    "mealId" to it.value.id,
                    "mealName" to it.value.name,
                    "price" to it.value.price,
                    "category" to it.value.category
                )
            },
            "totalPrice" to total,
            "status" to "pending",
            "orderDate" to System.currentTimeMillis(),
            "notes" to notes
        )

        db.collection("lunchOrders")
            .add(orderData)
            .addOnSuccessListener {
                showSuccessDialog(childName, total)
                cart.clear()
                updateCartDisplay()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error placing order: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getNextMonday(): Date {
        val calendar = Calendar.getInstance()
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return calendar.time
    }

    private fun showSuccessDialog(childName: String, total: Double) {
        val message = buildString {
            append("✅ Order Confirmed!\n\n")
            append("Child: $childName\n")
            append("Week: ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(getNextMonday())}\n\n")
            append("Meals Ordered:\n")
            cart.forEach { (day, meal) ->
                append("• $day: ${meal.name}\n")
            }
            append("\nTotal: R ${String.format("%.2f", total)}")
        }

        AlertDialog.Builder(this)
            .setTitle("Success!")
            .setMessage(message)
            .setPositiveButton("Done") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}