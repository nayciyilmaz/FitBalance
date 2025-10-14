package com.example.fitbalance.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fitbalance.screens.DetailsScreen
import com.example.fitbalance.screens.GoalScreen
import com.example.fitbalance.screens.HomeScreen
import com.example.fitbalance.screens.NotificationScreen
import com.example.fitbalance.screens.ProfileScreen
import com.example.fitbalance.screens.SignInScreen
import com.example.fitbalance.screens.SignUpScreen
import com.example.fitbalance.screens.SplashScreen
import com.example.fitbalance.screens.StatisticScreen

@Composable
fun FitBalanceNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = FitBalanceScreens.HomeScreen.route){
        composable(route = FitBalanceScreens.SplashScreen.route){
            SplashScreen(navController = navController)
        }
        composable(route = FitBalanceScreens.SignInScreen.route){
            SignInScreen(navController = navController)
        }
        composable(route = FitBalanceScreens.SignUpScreen.route){
            SignUpScreen(navController = navController)
        }
        composable(route = FitBalanceScreens.HomeScreen.route){
            HomeScreen(navController = navController)
        }
        composable(
            route = "${FitBalanceScreens.DetailsScreen.route}/{mealIndex}",
            arguments = listOf(navArgument("mealIndex") { type = NavType.IntType })
        ){
            val mealIndex = it.arguments?.getInt("mealIndex") ?: 0
            DetailsScreen(navController = navController, mealIndex = mealIndex)
        }
        composable(route = FitBalanceScreens.GoalScreen.route){
            GoalScreen(navController = navController)
        }
        composable(route = FitBalanceScreens.StatisticScreen.route){
            StatisticScreen(navController = navController)
        }
        composable(route = FitBalanceScreens.NotificationScreen.route){
            NotificationScreen(navController = navController)
        }
        composable(route = FitBalanceScreens.ProfileScreen.route){
            ProfileScreen(navController = navController)
        }
    }
}