package com.bloomhigh.datepicker

object RecalcLogic {
    fun recalcMinMax(
        year: Int,
        month: Int,
        day: Int,
        maximumDate: BHDatePicker.CustomDate,
        minimumDate: BHDatePicker.CustomDate,
        setMaxMonth: (Int) -> Unit,
        setMinMonth: (Int) -> Unit,
        setMaxDay: (Int) -> Unit,
        setMinDay: (Int) -> Unit,
        calcMaxDayForMonth: () -> Int
    ) {
        when (year) {
            maximumDate.year() -> {
                calcMaximumYear(
                    setMinMonth,
                    setMaxMonth,
                    maximumDate,
                    month,
                    setMaxDay,
                    calcMaxDayForMonth
                )
            }
            minimumDate.year() -> {
                calcMinimumYear(
                    setMinMonth,
                    minimumDate,
                    setMaxMonth,
                    month,
                    setMaxDay,
                    setMinDay,
                    calcMaxDayForMonth
                )
            }
            else -> {
                setMinMonth(0)
                setMaxMonth(11)
                setMinDay(1)
                setMaxDay(calcMaxDayForMonth())
            }
        }
    }

    private fun calcMinimumYear(
        setMinMonth: (Int) -> Unit,
        minimumDate: BHDatePicker.CustomDate,
        setMaxMonth: (Int) -> Unit,
        month: Int,
        setMaxDay: (Int) -> Unit,
        setMinDay: (Int) -> Unit,
        calcMaxDayForMonth: () -> Int
    ) {
        setMinMonth(minimumDate.month() - 1)
        setMaxMonth(11)
        if (month == minimumDate.month() - 1) {
            setMinDay(minimumDate.day())
            setMaxDay(calcMaxDayForMonth())
        } else {
            setMinDay(1)
            setMaxDay(calcMaxDayForMonth())
        }
    }

    private fun calcMaximumYear(
        setMinMonth: (Int) -> Unit,
        setMaxMonth: (Int) -> Unit,
        maximumDate: BHDatePicker.CustomDate,
        month: Int,
        setMaxDay: (Int) -> Unit,
        calcMaxDayForMonth: () -> Int
    ) {
        setMinMonth(0)
        setMaxMonth(maximumDate.month())
        if (month == maximumDate.month()) {
            setMaxDay(maximumDate.day())
        } else {
            setMaxDay(calcMaxDayForMonth())
        }
    }

}