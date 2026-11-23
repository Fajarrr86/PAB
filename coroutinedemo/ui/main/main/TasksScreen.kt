package com.example.coroutinedemo.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TasksScreen(
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TasksContent(
        state = state,
        onLoad = { viewModel.loadData() },
        onLoadWithError = { viewModel.loadData(shouldFail = true) },
        onCancel = viewModel::cancelLoading,
        onTap = viewModel::onTap,
        onBlock = viewModel::blockMainThread,
        onSort = viewModel::sortTasks,
        onAdd = viewModel::addTask,
        modifier = modifier
    )
}

@Composable
fun TasksContent(
    state: TasksUiState,
    onLoad: () -> Unit,
    onLoadWithError: () -> Unit,
    onCancel: () -> Unit,
    onTap: () -> Unit,
    onBlock: () -> Unit,
    onSort: () -> Unit,
    onAdd: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var newTitle by remember { mutableStateOf("") }
    var newCourse by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Coroutine Demo", style = MaterialTheme.typography.headlineSmall)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onLoad, enabled = !state.isLoading) { Text("Load") }
            OutlinedButton(onClick = onLoadWithError, enabled = !state.isLoading) {
                Text("Load (fail)")
            }
            OutlinedButton(onClick = onCancel, enabled = state.isLoading) { Text("Cancel") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onTap) { Text("Tap me: ${state.tapCount}") }
            OutlinedButton(onClick = onBlock) { Text("Block UI") }
            OutlinedButton(onClick = onSort, enabled = state.tasks.isNotEmpty()) {
                Text("Sort")
            }
        }

        if (state.isLoading) CircularProgressIndicator()

        // Latihan 2: tombol Retry
        state.errorMessage?.let { message ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(message, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onLoad) { Text("Retry") }
            }
        }

        if (state.announcement.isNotEmpty()) {
            Text("Announcement: ${state.announcement}")
        }
        if (state.courseCount > 0) {
            Text("Total courses: ${state.courseCount}")
        }

        // Modifikasi bebas: form tambah tugas
        OutlinedTextField(
            value = newTitle,
            onValueChange = { newTitle = it },
            label = { Text("Task title") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = newCourse,
            onValueChange = { newCourse = it },
            label = { Text("Course") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    onAdd(newTitle, newCourse)
                    newTitle = ""
                    newCourse = ""
                },
                enabled = newTitle.isNotBlank() && !state.isSaving
            ) { Text("Add task") }
            if (state.isSaving) CircularProgressIndicator(modifier = Modifier.padding(4.dp))
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.tasks, key = { it.id }) { task ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(task.title, style = MaterialTheme.typography.titleMedium)
                        Text(task.course, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}