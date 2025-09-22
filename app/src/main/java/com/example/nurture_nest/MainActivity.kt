package com.example.nurture_nest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.nurture_nest.Fragments.AdminDashboardFragment
import com.example.nurture_nest.Fragments.CalendarFragment
import com.example.nurture_nest.Fragments.ParentDashboardFragment
import com.example.nurture_nest.Fragments.ProfileFragment
import com.example.nurture_nest.Fragments.SettingsFragment
import com.example.nurture_nest.Fragments.TeacherDashboardFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private var userRole: String = "Parent" // default

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottom_navigation)

        // Get role from Intent (passed by SplashScreen/Login)
        userRole = intent.getStringExtra("userType") ?: "Parent"

        // Load default dashboard fragment based on role
        if (savedInstanceState == null) {
            loadFragment(getDashboardFragment(userRole))
        }

        // Bottom navigation
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> loadFragment(getDashboardFragment(userRole))
                R.id.nav_settings -> loadFragment(SettingsFragment())
                R.id.nav_calendar -> loadFragment(CalendarFragment())
                R.id.nav_profile -> loadFragment(ProfileFragment())
            }
            true
        }
    }

    /** Return the correct dashboard fragment based on user role */
    private fun getDashboardFragment(role: String): Fragment {
        return when (role) {
            "Parent" -> ParentDashboardFragment()
            "Teacher" -> TeacherDashboardFragment()
            "Admin" -> AdminDashboardFragment()
            else -> ParentDashboardFragment()
        }
    }

    /** Swap fragments in the container */
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
