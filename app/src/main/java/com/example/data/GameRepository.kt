package com.example.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameScoreDao: GameScoreDao) {
    val allScores: Flow<List<GameScore>> = gameScoreDao.getAllScores()

    fun getScoresForGame(gameType: String): Flow<List<GameScore>> {
        return gameScoreDao.getScoresByGame(gameType)
    }

    suspend fun saveScore(score: GameScore) {
        gameScoreDao.insertScore(score)
    }

    suspend fun deleteScore(score: GameScore) {
        gameScoreDao.deleteScore(score)
    }

    suspend fun clearHistory() {
        gameScoreDao.clearAllScores()
    }
}
