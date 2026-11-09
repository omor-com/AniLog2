package com.anilog.omor.dev.ui.privacy

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.ui.res.painterResource
import com.anilog.omor.dev.R
import com.anilog.omor.dev.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun PasscodeLockScreen(
    preferencesRepository: UserPreferencesRepository,
    onUnlocked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_anilog_logo),
                contentDescription = "AniLog Official Logo",
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "AniLog Protected",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter your passcode PIN to access your personal journal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // PIN Dots Indicator (4 or 6 digits)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            errorMessage?.let { err ->
                Text(
                    text = err,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Numeric Keypad
            Column(
                modifier = Modifier.width(280.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("", "0", "DEL")
                )

                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { key ->
                            if (key.isEmpty()) {
                                Spacer(modifier = Modifier.size(64.dp))
                            } else if (key == "DEL") {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            if (enteredPin.isNotEmpty()) {
                                                enteredPin = enteredPin.dropLast(1)
                                                errorMessage = null
                                            }
                                        }
                                        .testTag("pin_key_delete"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .clickable {
                                            if (enteredPin.length < 4) {
                                                val next = enteredPin + key
                                                enteredPin = next
                                                errorMessage = null

                                                if (next.length == 4) {
                                                    scope.launch {
                                                        val storedHash = preferencesRepository.passcodeHash.first()
                                                        val isValid = preferencesRepository.verifyPin(next, storedHash)
                                                        if (isValid) {
                                                            onUnlocked()
                                                        } else {
                                                            errorMessage = "Incorrect passcode. Try again."
                                                            enteredPin = ""
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        .testTag("pin_key_$key"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = key,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = { showForgotDialog = true },
                modifier = Modifier.testTag("btn_forgot_passcode")
            ) {
                Text(
                    text = "Forgot Passcode? / পাসওয়ার্ড ভুলে গেছেন?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (showForgotDialog) {
            AlertDialog(
                onDismissRequest = { showForgotDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.LockReset,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(
                        text = "Reset Forgotten Passcode?",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "পাসওয়ার্ড ভুলে গেছেন? ফরগেট বাটনে চাপ দিলে সাথে সাথে পাসওয়ার্ড রিসেট হয়ে যাবে এবং আপনি সরাসরি অ্যাপে প্রবেশ করতে পারবেন।",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "নিরাপত্তার সুবিধার্থে ৫ মিনিট পর আপনি নতুন পাসওয়ার্ড সেট করতে পারবেন।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                preferencesRepository.resetForgottenPasscode()
                                showForgotDialog = false
                                onUnlocked()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("btn_confirm_forgot_passcode")
                    ) {
                        Text("Reset & Open / রিসেট ও প্রবেশ করুন")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForgotDialog = false }) {
                        Text("Cancel / বাতিল")
                    }
                }
            )
        }
    }
}
