package com.example.nutrihanjum.diary

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.nutrihanjum.databinding.DiaryFragmentBinding
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.databinding.LayoutPopupYearMonthPickerBinding
import com.example.nutrihanjum.diary.viewcontainer.CalendarDayBinder
import com.example.nutrihanjum.diary.viewcontainer.CalendarHeaderBinder
import com.kizitonwose.calendarview.model.InDateStyle
import com.kizitonwose.calendarview.utils.yearMonth
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.min

class DiaryFragment: Fragment() {

    private var _binding: DiaryFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CalendarDiaryViewModel

    private lateinit var addDiaryLauncher: ActivityResultLauncher<Intent>
    private lateinit var detailDiaryLauncher: ActivityResultLauncher<Intent>

    private lateinit var dayBinder: CalendarDayBinder
    private lateinit var headerBinder: CalendarHeaderBinder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DiaryFragmentBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(CalendarDiaryViewModel::class.java)

        if (savedInstanceState == null) {
            viewModel.refreshAll()
        }

        initCalendar()
        initCommonView()
        addActivityLauncher()
        addLiveDataObserver()

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

            mIntent.putExtra("date", viewModel.getFormattedDate(viewModel.selectedDate))

            addDiaryLauncher.launch(mIntent)
        }

        binding.calendarModeController.setOnClickListener {
            if (viewModel.isWeekMode) {
                expandCalendar()
            } else {
                collapseCalendar()
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshAll()
        }

        initYearMonthPicker()
    }


    private fun initYearMonthPicker() {
        val yearMonthPicker = Dialog(requireContext())
        val pickerBinding = LayoutPopupYearMonthPickerBinding.inflate(layoutInflater)

        yearMonthPicker.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        yearMonthPicker.setContentView(pickerBinding.root)

        with(pickerBinding) {
            pickerYear.minValue = viewModel.signedDate.year
            pickerYear.maxValue = viewModel.today.year
            pickerYear.displayedValues = (pickerYear.minValue..viewModel.today.year).map { "${it}년" }.toTypedArray()
            pickerYear.setOnValueChangedListener { _, oldVal, newVal ->
                if (oldVal == newVal) return@setOnValueChangedListener

                pickerMonth.maxValue = if (newVal == viewModel.today.year) {
                    pickerMonth.value = min(viewModel.today.monthValue, pickerMonth.value)
                    viewModel.today.monthValue
                } else 12

                pickerMonth.displayedValues = (pickerMonth.minValue..pickerMonth.maxValue).map { "${it}월" }.toTypedArray()
            }
        }

        setDatePickerListener(yearMonthPicker, pickerBinding)
    }



    private fun setDatePickerListener(picker: Dialog, pickerBinding: LayoutPopupYearMonthPickerBinding) {
        binding.textviewCalendarMonth.setOnClickListener {
            with(pickerBinding) {
                val currentMonth = viewModel.currentDate.monthValue
                val currentYear = viewModel.currentDate.year

                pickerMonth.minValue = 1
                pickerMonth.maxValue = if (currentYear == viewModel.today.year) viewModel.today.monthValue else 12
                pickerMonth.displayedValues = (pickerMonth.minValue..pickerMonth.maxValue).map { "${it}월" }.toTypedArray()

                pickerMonth.value = currentMonth
                pickerYear.value = currentYear
            }

            picker.show()
        }

        pickerBinding.btnPickYearMonth.setOnClickListener {
            scrollToPickedMonth(YearMonth.of(pickerBinding.pickerYear.value, pickerBinding.pickerMonth.value))
            picker.dismiss()
        }

        pickerBinding.btnCancelPick.setOnClickListener {
            picker.dismiss()
        }
    }



    private fun scrollToPickedMonth(yearMonth: YearMonth) {
        val destDate = yearMonth.atDay(1)

        if (yearMonth in viewModel.firstMonth..viewModel.lastMonth) {
            binding.calendarView.scrollToDate(destDate)
        }
        else {
            viewModel.lastMonth = yearMonth.plusMonths(5)
            viewModel.firstMonth = viewModel.lastMonth.minusMonths(10)
            binding.calendarView.setup(viewModel.firstMonth, viewModel.lastMonth, DayOfWeek.SUNDAY)
            binding.calendarView.scrollToDate(destDate)

            viewModel.refreshAll()
        }
    }



    private fun addActivityLauncher() {
        addDiaryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                if (it.data?.hasExtra("addedContent") == true) {
                    val data = it.data?.getSerializableExtra("addedContent") as ContentDTO
                    viewModel.addToMap(data)
                    binding.calendarView.notifyDateChanged(viewModel.selectedDate)

                    if (viewModel.getFormattedDate(viewModel.selectedDate) == data.date) {
                        binding.recyclerviewDiary.adapter?.notifyItemChanged(0)
                        binding.recyclerviewDiary.adapter?.notifyItemInserted(viewModel.diaryMap.value!![data.date]!!.size)
                    }
                }
            }

            binding.btnAddDiary.isClickable = true
        }

        detailDiaryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.data?.hasExtra("modifiedContent") == true) {
                val data = it.data?.getSerializableExtra("modifiedContent") as ContentDTO

                viewModel.diaryMap.value!![data.date]!![viewModel.updatePosition] = data
                binding.calendarView.notifyDateChanged(viewModel.selectedDate)

                if (viewModel.getFormattedDate(viewModel.selectedDate) == data.date) {
                    binding.recyclerviewDiary.adapter?.notifyItemChanged(0)
                    binding.recyclerviewDiary.adapter?.notifyItemChanged(viewModel.updatePosition + 1)
                }
            }
            else if (it.data?.hasExtra("deletedContent") == true) {
                val data = it.data?.getSerializableExtra("deletedContent") as ContentDTO

                viewModel.diaryMap.value!![data.date]!!.removeAt(viewModel.updatePosition)
                binding.calendarView.notifyDateChanged(viewModel.selectedDate)

                if (viewModel.getFormattedDate(viewModel.selectedDate) == data.date) {
                    binding.recyclerviewDiary.adapter?.notifyItemChanged(0)
                    binding.recyclerviewDiary.adapter?.notifyItemRemoved(viewModel.updatePosition + 1)
                }
            }
        }
    }



    private fun initCalendar() {
        dayBinder = CalendarDayBinder(binding.calendarView, viewModel)
        binding.calendarView.dayBinder = dayBinder

        headerBinder = CalendarHeaderBinder()
        binding.calendarView.monthHeaderBinder = headerBinder

        if (binding.calendarView.itemAnimator is SimpleItemAnimator) {
            (binding.calendarView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }

        binding.calendarView.setup(
            viewModel.firstMonth,
            viewModel.lastMonth,
            DayOfWeek.SUNDAY
        )

        if (viewModel.isWeekMode) {
            binding.calendarView.updateMonthConfiguration(
                inDateStyle = InDateStyle.FIRST_MONTH,
                maxRowCount = 1,
                hasBoundaries = false
            )
        }
        else {
            binding.calendarView.updateMonthConfiguration(
                inDateStyle = InDateStyle.ALL_MONTHS,
                maxRowCount = 6,
                hasBoundaries = true
            )
        }

        binding.calendarView.scrollToDate(viewModel.currentDate)
        setCalendarListener()
    }



    private fun setCalendarListener() {
        dayBinder.onDaySelectedListener = {
            val data = viewModel.getDiaryList(it)

            (binding.recyclerviewDiary.adapter as DiaryRecyclerViewAdapter).updateDiary(data)
        }

        @SuppressLint("SetTextI18n")
        binding.calendarView.monthScrollListener = {
            if (!viewModel.isWeekMode) {
                binding.textviewCalendarMonth.text = "${it.year}년 ${it.month}월"
                viewModel.currentDate = it.yearMonth.atDay(1)
            }
            else {
                viewModel.currentDate = it.weekDays.last().last().date
                val yearMonth = viewModel.currentDate.yearMonth

                binding.textviewCalendarMonth.text = "${yearMonth.year}년 ${yearMonth.monthValue}월"
            }
        }
    }



    private fun addLiveDataObserver() {
        viewModel.diaryMap.observe(viewLifecycleOwner) {
            binding.calendarView.notifyCalendarChanged()

            val data = viewModel.getDiaryList(viewModel.selectedDate)

            (binding.recyclerviewDiary.adapter as DiaryRecyclerViewAdapter).updateDiary(data)
            binding.swipeRefreshLayout.isRefreshing = false
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
        val firstDate = maxOf(viewModel.firstVisibleDate,
            binding.calendarView.findFirstVisibleDay()?.date ?: return)
        val lastDate = viewModel.currentDate

        val dest = if (viewModel.selectedDate in firstDate..lastDate) {
            viewModel.selectedDate.yearMonth
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
        viewModel.isWeekMode = false
    }



    private val collapseAnimator: ValueAnimator by lazy {

        val animator = ValueAnimator.ofFloat(-1f, 1f)
        animator.addUpdateListener {
            binding.calendarModeController.scaleY = it.animatedValue as Float
        }
        animator
    }

    private fun collapseCalendar() {
        binding.calendarView.updateMonthConfiguration(
            inDateStyle = InDateStyle.FIRST_MONTH,
            maxRowCount = 1,
            hasBoundaries = false
        )

        if (viewModel.selectedDate.yearMonth == viewModel.currentDate.yearMonth) {
            binding.calendarView.scrollToDate(viewModel.selectedDate)
        }
        else {
            binding.calendarView.scrollToDate(viewModel.currentDate)
        }

        collapseAnimator.start()
        viewModel.isWeekMode = true
    }


    private val diaryClickListener = { item: ContentDTO, pos: Int ->
        viewModel.updatePosition = pos

        val mIntent = Intent(activity, DiaryDetailActivity::class.java)
        mIntent.putExtra("content", item)
        detailDiaryLauncher.launch(mIntent)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}