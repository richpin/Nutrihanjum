package com.example.nutrihanjum.diary.viewcontainer

import android.graphics.Color
import android.view.View
import com.example.nutrihanjum.R
import com.example.nutrihanjum.databinding.CalendarDayLayoutBinding
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import java.time.LocalDate
import java.time.YearMonth

class CalendarDayBinder(val calendarView: CalendarView): DayBinder<CalendarDayBinder.DayViewContainer> {

    var selectedDate: LocalDate = LocalDate.now()
    var onDaySelectedListener: ((day: LocalDate) -> Unit)? = null

    val lastDate = YearMonth.now().atEndOfMonth()
    val today = LocalDate.now()

    override fun bind(container: DayViewContainer, day: CalendarDay) {

        if (day.date.isAfter(lastDate)) {
            container.binding.textviewCalendarDay.visibility = View.INVISIBLE
        }
        else {
            container.binding.textviewCalendarDay.text = day.date.dayOfMonth.toString()
            container.day = day

            if (day.owner == DayOwner.THIS_MONTH && !day.date.isAfter(today)) {
                container.binding.textviewCalendarDay.setTextColor(Color.BLACK)
                if (day.date == selectedDate) {
                    container.binding.textviewCalendarDay.setBackgroundResource(R.drawable.box_rounded_yellow)
                }
                else {
                    container.binding.textviewCalendarDay.background = null
                }
            }
            else {
                container.binding.textviewCalendarDay.setTextColor(Color.LTGRAY)
                container.binding.textviewCalendarDay.background = null
            }
        }
    }

    override fun create(view: View) = DayViewContainer(view)


    inner class DayViewContainer(view: View): ViewContainer(view) {
        val binding = CalendarDayLayoutBinding.bind(view)
        var day: CalendarDay? = null

        init {
            view.setOnClickListener {
                day?.let {
                    if (it.date.isAfter(today)) return@let

                    val prevSelection = selectedDate
                    selectedDate = it.date

                    calendarView.notifyDateChanged(prevSelection)
                    calendarView.notifyDayChanged(it)

                    if (it.owner != DayOwner.THIS_MONTH) {
                        calendarView.scrollToMonth(YearMonth.from(it.date))
                    }

                    onDaySelectedListener?.invoke(it.date)
                }
            }
        }
    }
}