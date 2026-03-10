package com.example.japanese_self_study_guide.hiragana_katakana.ui

import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.japanese_self_study_guide.hiragana_katakana.HiraganaGroupProvider
import com.example.japanese_self_study_guide.hiragana_katakana.HiraganaItem
import com.example.japanese_self_study_guide.hiragana_katakana.HiraganaRepository
import kotlinx.coroutines.tasks.await

@Composable
fun HiraganaNavGraph(
    onExit: () -> Unit,
    dailyMode: Boolean = false,
    dailyIds: List<Int>? = null
) {
    val nav = rememberNavController()

    var symbolMap by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    LaunchedEffect(Unit) {
        runCatching {
            val repo = HiraganaRepository()
            val symbols = repo.symbols.await()
            symbolMap = symbols.associate { it.id to (it.symbol ?: "") }
        }
    }

    val startDest = if (dailyMode && dailyIds != null) "hiragana_daily" else "hiragana_list"

    key(startDest) {
        NavHost(navController = nav, startDestination = startDest) {

            composable("hiragana_list") {
                HiraganaScreen(
                    onBack       = onExit,
                    onGoToGroups = { nav.navigate("hiragana_groups") }
                )
            }

            composable("hiragana_daily") {
                var symbols by remember { mutableStateOf<List<HiraganaItem>>(emptyList()) }
                var loading by remember { mutableStateOf(true) }

                LaunchedEffect(dailyIds) {
                    runCatching {
                        val repo = HiraganaRepository()
                        symbols = repo.getSymbolsByIds(dailyIds!!).await()
                    }
                    loading = false
                }

                HiraganaDailyScreen(
                    dailyIds       = dailyIds ?: emptyList(),
                    symbols        = symbols,
                    isLoading      = loading,
                    onBack         = onExit,
                    onStartExercises = {
                        nav.navigate("hiragana_exercise/daily")
                    }
                )
            }

            composable("hiragana_groups") {
                val groups5 = HiraganaGroupProvider.GROUPS_ALL.filter { it[0] < 72 }.toTypedArray()
                val groups3 = HiraganaGroupProvider.GROUPS_ALL.filter { it[0] >= 72 }.toTypedArray()

                HiraganaGroupsScreen(
                    symbolMap    = symbolMap,
                    onGroupClick = { group ->
                        nav.navigate("hiragana_exercise/${group.joinToString(",")}")
                    },
                    onAllRandom  = { nav.navigate("hiragana_exercise/all_random") },
                    onBack       = { nav.popBackStack() }
                )
            }

            composable(
                route = "hiragana_exercise/{param}",
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

                HiraganaExerciseScreen(
                    groupIds   = groupIds,
                    dailyMode  = isDaily,
                    allRandom  = isAllRandom,
                    onBack     = { nav.popBackStack() },
                    onFinished = { correct, total, learnedNow, totalSymbols ->
                        nav.navigate("hiragana_finish/$correct/$total/$learnedNow/$totalSymbols") {
                            popUpTo("hiragana_list")
                        }
                    }
                )
            }

           composable(
                route = "hiragana_finish/{correct}/{total}/{learnedNow}/{totalSymbols}",
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
                        nav.navigate("hiragana_list") {
                            popUpTo("hiragana_list") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}