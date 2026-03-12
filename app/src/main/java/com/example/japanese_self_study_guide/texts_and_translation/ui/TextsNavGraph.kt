package com.example.japanese_self_study_guide.texts_and_translation.ui

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.japanese_self_study_guide.texts_and_translation.ExerciseModel
import com.example.japanese_self_study_guide.texts_and_translation.view_model.TextDetailViewModel

@Composable
fun TextsNavGraph(
    startTextId: Int = -1,
    onBack: () -> Unit
) {
    val navController = rememberNavController()
    val exerciseHolder = remember { mutableStateOf<List<ExerciseModel>>(emptyList()) }

    val startDest = if (startTextId > 0) "daily_text" else "texts_list"

    NavHost(navController = navController, startDestination = startDest) {

        composable("texts_list") {
            TextsScreen(
                onBack = onBack,
                onTextClick = { textId ->
                    navController.navigate("text_detail/$textId")
                }
            )
        }

        composable("daily_text") {
            Log.d("TextsNavGraph", "Daily mode startTextId=$startTextId")
            val vm: TextDetailViewModel = viewModel()
            TextDetailScreen(
                textId = startTextId,
                vm = vm,
                onBack = onBack,
                onGoToExercises = { id ->
                    exerciseHolder.value = vm.uiState.value.exercises
                    navController.navigate("exercise/$id")
                }
            )
        }

        composable(
            route = "text_detail/{textId}",
            arguments = listOf(navArgument("textId") { type = NavType.IntType })
        ) { backStack ->
            val textId = backStack.arguments!!.getInt("textId")
            Log.d("TextsNavGraph", "text_detail textId=$textId")
            val vm: TextDetailViewModel = viewModel(key = textId.toString())
            TextDetailScreen(
                textId = textId,
                vm = vm,
                onBack = {
                    if (!navController.popBackStack()) onBack()
                },
                onGoToExercises = { id ->
                    exerciseHolder.value = vm.uiState.value.exercises
                    navController.navigate("exercise/$id")
                }
            )
        }

        composable(
            route = "exercise/{textId}",
            arguments = listOf(navArgument("textId") { type = NavType.IntType })
        ) { backStack ->
            val textId = backStack.arguments!!.getInt("textId")
            ExerciseScreen(
                textId = textId,
                exercises = exerciseHolder.value,
                onBack = { navController.popBackStack() },
                onFinished = { correct, total ->
                    navController.navigate("exercise_finished/$textId/$correct/$total") {
                        popUpTo("text_detail/$textId")
                    }
                },
                onFailedLowScore = { navController.popBackStack() }
            )
        }

        composable(
            route = "exercise_finished/{textId}/{correct}/{total}",
            arguments = listOf(
                navArgument("textId") { type = NavType.IntType },
                navArgument("correct") { type = NavType.IntType },
                navArgument("total") { type = NavType.IntType }
            )
        ) { backStack ->
            val textId = backStack.arguments!!.getInt("textId")
            val correct = backStack.arguments!!.getInt("correct")
            val total = backStack.arguments!!.getInt("total")
            ExerciseFinishedScreen(
                correct = correct,
                total = total,
                onBackToText = {
                    navController.navigate("text_detail/$textId") {
                        popUpTo("text_detail/$textId") { inclusive = true }
                    }
                }
            )
        }
    }
}