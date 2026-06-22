package com.example.scfs.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from

object SetupRepository {

    suspend fun getNextScreen(): String {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id
            ?: return "auth"

        val machineLinks = SupabaseManager.client
            .from("machine_users")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeList<Map<String, String>>()

        if (machineLinks.isEmpty()) {
            return "machine"
        }

        val machineId = machineLinks.first()["machine_id"] ?: return "machine"

        val cats = SupabaseManager.client
            .from("cats")
            .select {
                filter {
                    eq("owner_user_id", userId)
                }
            }
            .decodeList<CatDto>()

        if (cats.isEmpty()) {
            return "cat"
        }

        val machineCats = SupabaseManager.client
            .from("machine_cats")
            .select {
                filter {
                    eq("machine_id", machineId)
                }
            }
            .decodeList<Map<String, String>>()

        if (machineCats.isEmpty()) {
            return "cat"
        }

        return "home"
    }
}