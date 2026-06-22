package com.example.scfs.data

import kotlinx.serialization.Serializable

@Serializable
data class CatInsert(
    val owner_user_id: String,
    val name: String,
    val weight_kg: Double? = null,
    val daily_food_goal_g: Int? = null
)


@kotlinx.serialization.Serializable
data class CatDto(
    val id: String,
    val owner_user_id: String? = null,
    val name: String,
    val weight_kg: Double? = null,
    val daily_food_goal_g: Int? = null,
    val created_at: String? = null
)

@Serializable
data class CatTrainingPhotoInsert(
    val machine_id: String,
    val cat_id: String,
    val image_path: String,
    val photo_index: Int
)
@kotlinx.serialization.Serializable
data class MachineDto(
    val id: String,
    val name: String,
    val created_at: String? = null
)

@kotlinx.serialization.Serializable
data class MachineStatusDto(
    val machine_id: String,
    val food_level_percent: Int? = null,
    val wifi_rssi: Int? = null,
    val motion_detected: Boolean? = null,
    val last_seen: String? = null,
    val updated_at: String? = null
)
@kotlinx.serialization.Serializable
data class DashboardCatDto(
    val cat_id: String,
    val owner_user_id: String,
    val cat_name: String,
    val weight_kg: Double? = null,
    val daily_food_goal_g: Int? = null,
    val machine_id: String,
    val machine_name: String
)