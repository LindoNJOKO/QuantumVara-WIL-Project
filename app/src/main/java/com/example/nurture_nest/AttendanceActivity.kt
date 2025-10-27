package com.example.nurture_nest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nurture_nest.databinding.ActivityAttendanceBinding
import com.google.firebase.database.*

data class AttendanceLog(
    val studentName: String? = null,
    val date: String? = null,
    val status: String? = null
)

class AttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAttendanceBinding
    private lateinit var database: DatabaseReference
    private lateinit var adapter: AttendanceLogsAdapter
    private val logsList = mutableListOf<AttendanceLog>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().getReference("attendance_logs")

        adapter = AttendanceLogsAdapter(logsList)
        binding.rvAttendanceLogs.layoutManager = LinearLayoutManager(this)
        binding.rvAttendanceLogs.adapter = adapter

        loadAttendanceLogs()

        binding.btnRefreshLogs.setOnClickListener {
            loadAttendanceLogs()
        }
    }

    private fun loadAttendanceLogs() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                logsList.clear()
                for (logSnapshot in snapshot.children) {
                    val log = logSnapshot.getValue(AttendanceLog::class.java)
                    log?.let { logsList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
