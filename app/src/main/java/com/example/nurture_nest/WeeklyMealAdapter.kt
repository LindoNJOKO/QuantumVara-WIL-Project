package com.example.nurture_nest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nurture_nest.R
import com.example.nurture_nest.model.Meal

class WeeklyMealAdapter(
    private val weeklyMeals: Map<String, List<Meal>>,
    private val onMealSelected: (String, Meal) -> Unit
) : RecyclerView.Adapter<WeeklyMealAdapter.DayViewHolder>() {

    private val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")

    inner class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDay: TextView = view.findViewById(R.id.tvDay)
        val rgMeals: RadioGroup = view.findViewById(R.id.rgMeals)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day_meals, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]
        val meals = weeklyMeals[day] ?: emptyList()

        holder.tvDay.text = day
        holder.rgMeals.removeAllViews()

        if (meals.isEmpty()) {
            val noMealsText = TextView(holder.itemView.context).apply {
                text = "No meals available for this day"
                setPadding(16, 8, 16, 8)
            }
            holder.rgMeals.addView(noMealsText)
            return
        }

        meals.forEach { meal ->
            val radioButton = RadioButton(holder.itemView.context).apply {
                id = View.generateViewId()
                text = buildString {
                    append(meal.name)
                    append(" - R ${String.format("%.2f", meal.price)}")
                    if (meal.allergens.isNotEmpty()) {
                        append("\n⚠️ Contains: ${meal.allergens.joinToString(", ")}")
                    }
                    if (meal.description.isNotEmpty()) {
                        append("\n${meal.description}")
                    }
                }
                setPadding(16, 12, 16, 12)
                textSize = 14f
            }

            holder.rgMeals.addView(radioButton)
        }

        holder.rgMeals.setOnCheckedChangeListener { _, checkedId ->
            val selectedIndex = holder.rgMeals.indexOfChild(
                holder.rgMeals.findViewById(checkedId)
            )
            if (selectedIndex >= 0 && selectedIndex < meals.size) {
                onMealSelected(day, meals[selectedIndex])
            }
        }
    }

    override fun getItemCount() = days.size
}