package com.example.fitbalance.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fitbalance.R
import com.example.fitbalance.components.EditScaffold
import com.example.fitbalance.components.MealCard
import com.example.fitbalance.viewmodels.StatisticScreenViewModel
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.daysOfWeek
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun StatisticScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: StatisticScreenViewModel = hiltViewModel()
) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(12) }
    val endMonth = remember { currentMonth.plusMonths(12) }
    val daysOfWeek = remember { daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY) }

    LaunchedEffect(viewModel.selectedDate) {
        viewModel.loadMealPlanForDate(viewModel.selectedDate)
    }

    EditScaffold(
        navController = navController,
        title = stringResource(R.string.istatistikler)
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CalendarSection(
                visibleMonth = viewModel.visibleMonth,
                onMonthChange = { viewModel.updateVisibleMonth(it) },
                daysOfWeek = daysOfWeek,
                startMonth = startMonth,
                endMonth = endMonth,
                selectedDate = viewModel.selectedDate,
                onDateClick = { viewModel.updateSelectedDate(it) }
            )

            viewModel.selectedDateMealPlan?.let { mealPlan ->
                val completedBreakfast = mealPlan.breakfast?.takeIf { it.isCompleted }
                val completedLunch = mealPlan.lunch?.takeIf { it.isCompleted }
                val completedDinner = mealPlan.dinner?.takeIf { it.isCompleted }

                val hasAnyCompletedMeal = completedBreakfast != null ||
                        completedLunch != null ||
                        completedDinner != null

                if (hasAnyCompletedMeal) {
                    TotalCaloriesCard(
                        totalCalories = viewModel.getTotalCaloriesForDate(),
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    MealsSection(
                        breakfast = completedBreakfast?.let {
                            stringResource(R.string.kahvalti) to it.items.map { item -> item.name to item.calories }
                        },
                        lunch = completedLunch?.let {
                            stringResource(R.string.ögle_yemegi) to it.items.map { item -> item.name to item.calories }
                        },
                        dinner = completedDinner?.let {
                            stringResource(R.string.aksam_yemegi) to it.items.map { item -> item.name to item.calories }
                        }
                    )
                } else {
                    NoMealPlanCard()
                }
            } ?: run {
                NoMealPlanCard()
            }
        }
    }
}

@Composable
fun CalendarSection(
    visibleMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    daysOfWeek: List<DayOfWeek>,
    startMonth: YearMonth,
    endMonth: YearMonth,
    selectedDate: LocalDate,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onMonthChange(visibleMonth.minusMonths(1)) }
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = null,
                        tint = Color.Black
                    )
                }

                Text(
                    text = visibleMonth.month.getDisplayName(TextStyle.FULL, Locale("tr"))
                        .replaceFirstChar { it.uppercase() } + " " + visibleMonth.year,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                )

                IconButton(
                    onClick = { onMonthChange(visibleMonth.plusMonths(1)) }
                ) {
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.Black
                    )
                }
            }

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysOfWeek.forEach { day ->
                    Text(
                        text = day.getDisplayName(TextStyle.SHORT, Locale("tr"))
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        ),
                        modifier = modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            HorizontalCalendar(
                state = rememberCalendarState(
                    startMonth = startMonth,
                    endMonth = endMonth,
                    firstVisibleMonth = visibleMonth,
                    firstDayOfWeek = DayOfWeek.MONDAY
                ),
                monthHeader = {},
                dayContent = { day ->
                    CalendarDayItem(
                        day = day,
                        selectedDate = selectedDate,
                        onDateClick = onDateClick
                    )
                }
            )
        }
    }
}

@Composable
fun TotalCaloriesCard(
    totalCalories: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.dark_green)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.total_alinan_kalori),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Text(
                text = "$totalCalories kcal",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun MealsSection(
    breakfast: Pair<String, List<Pair<String, Int>>>?,
    lunch: Pair<String, List<Pair<String, Int>>>?,
    dinner: Pair<String, List<Pair<String, Int>>>?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        breakfast?.let { (title, items) ->
            MealCard(
                title = title,
                items = items,
                showCalories = true
            )
        }

        lunch?.let { (title, items) ->
            MealCard(
                title = title,
                items = items,
                showCalories = true
            )
        }

        dinner?.let { (title, items) ->
            MealCard(
                title = title,
                items = items,
                showCalories = true
            )
        }
    }
}

@Composable
fun NoMealPlanCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.light_green)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.date_meal),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = colorResource(R.color.dark_green)
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CalendarDayItem(
    day: CalendarDay,
    selectedDate: LocalDate,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = day.date == selectedDate
    val isToday = day.date == LocalDate.now()

    Box(
        modifier = modifier
            .size(40.dp)
            .padding(2.dp)
            .clickable { onDateClick(day.date) }
            .background(
                color = when {
                    isSelected -> colorResource(R.color.green)
                    isToday -> colorResource(R.color.light_green)
                    else -> Color.Transparent
                },
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> Color.White
                    isToday -> colorResource(R.color.dark_green)
                    else -> Color.Black
                }
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun StatisticScreenPreview() {
    val navController = rememberNavController()
    StatisticScreen(navController = navController)
}