package com.anilog.omor.dev.ui.reviewcard

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anilog.omor.dev.R
import com.anilog.omor.dev.domain.model.CardAspectRatio
import com.anilog.omor.dev.domain.model.CardTemplate
import com.anilog.omor.dev.util.RatingFormatHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewCardGeneratorScreen(
    animeId: Long,
    viewModel: ReviewCardViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(animeId) {
        viewModel.loadAnime(context)
    }

    val previewRatio = when (uiState.customization.aspectRatio) {
        CardAspectRatio.PORTRAIT -> 4f / 5f
        CardAspectRatio.SQUARE -> 1f
        CardAspectRatio.LANDSCAPE -> 16f / 9f
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.action_sharing_studio),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.renderPreview(context) },
                        modifier = Modifier.testTag("btn_refresh_preview")
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Regenerate Preview")
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
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Card Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(previewRatio)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .testTag("review_card_preview_box"),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isRendering) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Rendering high-res card…", style = MaterialTheme.typography.bodySmall)
                    }
                } else if (uiState.previewBitmap != null) {
                    Image(
                        bitmap = uiState.previewBitmap!!.asImageBitmap(),
                        contentDescription = "Generated anime review card preview",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("Preparing card…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Aspect Ratio Selector (Portrait, Square, Landscape)
            Column {
                Text(
                    text = "Card Format / আকার",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CardAspectRatio.entries.forEach { ratio ->
                        FilterChip(
                            selected = uiState.customization.aspectRatio == ratio,
                            onClick = { viewModel.selectAspectRatio(ratio, context) },
                            label = { Text(ratio.displayName) },
                            modifier = Modifier.testTag("aspect_chip_${ratio.name.lowercase()}")
                        )
                    }
                }
            }

            // Template Selector (9 Designs)
            Column {
                Text(
                    text = "Select Design Theme / থিম নির্বাচন",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CardTemplate.entries.forEach { template ->
                        FilterChip(
                            selected = uiState.customization.template == template,
                            onClick = { viewModel.selectTemplate(template, context) },
                            label = { Text(template.displayName) },
                            modifier = Modifier.testTag("template_chip_${template.name.lowercase()}")
                        )
                    }
                }
            }

            // Custom Excerpt Adjustment
            OutlinedTextField(
                value = uiState.customization.customExcerpt ?: "",
                onValueChange = { viewModel.onExcerptChanged(it, context) },
                label = { Text("Shared Review Excerpt / প্রদর্শিত রিভিউ") },
                placeholder = { Text("Adjust the excerpt shown on the card without altering your saved review…") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_card_excerpt")
            )

            // Optional Field Toggles
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Information Visibility / তথ্যের প্রদর্শন",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cover Artwork / কভার ছবি", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = uiState.customization.showCover,
                        onCheckedChange = { viewModel.toggleCover(context) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Personal Rating / ব্যক্তিগত রেটিং", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = uiState.customization.showRating,
                        onCheckedChange = { viewModel.toggleRating(context) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Review Excerpt / রিভিউ টেক্সট", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = uiState.customization.showReview,
                        onCheckedChange = { viewModel.toggleReview(context) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Rafael Rank Badge / রাফায়েল র‍্যাঙ্ক ব্যাজ", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = uiState.customization.showRafaelRank,
                        onCheckedChange = { viewModel.toggleRafaelRank(context) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Finish Date / সমাপ্তির তারিখ", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = uiState.customization.showDate,
                        onCheckedChange = { viewModel.toggleIncludeDate(context) }
                    )
                }
            }

            // Share & Export Actions
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Share Image via Sharesheet
                Button(
                    onClick = {
                        val uri = uiState.cachedImageUri
                        if (uri != null) {
                            val anime = uiState.anime
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/png"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                val score = anime?.personalRating?.let { "★ ${RatingFormatHelper.formatRating(it)}/10" } ?: ""
                                putExtra(Intent.EXTRA_TEXT, "${anime?.title ?: "Anime"} $score — AniLog Review")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Review Card Image"))
                        } else {
                            Toast.makeText(context, "Rendering card, please wait…", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_share_card_image")
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.share_image))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Save to local storage
                    FilledTonalButton(
                        onClick = {
                            viewModel.saveCardPermanently(context) { file ->
                                Toast.makeText(context, "Saved: ${file.name}", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_save_card_storage")
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.save_image))
                    }

                    // Share review text
                    OutlinedButton(
                        onClick = {
                            val anime = uiState.anime
                            if (anime != null) {
                                val textIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    val score = anime.personalRating?.let { "★ ${RatingFormatHelper.formatRating(it)}/10" } ?: "Unrated"
                                    val text = "${anime.title} — $score\n\n“${uiState.customization.customExcerpt}”\n\nShared via AniLog Personal Anime Library"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(textIntent, "Share Review Text"))
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_share_review_caption")
                    ) {
                        Icon(Icons.Filled.TextFields, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.share_text))
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
