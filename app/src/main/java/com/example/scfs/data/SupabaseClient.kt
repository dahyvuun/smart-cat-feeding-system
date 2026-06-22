package com.example.scfs.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseManager {
    val client = createSupabaseClient(
        supabaseUrl = "https://ovotayvhflobdcvbooxk.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im92b3RheXZoZmxvYmRjdmJvb3hrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODE4Njc3OTAsImV4cCI6MjA5NzQ0Mzc5MH0.sHjKoBSrcxrffnfghDDOrBDX7B9ud7lpQHh76bMU98Y"
    ) {

        install(Auth)
        install(Postgrest)
        install(Storage)
    }
}