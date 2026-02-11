package az.stat.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri

// --- App Colors ---
val ProfessionalBlue = Color(0xFF003366)
val LightGrayBackground = Color(0xFFF5F5F5)
val HighlightGreen = Color(0xFF2E7D32)
val TrendRed = Color(0xFFD32F2F)

// --- 1. Data Model ---
data class StatItem(
    val label: String,
    val value: String,
    val unit: String,
    val changeText: String,
    val isPositive: Boolean
)

// --- 2. Manual Data (2021-2024) ---
val stats2024: Map<String, List<StatItem>> = mapOf(
    "DEMOQRAFİYA" to listOf(
        StatItem("Əhalinin sayı", "65047", "nəfər", "+0.4%", true),
        StatItem("Kişilər", "32985", "nəfər", "+0.4%", true),
        StatItem("Qadınlar", "32062", "nəfər", "+0.4%", true),
        StatItem("Şəhər əhalisi", "25711", "nəfər", "+0.2%", true),
        StatItem("Kənd əhalisi", "39336", "nəfər", "+0.5%", true),
        StatItem("Doğulanlar", "712", "nəfər", "-9.1%", false),
        StatItem("Ölənlər", "455", "nəfər", "-0.7%", false),
        StatItem("Təbii artım", "257", "nəfər", "-20.9%", false),
        StatItem("Gözlənilən ömür uzunluğu", "75.3", "il", "", true),
        StatItem("1 yaşadək ölən uşaqlar", "13", "nəfər", "0.0%", true),
        StatItem("Ana ölümü", "1", "nəfər", "+1.0%", true),
        StatItem("Nikahlar", "313", "ədəd", "-6.8%", false),
        StatItem("Boşanmalar", "178", "ədəd", "+6.0%", true)
    ),
    "SOSİAL MÜDAFİƏ" to listOf(
        StatItem("Pensiyaçıların sayı", "6269", "nəfər", "+0.03%", true),
        StatItem("Yaşa görə", "3833", "nəfər", "-1.1%", false),
        StatItem("Əlilliyə görə", "1645", "nəfər", "+0.9%", true),
        StatItem("Ailə başçısını itirməyə görə", "791", "nəfər", "+3.9%", true),
        StatItem("Orta pensiya məbləği", "391.18", "manat", "+10.6%", true),
        StatItem("Sosial müavinət alanlar", "3230", "nəfər", "+3.8%", true),
        StatItem("Bir nəfərə düşən orta aylıq müavinət", "187.61", "manat", "+0.5%", true),
        StatItem("ÜDSY alan ailələr", "1462", "ailə", "+65.9%", true),
        StatItem("ÜDSY alan ailə üzvləri", "6424", "nəfər", "+71.2%", true),
        StatItem("Orta aylıq ÜDSY məbləği", "108.20", "manat", "-7.2%", false),
        StatItem("Özəlləşdirilmiş mənzillər (say)", "3", "ədəd", "+50.0%", true),
        StatItem("Özəlləşdirilmiş mənzillər (sahə)", "147.8", "kv.m", "+42.3%", true)
    ),
    "MAKROİQTİSADİYYAT" to listOf(
        StatItem("Sənaye", "56767.3", "min manat", "+4.4%", true),
        StatItem("Kənd təsərrüfatı", "71813.5", "min manat", "-2.2%", false),
        StatItem("Tikinti", "39771.2", "min manat", "+12.7%", true),
        StatItem("Nəqliyyat", "8188.9", "min manat", "+36.2%", true),
        StatItem("İnformasiya və rabitə", "1793.0", "min manat", "+29.9%", true),
        StatItem("Ticarət", "74051.5", "min manat", "+4.9%", true)
    ),
    "ƏMƏK BAZARI" to listOf(
        StatItem("İşçi qüvvəsi", "35218", "nəfər", "+1.3%", true),
        StatItem("Məşğul əhali", "33472", "nəfər", "+2.1%", true),
        StatItem("Muzdlu işçilər", "8029", "nəfər", "-1.6%", false),
        StatItem("Orta aylıq nominal əməkhaqqı", "677.2", "manat", "+7.2%", true),
        StatItem("Yeni açılmış iş yerləri", "223", "ədəd", "+7.7%", true),
        StatItem("İşsiz əhali", "1746", "nəfər", "-11.8%", false),
        StatItem("İşsizlik səviyyəsi (%)", "5.0", "%", "", true)
    ),
    "SƏNAYE" to listOf(
        StatItem("Fəaliyyət göstərən sənaye müəssisələrinin sayı", "34", "ədəd", "0.0%", true),
        StatItem("Sənaye məhsulunun həcmi", "56767.3", "min manat", "+16.9%", true),
        StatItem("Hüquqi şəxslər", "687", "ədəd", "+2.7%", true),
        StatItem("Fiziki şəxslər", "10321", "ədəd", "+5.4%", true),
    ),
    "KƏND TƏSƏRRÜFATI" to listOf(
        StatItem("Dənli bitkilərin əkin sahəsi", "4347.8", "ha", "-14.8%", false),
        StatItem("Dənli bitkilərin istehsalı", "12395.0", "ton", "+44.8%", true),
        StatItem("İribuynuzlu mal-qara", "20433", "baş", "+2.2%", true),
        StatItem("Qoyun və keçilər", "118993", "baş", "+3.8%", true)
    ),
    "ASAYİŞ" to listOf(
        StatItem("Ümumi cinayətlər", "235", "ədəd", "-4.1%", false),
        StatItem("Ağır cinayətlər", "16", "ədəd", "-40.7%", false),
        StatItem("Yetkinlik yaşına çatmayanlar", "11", "ədəd", "+120.0%", true),
        StatItem("Zərərçəkmiş şəxslər", "219", "nəfər", "-12.0%", false)
    ),
)
val stats2023: Map<String, List<StatItem>> = mapOf(
    "DEMOQRAFİYA" to listOf(
        StatItem("Əhalinin sayı", "64793", "nəfər", "+0.5%", true),
        StatItem("Kişilər", "32846", "nəfər", "+0.7%", true),
        StatItem("Qadınlar", "31947", "nəfər", "+0.3%", true),
        StatItem("Şəhər əhalisi", "25666", "nəfər", "+0.2%", true),
        StatItem("Kənd əhalisi", "39127", "nəfər", "+0.7%", true),
        StatItem("Doğulanlar", "783", "nəfər", "-9.5%", false),
        StatItem("Ölənlər", "458", "nəfər", "-0.9%", false),
        StatItem("Təbii artım", "325", "nəfər", "-19.4%", false),
        StatItem("Gözlənilən ömür uzunluğu", "74.8", "il", "", true),
        StatItem("1 yaşadək ölən uşaqlar", "13", "nəfər", "+160.0%", true),
        StatItem("Ana ölümü", "0", "nəfər", "0.0%", true),
        StatItem("Nikahlar", "336", "ədəd", "-15.8%", false),
        StatItem("Boşanmalar", "168", "ədəd", "+93.1%", true)
    ),
    "SOSİAL MÜDAFİƏ" to listOf(
        StatItem("Pensiyaçıların sayı", "6267", "nəfər", "-4.8%", false),
        StatItem("Yaşa görə", "3876", "nəfər", "-1.5%", false),
        StatItem("Əlilliyə görə", "1630", "nəfər", "-1.6%", false),
        StatItem("Ailə başçısını itirməyə görə", "761", "nəfər", "-23.1%", false),
        StatItem("Orta pensiya məbləği", "353.58", "manat", "+16.7%", true),
        StatItem("Sosial müavinət alanlar", "3113", "nəfər", "+6.1%", true),
        StatItem("Bir nəfərə düşən orta aylıq müavinət", "186.66", "manat", "+20.2%", true),
        StatItem("ÜDSY alan ailələr", "881", "ailə", "-8.7%", false),
        StatItem("ÜDSY alan ailə üzvləri", "3753", "nəfər", "-12.3%", false),
        StatItem("Orta aylıq ÜDSY məbləği", "116.63", "manat", "+27.5%", true),
        StatItem("Özəlləşdirilmiş mənzillər (say)", "2", "ədəd", "0.0%", true),
        StatItem("Özəlləşdirilmiş mənzillər (sahə)", "103.9", "kv.m", "-68.5%", false)
    ),
    "MAKROİQTİSADİYYAT" to listOf(
        StatItem("Sənaye", "48543.8", "min manat", "-2.6%", false),
        StatItem("Kənd təsərrüfatı", "69454.0", "min manat", "-4.8%", false),
        StatItem("Tikinti", "35281.6", "min manat", "-71.3%", false),
        StatItem("Nəqliyyat", "6087.6", "min manat", "+4.8%", true),
        StatItem("İnformasiya və rabitə", "1376.3", "min manat", "+20.4%", true),
        StatItem("Ticarət", "69568.0", "min manat", "+1.0%", true)
    ),
    "ƏMƏK BAZARI" to listOf(
        StatItem("İşçi qüvvəsi", "34770", "nəfər", "+1.4%", true),
        StatItem("Məşğul əhali", "32791", "nəfər", "+1.7%", true),
        StatItem("Muzdlu işçilər", "8157", "nəfər", "+0.7%", true),
        StatItem("Orta aylıq nominal əməkhaqqı", "631.5", "manat", "+16.1%", true),
        StatItem("Yeni açılmış iş yerləri", "207", "ədəd", "+218.5%", true),
        StatItem("İşsiz əhali", "1979", "nəfər", "-2.7%", false),
        StatItem("İşsizlik səviyyəsi (%)", "5.7", "%", "", true)
    ),
    "SƏNAYE" to listOf(
        StatItem("Fəaliyyət göstərən sənaye müəssisələrinin sayı", "34", "ədəd", "0.0%", true),
        StatItem("Sənaye məhsulunun həcmi", "48543.8", "min manat", "-2.2%", false),
        StatItem("Hüquqi şəxslər", "669", "ədəd", "-3.1%", false),
        StatItem("Fiziki şəxslər", "9789", "ədəd", "+11.3%", true),
    ),
    "KƏND TƏSƏRRÜFATI" to listOf(
        StatItem("Dənli bitkilərin əkin sahəsi", "5101.7", "ha", "-21.9%", false),
        StatItem("Dənli bitkilərin istehsalı", "8561.8", "ton", "-57.7%", false),
        StatItem("İribuynuzlu mal-qara", "19989", "baş", "+3.4%", true),
        StatItem("Qoyun və keçilər", "114611", "baş", "-8.8%", false)
    ),
    "ASAYİŞ" to listOf(
        StatItem("Ümumi cinayətlər", "245", "ədəd", "-7.9%", false),
        StatItem("Ağır cinayətlər", "27", "ədəd", "+17.4%", true),
        StatItem("Yetkinlik yaşına çatmayanlar", "5", "ədəd", "+150.0%", true),
        StatItem("Zərərçəkmiş şəxslər", "249", "nəfər", "+12.7%", true)
    ),
)
val stats2022: Map<String, List<StatItem>> = mapOf(
    "DEMOQRAFİYA" to listOf(
        StatItem("Əhalinin sayı", "64462", "nəfər", "+0.8%", true),
        StatItem("Kişilər", "32626", "nəfər", "+0.9%", true),
        StatItem("Qadınlar", "31836", "nəfər", "+0.7%", true),
        StatItem("Şəhər əhalisi", "25618", "nəfər", "+0.7%", true),
        StatItem("Kənd əhalisi", "38844", "nəfər", "+0.8%", true),
        StatItem("Doğulanlar", "865", "nəfər", "+22.0%", true),
        StatItem("Ölənlər", "462", "nəfər", "-7.8%", false),
        StatItem("Təbii artım", "403", "nəfər", "+93.8%", true),
        StatItem("Gözlənilən ömür uzunluğu", "74.6", "il", "", true),
        StatItem("1 yaşadək ölən uşaqlar", "5", "nəfər", "0.0%", true),
        StatItem("Ana ölümü", "0", "nəfər", "0.0", true),
        StatItem("Nikahlar", "399", "ədəd", "+5.6%", true),
        StatItem("Boşanmalar", "87", "ədəd", "-32.0%", false)
    ),
    "SOSİAL MÜDAFİƏ" to listOf(
        StatItem("Pensiyaçıların sayı", "6580", "nəfər", "-8.2%", false),
        StatItem("Yaşa görə", "3934", "nəfər", "-2.5%", false),
        StatItem("Əlilliyə görə", "1657", "nəfər", "-25.5%", false),
        StatItem("Ailə başçısını itirməyə görə", "989", "nəfər", "+9.2%", true),
        StatItem("Orta pensiya məbləği", "302.94", "manat", "+9.6%", true),
        StatItem("Sosial müavinət alanlar", "2934", "nəfər", "+4.3%", true),
        StatItem("Bir nəfərə düşən orta aylıq müavinət", "155.28", "manat", "+29.2%", true),
        StatItem("ÜDSY alan ailələr", "965", "ailə", "+53.2%", true),
        StatItem("ÜDSY alan ailə üzvləri", "4279", "nəfər", "+54.8%", true),
        StatItem("Orta aylıq ÜDSY məbləği", "91.49", "manat", "+41.6%", true),
        StatItem("Özəlləşdirilmiş mənzillər (say)", "2", "ədəd", "-71.4%", false),
        StatItem("Özəlləşdirilmiş mənzillər (sahə)", "330.0", "kv.m", "-8.1%", false)
    ),
    "MAKROİQTİSADİYYAT" to listOf(
        StatItem("Sənaye", "49658.5", "min manat", "-8.1%", false),
        StatItem("Kənd təsərrüfatı", "66957.4", "min manat", "-1.0%", false),
        StatItem("Tikinti", "122970.8", "min manat", "+127.0%", true),
        StatItem("Nəqliyyat", "5563.4", "min manat", "+40.9%", true),
        StatItem("İnformasiya və rabitə", "1142.7", "min manat", "+8.9%", true),
        StatItem("Ticarət", "63120.5", "min manat", "+0.1%", true)
    ),
    "ƏMƏK BAZARI" to listOf(
        StatItem("İşçi qüvvəsi", "34276", "nəfər", "+1.4%", true),
        StatItem("Məşğul əhali", "32242", "nəfər", "+1.9%", true),
        StatItem("Muzdlu işçilər", "8100", "nəfər", "-1.3%", false),
        StatItem("Orta aylıq nominal əməkhaqqı", "544.1", "manat", "+15.0%", true),
        StatItem("Yeni açılmış iş yerləri", "65", "ədəd", "-47.6%", false),
        StatItem("İşsiz əhali", "2034", "nəfər", "-6.0%", false),
        StatItem("İşsizlik səviyyəsi (%)", "5.9", "%", "", true)
    ),
    "SƏNAYE" to listOf(
        StatItem("Fəaliyyət göstərən sənaye müəssisələrinin sayı", "30", "ədəd", "+11.1%", true),
        StatItem("Sənaye məhsulunun həcmi", "49658.5", "min manat", "-6.59%", false),
        StatItem("Hüquqi şəxslər", "648", "ədəd", "+4.2%", true),
        StatItem("Fiziki şəxslər", "8796", "ədəd", "+10.9%", true),
    ),
    "KƏND TƏSƏRRÜFATI" to listOf(
        StatItem("Dənli bitkilərin əkin sahəsi", "6533.0", "ha", "-8.6%", false),
        StatItem("Dənli bitkilərin istehsalı", "20246.4", "ton", "-9.8%", false),
        StatItem("İribuynuzlu mal-qara", "19326", "baş", "+1.2%", true),
        StatItem("Qoyun və keçilər", "125716", "baş", "+3.0%", true)
    ),
    "ASAYİŞ" to listOf(
        StatItem("Ümumi cinayətlər", "266", "ədəd", "+20.4%", true),
        StatItem("Ağır cinayətlər", "23", "ədəd", "+91.7%", true),
        StatItem("Yetkinlik yaşına çatmayanlar", "2", "ədəd", "0.0%", true),
        StatItem("Zərərçəkmiş şəxslər", "221", "nəfər", "+15.1%", true)
    ),
)
val stats2021: Map<String, List<StatItem>> = mapOf(
    "DEMOQRAFİYA" to listOf(
        StatItem("Əhalinin sayı", "63978", "nəfər", "", true),
        StatItem("Kişilər", "32350", "nəfər", "", true),
        StatItem("Qadınlar", "31628", "nəfər", "", true),
        StatItem("Şəhər əhalisi", "25450", "nəfər", "", true),
        StatItem("Kənd əhalisi", "38528", "nəfər", "", true),
        StatItem("Doğulanlar", "709", "nəfər", "", false),
        StatItem("Ölənlər", "501", "nəfər", "", false),
        StatItem("Təbii artım", "208", "nəfər", "", false),
        StatItem("Gözlənilən ömür uzunluğu", "74.3", "il", "", true),
        StatItem("1 yaşadək ölən uşaqlar", "5", "nəfər", "", true),
        StatItem("Ana ölümü", "0", "nəfər", "", true),
        StatItem("Nikahlar", "378", "ədəd", "", true),
        StatItem("Boşanmalar", "128", "ədəd", "", false)
    ),
    "SOSİAL MÜDAFİƏ" to listOf(
        StatItem("Pensiyaçıların sayı", "7164", "nəfər", "", false),
        StatItem("Yaşa görə", "4035", "nəfər", "", true),
        StatItem("Əlilliyə görə", "2223", "nəfər", "", false),
        StatItem("Ailə başçısını itirməyə görə", "906", "nəfər", "", false),
        StatItem("Orta pensiya məbləği", "276.50", "manat", "", true),
        StatItem("Sosial müavinət alanlar", "2813", "nəfər", "", true),
        StatItem("Bir nəfərə düşən orta aylıq müavinət", "120.14", "manat", "", true),
        StatItem("ÜDSY alan ailələr", "630", "ailə", "", true),
        StatItem("ÜDSY alan ailə üzvləri", "2765", "nəfər", "", true),
        StatItem("Orta aylıq ÜDSY məbləği", "64.59", "manat", "", true),
        StatItem("Özəlləşdirilmiş mənzillər (say)", "7", "ədəd", "", true),
        StatItem("Özəlləşdirilmiş mənzillər (sahə)", "359.0", "kv.m", "", true)
    ),
    "MAKROİQTİSADİYYAT" to listOf(
        StatItem("Sənaye", "53160.4", "min manat", "", true),
        StatItem("Kənd təsərrüfatı", "58203.3", "min manat", "", true),
        StatItem("Tikinti", "54181.1", "min manat", "", true),
        StatItem("Nəqliyyat", "3859.0", "min manat", "", true),
        StatItem("İnformasiya və rabitə", "1050.3", "min manat", "", true),
        StatItem("Ticarət", "55066.3", "min manat", "", true)
    ),
    "ƏMƏK BAZARI" to listOf(
        StatItem("İşçi qüvvəsi", "33806", "nəfər", "", true),
        StatItem("Məşğul əhali", "31641", "nəfər", "", true),
        StatItem("Muzdlu işçilər", "8203", "nəfər", "", true),
        StatItem("Orta aylıq nominal əməkhaqqı", "473.2", "manat", "", true),
        StatItem("Yeni açılmış iş yerləri", "124", "ədəd", "", true),
        StatItem("İşsiz əhali", "2165", "nəfər", "", true),
        StatItem("İşsizlik səviyyəsi (%)", "6.4", "%", "", true)
    ),
    "SƏNAYE" to listOf(
        StatItem("Fəaliyyət göstərən sənaye müəssisələrinin sayı", "27", "ədəd", "", true),
        StatItem("Sənaye məhsulunun həcmi", "53160.4", "min manat", "", true),
        StatItem("Hüquqi şəxslər", "622", "ədəd", "", true),
        StatItem("Fiziki şəxslər", "7929", "ədəd", "", true),
    ),
    "KƏND TƏSƏRRÜFATI" to listOf(
        StatItem("Dənli bitkilərin əkin sahəsi", "7150.0", "ha", "", true),
        StatItem("Dənli bitkilərin istehsalı", "22437.6", "ton", "", true),
        StatItem("İribuynuzlu mal-qara", "19101", "baş", "", true),
        StatItem("Qoyun və keçilər", "122080", "baş", "", true)
    ),
    "ASAYİŞ" to listOf(
        StatItem("Ümumi cinayətlər", "221", "ədəd", "", true),
        StatItem("Ağır cinayətlər", "12", "ədəd", "", true),
        StatItem("Yetkinlik yaşına çatmayanlar", "2", "ədəd", "", true),
        StatItem("Zərərçəkmiş şəxslər", "192", "nəfər", "", true)
    )
)
val yearlyData = mapOf(
    2024 to stats2024,
    2023 to stats2023,
    2022 to stats2022,
    2021 to stats2021
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT
            )
        )

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = LightGrayBackground) {
                    StatAzScreen()
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatAzScreen() {
    val years = yearlyData.keys.sortedDescending()
    var selectedYear by remember { mutableStateOf(years.first()) }
    val currentData = yearlyData[selectedYear] ?: emptyMap()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "GÖYGÖL RAYONU",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ProfessionalBlue), // Düzgün sətir budur
                actions = {
                    Row {
                        IconButton(onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                "https://www.facebook.com/ruzigar.h".toUri()
                            )
                            context.startActivity(intent)
                        }) {
                            Icon(
                                Icons.Default.Facebook,
                                contentDescription = "Facebook",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                "https://www.youtube.com/@DataVeStatistika".toUri()
                            )
                            context.startActivity(intent)
                        }) {
                            Icon(
                                Icons.Default.SmartDisplay,
                                contentDescription = "YouTube",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                "https://www.linkedin.com/in/ruzigarhasanov/".toUri()
                            )
                            context.startActivity(intent)
                        }) {
                            Icon(
                                Icons.Default.AccountBox,
                                contentDescription = "LinkedIn",
                                tint = Color.White
                            )
                        }
                    }
                }
            )
        },
        containerColor = LightGrayBackground
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            YearSelector(
                years = years,
                selectedYear = selectedYear,
                onYearSelected = { selectedYear = it }
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(currentData.entries.toList()) { (categoryName, stats) ->
                    CategoryCard(categoryName = categoryName, stats = stats)
                }

                // Məlumat bölməsi siyahının sonuna əlavə edilir
                item {
                    InfoCard()
                }
            }
        }
    }
}

@Composable
fun InfoCard() {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {

                Spacer(modifier = Modifier.width(8.dp))

            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                "Məlumatları təqdim etdi:",
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                fontSize = 13.sp
            )
            Text(
                "Göygöl Rayon Statistika İdarəsi",
                color = ProfessionalBlue,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = LightGrayBackground
            )

            Text(
                text = "© 2025-2026",
                modifier = Modifier.fillMaxWidth(),
                color = Color.Gray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearSelector(years: List<Int>, selectedYear: Int, onYearSelected: (Int) -> Unit) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(years) { year ->
            val isSelected = year == selectedYear
            FilterChip(
                selected = isSelected,
                onClick = { onYearSelected(year) },
                label = { Text(text = year.toString()) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ProfessionalBlue,
                    selectedLabelColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = ProfessionalBlue,
                    selectedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun CategoryCard(categoryName: String, stats: List<StatItem>) {
    var isExpanded by remember { mutableStateOf(false) }

    val categoryIcon = when (categoryName) {
        "DEMOQRAFİYA" -> Icons.Default.Groups
        "SOSİAL MÜDAFİƏ" -> Icons.Default.VolunteerActivism
        "MAKROİQTİSADİYYAT" -> Icons.AutoMirrored.Filled.TrendingUp
        "ƏMƏK BAZARI" -> Icons.Default.Work
        "SƏNAYE" -> Icons.Default.Factory
        "KƏND TƏSƏRRÜFATİ" -> Icons.Default.Agriculture
        "HÜQUQİ VƏ FİZİKİ ŞƏXSLƏR" -> Icons.Default.PersonAddAlt1
        "ASAYİŞ" -> Icons.Default.Security
        else -> Icons.Default.Info
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .animateContentSize()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = categoryName,
                    tint = ProfessionalBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = categoryName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ProfessionalBlue,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = ProfessionalBlue
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    stats.forEachIndexed { index, item ->
                        if (index > 0) Divider(
                            color = LightGrayBackground,
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        StatItemRow(item = item)
                    }
                }
            }
        }
    }
}

@Composable
fun StatItemRow(item: StatItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label on the left
        Text(
            text = item.label,
            modifier = Modifier.weight(1.0f),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = ProfessionalBlue
        )

        // Value, Unit, and Trend on the right
        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = item.value,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = ProfessionalBlue
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = item.unit,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 1.dp)
                )
            }
            if (item.changeText.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    val trendColor = if (item.isPositive) HighlightGreen else TrendRed
                    val trendIcon =
                        if (item.isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
                    Icon(
                        imageVector = trendIcon,
                        contentDescription = "Trend",
                        tint = trendColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.changeText,
                        color = trendColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatAzScreenPreview() {
    MaterialTheme {
        StatAzScreen()
    }
}
