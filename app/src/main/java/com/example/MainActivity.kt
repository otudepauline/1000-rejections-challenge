package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AskEntity
import com.example.data.model.AskOutcome
import com.example.ui.components.AddEditAskSheet
import com.example.ui.components.AskDetailSheet
import com.example.ui.screens.BackupScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AskViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: AskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainContent(viewModel = viewModel)
            }
        }
    }
}

enum class AppTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    HOME("Dashboard", Icons.Default.Home),
    HISTORY("History", Icons.AutoMirrored.Filled.List),
    STATS("Stats", Icons.Default.BarChart),
    BACKUP("Backup", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(viewModel: AskViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAddSheet by remember { mutableStateOf(false) }
    var askBeingEdited by remember { mutableStateOf<AskEntity?>(null) }
    var askDetailView by remember { mutableStateOf<AskEntity?>(null) }

    // User message snackbar trigger
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissUserMessage()
        }
    }

    // Android back navigation handler
    BackHandler(enabled = selectedTabIndex != 0) {
        selectedTabIndex = 0
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "1000 Rejections",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${uiState.stats.totalAsks}/1000",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.shareBackup(context, "csv") },
                        modifier = Modifier.testTag("topbar_share_backup_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Backup",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                AppTab.entries.forEachIndexed { index, tab ->
                    val isSelected = selectedTabIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (AppTab.entries[selectedTabIndex]) {
                AppTab.HOME -> {
                    HomeScreen(
                        uiState = uiState,
                        onAddNewAsk = {
                            askBeingEdited = null
                            showAddSheet = true
                        },
                        onAskClick = { ask ->
                            askDetailView = ask
                        },
                        onNavigateToHistory = {
                            selectedTabIndex = 1
                        },
                        onSeedSampleData = {
                            viewModel.seedSampleData()
                        }
                    )
                }
                AppTab.HISTORY -> {
                    HistoryScreen(
                        uiState = uiState,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onCategoryFilterChange = { viewModel.setCategoryFilter(it) },
                        onOutcomeFilterChange = { viewModel.setOutcomeFilter(it) },
                        onClearFilters = { viewModel.clearFilters() },
                        onAskClick = { ask -> askDetailView = ask },
                        onEditAsk = { ask ->
                            askBeingEdited = ask
                            showAddSheet = true
                        },
                        onDeleteAsk = { ask -> viewModel.deleteAsk(ask) },
                        onAddNewAsk = {
                            askBeingEdited = null
                            showAddSheet = true
                        }
                    )
                }
                AppTab.STATS -> {
                    StatsScreen(uiState = uiState)
                }
                AppTab.BACKUP -> {
                    BackupScreen(
                        uiState = uiState,
                        onExportShare = { ctx, fmt -> viewModel.shareBackup(ctx, fmt) },
                        onImportJson = { json -> viewModel.importBackup(json) },
                        onSeedSampleData = { viewModel.seedSampleData() },
                        onClearAllData = { viewModel.clearAllData() }
                    )
                }
            }
        }
    }

    // Add / Edit Modal Sheet
    if (showAddSheet) {
        AddEditAskSheet(
            askToEdit = askBeingEdited,
            onDismiss = {
                showAddSheet = false
                askBeingEdited = null
            },
            onSave = { title, category, dateEpochMillis, notes, outcome, takeaway ->
                if (askBeingEdited != null) {
                    val updated = askBeingEdited!!.copy(
                        title = title,
                        category = category,
                        dateEpochMillis = dateEpochMillis,
                        notes = notes,
                        outcome = outcome.name,
                        takeaway = takeaway
                    )
                    viewModel.updateAsk(updated)
                } else {
                    viewModel.addAsk(title, category, dateEpochMillis, notes, outcome, takeaway)
                }
            }
        )
    }

    // Detail View Sheet
    askDetailView?.let { ask ->
        AskDetailSheet(
            ask = ask,
            onDismiss = { askDetailView = null },
            onEdit = { currentAsk ->
                askDetailView = null
                askBeingEdited = currentAsk
                showAddSheet = true
            },
            onDelete = { currentAsk ->
                viewModel.deleteAsk(currentAsk)
                askDetailView = null
            },
            onQuickOutcomeChange = { currentAsk, newOutcome ->
                viewModel.quickUpdateOutcome(currentAsk, newOutcome)
                askDetailView = currentAsk.copy(outcome = newOutcome.name)
            }
        )
    }
}
