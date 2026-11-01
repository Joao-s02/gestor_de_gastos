package com.joao.gastos.ui

import android.provider.Settings
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.joao.gastos.data.ExpenseRepository
import com.joao.gastos.ui.form.ExpenseFormScreen
import com.joao.gastos.ui.form.ExpenseFormViewModel
import com.joao.gastos.ui.list.ExpenseListScreen
import com.joao.gastos.ui.list.ExpenseListViewModel

@Composable
fun GastosAppRoot(repository: ExpenseRepository) {
    val navController = rememberNavController()
    val reduceMotion = prefersReducedMotion()
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    NavHost(
        navController = navController,
        startDestination = LIST,
        modifier = Modifier.widthIn(max = 600.dp).fillMaxSize(),
        enterTransition = { if (reduceMotion) EnterTransition.None else fadeIn(tween(200)) + slideInHorizontally(tween(200)) { it / 5 } },
        exitTransition = { if (reduceMotion) ExitTransition.None else fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { -it / 5 } },
        popEnterTransition = { if (reduceMotion) EnterTransition.None else fadeIn(tween(200)) + slideInHorizontally(tween(200)) { -it / 5 } },
        popExitTransition = { if (reduceMotion) ExitTransition.None else fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { it / 5 } },
    ) {
        composable(LIST) {
            val viewModel = viewModel<ExpenseListViewModel>(
                factory = viewModelFactory {
                    initializer {
                        ExpenseListViewModel(repository, createSavedStateHandle())
                    }
                },
            )
            val justSaved = it.savedStateHandle.get<Boolean>(SAVED) == true
            ExpenseListScreen(
                viewModel = viewModel,
                justSaved = justSaved,
                onSavedMessageShown = { it.savedStateHandle[SAVED] = false },
                onAdd = {
                    navController.navigate(FORM) { launchSingleTop = true }
                },
                onEdit = { id ->
                    navController.navigate("form/$id") { launchSingleTop = true }
                },
            )
        }
        composable(FORM) {
            FormDestination(
                repository = repository,
                expenseId = null,
                onSaved = { markSavedAndClose(navController) },
                onCancel = { navController.popBackStack() },
            )
        }
        composable(
            FORM_WITH_ID,
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
        ) { entry ->
            FormDestination(
                repository = repository,
                expenseId = entry.arguments?.getLong("id"),
                onSaved = { markSavedAndClose(navController) },
                onCancel = { navController.popBackStack() },
            )
        }
    }
    }
}

@Composable
private fun prefersReducedMotion(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
}

private fun markSavedAndClose(navController: NavHostController) {
    navController.previousBackStackEntry?.savedStateHandle?.set(SAVED, true)
    navController.popBackStack()
}

@Composable
private fun FormDestination(
    repository: ExpenseRepository,
    expenseId: Long?,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
) {
    val viewModel = viewModel<ExpenseFormViewModel>(
        factory = viewModelFactory {
            initializer {
                ExpenseFormViewModel(repository, expenseId, createSavedStateHandle())
            }
        },
    )
    LaunchedEffect(viewModel) {
        viewModel.saved.collect { onSaved() }
    }
    ExpenseFormScreen(viewModel = viewModel, onCancel = onCancel)
}

private const val LIST = "list"
private const val FORM = "form"
private const val FORM_WITH_ID = "form/{id}"
private const val SAVED = "saved"
