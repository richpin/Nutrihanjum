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
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nutrihanjum.AddDiaryActivity
import com.example.nutrihanjum.R
import com.example.nutrihanjum.databinding.DiaryFragmentBinding
import com.example.nutrihanjum.databinding.ItemCommunityBinding
import com.example.nutrihanjum.model.ContentDTO
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

    private val diaryList = ArrayList<ContentDTO>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DiaryFragmentBinding.inflate(layoutInflater)

        collapseCalendar()

        binding.calendarView.currentDate = CalendarDay.today()
        binding.calendarView.selectedDate = CalendarDay.today()

        binding.recyclerviewDiary.visibility = View.VISIBLE

        binding.recyclerviewDiary.adapter = DiaryRecyclerViewAdapter(diaryList)
        binding.recyclerviewDiary.layoutManager = LinearLayoutManager(activity)

        setListeners()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.calendarView.currentDate = binding.calendarView.selectedDate
    }

    inner class DiaryRecyclerViewAdapter(
        private val diaryList: ArrayList<ContentDTO>
    ): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val itemBinding = ItemCommunityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            val holder = ViewHolder(itemBinding)

            return holder
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as ViewHolder).bind(position)
        }

        override fun getItemCount() = diaryList.size

        inner class ViewHolder(private val itemBinding: ItemCommunityBinding): RecyclerView.ViewHolder(itemBinding.root) {
            fun bind(position: Int) {
                Glide.with(this@DiaryFragment)
                    .load(diaryList[position].profileUrl)
                    .circleCrop()
                    .into(itemBinding.communityitemProfileImageview)

                Glide.with(this@DiaryFragment)
                    .load(diaryList[position].imageUrl)
                    .into(itemBinding.communityitemContentImageview)

                itemBinding.communityitemProfileTextview.text = diaryList[position].userId
                itemBinding.communityitemContentTextview.text = diaryList[position].content
            }
        }
    }

    private fun setListeners() {
        binding.calendarView.setOnDateChangedListener { widget, date, _ ->
            collapseCalendar()
            if (date != CalendarDay.from(2021, 10, 7)) {
                binding.recyclerviewDiary.visibility = View.GONE
            }
            widget.currentDate = date
        }

        binding.calendarView.setOnTitleClickListener {
            if (binding.calendarView.calendarMode == CalendarMode.WEEKS) {
                expandCalendar()
            }
            else {
                collapseCalendar()
            }
        }

        binding.btnAddDiary.setOnClickListener {
            startActivity(Intent(activity, AddDiaryActivity::class.java))
        }
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