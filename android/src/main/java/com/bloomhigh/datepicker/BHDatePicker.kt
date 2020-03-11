package com.bloomhigh.datepicker

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View.OnTouchListener
import androidx.constraintlayout.widget.ConstraintLayout
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.RCTEventEmitter
import java.text.DateFormatSymbols
import java.util.*
import java.util.concurrent.TimeUnit

@SuppressLint("ClickableViewAccessibility")
class BHDatePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val pMonth: NumberPicker
    private val pDay: NumberPicker
    private val pYear: NumberPicker
    private val months = DateFormatSymbols().months
    private val c = Calendar.getInstance()
    private var vibe: Vibrator?
    private val vibeDebouncer = Debouncer()
    private val onChangeDebouncer = Debouncer()
    private var minimumDate: CustomDate
    private var maximumDate: CustomDate

    companion object {
        private const val DEFAULT_MAX_YEARS = 100
        private const val DEFAULT_MIN_YEARS = 4

    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_date_picker, this, true)

        vibe = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?

        pMonth = findViewById(R.id.pMonth)
        pDay = findViewById(R.id.pDay)
        pYear = findViewById(R.id.pYear)

        setListeners()

        pYear.setFormatter { "$it" }
        pMonth.setFormatter {
            if (it < months.size) {
                months[it]
            } else {
                "$it ${months.size}"
            }
//            it.toString()
        }

        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        minimumDate = CustomDate("${year - DEFAULT_MAX_YEARS}-$month-$day")
        maximumDate = CustomDate("${year - DEFAULT_MIN_YEARS}-$month-$day")

        setInitialState()

    }

    var callback: DateSelectionCallback? = null

    interface DateSelectionCallback {
        fun onDateSelected(date: String)
    }

    private fun setListeners() {
        val touchListener = OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val previous = pDay.maxValue
                val new = calcMaxDayForMonth()
                if (previous != new) {
                    pDay.maxValue = new
                }
            }
            super.onTouchEvent(event)
        }
        pMonth.setOnTouchListener(touchListener)
        pYear.setOnTouchListener(touchListener)
        val valueChangeListener = NumberPicker.OnValueChangeListener { _, _, _ ->
            recalcMinMax()
            vibrate()
            onChangeDebouncer.debounce(Nothing::class, onChangeRunnable, 150, TimeUnit.MILLISECONDS)
        }
        pYear.setOnValueChangedListener(valueChangeListener)
        pDay.setOnValueChangedListener(valueChangeListener)
        pMonth.setOnValueChangedListener(valueChangeListener)
    }

    private fun setInitialState() {
        pYear.minValue = minimumDate.year()
        pYear.maxValue = maximumDate.year()

        pMonth.minValue = 0
        pMonth.maxValue = 11

        pMonth.order = NumberPicker.ASCENDING
        pMonth.value = maximumDate.month()

        pYear.value = maximumDate.year()

        pDay.minValue = 1
        pDay.maxValue = calcMaxDayForMonth()
    }

    private val onChangeRunnable = {
        onChange()
    }

    private fun onChange() {
        val year = "${pYear.value}"
        val month = if (pMonth.value + 1 < 10) {
            "0${pMonth.value + 1}"
        } else {
            "${pMonth.value + 1}"
        }
        val day = if (pDay.value < 10) {
            "0${pDay.value}"
        } else {
            "${pDay.value}"
        }
        val result = "$year-$month-$day"

        callback?.onDateSelected(result)
        postReactEvent(result)
    }

    private fun postReactEvent(selectedDate: String) {
        val event: WritableMap = Arguments.createMap()
        event.putString("date", selectedDate)
        val reactContext = context as ReactContext
        reactContext.getJSModule(RCTEventEmitter::class.java).receiveEvent(
            id,
            "topChange",
            event
        )
    }

    private fun setMaxMonth(maxMonth: Int) {
        pMonth.maxValue = maxMonth - 1
    }

    private fun setMinMonth(minMonth: Int) {
        pMonth.minValue = minMonth
    }

    private fun setMaxDay(maxDay: Int) {
        pDay.maxValue = maxDay
    }

    private fun setMinDay(minDay: Int) {
        pDay.minValue = minDay
    }

    fun setCurrentDate(value: String) {
        var current = CustomDate(value)

        if (current.isAfter(maximumDate)) {
            current = maximumDate
        }

        pDay.value = current.day()
        pMonth.value = current.month()
        pYear.value = current.year()
        recalcMinMax()
    }

    private fun recalcMinMax() = RecalcLogic.recalcMinMax(
        year = pYear.value,
        month = pMonth.value,
        day = pDay.value,
        maximumDate = maximumDate,
        minimumDate = minimumDate,
        setMaxDay = ::setMaxDay,
        setMinDay = ::setMinDay,
        setMaxMonth = ::setMaxMonth,
        setMinMonth = ::setMinMonth,
        calcMaxDayForMonth = ::calcMaxDayForMonth
    )

    class CustomDate(dateString: String) {
        private val x = dateString.split("-")
        fun year() = x[0].toInt()
        fun month() = x[1].toInt()
        fun day() = x[2].toInt()

        override fun toString() = "${year()}-${month()}-${day()}"

        private fun sum(): Int = year() + month() + day()

        fun isAfter(then: CustomDate): Boolean {
            return this.sum() > then.sum()
        }
    }

    fun setMinDate(minDate: String) {
        minimumDate = CustomDate(minDate)
        pYear.minValue = minimumDate.year()
        recalcMinMax()
    }

    fun setMaxDate(maxDate: String) {
        maximumDate = CustomDate(maxDate)
        pYear.maxValue = maximumDate.year()
        recalcMinMax()
    }

    private fun calcMaxDayForMonth(): Int {
        return GregorianCalendar(
            pYear.value,
            pMonth.value,
            1
        ).getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    private val vibrateRunnable = Runnable {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibe?.vibrate(VibrationEffect.createOneShot(10, 20))
        } else {
            vibe?.vibrate(10)
        }
    }

    private fun vibrate() {
        vibeDebouncer.debounce(Nothing::class, vibrateRunnable, 50, TimeUnit.MILLISECONDS)
    }
}