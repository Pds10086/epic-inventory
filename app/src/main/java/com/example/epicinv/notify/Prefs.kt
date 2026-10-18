package com.example.epicinv.notify

import android.content.Context

class Prefs(ctx: Context) {
    private val sp = ctx.getSharedPreferences("freegame_notify", Context.MODE_PRIVATE)

    private val notifiedKey = "notified_ids"

    fun filterNewForNotification(titles: List<String>): List<String> =
        titles.filter { it !in sp.getStringSet(notifiedKey, emptySet())!! }

    fun markNotified(titles: Set<String>) {
        sp.edit().putStringSet(notifiedKey,
            sp.getStringSet(notifiedKey, emptySet())!!.toMutableSet().apply { addAll(titles) }
        ).apply()
    }
}
