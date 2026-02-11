package az.stat.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreenActivity : AppCompatActivity() {

    private val splashDurationMs = 2500L
    private var isSplashVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        // Android 12+ üçün rəsmi SplashScreen API.
        // androidx.core:splashscreen kitabxanası ilə köhnə versiyalara da uyğun işləyir.
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        splashScreen.setKeepOnScreenCondition { isSplashVisible }

        // --- Seçilmiş yanaşma: modern SplashScreen API + coroutine delay ---
        lifecycleScope.launch {
            delay(splashDurationMs)
            isSplashVisible = false
            openMainActivity()
        }

        // --- Alternativ (ənənəvi) yanaşma: Handler/timer ---
        // Əgər istəsəniz yuxarıdakı coroutine blokunu silib bu metodu çağıra bilərsiniz.
        // startWithLegacyHandler()
    }

    private fun startWithLegacyHandler() {
        Handler(Looper.getMainLooper()).postDelayed({
            isSplashVisible = false
            openMainActivity()
        }, splashDurationMs)
    }

    private fun openMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
