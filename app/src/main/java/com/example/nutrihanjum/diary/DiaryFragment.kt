package com.example.nutrihanjum.diary

import android.annotation.SuppressLint
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
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.nutrihanjum.R
import com.example.nutrihanjum.databinding.DiaryFragmentBinding
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.UserViewModel
import com.example.nutrihanjum.diary.viewcontainer.CalendarDayBinder
import com.example.nutrihanjum.diary.viewcontainer.CalendarHeaderBinder
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.InDateStyle
import com.kizitonwose.calendarview.utils.yearMonth
import java.time.DayOfWeek
import java.time.YearMonth

class DiaryFragment: Fragment() {

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
    private var modifiedPosition = RecyclerView.NO_POSITION

    private lateinit var dayBinder: CalendarDayBinder
    private lateinit var headerBinder: CalendarHeaderBinder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DiaryFragmentBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(DiaryViewModel::class.java)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)

        initCommonView()
        initCalendar()
        addLiveDataObserver()

        addDiaryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
               if (it.data?.hasExtra("addedContent") == true) {
                   val data = it.data?.getSerializableExtra("addedContent") as ContentDTO

                   viewModel.diaryList.value!!.add(data)
                   binding.recyclerviewDiary.adapter?.notifyItemInserted(viewModel.diaryList.value!!.size - 1)
               }
               else if (modifiedPosition != RecyclerView.NO_POSITION) {
                   val data = it.data?.getSerializableExtra("modifiedContent") as ContentDTO

                   viewModel.diaryList.value!![modifiedPosition] = data
                   binding.recyclerviewDiary.adapter?.notifyItemChanged(modifiedPosition)
                   modifiedPosition = RecyclerView.NO_POSITION
               }
            }

            binding.btnAddDiary.isClickable = true
        }

        return binding.root
    }


    private fun initCommonView() {
        with(binding.recyclerviewDiary) {
            layoutManager = LinearLayoutManager(activity)
            adapter = DiaryRecyclerViewAdapter(viewModel.diaryList.value!!)
            adapter!!.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            (adapter as DiaryRecyclerViewAdapter).onPopupClickListener = popupClickListener
        }

        binding.btnAddDiary.setOnClickListener {
            it.isClickable = false
            val mIntent = Intent(activity, AddDiaryActivity::class.java)

            dayBinder.selectedDate.let { date ->
                mIntent.putExtra("date", getFormattedDate(date.year, date.monthValue, date.dayOfMonth))
            }

            addDiaryLauncher.launch(mIntent)
        }

        binding.layoutCalendarMonth.setOnClickListener {
            if (binding.calendarView.isWeekMode()) {
                expandCalendar()
            } else {
                collapseCalendar()
            }
        }
    }


    private fun updateForSignIn() {
        binding.btnAddDiary.isClickable = true
        dayBinder.onDaySelectedListener = {
            viewModel.loadAllDiaryAtDate(getFormattedDate(it.year, it.monthValue, it.dayOfMonth))
        }

        binding.recyclerviewDiary.visibility = View.VISIBLE

        dayBinder.selectedDate.let {
            viewModel.loadAllDiaryAtDate(getFormattedDate(it.year, it.monthValue, it.dayOfMonth))
        }
    }


    private fun updateForSignOut() {
        binding.btnAddDiary.isClickable = false
        binding.recyclerviewDiary.visibility = View.GONE
        (binding.calendarView.dayBinder as CalendarDayBinder).onDaySelectedListener = null
    }



    private fun initCalendar() {
        dayBinder = CalendarDayBinder(binding.calendarView)
        binding.calendarView.dayBinder = dayBinder

        headerBinder = CalendarHeaderBinder()
        binding.calendarView.monthHeaderBinder = headerBinder

        if (binding.calendarView.itemAnimator is SimpleItemAnimator) {
            (binding.calendarView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }

        val currentMonth = YearMonth.now()
        val firstDayOfWeek = DayOfWeek.SUNDAY

        binding.calendarView.setup(
            currentMonth.minusMonths(20),
            currentMonth,
            firstDayOfWeek
        )

        binding.calendarView.scrollToDate(dayBinder.selectedDate)

        @SuppressLint("SetTextI18n")
        binding.calendarView.monthScrollListener = {
            binding.calendarView.notifyDateChanged(dayBinder.selectedDate)

            if (!binding.calendarView.isWeekMode()) {
                binding.textviewCalendarMonth.text = "${it.month}월"
            } else {
                val firstDate = it.weekDays.first().first().date
                val lastDate = it.weekDays.last().last().date

                if (firstDate.yearMonth == lastDate.yearMonth) {
                    binding.textviewCalendarMonth.text = "${firstDate.monthValue}월"
                } else {
                    binding.textviewCalendarMonth.text = "${firstDate.monthValue}월 - ${lastDate.monthValue}월"
                }
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun addLiveDataObserver() {
        viewModel.diaryList.observe(viewLifecycleOwner) {
            binding.recyclerviewDiary.adapter?.notifyDataSetChanged()
        }

        userViewModel.signed.observe(viewLifecycleOwner) {
            if (it) {
                updateForSignIn()
            }
            else {
                updateForSignOut()
            }
        }

        viewModel.diaryDeleteResult.observe(viewLifecycleOwner) {
            if (it && modifiedPosition != RecyclerView.NO_POSITION) {
                viewModel.diaryList.value?.removeAt(modifiedPosition)
                binding.recyclerviewDiary.adapter?.notifyItemRemoved(modifiedPosition)
                modifiedPosition = RecyclerView.NO_POSITION
            }
        }
    }


    private fun expandCalendar() {
        if (!binding.calendarView.isWeekMode()) return

        binding.barCalendar.scaleY = -1f

        val firstDate = binding.calendarView.findFirstVisibleDay()?.date ?: return
        val lastDate = minOf( dayBinder.lastDate,
            binding.calendarView.findLastVisibleDay()?.date ?: return)

        val dest = if (dayBinder.selectedDate in firstDate..lastDate) {
            dayBinder.selectedDate.yearMonth
        } else {
            lastDate.yearMonth
        }

        binding.calendarView.updateMonthConfiguration(
            inDateStyle = InDateStyle.ALL_MONTHS,
            maxRowCount = 6,
            hasBoundaries = true
        )
        binding.calendarView.scrollToMonth(dest)
    }


    private fun collapseCalendar() {
        if (binding.calendarView.isWeekMode()) return

        binding.barCalendar.scaleY = 1f

        val firstDate = binding.calendarView.findFirstVisibleDay()?.date ?: return
        val lastDate = binding.calendarView.findLastVisibleDay()?.date ?: return


        binding.calendarView.updateMonthConfiguration(
            inDateStyle = InDateStyle.FIRST_MONTH,
            maxRowCount = 1,
            hasBoundaries = false
        )

        if (dayBinder.selectedDate in firstDate..lastDate) {
            binding.calendarView.scrollToDate(dayBinder.selectedDate)
        } else {
            binding.calendarView.scrollToDate(firstDate)
        }
    }

    private fun CalendarView.isWeekMode() = maxRowCount == 1

    private fun getFormattedDate(year: Int, month: Int, date: Int)
        = "${year}_${month}_${date}"


    private val popupClickListener = { view: View, diary: ContentDTO, pos: Int ->
        val popup = PopupMenu(activity, view)
        popup.menuInflater.inflate(R.menu.diary_popup, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete -> {
                    modifiedPosition = pos
                    viewModel.deleteDiary(diary.id, diary.imageUrl)
                    true
                }
                R.id.action_modify -> {
                    val mIntent = Intent(activity, AddDiaryActivity::class.java)
                    mIntent.putExtra("content", diary)

                    modifiedPosition = pos
                    addDiaryLauncher.launch(mIntent)

                    true
                }
                else -> false
            }
        }

        popup.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}