package com.example.vic_power

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory


class Loading : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.load)


        FirebaseApp.initializeApp(this)
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        //slide project name
        val projectName = findViewById<TextView>(R.id.projectName)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        projectName.startAnimation(slideUp)


        //auto run animate
        load_animate();

    }


    //load animate
    fun load_animate(){
        // Run loading for 10 seconds (10000 ms)
        Handler(Looper.getMainLooper()).postDelayed({
             val intent = Intent(this@Loading, Login::class.java)
            startActivity(intent)
            finish()
        }, 10000)
    }
}