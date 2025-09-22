package com.example.nurture_nest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
//import com.example.nurture_nest.Fragments.ParentDashboardFragment
//import com.example.nurture_nest.Fragments.TeacherDashboardFragment

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard) // must have a FrameLayout with id fragment_container

        val role = intent.getStringExtra("userType")

        //val fragment = when (role) {
          //  "Parent" -> ParentDashboardFragment()
            //"Teacher" -> TeacherDashboardFragment()
            //else -> null
        //}

        //fragment?.let {
        //    supportFragmentManager.beginTransaction()
        //        .replace(R.id.fragment_container, it)
        //        .commit()
        //}
    }
}
