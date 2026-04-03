package com.openascend.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.openascend.app.navigation.Routes
import com.openascend.app.ui.bootstrap.BootstrapViewModel
import com.openascend.app.ui.character.CharacterScreen
import com.openascend.app.ui.checkin.CheckInScreen
import com.openascend.app.ui.habits.HabitEditScreen
import com.openascend.app.ui.habits.HabitsScreen
import com.openascend.app.ui.home.HomeScreen
import com.openascend.app.ui.onboarding.OnboardingScreen
import com.openascend.app.ui.settings.SettingsScreen
import com.openascend.app.ui.weekly.WeeklyReviewScreen

@Composable
fun OpenAscendRoot() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Routes.Bootstrap,
    ) {
        composable(Routes.Bootstrap) {
            val vm: BootstrapViewModel = hiltViewModel()
            val target by vm.targetRoute.collectAsState()
            LaunchedEffect(target) {
                val route = target ?: return@LaunchedEffect
                navController.navigate(route) {
                    popUpTo(Routes.Bootstrap) { inclusive = true }
                }
            }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        composable(Routes.Onboarding) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Onboarding) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.Home) {
            HomeScreen(
                onOpenCharacter = { navController.navigate(Routes.Character) },
                onOpenHabits = { navController.navigate(Routes.Habits) },
                onOpenCheckIn = { navController.navigate(Routes.CheckIn) },
                onOpenWeekly = { navController.navigate(Routes.Weekly) },
                onOpenSettings = { navController.navigate(Routes.Settings) },
            )
        }
        composable(Routes.Character) {
            CharacterScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.Habits) {
            HabitsScreen(
                onBack = { navController.popBackStack() },
                onEditHabit = { id -> navController.navigate(Routes.habitEdit(id)) },
            )
        }
        composable(
            route = Routes.HabitEdit,
            arguments = listOf(navArgument("habitId") { type = NavType.LongType }),
        ) { backStackEntry ->
            HabitEditScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
                viewModel = hiltViewModel(backStackEntry),
            )
        }
        composable(Routes.CheckIn) {
            CheckInScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
        composable(Routes.Weekly) {
            WeeklyReviewScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.Settings) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
