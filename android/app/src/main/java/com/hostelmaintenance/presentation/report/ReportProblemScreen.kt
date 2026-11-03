package com.hostelmaintenance.presentation.report

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

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

private val priorities = listOf("Low", "Normal", "High", "Urgent")

@Composable
fun ReportProblemScreen(
    contentPadding: PaddingValues
) {
    val context = LocalContext.current
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var hostel by rememberSaveable { mutableStateOf("") }
    var room by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var priority by rememberSaveable { mutableStateOf("Normal") }
    var selectedImage by rememberSaveable { mutableStateOf<Uri?>(null) }
    var showValidation by rememberSaveable { mutableStateOf(false) }
    var categoryMenuExpanded by rememberSaveable { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImage = uri
    }

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
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Complaint title *") },
            singleLine = true,
            isError = showValidation && title.isBlank()
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
            onValueChange = { description = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Description *") },
            minLines = 4,
            isError = showValidation && description.isBlank()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            priorities.forEach { option ->
                FilterChip(
                    selected = priority == option,
                    onClick = { priority = option },
                    label = { Text(option) }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = hostel,
                onValueChange = { hostel = it },
                modifier = Modifier.weight(1f),
                label = { Text("Hostel *") },
                singleLine = true,
                isError = showValidation && hostel.isBlank()
            )
            OutlinedTextField(
                value = room,
                onValueChange = { room = it },
                modifier = Modifier.weight(1f),
                label = { Text("Room *") },
                singleLine = true,
                isError = showValidation && room.isBlank()
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { imagePicker.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.AddAPhoto, contentDescription = null)
                    Text(if (selectedImage == null) "  Add a photo" else "  Change photo")
                }
                selectedImage?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Selected maintenance issue",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        Button(
            onClick = {
                showValidation = true
                if (
                    title.isNotBlank() &&
                    category.isNotBlank() &&
                    description.isNotBlank() &&
                    hostel.isNotBlank() &&
                    room.isNotBlank()
                ) {
                    android.widget.Toast.makeText(
                        context,
                        "Your report was not submitted because the service is not connected yet.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            Text("Submit report")
        }
    }
}
