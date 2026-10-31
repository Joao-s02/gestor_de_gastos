package com.joao.gastos.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    NavHost(navController = navController, startDestination = LIST) {
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
                onAdd = { navController.navigate(FORM) },
                onEdit = { id -> navController.navigate("form/$id") },
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
