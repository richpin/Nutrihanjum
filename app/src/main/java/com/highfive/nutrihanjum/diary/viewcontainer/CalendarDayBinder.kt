package com.example.nutrihanjum.diary.viewcontainer

import android.graphics.Color
import android.view.View
import com.example.nutrihanjum.R
import com.example.nutrihanjum.databinding.CalendarDayLayoutBinding
import com.example.nutrihanjum.diary.CalendarDiaryViewModel
import com.example.nutrihanjum.diary.DiaryViewModel
import com.example.nutrihanjum.model.ContentDTO
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.yearMonth
import java.time.LocalDate
import java.time.YearMonth

class CalendarDayBinder(
    val calendarView: CalendarView,
    private val viewModel: CalendarDiaryViewModel
): DayBinder<CalendarDayBinder.DayViewContainer> {

    var onDaySelectedListener: ((day: LocalDate) -> Unit)? = null


    override fun bind(container: DayViewContainer, day: CalendarDay) {

        if (day.date.isAfter(viewModel.lastVisibleDate) || day.date.isBefore(viewModel.firstVisibleDate)) {
            container.binding.root.visibility = View.INVISIBLE
        }
        else {
            container.binding.textviewCalendarDay.text = day.date.dayOfMonth.toString()
            container.day = day

            if (day.owner == DayOwner.THIS_MONTH && !day.date.isAfter(viewModel.today) && !day.date.isBefore(viewModel.signedDate)) {
                container.binding.textviewCalendarDay.setTextColor(Color.BLACK)
                if (day.date == viewModel.selectedDate) {
                    container.binding.textviewCalendarDay.setBackgroundResource(R.drawable.calendar_selected_date_background)
                }
                else {
                    container.binding.textviewCalendarDay.background = null
                }

                if (viewModel.isDiaryEmptyAtDate(day.date)) {
                    container.binding.indicatorDiaryExist.visibility = View.GONE
                }
                else {
                    container.binding.indicatorDiaryExist.visibility = View.VISIBLE
                }

            }
            else {
                container.binding.textviewCalendarDay.setTextColor(Color.LTGRAY)
                container.binding.textviewCalendarDay.background = null

                if (viewModel.isDiaryEmptyAtDate(day.date)) {
                    container.binding.indicatorDiaryExist.visibility = View.GONE
                }
                else {
                    container.binding.indicatorDiaryExist.visibility = View.VISIBLE
                }
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
                    if (it.date.isAfter(viewModel.today) || it.date.isBefore(viewModel.signedDate)) return@let
                    if (it.owner != DayOwner.THIS_MONTH) {
                        calendarView.smoothScrollToDate(it.date)
                        return@setOnClickListener
                    }

                    val prevSelection = viewModel.selectedDate
                    viewModel.selectedDate = it.date

                    calendarView.notifyDateChanged(prevSelection)
                    calendarView.notifyDayChanged(it)

                    onDaySelectedListener?.invoke(it.date)
                }
            }
        }
    }
}