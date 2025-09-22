package com.example.nurture_nest

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.nurture_nest.fragments.ParentDashboardFragment
import com.example.nurture_nest.fragments.TeacherDashboardFragment
import com.example.nurture_nest.fragments.AdminDashboardFragment
import com.example.nurture_nest.fragments.SettingsFragment
import com.example.nurture_nest.fragments.CalendarFragment
import com.example.nurture_nest.fragments.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var bottomNav: BottomNavigationView
    private var userRole: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ✅ Use the same sharedPref as Login
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        userRole = sharedPref.getString("user_role", "") ?: ""

        // Load initial fragment based on role
        val initialFragment = when (userRole.lowercase()) {
            "parent" -> ParentDashboardFragment()
            "teacher" -> TeacherDashboardFragment()
            "admin" -> AdminDashboardFragment()
            else -> null
        }

        initialFragment?.let {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, it)
                .commit()
        }

        bottomNav = findViewById(R.id.bottom_navigation)

        // Bottom navigation listener
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    loadFragment(
                        when (userRole.lowercase()) {
                            "parent" -> ParentDashboardFragment()
                            "teacher" -> TeacherDashboardFragment()
                            "admin" -> AdminDashboardFragment()
                            else -> ParentDashboardFragment()
                        }
                    )
                }
                R.id.nav_settings -> loadFragment(SettingsFragment())
                R.id.nav_calendar -> loadFragment(CalendarFragment())
                R.id.nav_profile -> loadFragment(ProfileFragment())
            }
            true
        }
    }

    /**
     * Swap fragments in the container
     */
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
