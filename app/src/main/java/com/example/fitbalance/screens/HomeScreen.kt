package com.example.fitbalance.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.fitbalance.components.EditButtons
import com.example.fitbalance.components.EditScaffold
import com.example.fitbalance.components.InfoCard
import com.example.fitbalance.components.LoadingIndicator
import com.example.fitbalance.components.MealCard
import com.example.fitbalance.navigation.FitBalanceScreens
import com.example.fitbalance.viewmodels.HomeScreenViewModel
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: HomeScreenViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val messages = context.resources.getStringArray(R.array.home_messages)
    val pagerState = rememberPagerState(pageCount = { messages.size })

    LaunchedEffect(Unit) {
        viewModel.loadTodayMealPlan()
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            val nextPage = (pagerState.currentPage + 1) % messages.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    EditScaffold(
        navController = navController,
        title = stringResource(R.string.ana_sayfa)
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            InfoCard(
                messages = messages,
                pagerState = pagerState,
                modifier = modifier.fillMaxWidth()
            )
            key(viewModel.refreshTrigger) {
                HomeScreenDateCard(
                    currentDate = viewModel.currentDate,
                    caloriesBurned = viewModel.caloriesBurned
                )
            }

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = colorResource(R.color.green),
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .align(Alignment.CenterVertically)
                )
                Text(
                    text = stringResource(R.string.ögünlerim),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = colorResource(R.color.green)
                    ),
                    textAlign = TextAlign.Start
                )
            }
            HorizontalDivider(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                thickness = 1.dp,
                color = colorResource(R.color.green)
            )

            if (viewModel.isLoading) {
                LoadingIndicator(
                    text = stringResource(R.string.yapayzeka_ögün_plan)
                )
            } else if (viewModel.hasError) {
                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EditButtons(
                        text = stringResource(R.string.tekrar_dene),
                        onClick = { viewModel.generateNewMealPlan() },
                        modifier = modifier
                            .fillMaxWidth(),
                        containerColor = colorResource(R.color.green),
                        contentColor = Color.White
                    )
                    Text(
                        text = stringResource(R.string.ogün_olusturulamadi),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                key(viewModel.refreshTrigger) {
                    HomeScreenMealSection(
                        viewModel = viewModel,
                        onBreakfastClick = {
                            navController.navigate("${FitBalanceScreens.DetailsScreen.route}/0")
                        },
                        onLunchClick = {
                            navController.navigate("${FitBalanceScreens.DetailsScreen.route}/1")
                        },
                        onDinnerClick = {
                            navController.navigate("${FitBalanceScreens.DetailsScreen.route}/2")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreenMealSection(
    viewModel: HomeScreenViewModel,
    onBreakfastClick: () -> Unit,
    onLunchClick: () -> Unit,
    onDinnerClick: () -> Unit
) {
    Column {
        if (viewModel.hasAnyVisibleMeal) {
            viewModel.visibleBreakfast?.let { breakfast ->
                MealCard(
                    title = stringResource(R.string.kahvalti),
                    items = breakfast.items.map { it.name to it.calories },
                    showCalories = false,
                    onClick = onBreakfastClick
                )
            }

            viewModel.visibleLunch?.let { lunch ->
                MealCard(
                    title = stringResource(R.string.ögle_yemegi),
                    items = lunch.items.map { it.name to it.calories },
                    showCalories = false,
                    onClick = onLunchClick
                )
            }

            viewModel.visibleDinner?.let { dinner ->
                MealCard(
                    title = stringResource(R.string.aksam_yemegi),
                    items = dinner.items.map { it.name to it.calories },
                    showCalories = false,
                    onClick = onDinnerClick
                )
            }
        } else if (viewModel.shouldShowAllMealsCompleted) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(R.color.green)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.tüm_ögün),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreenDateCard(
    currentDate: String,
    caloriesBurned: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.blue)
        )
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = modifier
                        .background(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Whatshot,
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                Column(modifier = modifier.padding(start = 16.dp)) {
                    Text(
                        text = "Gün içerisinde alınan kalori :",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "$caloriesBurned kcal",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }

            HorizontalDivider(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                thickness = 1.dp,
                color = Color.White.copy(alpha = 0.7f)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = modifier
                        .background(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                Column(modifier = modifier.padding(start = 16.dp)) {
                    Text(
                        text = "Bugünün tarihi :",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = currentDate,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController()
    HomeScreen(navController = navController)
}