package com.example.scfs.data

import kotlinx.serialization.Serializable

@Serializable
data class MachineInsert(
    val id: String,
    val name: String
)