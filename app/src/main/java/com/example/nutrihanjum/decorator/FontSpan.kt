package com.example.nutrihanjum.decorator

import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan

class FontSpan(private val font: Typeface) : MetricAffectingSpan() {

    override fun updateMeasureState(textPaint: TextPaint) = updateTypeface(textPaint)

    override fun updateDrawState(textPaint: TextPaint) = updateTypeface(textPaint)

    private fun updateTypeface(textPaint: TextPaint) {
        val oldStyle: Int
        val old: Typeface = textPaint.typeface
        oldStyle = old.style

        val fake = oldStyle and font.style.inv()
        if (fake and Typeface.BOLD != 0) {
            textPaint.isFakeBoldText = true
        }

        if (fake and Typeface.ITALIC != 0) {
            textPaint.textSkewX = -0.25f
        }

        textPaint.typeface = font
    }

}