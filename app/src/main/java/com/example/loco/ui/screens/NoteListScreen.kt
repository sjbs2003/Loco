package com.example.loco.ui.screens

import android.net.Uri
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.loco.R
import com.example.loco.model.NoteEntity
import com.example.loco.ui.AppViewModelProvider
import com.example.loco.viewModel.NoteListViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    onNoteClick: (Long) -> Unit,
    onCreateNoteClick: () -> Unit,
    onSignOut: () -> Unit
) {
    val viewModel: NoteListViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val notes by viewModel.notes.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val categories = listOf("All", "Work", "Reading", "Important")
    val searchQuery by viewModel.searchQuery.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val darkGray = Color(0xFF1E1E1E)
    val lightGray = Color(0xFF2A2A2A)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = darkGray,
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Profile Section
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            UserProfileImage(
                                viewModel = viewModel,
                                modifier = Modifier.size(50.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Notes App",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = lightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // New Note Button
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Add, contentDescription = "New Note", tint = Color.White) },
                        label = { Text("New Note", color = Color.White) },
                        selected = false,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                onCreateNoteClick()
                            }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = darkGray
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Notes Section Title
                    Text(
                        text = "Your Notes",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
                    )

                    // Scrollable Notes List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    ) {
                        items(
                            items = notes,
                            key = { note -> note.id }
                        ) { note ->
                            NavigationDrawerItem(
                                icon = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_note),
                                        contentDescription = "Note",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = note.title.ifEmpty { "Untitled Note" },
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                selected = false,
                                onClick = {
                                    scope.launch {
                                        drawerState.close()
                                        onNoteClick(note.id)
                                    }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    unselectedContainerColor = darkGray
                                ),
                                modifier = Modifier.padding(horizontal = 0.dp)
                            )
                        }
                    }

                    // Sign Out Button at the bottom
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_logout),
                                contentDescription = "Sign Out",
                                tint = Color.White
                            )
                        },
                        label = { Text("Sign Out", color = Color.White) },
                        selected = false,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                onSignOut()
                            }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = darkGray
                        ),
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 24.dp)
                    )
                }
            }
        }
    ){
        Scaffold(
            containerColor = darkGray,
            topBar = {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("Search your notes", color = Color.Gray) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { /* Menu icon click disabled */ }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        UserProfileImage(
                            viewModel = viewModel
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = darkGray
                    )
                )
            },
            floatingActionButton = {
                Box(
                    modifier = Modifier
                        .padding(bottom = 20.dp, end = 16.dp)
                ) {
                    FloatingActionButton(
                        onClick = { onCreateNoteClick() },
                        containerColor = Color.DarkGray,
                        elevation = FloatingActionButtonDefaults.elevation(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Note",
                            tint = Color.White
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .background(darkGray)
            ) {
                // Category chips
                ScrollableTabRow(
                    selectedTabIndex = categories.indexOf(selectedCategory),
                    edgePadding = 0.dp,
                    modifier = Modifier.padding(vertical = 8.dp),
                    containerColor = darkGray,
                    contentColor = Color.White
                ) {
                    categories.forEach { category ->
                        Tab(
                            selected = category == selectedCategory,
                            onClick = { viewModel.updateSelectedCategory(category) },
                            text = {
                                Text(
                                    category,
                                    color = if (category == selectedCategory) Color.White else Color.Gray
                                )
                            },
                            modifier = Modifier
                                .padding(horizontal = 4.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (category == selectedCategory)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        lightGray
                                )
                        )
                    }
                }

                // Grid view for notes
                if (notes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No notes yet. Click + to create one!",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        verticalItemSpacing = 16.dp,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        items(notes) { note ->
                            NoteCard(note = note, onClick = { onNoteClick(note.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserProfileImage(
    viewModel: NoteListViewModel,
    modifier: Modifier = Modifier
) {
    val userPhotoUrl by viewModel.userprofilePic.collectAsState()

    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
    ) {
        AsyncImage(
            model = userPhotoUrl ?: R.drawable.default_image,
            contentDescription = "Profile picture",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            fallback = painterResource(R.drawable.default_image),
            error = painterResource(R.drawable.default_image),
            placeholder = painterResource(R.drawable.default_image)
        )
    }
}

@Composable
fun NoteCard(note: NoteEntity, onClick: () -> Unit) {
    val backgroundColor = remember {
        listOf(
            Color(0xFFFFF9C4), // Light Yellow
            Color(0xFFE1BEE7), // Light Purple
            Color(0xFFBBDEFB), // Light Blue
            Color(0xFFC8E6C9), // Light Green
            Color(0xFFFFCCBC)  // Light Orange
        ).random()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))

            // display the image if exist
            note.imageUri?.let { uri->
                Image(
                    painter = rememberAsyncImagePainter(
                        model = if (uri.startsWith("file://")) {
                            Uri.parse(uri)
                        } else{
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
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Category: ${note.category}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray
            )
        }
    }
}