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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import com.example.fitbalance.components.EditScaffold
import com.example.fitbalance.viewmodels.GoalScreenViewModel

@Composable
fun GoalScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: GoalScreenViewModel = hiltViewModel()
) {
    EditScaffold(
        navController = navController,
        title = stringResource(R.string.hedefler)
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TrackingCard(
                title = stringResource(R.string.kilo_takibi),
                selectedPeriod = viewModel.selectedWeightPeriod,
                isPeriodExpanded = viewModel.weightPeriodExpanded,
                onPeriodExpandChange = { viewModel.toggleWeightPeriodExpanded() },
                onPeriodSelected = { viewModel.updateSelectedWeightPeriod(it) },
                periodOptions = viewModel.getPeriodOptions(),
                backgroundColor = colorResource(R.color.light_green),
                textColor = colorResource(R.color.dark_green),
                summaryLabel = stringResource(R.string.toplam_degisim),
                summaryValue = viewModel.totalWeightChange?.let { String.format("%+.1f kg", it) },
                monthlyData = viewModel.monthlyWeightDataList.map { (month, data) ->
                    Triple(
                        month,
                        data.first?.let { String.format("%.1f kg", it) },
                        data.second?.let { String.format("(%+.1f)", it) })
                }
            )

            TrackingCard(
                title = stringResource(R.string.gunluk_ortalama_kalori),
                selectedPeriod = viewModel.selectedCaloriePeriod,
                isPeriodExpanded = viewModel.caloriePeriodExpanded,
                onPeriodExpandChange = { viewModel.toggleCaloriePeriodExpanded() },
                onPeriodSelected = { viewModel.updateSelectedCaloriePeriod(it) },
                periodOptions = viewModel.getPeriodOptions(),
                backgroundColor = colorResource(R.color.light_blue),
                textColor = colorResource(R.color.dark_blue),
                summaryLabel = stringResource(R.string.genel_ortalama),
                summaryValue = viewModel.averageCalories?.let { "$it kcal" },
                monthlyData = viewModel.monthlyCalorieDataList.map { (month, calories) ->
                    Triple(month, calories?.let { "$it kcal" }, null)
                }
            )
        }
    }
}

@Composable
fun TrackingCard(
    title: String,
    selectedPeriod: String,
    isPeriodExpanded: Boolean,
    onPeriodExpandChange: () -> Unit,
    onPeriodSelected: (String) -> Unit,
    periodOptions: List<String>,
    backgroundColor: Color,
    textColor: Color,
    summaryLabel: String,
    summaryValue: String?,
    monthlyData: List<Triple<String, String?, String?>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = textColor.copy(alpha = 0.15f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = textColor
                )

                Box {
                    Row(
                        modifier = modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(textColor.copy(alpha = 0.1f))
                            .clickable { onPeriodExpandChange() }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = selectedPeriod,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            color = textColor
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = textColor,
                            modifier = modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = isPeriodExpanded,
                        onDismissRequest = { onPeriodExpandChange() },
                        modifier = modifier.background(
                            Color.White,
                            RoundedCornerShape(12.dp)
                        )
                    ) {
                        periodOptions.forEach { period ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        period,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                },
                                onClick = { onPeriodSelected(period) }
                            )
                        }
                    }
                }
            }

            if (monthlyData.isEmpty()) {
                EmptyDataMessage()
            } else {
                Card(
                    modifier = modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.7f)
                    )
                ) {
                    Column(
                        modifier = modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        monthlyData.forEachIndexed { index, (month, mainValue, extraValue) ->
                            MonthRow(
                                month = month,
                                mainValue = mainValue,
                                extraValue = extraValue,
                                textColor = textColor
                            )
                            if (index < monthlyData.size - 1) {
                                HorizontalDivider(
                                    color = textColor.copy(alpha = 0.3f),
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            summaryValue?.let { value ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = textColor.copy(alpha = 0.15f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(textColor)
                            )
                            Text(
                                text = summaryLabel,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                ),
                                color = textColor
                            )
                        }
                        Text(
                            text = value,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthRow(
    month: String,
    mainValue: String?,
    extraValue: String?,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = month,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                letterSpacing = 0.sp
            ),
            color = textColor,
            modifier = Modifier.weight(1f)
        )

        if (mainValue != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mainValue,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        letterSpacing = (-0.3).sp
                    ),
                    color = textColor
                )

                extraValue?.let { extra ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = textColor.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = extra,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = textColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = textColor.copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.veri_yok),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    ),
                    color = textColor.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyDataMessage(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.yeterli_veri_yok),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                ),
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GoalScreenPreview() {
    val navController = rememberNavController()
    GoalScreen(navController = navController)
}