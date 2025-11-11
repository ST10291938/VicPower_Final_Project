package com.example.vic_power

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class home_employee : AppCompatActivity() {
    private lateinit var adapter: BookingAdapter
    private val bookings = mutableListOf<Booking>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.page_home_employee)

        val recyclerView = findViewById<RecyclerView>(R.id.bookingsRecyclerView)
        val searchBar = findViewById<EditText>(R.id.searchBar)
        val logoutBtn = findViewById<Button>(R.id.logoutBtn)

        val filterPending = findViewById<Button>(R.id.filterPending)
        val filterConfirmed = findViewById<Button>(R.id.filterConfirmed)
        val filterCompleted = findViewById<Button>(R.id.filterCompleted)

        adapter = BookingAdapter(bookings) { id, status -> updateBookingStatus(id, status) }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadBookings()

        // Search filter
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterBookings(s.toString())
            }
        })

        // Logout
        logoutBtn.setOnClickListener {

            val intent = Intent(this@home_employee, Login::class.java)
            startActivity(intent)
            //auth.signOut()
            Snackbar.make(recyclerView, "Logged out", Snackbar.LENGTH_SHORT).show()
          //  finish()
        }

        // Filters
        filterPending.setOnClickListener { filterByStatus("Pending") }
        filterConfirmed.setOnClickListener { filterByStatus("Confirmed") }
        filterCompleted.setOnClickListener { filterByStatus("Completed") }
    }

    private fun loadBookings() {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).get().addOnSuccessListener { doc ->
            if (!doc.exists() || doc.getString("role") != "employee") {
                Snackbar.make(findViewById(R.id.bookingsRecyclerView), "Unauthorized access", Snackbar.LENGTH_LONG).show()
                auth.signOut()
                finish()
                return@addOnSuccessListener
            }

            db.collection("bookings")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Snackbar.make(findViewById(R.id.bookingsRecyclerView), "Error: ${e.message}", Snackbar.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }
                    bookings.clear()
                    snapshot?.forEach { doc ->
                        val booking = Booking(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            service = doc.getString("service") ?: "",
                            date = doc.getString("date") ?: "",
                            time = doc.getString("time") ?: "",
                            status = doc.getString("status") ?: "Pending"
                        )
                        bookings.add(booking)
                    }
                    adapter.updateList(bookings)
                }
        }
    }

    private fun filterBookings(query: String) {
        val filtered = bookings.filter {
            it.name.contains(query, true) ||
                    it.service.contains(query, true) ||
                    it.date.contains(query, true) ||
                    it.status.contains(query, true)
        }
        adapter.updateList(filtered)
    }

    private fun filterByStatus(status: String) {
        val filtered = bookings.filter { it.status == status }
        adapter.updateList(filtered)
        Snackbar.make(findViewById(R.id.bookingsRecyclerView), "Showing $status bookings", Snackbar.LENGTH_SHORT).show()
    }

    private fun updateBookingStatus(bookingId: String, newStatus: String) {
        db.collection("bookings").document(bookingId)
            .update("status", newStatus)
            .addOnSuccessListener {
                Snackbar.make(findViewById(R.id.bookingsRecyclerView), "Booking updated to $newStatus", Snackbar.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Snackbar.make(findViewById(R.id.bookingsRecyclerView), "Error: ${it.message}", Snackbar.LENGTH_LONG).show()
            }
    }
}
