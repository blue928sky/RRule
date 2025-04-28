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
        get() = byMonth.all(byMonthValidator())
    val isByMonthDayValid
        get() = byMonthDay.all(byMonthDayValidator())
    val isBySetPosValid
        get() = bySetPos.all(bySetPosValidator())
    val isValid: Boolean
        get() = listOf(isIntervalValid, isByMonthValid, isByMonthDayValid, isBySetPosValid).all { it }

    /**
     * Converted to string for iCalendar(RFC 2445)
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
                val (key, value) = runCatching {
                    component.split("=").let { it[0] to it[1] }
                }.getOrElse { return@forEach }
                when (key) {
                    FREQ -> freq = runCatching {
                        enumValueOf<Frequency>(name = value.uppercase())
                    }.getOrElse {
                        throw IllegalArgumentException(createFrequencyValidateErrorMessage())
                    }

                    INTERVAL -> runCatching {
                        require(value.toInt() >= 0)
                        interval = value.toInt()
                    }.getOrElse {
                        throw IllegalArgumentException(INTERVAL_VALIDATE_ERROR_MESSAGE)
                    }

                    BYDAY -> byDay = runCatching {
                        value.mapToSet(transform = Week::initialValueOf)
                    }.getOrElse {
                        throw IllegalArgumentException(createByDayValidateErrorMessage())
                    }

                    BYMONTH -> runCatching {
                        byMonth = value.mapToSet(
                            validate = byMonthValidator(),
                            transform = String::toInt,
                        )
                    }.getOrElse {
                        throw IllegalArgumentException(BYMONTH_VALIDATE_ERROR_MESSAGE)
                    }

                    BYMONTHDAY -> runCatching {
                        byMonthDay = value.mapToSet(
                            validate = byMonthDayValidator(),
                            transform = String::toInt,
                        )
                    }.getOrElse {
                        throw IllegalArgumentException(BYMONTHDAY_VALIDATE_ERROR_MESSAGE)
                    }

                    BYSETPOS -> runCatching {
                        bySetPos = value.mapToSet(
                            validate = bySetPosValidator(),
                            transform = String::toInt,
                        )
                    }.getOrElse {
                        throw IllegalArgumentException(BYSETPOS_VALIDATE_ERROR_MESSAGE)
                    }
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

private fun createFrequencyValidateErrorMessage() = "FREQ must be in format ${Frequency.entries.joinToString(separator = ",")}"
private fun createByDayValidateErrorMessage() = "BYDAY must be in format ${Week.entries.map(Week::initial).joinToString(separator = ",")}"
