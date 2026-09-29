package com.game.tiktactoe.model

import org.json.JSONObject

data class MatchRecord(
    val id: String,
    val gameType: String, // "human" or "ai"
    val player1Name: String,
    val player2Name: String, // or "AI"
    val winner: String,
    val player1Score: Int,
    val player2Score: Int,
    val matchCount: Int,
    val difficulty: String, // "easy", "medium", "hard", or ""
    val dateTimeMillis: Long
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("gameType", gameType)
        put("player1Name", player1Name)
        put("player2Name", player2Name)
        put("winner", winner)
        put("player1Score", player1Score)
        put("player2Score", player2Score)
        put("matchCount", matchCount)
        put("difficulty", difficulty)
        put("dateTime", dateTimeMillis)
    }

    companion object {
        fun fromJson(json: JSONObject): MatchRecord = MatchRecord(
            id = json.getString("id"),
            gameType = json.getString("gameType"),
            player1Name = json.getString("player1Name"),
            player2Name = json.getString("player2Name"),
            winner = json.getString("winner"),
            player1Score = json.getInt("player1Score"),
            player2Score = json.getInt("player2Score"),
            matchCount = json.getInt("matchCount"),
            difficulty = json.optString("difficulty", ""),
            dateTimeMillis = json.getLong("dateTime")
        )
    }
}
