package com.example.fitbalance.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitbalance.data.MealPlan
import com.example.fitbalance.data.UserData
import com.example.fitbalance.data.WeightEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class GoalScreenViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var selectedWeightPeriod by mutableStateOf("Son 3 Ay")
    var weightPeriodExpanded by mutableStateOf(false)

    var selectedCaloriePeriod by mutableStateOf("Son 3 Ay")
    var caloriePeriodExpanded by mutableStateOf(false)

    var monthlyWeightDataList by mutableStateOf<List<Pair<String, Pair<Double?, Double?>>>>(emptyList())
        private set

    var totalWeightChange by mutableStateOf<Double?>(null)
        private set

    var monthlyCalorieDataList by mutableStateOf<List<Pair<String, Int?>>>(emptyList())
        private set

    var averageCalories by mutableStateOf<Int?>(null)
        private set

    private var weightHistory: List<WeightEntry> = emptyList()
    private var mealPlans: List<MealPlan> = emptyList()
    private var userRegistrationDate: LocalDate? = null

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val userId = firebaseAuth.currentUser?.uid
            if (userId == null) {
                errorMessage = "Kullanıcı bulunamadı"
                isLoading = false
                return@launch
            }

            try {
                val userDoc = firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()

                val userData = userDoc.toObject(UserData::class.java)
                weightHistory = userData?.weightHistory ?: emptyList()

                userRegistrationDate = if (weightHistory.isNotEmpty()) {
                    val oldestEntry = weightHistory.minByOrNull { it.date }
                    oldestEntry?.let {
                        Instant.ofEpochMilli(it.date)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                } else {
                    LocalDate.now()
                }

                val mealSnapshot = firestore.collection("meal_plans")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                mealPlans = mealSnapshot.documents.mapNotNull { doc ->
                    doc.toObject(MealPlan::class.java)
                }

                updateWeightData()
                updateCalorieData()

                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Veriler yüklenemedi: ${e.message}"
                isLoading = false
                e.printStackTrace()
            }
        }
    }

    fun updateSelectedWeightPeriod(period: String) {
        selectedWeightPeriod = period
        weightPeriodExpanded = false
        updateWeightData()
    }

    fun updateSelectedCaloriePeriod(period: String) {
        selectedCaloriePeriod = period
        caloriePeriodExpanded = false
        updateCalorieData()
    }

    fun toggleWeightPeriodExpanded() {
        weightPeriodExpanded = !weightPeriodExpanded
    }

    fun toggleCaloriePeriodExpanded() {
        caloriePeriodExpanded = !caloriePeriodExpanded
    }

    private fun getPeriodMonths(period: String): Int {
        return when (period) {
            "Son 1 Ay" -> 1
            "Son 2 Ay" -> 2
            "Son 3 Ay" -> 3
            "Son 4 Ay" -> 4
            "Son 6 Ay" -> 6
            "Son 9 Ay" -> 9
            "Son 12 Ay" -> 12
            else -> 3
        }
    }

    private fun updateWeightData() {
        val months = getPeriodMonths(selectedWeightPeriod)
        val result = generateMonthlyWeightData(months)
        monthlyWeightDataList = result.first
        totalWeightChange = result.second
    }

    private fun updateCalorieData() {
        val months = getPeriodMonths(selectedCaloriePeriod)
        val result = generateMonthlyCalorieData(months)
        monthlyCalorieDataList = result.first
        averageCalories = result.second
    }

    private fun generateMonthlyWeightData(selectedMonths: Int): Pair<List<Pair<String, Pair<Double?, Double?>>>, Double?> {
        if (weightHistory.isEmpty() || userRegistrationDate == null) {
            return Pair(emptyList(), null)
        }

        val now = LocalDate.now()
        val registrationDate = userRegistrationDate!!
        val monthlyLastWeights = mutableMapOf<YearMonth, Double>()

        weightHistory.forEach { entry ->
            val date = Instant.ofEpochMilli(entry.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            val yearMonth = YearMonth.from(date)

            val existingDate = monthlyLastWeights.keys.find { it == yearMonth }
            if (existingDate == null) {
                monthlyLastWeights[yearMonth] = entry.weight
            } else {
                val laterEntry = weightHistory
                    .filter {
                        val d = Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
                        YearMonth.from(d) == yearMonth && it.date >= entry.date
                    }
                    .maxByOrNull { it.date }

                laterEntry?.let { monthlyLastWeights[yearMonth] = it.weight }
            }
        }

        val properMonthlyWeights = mutableMapOf<YearMonth, Double>()
        weightHistory
            .sortedBy { it.date }
            .forEach { entry ->
                val date = Instant.ofEpochMilli(entry.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val yearMonth = YearMonth.from(date)
                properMonthlyWeights[yearMonth] = entry.weight
            }

        val dataList = mutableListOf<Pair<String, Pair<Double?, Double?>>>()
        val startYearMonth = YearMonth.from(now).minusMonths((selectedMonths - 1).toLong())
        val registrationYearMonth = YearMonth.from(registrationDate)

        var previousMonthWeight: Double? = null
        var firstWeight: Double? = null
        var lastWeight: Double? = null

        for (i in 0 until selectedMonths) {
            val targetMonth = startYearMonth.plusMonths(i.toLong())
            val monthName = targetMonth.month.getDisplayName(TextStyle.FULL, Locale("tr"))
                .replaceFirstChar { it.uppercase() } + " ${targetMonth.year}"

            if (targetMonth.isBefore(registrationYearMonth)) {
                dataList.add(Pair(monthName, Pair(null, null)))
            } else {
                val lastWeightOfMonth = properMonthlyWeights[targetMonth]

                val changeFromPrevious = if (lastWeightOfMonth != null && previousMonthWeight != null) {
                    lastWeightOfMonth - previousMonthWeight
                } else null

                dataList.add(Pair(monthName, Pair(lastWeightOfMonth, changeFromPrevious)))

                if (lastWeightOfMonth != null) {
                    if (firstWeight == null) firstWeight = lastWeightOfMonth
                    lastWeight = lastWeightOfMonth
                    previousMonthWeight = lastWeightOfMonth
                }
            }
        }

        val totalChange = if (firstWeight != null && lastWeight != null) {
            lastWeight - firstWeight
        } else null

        return Pair(dataList, totalChange)
    }

    private fun generateMonthlyCalorieData(selectedMonths: Int): Pair<List<Pair<String, Int?>>, Int?> {
        if (mealPlans.isEmpty() || userRegistrationDate == null) {
            return Pair(emptyList(), null)
        }

        val now = LocalDate.now()
        val registrationDate = userRegistrationDate!!

        val monthlyCalories = mutableMapOf<YearMonth, MutableList<Int>>()

        mealPlans.forEach { plan ->
            val date = LocalDate.parse(plan.date, DateTimeFormatter.ISO_LOCAL_DATE)
            val yearMonth = YearMonth.from(date)

            var dailyCalories = 0
            plan.breakfast?.let { if (it.isCompleted) dailyCalories += it.totalCalories }
            plan.lunch?.let { if (it.isCompleted) dailyCalories += it.totalCalories }
            plan.dinner?.let { if (it.isCompleted) dailyCalories += it.totalCalories }

            if (dailyCalories > 0) {
                monthlyCalories.getOrPut(yearMonth) { mutableListOf() }.add(dailyCalories)
            }
        }

        val dataList = mutableListOf<Pair<String, Int?>>()
        val startYearMonth = YearMonth.from(now).minusMonths((selectedMonths - 1).toLong())
        val registrationYearMonth = YearMonth.from(registrationDate)

        val allCalories = mutableListOf<Int>()

        for (i in 0 until selectedMonths) {
            val targetMonth = startYearMonth.plusMonths(i.toLong())
            val monthName = targetMonth.month.getDisplayName(TextStyle.FULL, Locale("tr"))
                .replaceFirstChar { it.uppercase() } + " ${targetMonth.year}"

            if (targetMonth.isBefore(registrationYearMonth)) {
                dataList.add(Pair(monthName, null))
            } else {
                val calories = monthlyCalories[targetMonth]
                val avgCalories = calories?.average()?.toInt()
                dataList.add(Pair(monthName, avgCalories))

                if (calories != null) {
                    allCalories.addAll(calories)
                }
            }
        }

        val overallAverage = if (allCalories.isNotEmpty()) {
            allCalories.average().toInt()
        } else null

        return Pair(dataList, overallAverage)
    }

    fun getPeriodOptions(): List<String> {
        return listOf("Son 1 Ay", "Son 2 Ay", "Son 3 Ay", "Son 4 Ay", "Son 6 Ay", "Son 9 Ay", "Son 12 Ay")
    }
}