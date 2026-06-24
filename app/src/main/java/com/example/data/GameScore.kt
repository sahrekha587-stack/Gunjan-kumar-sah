package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_scores")
data class GameScore(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val gameType: String, // "TIC_TAC_TOE", "MEMORY_MATCH", "SLIDING_PUZZLE"
    val scoreName: String, // e.g. "Moves", "Time", "Win", "Loss", "Draw"
    val scoreValue: Int, // e.g. number of moves, or win indicator (1)
    val metadata: String = "", // extra information, e.g. details of the victory
    val difficulty: String = "NORMAL", // "EASY", "NORMAL", "HARD"
    val timestamp: Long = System.currentTimeMillis()
)
