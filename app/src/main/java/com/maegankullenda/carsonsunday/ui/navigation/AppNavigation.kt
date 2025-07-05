package com.maegankullenda.carsonsunday.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.maegankullenda.carsonsunday.ui.auth.loginScreen
import com.maegankullenda.carsonsunday.ui.auth.registerScreen
import com.maegankullenda.carsonsunday.ui.events.createEventScreen
import com.maegankullenda.carsonsunday.ui.events.eventsScreen
import com.maegankullenda.carsonsunday.ui.events.respondentsScreen
import com.maegankullenda.carsonsunday.ui.notices.createNoticeScreen
import com.maegankullenda.carsonsunday.ui.notices.noticesScreen
import com.maegankullenda.carsonsunday.ui.welcome.welcomeScreen

@Composable
fun appNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
    ) {
        composable(Screen.Login.route) {
            loginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToWelcome = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Register.route) {
            registerScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToWelcome = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Welcome.route) {
            welcomeScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToEvents = {
                    navController.navigate(Screen.Events.route)
                },
                onNavigateToNotices = {
                    navController.navigate(Screen.Notices.route)
                },
            )
        }

        composable(Screen.Events.route) {
            eventsScreen(
                onNavigateToCreateEvent = {
                    navController.navigate(Screen.CreateEvent.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEventDetail = { eventId ->
                    navController.navigate(Screen.EventDetail.createRoute(eventId))
                },
            )
        }

        composable(Screen.CreateEvent.route) {
            createEventScreen(
                onEventCreated = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }

        composable(Screen.Notices.route) {
            noticesScreen(
                onNavigateToCreateNotice = {
                    navController.navigate(Screen.CreateNotice.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }

        composable(Screen.CreateNotice.route) {
            createNoticeScreen(
                onNoticeCreated = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }

        composable(
            route = Screen.EventDetail.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            com.maegankullenda.carsonsunday.ui.events.eventDetailScreen(
                eventId = eventId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditEvent = { editEventId ->
                    navController.navigate(Screen.EditEvent.createRoute(editEventId))
                },
                onNavigateToRespondents = { respondentsEventId ->
                    navController.navigate(Screen.Respondents.createRoute(respondentsEventId))
                },
            )
        }

        composable(
            route = Screen.EditEvent.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            com.maegankullenda.carsonsunday.ui.events.editEventScreen(
                eventId = eventId,
                onEventUpdated = {
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.Respondents.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            respondentsScreen(
                eventId = eventId,
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
