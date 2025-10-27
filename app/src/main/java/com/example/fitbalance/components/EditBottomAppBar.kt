package com.example.fitbalance.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.fitbalance.R
import com.example.fitbalance.data.BottomNavItem
import com.example.fitbalance.navigation.FitBalanceScreens

@Composable
fun EditBottomAppBar(
    navController: NavController,
    modifier: Modifier = Modifier,
    onFabClick: () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        BottomNavItem(
            route = FitBalanceScreens.HomeScreen.route,
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            label = stringResource(R.string.ana_sayfa)
        ),
        BottomNavItem(
            route = FitBalanceScreens.GoalScreen.route,
            selectedIcon = Icons.Filled.Star,
            unselectedIcon = Icons.Outlined.Star,
            label = stringResource(R.string.hedefler)
        ),
        BottomNavItem(
            route = "",
            selectedIcon = Icons.Filled.Notifications,
            unselectedIcon = Icons.Outlined.Notifications,
            label = ""
        ),
        BottomNavItem(
            route = FitBalanceScreens.StatisticScreen.route,
            selectedIcon = Icons.Filled.BarChart,
            unselectedIcon = Icons.Outlined.BarChart,
            label = stringResource(R.string.istatistikler)
        ),
        BottomNavItem(
            route = FitBalanceScreens.ProfileScreen.route,
            selectedIcon = Icons.Filled.AccountCircle,
            unselectedIcon = Icons.Outlined.AccountCircle,
            label = stringResource(R.string.profil)
        )
    )

    NavigationBar(
        modifier = modifier,
        containerColor = Color.White,
        contentColor = colorResource(id = R.color.green)
    ) {
        items.forEachIndexed { index, item ->
            if (index == 2) {
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = {
                        FloatingActionButton(
                            onClick = onFabClick,
                            containerColor = colorResource(id = R.color.green),
                            contentColor = Color.White
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    },
                    label = null,
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent
                    )
                )
            } else {
                NavigationBarItem(
                    selected = currentRoute == item.route,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(FitBalanceScreens.NotificationScreen.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (currentRoute == item.route) item.selectedIcon else item.unselectedIcon,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            color = if (currentRoute == item.route) {
                                colorResource(id = R.color.green)
                            } else {
                                Color.Gray
                            }
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colorResource(id = R.color.green),
                        unselectedIconColor = Color.Gray,
                        selectedTextColor = colorResource(id = R.color.green),
                        unselectedTextColor = Color.Gray,
                        indicatorColor = colorResource(id = R.color.light_green).copy(alpha = 0.2f)
                    )
                )
            }
        }
    }
}