package com.example.myapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.data.repository.CategoryRepository
import com.example.myapplication.data.repository.PinRepository
import com.example.myapplication.data.repository.TaskRepository
import com.example.myapplication.ui.screen.categories.CategoriesScreen
import com.example.myapplication.ui.screen.categories.CategoriesViewModel
import com.example.myapplication.ui.screen.pinlock.PinLockScreen
import com.example.myapplication.ui.screen.pinlock.PinLockViewModel
import com.example.myapplication.ui.screen.search.SearchScreen
import com.example.myapplication.ui.screen.search.SearchViewModel
import com.example.myapplication.ui.screen.settings.SettingsScreen
import com.example.myapplication.ui.screen.settings.SettingsViewModel
import com.example.myapplication.ui.screen.taskdetail.TaskDetailScreen
import com.example.myapplication.ui.screen.taskdetail.TaskDetailViewModel
import com.example.myapplication.ui.screen.tasklist.TaskListScreen
import com.example.myapplication.ui.screen.tasklist.TaskListViewModel

object Routes {
    const val PIN_LOCK = "pinlock"
    const val TASK_LIST = "tasks"
    const val TASK_DETAIL = "task/{taskId}?parentTaskId={parentTaskId}"
    const val CATEGORIES = "categories"
    const val SETTINGS = "settings"
    const val SEARCH = "search"

    fun taskDetail(taskId: Long, parentTaskId: Long? = null): String {
        val base = "task/$taskId"
        return if (parentTaskId != null) "$base?parentTaskId=$parentTaskId" else base
    }
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    pinRepo: PinRepository,
    taskRepo: TaskRepository,
    catRepo: CategoryRepository,
) {
    NavHost(navController = navController, startDestination = Routes.PIN_LOCK) {
        composable(Routes.PIN_LOCK) {
            val vm: PinLockViewModel = viewModel(factory = PinLockViewModel.Factory(pinRepo))
            PinLockScreen(
                viewModel = vm,
                onUnlocked = { navController.navigate(Routes.TASK_LIST) { popUpTo(Routes.PIN_LOCK) { inclusive = true } } }
            )
        }

        composable(Routes.TASK_LIST) {
            val vm: TaskListViewModel = viewModel(factory = TaskListViewModel.Factory(taskRepo, catRepo))
            TaskListScreen(
                viewModel = vm,
                onTaskClick = { id -> navController.navigate(Routes.taskDetail(id)) },
                onNewTask = { navController.navigate(Routes.taskDetail(0)) },
                onSearch = { navController.navigate(Routes.SEARCH) },
                onCategories = { navController.navigate(Routes.CATEGORIES) },
                onSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }

        composable(
            route = Routes.TASK_DETAIL,
            arguments = listOf(
                navArgument("taskId") { type = NavType.LongType },
                navArgument("parentTaskId") { type = NavType.LongType; defaultValue = -1L },
            ),
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getLong("taskId") ?: 0L
            val rawParentId = backStackEntry.arguments?.getLong("parentTaskId") ?: -1L
            val parentTaskId = if (rawParentId == -1L) null else rawParentId
            val vm: TaskDetailViewModel = viewModel(
                factory = TaskDetailViewModel.Factory(taskId, parentTaskId, taskRepo, catRepo)
            )
            TaskDetailScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onSubtaskClick = { id -> navController.navigate(Routes.taskDetail(id)) },
            )
        }

        composable(Routes.CATEGORIES) {
            val vm: CategoriesViewModel = viewModel(factory = CategoriesViewModel.Factory(catRepo))
            CategoriesScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(pinRepo))
            SettingsScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.SEARCH) {
            val vm: SearchViewModel = viewModel(factory = SearchViewModel.Factory(taskRepo, catRepo))
            SearchScreen(viewModel = vm, onBack = { navController.popBackStack() }, onTaskClick = { id -> navController.navigate(Routes.taskDetail(id)) })
        }
    }
}
