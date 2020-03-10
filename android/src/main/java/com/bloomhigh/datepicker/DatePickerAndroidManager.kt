package com.bloomhigh.datepicker

import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class DatePickerAndroidManager : SimpleViewManager<BHDatePicker>() {
    override fun getName(): String {
        return REACT_CLASS
    }

    public override fun createViewInstance(c: ThemedReactContext): BHDatePicker {
        return BHDatePicker(c)
    }

    companion object {
        val format = SimpleDateFormat("YYYY-MM-DD")
        const val REACT_CLASS = "DatePickerAndroid"
    }

    @ReactProp(name = "minDate")
    fun setMinDate(view: BHDatePicker, minDate: String?) {
        if (minDate != null) {
            val date: Date?
            date = try {
                format.parse(minDate)
            } catch (e: ParseException) {
                Date()
            }
            val c = Calendar.getInstance()
            c.time = date
            view.setMinYear(c[Calendar.YEAR] + 1)
        }
    }

    @ReactProp(name = "maxDate")
    fun setMaxDate(view: BHDatePicker, maxDate: String?) {
        if (maxDate != null) {
            val date: Date?
            date = try {
                format.parse(maxDate)
            } catch (e: ParseException) {
                Date()
            }
            val c = Calendar.getInstance()
            c.time = date
            view.setMaxYear(c[Calendar.YEAR] + 1)
        }
    }

    @ReactProp(name = "value")
    fun setValue(view: BHDatePicker, value: String?
    ) {
        if (value != null) {
            view.setCurrentDate(value)
        }
    }

    override fun getExportedCustomBubblingEventTypeConstants(): MutableMap<String, Any> {
        return MapBuilder.builder<String, Any>()
                .put(
                        "topChange",
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of("bubbled", "onChange")))
                .build()
    }
}