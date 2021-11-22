package com.example.nutrihanjum.diary.decorator

import android.content.Context
import android.text.style.TextAppearanceSpan
import androidx.core.content.res.ResourcesCompat
import com.example.nutrihanjum.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import java.time.DayOfWeek
import java.time.LocalDate

class SundayDecorator(val context: Context): DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay?): Boolean {
        day?.let {
            val date = LocalDate.of(it.year, it.month, it.day)

            return date.dayOfWeek == DayOfWeek.SUNDAY
        }

        return false
    }

    override fun decorate(view: DayViewFacade?) {
        view?.let {
            if (!it.areDaysDisabled()) {
                it.addSpan(TextAppearanceSpan(context, R.style.CalendarSundayAppearance))
                ResourcesCompat.getFont(context, R.font.apple_gothic_neo_b)?.let { font ->
                    it.addSpan(FontSpan(font))
                }
            }
        }
    }
}