package com.example.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth

object SupabaseClientProvider {
    const val SUPABASE_URL = "https://nfckyqgvufhcumqjznho.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5mY2t5cWd2dWZoY3VtcWp6bmhvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODIyNjEyNDAsImV4cCI6MjA5NzgzNzI0MH0.tI_Nng_7eHVoW6mJPKZGPEW_AME8qiMwkNEEk19zXBo"

    val client by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            install(Postgrest)
            install(Auth)
        }
    }
}
