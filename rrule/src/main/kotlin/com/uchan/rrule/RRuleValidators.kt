package com.uchan.rrule

import com.uchan.rrule.RRuleSyntax.LIST_SEPARATOR

internal const val MAX_MONTH_DAYS = 31
internal const val MAX_YEAR_DAYS = 366

internal fun byMonthValidator(): (Int) -> Boolean = { it in 1..12 }
internal fun byMonthDayValidator(): (Int) -> Boolean = inRangeNonZero(-MAX_MONTH_DAYS..MAX_MONTH_DAYS)
internal fun bySetPosValidator(): (Int) -> Boolean = inRangeNonZero(-MAX_YEAR_DAYS..MAX_YEAR_DAYS)

internal fun <R> String.mapToSet(
    validate: (R) -> Boolean = { true },
    transform: (String) -> R,
): Set<R> {
    val list = split(LIST_SEPARATOR).map(transform)
    require(list.all(validate))

    return list.toSet()
}

private fun inRangeNonZero(range: IntRange): (Int) -> Boolean = {
    it != 0 && it in range
}
