package com.example.fitbalance.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fitbalance.R
import com.example.fitbalance.components.EditScaffold
import com.example.fitbalance.components.EditButtons
import com.example.fitbalance.navigation.FitBalanceScreens
import com.example.fitbalance.viewmodels.ProfileScreenViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ProfileScreenViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.refreshUserData()
    }

    EditScaffold(
        navController = navController,
        title = stringResource(R.string.profil)
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            viewModel.userData?.let { user ->
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = modifier
                            .fillMaxWidth(0.35f)
                            .aspectRatio(1f)
                            .background(
                                color = colorResource(id = R.color.light_green),
                                shape = CircleShape
                            )
                            .border(
                                width = 4.dp,
                                color = colorResource(id = R.color.green),
                                shape = CircleShape
                            )
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = modifier.fillMaxSize(),
                            tint = Color.White
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = colorResource(id = R.color.green)
                        )
                        Text(
                            text = " ${user.surname}",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = colorResource(id = R.color.green)
                        )
                    }

                    ProfileDetailsCard(
                        user = user,
                        onSettingsClick = {
                            navController.navigate(FitBalanceScreens.SettingsScreen.route)
                        }
                    )

                    EditButtons(
                        text = stringResource(R.string.log_out),
                        onClick = {
                            viewModel.signOut {
                                navController.navigate(FitBalanceScreens.SignInScreen.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        },
                        modifier = modifier.padding(horizontal = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileDetailsCard(
    user: com.example.fitbalance.data.UserData,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = MaterialTheme.shapes.large,
        color = colorResource(id = R.color.green),
        tonalElevation = 8.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.person_details),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = Color.White,
                modifier = modifier.padding(bottom = 12.dp)
            )

            ProfileInfoItem(
                label = stringResource(R.string.mail),
                value = user.email
            )

            HorizontalDivider(
                modifier = modifier.padding(vertical = 8.dp),
                color = Color.White
            )

            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileInfoItem(
                    label = stringResource(R.string.boy),
                    value = "${user.height.toInt()} cm",
                    modifier = modifier.weight(1f)
                )
                ProfileInfoItem(
                    label = stringResource(R.string.kilo),
                    value = "${user.currentWeight.toInt()} kg",
                    modifier = modifier.weight(1f)
                )
                ProfileInfoItem(
                    label = stringResource(R.string.yas),
                    value = user.age.toString(),
                    modifier = modifier.weight(1f)
                )
            }

            HorizontalDivider(
                modifier = modifier.padding(vertical = 8.dp),
                color = Color.White
            )

            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileInfoItem(
                    label = stringResource(R.string.gender_label),
                    value = user.gender,
                    modifier = modifier.weight(1f)
                )
                ProfileInfoItem(
                    label = stringResource(R.string.goal_label),
                    value = user.goal,
                    modifier = modifier.weight(1f)
                )
            }

            EditButtons(
                text = stringResource(R.string.update_details),
                onClick = onSettingsClick,
                modifier = modifier.padding(top = 20.dp),
                containerColor = Color.White,
                contentColor = colorResource(id = R.color.green)
            )
        }
    }
}

@Composable
fun ProfileInfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.2f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = Color.White.copy(alpha = 0.85f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    val navController = rememberNavController()
    ProfileScreen(navController = navController)
}