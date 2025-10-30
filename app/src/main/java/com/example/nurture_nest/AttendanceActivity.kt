package com.example.nurture_nest

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nurture_nest.adapters.AttendanceLogsAdapter
import com.example.nurture_nest.databinding.ActivityAttendanceBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

data class AttendanceLog(
    val studentName: String? = null,
    val date: String? = null,
    val status: String? = null
)

class AttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAttendanceBinding
    private lateinit var adapter: AttendanceLogsAdapter
    private val logsList = mutableListOf<AttendanceLog>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = AttendanceLogsAdapter(logsList)
        binding.rvAttendanceLogs.layoutManager = LinearLayoutManager(this)
        binding.rvAttendanceLogs.adapter = adapter

        loadAttendanceLogs()

        binding.btnRefreshLogs.setOnClickListener {
            loadAttendanceLogs()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel() // cancel coroutines when activity is destroyed
    }

    private fun loadAttendanceLogs() {
        logsList.clear()
        adapter.notifyDataSetChanged()

        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmptyMessage.visibility = View.GONE
        binding.btnRefreshLogs.isEnabled = false

        val currentTeacherId = auth.currentUser?.uid
        if (currentTeacherId == null) {
            showEmptyMessage("Not logged in.")
            return
        }

        scope.launch {
            try {
                val dateDocuments = db.collection("attendance").get().await()
                if (dateDocuments.isEmpty) {
                    showEmptyMessage("No attendance records found.")
                    return@launch
                }

                val allLogs = mutableListOf<AttendanceLog>()

                for (dateDoc in dateDocuments.documents) {
                    val date = dateDoc.id
                    val recordsSnapshot = db.collection("attendance")
                        .document(date)
                        .collection("records")
                        .whereEqualTo("teacherId", currentTeacherId)
                        .get()
                        .await()

                    val logsForDate = recordsSnapshot.documents.map { mapToAttendanceLog(it, date) }
                    allLogs.addAll(logsForDate)
                }

                logsList.clear()
                logsList.addAll(allLogs.sortedByDescending { it.date })

                if (logsList.isEmpty()) {
                    showEmptyMessage("No records for this teacher.")
                } else {
                    adapter.notifyDataSetChanged()
                    binding.rvAttendanceLogs.visibility = View.VISIBLE
                }

            } catch (e: Exception) {
                showEmptyMessage("Error loading records: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnRefreshLogs.isEnabled = true
            }
        }
    }

    private fun showEmptyMessage(message: String) {
        binding.tvEmptyMessage.text = message
        binding.tvEmptyMessage.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        binding.btnRefreshLogs.isEnabled = true
        binding.rvAttendanceLogs.visibility = View.GONE
    }

    private fun mapToAttendanceLog(record: DocumentSnapshot, date: String): AttendanceLog {
        return AttendanceLog(
            studentName = record.getString("studentName"),
            date = date,
            status = record.getString("status")
        )
    }
}
