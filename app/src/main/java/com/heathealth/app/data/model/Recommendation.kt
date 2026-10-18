package com.heathealth.app.data.model

data class Recommendation(
    val id: String,
    val title: String,
    val description: String,
    val category: String = "Safety Action"
)
