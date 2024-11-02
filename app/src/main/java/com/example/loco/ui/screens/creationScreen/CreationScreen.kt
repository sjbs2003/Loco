//package com.example.loco.ui.screens.creationScreen
//
//
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.defaultMinSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.wrapContentHeight
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Check
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.FloatingActionButton
//import androidx.compose.material3.Icon
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TopAppBar
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import com.example.loco.R
//import com.example.loco.data.NoteEntity
//
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun NoteCreationScreen(
//    note: NoteEntity?,
//    onSaveClick: (NoteEntity) -> Unit
//) {
//    var title by remember { mutableStateOf(note?.title ?: "") }
//    var content by remember { mutableStateOf(note?.content ?: "") }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(title = {
//                Text(if (note == null)
//                    stringResource(R.string.create_notes)
//                else stringResource(R.string.edit_notes)
//                ) }
//            )
//        },
//        floatingActionButton = {
//            FloatingActionButton(onClick = { onSaveClick(NoteEntity(id = note?.id ?: 0, title = title, content = content)) }) {
//                Icon(Icons.Default.Check, contentDescription = "Save Note")
//            }
//        }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .padding(padding)
//                .padding(16.dp)
//        ) {
//            OutlinedTextField(
//                value = title,
//                onValueChange = { title = it },
//                label = { Text(stringResource(R.string.title)) },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(bottom = 16.dp)
//            )
//            OutlinedTextField(
//                value = content,
//                onValueChange = { content = it },
//                label = { Text(stringResource(R.string.content)) },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .wrapContentHeight()
//                    .padding(bottom = 16.dp)
//                    .defaultMinSize(minHeight = 150.dp)
//            )
//        }
//    }
//}
//
//@Preview
//@Composable
//fun NoteCreationScreenPreview() {
//    NoteCreationScreen(note = null, onSaveClick = {})
//}
package com.example.noteapp.ui.screens_view

import android.app.Activity
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.loco.ui.screens.AppViewModelProvider
import com.example.loco.R
//import com.example.noteapp.data.model.NoteRepository
//import com.example.noteapp.ui.AppViewModelProvider
import com.example.noteapp.viewModel.NoteCreationViewModel

enum class SpeechField {
    TITLE, CONTENT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteCreationScreen(
    onBackClick:() -> Unit
) {
    val viewModel: NoteCreationViewModel = viewModel(
        factory = AppViewModelProvider.Factory
    )
    val noteState by viewModel.noteState.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val categories = listOf("All","Work","Reading","Important" )
    val darkGray = Color(0xFF1E1E1E)
    var expanded by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateImage(it.toString()) }
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

    Scaffold(
        containerColor = darkGray,
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
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_images),
                                contentDescription = "Add Image",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { /* Handle notifications */ }) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { /* Handle more options */ }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = darkGray
                    ),
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = (painterResource(R.drawable.ic_folder)),
                        contentDescription = "Category",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(selectedCategory, color = Color.White)
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Select Category",
                            tint = Color.White
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded =false },
                        modifier = Modifier.background(darkGray)
                    ) {
                        categories.forEach { category->
                            DropdownMenuItem(
                                text = { Text(category, color = Color.White) },
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
                .background(darkGray)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = noteState.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    placeholder = { Text("Title", color = Color.Gray) },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = darkGray,
                        unfocusedContainerColor = darkGray,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = { startSpeechRecognition(SpeechField.TITLE) }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_mic),
                        contentDescription = "Speech to Text for Title",
                        tint = Color.White
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
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = "Note Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalAlignment = Alignment.Top
            ) {
                TextField(
                    value = noteState.content,
                    onValueChange = { viewModel.updateContent(it) },
                    placeholder = { Text("Note content", color = Color.Gray) },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = darkGray,
                        unfocusedContainerColor = darkGray,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(end = 8.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
                )
                IconButton(onClick = { startSpeechRecognition(SpeechField.CONTENT) }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_mic),
                        contentDescription = "Speech to Text for Content",
                        tint = Color.White
                    )
                }
            }
        }
    }
}