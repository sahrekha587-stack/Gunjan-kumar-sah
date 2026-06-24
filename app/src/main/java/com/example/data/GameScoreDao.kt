package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameScoreDao {
    @Query("SELECT * FROM game_scores ORDER BY timestamp DESC")
    fun getAllScores(): Flow<List<GameScore>>

    @Query("SELECT * FROM game_scores WHERE gameType = :gameType ORDER BY timestamp DESC")
    fun getScoresByGame(gameType: String): Flow<List<GameScore>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: GameScore)

    @Delete
    suspend fun deleteScore(score: GameScore)

    @Query("DELETE FROM game_scores")
    suspend fun clearAllScores()
}
