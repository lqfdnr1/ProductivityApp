package com.productivityapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Projects : Screen("projects")
    object ProjectDetail : Screen("project/{projectId}") {
        fun createRoute(projectId: Long) = "project/$projectId"
    }
    object Plans : Screen("plans/{projectId}") {
        fun createRoute(projectId: Long) = "plans/$projectId"
    }
    object PlanDetail : Screen("plan/{planId}") {
        fun createRoute(planId: Long) = "plan/$planId"
    }
    object Tasks : Screen("tasks/{planId}") {
        fun createRoute(planId: Long) = "tasks/$planId"
    }
    object TaskEdit : Screen("task_edit?taskId={taskId}&planId={planId}") {
        fun createRoute(taskId: Long? = null, planId: Long) =
            "task_edit?taskId=${taskId ?: -1}&planId=$planId"
    }
    object GuidedMode : Screen("guided/{projectId}") {
        fun createRoute(projectId: Long) = "guided/$projectId"
    }
    object Calendar : Screen("calendar")
    object ProjectSetup : Screen("project_setup")
}

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home.route, Icons.Default.Home, "Home"),
    BottomNavItem(Screen.Projects.route, Icons.Default.Folder, "Projects"),
    BottomNavItem(Screen.Calendar.route, Icons.Default.CalendarMonth, "Calendar")
)
