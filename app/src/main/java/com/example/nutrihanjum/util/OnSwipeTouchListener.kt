package com.example.nutrihanjum.util

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

open class OnSwipeTouchListener(ctx: Context) : View.OnTouchListener {

    private val gestureDetector: GestureDetector

    companion object {
        private val SWIPE_THRESHOLD = 20
        private val SWIPE_VELOCITY_THRESHOLD = 80
    }

    init {
        gestureDetector = GestureDetector(ctx, GestureListener())
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        try {
            return gestureDetector.onTouchEvent(event)
        } catch (e: Exception) {
            // Error Handling
        }
        return false
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            var result = false
            try {
                val diffY = e2.y - e1.y
                if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom()
                    } else {
                        onSwipeTop()
                    }
                    result = true
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }

            return result
        }


    }

    open fun onSwipeTop() {}

    open fun onSwipeBottom() {}
}