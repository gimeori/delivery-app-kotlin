package com.example.deliveryapp

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView

class MapScrollView(context: Context, attrs: AttributeSet?) : ScrollView(context, attrs) {
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                super.onInterceptTouchEvent(ev)
            }
            MotionEvent.ACTION_MOVE -> {
                false
            }
            else -> {
                super.onInterceptTouchEvent(ev)
            }
        }
    }
}
