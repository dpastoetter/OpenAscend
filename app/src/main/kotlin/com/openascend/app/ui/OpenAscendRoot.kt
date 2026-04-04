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
import androidx.navigation.navDeepLink
import com.openascend.app.navigation.Routes
import com.openascend.app.ui.bootstrap.BootstrapViewModel
import com.openascend.app.ui.boss.BossRitualScreen
import com.openascend.app.ui.character.CharacterScreen
import com.openascend.app.ui.companion.CompanionPlayScreen
import com.openascend.app.ui.checkin.CheckInScreen
import com.openascend.app.ui.habits.HabitEditScreen
import com.openascend.app.ui.habits.HabitsScreen
import com.openascend.app.ui.home.HomeScreen
import com.openascend.app.ui.onboarding.OnboardingScreen
import com.openascend.app.ui.settings.SettingsScreen
import com.openascend.app.ui.sigil.SealSigilScreen
import com.openascend.app.ui.weekly.WeeklyReviewScreen

private val deepLinkBase = "openascend://"

@Composable
fun OpenAscendRoot(
    initialDeepLinkRoute: String? = null,
) {
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
                val dest = if (route == Routes.Home && initialDeepLinkRoute != null) {
                    initialDeepLinkRoute
                } else {
                    route
                }
                navController.navigate(dest) {
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
        composable(
            Routes.Home,
            deepLinks = listOf(navDeepLink { uriPattern = "${deepLinkBase}home" }),
        ) {
            HomeScreen(
                onOpenCharacter = { navController.navigate(Routes.Character) },
                onOpenHabits = { navController.navigate(Routes.Habits) },
                onOpenCheckIn = { navController.navigate(Routes.CheckIn) },
                onOpenWeekly = { navController.navigate(Routes.Weekly) },
                onOpenSettings = { navController.navigate(Routes.Settings) },
                onOpenBossRitual = { navController.navigate(Routes.BossRitual) },
                onOpenCompanionPlay = { navController.navigate(Routes.CompanionPlay) },
            )
        }
        composable(
            Routes.CompanionPlay,
            deepLinks = listOf(
                navDeepLink { uriPattern = "${deepLinkBase}companion" },
                navDeepLink { uriPattern = "${deepLinkBase}companion_play" },
            ),
        ) {
            CompanionPlayScreen(onBack = { navController.popBackStack() })
        }
        composable(
            Routes.Character,
            deepLinks = listOf(navDeepLink { uriPattern = "${deepLinkBase}character" }),
        ) {
            CharacterScreen(onBack = { navController.popBackStack() })
        }
        composable(
            Routes.Habits,
            deepLinks = listOf(navDeepLink { uriPattern = "${deepLinkBase}habits" }),
        ) {
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
        composable(
            Routes.CheckIn,
            deepLinks = listOf(
                navDeepLink { uriPattern = "${deepLinkBase}check_in" },
                navDeepLink { uriPattern = "${deepLinkBase}checkin" },
            ),
        ) {
            CheckInScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
                onNavigateToSigil = {
                    navController.navigate(Routes.SealSigil) {
                        popUpTo(Routes.CheckIn) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.SealSigil) {
            SealSigilScreen(
                onFinished = {
                    navController.popBackStack(Routes.Home, inclusive = false)
                },
            )
        }
        composable(
            Routes.Weekly,
            deepLinks = listOf(navDeepLink { uriPattern = "${deepLinkBase}weekly" }),
        ) {
            WeeklyReviewScreen(
                onBack = { navController.popBackStack() },
                onOpenBossRitual = { navController.navigate(Routes.BossRitual) },
            )
        }
        composable(
            Routes.Settings,
            deepLinks = listOf(navDeepLink { uriPattern = "${deepLinkBase}settings" }),
        ) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(
            Routes.BossRitual,
            deepLinks = listOf(navDeepLink { uriPattern = "${deepLinkBase}boss" }),
        ) {
            BossRitualScreen(
                onBack = { navController.popBackStack() },
                onOpenWeekly = {
                    navController.navigate(Routes.Weekly) {
                        popUpTo(Routes.Home) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }
    }
}
