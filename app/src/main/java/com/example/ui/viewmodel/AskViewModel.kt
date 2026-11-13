package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.AskEntity
import com.example.data.model.AskOutcome
import com.example.data.repository.AskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MonthlyStat(
    val monthYearKey: String, // e.g. "2026-10"
    val displayMonth: String, // e.g. "October 2026"
    val totalAsks: Int,
    val rejections: Int,
    val acceptances: Int,
    val pending: Int
)

data class MilestoneInfo(
    val levelName: String,
    val nextThreshold: Int,
    val asksNeededForNext: Int,
    val badgeIconName: String,
    val quote: String
)

data class StatsSummary(
    val totalAsks: Int = 0,
    val targetAsks: Int = 1000,
    val rejections: Int = 0,
    val acceptances: Int = 0,
    val pending: Int = 0,
    val rejectionRate: Float = 0f,
    val acceptanceRate: Float = 0f,
    val progressFraction: Float = 0f,
    val milestone: MilestoneInfo = MilestoneInfo("Courage Initiate", 10, 10, "sparkle", "Every 'no' is proof you dared."),
    val monthlyStats: List<MonthlyStat> = emptyList(),
    val categoryDistribution: Map<String, Int> = emptyMap()
)

data class AskUiState(
    val allAsks: List<AskEntity> = emptyList(),
    val filteredAsks: List<AskEntity> = emptyList(),
    val stats: StatsSummary = StatsSummary(),
    val searchQuery: String = "",
    val selectedCategoryFilter: String? = null,
    val selectedOutcomeFilter: String? = null,
    val userMessage: String? = null
)

class AskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AskRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AskRepository(database.askDao())
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter

    private val _selectedOutcomeFilter = MutableStateFlow<String?>(null)
    val selectedOutcomeFilter: StateFlow<String?> = _selectedOutcomeFilter

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage

    val uiState: StateFlow<AskUiState> = combine(
        repository.allAsks,
        _searchQuery,
        _selectedCategoryFilter,
        _selectedOutcomeFilter,
        _userMessage
    ) { asks, query, categoryFilter, outcomeFilter, message ->
        val filtered = asks.filter { ask ->
            val matchesQuery = query.isBlank() ||
                    ask.title.contains(query, ignoreCase = true) ||
                    ask.notes.contains(query, ignoreCase = true) ||
                    ask.takeaway.contains(query, ignoreCase = true)

            val matchesCategory = categoryFilter == null || ask.category.equals(categoryFilter, ignoreCase = true)

            val matchesOutcome = outcomeFilter == null || ask.outcome.equals(outcomeFilter, ignoreCase = true)

            matchesQuery && matchesCategory && matchesOutcome
        }

        val stats = calculateStats(asks)

        AskUiState(
            allAsks = asks,
            filteredAsks = filtered,
            stats = stats,
            searchQuery = query,
            selectedCategoryFilter = categoryFilter,
            selectedOutcomeFilter = outcomeFilter,
            userMessage = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AskUiState()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = if (_selectedCategoryFilter.value == category) null else category
    }

    fun setOutcomeFilter(outcome: String?) {
        _selectedOutcomeFilter.value = if (_selectedOutcomeFilter.value == outcome) null else outcome
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCategoryFilter.value = null
        _selectedOutcomeFilter.value = null
    }

    fun dismissUserMessage() {
        _userMessage.value = null
    }

    fun addAsk(
        title: String,
        category: String,
        dateEpochMillis: Long,
        notes: String,
        outcome: AskOutcome,
        takeaway: String
    ) {
        viewModelScope.launch {
            val ask = AskEntity(
                title = title.trim(),
                category = category.trim(),
                dateEpochMillis = dateEpochMillis,
                notes = notes.trim(),
                outcome = outcome.name,
                takeaway = takeaway.trim()
            )
            repository.insert(ask)
            _userMessage.value = "Ask logged! Keep building courage."
        }
    }

    fun updateAsk(ask: AskEntity) {
        viewModelScope.launch {
            repository.update(ask)
            _userMessage.value = "Record updated successfully."
        }
    }

    fun deleteAsk(ask: AskEntity) {
        viewModelScope.launch {
            repository.delete(ask)
            _userMessage.value = "Ask removed."
        }
    }

    fun quickUpdateOutcome(ask: AskEntity, newOutcome: AskOutcome) {
        viewModelScope.launch {
            val updated = ask.copy(outcome = newOutcome.name)
            repository.update(updated)
            val msg = when (newOutcome) {
                AskOutcome.REJECTED -> "Marked as Rejected. Rejection = Courage badge added!"
                AskOutcome.ACCEPTED -> "Marked as Accepted. Congratulations on the win!"
                AskOutcome.PENDING -> "Marked as Awaiting Answer."
            }
            _userMessage.value = msg
        }
    }

    fun shareBackup(context: Context, format: String = "csv") {
        viewModelScope.launch {
            val currentAsks = uiState.value.allAsks
            if (currentAsks.isEmpty()) {
                _userMessage.value = "No asks to export yet. Add your first ask!"
                return@launch
            }
            repository.shareBackup(context, currentAsks, format)
        }
    }

    fun importBackup(jsonString: String) {
        viewModelScope.launch {
            try {
                val count = repository.importFromJson(jsonString)
                _userMessage.value = "Successfully imported $count asks!"
            } catch (e: Exception) {
                _userMessage.value = "Failed to import: ${e.localizedMessage ?: "Invalid file"}"
            }
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val day = 86400000L
            val samples = listOf(
                AskEntity(
                    title = "Asked coffee shop barista for a free pastry",
                    category = "Discounts & Favors",
                    dateEpochMillis = now - day * 1,
                    notes = "Heart was racing! Barista smiled, laughed, and politely said no store policy doesn't allow it.",
                    outcome = AskOutcome.REJECTED.name,
                    takeaway = "Felt awkward for 5 seconds, then total euphoria that I survived!"
                ),
                AskEntity(
                    title = "Pitched speaking at the Annual Tech & Design Summit",
                    category = "Career & Work",
                    dateEpochMillis = now - day * 3,
                    notes = "Submitted a talk proposal on Women in Engineering and Creative Leadership.",
                    outcome = AskOutcome.ACCEPTED.name,
                    takeaway = "If you don't ask, the answer is always no. Got the slot!"
                ),
                AskEntity(
                    title = "Requested a 15% discount on gym annual renewal",
                    category = "Discounts & Favors",
                    dateEpochMillis = now - day * 6,
                    notes = "Manager offered 10% instead! Counted as a win.",
                    outcome = AskOutcome.ACCEPTED.name,
                    takeaway = "Never accept sticker price without asking gently."
                ),
                AskEntity(
                    title = "Asked industry mentor for a 15-minute coffee chat",
                    category = "Career & Work",
                    dateEpochMillis = now - day * 10,
                    notes = "Sent a short personalized DM on LinkedIn. Waiting for her reply.",
                    outcome = AskOutcome.PENDING.name,
                    takeaway = "Sent with confidence. Ball is in her court."
                ),
                AskEntity(
                    title = "Asked cute stranger at bookstore for book recommendation",
                    category = "Dating & Romance",
                    dateEpochMillis = now - day * 14,
                    notes = "They were wearing headphones, apologized and walked off briskly.",
                    outcome = AskOutcome.REJECTED.name,
                    takeaway = "Rejection was completely harmless! My fear was 100x worse than reality."
                ),
                AskEntity(
                    title = "Requested client to double project scope and budget",
                    category = "Sales & Business",
                    dateEpochMillis = now - day * 22,
                    notes = "Client said budget was capped for Q3, but loved the vision.",
                    outcome = AskOutcome.REJECTED.name,
                    takeaway = "They appreciated the ambition. Planting seeds for Q4."
                )
            )
            repository.insertAll(samples)
            _userMessage.value = "Loaded 6 inspiring starter asks!"
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAll()
            _userMessage.value = "All ask records cleared."
        }
    }

    private fun calculateStats(asks: List<AskEntity>): StatsSummary {
        val total = asks.size
        var rejections = 0
        var acceptances = 0
        var pending = 0

        val categoryMap = mutableMapOf<String, Int>()
        val monthGroupMap = mutableMapOf<String, MutableList<AskEntity>>()

        val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val displayMonthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

        for (ask in asks) {
            when (AskOutcome.fromString(ask.outcome)) {
                AskOutcome.REJECTED -> rejections++
                AskOutcome.ACCEPTED -> acceptances++
                AskOutcome.PENDING -> pending++
            }

            categoryMap[ask.category] = (categoryMap[ask.category] ?: 0) + 1

            val date = Date(ask.dateEpochMillis)
            val monthKey = monthFormat.format(date)
            monthGroupMap.getOrPut(monthKey) { mutableListOf() }.add(ask)
        }

        val decided = rejections + acceptances
        val rejectionRate = if (decided > 0) (rejections.toFloat() / decided) * 100f else 0f
        val acceptanceRate = if (decided > 0) (acceptances.toFloat() / decided) * 100f else 0f
        val progressFraction = (total.toFloat() / 1000f).coerceIn(0f, 1f)

        // Milestones
        val milestone = when {
            total >= 1000 -> MilestoneInfo("1,000 Rejections Conqueror!", 1000, 0, "crown", "You have rewritten your fear forever. Unstoppable.")
            total >= 750 -> MilestoneInfo("Legendary Fearless", 1000, 1000 - total, "trophy", "The final stretch to 1,000 asks!")
            total >= 500 -> MilestoneInfo("Unshakable Force", 750, 750 - total, "shield", "Over halfway to 1,000! Boldness is your nature.")
            total >= 250 -> MilestoneInfo("Audacity Champion", 500, 500 - total, "star", "250+ asks! You handle 'no' like a seasoned pro.")
            total >= 100 -> MilestoneInfo("Centurion of Courage", 250, 250 - total, "diamond", "Triple digits! Rejection no longer stings.")
            total >= 50 -> MilestoneInfo("Rejection Warrior", 100, 100 - total, "fire", "50 asks logged. Your comfort zone is expanding daily.")
            total >= 25 -> MilestoneInfo("Fearless Seeker", 50, 50 - total, "heart", "25 asks! Confidence is a muscle you are flexing.")
            total >= 10 -> MilestoneInfo("Courage Initiate", 25, 25 - total, "flower", "Double digits! Momentum is building.")
            else -> MilestoneInfo("Brave Beginner", 10, 10 - total, "sparkle", "Every journey to 1,000 begins with asking once.")
        }

        val sortedMonthlyStats = monthGroupMap.entries
            .sortedByDescending { it.key }
            .map { entry ->
                val monthAsks = entry.value
                val date = try {
                    monthFormat.parse(entry.key) ?: Date()
                } catch (e: Exception) {
                    Date()
                }
                MonthlyStat(
                    monthYearKey = entry.key,
                    displayMonth = displayMonthFormat.format(date),
                    totalAsks = monthAsks.size,
                    rejections = monthAsks.count { it.outcome == AskOutcome.REJECTED.name },
                    acceptances = monthAsks.count { it.outcome == AskOutcome.ACCEPTED.name },
                    pending = monthAsks.count { it.outcome == AskOutcome.PENDING.name }
                )
            }

        return StatsSummary(
            totalAsks = total,
            targetAsks = 1000,
            rejections = rejections,
            acceptances = acceptances,
            pending = pending,
            rejectionRate = rejectionRate,
            acceptanceRate = acceptanceRate,
            progressFraction = progressFraction,
            milestone = milestone,
            monthlyStats = sortedMonthlyStats,
            categoryDistribution = categoryMap
        )
    }
}
