package com.example.nutrihanjum.diary

import android.animation.ValueAnimator
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
import java.time.LocalDate
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
    private lateinit var detailDiaryLauncher: ActivityResultLauncher<Intent>

    private var updatePosition = RecyclerView.NO_POSITION
    private var updateDate: Int = 0

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
        addActivityLauncher()

        return binding.root
    }


    private fun initCommonView() {
        with(binding.recyclerviewDiary) {
            layoutManager = LinearLayoutManager(activity)
            adapter = DiaryRecyclerViewAdapter(arrayListOf())
            adapter!!.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            (adapter as DiaryRecyclerViewAdapter).onDiaryClickListener = diaryClickListener
        }

        binding.btnAddDiary.setOnClickListener {
            it.isClickable = false
            val mIntent = Intent(activity, AddDiaryActivity::class.java)

            dayBinder.selectedDate.let { date ->
                mIntent.putExtra("date", getFormattedDate(date))
            }
            updateDate = getFormattedDate(dayBinder.selectedDate)
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


    private fun addActivityLauncher() {
        addDiaryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                if (it.data?.hasExtra("addedContent") == true) {
                    val data = it.data?.getSerializableExtra("addedContent") as ContentDTO
                    viewModel.addToMap(data)
                    binding.recyclerviewDiary.adapter?.notifyItemChanged(0)
                    binding.recyclerviewDiary.adapter?.notifyItemInserted(viewModel.diaryMap.value!![updateDate]!!.size)
                    binding.calendarView.notifyDateChanged(dayBinder.selectedDate)
                }
            }

            binding.btnAddDiary.isClickable = true
        }

        detailDiaryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.data?.hasExtra("modifiedContent") == true) {
                val data = it.data?.getSerializableExtra("modifiedContent") as ContentDTO

                viewModel.diaryMap.value!![data.date]!![updatePosition] = data
                binding.recyclerviewDiary.adapter?.notifyItemChanged(0)
                binding.recyclerviewDiary.adapter?.notifyItemChanged(updatePosition + 1)
                binding.calendarView.notifyDateChanged(dayBinder.selectedDate)
                updatePosition = RecyclerView.NO_POSITION
            }
            else if (it.data?.hasExtra("deletedContent") == true) {
                viewModel.diaryMap.value!![updateDate]!!.removeAt(updatePosition)
                binding.recyclerviewDiary.adapter?.notifyItemChanged(0)
                binding.recyclerviewDiary.adapter?.notifyItemRemoved(updatePosition + 1)
                binding.calendarView.notifyDateChanged(dayBinder.selectedDate)
                updatePosition = RecyclerView.NO_POSITION
            }
        }
    }


    private fun updateForSignIn() {
        viewModel.loadAllDiary(getFormattedDate(YearMonth.now().minusMonths(10).atDay(1)))
        binding.btnAddDiary.isClickable = true

        dayBinder.onDaySelectedListener = {
            val data = viewModel.getDiaryList(getFormattedDate(it))

            (binding.recyclerviewDiary.adapter as DiaryRecyclerViewAdapter).updateDiary(data)
        }

        binding.recyclerviewDiary.visibility = View.VISIBLE
    }


    private fun updateForSignOut() {
        binding.btnAddDiary.isClickable = false
        binding.recyclerviewDiary.visibility = View.GONE
        (binding.calendarView.dayBinder as CalendarDayBinder).onDaySelectedListener = null
        viewModel.clearDairyForSignOut()
        binding.calendarView.notifyCalendarChanged()
    }



    private fun initCalendar() {
        dayBinder = CalendarDayBinder(binding.calendarView, viewModel.diaryMap.value!!)
        binding.calendarView.dayBinder = dayBinder

        headerBinder = CalendarHeaderBinder()
        binding.calendarView.monthHeaderBinder = headerBinder

        if (binding.calendarView.itemAnimator is SimpleItemAnimator) {
            (binding.calendarView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }

        val currentMonth = YearMonth.now()
        val firstDayOfWeek = DayOfWeek.SUNDAY

        binding.calendarView.setup(
            currentMonth.minusMonths(10),
            currentMonth,
            firstDayOfWeek
        )

        binding.calendarView.scrollToDate(dayBinder.selectedDate)

        @SuppressLint("SetTextI18n")
        binding.calendarView.monthScrollListener = {
            binding.calendarView.notifyDateChanged(dayBinder.selectedDate)

            if (!binding.calendarView.isWeekMode()) {
                binding.textviewCalendarMonth.text = "${it.year}년 ${it.month}월"
            } else {
                val lastDate = it.weekDays.last().last().date
                val yearMonth = if (currentMonth.isBefore(lastDate.yearMonth)) currentMonth else lastDate.yearMonth
                binding.textviewCalendarMonth.text = "${yearMonth.year}년 ${yearMonth.monthValue}월"
            }
        }
    }


    private fun addLiveDataObserver() {
        viewModel.diaryMap.observe(viewLifecycleOwner) {
            binding.calendarView.notifyCalendarChanged()

            val data = viewModel.getDiaryList(getFormattedDate(dayBinder.selectedDate))

            (binding.recyclerviewDiary.adapter as DiaryRecyclerViewAdapter).updateDiary(data)
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


    private val expandAnimator: ValueAnimator by lazy {

        val animator = ValueAnimator.ofFloat(1f, -1f)
        animator.addUpdateListener {
            binding.calendarModeController.scaleY = it.animatedValue as Float
        }
        animator
    }

    private fun expandCalendar() {
        if (!binding.calendarView.isWeekMode()) return

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
        expandAnimator.start()
    }


    private val collapseAnimator: ValueAnimator by lazy {

        val animator = ValueAnimator.ofFloat(-1f, 1f)
        animator.addUpdateListener {
            binding.calendarModeController.scaleY = it.animatedValue as Float
        }
        animator
    }


    private fun collapseCalendar() {
        if (binding.calendarView.isWeekMode()) return

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

        collapseAnimator.start()
    }


    private fun CalendarView.isWeekMode() = maxRowCount == 1

    private fun getFormattedDate(date: LocalDate) : Int {
        return date.year * 10000 + date.monthValue * 100 + date.dayOfMonth
    }

    private val diaryClickListener = { item: ContentDTO, pos: Int ->
        updateDate = item.date
        updatePosition = pos

        val mIntent = Intent(activity, DiaryDetailActivity::class.java)
        mIntent.putExtra("content", item)
        detailDiaryLauncher.launch(mIntent)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}