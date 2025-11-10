package com.example.nunosrealtyapp.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.nunosrealtyapp.R

class SimpleDotsIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var dotSize = 16
    private var dotSpacing = 8
    private var selectedDotColor = ContextCompat.getColor(context, R.color.primary)
    private var unselectedDotColor = ContextCompat.getColor(context, R.color.gray_light)

    fun setViewPager2(viewPager: ViewPager2) {
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position)
            }
        })

        // Create dots when adapter is set
        viewPager.adapter?.registerAdapterDataObserver(object : androidx.recyclerview.widget.RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                createDots(viewPager.adapter?.itemCount ?: 0)
            }
        })

        createDots(viewPager.adapter?.itemCount ?: 0)
    }

    private fun createDots(count: Int) {
        removeAllViews()

        for (i in 0 until count) {
            val dot = android.widget.ImageView(context).apply {
                layoutParams = LayoutParams(dotSize, dotSize).apply {
                    setMargins(dotSpacing, 0, dotSpacing, 0)
                    gravity = Gravity.CENTER
                }
                setImageDrawable(
                    ContextCompat.getDrawable(context, R.drawable.dot_selector)
                )
                isSelected = i == 0
            }
            addView(dot)
        }
    }

    private fun updateDots(selectedPosition: Int) {
        for (i in 0 until childCount) {
            getChildAt(i).isSelected = i == selectedPosition
        }
    }
}