package com.bloomhigh.datepicker

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.RCTEventEmitter
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
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
            if (it - 1 in 0..months.size) {
                months[it - 1]
            } else {
                "$it ${months.size}"
            }
//            it.toString()
        }

        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH) + 1
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
//        val touchListener = OnTouchListener { _, event ->
//            if (event.action == MotionEvent.ACTION_UP) {
//                val previous = pDay.maxValue
//                val new = calcMaxDayForMonth()
//                if (previous != new) {
//                    pDay.maxValue = new
//                }
//            }
//            super.onTouchEvent(event)
//        }
//        pMonth.setOnTouchListener(touchListener)
//        pYear.setOnTouchListener(touchListener)
        val valueChangeListener = NumberPicker.OnValueChangeListener { _, _, _ ->
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
        pDay.maxValue = RecalcLogic.calcMaxDayForDate(pYear.value, pMonth.value)
    }

    private val onChangeRunnable = {
        recalcMinMax()
        onChange()
    }

    private fun onChange() {
        val date = getCurrentDate().toString()
        callback?.onDateSelected(getCurrentDate().toString())
        postReactEvent(getCurrentDateString())
    }

    private fun getCurrentDateString(): String {
        val year = "${pYear.value}"
        val month = if (pMonth.value < 10) {
            "0${pMonth.value}"
        } else {
            "${pMonth.value}"
        }
        val day = if (pDay.value < 10) {
            "0${pDay.value}"
        } else {
            "${pDay.value}"
        }
        return "$year-$month-$day"
    }

    private fun getCurrentDate(): CustomDate {
        return CustomDate(getCurrentDateString())
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

    private fun recalcMinMax() {
        with(RecalcLogic.recalcMinMax(
            currentDate = getCurrentDate(),
            maximumDate = maximumDate,
            minimumDate = minimumDate
        )) {
            pMonth.minValue = this.minMonth
            pMonth.maxValue = this.maxMonth
            pDay.minValue = this.minDay
            pDay.maxValue = this.maxDay
        }
    }

    class CustomDate(dateString: String) {
        private val x = dateString.split("-")
        fun year() = x[0].toInt()
        fun month() = x[1].toInt()
        fun day() = x[2].toInt()

        override fun toString(): String {
            val year = "${year()}"
            val month = if (month() < 10) {
                "0${month()}"
            } else {
                "${month()}"
            }
            val day = if (day() < 10) {
                "0${day()}"
            } else {
                "${day()}"
            }
            return "$year-$month-$day"
        }

        private fun sum(): Long {
            return SimpleDateFormat("yyyy-mm-dd").parse(this.toString()).time
        }

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