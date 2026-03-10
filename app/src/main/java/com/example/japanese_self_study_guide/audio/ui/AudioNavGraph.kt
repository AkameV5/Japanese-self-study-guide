package com.example.japanese_self_study_guide.audio.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.japanese_self_study_guide.audio.AudioActivity

@Composable
fun AudioNavGraph(
    onExit: () -> Unit = {},
    dailyMode: Boolean = false
) {
    val navController = rememberNavController()

    val startDest = if (dailyMode && AudioActivity.pendingDailyAudioId != null) {
        "audio_player/${AudioActivity.pendingDailyAudioId}"
    } else {
        "audio_list"
    }

    NavHost(navController = navController, startDestination = startDest) {

        composable("audio_list") {
            AudioScreen(
                onAudioClick = { audio ->
                    navController.navigate(
                        "audio_player_full/${audio.id}/${audio.url.enc()}/${audio.name.enc()}/${audio.description.enc()}"
                    )
                }
            )
        }

        composable(
            route = "audio_player/{audioId}",
            arguments = listOf(navArgument("audioId") { type = NavType.IntType })
        ) { back ->
            val audioId = back.arguments!!.getInt("audioId")
            AudioPlayerScreen(
                audioId          = audioId,
                audioUrl         = AudioActivity.pendingDailyAudioUrl,
                audioName        = AudioActivity.pendingDailyAudioName,
                audioDescription = AudioActivity.pendingDailyAudioDescription,
                onBack           = onExit,
                onGoToExercises  = { id -> navController.navigate("audio_exercise/$id") }
            )
        }

        composable(
            route = "audio_player_full/{audioId}/{url}/{name}/{description}",
            arguments = listOf(
                navArgument("audioId")     { type = NavType.StringType },
                navArgument("url")         { type = NavType.StringType },
                navArgument("name")        { type = NavType.StringType },
                navArgument("description") { type = NavType.StringType }
            )
        ) { back ->
            val args = back.arguments!!
            AudioPlayerScreen(
                audioId          = args.getString("audioId", "0").toIntOrNull() ?: 0,
                audioUrl         = args.getString("url", "").dec(),
                audioName        = args.getString("name", "").dec(),
                audioDescription = args.getString("description", "").dec(),
                onBack           = { navController.popBackStack() },
                onGoToExercises  = { id -> navController.navigate("audio_exercise/$id") }
            )
        }

        composable(
            route = "audio_exercise/{audioId}",
            arguments = listOf(navArgument("audioId") { type = NavType.IntType })
        ) { back ->
            AudioExerciseScreen(
                audioId    = back.arguments!!.getInt("audioId"),
                onBack     = { navController.popBackStack() },
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
                navArgument("total")   { type = NavType.IntType }
            )
        ) { back ->
            AudioExerciseFinishScreen(
                correct = back.arguments!!.getInt("correct"),
                total   = back.arguments!!.getInt("total"),
                onBack  = {
                    navController.navigate("audio_list") {
                        popUpTo("audio_list") { inclusive = true }
                    }
                }
            )
        }
    }
}

private fun String.enc(): String = java.net.URLEncoder.encode(this, "UTF-8")
private fun String.dec(): String = java.net.URLDecoder.decode(this, "UTF-8")