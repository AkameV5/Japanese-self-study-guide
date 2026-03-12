package com.example.japanese_self_study_guide.kanji.ui

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
import com.example.japanese_self_study_guide.kanji.ExerciseGroup
import com.example.japanese_self_study_guide.kanji.GroupsProvider
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanjiExerciseGroupsScreen(
    onBack: () -> Unit,
    onGroupClick: (ExerciseGroup) -> Unit
) {
    val groups = remember { GroupsProvider.getGroups() }
    val kanjiPreview = remember { mutableStateMapOf<String, List<String>>() }

    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        for (group in groups) {
            val ids = (group.startId..group.endId).map { it.toDouble() }
            val chunks = ids.chunked(10)
            val collected = mutableListOf<Pair<Double, String>>()
            var done = 0
            for (chunk in chunks) {
                db.collection("Kanji").whereIn("id", chunk).get()
                    .addOnSuccessListener { query ->
                        for (doc in query.documents) {
                            val id = (doc.get("id") as? Double) ?: continue
                            val char = doc.getString("kanji") ?: continue
                            collected.add(id to char)
                        }
                        done++
                        if (done == chunks.size) {
                            kanjiPreview[group.title] = collected.sortedBy { it.first }.map { it.second }
                        }
                    }
                    .addOnFailureListener { done++ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.kanji_groups_title),
                        color = MaterialTheme.colorScheme.onPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null,
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(groups) { group ->
                val isReview = group.title.contains("Общее") || group.title.contains("Окончание")
                val preview = kanjiPreview[group.title]

                Card(
                    onClick = { onGroupClick(group) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isReview)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                group.title,
                                fontWeight = if (isReview) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                stringResource(R.string.kanji_group_exercises_count, group.limit),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        if (preview == null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    stringResource(R.string.kanji_group_loading),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            val sample = remember(preview) { preview.shuffled().take(5) }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                sample.forEach { char ->
                                    Text(
                                        text = char,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(Modifier.weight(1f))
                                Text(
                                    stringResource(R.string.kanji_group_total, preview.size),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}