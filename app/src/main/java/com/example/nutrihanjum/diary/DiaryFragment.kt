package com.example.nutrihanjum.diary

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.updateLayoutParams
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
import com.example.nutrihanjum.util.OnSwipeTouchListener
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

    private lateinit var dayBinder: CalendarDayBinder
    private lateinit var headerBinder: CalendarHeaderBinder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DiaryFragmentBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(DiaryViewModel::class.java)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)

        initView()
        addLiveDataObserver()

        return binding.root
    }



    private fun updateForSignIn() {
        binding.btnAddDiary.isClickable = true
        dayBinder.onDaySelectedListener = {
            viewModel.loadAllDiaryAtDate(getFormattedDate(it.year, it.monthValue, it.dayOfMonth))
        }
        binding.recyclerviewDiary.visibility = View.VISIBLE
    }


    private fun updateForSignOut() {
        binding.btnAddDiary.isClickable = false
        binding.recyclerviewDiary.visibility = View.GONE
        (binding.calendarView.dayBinder as CalendarDayBinder).onDaySelectedListener = null
    }


    private val popupClickListener = { view: View, diary: ContentDTO ->
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


    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        with(binding.recyclerviewDiary) {
            visibility = View.VISIBLE
            layoutManager = LinearLayoutManager(activity)
            adapter = DiaryRecyclerViewAdapter(viewModel.diaryList.value!!)
            adapter!!.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            (adapter as DiaryRecyclerViewAdapter).onPopupClickListener = popupClickListener
        }

        addDiaryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                dayBinder.selectedDate.let { date ->
                    viewModel.loadAllDiaryAtDate(getFormattedDate(date.year, date.monthValue, date.dayOfMonth))
                }
            }

            binding.btnAddDiary.isClickable = true
        }

        binding.btnAddDiary.setOnClickListener {
            it.isClickable = false
            val mIntent = Intent(activity, AddDiaryActivity::class.java)

            dayBinder.selectedDate.let { date ->
                mIntent.putExtra("date", getFormattedDate(date.year, date.monthValue, date.dayOfMonth))
            }

            addDiaryLauncher.launch(mIntent)
        }

        initCalendar()

        binding.barCalendar.setOnTouchListener(
            object: OnSwipeTouchListener(requireContext()) {
                override fun onSwipeBottom() {
                    expandCalendar()
                }

                override fun onSwipeTop() {
                    collapseCalendar()
                }
        })
    }


    private fun initCalendar() {
        dayBinder = CalendarDayBinder(binding.calendarView)
        headerBinder = CalendarHeaderBinder()

        binding.calendarView.dayBinder = dayBinder
        binding.calendarView.monthHeaderBinder = headerBinder

        val currentMonth = YearMonth.now()
        val firstDayOfWeek = DayOfWeek.SUNDAY

        binding.calendarView.daySize = CalendarView.sizeAutoWidth(dpToPx(35.toFloat(), context).toInt())
        binding.calendarView.setup(
            currentMonth.minusMonths(20),
            currentMonth,
            firstDayOfWeek
        )

        if (binding.calendarView.itemAnimator is SimpleItemAnimator) {
            (binding.calendarView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }

        binding.calendarView.scrollToMonth(currentMonth)

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

        binding.layoutCalendarMonth.setOnClickListener {
            if (binding.calendarView.isWeekMode()) {
                expandCalendar()
            } else {
                collapseCalendar()
            }
        }
    }


    private fun dpToPx(dp: Float, context: Context?): Float {
        return if (context != null) {
            val resources = context.resources
            val metrics = resources.displayMetrics
            dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        } else {
            val metrics = Resources.getSystem().displayMetrics
            dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun addLiveDataObserver() {
        viewModel.diaryList.observe(viewLifecycleOwner) {
            binding.recyclerviewDiary.adapter?.notifyDataSetChanged()
        }

        viewModel.diaryChanged.observe(viewLifecycleOwner) {
            if (it) {
                (binding.calendarView.dayBinder as CalendarDayBinder).selectedDate.let { date ->
                    viewModel.loadAllDiaryAtDate(getFormattedDate(date.year, date.monthValue, date.dayOfMonth))
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
        if (!binding.calendarView.isWeekMode()) return

        val firstDate = binding.calendarView.findFirstVisibleDay()?.date ?: return
        val lastDate = minOf( dayBinder.lastDate,
            binding.calendarView.findLastVisibleDay()?.date ?: return)

        val dest = if (dayBinder.selectedDate in firstDate..lastDate) {
            dayBinder.selectedDate.yearMonth
        } else {
            lastDate.yearMonth
        }

        val oldHeight = binding.calendarView.daySize.height
        val newHeight = oldHeight * 6

        val animator = ValueAnimator.ofInt(oldHeight, newHeight)
        animator.addUpdateListener {
            binding.calendarView.updateLayoutParams {
                height = it.animatedValue as Int
            }
        }

        binding.calendarView.updateMonthConfigurationAsync(
            inDateStyle = InDateStyle.ALL_MONTHS,
            maxRowCount = 6,
            hasBoundaries = true
        ) {
            binding.calendarView.scrollToMonth(dest)
            animator.start()
        }


    }

    private fun collapseCalendar() {
        if (binding.calendarView.isWeekMode()) return

        val firstDate = binding.calendarView.findFirstVisibleDay()?.date ?: return
        val lastDate = binding.calendarView.findLastVisibleDay()?.date ?: return

        val newHeight = binding.calendarView.daySize.height
        val oldHeight = newHeight * 6

        val animator = ValueAnimator.ofInt(oldHeight, newHeight)
        animator.addUpdateListener {
            binding.calendarView.updateLayoutParams {
                height = it.animatedValue as Int
            }
        }

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

        animator.start()
    }

    private fun CalendarView.isWeekMode() = binding.calendarView.maxRowCount == 1

    private fun getFormattedDate(year: Int, month: Int, date: Int)
        = "${year}_${month}_${date}"


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}