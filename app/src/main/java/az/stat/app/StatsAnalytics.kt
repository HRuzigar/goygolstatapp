package az.stat.app

import kotlin.math.abs

data class TrendPoint(
    val categoryName: String,
    val label: String,
    val changePercent: Double,
    val isPositive: Boolean
)

data class YearInsights(
    val totalIndicators: Int,
    val improvingCount: Int,
    val decliningCount: Int,
    val neutralCount: Int,
    val averageAbsoluteChangePercent: Double,
    val strongestIncrease: TrendPoint?,
    val strongestDecline: TrendPoint?,
    val mostVolatileCategory: String?
)

fun calculateYearInsights(currentData: Map<String, List<StatItem>>): YearInsights {
    val allStats = currentData.flatMap { (category, stats) ->
        stats.map { category to it }
    }

    val trendPoints = allStats.mapNotNull { (category, stat) ->
        parseChangePercent(stat.changeText)?.let { signedPercent ->
            TrendPoint(
                categoryName = category,
                label = stat.label,
                changePercent = signedPercent,
                isPositive = stat.isPositive
            )
        }
    }

    val improvingCount = trendPoints.count { it.changePercent > 0.0 }
    val decliningCount = trendPoints.count { it.changePercent < 0.0 }
    val neutralCount = trendPoints.count { it.changePercent == 0.0 }

    val averageAbsoluteChange = if (trendPoints.isEmpty()) {
        0.0
    } else {
        trendPoints.sumOf { abs(it.changePercent) } / trendPoints.size
    }

    val strongestIncrease = trendPoints
        .filter { it.changePercent > 0.0 }
        .maxByOrNull { it.changePercent }

    val strongestDecline = trendPoints
        .filter { it.changePercent < 0.0 }
        .minByOrNull { it.changePercent }

    val mostVolatileCategory = currentData
        .mapValues { (_, stats) ->
            stats.mapNotNull { parseChangePercent(it.changeText) }
                .takeIf { it.isNotEmpty() }
                ?.let { values -> values.sumOf { abs(it) } / values.size }
        }
        .maxByOrNull { (_, volatility) -> volatility ?: Double.NEGATIVE_INFINITY }
        ?.takeIf { it.value != null }
        ?.key

    return YearInsights(
        totalIndicators = allStats.size,
        improvingCount = improvingCount,
        decliningCount = decliningCount,
        neutralCount = neutralCount,
        averageAbsoluteChangePercent = averageAbsoluteChange,
        strongestIncrease = strongestIncrease,
        strongestDecline = strongestDecline,
        mostVolatileCategory = mostVolatileCategory
    )
}

fun parseChangePercent(changeText: String): Double? {
    val normalized = changeText.trim().removeSuffix("%")
    if (normalized.isBlank()) return null

    val negative = normalized.startsWith('-') || normalized.contains('−')
    val number = normalized
        .replace("+", "")
        .replace("-", "")
        .replace("−", "")
        .toDoubleOrNull()
        ?: return null

    return if (negative) -number else number
}
