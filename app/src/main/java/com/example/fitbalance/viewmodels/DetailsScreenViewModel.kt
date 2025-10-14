package com.example.fitbalance.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailsScreenViewModel @Inject constructor() : ViewModel() {

    var isEditing by mutableStateOf(false)
        private set

    var editableMealItems by mutableStateOf<List<Pair<String, String>>>(emptyList())
        private set

    fun startEditing(items: List<Pair<String, Int>>) {
        isEditing = true
        editableMealItems = items.map { it.first to it.second.toString() }
    }

    fun cancelEditing() {
        isEditing = false
        editableMealItems = emptyList()
    }

    fun confirmEditing() {
        isEditing = false
    }

    fun updateItemName(index: Int, newName: String) {
        editableMealItems = editableMealItems.toMutableList().apply {
            this[index] = this[index].copy(first = newName)
        }
    }

    fun updateItemCalories(index: Int, newCalories: String) {
        editableMealItems = editableMealItems.toMutableList().apply {
            this[index] = this[index].copy(second = newCalories)
        }
    }

    fun removeItem(index: Int) {
        editableMealItems = editableMealItems.toMutableList().apply {
            removeAt(index)
        }
    }

    fun addNewItem() {
        editableMealItems = editableMealItems.toMutableList().apply {
            add("" to "")
        }
    }

    fun getTotalCalories(): Int {
        return editableMealItems.sumOf {
            it.second.toIntOrNull() ?: 0
        }
    }
}