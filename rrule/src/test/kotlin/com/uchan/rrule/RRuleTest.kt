package com.uchan.rrule

import kotlin.test.Test
import kotlin.test.assertEquals
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
    fun toRFC5545String_interval_test() {
        listOf(2, 3, 5).forEach {
            val rrule = RRule(rfc5545String = "RRULE:INTERVAL=$it")
            assertEquals("RRULE:FREQ=DAILY;INTERVAL=$it", rrule.toRFC5545String())
        }

        val rrule = RRule(interval = 1)
        assertEquals("RRULE:FREQ=DAILY", rrule.toRFC5545String())
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
}
