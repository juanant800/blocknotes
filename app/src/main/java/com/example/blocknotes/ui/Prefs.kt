package com.example.blocknotes.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object Prefs {
    var name by mutableStateOf("Tú")

    fun load(ctx: Context) {
        name = ctx.getSharedPreferences("bn", Context.MODE_PRIVATE).getString("name", "Tú") ?: "Tú"
    }

    fun save(ctx: Context, value: String) {
        name = value
        ctx.getSharedPreferences("bn", Context.MODE_PRIVATE).edit().putString("name", value).apply()
    }
}
