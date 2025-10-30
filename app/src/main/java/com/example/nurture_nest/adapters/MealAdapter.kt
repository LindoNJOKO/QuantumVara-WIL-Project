package com.example.nurture_nest.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nurture_nest.R
import com.example.nurture_nest.model.Meal

class MealAdapter(
    private val meals: List<Meal>,
    private val onEditClick: (Meal) -> Unit,
    private val onDeleteClick: (Meal) -> Unit,
    private val onToggleAvailability: (Meal) -> Unit
) : RecyclerView.Adapter<MealAdapter.MealViewHolder>() {

    inner class MealViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMealName: TextView = view.findViewById(R.id.tvMealName)
        val tvDayCategory: TextView = view.findViewById(R.id.tvDayCategory)
        val tvIngredients: TextView = view.findViewById(R.id.tvIngredients)
        val tvAllergens: TextView = view.findViewById(R.id.tvAllergens)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvAvailability: TextView = view.findViewById(R.id.tvAvailability)
        val btnEdit: Button = view.findViewById(R.id.btnEdit)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
        val btnToggle: Button = view.findViewById(R.id.btnToggleAvailability)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meal_admin, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = meals[position]

        holder.tvMealName.text = meal.name
        holder.tvDayCategory.text = "${meal.dayOfWeek} • ${meal.category}"
        holder.tvIngredients.text = "Ingredients: ${meal.ingredients.joinToString(", ")}"

        if (meal.allergens.isNotEmpty()) {
            holder.tvAllergens.visibility = View.VISIBLE
            holder.tvAllergens.text = "⚠️ Allergens: ${meal.allergens.joinToString(", ")}"
        } else {
            holder.tvAllergens.visibility = View.GONE
        }

        holder.tvPrice.text = "R ${String.format("%.2f", meal.price)}"

        holder.tvAvailability.text = if (meal.isAvailable) "✓ Available" else "✗ Unavailable"
        holder.tvAvailability.setTextColor(
            if (meal.isAvailable)
                holder.itemView.context.getColor(android.R.color.holo_green_dark)
            else
                holder.itemView.context.getColor(android.R.color.holo_red_dark)
        )

        holder.btnEdit.setOnClickListener { onEditClick(meal) }
        holder.btnDelete.setOnClickListener { onDeleteClick(meal) }
        holder.btnToggle.setOnClickListener { onToggleAvailability(meal) }

        holder.btnToggle.text = if (meal.isAvailable) "Mark Unavailable" else "Mark Available"
    }

    override fun getItemCount() = meals.size
}