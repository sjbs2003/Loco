package com.example.loco

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.loco.ui.screens.AuthScreen
import com.example.loco.ui.screens.NoteCreationScreen
import com.example.loco.ui.screens.NoteDetailScreen
import com.example.loco.ui.screens.NoteListScreen
import com.example.loco.viewModel.AuthViewModel

enum class NoteScreen(val route: String) {
    Login("login"),
    NoteList("noteList"),
    NoteDetail("noteDetail/{noteId}"),
    NoteCreate("noteCreate")
}

@Composable
fun NoteApp(initialNoteId: Long? = null) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)

    LaunchedEffect(initialNoteId) {
        // If app was opened from notification, navigate to the specific note
        initialNoteId?.let { noteId ->
            navController.navigate(NoteScreen.NoteDetail.route.replace("{noteId}", noteId.toString()))
        }
    }

    NavHost(
        navController = navController,
        startDestination = NoteScreen.Login.route
    ) {
        composable(route = NoteScreen.Login.route) {
            AuthScreen(
                onLoginSuccess = {
                    navController.navigate(NoteScreen.NoteList.route) {
                        popUpTo(NoteScreen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = NoteScreen.NoteList.route) {
            NoteListScreen(
                onNoteClick = { nodeId ->
                    navController.navigate(NoteScreen.NoteDetail.route.replace("{noteId}", nodeId.toString()))
                },
                onCreateNoteClick = {
                    navController.navigate(NoteScreen.NoteCreate.route)
                },
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(NoteScreen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = NoteScreen.NoteDetail.route,
            arguments = listOf(navArgument("noteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: return@composable
            NoteDetailScreen(
                noteId = noteId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = NoteScreen.NoteCreate.route) {
            NoteCreationScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}