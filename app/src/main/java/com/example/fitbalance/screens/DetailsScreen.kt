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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fitbalance.R
import com.example.fitbalance.components.DetailsScreenEditableMealCard
import com.example.fitbalance.components.EditScaffold
import com.example.fitbalance.components.InfoCard
import com.example.fitbalance.components.MealCard
import com.example.fitbalance.viewmodels.DetailsScreenViewModel
import com.example.fitbalance.viewmodels.HomeScreenViewModel
import androidx.compose.runtime.rememberCoroutineScope
import com.example.fitbalance.components.LoadingIndicator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DetailsScreen(
    navController: NavController,
    mealIndex: Int,
    modifier: Modifier = Modifier,
    viewModel: DetailsScreenViewModel = hiltViewModel(),
    homeViewModel: HomeScreenViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val messages = context.resources.getStringArray(R.array.details_messages)
    val pagerState = rememberPagerState(pageCount = { messages.size })
    val scope = rememberCoroutineScope()

    val (mealTitle, currentMeal, mealType) = when (mealIndex) {
        0 -> Triple(
            stringResource(R.string.kahvalti),
            homeViewModel.breakfast,
            "breakfast"
        )
        1 -> Triple(
            stringResource(R.string.ögle_yemegi),
            homeViewModel.lunch,
            "lunch"
        )
        2 -> Triple(
            stringResource(R.string.aksam_yemegi),
            homeViewModel.dinner,
            "dinner"
        )
        else -> Triple("", null, "")
    }

    val mealItems = currentMeal?.items?.map { it.name to it.calories } ?: emptyList()

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            val nextPage = (pagerState.currentPage + 1) % messages.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    LaunchedEffect(homeViewModel.currentMealPlanId, mealType) {
        if (homeViewModel.currentMealPlanId.isNotEmpty()) {
            viewModel.checkCanChangeToday(homeViewModel.currentMealPlanId, mealType)
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
                InfoCard(
                    messages = messages,
                    pagerState = pagerState,
                    modifier = modifier.fillMaxWidth()
                )

                if (!viewModel.isEditing && !viewModel.isLoading) {
                    DetailsScreenActionButtons(
                        onEditClick = {
                            viewModel.startEditing(
                                mealItems,
                                homeViewModel.currentMealPlanId,
                                mealType
                            )
                        },
                        onChangeClick = {
                            viewModel.changeMeal(
                                homeViewModel.currentMealPlanId,
                                mealType
                            ) { newMeal ->
                                homeViewModel.updateMeal(mealType, newMeal)
                            }
                        },
                        onShareClick = {
                            viewModel.shareMeal(mealTitle, mealItems)
                        },
                        canChange = viewModel.canChangeToday
                    )
                }

                if (viewModel.isLoading) {
                    LoadingIndicator(
                        text = stringResource(R.string.yapayzeka_ögünplan)
                    )
                }

                if (viewModel.isEditing) {
                    DetailsScreenEditableMealCard(
                        title = mealTitle,
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
                            viewModel.confirmEditing { updatedMeal ->
                                homeViewModel.updateMeal(mealType, updatedMeal)
                            }
                        },
                        onCancel = {
                            viewModel.cancelEditing()
                        }
                    )
                } else if (!viewModel.isLoading) {
                    MealCard(
                        title = mealTitle,
                        items = mealItems,
                        showCalories = true
                    )
                }
            }

            if (!viewModel.isEditing && !viewModel.isLoading) {
                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DetailsScreenBottomButton(
                            text = stringResource(R.string.yaptım),
                            onClick = {
                                scope.launch {
                                    homeViewModel.markMealStatusSync(mealType, true)
                                    navController.popBackStack()
                                }
                            },
                            modifier = modifier.weight(1f)
                        )

                        DetailsScreenBottomButton(
                            text = stringResource(R.string.yapmadım),
                            onClick = {
                                scope.launch {
                                    homeViewModel.markMealStatusSync(mealType, false)
                                    navController.popBackStack()
                                }
                            },
                            modifier = modifier.weight(1f)
                        )

                        DetailsScreenBottomButton(
                            text = stringResource(R.string.geri),
                            onClick = {
                                navController.popBackStack()
                            },
                            modifier = modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailsScreenBottomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(R.color.green),
            contentColor = Color.White
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            ),
            textAlign = TextAlign.Center,
            modifier = modifier.padding(vertical = 4.dp)
        )
    }
}

@Composable
fun DetailsScreenActionButtons(
    onEditClick: () -> Unit = {},
    onChangeClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    canChange: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionButtonItem(
            icon = Icons.Default.Edit,
            text = stringResource(R.string.düzenle),
            onClick = onEditClick,
            modifier = Modifier.weight(1f),
            enabled = true
        )
        ActionButtonItem(
            icon = Icons.Default.AutoFixHigh,
            text = stringResource(R.string.değiştir),
            onClick = onChangeClick,
            modifier = Modifier.weight(1f),
            enabled = canChange
        )
        ActionButtonItem(
            icon = Icons.Default.Share,
            text = stringResource(R.string.paylaş),
            onClick = onShareClick,
            modifier = Modifier.weight(1f),
            enabled = true
        )
    }
}

@Composable
fun ActionButtonItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Card(
        modifier = modifier
            .clickable(enabled = enabled) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) colorResource(R.color.blue) else Color.Gray
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
                tint = if (enabled) Color.White else Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = text,
                color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DetailsScreenPreview() {
    val navController = rememberNavController()
    DetailsScreen(navController = navController, mealIndex = 0)
}