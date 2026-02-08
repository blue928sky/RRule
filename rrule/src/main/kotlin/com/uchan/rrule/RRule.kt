package com.uchan.rrule

import com.uchan.rrule.RRuleSyntax.BYDAY
import com.uchan.rrule.RRuleSyntax.BYMONTH
import com.uchan.rrule.RRuleSyntax.BYMONTHDAY
import com.uchan.rrule.RRuleSyntax.BYSETPOS
import com.uchan.rrule.RRuleSyntax.FREQ
import com.uchan.rrule.RRuleSyntax.INTERVAL
import com.uchan.rrule.RRuleSyntax.KEY_VALUE_SEPARATOR
import com.uchan.rrule.RRuleSyntax.LIST_SEPARATOR
import com.uchan.rrule.RRuleSyntax.PARTS_SEPARATOR
import com.uchan.rrule.RRuleSyntax.PROPERTY_NAME
import com.uchan.rrule.RRuleSyntax.PROPERTY_SEPARATOR

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
            append(PROPERTY_NAME, PROPERTY_SEPARATOR)

            append(FREQ, KEY_VALUE_SEPARATOR)
            append(freq)

            if (interval > 1) {
                append(PARTS_SEPARATOR, INTERVAL, KEY_VALUE_SEPARATOR)
                append(interval)
            }

            if (byDay.isNotEmpty()) {
                append(PARTS_SEPARATOR, BYDAY, KEY_VALUE_SEPARATOR)
                append(byDay.joinToString(separator = LIST_SEPARATOR, transform = Week::initial))
            }

            if (byMonth.isNotEmpty()) {
                append(PARTS_SEPARATOR, BYMONTH, KEY_VALUE_SEPARATOR)
                append(byMonth.joinToString(separator = LIST_SEPARATOR))
            }

            if (byMonthDay.isNotEmpty()) {
                append(PARTS_SEPARATOR, BYMONTHDAY, KEY_VALUE_SEPARATOR)
                append(byMonthDay.joinToString(separator = LIST_SEPARATOR))
            }

            if (bySetPos.isNotEmpty()) {
                append(PARTS_SEPARATOR, BYSETPOS, KEY_VALUE_SEPARATOR)
                append(bySetPos.joinToString(separator = LIST_SEPARATOR))
            }
        }
    }

    companion object {
        /**
         * Functions like secondary constructor.
         *
         *     val rrule = RRule(rfc5545String = "RRULE:FREQ=DAILY")
         *
         */
        operator fun invoke(rfc5545String: String): RRule {
            val components = rfc5545String
                .removePrefix("$PROPERTY_NAME$PROPERTY_SEPARATOR")
                .split(PARTS_SEPARATOR)
                .associate { component ->
                    val parts = component.split(KEY_VALUE_SEPARATOR, limit = 2)
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
private val FREQUENCY_ENTRIES = Frequency.entries.joinToString(separator = LIST_SEPARATOR)
private val WEEK_ENTRIES = Week.entries.joinToString(separator = LIST_SEPARATOR, transform = Week::initial)

private fun createFrequencyValidateErrorMessage() = "FREQ must be in format $FREQUENCY_ENTRIES"
private fun createByDayValidateErrorMessage() = "BYDAY must be in format $WEEK_ENTRIES"
