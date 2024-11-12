package com.example.loco.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.loco.R
import com.example.loco.AppViewModelProvider
import com.example.loco.ui.theme.AppFonts
import com.example.loco.viewModel.NoteCreationViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


enum class SpeechField {
    TITLE, CONTENT
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteCreationScreen(
    onBackClick: () -> Unit
) {
    val viewModel: NoteCreationViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val fontSettings by viewModel.fontSettings.collectAsState()
    var showFontDrawer by remember { mutableStateOf(false) }
    val noteState by viewModel.noteState.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val categories = listOf("All", "Work", "Reading", "Important")
    val colorScheme = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val reminderState by viewModel.reminderState.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Pass the URI directly to the ViewModel
            viewModel.updateImage(it.toString())
        }
    }

    // State to keep track of which field is currently selected for speech input
    var currentSpeechField by remember { mutableStateOf<SpeechField?>(null) }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK){
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
            when (currentSpeechField) {
                SpeechField.TITLE -> viewModel.updateTitle(noteState.title + spokenText)
                SpeechField.CONTENT -> viewModel.updateContent(noteState.content + spokenText)
                null -> { /* Do nothing */}
            }
        }
        currentSpeechField = null
    }

    // Function to start speech recognition
    fun startSpeechRecognition(field: SpeechField) {
        currentSpeechField = field
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        }
        speechRecognizerLauncher.launch(intent)
    }

    ModalNavigationDrawer(
        drawerContent = {
            if (showFontDrawer) {
                FontSelectionDrawer(
                    onFontSelected = { font, isTitle ->
                        if (isTitle) {
                            viewModel.updateTitleFont(font)
                        } else {
                            viewModel.updateContentFont(font)
                        }
                    },
                    onDismiss = { showFontDrawer = false },
                    currentTitleFont = fontSettings.titleFont,
                    currentContentFont = fontSettings.contentFont
                )
            }
        },
        drawerState = rememberDrawerState(
            initialValue = if (showFontDrawer) DrawerValue.Open else DrawerValue.Closed
        ),
        gesturesEnabled = showFontDrawer
    ) {
        Scaffold(
            containerColor = colorScheme.surface,
            topBar = {
                Column {
                    TopAppBar(
                        title = { },
                        navigationIcon = {
                            IconButton(onClick = {
                                viewModel.saveNote()
                                onBackClick()
                            }) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Done",
                                    tint = colorScheme.onSurface
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_images),
                                    contentDescription = "Add Image",
                                    tint = colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { showReminderDialog = true }) {
                                Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Set Reminder",
                                tint = if (reminderState.isReminderSet) colorScheme.primary else colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { showFontDrawer = true }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "Font Settings",
                                    tint = colorScheme.onSurface
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = colorScheme.surface
                        )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_folder),
                            contentDescription = "Category",
                            tint = colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(selectedCategory, color = colorScheme.onSurface)
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Select Category",
                                tint = colorScheme.onSurface
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(colorScheme.surface)
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category, color = colorScheme.onSurface) },
                                    onClick = {
                                        viewModel.updateCategory(category)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .background(colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = noteState.title,
                        onValueChange = { viewModel.updateTitle(it) },
                        placeholder = {
                            Text(
                                "Title",
                                color = colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontSettings.titleFont
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = colorScheme.onSurface,
                            unfocusedTextColor = colorScheme.onSurface,
                            focusedContainerColor = colorScheme.surface,
                            unfocusedContainerColor = colorScheme.surface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )
                    IconButton(onClick = { startSpeechRecognition(SpeechField.TITLE) }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_mic),
                            contentDescription = "Speech to Text for Title",
                            tint = colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Image view
                noteState.imageUri?.let { uri ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = if (uri.contains("|")) {
                                    Uri.parse(uri.split("|")[1]) // Use local path
                                } else {
                                    Uri.parse(uri)
                                }
                            ),
                            contentDescription = "Note Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalAlignment = Alignment.Top
                ) {
                    TextField(
                        value = noteState.content,
                        onValueChange = { viewModel.updateContent(it) },
                        placeholder = {
                            Text(
                                "Note content",
                                color = colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 16.sp,
                            fontFamily = fontSettings.contentFont
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = colorScheme.onSurface,
                            unfocusedTextColor = colorScheme.onSurface,
                            focusedContainerColor = colorScheme.surface,
                            unfocusedContainerColor = colorScheme.surface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(end = 8.dp)
                    )
                    IconButton(onClick = { startSpeechRecognition(SpeechField.CONTENT) }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_mic),
                            contentDescription = "Speech to Text for Content",
                            tint = colorScheme.onSurface
                        )
                    }
                }
            }

            if (showReminderDialog) {
                ReminderDialog(
                    onDismiss = { showReminderDialog = false },
                    onSetReminder = { timestamp, date, time ->
                        viewModel.setReminder(context, timestamp, date, time)
                    }
                )
            }
        }
    }
}

@Composable
fun ReminderDialog(
    onDismiss: () -> Unit,
    onSetReminder: (Long, String, String) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val colorScheme = MaterialTheme.colorScheme

    var selectedDate by remember { mutableStateOf("Select Date") }
    var selectedTime by remember { mutableStateOf("Select Time") }
    var isDateSelected by remember { mutableStateOf(false) }
    var isTimeSelected by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Set Reminder",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showDatePicker(context, calendar) { date ->
                                calendar.time = date
                                selectedDate = dateFormatter.format(date)
                                isDateSelected = true
                            }
                        },
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = "Select Date",
                            tint = colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = selectedDate,
                            color = if (isDateSelected)
                                colorScheme.onSurface
                            else
                                colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showTimePicker(context, calendar) { hour, minute ->
                                calendar.set(Calendar.HOUR_OF_DAY, hour)
                                calendar.set(Calendar.MINUTE, minute)
                                selectedTime = timeFormatter.format(calendar.time)
                                isTimeSelected = true
                            }
                        },
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = "Select Time",
                            tint = colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = selectedTime,
                            color = if (isTimeSelected)
                                colorScheme.onSurface
                            else
                                colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            "Cancel",
                            color = colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Button(
                        onClick = {
                            if (isDateSelected && isTimeSelected) {
                                onSetReminder(
                                    calendar.timeInMillis,
                                    selectedDate,
                                    selectedTime
                                )
                                onDismiss()
                            }
                        },
                        enabled = isDateSelected && isTimeSelected,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary,
                            disabledContainerColor = colorScheme.surfaceVariant
                        )
                    ) {
                        Text("Set Reminder")
                    }
                }
            }
        }
    }
}

private fun showDatePicker(
    context: android.content.Context,
    calendar: Calendar,
    onDateSelected: (Date) -> Unit
) {
    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            onDateSelected(calendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = System.currentTimeMillis()
    }
    datePickerDialog.show()
}

private fun showTimePicker(
    context: android.content.Context,
    calendar: Calendar,
    onTimeSelected: (Int, Int) -> Unit
) {
    android.app.TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            onTimeSelected(hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    ).show()
}

@Composable
fun FontSelectionDrawer(
    onFontSelected: (FontFamily, Boolean) -> Unit,
    onDismiss: () -> Unit,
    currentTitleFont: FontFamily,
    currentContentFont: FontFamily
) {
    val colorScheme = MaterialTheme.colorScheme

    ModalDrawerSheet(
        drawerContainerColor = colorScheme.surface,
        modifier = Modifier.width(300.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            Text(
                "Font Settings",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp),
                color = colorScheme.onSurface
            )

            Text(
                "Title Font",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp),
                color = colorScheme.onSurface
            )

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(AppFonts.availableFonts) { (name, font) ->
                    FontSelectionItem(
                        fontName = name,
                        font = font,
                        isSelected = currentTitleFont == font,
                        onClick = { onFontSelected(font, true) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Content Font",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp),
                color = colorScheme.onSurface
            )

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(AppFonts.availableFonts) { (name, font) ->
                    FontSelectionItem(
                        fontName = name,
                        font = font,
                        isSelected = currentContentFont == font,
                        onClick = { onFontSelected(font, false) }
                    )
                }
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 16.dp)
            ) {
                Text("Done", color = colorScheme.primary)
            }
        }
    }
}

@Composable
fun FontSelectionItem(
    fontName: String,
    font: FontFamily,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = fontName,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = font
            ),
            color = colorScheme.onSurface
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = colorScheme.primary
            )
        }
    }
}