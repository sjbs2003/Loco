package com.example.loco.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.loco.R
import com.example.loco.AppViewModelProvider
import com.example.loco.viewModel.NoteDetailViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: Long,
    onBackClick: () -> Unit
) {
    val viewModel: NoteDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val noteState by viewModel.noteState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    fun copyImageToAppStorage(context: Context, uri: Uri): Uri {
        val contentResolver = context.contentResolver
        val fileName = "note_image_${System.currentTimeMillis()}.jpg"
        val imagesDir = File(context.filesDir, "note_images").apply {
            if (!exists()) mkdirs()
        }
        val file = File(imagesDir, fileName)

        contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return Uri.fromFile(file)
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val permanentUri = copyImageToAppStorage(context, it)
            viewModel.updateImage(permanentUri.toString())
        }
    }

    var currentSpeechField by remember { mutableStateOf<SpeechField?>(null) }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
            when (currentSpeechField) {
                SpeechField.TITLE -> viewModel.updateTitle((noteState?.title ?: "") + spokenText)
                SpeechField.CONTENT -> viewModel.updateContent((noteState?.content ?: "") + spokenText)
                null -> { /* Do nothing */ }
            }
        }
        currentSpeechField = null
    }

    fun startSpeechRecognition(field: SpeechField) {
        currentSpeechField = field
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        }
        speechRecognizerLauncher.launch(intent)
    }

    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
    }

    Scaffold(
        containerColor = colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
                    IconButton(
                        onClick = {
                            noteState?.let { shareNoteContent(context, it.title, it.content) }
                        }
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share",
                            tint = colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { viewModel.saveNote() }) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Save",
                            tint = colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = {
                            viewModel.deleteNote()
                            onBackClick()
                        }
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.surface,
                    navigationIconContentColor = colorScheme.onSurface,
                    actionIconContentColor = colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colorScheme.surface)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = noteState?.title ?: "",
                    onValueChange = { viewModel.updateTitle(it) },
                    textStyle = TextStyle(
                        color = colorScheme.onSurface,
                        fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                        fontWeight = FontWeight.Bold
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

            noteState?.imageUri?.let { uri ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = if (uri.startsWith("file://")) {
                                Uri.parse(uri)
                            } else {
                                uri
                            }
                        ),
                        contentDescription = "Note Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp)),
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
                BasicTextField(
                    value = noteState?.content ?: "",
                    onValueChange = { viewModel.updateContent(it) },
                    textStyle = TextStyle(
                        color = colorScheme.onSurface.copy(alpha = 0.8f),
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
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
    }
}

fun shareNoteContent(context: Context, title: String, content: String) {
    val sharedText = "$title\n\n$content"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TITLE, title)
        putExtra(Intent.EXTRA_TEXT, sharedText)
    }
    context.startActivity(
        Intent.createChooser(
            intent,
            context.getString(R.string.share_note)
        )
    )
}