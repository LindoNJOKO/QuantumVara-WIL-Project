package com.example.nurture_nest.com.example.nurture_nest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.nurture_nest.Fragments.AdminDashboardFragment
import com.example.nurture_nest.R

class AdminDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make sure this is the layout that contains the fragment container
        setContentView(R.layout.activity_admin_dashboard)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AdminDashboardFragment())
                .commit()
        }
    }
}