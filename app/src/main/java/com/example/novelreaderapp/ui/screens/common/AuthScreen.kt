package com.example.novelreaderapp.ui.screens.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.novelreaderapp.viewmodel.AuthViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import com.example.novelreaderapp.backendconnection.backmodels.NovelBackend

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    autoNavigate: Boolean = false,
    onAuthSuccess: () -> Unit
) {
    val token by authViewModel.authToken.collectAsState()
    val username by authViewModel.username.collectAsState()
    val error by authViewModel.error.collectAsState()
    val novels by authViewModel.userNovels.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingNovel by remember { mutableStateOf<NovelBackend?>(null) }

    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }

    val currentUserId = authViewModel.userId.collectAsState().value ?: ""


    LaunchedEffect(token, autoNavigate) {
        if (!token.isNullOrEmpty() && autoNavigate) {
            onAuthSuccess()
        }
    }

    if (token.isNullOrEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isRegistering) "Register" else "Login",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = usernameInput,
                onValueChange = { usernameInput = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = passwordInput,
                onValueChange = { passwordInput = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            if (!error.isNullOrEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = {
                    if (isRegistering) {
                        authViewModel.register(usernameInput, passwordInput)
                    } else {
                        authViewModel.login(usernameInput, passwordInput)
                    }
                }) {
                    Text(if (isRegistering) "Register" else "Login")
                }

                Spacer(Modifier.width(8.dp))

                TextButton(onClick = { isRegistering = !isRegistering }) {
                    Text(if (isRegistering) "Already have an account? Login" else "Don't have an account? Register")
                }
            }
        }
    } else {
        if (!autoNavigate) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Welcome, ${username ?: "User"}",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { authViewModel.fetchUserNovels() }) {
                        Text("Refresh Novels")
                    }
                    Button(onClick = {
                        editingNovel = null
                        showDialog = true
                    }) {
                        Text("Add Novel")
                    }
                }

                Spacer(Modifier.height(16.dp))

                Divider()

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(novels) { novel ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(novel.title, style = MaterialTheme.typography.titleLarge)
                                Spacer(Modifier.height(4.dp))
                                Text("Author: ${novel.author}", style = MaterialTheme.typography.bodyMedium)
                                Text("Status: ${novel.status}", style = MaterialTheme.typography.bodyMedium)
                                Text("Chapters: ${novel.chapters}/${novel.totalChapters}", style = MaterialTheme.typography.bodySmall)
                                if (!novel.notes.isNullOrBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text("Notes: ${novel.notes}", style = MaterialTheme.typography.bodySmall)
                                }

                                Spacer(Modifier.height(12.dp))
                                Row {
                                    OutlinedButton(onClick = {
                                        editingNovel = novel
                                        showDialog = true
                                    }) {
                                        Text("Edit")
                                    }

                                    Spacer(Modifier.width(8.dp))

                                    OutlinedButton(onClick = {
                                        authViewModel.deleteNovel(novel.id.toString()) { success, _ -> }
                                    }) {
                                        Text("Delete")
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { authViewModel.logout() },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Logout")
                }

                if (showDialog) {
                    NovelDialog(
                        novel = editingNovel,
                        currentUserId = currentUserId ?: "",
                        onDismiss = { showDialog = false },
                        onSubmit = { submitted ->
                            showDialog = false
                            if (!submitted.id.isNullOrEmpty()) {
                                authViewModel.updateNovel(submitted.id, submitted) { _, _ -> }
                            } else {
                                authViewModel.createNovel(submitted) { _, _ -> }
                            }
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun NovelDialog(
    novel: NovelBackend?,
    currentUserId: String,
    onDismiss: () -> Unit,
    onSubmit: (NovelBackend) -> Unit
) {
    var title by remember { mutableStateOf(novel?.title ?: "") }
    var author by remember { mutableStateOf(novel?.author ?: "") }
    var status by remember { mutableStateOf(novel?.status ?: "on-hold") }
    var chapters by remember { mutableStateOf(novel?.chapters?.toString() ?: "0") }
    var totalChapters by remember { mutableStateOf(novel?.totalChapters?.toString() ?: "0") }
    var notes by remember { mutableStateOf(novel?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                onSubmit(
                    NovelBackend(
                        id = if (novel?.id.isNullOrEmpty()) null else novel?.id,
                        title = title.trim(), // Required — must not be empty
                        author = author.trim().ifEmpty { "Unknown" },
                        status = status.trim().ifEmpty { "on-hold" }, // fallback if user deletes content
                        chapters = chapters.toIntOrNull() ?: 0,
                        totalChapters = totalChapters.toIntOrNull() ?: 0,
                        notes = notes.trim().ifEmpty { "No notes." },
                        userId = currentUserId
                    )
                )
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = {
            Text(if (novel == null) "Add Novel" else "Edit Novel")
        },
        text = {
            Column {
                TextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                TextField(value = author, onValueChange = { author = it }, label = { Text("Author") })
                TextField(value = status, onValueChange = { status = it }, label = { Text("Status") })
                TextField(value = chapters, onValueChange = { chapters = it }, label = { Text("Chapters") })
                TextField(value = totalChapters, onValueChange = { totalChapters = it }, label = { Text("Total Chapters") })
                TextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") })
            }
        }
    )
}
