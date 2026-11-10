package com.anilog.omor.dev

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.anilog.omor.dev.domain.model.AppThemeMode
import com.anilog.omor.dev.ui.navigation.AniLogNavGraph
import com.anilog.omor.dev.ui.theme.AniLogTheme
import com.anilog.omor.dev.util.LocaleHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as AniLogApplication

        setContent {
            val themeMode by app.userPreferencesRepository.themeMode.collectAsStateWithLifecycle(AppThemeMode.SYSTEM)
            val appLanguage by app.userPreferencesRepository.appLanguage.collectAsStateWithLifecycle(LocaleHelper.LANGUAGE_ENGLISH)
            val context = LocalContext.current

            LaunchedEffect(appLanguage) {
                LocaleHelper.applyConfiguration(context, appLanguage)
            }

            AniLogTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    AniLogNavGraph(
                        app = app,
                        navController = navController
                    )
                }
            }
        }
    }
}
