package com.example.fitbalance.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor() : ViewModel() {

    var currentDate by mutableStateOf("")
        private set

    var caloriesBurned by mutableStateOf(0)
        private set

    init {
        updateCurrentDate()
    }

    private fun updateCurrentDate() {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("d MMMM EEEE", Locale("tr"))
        currentDate = today.format(formatter)
    }
}