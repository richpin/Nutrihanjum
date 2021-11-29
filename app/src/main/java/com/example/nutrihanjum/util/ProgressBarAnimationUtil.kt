package com.example.nutrihanjum.util

import android.animation.ValueAnimator
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlin.math.abs
import kotlin.math.max

object ProgressBarAnimationUtil {
    fun LinearProgressIndicator.setProgressWithAnimation(newProgress: Int) {
        val oldProgress = progress
        val animator = ValueAnimator.ofInt(oldProgress, max(newProgress, 20))
        animator.duration = 900

        animator.addUpdateListener {
            progress = it.animatedValue as Int
        }

        animator.start()
    }

    fun LinearProgressIndicator.setProgressWithNoAnimation(newProgress: Int) {
        val oldProgress = progress
        val animator = ValueAnimator.ofInt(oldProgress, max(newProgress, 20))
        animator.duration = 900

        animator.addUpdateListener {
            progress = it.animatedValue as Int
        }

        animator.start()
    }
}