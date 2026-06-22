package com.example.scfs.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from

object DashboardRepository {

    suspend fun getDashboardCats(): List<DashboardCatDto> {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return emptyList()

        return SupabaseManager.client
            .from("dashboard_cats")
            .select {
                filter {
                    eq("owner_user_id", userId)
                }
            }
            .decodeList()
    }

    suspend fun getMachineStatus(machineId: String): MachineStatusDto? {
        return SupabaseManager.client
            .from("machine_status")
            .select {
                filter {
                    eq("machine_id", machineId)
                }
            }
            .decodeList<MachineStatusDto>()
            .firstOrNull()
    }
}