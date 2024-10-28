package com.uchan.rrule

enum class Week(val initial: String) {
    MONDAY(initial = "MO"),
    TUESDAY(initial = "TU"),
    WEDNESDAY(initial = "WE"),
    THURSDAY(initial = "TH"),
    FRIDAY(initial = "FR"),
    SATURDAY(initial = "SA"),
    SUNDAY(initial = "SU"),
    ;

    companion object {
        val WEEKDAYS = setOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)
        val WEEKEND = setOf(SATURDAY, SUNDAY)

        fun initialValueOf(initial: String): Week = entries.find {
            it.initial == initial
        } ?: throw IllegalArgumentException()
    }
}
