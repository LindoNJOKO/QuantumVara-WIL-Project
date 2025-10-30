package com.example.nurture_nest

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nurture_nest.adapters.MealAdapter
import com.example.nurture_nest.model.Meal
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.Query


class AdminMealManagementActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var rvMeals: RecyclerView
    private lateinit var fabAddMeal: FloatingActionButton
    private lateinit var adapter: MealAdapter
    private val meals = mutableListOf<Meal>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_meal_management)

        supportActionBar?.title = "Meal Management"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        rvMeals = findViewById(R.id.rvMeals)
        fabAddMeal = findViewById(R.id.fabAddMeal)

        setupRecyclerView()
        loadMeals()

        fabAddMeal.setOnClickListener {
            showAddEditMealDialog(null)
        }
    }

    private fun setupRecyclerView() {
        adapter = MealAdapter(
            meals = meals,
            onEditClick = { meal -> showAddEditMealDialog(meal) },
            onDeleteClick = { meal -> deleteMeal(meal) },
            onToggleAvailability = { meal -> toggleMealAvailability(meal) }
        )
        rvMeals.layoutManager = LinearLayoutManager(this)
        rvMeals.adapter = adapter
    }

    private fun loadMeals() {
        // SIMPLE QUERY - NO ORDERING
        db.collection("meals")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Error loading meals: ${e.message}", Toast.LENGTH_SHORT).show()
                    android.util.Log.e("AdminMealMgmt", "Error loading meals", e)
                    return@addSnapshotListener
                }

                if (snapshots == null) {
                    Toast.makeText(this, "No meals found", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                meals.clear()
                snapshots.documents.forEach { doc ->
                    try {
                        val meal = doc.toObject(Meal::class.java)?.copy(id = doc.id)
                        meal?.let { meals.add(it) }
                    } catch (ex: Exception) {
                        android.util.Log.e("AdminMealMgmt", "Error parsing meal: ${doc.id}", ex)
                    }
                }

                // Sort locally
                meals.sortWith(compareBy({ getDayOrder(it.dayOfWeek) }, { it.category }))

                adapter.notifyDataSetChanged()

                android.util.Log.d("AdminMealMgmt", "Loaded ${meals.size} meals")
            }
    }

    private fun getDayOrder(day: String): Int {
        return when (day) {
            "Monday" -> 1
            "Tuesday" -> 2
            "Wednesday" -> 3
            "Thursday" -> 4
            "Friday" -> 5
            else -> 6
        }
    }

    private fun showAddEditMealDialog(meal: Meal?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_meal, null)

        val etName = dialogView.findViewById<EditText>(R.id.etMealName)
        val etDescription = dialogView.findViewById<EditText>(R.id.etMealDescription)
        val etIngredients = dialogView.findViewById<EditText>(R.id.etIngredients)
        val etAllergens = dialogView.findViewById<EditText>(R.id.etAllergens)
        val etPrice = dialogView.findViewById<EditText>(R.id.etPrice)
        val spinnerDay = dialogView.findViewById<Spinner>(R.id.spinnerDay)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)

        // Setup spinners
        val days = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
        spinnerDay.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, days)

        val categories = arrayOf("Main Course", "Vegetarian", "Soup", "Salad", "Dessert")
        spinnerCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        // Pre-fill if editing
        meal?.let {
            etName.setText(it.name)
            etDescription.setText(it.description)
            etIngredients.setText(it.ingredients.joinToString(", "))
            etAllergens.setText(it.allergens.joinToString(", "))
            etPrice.setText(it.price.toString())

            val dayIndex = days.indexOf(it.dayOfWeek)
            if (dayIndex >= 0) spinnerDay.setSelection(dayIndex)

            val categoryIndex = categories.indexOf(it.category)
            if (categoryIndex >= 0) spinnerCategory.setSelection(categoryIndex)
        }

        AlertDialog.Builder(this)
            .setTitle(if (meal == null) "Add Meal" else "Edit Meal")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                val description = etDescription.text.toString().trim()
                val ingredientsText = etIngredients.text.toString().trim()
                val allergensText = etAllergens.text.toString().trim()
                val priceText = etPrice.text.toString().trim()

                if (name.isEmpty() || priceText.isEmpty()) {
                    Toast.makeText(this, "Name and price are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val ingredients = if (ingredientsText.isNotEmpty()) {
                    ingredientsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                } else {
                    emptyList()
                }

                val allergens = if (allergensText.isNotEmpty()) {
                    allergensText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                } else {
                    emptyList()
                }

                val price = priceText.toDoubleOrNull() ?: 0.0

                val mealData = Meal(
                    id = meal?.id ?: "",
                    name = name,
                    description = description,
                    ingredients = ingredients,
                    allergens = allergens,
                    category = spinnerCategory.selectedItem.toString(),
                    dayOfWeek = spinnerDay.selectedItem.toString(),
                    price = price,
                    isAvailable = meal?.isAvailable ?: true,
                    createdAt = meal?.createdAt ?: System.currentTimeMillis()
                )

                saveMeal(mealData)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveMeal(meal: Meal) {
        val mealMap = hashMapOf(
            "name" to meal.name,
            "description" to meal.description,
            "ingredients" to meal.ingredients,
            "allergens" to meal.allergens,
            "category" to meal.category,
            "dayOfWeek" to meal.dayOfWeek,
            "price" to meal.price,
            "isAvailable" to meal.isAvailable,
            "createdAt" to meal.createdAt
        )

        if (meal.id.isEmpty()) {
            // Add new meal
            db.collection("meals")
                .add(mealMap)
                .addOnSuccessListener { docRef ->
                    Toast.makeText(this, "Meal added successfully", Toast.LENGTH_SHORT).show()
                    android.util.Log.d("AdminMealMgmt", "Meal added with ID: ${docRef.id}")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    android.util.Log.e("AdminMealMgmt", "Error adding meal", e)
                }
        } else {
            // Update existing meal
            db.collection("meals").document(meal.id)
                .set(mealMap)  // Changed from update to set
                .addOnSuccessListener {
                    Toast.makeText(this, "Meal updated successfully", Toast.LENGTH_SHORT).show()
                    android.util.Log.d("AdminMealMgmt", "Meal updated: ${meal.id}")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    android.util.Log.e("AdminMealMgmt", "Error updating meal", e)
                }
        }
    }

    private fun deleteMeal(meal: Meal) {
        AlertDialog.Builder(this)
            .setTitle("Delete Meal")
            .setMessage("Are you sure you want to delete '${meal.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                db.collection("meals").document(meal.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Meal deleted", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun toggleMealAvailability(meal: Meal) {
        db.collection("meals").document(meal.id)
            .update("isAvailable", !meal.isAvailable)
            .addOnSuccessListener {
                val status = if (!meal.isAvailable) "available" else "unavailable"
                Toast.makeText(this, "Meal marked as $status", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}