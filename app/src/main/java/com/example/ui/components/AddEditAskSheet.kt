package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AskEntity
import com.example.data.model.AskCategory
import com.example.data.model.AskOutcome
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditAskSheet(
    askToEdit: AskEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        category: String,
        dateEpochMillis: Long,
        notes: String,
        outcome: AskOutcome,
        takeaway: String
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf(askToEdit?.title ?: "") }
    var category by remember { mutableStateOf(askToEdit?.category ?: AskCategory.DEFAULT_CATEGORIES[0]) }
    var isCustomCategory by remember { mutableStateOf(askToEdit != null && !AskCategory.DEFAULT_CATEGORIES.contains(askToEdit.category)) }
    var customCategoryText by remember { mutableStateOf(if (isCustomCategory) askToEdit?.category ?: "" else "") }

    var dateEpochMillis by remember { mutableLongStateOf(askToEdit?.dateEpochMillis ?: System.currentTimeMillis()) }
    var notes by remember { mutableStateOf(askToEdit?.notes ?: "") }
    var takeaway by remember { mutableStateOf(askToEdit?.takeaway ?: "") }
    var outcome by remember {
        mutableStateOf(
            if (askToEdit != null) AskOutcome.fromString(askToEdit.outcome) else AskOutcome.REJECTED
        )
    }

    var titleError by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = Color.Black.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (askToEdit != null) "Edit Ask" else "Log a New Ask",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Every ask expands your courage zone",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("dismiss_sheet_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // What was the ask?
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (titleError && it.isNotBlank()) titleError = false
                },
                label = { Text("What did you ask for?") },
                placeholder = { Text("e.g., Asked for a 20% discount on coffee") },
                isError = titleError,
                supportingText = {
                    if (titleError) {
                        Text("Please describe what you asked for.", color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ask_title_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Outcome Selection (Pink / Berry Palette)
            Text(
                text = "What was the outcome?",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Rejected
                OutcomeSelectButton(
                    title = "Rejected",
                    subtitle = "+1 Courage",
                    icon = Icons.Default.Shield,
                    isSelected = outcome == AskOutcome.REJECTED,
                    activeColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    onClick = { outcome = AskOutcome.REJECTED }
                )

                // Accepted
                OutcomeSelectButton(
                    title = "Accepted",
                    subtitle = "Sweet Win",
                    icon = Icons.Default.Favorite,
                    isSelected = outcome == AskOutcome.ACCEPTED,
                    activeColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f),
                    onClick = { outcome = AskOutcome.ACCEPTED }
                )

                // Pending
                OutcomeSelectButton(
                    title = "Pending",
                    subtitle = "Awaiting",
                    icon = Icons.Default.HourglassEmpty,
                    isSelected = outcome == AskOutcome.PENDING,
                    activeColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f),
                    onClick = { outcome = AskOutcome.PENDING }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selection
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AskCategory.DEFAULT_CATEGORIES.forEach { cat ->
                    val isSelected = !isCustomCategory && category == cat
                    CategoryChip(
                        category = cat,
                        isSelected = isSelected,
                        onClick = {
                            isCustomCategory = false
                            category = cat
                        }
                    )
                }

                CategoryChip(
                    category = "+ Custom",
                    isSelected = isCustomCategory,
                    onClick = {
                        isCustomCategory = true
                    }
                )
            }

            if (isCustomCategory) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customCategoryText,
                    onValueChange = { customCategoryText = it },
                    label = { Text("Custom Category Name") },
                    placeholder = { Text("e.g., Speaking & Podcasting") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date Selection
            Text(
                text = "Date of Ask",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val now = System.currentTimeMillis()
                val oneDay = 86400000L

                // Quick buttons: Today, Yesterday
                OutlinedButton(
                    onClick = { dateEpochMillis = now },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (Math.abs(dateEpochMillis - now) < oneDay / 2) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Today", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { dateEpochMillis = now - oneDay },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (Math.abs(dateEpochMillis - (now - oneDay)) < oneDay / 2) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Yesterday", fontSize = 12.sp)
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Date",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = dateFormat.format(Date(dateEpochMillis)),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes / Context
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Details & Context (Optional)") },
                placeholder = { Text("Who was it? How did you approach them?") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Takeaway & Feeling
            OutlinedTextField(
                value = takeaway,
                onValueChange = { takeaway = it },
                label = { Text("Takeaway / How it felt (Optional)") },
                placeholder = { Text("What did you learn about yourself? How quick did fear fade?") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save CTA Button
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        return@Button
                    }
                    val finalCategory = if (isCustomCategory && customCategoryText.isNotBlank()) {
                        customCategoryText.trim()
                    } else {
                        category
                    }
                    onSave(
                        title.trim(),
                        finalCategory,
                        dateEpochMillis,
                        notes.trim(),
                        outcome,
                        takeaway.trim()
                    )
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_ask_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (askToEdit != null) "Update Ask" else "Save Ask to My 1,000",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun OutcomeSelectButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) activeColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    val borderColor = if (isSelected) activeColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .background(bgColor)
            .border(width = if (isSelected) 2.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(14.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = if (isSelected) activeColor.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
