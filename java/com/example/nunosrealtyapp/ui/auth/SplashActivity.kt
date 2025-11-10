package com.example.nunosrealtyapp.ui.auth

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.nunosrealtyapp.ui.customer.HomeActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use a handler to delay the splash screen for 2 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            checkAuthenticationState()
        }, 2000)
    }

    private fun checkAuthenticationState() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // User is logged in, go to Home
            startActivity(Intent(this, HomeActivity::class.java))
        } else {
            // User is not logged in, check if onboarding was shown
            val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val isOnboardingCompleted = sharedPreferences.getBoolean("onboarding_completed", false)

            if (isOnboardingCompleted) {
                // Onboarding was shown, go to Login
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                // First time user, show onboarding
                startActivity(Intent(this, OnboardingActivity::class.java))
            }
        }
        finish()
    }
}