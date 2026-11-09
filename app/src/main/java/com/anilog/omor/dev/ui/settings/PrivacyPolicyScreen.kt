package com.anilog.omor.dev.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "AniLog Privacy Policy",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Last updated: November 2026",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            PolicySection(
                title = "1. Local-First & Zero Data Collection",
                body = "AniLog is strictly an on-device personal anime journal. All your anime entries, custom cover images, personal ratings, multiline reviews, notes, and custom collections are stored locally on your device in an isolated SQLite database and private app storage. AniLog does not operate external user accounts, cloud tracking databases, or telemetry servers."
            )

            PolicySection(
                title = "2. No Analytics, Tracking, or Advertising",
                body = "AniLog contains zero third-party advertisement SDKs, analytical trackers, user profiling tools, or behavioral cookies. Your personal thoughts, ratings, and viewing habits remain entirely private to you."
            )

            PolicySection(
                title = "3. Media & Storage Permissions",
                body = "AniLog utilizes Android's zero-permission Photo Picker for choosing cover artwork. The application never asks for broad READ_EXTERNAL_STORAGE permissions. Imported artwork is saved into private app-internal directories."
            )

            PolicySection(
                title = "4. Optional Online Anime Lookup",
                body = "AniLog includes an optional online search feature powered by the open, public Jikan API. When you voluntarily search for an anime title, the search term is sent over HTTPS to retrieve public title, poster, and episode details. No personal reviews or device identifiers are ever transmitted."
            )

            PolicySection(
                title = "5. Sharing & Exports",
                body = "When you export a backup or share a generated Review Card, files are handled strictly via Android's secure FileProvider and Android Sharesheet. You control where and how files are stored or shared."
            )

            PolicySection(
                title = "6. Developer Contact",
                body = "Developer: Muhammed Omor\nContact Email: muhammedomor.studio@gmail.com\nOfficial Shop: https://muhammedomor.supportkori.shop/"
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PolicySection(title: String, body: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
    }
}
