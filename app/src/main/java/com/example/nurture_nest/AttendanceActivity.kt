package com.example.nurture_nest

import android.view.View
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nurture_nest.adapters.AttendanceLogsAdapter
import com.example.nurture_nest.databinding.ActivityAttendanceBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class AttendanceLog(
    val studentName: String? = null,
    val date: String? = null,
    val status: String? = null,
    val teacherId: String? = null
)

class AttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAttendanceBinding
    private lateinit var adapter: AttendanceLogsAdapter
    private val logsList = mutableListOf<AttendanceLog>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var userRole: String = "parent"
    private var selectedMonth: Int? = null
    private var sortDescending = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = AttendanceLogsAdapter(logsList)
        binding.rvAttendanceLogs.layoutManager = LinearLayoutManager(this)
        binding.rvAttendanceLogs.adapter = adapter

        setupMonthSpinner()
        loadUserRoleAndData()

        binding.btnRefreshLogs.setOnClickListener { loadAttendanceLogs() }
        binding.btnClearFilter.setOnClickListener {
            selectedMonth = null
            binding.spinnerMonthFilter.setSelection(0)
            binding.btnClearFilter.visibility = View.GONE
            loadAttendanceLogs()
        }
//        binding.btnSort.setOnClickListener {
//            sortDescending = !sortDescending
//            loadAttendanceLogs()
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    /** Sets up the dropdown (Spinner) for month filtering */
    private fun setupMonthSpinner() {
        val months = resources.getStringArray(R.array.months_array)
        val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMonthFilter.adapter = adapterSpinner

        binding.spinnerMonthFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    selectedMonth = null
                    binding.btnClearFilter.visibility = View.GONE
                } else {
                    selectedMonth = position // 1 = January, 12 = December
                    binding.btnClearFilter.visibility = View.VISIBLE
                }
                loadAttendanceLogs()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun loadUserRoleAndData() {
        val currentUser = auth.currentUser ?: return showEmptyMessage("Not logged in.")
        db.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { doc ->
                userRole = doc.getString("role")?.lowercase() ?: "parent"
                loadAttendanceLogs()
            }
            .addOnFailureListener {
                showEmptyMessage("Error loading user role.")
            }
    }

    private fun loadAttendanceLogs() {
        logsList.clear()
        adapter.notifyDataSetChanged()
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmptyMessage.visibility = View.GONE
        binding.btnRefreshLogs.isEnabled = false

        scope.launch {
            try {
                val allLogs = mutableListOf<AttendanceLog>()
                val dateDocuments = db.collection("attendance").get().await()
                if (dateDocuments.isEmpty) {
                    showEmptyMessage("No attendance records found.")
                    return@launch
                }

                val dateDocs = dateDocuments.documents.filter { doc ->
                    val id = doc.id
                    Regex("""\d{4}-\d{2}-\d{2}""").matches(id)
                }

                val filteredDocs = if (selectedMonth != null) {
                    dateDocs.filter { doc ->
                        val docMonth = SimpleDateFormat("MM", Locale.getDefault())
                            .parse(doc.id.substring(5, 7))?.let {
                                doc.id.substring(5, 7).toInt()
                            } ?: 0
                        docMonth == selectedMonth
                    }
                } else dateDocs

                for (dateDoc in filteredDocs) {
                    val date = dateDoc.id
                    val recordsSnap = db.collection("attendance")
                        .document(date)
                        .collection("records")
                        .get()
                        .await()

                    val records = when (userRole) {
                        "parent" -> {
                            val parentId = auth.currentUser?.uid ?: return@launch
                            val userDoc = db.collection("users").document(parentId).get().await()
                            val children = when (val data = userDoc.get("children")) {
                                is List<*> -> data.filterIsInstance<String>()
                                is Map<*, *> -> data.values.filterIsInstance<String>()
                                else -> emptyList()
                            }
                            recordsSnap.documents.filter { record ->
                                val studentName = record.getString("studentName")?.trim()?.lowercase()
                                children.any { it.trim().lowercase() == studentName }
                            }
                        }
                        "teacher" -> recordsSnap.documents.filter {
                            it.getString("teacherId") == auth.currentUser?.uid
                        }
                        else -> recordsSnap.documents
                    }

                    allLogs.addAll(records.map { mapToAttendanceLog(it, date) })
                }

                logsList.clear()
                logsList.addAll(
                    if (sortDescending)
                        allLogs.sortedByDescending { it.date }
                    else
                        allLogs.sortedBy { it.date }
                )

                if (logsList.isEmpty()) {
                    showEmptyMessage("No attendance records for selected month.")
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
            status = record.getString("status"),
            teacherId = record.getString("teacherId")
        )
    }
}
