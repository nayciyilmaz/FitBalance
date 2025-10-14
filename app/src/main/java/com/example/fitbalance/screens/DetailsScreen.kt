package com.example.fitbalance.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.fitbalance.components.DetailsScreenEditableMealCard
import com.example.fitbalance.components.EditHorizontalPager
import com.example.fitbalance.components.EditScaffold
import com.example.fitbalance.components.MealCard
import com.example.fitbalance.navigation.FitBalanceScreens
import com.example.fitbalance.viewmodels.DetailsScreenViewModel
import kotlinx.coroutines.delay

@Composable
fun DetailsScreen(
    navController: NavController,
    mealIndex: Int,
    modifier: Modifier = Modifier,
    viewModel: DetailsScreenViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val messages = context.resources.getStringArray(R.array.details_messages)
    val pagerState = rememberPagerState(pageCount = { messages.size })

    val breakfast = listOf("Yulaf ezmesi" to 150, "Süt" to 60, "Muz" to 89, "Yumurta" to 78)
    val lunch = listOf("Tavuk göğsü" to 165, "Pirinç pilavı" to 130, "Yoğurt" to 59, "Salata" to 25)
    val dinner = listOf("Balık" to 206, "Haşlanmış sebze" to 35, "Tam buğday ekmeği" to 69, "Ayran" to 38)

    val meals = listOf(
        "KAHVALTI" to breakfast,
        "ÖĞLE YEMEĞİ" to lunch,
        "AKŞAM YEMEĞİ" to dinner
    )

    val selectedMeal = meals[mealIndex]

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            val nextPage = (pagerState.currentPage + 1) % messages.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    EditScaffold(
        navController = navController,
        title = stringResource(R.string.detaylar),
        showBottomBar = false
    ) { innerPadding ->
        Box(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DetailsScreenInfoCard(
                    messages = messages,
                    pagerState = pagerState
                )

                if (!viewModel.isEditing) {
                    DetailsScreenActionButtons(
                        onEditClick = {
                            viewModel.startEditing(selectedMeal.second)
                        }
                    )
                }

                if (viewModel.isEditing) {
                    DetailsScreenEditableMealCard(
                        title = selectedMeal.first,
                        items = viewModel.editableMealItems,
                        totalCalories = viewModel.getTotalCalories(),
                        onItemNameChange = { index, name ->
                            viewModel.updateItemName(index, name)
                        },
                        onItemCaloriesChange = { index, calories ->
                            viewModel.updateItemCalories(index, calories)
                        },
                        onRemoveItem = { index ->
                            viewModel.removeItem(index)
                        },
                        onAddItem = {
                            viewModel.addNewItem()
                        },
                        onConfirm = {
                            viewModel.confirmEditing()
                        },
                        onCancel = {
                            viewModel.cancelEditing()
                        }
                    )
                } else {
                    MealCard(
                        title = selectedMeal.first,
                        items = selectedMeal.second,
                        showCalories = true
                    )
                }
            }

            if (!viewModel.isEditing) {
                Button(
                    onClick = {
                        navController.navigate(FitBalanceScreens.HomeScreen.route) {
                            popUpTo(FitBalanceScreens.HomeScreen.route) {
                                inclusive = true
                            }
                        }
                    },
                    modifier = modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.green),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = stringResource(R.string.yaptım),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DetailsScreenActionButtons(
    onEditClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionButtonItem(
            icon = Icons.Default.Edit,
            text = stringResource(R.string.düzenle),
            onClick = onEditClick,
            modifier = Modifier.weight(1f)
        )
        ActionButtonItem(
            icon = Icons.Default.AutoFixHigh,
            text = stringResource(R.string.değiştir),
            onClick = { },
            modifier = Modifier.weight(1f)
        )
        ActionButtonItem(
            icon = Icons.Default.Share,
            text = stringResource(R.string.paylaş),
            onClick = { },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ActionButtonItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.blue)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun DetailsScreenInfoCard(
    modifier: Modifier = Modifier,
    messages: Array<String>,
    pagerState: PagerState
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DetailsScreenPreview() {
    val navController = rememberNavController()
    DetailsScreen(navController = navController,mealIndex = 0)
}