package com.uchan.rrule

/**
 * Data class for handling rrule in Kotlin
 *
 * @param freq Frequency. Default = [Frequency.DAILY].
 * @param interval Repeat interval. Default = 1. Must be 0 or greater.
 * @param byDay Specifies day of week. Default = emptySet() [Week].
 * @param byMonth Specifies month. Default = emptySet(). between 1 and 12.
 * @param byMonthDay Specifies day of month. Default = emptySet(). between -31 and -1 or between 1
 *   and 31.
 * @param bySetPos Specifies the N-th iteration in the rule. Default = emptySet(). between -366 and -1
 *   or  between 1 and 366.
 */
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
        get() = byMonth.all(byMonthValidator())
    val isByMonthDayValid
        get() = byMonthDay.all(byMonthDayValidator())
    val isBySetPosValid
        get() = bySetPos.all(bySetPosValidator())
    val isValid: Boolean
        get() = listOf(isIntervalValid, isByMonthValid, isByMonthDayValid, isBySetPosValid).all { it }

    /**
     * Converted to string for iCalendar(RFC 2445).
     */
    fun toRFC5545String(): String {
        requirePrecondition()

        return buildString {
            append(NAME, ":")

            append(FREQ, "=")
            append(freq)

            if (interval > 1) {
                append(";", INTERVAL, "=")
                append(interval)
            }

            if (byDay.isNotEmpty()) {
                append(";", BYDAY, "=")
                append(byDay.joinToString(separator = ",", transform = Week::initial))
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
         * Functions like secondary constructor.
         *
         *     val rrule = RRule(rfc5545String = "RRULE:FREQ=DAILY")
         *
         */
        operator fun invoke(rfc5545String: String): RRule {
            val components = rfc5545String
                .removePrefix("$NAME:")
                .split(";")
                .associate { component ->
                    val parts = component.split("=", limit = 2)
                    if (parts.size < 2) "" to "" else parts[0] to parts[1]
                }

            val freq = components[FREQ]?.let { value ->
                runCatching { enumValueOf<Frequency>(value.uppercase()) }
                    .getOrElse { throw IllegalArgumentException(createFrequencyValidateErrorMessage()) }
            } ?: Frequency.DAILY

            val interval = components[INTERVAL]?.let { value ->
                value.toIntOrNull()?.takeIf { it >= 0 }
                    ?: throw IllegalArgumentException(INTERVAL_VALIDATE_ERROR_MESSAGE)
            } ?: 1

            val byDay = components[BYDAY]?.let { value ->
                runCatching { value.mapToSet(transform = Week::initialValueOf) }
                    .getOrElse { throw IllegalArgumentException(createByDayValidateErrorMessage()) }
            } ?: emptySet()

            // Common helper for parsing integer sets
            fun parseSet(key: String, validator: (Int) -> Boolean, errorMsg: String): Set<Int> = components[key]?.let { value ->
                runCatching { value.mapToSet(validate = validator, transform = String::toInt) }
                    .getOrElse { throw IllegalArgumentException(errorMsg) }
            } ?: emptySet()

            return RRule(
                freq = freq,
                interval = interval,
                byDay = byDay,
                byMonth = parseSet(BYMONTH, byMonthValidator(), BYMONTH_VALIDATE_ERROR_MESSAGE),
                byMonthDay = parseSet(BYMONTHDAY, byMonthDayValidator(), BYMONTHDAY_VALIDATE_ERROR_MESSAGE),
                bySetPos = parseSet(BYSETPOS, bySetPosValidator(), BYSETPOS_VALIDATE_ERROR_MESSAGE),
            )
        }

        private fun byMonthValidator(): (Int) -> Boolean = (1..12)::contains

        private fun byMonthDayValidator(): (Int) -> Boolean = {
            it != 0 && (-31..31).contains(it)
        }

        private fun bySetPosValidator(): (Int) -> Boolean = {
            it != 0 && (-366..366).contains(it)
        }

        private fun <R> String.mapToSet(
            validate: (R) -> Boolean = { true },
            transform: (String) -> R,
        ): Set<R> {
            val list = split(",").map(transform)
            require(list.all(validate))

            return list.toSet()
        }
    }

    private fun requirePrecondition() {
        require(isIntervalValid) { INTERVAL_VALIDATE_ERROR_MESSAGE }
        require(isByMonthValid) { BYMONTH_VALIDATE_ERROR_MESSAGE }
        require(isByMonthDayValid) { BYMONTHDAY_VALIDATE_ERROR_MESSAGE }
        require(isBySetPosValid) { BYSETPOS_VALIDATE_ERROR_MESSAGE }
    }
}

private const val INTERVAL_VALIDATE_ERROR_MESSAGE = "INTERVAL must be positive number"
private const val BYMONTH_VALIDATE_ERROR_MESSAGE = "BYMONTH must be number in range 1-12"
private const val BYMONTHDAY_VALIDATE_ERROR_MESSAGE = "BYMONTHDAY must be number in range (-31..-1, 1..31)"
private const val BYSETPOS_VALIDATE_ERROR_MESSAGE = "BYSETPOS must be number in range (-366..-1, 1..366)"
private val FREQUENCY_ENTRIES = Frequency.entries.joinToString(separator = ",")
private val WEEK_ENTRIES = Week.entries.joinToString(separator = ",", transform = Week::initial)

private fun createFrequencyValidateErrorMessage() = "FREQ must be in format $FREQUENCY_ENTRIES"
private fun createByDayValidateErrorMessage() = "BYDAY must be in format $WEEK_ENTRIES"
