package com.example.loco.ui.screens.home

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.loco.R
import com.example.loco.data.NoteEntity


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    notes: List<NoteEntity>,
    onNoteClick: (NoteEntity) -> Unit,
    onAddNoteClick: () -> Unit // Add this parameter
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.notes)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddNoteClick() }) { // Call onAddNoteClick
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    ) {padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            items(notes){ note ->
                HomeListItem(note = note, onClick = { onNoteClick(note) })
            }
        }
    }
}



@Composable
fun HomeListItem(note: NoteEntity, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = note.title, style = MaterialTheme.typography.headlineMedium)
            Text(text = note.content, maxLines = 1, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview
@Composable
private fun NoteListScreenPreview() {
    val notes = listOf(
        NoteEntity(1, "Title 1", "Content 1"),
        NoteEntity(2, "Title 2", "Content 2")
    )
    HomeScreen(notes = notes, onNoteClick = {}, onAddNoteClick = {})
}