package com.example.loco

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.loco.ui.screens.AuthScreen
import com.example.loco.ui.screens.SignUp
import com.example.loco.ui.screens.NoteCreationScreen
import com.example.loco.ui.screens.NoteDetailScreen
import com.example.loco.ui.screens.NoteListScreen
import com.example.loco.viewModel.AuthState
import com.example.loco.viewModel.AuthViewModel
import com.google.firebase.firestore.FirebaseFirestore

enum class NoteScreen(val route: String) {
    Login("login"),
    SignUp("signup"),
    NoteList("noteList"),
    NoteDetail("noteDetail/{noteId}"),
    NoteCreate("noteCreate/{noteId}")
}

@Composable
fun NoteApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val firestore = FirebaseFirestore.getInstance()
    val authState by authViewModel.authState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = when (authState) {
            is AuthState.Authenticated -> NoteScreen.NoteList.route
            else -> NoteScreen.Login.route
        }
    ) {
        composable(route = NoteScreen.Login.route) {
            AuthScreen(
                onLoginSuccess = {
                    navController.navigate(NoteScreen.NoteList.route) {
                        popUpTo(NoteScreen.Login.route) { inclusive = true }
                    }
                },
                onSignUpClick = {
                    navController.navigate(NoteScreen.SignUp.route)
                }
            )
        }

        composable(route = NoteScreen.SignUp.route) {
            SignUp(
                onSignUpSuccess = {
                    navController.navigate(NoteScreen.NoteList.route) {
                        popUpTo(NoteScreen.SignUp.route) { inclusive = true }
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