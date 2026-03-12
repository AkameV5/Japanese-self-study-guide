package com.example.japanese_self_study_guide.kanji.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun KanjiNavGraph(
    onExit: () -> Unit,
    dailyMode: Boolean = false,
    dailyIds: IntArray? = null,
    dailyStartId: Int = 0,
    dailyEndId: Int = 0,
    dailyLimit: Int = 999
) {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = "kanji_list") {

        composable("kanji_list") {
            KanjiScreen(
                dailyMode = dailyMode,
                dailyIds = dailyIds,
                onBack = onExit,
                onGoToExercises = { _, _, _ ->
                    // daily mode — use parameters from Activity
                    nav.navigate("kanji_exercise/$dailyStartId/$dailyEndId/$dailyLimit/true")
                },
                onGoToExerciseGroups = {
                    nav.navigate("kanji_groups")
                }
            )
        }

        composable("kanji_groups") {
            KanjiExerciseGroupsScreen(
                onBack = { nav.popBackStack() },
                onGroupClick = { group ->
                    nav.navigate("kanji_exercise/${group.startId}/${group.endId}/${group.limit}/false")
                }
            )
        }

        composable(
            route = "kanji_exercise/{startId}/{endId}/{limit}/{daily}",
            arguments = listOf(
                navArgument("startId") { type = NavType.IntType },
                navArgument("endId")   { type = NavType.IntType },
                navArgument("limit")   { type = NavType.IntType },
                navArgument("daily")   { type = NavType.BoolType }
            )
        ) { back ->
            val args = back.arguments!!
            KanjiExerciseScreen(
                startId    = args.getInt("startId"),
                endId      = args.getInt("endId"),
                limit      = args.getInt("limit"),
                dailyMode  = args.getBoolean("daily"),
                onBack     = { nav.popBackStack() },
                onFinished = { correct, total, learned, totalKanji ->
                    nav.navigate("kanji_finish/$correct/$total/$learned/$totalKanji") {
                        popUpTo("kanji_list")
                    }
                }
            )
        }

        composable(
            route = "kanji_finish/{correct}/{total}/{learned}/{totalKanji}",
            arguments = listOf(
                navArgument("correct")    { type = NavType.IntType },
                navArgument("total")      { type = NavType.IntType },
                navArgument("learned")    { type = NavType.IntType },
                navArgument("totalKanji") { type = NavType.IntType }
            )
        ) { back ->
            val args = back.arguments!!
            KanjiExerciseFinishScreen(
                correct    = args.getInt("correct"),
                total      = args.getInt("total"),
                learned    = args.getInt("learned"),
                totalKanji = args.getInt("totalKanji"),
                onBackToList = {
                    nav.navigate("kanji_list") {
                        popUpTo("kanji_list") { inclusive = true }
                    }
                }
            )
        }
    }
}