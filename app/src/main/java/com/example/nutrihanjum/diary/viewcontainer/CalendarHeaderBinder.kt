package com.example.nutrihanjum.diary.viewcontainer

import android.annotation.SuppressLint
import android.view.View
import com.example.nutrihanjum.databinding.CalendarHeaderLayoutBinding
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer

class CalendarHeaderBinder: MonthHeaderFooterBinder<CalendarHeaderBinder.CalendarHeaderContainer> {


    override fun bind(container: CalendarHeaderContainer, month: CalendarMonth) {}

    override fun create(view: View) = CalendarHeaderContainer(view)


    inner class CalendarHeaderContainer(view: View): ViewContainer(view)
}