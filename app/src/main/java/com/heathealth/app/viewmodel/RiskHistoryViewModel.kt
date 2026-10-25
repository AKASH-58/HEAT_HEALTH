package com.heathealth.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.heathealth.app.data.local.HeatHealthDatabase
import com.heathealth.app.data.local.entity.HeatRiskLogEntity
import com.heathealth.app.data.model.RiskLevel
import com.heathealth.app.data.repository.HeatHealthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TimePeriod(val displayName: String, val durationMs: Long) {
    LAST_7_DAYS("Last 7 Days", 7 * 86400 * 1000L),
    LAST_30_DAYS("Last 30 Days", 30 * 86400 * 1000L),
    ALL_TIME("All History", 0L)
}

data class RiskTrendsSummary(
    val totalPredictions: Int = 0,
    val highestRiskCategory: RiskLevel? = null,
    val averageTemperature: Double? = null,
    val averageFeelsLike: Double? = null,
    val riskDistribution: Map<RiskLevel, Int> = emptyMap(),
    val insights: List<String> = emptyList()
)

class RiskHistoryViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: HeatHealthRepository = run {
        try {
            val db = HeatHealthDatabase.getDatabase(application)
            HeatHealthRepository(
                heatRiskLogDao = db.heatRiskLogDao(),
                alertLogDao = db.alertLogDao()
            )
        } catch (e: Throwable) {
            HeatHealthRepository()
        }
    },
    autoLoad: Boolean = true
) : AndroidViewModel(application) {

    constructor(
        repository: HeatHealthRepository,
        autoLoad: Boolean = true
    ) : this(
        application = Application(),
        repository = repository,
        autoLoad = autoLoad
    )

    private val _selectedPeriod = MutableStateFlow(TimePeriod.LAST_7_DAYS)
    val selectedPeriod: StateFlow<TimePeriod> = _selectedPeriod.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val historyLogs: StateFlow<List<HeatRiskLogEntity>> = _selectedPeriod
        .flatMapLatest { period ->
            if (period == TimePeriod.ALL_TIME) {
                repository.getAllRiskLogs()
            } else {
                val minTimestamp = System.currentTimeMillis() - period.durationMs
                repository.getRiskLogsSince(minTimestamp)
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val trendsSummary: StateFlow<RiskTrendsSummary> = historyLogs
        .map { logs -> calculateTrendsSummary(logs, _selectedPeriod.value) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            RiskTrendsSummary()
        )

    fun selectPeriod(period: TimePeriod) {
        _selectedPeriod.value = period
    }

    fun deleteLog(id: Long) {
        viewModelScope.launch {
            repository.deleteRiskLogById(id)
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearAllRiskLogs()
        }
    }

    companion object {
        fun calculateTrendsSummary(logs: List<HeatRiskLogEntity>, period: TimePeriod): RiskTrendsSummary {
            if (logs.isEmpty()) {
                return RiskTrendsSummary(
                    totalPredictions = 0,
                    highestRiskCategory = null,
                    averageTemperature = null,
                    averageFeelsLike = null,
                    riskDistribution = emptyMap(),
                    insights = listOf("No heat-risk predictions recorded for ${period.displayName.lowercase()}. Fetch live predictions on the Home dashboard to begin tracking your history.")
                )
            }

            val total = logs.size
            val avgTemp = logs.map { it.temperatureCelsius }.average()
            val avgFeelsLike = logs.map { it.feelsLikeCelsius }.average()

            val distribution = mapOf(
                RiskLevel.LOW to logs.count { RiskLevel.fromDisplayName(it.riskLevel) == RiskLevel.LOW },
                RiskLevel.MODERATE to logs.count { RiskLevel.fromDisplayName(it.riskLevel) == RiskLevel.MODERATE },
                RiskLevel.HIGH to logs.count { RiskLevel.fromDisplayName(it.riskLevel) == RiskLevel.HIGH },
                RiskLevel.EXTREME to logs.count {
                    val lvl = RiskLevel.fromDisplayName(it.riskLevel)
                    lvl == RiskLevel.EXTREME || lvl == RiskLevel.VERY_HIGH
                }
            )

            val highestRisk = logs.map { RiskLevel.fromDisplayName(it.riskLevel) }.maxByOrNull { it.ordinal }

            val insights = mutableListOf<String>()

            val highAndExtremeCount = (distribution[RiskLevel.HIGH] ?: 0) + (distribution[RiskLevel.EXTREME] ?: 0)
            if (highAndExtremeCount > 0) {
                insights.add("You recorded $highAndExtremeCount high or extreme-risk predictions during ${period.displayName.lowercase()}.")
            } else {
                insights.add("No high or extreme-risk predictions recorded during ${period.displayName.lowercase()}.")
            }

            if (logs.size >= 2) {
                val latestRisk = RiskLevel.fromDisplayName(logs.first().riskLevel)
                val previousRisk = RiskLevel.fromDisplayName(logs[1].riskLevel)

                if (latestRisk.ordinal > previousRisk.ordinal) {
                    insights.add("Your latest prediction (${latestRisk.displayName}) escalated compared to your previous prediction (${previousRisk.displayName}).")
                } else if (latestRisk.ordinal < previousRisk.ordinal) {
                    insights.add("Your latest prediction (${latestRisk.displayName}) shows lower thermal stress than your previous prediction (${previousRisk.displayName}).")
                } else {
                    insights.add("Your latest prediction category (${latestRisk.displayName}) is identical to your previous prediction.")
                }
            } else {
                insights.add("Record additional predictions over time to generate comparative trend insights.")
            }

            val formattedFeelsLike = String.format(java.util.Locale.US, "%.1f", avgFeelsLike)
            insights.add("Average recorded feels-like temperature during this period was ${formattedFeelsLike}°C.")
            insights.add("Note: Insights reflect recorded environmental predictions only and do not constitute a medical diagnosis.")

            return RiskTrendsSummary(
                totalPredictions = total,
                highestRiskCategory = highestRisk,
                averageTemperature = avgTemp,
                averageFeelsLike = avgFeelsLike,
                riskDistribution = distribution,
                insights = insights
            )
        }
    }
}
