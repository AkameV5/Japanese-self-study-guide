package com.example.japanese_self_study_guide.hiragana_katakana.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.japanese_self_study_guide.R
import com.example.japanese_self_study_guide.hiragana_katakana.HiraganaGroupProvider
import com.example.japanese_self_study_guide.hiragana_katakana.KatakanaGroupProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    title: String,
    symbolMap: Map<Int, String>,
    groups5: Array<IntArray>,
    groups3: Array<IntArray>,
    onGroupClick: (IntArray) -> Unit,
    onAllRandom: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                SectionTitle(stringResource(R.string.groups_main_title))
            }
            items(groups5.toList()) { group ->
                GroupButton(label = groupLabel(group, symbolMap), onClick = { onGroupClick(group) })
            }
            item {
                SectionTitle(stringResource(R.string.groups_youon_title))
            }
            items(groups3.toList()) { group ->
                GroupButton(label = groupLabel(group, symbolMap), onClick = { onGroupClick(group) })
            }
            item {
                SectionTitle(stringResource(R.string.groups_training_title))
            }
            item {
                GroupButton(
                    label = stringResource(R.string.groups_all_random),
                    onClick = onAllRandom
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 20.sp,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun GroupButton(label: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(label, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

private fun groupLabel(ids: IntArray, symbolMap: Map<Int, String>): String =
    ids.toList().mapNotNull { symbolMap[it] }.joinToString("・")

@Composable
fun HiraganaGroupsScreen(
    symbolMap: Map<Int, String>,
    onGroupClick: (IntArray) -> Unit,
    onAllRandom: () -> Unit,
    onBack: () -> Unit
) {
    val groups5 = HiraganaGroupProvider.GROUPS_ALL.filter { it.size >= 3 && it[0] < 72 }.toTypedArray()
    val groups3 = HiraganaGroupProvider.GROUPS_ALL.filter { it[0] >= 72 }.toTypedArray()

    GroupsScreen(
        title       = stringResource(R.string.hiragana_title),
        symbolMap   = symbolMap,
        groups5     = groups5,
        groups3     = groups3,
        onGroupClick = onGroupClick,
        onAllRandom  = onAllRandom,
        onBack       = onBack
    )
}

@Composable
fun KatakanaGroupsScreen(
    symbolMap: Map<Int, String>,
    onGroupClick: (IntArray) -> Unit,
    onAllRandom: () -> Unit,
    onBack: () -> Unit
) {
    val groups5 = KatakanaGroupProvider.GROUPS_ALL.filter { it.size >= 3 && it[0] < 72 }.toTypedArray()
    val groups3 = KatakanaGroupProvider.GROUPS_ALL.filter { it[0] >= 72 }.toTypedArray()

    GroupsScreen(
        title        = stringResource(R.string.katakana_title),
        symbolMap    = symbolMap,
        groups5      = groups5,
        groups3      = groups3,
        onGroupClick = onGroupClick,
        onAllRandom  = onAllRandom,
        onBack       = onBack
    )
}