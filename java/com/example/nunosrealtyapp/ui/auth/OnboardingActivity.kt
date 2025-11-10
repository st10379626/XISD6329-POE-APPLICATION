package com.example.nunosrealtyapp.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.nunosrealtyapp.R
import com.example.nunosrealtyapp.databinding.ActivityOnboardingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var onboardingAdapter: OnboardingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupListeners()
    }

    private fun setupViewPager() {
        val onboardingItems = listOf(
            OnboardingItem(
                title = "Find your dream house",
                description = "Discover your dream home with ease. Browse listings, book viewings, and unlock a world of property possibilities – all at your fingertips.",
                imageRes = R.drawable.onboarding_1
            ),
            OnboardingItem(
                title = "Easy Booking",
                description = "Book property viewings with just a few taps. Select your preferred date and time effortlessly.",
                imageRes = R.drawable.onboarding_2
            ),
            OnboardingItem(
                title = "Home sweet home",
                description = "No matter what you are looking for whether you want to rent or buy we will always aim to fulfill your wishes every time.",
                imageRes = R.drawable.onboarding_3
            )
        )

        onboardingAdapter = com.example.nunosrealtyapp.ui.auth.OnboardingAdapter(onboardingItems)
        binding.viewPager.adapter = onboardingAdapter

        // Set up custom dots indicator for ViewPager2
        binding.dotsIndicator.setViewPager2(binding.viewPager)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.getStartedButton.visibility =
                    if (position == onboardingAdapter.itemCount - 1) android.view.View.VISIBLE
                    else android.view.View.GONE
            }
        })
    }
    private fun setupListeners() {
        binding.getStartedButton.setOnClickListener {
            // Mark onboarding as completed
            val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
            sharedPreferences.edit().putBoolean("onboarding_completed", true).apply()

            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }



    data class OnboardingItem(
        val title: String,
        val description: String,
        val imageRes: Int
    )
}