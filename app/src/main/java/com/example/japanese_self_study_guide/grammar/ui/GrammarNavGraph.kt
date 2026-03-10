package com.example.japanese_self_study_guide.grammar.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun GrammarNavGraph(
    onExit: () -> Unit = {},
    startGrammarId: Int? = null
) {
    val nav = rememberNavController()

    val startDest = if (startGrammarId != null) "grammar_daily" else "grammar_list"

    NavHost(navController = nav, startDestination = startDest) {

        composable("grammar_list") {
            GrammarScreen(
                onBack = onExit,
                onRuleClick = { rule ->
                    nav.navigate(
                        "grammar_detail/${rule.id}/${rule.structure.enc()}/${rule.explanation.enc()}/${rule.example.enc()}/${rule.translation.enc()}"
                    )
                }
            )
        }

        composable(route = "grammar_daily") {
            GrammarDetailScreen(
                grammarId   = startGrammarId!!,
                structure   = "",
                explanation = "",
                example     = "",
                translation = "",
                dailyMode   = true,
                onBack      = onExit,
                onGoToExercises = { id -> nav.navigate("grammar_exercise/$id") }
            )
        }

        composable(
            route = "grammar_detail/{grammarId}/{structure}/{explanation}/{example}/{translation}",
            arguments = listOf(
                navArgument("grammarId")   { type = NavType.IntType },
                navArgument("structure")   { type = NavType.StringType },
                navArgument("explanation") { type = NavType.StringType },
                navArgument("example")     { type = NavType.StringType },
                navArgument("translation") { type = NavType.StringType }
            )
        ) { back ->
            val args = back.arguments!!
            GrammarDetailScreen(
                grammarId   = args.getInt("grammarId"),
                structure   = args.getString("structure", "").dec(),
                explanation = args.getString("explanation", "").dec(),
                example     = args.getString("example", "").dec(),
                translation = args.getString("translation", "").dec(),
                dailyMode   = false,
                onBack      = { nav.popBackStack() },
                onGoToExercises = { id -> nav.navigate("grammar_exercise/$id") }
            )
        }

        composable(
            route = "grammar_exercise/{grammarId}",
            arguments = listOf(navArgument("grammarId") { type = NavType.IntType })
        ) { back ->
            val grammarId = back.arguments!!.getInt("grammarId")
            GrammarExerciseScreen(
                grammarId = grammarId,
                onBack = { nav.popBackStack() },
                onFinished = { correct, total ->
                    nav.navigate("grammar_finish/$correct/$total") {
                        popUpTo("grammar_list")
                    }
                }
            )
        }

        composable(
            route = "grammar_finish/{correct}/{total}",
            arguments = listOf(
                navArgument("correct") { type = NavType.IntType },
                navArgument("total")   { type = NavType.IntType }
            )
        ) { back ->
            val correct = back.arguments!!.getInt("correct")
            val total   = back.arguments!!.getInt("total")
            GrammarExerciseFinishScreen(
                correct = correct,
                total   = total,
                onBackToList = {
                    nav.navigate("grammar_list") {
                        popUpTo("grammar_list") { inclusive = true }
                    }
                }
            )
        }
    }
}

private fun String.enc() = java.net.URLEncoder.encode(this, "UTF-8")
private fun String.dec() = java.net.URLDecoder.decode(this, "UTF-8")