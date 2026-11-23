package com.example.coroutinedemo.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coroutinedemo.data.Task
import com.example.coroutinedemo.data.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class TasksViewModel(
    private val repository: TaskRepository = TaskRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun loadData(shouldFail: Boolean = false) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                coroutineScope {
                    val tasks = async { repository.getTasks(shouldFail) }
                    val announcement = async { repository.getAnnouncement() }
                    val courses = async { repository.getCourseCount() } // Latihan 1

                    val loadedTasks = tasks.await()
                    val loadedAnnouncement = announcement.await()
                    val loadedCourses = courses.await()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            tasks = loadedTasks,
                            announcement = loadedAnnouncement,
                            courseCount = loadedCourses
                        )
                    }
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Failed to load: ${e.message}")
                }
            }
        }
    }

    fun cancelLoading() {
        loadJob?.cancel()
        _uiState.update { it.copy(isLoading = false, errorMessage = "Loading cancelled") }
    }

    fun onTap() {
        _uiState.update { it.copy(tapCount = it.tapCount + 1) }
    }

    // Contoh buruk yang disengaja
    fun blockMainThread() {
        Thread.sleep(3000L)
    }

    // Latihan 6: sort di Dispatchers.Default
    fun sortTasks() {
        viewModelScope.launch {
            val sorted = withContext(Dispatchers.Default) {
                _uiState.value.tasks.sortedBy { it.title }
            }
            _uiState.update { it.copy(tasks = sorted) }
        }
    }

    // Modifikasi bebas: tambah tugas dengan simulasi simpan
    fun addTask(title: String, course: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val newId = (_uiState.value.tasks.maxOfOrNull { it.id } ?: 0) + 1
            val saved = repository.saveTask(
                Task(newId, title.trim(), course.trim().ifBlank { "General" })
            )
            _uiState.update { it.copy(isSaving = false, tasks = it.tasks + saved) }
        }
    }
}