package com.example.japanese_self_study_guide.main_profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.japanese_self_study_guide.R
import kotlinx.coroutines.launch

private data class NavItem(val dest: String, val labelRes: Int, val icon: ImageVector)

private val navItems = listOf(
    NavItem("profile",    R.string.nav_profile,    Icons.Default.Person),
    NavItem("hiragana",   R.string.hiragana_title, Icons.Default.Translate),
    NavItem("katakana",   R.string.katakana_title, Icons.Default.Translate),
    NavItem("kanji",      R.string.kanji_title,    Icons.Default.TextFields),
    NavItem("dictionary", R.string.dict_title,     Icons.Default.MenuBook),
    NavItem("grammar",    R.string.grammar_title,  Icons.Default.School),
    NavItem("texts",      R.string.texts_title,    Icons.Default.Article),
    NavItem("audio",      R.string.audio_title,    Icons.Default.Headphones)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivityContent(
    viewModel: MainViewModel,
    currentLang: String,
    onTileClick: (Map<String, Any>) -> Unit,
    onNavigate: (String) -> Unit,
    onLangChange: (String) -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        SettingsScreen(
            currentLang  = currentLang,
            onLangChange = { lang ->
                onLangChange(lang)
            },
            onLogout = {
                showSettings = false
                onLogout()
            },
            onBack = { showSettings = false }
        )
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(280.dp)) {
                NavHeader(
                    username     = state.username,
                    userEmail    = state.userEmail,
                    avatarBase64 = state.avatarBase64
                )

                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                navItems.forEach { item ->
                    NavigationDrawerItem(
                        icon     = { Icon(item.icon, contentDescription = null) },
                        label    = { Text(androidx.compose.ui.res.stringResource(item.labelRes)) },
                        selected = false,
                        onClick  = {
                            scope.launch { drawerState.close() }
                            onNavigate(item.dest)
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                Spacer(Modifier.weight(1f))
                HorizontalDivider()

                NavigationDrawerItem(
                    icon     = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label    = { Text(androidx.compose.ui.res.stringResource(R.string.settings_title)) },
                    selected = false,
                    onClick  = {
                        scope.launch { drawerState.close() }
                        showSettings = true
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Thuru Learn") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = null)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor             = MaterialTheme.colorScheme.primary,
                        titleContentColor          = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { padding ->
            MainScreen(
                state       = state,
                onTileClick = { tile -> onTileClick(tile.rec) },
                modifier    = Modifier.padding(padding)
            )
        }
    }
}