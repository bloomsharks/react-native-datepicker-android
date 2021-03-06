package com.bloomhigh.datepicker

import java.util.*


class RecalcResult(
    val minMonth: Int,
    val maxMonth: Int,
    val minDay: Int,
    val maxDay: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RecalcResult

        if (minMonth != other.minMonth) return false
        if (maxMonth != other.maxMonth) return false
        if (minDay != other.minDay) return false
        if (maxDay != other.maxDay) return false

        return true
    }

    override fun hashCode(): Int {
        var result = minMonth
        result = 31 * result + maxMonth
        result = 31 * result + minDay
        result = 31 * result + maxDay
        return result
    }

    override fun toString(): String {
        return "minMonth=$minMonth; maxMonth=$maxMonth; minDay=$minDay; maxDay=$maxDay;"
    }
}

object RecalcLogic {

    const val START_MONTH = 1
    const val END_MONTH = 12
    const val START_DAY = 1

    fun recalcMinMax(
        currentDate: BHDatePicker.CustomDate,
        maximumDate: BHDatePicker.CustomDate,
        minimumDate: BHDatePicker.CustomDate
    ): RecalcResult {
        return when (currentDate.year()) {
            maximumDate.year() -> calcMaximumYear(
                currentDate = currentDate,
                maximumDate = maximumDate
            )
            minimumDate.year() -> calcMinimumYear(
                currentDate = currentDate,
                minimumDate = minimumDate
            )
            else -> calcMid(currentDate = currentDate)
        }
    }

    private fun calcMid(currentDate: BHDatePicker.CustomDate): RecalcResult {
        return RecalcResult(
            minMonth = START_MONTH,
            maxMonth = END_MONTH,
            minDay = START_DAY,
            maxDay = calcMaxDayForDate(currentDate.year(), currentDate.month())
        )
    }

    private fun calcMinimumYear(
        currentDate: BHDatePicker.CustomDate,
        minimumDate: BHDatePicker.CustomDate
    ): RecalcResult {
        return RecalcResult(
            minMonth = minimumDate.month(),
            maxMonth = END_MONTH,
            minDay = if (currentDate.month() == minimumDate.month())
                minimumDate.day()
            else
                START_DAY,
            maxDay = calcMaxDayForDate(currentDate.year(), currentDate.month())
        )
    }

    private fun calcMaximumYear(
        currentDate: BHDatePicker.CustomDate,
        maximumDate: BHDatePicker.CustomDate
    ): RecalcResult {
        return RecalcResult(
            minMonth = START_MONTH,
            maxMonth = maximumDate.month(),
            maxDay = if (currentDate.month() == maximumDate.month())
                maximumDate.day()
            else
                calcMaxDayForDate(currentDate.year(), currentDate.month()),
            minDay = START_DAY
        )
    }

    fun calcMaxDayForDate(year: Int, month: Int): Int {
        return GregorianCalendar(
            year,
            month - 1,
            1
        ).getActualMaximum(Calendar.DAY_OF_MONTH)
    }

}