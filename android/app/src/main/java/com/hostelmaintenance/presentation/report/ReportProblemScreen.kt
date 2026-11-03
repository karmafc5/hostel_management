package com.hostelmaintenance.presentation.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hostelmaintenance.data.report.CreateReportRequestDto
import com.hostelmaintenance.presentation.components.HostelFieldLabel
import com.hostelmaintenance.presentation.components.hostelTextFieldColors
import com.hostelmaintenance.presentation.components.hostelTextFieldTextStyle

private val categories = listOf(
    "Plumbing problems",
    "Electrical problems",
    "Broken furniture",
    "Water supply problems",
    "Bathroom problems",
    "Door and window problems",
    "Internet and Wi-Fi problems",
    "Cleaning and sanitation",
    "Structural damage",
    "Other problems"
)

private val priorities = listOf("LOW", "NORMAL", "HIGH", "URGENT")

@Composable
fun ReportProblemScreen(
    contentPadding: PaddingValues,
    state: ReportsUiState,
    onSubmit: (CreateReportRequestDto) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var hostel by rememberSaveable { mutableStateOf("") }
    var room by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var priority by rememberSaveable { mutableStateOf("NORMAL") }
    var showValidation by rememberSaveable { mutableStateOf(false) }
    var categoryMenuExpanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 20.dp,
                top = contentPadding.calculateTopPadding() + 12.dp,
                end = 20.dp,
                bottom = contentPadding.calculateBottomPadding() + 20.dp
            )
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Tell us what needs attention. Required fields are marked *.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        state.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        OutlinedTextField(
            value = title,
            onValueChange = { title = it.take(120) },
            modifier = Modifier.fillMaxWidth(),
            label = { HostelFieldLabel("Complaint title *") },
            textStyle = hostelTextFieldTextStyle(),
            colors = hostelTextFieldColors(),
            singleLine = true,
            isError = showValidation && title.trim().length < 3
        )
        Column {
            OutlinedButton(
                onClick = { categoryMenuExpanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(if (category.isBlank()) "Select category *" else category)
            }
            DropdownMenu(
                expanded = categoryMenuExpanded,
                onDismissRequest = { categoryMenuExpanded = false }
            ) {
                categories.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            category = option
                            categoryMenuExpanded = false
                        }
                    )
                }
            }
            if (showValidation && category.isBlank()) {
                Text(
                    "Choose a maintenance category.",
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        OutlinedTextField(
            value = description,
            onValueChange = { description = it.take(2000) },
            modifier = Modifier.fillMaxWidth(),
            label = { HostelFieldLabel("Description *") },
            textStyle = hostelTextFieldTextStyle(),
            colors = hostelTextFieldColors(),
            minLines = 4,
            isError = showValidation && description.trim().length < 10
        )
        Text("Priority", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            priorities.forEach { option ->
                FilterChip(
                    selected = priority == option,
                    onClick = { priority = option },
                    label = { Text(option.lowercase().replaceFirstChar(Char::uppercase)) }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = hostel,
                onValueChange = { hostel = it.take(100) },
                modifier = Modifier.weight(1f),
                label = { HostelFieldLabel("Hostel *") },
                textStyle = hostelTextFieldTextStyle(),
                colors = hostelTextFieldColors(),
                singleLine = true,
                isError = showValidation && hostel.isBlank()
            )
            OutlinedTextField(
                value = room,
                onValueChange = { room = it.take(30) },
                modifier = Modifier.weight(1f),
                label = { HostelFieldLabel("Room *") },
                textStyle = hostelTextFieldTextStyle(),
                colors = hostelTextFieldColors(),
                singleLine = true,
                isError = showValidation && room.isBlank()
            )
        }
        Button(
            onClick = {
                showValidation = true
                if (
                    title.trim().length >= 3 &&
                    category.isNotBlank() &&
                    description.trim().length >= 10 &&
                    hostel.isNotBlank() &&
                    room.isNotBlank()
                ) {
                    onSubmit(
                        CreateReportRequestDto(
                            title = title.trim(),
                            category = category,
                            description = description.trim(),
                            hostel = hostel.trim(),
                            room = room.trim(),
                            priority = priority
                        )
                    )
                }
            },
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator()
            } else {
                Text("Submit report")
            }
        }
    }
}
