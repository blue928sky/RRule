package com.uchan.rrule

data class RRule(
    val freq: Frequency = Frequency.DAILY,
    val interval: Int = 1,
    val byDay: Set<Week> = emptySet(),
    val byMonth: Set<Int> = emptySet(),
    val byMonthDay: Set<Int> = emptySet(),
    val bySetPos: Set<Int> = emptySet(),
) {
    val isIntervalValid
        get() = interval >= 0
    val isByMonthValid
        get() = byMonth.all((1..12)::contains)
    val isByMonthDayValid
        get() = byMonthDay.all { (-31..31).contains(it) && it != 0 }
    val isBySetPosValid
        get() = bySetPos.all { (-366..366).contains(it) && it != 0 }
    val isValid: Boolean
        get() = listOf(isIntervalValid, isByMonthValid, isByMonthDayValid, isBySetPosValid).all { it }

    /**
     * Converted to string for iCalendar(RFC 2445)
     */
    fun toRFC5545String(): String = buildString {
        append(NAME, ":")

        append(FREQ, "=")
        append(freq)

        if (interval > 1) {
            append(";", INTERVAL, "=")
            append(interval)
        }

        if (byDay.isNotEmpty()) {
            append(";", BYDAY, "=")
            append(byDay.joinToString(separator = ",") { it.initial })
        }

        if (byMonth.isNotEmpty()) {
            append(";", BYMONTH, "=")
            append(byMonth.joinToString(separator = ","))
        }

        if (byMonthDay.isNotEmpty()) {
            append(";", BYMONTHDAY, "=")
            append(byMonthDay.joinToString(separator = ","))
        }

        if (bySetPos.isNotEmpty()) {
            append(";", BYSETPOS, "=")
            append(bySetPos.joinToString(separator = ","))
        }
    }

    companion object {
        private const val NAME = "RRULE"
        const val FREQ = "FREQ"
        const val INTERVAL = "INTERVAL"
        const val BYDAY = "BYDAY"
        const val BYMONTH = "BYMONTH"
        const val BYMONTHDAY = "BYMONTHDAY"
        const val BYSETPOS = "BYSETPOS"

        /**
         * Functions like secondary constructor
         */
        operator fun invoke(rfc5545String: String): RRule {
            var freq: Frequency = Frequency.DAILY
            var interval = 1
            var byDay: Set<Week> = emptySet()
            var byMonth: Set<Int> = emptySet()
            var byMonthDay: Set<Int> = emptySet()
            var bySetPos: Set<Int> = emptySet()
            val components = rfc5545String
                .replace(oldValue = "$NAME:", newValue = "")
                .split(";")
            components.forEach { component ->
                val componentPair = runCatching {
                    component.split("=").let { it[0] to it[1] }
                }.getOrNull()
                when (componentPair?.first) {
                    FREQ -> freq = runCatching {
                        enumValueOf<Frequency>(name = componentPair.second)
                    }.getOrDefault(defaultValue = Frequency.DAILY)

                    INTERVAL -> interval = componentPair.second.toInt()
                    BYDAY -> byDay = runCatching {
                        componentPair.second.mapToSet(Week::initialValueOf)
                    }.getOrDefault(defaultValue = emptySet())

                    BYMONTH -> byMonth = componentPair.second.mapToSet(String::toInt)
                    BYMONTHDAY -> byMonthDay = componentPair.second.mapToSet(String::toInt)
                    BYSETPOS -> bySetPos = componentPair.second.mapToSet(String::toInt)
                }
            }

            return RRule(
                freq = freq,
                interval = interval,
                byDay = byDay,
                byMonth = byMonth,
                byMonthDay = byMonthDay,
                bySetPos = bySetPos,
            )
        }

        private fun <R> String.mapToSet(transform: (String) -> R): Set<R> {
            return split(",").map(transform).toSet()
        }
    }
}
