package com.maegankullenda.carsonsunday.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Welcome : Screen("welcome")
    object Events : Screen("events")
    object CreateEvent : Screen("create_event")
    object EditEvent : Screen("edit_event/{eventId}") {
        fun createRoute(eventId: String) = "edit_event/$eventId"
    }
    object Notices : Screen("notices")
    object CreateNotice : Screen("create_notice")
    object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
    object Respondents : Screen("respondents/{eventId}") {
        fun createRoute(eventId: String) = "respondents/$eventId"
    }
    object Settings : Screen("settings")
}
