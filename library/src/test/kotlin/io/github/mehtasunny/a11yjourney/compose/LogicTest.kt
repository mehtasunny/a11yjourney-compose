package io.github.mehtasunny.a11yjourney.compose

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LogicTest {

    @Test
    fun contrastMatchesWcagReferenceValues() {
        assertEquals(21f, contrastRatio(Color.Black, Color.White), 0.01f)
        assertEquals(4.54f, contrastRatio(Color(0xFF767676), Color.White), 0.01f)
        assertEquals(1f, contrastRatio(Color.Gray, Color.Gray), 0.001f)
    }

    @Test
    fun bundledSchemesPassEveryPair() {
        assertEquals(emptyList<ContrastIssue>(), auditContrast(AccessibleLightColors))
        assertEquals(emptyList<ContrastIssue>(), auditContrast(AccessibleDarkColors))
    }

    @Test
    fun weakSchemeIsReportedAndRejected() {
        val weak = lightColorScheme(primary = Color(0xFF90CAF9), onPrimary = Color.White)
        val issues = auditContrast(weak)
        assertTrue(issues.any { it.pair == "onPrimary on primary" })
        val failed = runCatching { requireAccessibleContrast(weak) }.exceptionOrNull()
        assertTrue(failed is IllegalStateException)
    }

    @Test
    fun statusBannerColorsMeetTextContrastInBothThemes() {
        for (scheme in listOf(AccessibleLightColors, AccessibleDarkColors)) {
            for (kind in StatusKind.entries) {
                val c = statusColors(kind, scheme)
                assertTrue("$kind", contrastRatio(c.content, c.container) >= ContrastMinimum.TEXT)
            }
        }
    }

    @Test
    fun labelsMustSaySomething() {
        assertTrue(isMeaningfulLabel("Swap start and destination"))
        assertFalse(isMeaningfulLabel(""))
        assertFalse(isMeaningfulLabel("button1"))
        assertFalse(isMeaningfulLabel("Icon"))
        assertFalse(isMeaningfulLabel("ic_close_v2.png"))
        assertTrue(runCatching { requireMeaningfulLabel(" ", "X") }.isFailure)
    }

    @Test
    fun dateValidationSaysWhatToFix() {
        assertEquals("Enter a date", validateDate(DateParts()))
        assertEquals("Date must include a day", validateDate(DateParts("4", "", "1961")))
        assertEquals("Year must include 4 numbers", validateDate(DateParts("4", "12", "61")))
        assertEquals("Enter a real date", validateDate(DateParts("2", "30", "2024")))
        assertEquals("Enter a real date", validateDate(DateParts("13", "1", "2024")))
        assertNull(validateDate(DateParts("2", "29", "2024")))
        assertNull(validateDate(DateParts("4", "12", "1961")))
    }

    @Test
    fun countdownIsSpokenInRoundedSteps() {
        assertEquals(120, spokenCountdown(61))
        assertEquals(120, spokenCountdown(119))
        assertEquals(60, spokenCountdown(46))
        assertEquals(45, spokenCountdown(31))
        assertEquals(20, spokenCountdown(17))
        assertEquals(5, spokenCountdown(1))
        assertEquals("2 minutes", spokenDuration(120))
        assertEquals("1 minute 30 seconds", spokenDuration(90))
        assertEquals("45 seconds", spokenDuration(45))
    }

    @Test
    fun sessionTimerWarnsAndExtends() {
        val timer = SessionTimer(sessionSeconds = 300, warnAtSeconds = 120)
        timer.tick(179)
        assertFalse(timer.warning)
        timer.tick(1)
        assertTrue(timer.warning)
        timer.extend()
        assertEquals(300, timer.secondsRemaining)
        timer.tick(400)
        assertTrue(timer.expired)
        assertFalse(timer.warning)
    }

    @Test
    fun sessionTimerRefusesTooShortAWarning() {
        assertTrue(runCatching { SessionTimer(sessionSeconds = 300, warnAtSeconds = 10) }.isFailure)
    }

    @Test
    fun arrivalsReadAsSentences() {
        assertEquals(
            "Route 7 to Downtown, arriving in 5 minutes",
            describeArrival(Arrival("7", "Downtown", 5)),
        )
        assertEquals(
            "Route 44 to Airport, arriving in 1 minute, Delayed",
            describeArrival(Arrival("44", "Airport", 1, ArrivalStatus.Delayed)),
        )
        assertEquals(
            "Route 7 to Downtown, arriving now",
            describeArrival(Arrival("7", "Downtown", 0)),
        )
        assertEquals(
            "Route 12 to Harbor, Cancelled",
            describeArrival(Arrival("12", "Harbor", 8, ArrivalStatus.Cancelled)),
        )
        assertEquals(
            "Route 3 to Uptown, scheduled at 6:15 PM",
            describeArrival(Arrival("3", "Uptown", null, scheduledTime = "6:15 PM")),
        )
    }

    @Test
    fun announcementsAreThrottledUnlessUrgent() {
        var now = 0L
        val throttle = AnnouncementThrottle(minIntervalMillis = 60_000) { now }
        assertEquals("a", throttle.offer("a"))
        now = 10_000
        assertNull(throttle.offer("b"))
        assertEquals("c", throttle.offer("c", urgent = true))
        assertNull(throttle.offer("c", urgent = true))
        now = 80_000
        assertEquals("d", throttle.offer("d"))
    }
}
