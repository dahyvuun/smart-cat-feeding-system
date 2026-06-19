package com.example.scfs.data

import kotlinx.serialization.Serializable

@Serializable
data class CatInsert(
    val name: String,
    val weight_kg: Double? = null,
    val daily_food_goal_g: Int? = null
)

@Serializable
data class CatDto(
    val id: String? = null,
    val name: String,
    val weight_kg: Double? = null,
    val daily_food_goal_g: Int? = null
)