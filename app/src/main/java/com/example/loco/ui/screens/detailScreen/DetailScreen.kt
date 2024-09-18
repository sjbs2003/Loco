package com.example.loco.ui.screens.detailScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.content.Context
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.example.loco.R
import com.example.loco.data.NoteEntity



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    note: NoteEntity,
    onEditClick: () -> Unit,
    onDeleteClick:  () -> Unit,

    ) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(note.title) },
                actions = {
                    IconButton(onClick = { shareNoteContent(context, note.title, note.content) } ) {
                        Icon(Icons.Default.Share, contentDescription = "Share Note")
                    }
                    IconButton(onClick = { onEditClick() }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Note")
                    }
                    IconButton(onClick = { onDeleteClick() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Note")
                    }
                }
            )
        }
    ) {padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(text = note.content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}


fun shareNoteContent(context: Context, title: String, content: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TITLE, title)
        putExtra(Intent.EXTRA_TEXT, content)
    }
    context.startActivity(
        Intent.createChooser(
            intent,
            context.getString(R.string.share_notes)
        )
    )
}



@Preview
@Composable
fun NoteDetailScreenPreview() {
    val note = NoteEntity(id = 1, title = "Sample Note", content = "This is the content of the sample note.")
    NoteDetailScreen(note = note, onEditClick = {}, onDeleteClick = {} )
}

