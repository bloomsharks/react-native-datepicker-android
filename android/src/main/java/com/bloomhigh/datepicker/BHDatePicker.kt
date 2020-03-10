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
    private val debouncer = Debouncer()


    init {
        LayoutInflater.from(context).inflate(R.layout.view_date_picker, this, true)

        vibe = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?

        pMonth = findViewById(R.id.pMonth)
        pDay = findViewById(R.id.pDay)
        pYear = findViewById(R.id.pYear)

        pMonth.minValue = 0
        pMonth.maxValue = 11
        pMonth.setFormatter { months[it] }
        pMonth.order = NumberPicker.ASCENDING
        pMonth.value = c.get(Calendar.MONTH)

        pYear.minValue = c.get(Calendar.YEAR) - 100
        pYear.maxValue = c.get(Calendar.YEAR) - 4
        pYear.value = pYear.maxValue
        pYear.setFormatter { "$it" }

        pDay.minValue = 1
        pDay.maxValue = calcMaxDayForMonth()

        val touchListener = OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val previous = pDay.maxValue
                val new = calcMaxDayForMonth()
                if(previous != new) {
                    pDay.maxValue = new
                }
            }
            super.onTouchEvent(event)
        }
        pMonth.setOnTouchListener(touchListener)
        pYear.setOnTouchListener(touchListener)

        val valueChangeListener = NumberPicker.OnValueChangeListener { p, o, n ->
            vibrate()
            onChange()
        }

        pYear.setOnValueChangedListener(valueChangeListener)
        pDay.setOnValueChangedListener(valueChangeListener)
        pMonth.setOnValueChangedListener(valueChangeListener)
    }

    private fun onChange() {
        val year = "${pYear.value}"
        val month = if(pMonth.value + 1< 10) {
            "0${pMonth.value + 1}"
        } else {
            "${pMonth.value + 1}"
        }
        val day = if(pDay.value < 10) {
            "0${pDay.value}"
        } else {
            "${pDay.value}"
        }
        val result = "$year-$month-$day"

        val event: WritableMap = Arguments.createMap()
        event.putString("date", result)
        val reactContext = context as ReactContext
        reactContext.getJSModule(RCTEventEmitter::class.java).receiveEvent(
                id,
                "topChange",
                event)
    }

    fun setCurrentDate(value: String) {
        val t = value.split("-")
        val year = t[0].toInt()
        val month = t[1].toInt() - 1
        val day = t[2].toInt()

        pDay.value = day
        pMonth.value = month
        pYear.value = year

        onChange()
    }

    fun setMinYear(minYear: Int) {
        pYear.minValue = minYear
    }

    fun setMaxYear(maxYear: Int) {
        pYear.maxValue = maxYear
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
        debouncer.debounce(Nothing::class, vibrateRunnable, 50, TimeUnit.MILLISECONDS)
    }
}