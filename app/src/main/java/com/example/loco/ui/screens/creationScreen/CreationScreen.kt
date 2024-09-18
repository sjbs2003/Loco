package com.example.loco.ui.screens.creationScreen


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.loco.R
import com.example.loco.data.NoteEntity


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteCreationScreen(
    note: NoteEntity?,
    onSaveClick: (NoteEntity) -> Unit
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(if (note == null)
                    stringResource(R.string.create_notes)
                else stringResource(R.string.edit_notes)
                ) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onSaveClick(NoteEntity(id = note?.id ?: 0, title = title, content = content)) }) {
                Icon(Icons.Default.Check, contentDescription = "Save Note")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.title)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text(stringResource(R.string.content)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(bottom = 16.dp)
                    .defaultMinSize(minHeight = 150.dp)
            )
        }
    }
}

@Preview
@Composable
fun NoteCreationScreenPreview() {
    NoteCreationScreen(note = null, onSaveClick = {})
}
