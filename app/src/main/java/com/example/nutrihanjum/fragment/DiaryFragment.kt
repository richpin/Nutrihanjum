package com.example.nutrihanjum.fragment

import android.annotation.SuppressLint
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.nutrihanjum.AddDiaryActivity
import com.example.nutrihanjum.R
import com.example.nutrihanjum.databinding.DiaryFragmentBinding
import com.example.nutrihanjum.util.OnSwipeTouchListener
import com.example.nutrihanjum.viewmodel.DiaryViewModel
import com.example.nutrihanjum.viewmodel.UserViewModel
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import java.util.*

class DiaryFragment private constructor() : Fragment() {

    companion object {
        @Volatile private var instance: DiaryFragment? = null

        @JvmStatic fun getInstance(): DiaryFragment = instance ?: synchronized(this) {
            instance ?: DiaryFragment().also {
                instance = it
            }
        }
    }

    private var _binding: DiaryFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DiaryViewModel by viewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DiaryFragmentBinding.inflate(layoutInflater)

        binding.calendarView.selectedDate = CalendarDay.today()

        setListeners()

        return binding.root
    }


    private fun setListeners() {
        binding.calendarView.setOnDateChangedListener { widget, date, _ ->
            widget.state().edit()
                .setCalendarDisplayMode(CalendarMode.WEEKS)
                .commit()

            widget.currentDate = date
        }

        binding.calendarView.setOnTitleClickListener {
            binding.calendarView.state().edit()
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit()
        }

        binding.btnAddDiary.setOnClickListener {
            startActivity(Intent(activity, AddDiaryActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}