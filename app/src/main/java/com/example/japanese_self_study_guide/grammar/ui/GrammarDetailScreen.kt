package com.example.japanese_self_study_guide.grammar.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.japanese_self_study_guide.grammar.GrammarRule
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrammarDetailScreen(

    grammarId: Int,
    structure: String,
    explanation: String,
    example: String,
    translation: String,
    dailyMode: Boolean = false,
    onBack: () -> Unit,
    onGoToExercises: (Int) -> Unit
) {
    android.util.Log.d("DAILY_DEBUG", "GrammarDetailScreen composed, grammarId=$grammarId, dailyMode=$dailyMode")

    var rule by remember {
        mutableStateOf(
            GrammarRule(grammarId, structure, explanation, example, translation)
        )
    }
    var isLoading by remember { mutableStateOf(dailyMode) }

    LaunchedEffect(grammarId) {
        if (!dailyMode) return@LaunchedEffect
        FirebaseFirestore.getInstance()
            .collection("Grammar")
            .get()
            .addOnSuccessListener { query ->
                val doc = query.documents.firstOrNull { doc ->
                    val idVal = doc.get("id")
                    when (idVal) {
                        is Long    -> idVal.toInt() == grammarId
                        is Int     -> idVal == grammarId
                        is Double  -> idVal.toInt() == grammarId
                        is String  -> idVal.toIntOrNull() == grammarId
                        else       -> false
                    }
                }
                if (doc != null) {
                    rule = GrammarRule(
                        grammarId,
                        doc.getString("structure") ?: "",
                        doc.getString("explanation") ?: "",
                        doc.getString("example") ?: "",
                        doc.getString("translation") ?: ""
                    )
                }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isLoading) "…" else rule.structure.take(30),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.audio_back),
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = rule.structure,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = rule.explanation,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 17.sp,
                    lineHeight = 26.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (rule.example.isNotBlank()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                Text(
                    text = stringResource(R.string.grammar_example_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = rule.example,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (rule.translation.isNotBlank()) {
                    Text(
                        text = rule.translation,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onGoToExercises(grammarId) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    stringResource(R.string.grammar_exercises_btn),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}