package com.example.fitbalance.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fitbalance.screens.HomeScreen
import com.example.fitbalance.screens.SignInScreen
import com.example.fitbalance.screens.SignUpScreen
import com.example.fitbalance.screens.SplashScreen

@Composable
fun FitBalanceNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = FitBalanceScreens.SignUpScreen.route){
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
    }
}