package az.stat.app

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import az.stat.app.databinding.ActivityStatisticsDashboardBinding
import java.util.Locale

class StatisticsDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatisticsDashboardBinding

    private val repository = DashboardRepository()
    private var selectedYear = 2024
    private var selectedDistrict = "Göygöl"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setupCharts()
        loadData()
    }

    // region UI setup
    private fun initViews() {
        val years = repository.years
        val districts = repository.districts

        binding.yearSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            years
        )

        binding.districtSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            districts
        )

        binding.yearSpinner.setSelection(years.indexOf(selectedYear))
        binding.districtSpinner.setSelection(districts.indexOf(selectedDistrict))

        binding.yearSpinner.setOnItemSelectedListener(SimpleItemSelectedListener { position ->
            selectedYear = years[position]
            updateCharts()
        })

        binding.districtSpinner.setOnItemSelectedListener(SimpleItemSelectedListener { position ->
            selectedDistrict = districts[position]
            updateCharts()
        })
    }
    // endregion

    // region Chart setup
    private fun setupCharts() {
        setupLineChart(binding.lineChart)
        setupBarChart(binding.barChart)
        setupPieChart(binding.pieChart)

        val marker = DashboardMarkerView()
        binding.lineChart.marker = marker
        binding.barChart.marker = marker
        binding.pieChart.marker = marker
    }

    private fun setupLineChart(chart: LineChart) {
        chart.description.isEnabled = false
        chart.legend.isEnabled = true
        chart.axisRight.isEnabled = false
        chart.setTouchEnabled(true)
        chart.setPinchZoom(true)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.granularity = 1f
        chart.xAxis.setDrawGridLines(false)
        chart.axisLeft.valueFormatter = CompactNumberFormatter(" min")
    }

    private fun setupBarChart(chart: BarChart) {
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.setFitBars(true)
        chart.setScaleEnabled(true)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.granularity = 1f
        chart.xAxis.setDrawGridLines(false)
        chart.axisLeft.valueFormatter = CompactNumberFormatter(" sub.")
    }

    private fun setupPieChart(chart: PieChart) {
        chart.description.isEnabled = false
        chart.isDrawHoleEnabled = true
        chart.holeRadius = 55f
        chart.setUsePercentValues(true)
        chart.setEntryLabelColor(Color.WHITE)
        chart.legend.isWordWrapEnabled = true
    }
    // endregion

    // region Data
    private fun loadData() {
        updateCharts()
    }

    private fun updateCharts() {
        val dashboardData = repository.getDashboardData(selectedYear, selectedDistrict)

        binding.populationValue.text = String.format(Locale.getDefault(), "%,d", dashboardData.population)
        binding.salaryValue.text = String.format(Locale.getDefault(), "%.0f ₼", dashboardData.avgSalary)
        binding.businessValue.text = String.format(Locale.getDefault(), "%,d", dashboardData.businessCount)

        bindLineData(dashboardData)
        bindBarData(dashboardData)
        bindPieData(dashboardData)
    }

    private fun bindLineData(data: DashboardData) {
        val trendEntries = data.yearlyPopulationTrend.mapIndexed { index, value ->
            Entry(index.toFloat(), value.toFloat())
        }

        val set = LineDataSet(trendEntries, "Əhali dinamikası")
        set.color = Color.parseColor("#1E88E5")
        set.lineWidth = 2.4f
        set.valueTextColor = Color.parseColor("#1565C0")
        set.valueFormatter = CompactNumberFormatter("k")
        set.setCircleColor(Color.parseColor("#0D47A1"))
        set.circleHoleColor = Color.WHITE
        set.mode = LineDataSet.Mode.CUBIC_BEZIER
        set.setDrawFilled(true)
        set.fillDrawable = gradientDrawable()

        binding.lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(data.yearLabels)
        binding.lineChart.data = LineData(set)
        binding.lineChart.animateX(700)
        binding.lineChart.invalidate()
    }

    private fun bindBarData(data: DashboardData) {
        val entries = repository.getDistrictComparison(selectedYear).mapIndexed { index, stat ->
            BarEntry(index.toFloat(), stat.businessCount.toFloat())
        }

        val set = BarDataSet(entries, "Sahibkarlıq subyektləri")
        set.setGradientColor(
            Color.parseColor("#26A69A"),
            Color.parseColor("#80CBC4")
        )
        set.valueTextColor = Color.parseColor("#004D40")
        set.valueFormatter = CompactNumberFormatter("")

        val districtLabels = repository.getDistrictComparison(selectedYear).map { it.name }
        binding.barChart.xAxis.valueFormatter = IndexAxisValueFormatter(districtLabels)
        binding.barChart.data = BarData(set).apply { barWidth = 0.55f }
        binding.barChart.animateY(700)
        binding.barChart.invalidate()
    }

    private fun bindPieData(data: DashboardData) {
        val entries = listOf(
            PieEntry(data.population.toFloat(), "Əhali"),
            PieEntry(data.avgSalary.toFloat(), "Orta maaş"),
            PieEntry(data.businessCount.toFloat(), "Subyekt")
        )

        val set = PieDataSet(entries, "Struktur")
        set.colors = listOf(
            Color.parseColor("#5E35B1"),
            Color.parseColor("#7E57C2"),
            Color.parseColor("#B39DDB")
        )
        set.valueTextSize = 12f
        set.valueTextColor = Color.WHITE

        binding.pieChart.data = PieData(set).apply {
            setValueFormatter(PercentFormatter())
        }
        binding.pieChart.centerText = "${selectedDistrict} / $selectedYear"
        binding.pieChart.animateXY(600, 600)
        binding.pieChart.invalidate()
    }
    // endregion

    private fun gradientDrawable(): Drawable? =
        ContextCompat.getDrawable(this, R.drawable.chart_line_gradient)

    private inner class DashboardMarkerView : MarkerView(this, android.R.layout.simple_list_item_1) {
        private val textView: TextView = findViewById(android.R.id.text1)

        override fun refreshContent(e: Entry?, highlight: Highlight?) {
            textView.text = e?.y?.let { "Dəyər: ${it.toInt()}" } ?: "-"
            super.refreshContent(e, highlight)
        }

        override fun getOffset(): MPPointF = MPPointF(-(width / 2f), -height.toFloat())
    }
}

private data class DashboardData(
    val year: Int,
    val district: String,
    val population: Int,
    val avgSalary: Float,
    val businessCount: Int,
    val yearlyPopulationTrend: List<Int>,
    val yearLabels: List<String>
)

private data class DistrictStat(
    val name: String,
    val population: Int,
    val avgSalary: Float,
    val businessCount: Int
)

private class DashboardRepository {
    val years = listOf(2021, 2022, 2023, 2024)
    val districts = listOf("Göygöl", "Gəncə", "Mingəçevir", "Şəki", "Qəbələ")

    private val data: Map<Int, List<DistrictStat>> = mapOf(
        2021 to listOf(
            DistrictStat("Göygöl", 63600, 528f, 1205),
            DistrictStat("Gəncə", 335300, 610f, 4280),
            DistrictStat("Mingəçevir", 106500, 590f, 2044),
            DistrictStat("Şəki", 188100, 560f, 2310),
            DistrictStat("Qəbələ", 108700, 572f, 1578)
        ),
        2022 to listOf(
            DistrictStat("Göygöl", 64462, 570f, 1286),
            DistrictStat("Gəncə", 338100, 647f, 4412),
            DistrictStat("Mingəçevir", 107800, 618f, 2120),
            DistrictStat("Şəki", 189400, 582f, 2388),
            DistrictStat("Qəbələ", 110200, 596f, 1662)
        ),
        2023 to listOf(
            DistrictStat("Göygöl", 64912, 603f, 1364),
            DistrictStat("Gəncə", 341400, 688f, 4558),
            DistrictStat("Mingəçevir", 109100, 651f, 2196),
            DistrictStat("Şəki", 191300, 611f, 2485),
            DistrictStat("Qəbələ", 111700, 628f, 1745)
        ),
        2024 to listOf(
            DistrictStat("Göygöl", 65047, 631.5f, 1431),
            DistrictStat("Gəncə", 344200, 724f, 4688),
            DistrictStat("Mingəçevir", 110700, 676f, 2281),
            DistrictStat("Şəki", 193000, 640f, 2572),
            DistrictStat("Qəbələ", 113300, 652f, 1827)
        )
    )

    fun getDashboardData(year: Int, district: String): DashboardData {
        val districtStats = data[year].orEmpty().firstOrNull { it.name == district }
            ?: data[year].orEmpty().first()

        val trend = years.map { yearValue ->
            data[yearValue].orEmpty().firstOrNull { it.name == district }?.population ?: 0
        }

        return DashboardData(
            year = year,
            district = district,
            population = districtStats.population,
            avgSalary = districtStats.avgSalary,
            businessCount = districtStats.businessCount,
            yearlyPopulationTrend = trend,
            yearLabels = years.map(Int::toString)
        )
    }

    fun getDistrictComparison(year: Int): List<DistrictStat> = data[year].orEmpty()
}

private class CompactNumberFormatter(private val suffix: String) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String =
        String.format(Locale.getDefault(), "%,.0f%s", value, suffix)
}

private class PercentFormatter : ValueFormatter() {
    override fun getPieLabel(value: Float, pieEntry: PieEntry?): String =
        String.format(Locale.getDefault(), "%.1f%%", value)
}
