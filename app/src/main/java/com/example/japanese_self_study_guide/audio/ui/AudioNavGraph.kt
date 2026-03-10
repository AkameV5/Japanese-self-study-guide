package com.example.japanese_self_study_guide.audio.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun AudioNavGraph(
    onExit: () -> Unit = {},
    startAudioId: Int? = null,
    startAudioUrl: String = "",
    startAudioName: String = "",
    startAudioDescription: String = ""
) {
    val navController = rememberNavController()

    val startDest = if (startAudioId != null) {
        "audio_player/$startAudioId/${startAudioUrl.encode()}/${startAudioName.encode()}/${startAudioDescription.encode()}"
    } else {
        "audio_list"
    }

    NavHost(navController = navController, startDestination = startDest) {

        composable("audio_list") {
            AudioScreen(
                onAudioClick = { audio ->
                    navController.navigate(
                        "audio_player/${audio.id}/${audio.url.encode()}/${audio.name.encode()}/${audio.description.encode()}"
                    )
                }
            )
        }

        composable(
            route = "audio_player/{audioId}/{url}/{name}/{description}",
            arguments = listOf(
                navArgument("audioId") { type = NavType.IntType },
                navArgument("url") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType },
                navArgument("description") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val audioId = backStackEntry.arguments!!.getInt("audioId")
            val url = backStackEntry.arguments!!.getString("url", "").decode()
            val name = backStackEntry.arguments!!.getString("name", "").decode()
            val description = backStackEntry.arguments!!.getString("description", "").decode()

            AudioPlayerScreen(
                audioId = audioId,
                audioUrl = url,
                audioName = name,
                audioDescription = description,
                onBack = { navController.popBackStack() },
                onGoToExercises = { id ->
                    navController.navigate("audio_exercise/$id")
                }
            )
        }

        composable(
            route = "audio_exercise/{audioId}",
            arguments = listOf(navArgument("audioId") { type = NavType.IntType })
        ) { backStackEntry ->
            val audioId = backStackEntry.arguments!!.getInt("audioId")

            AudioExerciseScreen(
                audioId = audioId,
                onBack = { navController.popBackStack() },
                onFinished = { correct, total ->
                    navController.navigate("audio_exercise_finish/$correct/$total") {
                        popUpTo("audio_list")
                    }
                }
            )
        }

        composable(
            route = "audio_exercise_finish/{correct}/{total}",
            arguments = listOf(
                navArgument("correct") { type = NavType.IntType },
                navArgument("total") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val correct = backStackEntry.arguments!!.getInt("correct")
            val total = backStackEntry.arguments!!.getInt("total")

            AudioExerciseFinishScreen(
                correct = correct,
                total = total,
                onBack = {
                    navController.navigate("audio_list") {
                        popUpTo("audio_list") { inclusive = true }
                    }
                }
            )
        }
    }
}

private fun String.encode(): String =
    java.net.URLEncoder.encode(this, "UTF-8")

private fun String.decode(): String =
    java.net.URLDecoder.decode(this, "UTF-8")