package com.game.tiktactoe.services

import android.content.Context
import com.game.tiktactoe.model.MatchRecord
import org.json.JSONArray

object MatchHistoryService {
    private const val PREFS_NAME = "tictactoe_prefs"
    private const val KEY_HISTORY = "match_history"
    private const val MAX_ENTRIES = 100

    fun getHistory(context: Context): List<MatchRecord> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()

        val array = JSONArray(raw)
        val records = ArrayList<MatchRecord>(array.length())
        for (i in 0 until array.length()) {
            records.add(MatchRecord.fromJson(array.getJSONObject(i)))
        }
        return records.sortedByDescending { it.dateTimeMillis }
    }

    fun addMatch(context: Context, record: MatchRecord) {
        val history = ArrayList(getHistory(context))
        history.add(0, record)
        val trimmed = if (history.size > MAX_ENTRIES) history.subList(0, MAX_ENTRIES) else history

        val array = JSONArray()
        trimmed.forEach { array.put(it.toJson()) }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply()
    }

    fun clearHistory(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_HISTORY).apply()
    }
}
