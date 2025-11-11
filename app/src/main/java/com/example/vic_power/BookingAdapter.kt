package com.example.vic_power

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BookingAdapter(
    private var bookings: List<Booking>,
    private val onUpdateStatus: (String, String) -> Unit
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userIcon: ImageView = view.findViewById(R.id.userIcon)
        val userName: TextView = view.findViewById(R.id.userName)
        val bookingDate: TextView = view.findViewById(R.id.bookingDate)
        val infoText: TextView = view.findViewById(R.id.bookingInfo)

        val approveBtn: Button = view.findViewById(R.id.approveBtn)
        val cancelBtn: Button = view.findViewById(R.id.cancelBtn)
        val completeBtn: Button = view.findViewById(R.id.completeBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]

        // User details
        holder.userName.text = booking.name
        holder.bookingDate.text = booking.date

        // Booking info
        holder.infoText.text = "${booking.service} at ${booking.time} \n\n ${booking.status}"

        // Button visibility based on status
        holder.approveBtn.visibility = if (booking.status == "Pending") View.VISIBLE else View.GONE
        holder.cancelBtn.visibility = if (booking.status in listOf("Pending", "Confirmed")) View.VISIBLE else View.GONE
        holder.completeBtn.visibility = if (booking.status == "Confirmed") View.VISIBLE else View.GONE

        // Color  buttons
        holder.approveBtn.setBackgroundColor(Color.GRAY)
        holder.cancelBtn.setBackgroundColor(Color.RED)
        holder.completeBtn.setBackgroundColor(Color.GREEN)

        when (booking.status) {
            "Pending" -> holder.infoText.setTextColor(Color.GRAY)
            "Confirmed" -> holder.infoText.setTextColor(Color.GRAY)
            "Cancelled" -> holder.infoText.setTextColor(Color.GRAY)
            "Completed" -> holder.infoText.setTextColor(Color.GRAY)
            else -> holder.infoText.setTextColor(Color.BLACK)
        }

        // Button actions
        holder.approveBtn.setOnClickListener { onUpdateStatus(booking.id, "Confirmed") }
        holder.cancelBtn.setOnClickListener { onUpdateStatus(booking.id, "Cancelled") }
        holder.completeBtn.setOnClickListener { onUpdateStatus(booking.id, "Completed") }

        holder.userIcon.setImageResource(R.drawable.user)
    }

    override fun getItemCount() = bookings.size

    fun updateList(newBookings: List<Booking>) {
        bookings = newBookings
        notifyDataSetChanged()
    }
}
