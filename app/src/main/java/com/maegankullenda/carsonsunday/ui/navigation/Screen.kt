package com.maegankullenda.carsonsunday.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Welcome : Screen("welcome")
}
