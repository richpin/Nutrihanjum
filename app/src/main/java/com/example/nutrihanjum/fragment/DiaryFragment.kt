package com.example.nutrihanjum.fragment

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.style.*
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nutrihanjum.AddDiaryActivity
import com.example.nutrihanjum.R
import com.example.nutrihanjum.databinding.DiaryFragmentBinding
import com.example.nutrihanjum.databinding.ItemDiaryBinding
import com.example.nutrihanjum.decorator.SaturdayDecorator
import com.example.nutrihanjum.decorator.SundayDecorator
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.util.OnSwipeTouchListener
import com.example.nutrihanjum.viewmodel.DiaryViewModel
import com.example.nutrihanjum.viewmodel.UserViewModel
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.format.CalendarWeekDayFormatter
import java.time.DayOfWeek
import java.time.LocalDate
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

    private lateinit var viewModel: DiaryViewModel
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DiaryFragmentBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(DiaryViewModel::class.java)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)

        binding.recyclerviewDiary.visibility = View.VISIBLE
        binding.recyclerviewDiary.adapter = DiaryRecyclerViewAdapter(arrayListOf())
        binding.recyclerviewDiary.layoutManager = LinearLayoutManager(activity)

        initCalendar()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.diaryList.observe(viewLifecycleOwner) {
            with (binding.recyclerviewDiary.adapter as DiaryRecyclerViewAdapter) {
                diaryList = it
                notifyDataSetChanged()
                binding.recyclerviewDiary.visibility = View.VISIBLE
            }
        }

        userViewModel.signed.observe(viewLifecycleOwner) {
            if (it) {
                updateForSignIn()
            }
            else {
                updateForSignOut()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.calendarView.currentDate = binding.calendarView.selectedDate
    }

    inner class DiaryRecyclerViewAdapter(
        var diaryList: ArrayList<Pair<ContentDTO, String>>,
    ): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val itemBinding =
                ItemDiaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)

            itemBinding.btnMore.setOnClickListener { view ->
                val popup = PopupMenu(activity, view)
                popup.menuInflater.inflate(R.menu.diary_popup, popup.menu)

                popup.setOnMenuItemClickListener { item ->
                    return@setOnMenuItemClickListener when (item.itemId) {
                        R.id.action_delete -> {

                            true
                        }
                        R.id.action_modify -> {
                            true
                        }
                        else -> false
                    }
                }

                popup.show()
            }

            return ViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as ViewHolder).bind(position)
        }

        override fun getItemCount() = diaryList.size

        inner class ViewHolder(private val itemBinding: ItemDiaryBinding): RecyclerView.ViewHolder(itemBinding.root) {
            fun bind(position: Int) {
                with(itemBinding) {
                    Glide.with(this@DiaryFragment)
                        .load(diaryList[position].first.imageUrl)
                        .into(communityitemContentImageview)

                    communityitemContentTextview.text = diaryList[position].first.content
                    textviewMealTime.text = diaryList[position].first.mealTime
                }
            }
        }
    }

    private fun updateForSignIn() {
        binding.calendarView.setOnDateChangedListener { widget, date, _ ->
            collapseCalendar()
            widget.currentDate = date
            binding.recyclerviewDiary.visibility = View.GONE
            viewModel.getDiary("${date.year}_${date.month}_${date.day}")
        }

        binding.btnAddDiary.setOnClickListener {

            binding.calendarView.selectedDate?.let {
                val mIntent = Intent(activity, AddDiaryActivity::class.java)

                mIntent.putExtra("date", "${it.year}_${it.month}_${it.day}")
                startActivity(mIntent)
            }
        }

        if (binding.calendarView.calendarMode == CalendarMode.WEEKS) {
            binding.recyclerviewDiary.visibility = View.VISIBLE
            binding.calendarView.selectedDate?.let {
                viewModel.getDiary("${it.year}_${it.month}_${it.day}")
            }
        }
    }

    private fun updateForSignOut() {
        binding.calendarView.setOnDateChangedListener { widget, date, _ ->
            collapseCalendar()
            widget.currentDate = date
        }

        binding.btnAddDiary.setOnClickListener(null)
        binding.recyclerviewDiary.visibility = View.GONE
    }

    private fun initCalendar() {
        binding.calendarView.currentDate = CalendarDay.today()
        binding.calendarView.selectedDate = CalendarDay.today()
        binding.calendarView.state().edit()
            .setMaximumDate(CalendarDay.today())
            .commit()

        collapseCalendar()

        binding.calendarView.setOnTitleClickListener {
            if (binding.calendarView.calendarMode == CalendarMode.WEEKS) {
                expandCalendar()
            }
            else {
                collapseCalendar()
            }
        }

        binding.calendarView.addDecorator(SundayDecorator(requireContext()))
        binding.calendarView.addDecorator(SaturdayDecorator(requireContext()))
    }

    private fun expandCalendar() {
        binding.calendarView.state().edit()
            .setCalendarDisplayMode(CalendarMode.MONTHS)
            .commit()

        binding.recyclerviewDiary.visibility = View.GONE
    }

    private fun collapseCalendar() {
        binding.calendarView.state().edit()
            .setCalendarDisplayMode(CalendarMode.WEEKS)
            .commit()

        binding.recyclerviewDiary.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}