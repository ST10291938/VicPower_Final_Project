package com.example.vic_power

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale

class user_home : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var notification: View
    private lateinit var notificationText: TextView
    private lateinit var contentContainer: FrameLayout
    private lateinit var bottomNav: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.page_user_home)

         contentContainer = findViewById<FrameLayout>(R.id.contentContainer)
         bottomNav = findViewById<LinearLayout>(R.id.bottomNav)



        notification = findViewById(R.id.notificationBanner)
        notificationText = findViewById(R.id.notificationText)

showNotification(greetUser(this))

        buttons()
        supportFragmentManager.beginTransaction()
            .replace(R.id.contentContainer, ServicesFragment())
            .commit()
    }

    fun buttons(){

// Services
        bottomNav.getChildAt(0).setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.contentContainer, ServicesFragment())
                .commit()
        }

// Book Now
        bottomNav.getChildAt(1).setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.contentContainer, BookNowFragment())
                .commit()
        }

// My Bookings
        bottomNav.getChildAt(2).setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.contentContainer, MyBookingsFragment())
                .commit()
        }




        bottomNav.getChildAt(3).setOnClickListener {
            // Logout
           // auth.signOut()
            startActivity(Intent(this, Login::class.java))
            //finish()
        }
    }


    fun selectTab(index: Int) {
        for (i in 0 until bottomNav.childCount) {
            val tab = bottomNav.getChildAt(i) as LinearLayout
            val textView = tab.getChildAt(1) as TextView
            textView.setTextColor(if (i == index) Color.CYAN else Color.WHITE)
        }
    }




    private fun showNotification(message: String) {
        notificationText.text = message
        val slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_top_left)
        val slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_top_left)

        notification.visibility = View.VISIBLE
        notification.startAnimation(slideIn)

        // Auto dismiss after 3 seconds using main looper
        Handler(Looper.getMainLooper()).postDelayed({
            notification.startAnimation(slideOut)
            notification.visibility = View.GONE
        }, 3000)
    }

    fun greetUser(context: Context): String {
        val sharedPref = context.getSharedPreferences("user_info", Context.MODE_PRIVATE)
        val name = sharedPref.getString("username", "User") ?: "User"

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val greeting = when (hour) {
            in 0..11 -> "Good morning"
            in 12..17 -> "Good afternoon"
            else -> "Good evening"
        }

        val formattedName = name.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }

        return "$greeting, $formattedName!"
    }


}