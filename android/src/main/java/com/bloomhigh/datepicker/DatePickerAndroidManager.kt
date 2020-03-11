package com.bloomhigh.datepicker

import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

class DatePickerAndroidManager : SimpleViewManager<BHDatePicker>() {
    override fun getName(): String {
        return REACT_CLASS
    }

    public override fun createViewInstance(c: ThemedReactContext): BHDatePicker {
        return BHDatePicker(c)
    }

    companion object {
        const val REACT_CLASS = "DatePickerAndroid"
    }

    @ReactProp(name = "minDate")
    fun setMinDate(view: BHDatePicker, minDate: String?) {
        if (minDate != null) {
            view.setMinDate(minDate)
        }
    }

    @ReactProp(name = "maxDate")
    fun setMaxDate(view: BHDatePicker, maxDate: String?) {
        if (maxDate != null) {
            view.setMaxDate(maxDate)
        }
    }

    @ReactProp(name = "value")
    fun setValue(view: BHDatePicker, value: String?) {
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