package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AskOutcome
import com.example.ui.theme.OutcomeAcceptedColor
import com.example.ui.theme.OutcomePendingColor
import com.example.ui.theme.OutcomeRejectedColor

@Composable
fun OutcomeBadge(
    outcome: AskOutcome,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val (bgColor, contentColor, icon: ImageVector, text) = when (outcome) {
        AskOutcome.REJECTED -> Quadruple(
            OutcomeRejectedColor.copy(alpha = 0.15f),
            OutcomeRejectedColor,
            Icons.Default.Shield,
            "Rejected"
        )
        AskOutcome.ACCEPTED -> Quadruple(
            OutcomeAcceptedColor.copy(alpha = 0.15f),
            OutcomeAcceptedColor,
            Icons.Default.CheckCircle,
            "Accepted"
        )
        AskOutcome.PENDING -> Quadruple(
            OutcomePendingColor.copy(alpha = 0.12f),
            OutcomePendingColor,
            Icons.Default.HourglassEmpty,
            "Pending"
        )
    }

    val clickableModifier = if (onClick != null) {
        modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    } else {
        modifier.clip(RoundedCornerShape(12.dp))
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = clickableModifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
            Box(modifier = Modifier.size(4.dp))
            Text(
                text = text,
                color = contentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun CategoryChip(
    category: String,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.value == 0xFF1B0B14UL
    val activeBg = MaterialTheme.colorScheme.primaryContainer
    val activeText = MaterialTheme.colorScheme.onPrimaryContainer
    val inactiveBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFFFEEF3)
    val inactiveText = MaterialTheme.colorScheme.onSurfaceVariant

    val shape = RoundedCornerShape(16.dp)
    val clickMod = if (onClick != null) modifier.clip(shape).clickable { onClick() } else modifier

    Box(
        modifier = clickMod
            .background(if (isSelected) activeBg else inactiveBg, shape)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = shape
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = category,
            color = if (isSelected) activeText else inactiveText,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
