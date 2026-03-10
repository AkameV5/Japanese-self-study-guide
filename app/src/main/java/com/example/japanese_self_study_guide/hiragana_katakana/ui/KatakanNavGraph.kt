package com.example.japanese_self_study_guide.hiragana_katakana.ui

import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.japanese_self_study_guide.hiragana_katakana.HiraganaItem
import com.example.japanese_self_study_guide.hiragana_katakana.KatakanaGroupProvider
import com.example.japanese_self_study_guide.hiragana_katakana.KatakanaRepository
import kotlinx.coroutines.tasks.await

@Composable
fun KatakanaNavGraph(
    onExit: () -> Unit,
    dailyMode: Boolean = false,
    dailyIds: List<Int>? = null
) {
    val nav = rememberNavController()

    var symbolMap by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    LaunchedEffect(Unit) {
        runCatching {
            val repo = KatakanaRepository()
            val symbols = repo.symbols.await()
            symbolMap = symbols.associate { it.id to (it.symbol ?: "") }
        }
    }

    val startDest = if (dailyMode && dailyIds != null) "katakana_daily" else "katakana_list"

    key(startDest) {
        NavHost(navController = nav, startDestination = startDest) {

            composable("katakana_list") {
                KatakanaScreen(
                    onBack       = onExit,
                    onGoToGroups = { nav.navigate("katakana_groups") }
                )
            }

            composable("katakana_daily") {
                var symbols by remember { mutableStateOf<List<HiraganaItem>>(emptyList()) }
                var loading by remember { mutableStateOf(true) }

                LaunchedEffect(dailyIds) {
                    runCatching {
                        val repo = KatakanaRepository()
                        symbols = repo.getSymbolsByIds(dailyIds!!).await()
                    }
                    loading = false
                }

                KatakanaDailyScreen(
                    symbols          = symbols,
                    isLoading        = loading,
                    onBack           = onExit,
                    onStartExercises = { nav.navigate("katakana_exercise/daily") }
                )
            }

            composable("katakana_groups") {
                val groups5 = KatakanaGroupProvider.GROUPS_ALL.filter { it[0] < 72 }.toTypedArray()
                val groups3 = KatakanaGroupProvider.GROUPS_ALL.filter { it[0] >= 72 }.toTypedArray()

                KatakanaGroupsScreen(
                    symbolMap    = symbolMap,
                    onGroupClick = { group ->
                        nav.navigate("katakana_exercise/${group.joinToString(",")}")
                    },
                    onAllRandom  = { nav.navigate("katakana_exercise/all_random") },
                    onBack       = { nav.popBackStack() }
                )
            }

            composable(
                route = "katakana_exercise/{param}",
                arguments = listOf(navArgument("param") { type = NavType.StringType })
            ) { back ->
                val param = back.arguments!!.getString("param", "")

                val (groupIds, isDaily, isAllRandom) = remember(param) {
                    when {
                        param == "all_random" -> Triple(emptyList(), false, true)
                        param == "daily"      -> Triple(dailyIds ?: emptyList(), true, false)
                        else                  -> Triple(
                            param.split(",").mapNotNull { it.toIntOrNull() },
                            false, false
                        )
                    }
                }

                KatakanaExerciseScreen(
                    groupIds   = groupIds,
                    dailyMode  = isDaily,
                    allRandom  = isAllRandom,
                    onBack     = { nav.popBackStack() },
                    onFinished = { correct, total, learnedNow, totalSymbols ->
                        nav.navigate("katakana_finish/$correct/$total/$learnedNow/$totalSymbols") {
                            popUpTo("katakana_list")
                        }
                    }
                )
            }

            composable(
                route = "katakana_finish/{correct}/{total}/{learnedNow}/{totalSymbols}",
                arguments = listOf(
                    navArgument("correct")      { type = NavType.IntType },
                    navArgument("total")        { type = NavType.IntType },
                    navArgument("learnedNow")   { type = NavType.IntType },
                    navArgument("totalSymbols") { type = NavType.IntType }
                )
            ) { back ->
                val args = back.arguments!!
                ExerciseFinishScreen(
                    correct      = args.getInt("correct"),
                    total        = args.getInt("total"),
                    learnedNow   = args.getInt("learnedNow"),
                    totalSymbols = args.getInt("totalSymbols"),
                    onBackToList = {
                        nav.navigate("katakana_list") {
                            popUpTo("katakana_list") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}