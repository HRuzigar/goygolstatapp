package az.stat.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StatsAnalyticsTest {

    @Test
    fun parseChangePercent_handlesValidAndInvalidValues() {
        assertEquals(12.4, parseChangePercent("+12.4%"), 0.0001)
        assertEquals(-9.1, parseChangePercent("-9.1%"), 0.0001)
        assertEquals(-7.5, parseChangePercent("−7.5%"), 0.0001)
        assertEquals(0.0, parseChangePercent("0.0%"), 0.0001)
        assertNull(parseChangePercent(""))
        assertNull(parseChangePercent("N/A"))
    }

    @Test
    fun calculateYearInsights_returnsExpectedSummary() {
        val input = mapOf(
            "A" to listOf(
                StatItem("x", "1", "ədəd", "+10.0%", true),
                StatItem("y", "2", "ədəd", "-3.0%", false),
                StatItem("z", "3", "ədəd", "0.0%", true)
            ),
            "B" to listOf(
                StatItem("k", "4", "ədəd", "+20.0%", false),
                StatItem("m", "5", "ədəd", "", true)
            )
        )

        val result = calculateYearInsights(input)

        assertEquals(5, result.totalIndicators)
        assertEquals(2, result.improvingCount)
        assertEquals(1, result.decliningCount)
        assertEquals(1, result.neutralCount)
        assertEquals(8.25, result.averageAbsoluteChangePercent, 0.0001)
        assertEquals("k", result.strongestIncrease?.label)
        assertEquals("y", result.strongestDecline?.label)
        assertEquals(-3.0, result.strongestDecline?.changePercent ?: 0.0, 0.0001)
        assertEquals("B", result.mostVolatileCategory)
    }
}
