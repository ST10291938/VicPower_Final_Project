package com.example.vic_power

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MyBookingsFragment : Fragment() {

    private lateinit var table: TableLayout
    private lateinit var searchView: SearchView
    private var allBookings: List<Map<String, Any>> = emptyList()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_bookings, container, false)

        table = view.findViewById(R.id.bookingsTable)
        searchView = view.findViewById(R.id.searchView)

        val db = Firebase.firestore
        val sharedPref = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("userId", "") ?: ""

        table.removeViews(1, table.childCount - 1)

        db.collection("bookings")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    val row = TableRow(context)
                    val text = TextView(context)
                    text.text = "No bookings found"
                    text.gravity = Gravity.CENTER
                    text.setPadding(8, 16, 8, 16)
                    row.addView(text)
                    table.addView(row)
                    return@addOnSuccessListener
                }

                val bookings = snapshot.documents.sortedByDescending { it.getTimestamp("createdAt") }
                allBookings = bookings.map { it.data ?: emptyMap() }

                renderTable(allBookings)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error loading bookings: ${e.message}", Toast.LENGTH_LONG).show()
            }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterTable(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterTable(newText ?: "")
                return true
            }
        })

        return view
    }

    private fun renderTable(bookings: List<Map<String, Any>>) {
        // Clear old rows (keep header)
        table.removeViews(1, table.childCount - 1)

        for (doc in bookings) {
            val row = TableRow(context)
            row.setPadding(8, 8, 8, 8)

            val service = TextView(context).apply { text = doc["service"] as? String ?: "-" }
            val date = TextView(context).apply { text = doc["date"] as? String ?: "-" }
            val time = TextView(context).apply { text = doc["time"] as? String ?: "-" }
            val notes = TextView(context).apply { text = doc["notes"] as? String ?: "-" }
            val status = TextView(context).apply {
                text = doc["status"] as? String ?: "Pending"
                when (text.toString().lowercase()) {
                    "pending" -> setTextColor(Color.parseColor("#FFA500"))
                    "completed" -> setTextColor(Color.parseColor("#4CAF50"))
                    "cancelled" -> setTextColor(Color.parseColor("#F44336"))
                    else -> setTextColor(Color.BLACK)
                }
            }

            row.addView(service)
            row.addView(date)
            row.addView(time)
            row.addView(notes)
            row.addView(status)

            table.addView(row)
        }
    }

    private fun filterTable(query: String) {
        val filtered = if (query.isEmpty()) {
            allBookings
        } else {
            allBookings.filter { booking ->
                booking.values.any { value ->
                    value.toString().contains(query, ignoreCase = true)
                }

            }
        }
        renderTable(filtered)
    }
}
