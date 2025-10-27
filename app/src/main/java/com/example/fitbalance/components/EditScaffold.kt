package com.example.fitbalance.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.fitbalance.navigation.FitBalanceScreens

@Composable
fun EditScaffold(
    navController: NavController,
    title: String,
    showBottomBar: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            EditTopAppBar(title = title)
        },
        bottomBar = {
            if (showBottomBar) {
                EditBottomAppBar(
                    navController = navController,
                    onFabClick = {
                        navController.navigate(FitBalanceScreens.NotificationScreen.route)
                    }
                )
            }
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}