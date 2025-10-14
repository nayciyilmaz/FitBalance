package com.example.fitbalance.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.fitbalance.components.EditHorizontalPager
import com.example.fitbalance.components.EditScaffold
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

    val breakfast = listOf("Yulaf ezmesi" to 150, "Süt" to 60, "Muz" to 89, "Yumurta" to 78)
    val lunch = listOf("Tavuk göğsü" to 165, "Pirinç pilavı" to 130, "Yoğurt" to 59, "Salata" to 25)
    val dinner = listOf("Balık" to 206, "Haşlanmış sebze" to 35, "Tam buğday ekmeği" to 69, "Ayran" to 38)

    val meals = listOf(
        "KAHVALTI" to breakfast,
        "ÖĞLE YEMEĞİ" to lunch,
        "AKŞAM YEMEĞİ" to dinner
    )

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
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HomeScreenInfoCard(
                    messages = messages,
                    pagerState = pagerState
                )

                HomeScreenDateCard(
                    currentDate = viewModel.currentDate,
                    caloriesBurned = viewModel.caloriesBurned
                )

                Text(
                    text = "ÖĞÜNLERİM",
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Start
                )

                meals.forEachIndexed { index, meal ->
                    MealCard(
                        title = meal.first,
                        items = meal.second,
                        showCalories = false,
                        onClick = {
                            navController.navigate("${FitBalanceScreens.DetailsScreen.route}/$index")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreenInfoCard(
    messages: Array<String>,
    pagerState: PagerState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.blue)
        )
    ) {
        EditHorizontalPager(
            messages = messages,
            pagerState = pagerState
        )
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
            .padding(top = 12.dp, bottom = 18.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.blue)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = currentDate,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            Text(
                text = "Gün içerisinde alınan kalori = $caloriesBurned",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController()
    HomeScreen(navController = navController)
}