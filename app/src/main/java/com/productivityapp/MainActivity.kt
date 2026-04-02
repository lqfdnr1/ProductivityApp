package com.productivityapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.productivityapp.ui.navigation.Screen
import com.productivityapp.ui.navigation.bottomNavItems
import com.productivityapp.ui.screens.calendar.CalendarScreen
import com.productivityapp.ui.screens.guide.GuidedModeScreen
import com.productivityapp.ui.screens.guide.ProjectSetupScreen
import com.productivityapp.ui.screens.template.TemplateManagementScreen
import com.productivityapp.ui.screens.home.HomeScreen
import com.productivityapp.ui.screens.projects.ProjectDetailScreen
import com.productivityapp.ui.screens.projects.ProjectsScreen
import com.productivityapp.ui.screens.tasks.TaskEditScreen
import com.productivityapp.ui.screens.tasks.TasksScreen
import com.productivityapp.ui.screens.tasks.navigateToTaskEdit
import com.productivityapp.ui.theme.ProductivityAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProductivityAppTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Projects.route,
        Screen.Calendar.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(Screen.Home.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToProject = { projectId ->
                        navController.navigate(Screen.ProjectDetail.createRoute(projectId))
                    },
                    onNavigateToGuided = { projectId ->
                        navController.navigate(Screen.GuidedMode.createRoute(projectId))
                    }
                )
            }

            composable(Screen.Projects.route) {
                ProjectsScreen(
                    onNavigateToProject = { projectId ->
                        navController.navigate(Screen.ProjectDetail.createRoute(projectId))
                    },
                    onNavigateToProjectSetup = {
                        navController.navigate(Screen.ProjectSetup.route)
                    },
                    onNavigateToTemplateManagement = {
                        navController.navigate(Screen.TemplateManagement.route)
                    }
                )
            }

            composable(
                route = Screen.ProjectDetail.route,
                arguments = listOf(navArgument("projectId") { type = NavType.LongType })
            ) {
                ProjectDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPlan = { planId ->
                        navController.navigate(Screen.Tasks.createRoute(planId))
                    },
                    onNavigateToGuided = { projectId ->
                        navController.navigate(Screen.GuidedMode.createRoute(projectId))
                    }
                )
            }

            composable(
                route = Screen.Tasks.route,
                arguments = listOf(navArgument("planId") { type = NavType.LongType })
            ) { backStackEntry ->
                val planId = backStackEntry.arguments?.getLong("planId") ?: return@composable
                TasksScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToTaskEdit = { taskId ->
                        navController.navigate(navigateToTaskEdit(taskId, planId))
                    }
                )
            }

            composable(
                route = Screen.TaskEdit.route,
                arguments = listOf(
                    navArgument("taskId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument("planId") { type = NavType.LongType }
                )
            ) {
                TaskEditScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.GuidedMode.route,
                arguments = listOf(navArgument("projectId") { type = NavType.LongType })
            ) {
                GuidedModeScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Calendar.route) {
                CalendarScreen()
            }

            composable(Screen.ProjectSetup.route) {
                ProjectSetupScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onProjectCreated = { projectId ->
                        navController.popBackStack()
                        navController.navigate(Screen.ProjectDetail.createRoute(projectId))
                    }
                )
            }

            composable(Screen.TemplateManagement.route) {
                TemplateManagementScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
