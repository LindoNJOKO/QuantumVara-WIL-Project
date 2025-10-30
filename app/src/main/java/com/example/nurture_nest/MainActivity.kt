package com.example.nurture_nest

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.nurture_nest.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.core.content.ContextCompat
import com.stripe.android.PaymentConfiguration

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var bottomNav: BottomNavigationView
    private var userRole: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Stripe
        val publishableKey = BuildConfig.STRIPE_PUBLISHABLE_KEY
        PaymentConfiguration.init(applicationContext, publishableKey)

        setContentView(R.layout.activity_main)

        // SharedPreferences to get user role
        prefs = getSharedPreferences("NurtureNestPrefs", MODE_PRIVATE)
        userRole = prefs.getString("role", "") ?: ""

        // Initialize bottom navigation
        bottomNav = findViewById(R.id.bottom_navigation)

        // Hide Calendar tab for teachers
        if (userRole.lowercase() == "teacher") {
            bottomNav.menu.findItem(R.id.nav_calendar).isVisible = false
        }

        // Apply color selector
        val navColors = ContextCompat.getColorStateList(this, R.color.nav_item_color)
        bottomNav.itemIconTintList = navColors
        bottomNav.itemTextColor = navColors

        // Load initial fragment based on role
        val initialFragment: Fragment = when (userRole.lowercase()) {
            "parent" -> ParentDashboardFragment()
            "teacher" -> TeacherDashboardFragment()
            "admin" -> AdminDashboardFragment()
            else -> ParentDashboardFragment()
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, initialFragment)
            .commit()

        // Bottom navigation listener
        bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_dashboard -> when (userRole.lowercase()) {
                    "parent" -> ParentDashboardFragment()
                    "teacher" -> TeacherDashboardFragment()
                    "admin" -> AdminDashboardFragment()
                    else -> ParentDashboardFragment()
                }
                R.id.nav_settings -> SettingsFragment()
                R.id.nav_calendar -> CalendarFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> initialFragment
            }
            loadFragment(fragment)
            true
        }
    }

    // Swap fragments in the container
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
