package com.example.nurture_nest


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.nurture_nest.LunchOrdering
import com.example.nurture_nest.R
import com.example.nurture_nest.model.Meal

class WeeklyMenuPagerAdapter(
    private val activity: LunchOrdering,
    private val days: List<String>,
    private val weeklyMeals: Map<String, List<Meal>>,
    private val cart: Map<String, Meal>,
    private val onMealSelected: (String, Meal) -> Unit
) : RecyclerView.Adapter<WeeklyMenuPagerAdapter.DayViewHolder>() {

    inner class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDayTitle: TextView = view.findViewById(R.id.tvDayTitle)
        val llMealsList: LinearLayout = view.findViewById(R.id.llMealsList)
        val tvNoMeals: TextView = view.findViewById(R.id.tvNoMeals)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.page_day_menu, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]
        val meals = weeklyMeals[day] ?: emptyList()
        val selectedMeal = cart[day]

        holder.tvDayTitle.text = "$day's Menu"
        holder.llMealsList.removeAllViews()

        if (meals.isEmpty()) {
            holder.tvNoMeals.visibility = View.VISIBLE
            holder.llMealsList.visibility = View.GONE
            return
        }

        holder.tvNoMeals.visibility = View.GONE
        holder.llMealsList.visibility = View.VISIBLE

        meals.forEach { meal ->
            val mealCard = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.card_meal_item, holder.llMealsList, false)

            val cardView = mealCard.findViewById<CardView>(R.id.cardMeal)
            val tvMealName = mealCard.findViewById<TextView>(R.id.tvMealName)
            val tvMealDesc = mealCard.findViewById<TextView>(R.id.tvMealDescription)
            val tvCategory = mealCard.findViewById<TextView>(R.id.tvMealCategory)
            val tvPrice = mealCard.findViewById<TextView>(R.id.tvMealPrice)
            val tvIngredients = mealCard.findViewById<TextView>(R.id.tvMealIngredients)
            val tvAllergens = mealCard.findViewById<TextView>(R.id.tvMealAllergens)
            val btnAddToCart = mealCard.findViewById<Button>(R.id.btnAddToCart)
            val btnRemoveFromCart = mealCard.findViewById<Button>(R.id.btnRemoveFromCart)
            val ivSelected = mealCard.findViewById<ImageView>(R.id.ivSelectedIndicator)

            tvMealName.text = meal.name
            tvMealDesc.text = meal.description
            tvCategory.text = "🍽️ ${meal.category}"
            tvPrice.text = "R ${String.format("%.2f", meal.price)}"

            if (meal.ingredients.isNotEmpty()) {
                tvIngredients.visibility = View.VISIBLE
                tvIngredients.text = "Ingredients: ${meal.ingredients.joinToString(", ")}"
            } else {
                tvIngredients.visibility = View.GONE
            }

            if (meal.allergens.isNotEmpty()) {
                tvAllergens.visibility = View.VISIBLE
                tvAllergens.text = "⚠️ Contains: ${meal.allergens.joinToString(", ")}"
            } else {
                tvAllergens.visibility = View.GONE
            }

            // Check if this meal is already in cart for this day
            val isSelected = selectedMeal?.id == meal.id

            if (isSelected) {
                cardView.setCardBackgroundColor(
                    holder.itemView.context.getColor(R.color.selected_meal_bg)
                )
                btnAddToCart.visibility = View.GONE
                btnRemoveFromCart.visibility = View.VISIBLE
                ivSelected.visibility = View.VISIBLE
            } else {
                cardView.setCardBackgroundColor(
                    holder.itemView.context.getColor(android.R.color.white)
                )
                btnAddToCart.visibility = View.VISIBLE
                btnRemoveFromCart.visibility = View.GONE
                ivSelected.visibility = View.GONE
            }

            btnAddToCart.setOnClickListener {
                onMealSelected(day, meal)
                notifyItemChanged(position)
            }

            btnRemoveFromCart.setOnClickListener {
                activity.removeFromCart(day)
                notifyItemChanged(position)
            }

            holder.llMealsList.addView(mealCard)
        }
    }

    override fun getItemCount() = days.size
}