package com.uchan.rrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RRuleTest {
    @Test
    fun default_test() {
        val rrule = RRule()
        assertEquals(Frequency.DAILY, rrule.freq)
        assertEquals(1, rrule.interval)
        assertEquals(emptySet(), rrule.byDay)
        assertEquals(emptySet(), rrule.byMonth)
        assertEquals(emptySet(), rrule.byMonthDay)
        assertEquals(emptySet(), rrule.bySetPos)
    }

    @Test
    fun toRFC5545String_default_test() {
        val rrule = RRule(rfc5545String = "")
        assertEquals("RRULE:FREQ=DAILY", rrule.toRFC5545String())
    }

    @Test
    fun toRFC5545String_freq_test() {
        listOf("DAILY", "WEEKLY", "MONTHLY", "YEARLY").forEach {
            val rrule = RRule(rfc5545String = "RRULE:FREQ=$it")
            assertEquals("RRULE:FREQ=$it", rrule.toRFC5545String())
        }
    }

    @Test
    fun toRFC5545String_freqLower_test() {
        val rrule = RRule(rfc5545String = "RRULE:FREQ=monthly")
        assertEquals("RRULE:FREQ=MONTHLY", rrule.toRFC5545String())
    }

    @Test
    fun toRFC5545String_interval_test() {
        listOf(2, 3, 5).forEach {
            val rrule = RRule(rfc5545String = "RRULE:INTERVAL=$it")
            assertEquals("RRULE:FREQ=DAILY;INTERVAL=$it", rrule.toRFC5545String())
        }

        val rrule = RRule(interval = 1)
        assertEquals("RRULE:FREQ=DAILY", rrule.toRFC5545String())
    }

    @Test
    fun toRFC5545String_freqInvalid_test() {
        val exception = assertFailsWith<IllegalArgumentException> {
            RRule(interval = -1).toRFC5545String()
        }
        assertEquals("INTERVAL must be positive number", exception.message)
    }

    @Test
    fun toRFC5545String_byDay_test() {
        listOf("MO", "TU", "WE", "TH", "FR", "SA", "SU").forEach {
            val rrule = RRule(rfc5545String = "RRULE:BYDAY=$it")
            assertEquals("RRULE:FREQ=DAILY;BYDAY=$it", rrule.toRFC5545String())
        }
    }

    @Test
    fun toRFC5545String_byDay_multiple_test() {
        val rrule = RRule(rfc5545String = "RRULE:FREQ=WEEKLY;BYDAY=MO,TU,MO")
        assertEquals("RRULE:FREQ=WEEKLY;BYDAY=MO,TU", rrule.toRFC5545String())
    }

    @Test
    fun toRFC5545String_byMonth_test() {
        listOf(1, 5, 12).forEach {
            val rrule = RRule(rfc5545String = "RRULE:FREQ=YEARLY;BYMONTH=$it")
            assertEquals("RRULE:FREQ=YEARLY;BYMONTH=$it", rrule.toRFC5545String())
        }
    }

    @Test
    fun toRFC5545String_byMonth_multiple_test() {
        val rrule = RRule(rfc5545String = "RRULE:FREQ=YEARLY;BYMONTH=1,3,3")
        assertEquals("RRULE:FREQ=YEARLY;BYMONTH=1,3", rrule.toRFC5545String())
    }

    @Test
    fun toRFC5545String_byMonthInvalid_test() {
        listOf(0, 13).forEach {
            val exception = assertFailsWith<IllegalArgumentException> {
                RRule(byMonth = setOf(it)).toRFC5545String()
            }
            assertEquals("BYMONTH must be number in range 1-12", exception.message)
        }
    }

    @Test
    fun toRFC5545String_byMonthDay_test() {
        listOf(1, 3, 31).forEach {
            val rrule = RRule(rfc5545String = "RRULE:FREQ=YEARLY;BYMONTHDAY=$it")
            assertEquals("RRULE:FREQ=YEARLY;BYMONTHDAY=$it", rrule.toRFC5545String())
        }
    }

    @Test
    fun toRFC5545String_byMonthDay_multiple_test() {
        val rrule = RRule(rfc5545String = "RRULE:FREQ=YEARLY;BYMONTHDAY=1,3,5")
        assertEquals("RRULE:FREQ=YEARLY;BYMONTHDAY=1,3,5", rrule.toRFC5545String())
    }

    @Test
    fun toRFC5545String_byMonthDayInvalid_test() {
        listOf(-32, 0, 32).forEach {
            val exception = assertFailsWith<IllegalArgumentException> {
                RRule(byMonthDay = setOf(it)).toRFC5545String()
            }
            assertEquals("BYMONTHDAY must be number in range (-31..-1, 1..31)", exception.message)
        }
    }

    @Test
    fun toRFC5545String_bySetPos_test() {
        listOf(-1, 2, 3).forEach {
            val rrule = RRule(rfc5545String = "RRULE:FREQ=DAILY;BYDAY=MO;BYSETPOS=$it")
            assertEquals("RRULE:FREQ=DAILY;BYDAY=MO;BYSETPOS=$it", rrule.toRFC5545String())
        }
    }

    @Test
    fun toRFC5545String_bySetPos_multiple_test() {
        val rrule = RRule(rfc5545String = "RRULE:FREQ=DAILY;BYDAY=MO;BYSETPOS=1,-1")
        assertEquals("RRULE:FREQ=DAILY;BYDAY=MO;BYSETPOS=1,-1", rrule.toRFC5545String())
    }

    @Test
    fun toRFC5545String_bySetPosInvalid_test() {
        listOf(-367, 0, 367).forEach {
            val exception = assertFailsWith<IllegalArgumentException> {
                RRule(bySetPos = setOf(it)).toRFC5545String()
            }
            assertEquals("BYSETPOS must be number in range (-366..-1, 1..366)", exception.message)
        }
    }

    @Test
    fun isIntervalValid_true_test() {
        val rrule = RRule(interval = 0)
        assertTrue(rrule.isIntervalValid)
    }

    @Test
    fun isIntervalValid_false_test() {
        val rrule = RRule(interval = -1)
        assertFalse(rrule.isIntervalValid)
    }

    @Test
    fun isByMonthValid_true_test() {
        listOf(emptySet(), setOf(1, 12)).forEach {
            assertTrue(RRule(byMonth = it).isByMonthValid)
        }
    }

    @Test
    fun isByMonthValid_false_test() {
        listOf(setOf(0), setOf(13)).forEach {
            assertFalse(RRule(byMonth = it).isByMonthValid)
        }
    }

    @Test
    fun isByMonthDayValid_true_test() {
        listOf(emptySet(), setOf(-31, -1, 1, 31)).forEach {
            assertTrue(RRule(byMonthDay = it).isByMonthDayValid)
        }
    }

    @Test
    fun isByMonthDayValid_false_test() {
        listOf(setOf(0), setOf(-32), setOf(32)).forEach {
            assertFalse(RRule(byMonthDay = it).isByMonthDayValid)
        }
    }

    @Test
    fun isBySetPos_true_test() {
        listOf(emptySet(), setOf(-366, -1, 1, 366)).forEach {
            assertTrue(RRule(bySetPos = it).isBySetPosValid)
        }
    }

    @Test
    fun isBySetPos_false_test() {
        listOf(setOf(0), setOf(-367), setOf(367)).forEach {
            assertFalse(RRule(bySetPos = it).isBySetPosValid)
        }
    }

    @Test
    fun isValid_true_test() {
        listOf(
            RRule(),
            RRule(interval = 1),
        ).forEach {
            assertTrue(it.isValid)
        }
    }

    @Test
    fun isValid_false_test() {
        listOf(
            RRule(interval = -1),
            RRule(byMonth = setOf(0)),
            RRule(byMonthDay = setOf(0)),
            RRule(bySetPos = setOf(0)),
        ).forEach {
            assertFalse(it.isValid)
        }
    }

    @Test
    fun freqInvalid_throwIllegalArgumentException_test() {
        val exception = assertFailsWith<IllegalArgumentException> {
            RRule(rfc5545String = "RRULE:FREQ=day")
        }
        assertEquals("FREQ must be in format DAILY,WEEKLY,MONTHLY,YEARLY", exception.message)
    }

    @Test
    fun intervalNonNumeric_throwIllegalArgumentException_test() {
        val exception = assertFailsWith<IllegalArgumentException> {
            RRule(rfc5545String = "RRULE:INTERVAL=two")
        }
        assertEquals("INTERVAL must be positive number", exception.message)
    }

    @Test
    fun byDayNonNumeric_throwIllegalArgumentException_test() {
        val exception = assertFailsWith<IllegalArgumentException> {
            RRule(rfc5545String = "FREQ=MONTHLY;BYDAY=NA")
        }
        assertEquals("BYDAY must be in format MO,TU,WE,TH,FR,SA,SU", exception.message)
    }

    @Test
    fun byMonthNonNumeric_throwIllegalArgumentException_test() {
        val exception = assertFailsWith<IllegalArgumentException> {
            RRule(rfc5545String = "RRULE:FREQ=YEARLY;BYMONTH=two")
        }
        assertEquals("BYMONTH must be number in range 1-12", exception.message)
    }

    @Test
    fun byMonthInvalidNumber_throwIllegalArgumentException_test() {
        val exception = assertFailsWith<IllegalArgumentException> {
            RRule(rfc5545String = "RRULE:FREQ=YEARLY;BYMONTH=13")
        }
        assertEquals("BYMONTH must be number in range 1-12", exception.message)
    }

    @Test
    fun byMonthDayNonNumeric_throwIllegalArgumentException_test() {
        val exception = assertFailsWith<IllegalArgumentException> {
            RRule(rfc5545String = "RRULE:FREQ=MONTHLY;BYMONTHDAY=two")
        }
        assertEquals("BYMONTHDAY must be number in range (-31..-1, 1..31)", exception.message)
    }

    @Test
    fun byMonthDayInvalidNumber_throwIllegalArgumentException_test() {
        listOf(
            "RRULE:FREQ=MONTHLY;BYMONTHDAY=0",
            "RRULE:FREQ=MONTHLY;BYMONTHDAY=32",
        ).forEach {
            val exception = assertFailsWith<IllegalArgumentException> {
                RRule(rfc5545String = it)
            }
            assertEquals("BYMONTHDAY must be number in range (-31..-1, 1..31)", exception.message)
        }
    }

    @Test
    fun isBySetPosValidNonNumeric_throwIllegalArgumentException_test() {
        val exception = assertFailsWith<IllegalArgumentException> {
            RRule(rfc5545String = "RRULE:FREQ=MONTHLY;BYSETPOS=two")
        }
        assertEquals("BYSETPOS must be number in range (-366..-1, 1..366)", exception.message)
    }

    @Test
    fun isBySetPosValidInvalidNumber_throwIllegalArgumentException_test() {
        listOf(
            "RRULE:FREQ=MONTHLY;BYSETPOS=0",
            "RRULE:FREQ=MONTHLY;BYSETPOS=367",
        ).forEach {
            val exception = assertFailsWith<IllegalArgumentException> {
                RRule(rfc5545String = it)
            }
            assertEquals("BYSETPOS must be number in range (-366..-1, 1..366)", exception.message)
        }
    }
}
