package com.anilog.omor.dev.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anilog.omor.dev.R
import com.anilog.omor.dev.data.repository.UserPreferencesRepository
import com.anilog.omor.dev.domain.model.AppThemeMode
import com.anilog.omor.dev.domain.model.SortOption
import com.anilog.omor.dev.ui.ranking.RafaelSettingsDialog
import com.anilog.omor.dev.util.LocaleHelper
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    preferencesRepository: UserPreferencesRepository,
    onNavigateToBackup: () -> Unit,
    onNavigateToBulkImport: () -> Unit,
    onNavigateToExportFormats: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(uiState.activeRecoveryDeadline) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000L)
        }
    }
    val isCooldownActive = preferencesRepository.isRecoveryActive(uiState.activeRecoveryDeadline, currentTime)
    val remainingCooldownSeconds = preferencesRepository.getRemainingRecoverySeconds(uiState.activeRecoveryDeadline, currentTime)

    var showThemeMenu by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showSetPinDialog by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }
    var showRafaelDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.nav_settings),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Language & Localization Section (Section 3.4)
            SettingsSectionHeader(title = stringResource(R.string.setting_language))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLanguageMenu = true }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Translate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Language / ভাষা", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = if (uiState.appLanguage == LocaleHelper.LANGUAGE_BENGALI) "বাংলা (Bangla)" else "English",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Box {
                            TextButton(onClick = { showLanguageMenu = true }, modifier = Modifier.testTag("btn_select_language")) {
                                Text("Change / পরিবর্তন")
                            }
                            DropdownMenu(
                                expanded = showLanguageMenu,
                                onDismissRequest = { showLanguageMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "English",
                                            fontWeight = if (uiState.appLanguage == LocaleHelper.LANGUAGE_ENGLISH) FontWeight.Bold else FontWeight.Normal,
                                            color = if (uiState.appLanguage == LocaleHelper.LANGUAGE_ENGLISH) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        viewModel.setAppLanguage(LocaleHelper.LANGUAGE_ENGLISH)
                                        showLanguageMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "বাংলা (Bangla)",
                                            fontWeight = if (uiState.appLanguage == LocaleHelper.LANGUAGE_BENGALI) FontWeight.Bold else FontWeight.Normal,
                                            color = if (uiState.appLanguage == LocaleHelper.LANGUAGE_BENGALI) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        viewModel.setAppLanguage(LocaleHelper.LANGUAGE_BENGALI)
                                        showLanguageMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Appearance & Theming Section
            SettingsSectionHeader(title = "Appearance & Theming")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Theme Mode Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showThemeMenu = true }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.ColorLens, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("App Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(uiState.themeMode.displayName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Box {
                            TextButton(onClick = { showThemeMenu = true }, modifier = Modifier.testTag("btn_select_theme")) {
                                Text("Change")
                            }
                            DropdownMenu(
                                expanded = showThemeMenu,
                                onDismissRequest = { showThemeMenu = false }
                            ) {
                                AppThemeMode.entries.forEach { mode ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = mode.displayName,
                                                fontWeight = if (uiState.themeMode == mode) FontWeight.Bold else FontWeight.Normal,
                                                color = if (uiState.themeMode == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            viewModel.setThemeMode(mode)
                                            showThemeMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Default Feed Sort
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSortMenu = true }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Sort, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Default Feed Sorting", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(uiState.defaultSort.displayName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Box {
                            TextButton(onClick = { showSortMenu = true }) {
                                Text("Change")
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                SortOption.entries.forEach { sort ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = sort.displayName,
                                                fontWeight = if (uiState.defaultSort == sort) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            viewModel.setDefaultSort(sort)
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Rafael Intelligent Ranking Engine Section (Section 6)
            SettingsSectionHeader(title = stringResource(R.string.rafael_title))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showRafaelDialog = true }
                        .padding(16.dp)
                        .testTag("settings_rafael_row"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(stringResource(R.string.rafael_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(stringResource(R.string.rafael_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }

            // Security & Privacy Lock (Section 11)
            SettingsSectionHeader(title = "Privacy & Security")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Passcode PIN Lock", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = if (uiState.isPasscodeEnabled) "PIN protection enabled" else "Require 4-digit PIN to open AniLog",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = uiState.isPasscodeEnabled,
                            onCheckedChange = { enabled ->
                                if (isCooldownActive) {
                                    val m = remainingCooldownSeconds / 60
                                    val s = remainingCooldownSeconds % 60
                                    Toast.makeText(context, "নিরাপত্তা বিরতি সক্রিয়: ${m}m ${s}s পর নতুন পাসওয়ার্ড সেট করতে পারবেন", Toast.LENGTH_LONG).show()
                                } else if (enabled && !uiState.hasPasscodeSet) {
                                    showSetPinDialog = true
                                } else {
                                    viewModel.togglePasscodeEnabled(enabled)
                                }
                            },
                            modifier = Modifier.testTag("switch_passcode")
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Configurable Passcode Recovery Waiting Period (Section 11.1: 5 through 60 minutes)
                    Column {
                        Text(
                            text = stringResource(R.string.recovery_duration_setting),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Waiting duration: ${uiState.recoveryDurationMinutes} minutes (5–60 min range)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Slider(
                            value = uiState.recoveryDurationMinutes.toFloat(),
                            onValueChange = { viewModel.setRecoveryDurationMinutes(it.toInt()) },
                            valueRange = 5f..60f,
                            steps = 54,
                            modifier = Modifier.testTag("slider_recovery_duration")
                        )
                    }

                    if (isCooldownActive) {
                        val minutes = remainingCooldownSeconds / 60
                        val seconds = remainingCooldownSeconds % 60
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.HourglassTop,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "পাসওয়ার্ড রিসেট হয়েছে (Security Recovery Active)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = "নতুন পাসওয়ার্ড সেট করতে পারবেন: ${minutes}m ${seconds}s পর",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    } else if (uiState.hasPasscodeSet) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { showSetPinDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Change PIN")
                        }
                    }
                }
            }

            // Data & Bulk Management Section (Sections 8 & 10)
            SettingsSectionHeader(title = "Data & Bulk Management")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Bulk Anime Import
                    SettingsActionRow(
                        icon = Icons.Filled.PostAdd,
                        title = stringResource(R.string.bulk_import_title),
                        subtitle = "Paste hundreds of titles, numbered/bulleted/CSV lists",
                        onClick = onNavigateToBulkImport
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    // Multiple Export & Copy Formats
                    SettingsActionRow(
                        icon = Icons.Filled.TextSnippet,
                        title = stringResource(R.string.export_formats_title),
                        subtitle = "Formats A, B, C and full structured JSON export",
                        onClick = onNavigateToExportFormats
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    // Full ZIP Backup & Restore
                    SettingsActionRow(
                        icon = Icons.Filled.Archive,
                        title = "Full ZIP Backup & Restore",
                        subtitle = "Full ZIP archive with cover images and cross-refs",
                        onClick = onNavigateToBackup
                    )
                }
            }

            // Section 1: MANDATORY OFFICIAL DEVELOPER SECTION (Preserved exact values)
            SettingsSectionHeader(title = "Developer")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Developer Name
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Developer", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Muhammed Omor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Official Shop Button: https://muhammedomor.supportkori.shop/
                    Button(
                        onClick = {
                            val shopUrl = "https://muhammedomor.supportkori.shop/"
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(shopUrl))
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Could not open browser for shop", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_developer_shop")
                    ) {
                        Icon(Icons.Filled.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Visit Official Shop")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Email Contact Button: muhammedomor.studio@gmail.com with subject "AniLog App Support"
                    OutlinedButton(
                        onClick = {
                            val email = "muhammedomor.studio@gmail.com"
                            val subject = "AniLog App Support"
                            val mailtoUri = Uri.parse("mailto:$email?subject=${Uri.encode(subject)}")
                            val emailIntent = Intent(Intent.ACTION_SENDTO, mailtoUri)
                            try {
                                context.startActivity(emailIntent)
                            } catch (_: Exception) {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Developer Email", email)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Email copied to clipboard: $email", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_contact_email")
                    ) {
                        Icon(Icons.Filled.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Contact: muhammedomor.studio@gmail.com")
                    }
                }
            }

            // About & Legal Section
            SettingsSectionHeader(title = "About & Legal")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // About AniLog
                    SettingsActionRow(
                        icon = Icons.Filled.Info,
                        title = "About AniLog",
                        subtitle = "Version 1.0 (Build 1)",
                        onClick = { showAboutDialog = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    // Privacy Policy
                    SettingsActionRow(
                        icon = Icons.Filled.Policy,
                        title = "Privacy Policy",
                        subtitle = "100% on-device private journal disclosure",
                        onClick = onNavigateToPrivacyPolicy
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    // Open Source Licenses
                    SettingsActionRow(
                        icon = Icons.Filled.Code,
                        title = "Open Source Licenses",
                        subtitle = "Android Jetpack, Room, Kotlin, Coil, OkHttp",
                        onClick = { showLicensesDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }

        // Rafael Settings Dialog
        if (showRafaelDialog) {
            RafaelSettingsDialog(
                preferencesRepository = preferencesRepository,
                onDismiss = { showRafaelDialog = false }
            )
        }

        // Set PIN Dialog
        if (showSetPinDialog) {
            AlertDialog(
                onDismissRequest = { showSetPinDialog = false },
                title = { Text("Set 4-Digit Passcode PIN") },
                text = {
                    Column {
                        Text(
                            text = "Enter a 4-digit numeric PIN to protect your anime journal:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = newPinInput,
                            onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) newPinInput = it },
                            label = { Text("4-Digit PIN") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_pin_dialog")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newPinInput.length == 4 && !isCooldownActive) {
                                viewModel.setPasscode(newPinInput)
                                showSetPinDialog = false
                                newPinInput = ""
                            }
                        },
                        enabled = newPinInput.length == 4 && !isCooldownActive,
                        modifier = Modifier.testTag("btn_confirm_pin")
                    ) {
                        Text(stringResource(R.string.action_save))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSetPinDialog = false }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            )
        }

        // About Dialog
        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                icon = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_anilog_logo),
                        contentDescription = "AniLog Official A Logo",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                },
                title = { Text("AniLog") },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("AniLog — Personal Anime Library", fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.tagline))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Developer: Muhammed Omor")
                        Text("Version: 1.0 (Production Native Android)")
                        Text("Storage: SQLite with Room Database (100% offline-ready)")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) {
                        Text(stringResource(R.string.action_close))
                    }
                }
            )
        }

        // Open Source Licenses Dialog
        if (showLicensesDialog) {
            AlertDialog(
                onDismissRequest = { showLicensesDialog = false },
                title = { Text("Open Source Licenses") },
                text = {
                    Column(
                        modifier = Modifier
                            .height(260.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("• Android Jetpack & Jetpack Compose (Apache 2.0)", fontWeight = FontWeight.SemiBold)
                        Text("• AndroidX Room Persistence (Apache 2.0)", fontWeight = FontWeight.SemiBold)
                        Text("• Kotlin Coroutines & Flow (Apache 2.0)", fontWeight = FontWeight.SemiBold)
                        Text("• Coil Image Loading for Compose (Apache 2.0)", fontWeight = FontWeight.SemiBold)
                        Text("• Square OkHttp (Apache 2.0)", fontWeight = FontWeight.SemiBold)
                        Text("• Material Design 3 Components (Apache 2.0)", fontWeight = FontWeight.SemiBold)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLicensesDialog = false }) {
                        Text("Done")
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
