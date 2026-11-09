package com.anilog.omor.dev.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anilog.omor.dev.domain.model.AnimeStatus
import com.anilog.omor.dev.ui.theme.StatusCompleted
import com.anilog.omor.dev.ui.theme.StatusDropped
import com.anilog.omor.dev.ui.theme.StatusOnHold
import com.anilog.omor.dev.ui.theme.StatusPlanToWatch
import com.anilog.omor.dev.ui.theme.StatusWatching

@Composable
fun StatusBadge(
    status: AnimeStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status) {
        AnimeStatus.PLAN_TO_WATCH -> StatusPlanToWatch.copy(alpha = 0.18f) to StatusPlanToWatch
        AnimeStatus.WATCHING -> StatusWatching.copy(alpha = 0.18f) to StatusWatching
        AnimeStatus.COMPLETED -> StatusCompleted.copy(alpha = 0.18f) to StatusCompleted
        AnimeStatus.ON_HOLD -> StatusOnHold.copy(alpha = 0.18f) to StatusOnHold
        AnimeStatus.DROPPED -> StatusDropped.copy(alpha = 0.18f) to StatusDropped
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.displayName,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp
        )
    }
}
