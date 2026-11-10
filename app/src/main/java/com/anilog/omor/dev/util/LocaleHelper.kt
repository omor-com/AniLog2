package com.anilog.omor.dev.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {
    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_BENGALI = "bn"

    fun setLocale(context: Context, languageCode: String): Context {
        val targetLocale = if (languageCode == LANGUAGE_BENGALI) Locale.forLanguageTag("bn") else Locale.ENGLISH
        Locale.setDefault(targetLocale)

        val resources = context.resources
        val config = Configuration(resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(targetLocale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = targetLocale
        }

        return context.createConfigurationContext(config)
    }

    fun applyConfiguration(context: Context, languageCode: String) {
        val targetLocale = if (languageCode == LANGUAGE_BENGALI) Locale.forLanguageTag("bn") else Locale.ENGLISH
        Locale.setDefault(targetLocale)

        val resources = context.resources
        val config = resources.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(targetLocale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = targetLocale
        }
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
