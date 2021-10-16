package com.example.nutrihanjum.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutrihanjum.AddDiaryActivity
import com.example.nutrihanjum.R
import com.example.nutrihanjum.RecyclerViewAdapter.DiaryRecyclerViewAdapter
import com.example.nutrihanjum.databinding.DiaryFragmentBinding
import com.example.nutrihanjum.decorator.SaturdayDecorator
import com.example.nutrihanjum.decorator.SundayDecorator
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.viewmodel.DiaryViewModel
import com.example.nutrihanjum.viewmodel.UserViewModel
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode

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

    private lateinit var addDiaryLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DiaryFragmentBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(DiaryViewModel::class.java)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)

        addDiaryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                binding.calendarView.selectedDate?.let { date ->
                    viewModel.loadAllDiaryAtDate(getFormattedDate(date.year, date.month, date.day))
                }
            }

            binding.btnAddDiary.isClickable = true
        }

        initView()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addLiveDataObserver()
    }

    override fun onResume() {
        super.onResume()
        binding.calendarView.currentDate = binding.calendarView.selectedDate
    }

    private fun updateForSignIn() {
        binding.calendarView.setOnDateChangedListener { widget, date, _ ->
            collapseCalendar()
            widget.currentDate = date
            binding.recyclerviewDiary.visibility = View.GONE
            viewModel.loadAllDiaryAtDate(getFormattedDate(date.year, date.month, date.day))
        }

        binding.btnAddDiary.isClickable = true

        if (binding.calendarView.calendarMode == CalendarMode.WEEKS) {
            binding.recyclerviewDiary.visibility = View.VISIBLE
            binding.calendarView.selectedDate?.let {
                viewModel.loadAllDiaryAtDate(getFormattedDate(it.year, it.month, it.day))
            }
        }
    }

    private fun updateForSignOut() {
        binding.calendarView.setOnDateChangedListener { widget, date, _ ->
            collapseCalendar()
            widget.currentDate = date
        }
        binding.btnAddDiary.isClickable = false
        binding.recyclerviewDiary.visibility = View.GONE
    }

    private fun initCalendar() {
        with (binding.calendarView) {
            currentDate = CalendarDay.today()
            selectedDate = CalendarDay.today()
            state().edit()
                .setMaximumDate(CalendarDay.today())
                .commit()

            collapseCalendar()

            setOnTitleClickListener {
                if (calendarMode == CalendarMode.WEEKS) {
                    expandCalendar()
                } else {
                    collapseCalendar()
                }
            }

            addDecorator(SundayDecorator(requireContext()))
            addDecorator(SaturdayDecorator(requireContext()))
        }
    }


    private fun makePopupClickListener() = { view: View, diary: ContentDTO ->
        val popup = PopupMenu(activity, view)
        popup.menuInflater.inflate(R.menu.diary_popup, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete -> {
                    viewModel.deleteDiary(diary.id, diary.imageUrl)
                    true
                }
                R.id.action_modify -> {
                    val mIntent = Intent(activity, AddDiaryActivity::class.java)
                    mIntent.putExtra("content", diary)

                    addDiaryLauncher.launch(mIntent)

                    true
                }
                else -> false
            }
        }

        popup.show()
    }


    private fun initView() {
        with(binding.recyclerviewDiary) {
            visibility = View.VISIBLE
            layoutManager = LinearLayoutManager(activity)
            adapter = DiaryRecyclerViewAdapter(arrayListOf())
            (adapter as DiaryRecyclerViewAdapter).onPopupClickListener = makePopupClickListener()
        }

        binding.btnAddDiary.setOnClickListener {
            binding.calendarView.selectedDate?.let { date ->
                it.isClickable = false
                val mIntent = Intent(activity, AddDiaryActivity::class.java)

                mIntent.putExtra("date", getFormattedDate(date.year, date.month, date.day))
                addDiaryLauncher.launch(mIntent)
            }
        }

        initCalendar()
    }


    private fun addLiveDataObserver() {
        viewModel.diaryList.observe(viewLifecycleOwner) {
            with (binding.recyclerviewDiary.adapter as DiaryRecyclerViewAdapter) {
                diaryList = it
                notifyDataSetChanged()
                binding.recyclerviewDiary.visibility = View.VISIBLE
            }
        }

        viewModel.diaryChanged.observe(viewLifecycleOwner) {
            if (it) {
                binding.calendarView.selectedDate?.let { date ->
                     viewModel.loadAllDiaryAtDate(getFormattedDate(date.year, date.month, date.day))
                }
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

    private fun getFormattedDate(year: Int, month: Int, date: Int)
        = "${year}_${month}_${date}"


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}