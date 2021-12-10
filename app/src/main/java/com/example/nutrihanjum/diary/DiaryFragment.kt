package com.example.nutrihanjum.diary

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
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
import com.example.nutrihanjum.databinding.LayoutPopupYearMonthPickerBinding
import com.example.nutrihanjum.diary.viewcontainer.CalendarDayBinder
import com.example.nutrihanjum.diary.viewcontainer.CalendarHeaderBinder
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.InDateStyle
import com.kizitonwose.calendarview.utils.yearMonth
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.min

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

        if (savedInstanceState == null) {
            viewModel.loadAllDiary(getFormattedDate(viewModel.firstMonth.atDay(1)), getFormattedDate(viewModel.lastMonth.atEndOfMonth()))
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

            viewModel.selectedDate.let { date ->
                mIntent.putExtra("date", getFormattedDate(date))
            }
            updateDate = getFormattedDate(viewModel.selectedDate)
            addDiaryLauncher.launch(mIntent)
        }

        binding.calendarModeController.setOnClickListener {
            if (viewModel.isWeekMode) {
                expandCalendar()
            } else {
                collapseCalendar()
            }
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

                pickerMonth.value = min(viewModel.today.monthValue, pickerMonth.value)
                pickerMonth.minValue = if (newVal == viewModel.signedDate.year) viewModel.signedDate.monthValue else 1
                pickerMonth.maxValue = if (newVal == viewModel.lastMonth.year) viewModel.lastMonth.monthValue else 12
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

                pickerMonth.minValue = if (currentYear == viewModel.signedDate.year) viewModel.signedDate.monthValue else 1
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
        val destDate = if (yearMonth == viewModel.signedDate.yearMonth) {
            viewModel.signedDate
        } else {
            yearMonth.atDay(1)
        }

        if (yearMonth.isBefore(viewModel.firstMonth)) {
            val endMonth = viewModel.firstMonth.minusMonths(1)

            viewModel.firstMonth = yearMonth.minusMonths(5)
            binding.calendarView.setup(viewModel.firstMonth, viewModel.lastMonth, DayOfWeek.SUNDAY)
            binding.calendarView.scrollToDate(destDate)

            viewModel.loadAllDiary(
                getFormattedDate(viewModel.firstVisibleDate),
                getFormattedDate(endMonth.atEndOfMonth())
            )

        }
        else {
            binding.calendarView.scrollToDate(destDate)
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
                    binding.calendarView.notifyDateChanged(viewModel.selectedDate)
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
                binding.calendarView.notifyDateChanged(viewModel.selectedDate)
                updatePosition = RecyclerView.NO_POSITION
            }
            else if (it.data?.hasExtra("deletedContent") == true) {
                viewModel.diaryMap.value!![updateDate]!!.removeAt(updatePosition)
                binding.recyclerviewDiary.adapter?.notifyItemChanged(0)
                binding.recyclerviewDiary.adapter?.notifyItemRemoved(updatePosition + 1)
                binding.calendarView.notifyDateChanged(viewModel.selectedDate)
                updatePosition = RecyclerView.NO_POSITION
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

        setCalendarListener()

        if (viewModel.isWeekMode) {
            collapseCalendar()
        }
        else {
            expandCalendar()
        }

        binding.calendarView.scrollToDate(viewModel.currentDate)
    }



    private fun setCalendarListener() {
        dayBinder.onDaySelectedListener = {
            val data = viewModel.getDiaryList(getFormattedDate(it))

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
                Log.wtf("TEST", viewModel.currentDate.toString())

                binding.textviewCalendarMonth.text = "${yearMonth.year}년 ${yearMonth.monthValue}월"
            }
        }
    }




    private fun addLiveDataObserver() {
        viewModel.diaryMap.observe(viewLifecycleOwner) {
            binding.calendarView.notifyCalendarChanged()

            val data = viewModel.getDiaryList(getFormattedDate(viewModel.selectedDate))

            (binding.recyclerviewDiary.adapter as DiaryRecyclerViewAdapter).updateDiary(data)
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
        if (!viewModel.isWeekMode) return

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
        if (viewModel.isWeekMode) return


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