package com.heathealth.app.data.model

import com.heathealth.app.data.local.entity.WaterIntakeEntity

data class WaterTrackerUiState(
    val todayEntries: List<WaterIntakeEntity> = emptyList(),
    val todayTotalMl: Int = 0,
    val targetMl: Int = 2500,
    val customAmountInput: String = "",
    val customInputError: String? = null,
    val isLoading: Boolean = false,
    val lastDeletedEntry: WaterIntakeEntity? = null,
    val userFeedbackMessage: String? = null
) {
    val progress: Float
        get() = if (targetMl > 0) (todayTotalMl.toFloat() / targetMl.toFloat()).coerceIn(0f, 1f) else 0f

    val progressPercentage: Int
        get() = (progress * 100).toInt()
}
