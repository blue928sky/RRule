package com.uchan.rrule

internal object RRuleSyntax {
    // Keywords
    const val PROPERTY_NAME = "RRULE"
    const val FREQ = "FREQ"
    const val INTERVAL = "INTERVAL"
    const val BYDAY = "BYDAY"
    const val BYMONTH = "BYMONTH"
    const val BYMONTHDAY = "BYMONTHDAY"
    const val BYSETPOS = "BYSETPOS"

    // Separators
    const val PROPERTY_SEPARATOR = ":"
    const val KEY_VALUE_SEPARATOR = "="
    const val PARTS_SEPARATOR = ";"
    const val LIST_SEPARATOR = ","
}
