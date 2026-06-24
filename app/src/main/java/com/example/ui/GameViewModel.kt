package com.example.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.GameRepository
import com.example.data.GameScore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class ActiveScreen {
    DASHBOARD,
    TIC_TAC_TOE,
    MEMORY_MATCH,
    SLIDING_PUZZLE,
    LEADERBOARD
}

data class MemoryCard(
    val id: Int,
    val emoji: String,
    val isRevealed: Boolean = false,
    val isMatched: Boolean = false
)

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    // Active navigation and application states
    private val _currentScreen = MutableStateFlow(ActiveScreen.DASHBOARD)
    val currentScreen: StateFlow<ActiveScreen> = _currentScreen.asStateFlow()

    // ----------------------------------------------------
    // ROOM DATA FEED
    // ----------------------------------------------------
    val highScoresList = repository.allScores

    fun navigateTo(screen: ActiveScreen) {
        _currentScreen.value = screen
        if (screen == ActiveScreen.LEADERBOARD) {
            refreshScores()
        }
    }

    private fun refreshScores() {
        // High scores flow automatically, no polling required
    }

    // ----------------------------------------------------
    // TIC TAC TOE GAME STATE
    // ----------------------------------------------------
    private val _tttBoard = MutableStateFlow(List(9) { "" })
    val tttBoard: StateFlow<List<String>> = _tttBoard.asStateFlow()

    private val _tttCurrentPlayer = MutableStateFlow("X") // "X" is human, "O" is AI or Player 2
    val tttCurrentPlayer: StateFlow<String> = _tttCurrentPlayer.asStateFlow()

    private val _tttWinner = MutableStateFlow<String?>(null) // "X", "O", "Draw", or null
    val tttWinner: StateFlow<String?> = _tttWinner.asStateFlow()

    private val _tttIsVsAI = MutableStateFlow(true)
    val tttIsVsAI: StateFlow<Boolean> = _tttIsVsAI.asStateFlow()

    private val _tttDifficulty = MutableStateFlow("NORMAL") // "EASY", "NORMAL", "HARD"
    val tttDifficulty: StateFlow<String> = _tttDifficulty.asStateFlow()

    private val _tttIsThinking = MutableStateFlow(false)
    val tttIsThinking: StateFlow<Boolean> = _tttIsThinking.asStateFlow()

    fun setTttVsAI(vsAI: Boolean) {
        _tttIsVsAI.value = vsAI
        resetTttGame()
    }

    fun setTttDifficulty(diff: String) {
        _tttDifficulty.value = diff
        resetTttGame()
    }

    fun resetTttGame() {
        _tttBoard.value = List(9) { "" }
        _tttCurrentPlayer.value = "X"
        _tttWinner.value = null
        _tttIsThinking.value = false
    }

    fun makeTttMove(index: Int) {
        if (_tttBoard.value[index] != "" || _tttWinner.value != null || _tttIsThinking.value) return

        val currentBoard = _tttBoard.value.toMutableList()
        currentBoard[index] = _tttCurrentPlayer.value
        _tttBoard.value = currentBoard

        // Check if there's a winner
        val winner = checkTttWinner(currentBoard)
        if (winner != null) {
            _tttWinner.value = winner
            saveTttResult(winner)
        } else {
            // Toggle player
            if (_tttIsVsAI.value) {
                // If Singleplayer, trigger AI turn
                _tttCurrentPlayer.value = "O"
                triggerTttComputerMove()
            } else {
                _tttCurrentPlayer.value = if (_tttCurrentPlayer.value == "X") "O" else "X"
            }
        }
    }

    private fun triggerTttComputerMove() {
        _tttIsThinking.value = true
        viewModelScope.launch {
            delay(600) // Thinking aesthetic delay
            val board = _tttBoard.value.toMutableList()
            val aiMove = when (_tttDifficulty.value) {
                "EASY" -> getEasyMove(board)
                "NORMAL" -> if (Random.nextFloat() < 0.6f) getHardMove(board) else getEasyMove(board)
                else -> getHardMove(board) // Unbeatable Minimax
            }

            if (aiMove != -1) {
                board[aiMove] = "O"
                _tttBoard.value = board
                val winner = checkTttWinner(board)
                if (winner != null) {
                    _tttWinner.value = winner
                    saveTttResult(winner)
                } else {
                    _tttCurrentPlayer.value = "X"
                }
            }
            _tttIsThinking.value = false
        }
    }

    private fun getEasyMove(board: List<String>): Int {
        val available = board.indices.filter { board[it] == "" }
        return if (available.isNotEmpty()) available.random() else -1
    }

    private fun getHardMove(board: List<String>): Int {
        var bestVal = -1000
        var bestMove = -1

        for (i in board.indices) {
            if (board[i] == "") {
                val demoBoard = board.toMutableList()
                demoBoard[i] = "O"
                val moveVal = minimax(demoBoard, 0, false)
                if (moveVal > bestVal) {
                    bestMove = i
                    bestVal = moveVal
                }
            }
        }
        return if (bestMove != -1) bestMove else getEasyMove(board)
    }

    private fun minimax(board: MutableList<String>, depth: Int, isMax: Boolean): Int {
        val score = evaluateBoardForMinimax(board)
        if (score == 10) return score - depth
        if (score == -10) return score + depth
        if (!board.contains("")) return 0

        if (isMax) {
            var best = -1000
            for (i in board.indices) {
                if (board[i] == "") {
                    board[i] = "O"
                    best = maxOf(best, minimax(board, depth + 1, false))
                    board[i] = ""
                }
            }
            return best
        } else {
            var best = 1000
            for (i in board.indices) {
                if (board[i] == "") {
                    board[i] = "X"
                    best = minOf(best, minimax(board, depth + 1, true))
                    board[i] = ""
                }
            }
            return best
        }
    }

    private fun evaluateBoardForMinimax(b: List<String>): Int {
        val lines = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // rows
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // columns
            listOf(0, 4, 8), listOf(2, 4, 6)                 // diagonals
        )
        for (line in lines) {
            if (b[line[0]] == "O" && b[line[1]] == "O" && b[line[2]] == "O") return 10
            if (b[line[0]] == "X" && b[line[1]] == "X" && b[line[2]] == "X") return -10
        }
        return 0
    }

    private fun checkTttWinner(b: List<String>): String? {
        val lines = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
        )
        for (line in lines) {
            if (b[line[0]] != "" && b[line[0]] == b[line[1]] && b[line[0]] == b[line[2]]) {
                return b[line[0]]
            }
        }
        if (!b.contains("")) return "Draw"
        return null
    }

    private fun saveTttResult(winner: String) {
        viewModelScope.launch {
            val scoreName = when (winner) {
                "X" -> "Victory"
                "O" -> "Loss"
                else -> "Draw"
            }
            val desc = if (_tttIsVsAI.value) "vs Computer (AI)" else "Pass & Play Mode"
            repository.saveScore(
                GameScore(
                    gameType = "TIC_TAC_TOE",
                    scoreName = scoreName,
                    scoreValue = if (winner == "X") 1 else 0,
                    metadata = desc,
                    difficulty = _tttDifficulty.value
                )
            )
        }
    }

    // ----------------------------------------------------
    // MEMORY MATCH GAME STATE
    // ----------------------------------------------------
    private val _memCards = MutableStateFlow<List<MemoryCard>>(emptyList())
    val memCards: StateFlow<List<MemoryCard>> = _memCards.asStateFlow()

    private val _memMoves = MutableStateFlow(0)
    val memMoves: StateFlow<Int> = _memMoves.asStateFlow()

    private val _memMatches = MutableStateFlow(0)
    val memMatches: StateFlow<Int> = _memMatches.asStateFlow()

    private val _memIsFinished = MutableStateFlow(false)
    val memIsFinished: StateFlow<Boolean> = _memIsFinished.asStateFlow()

    private val _memTimeElapsed = MutableStateFlow(0) // seconds
    val memTimeElapsed: StateFlow<Int> = _memTimeElapsed.asStateFlow()

    private var memoryTimerRunning = false
    private var firstSelectedCardIdx: Int? = null
    private var secondSelectedCardIdx: Int? = null
    private var isFlippingDisabled = false

    private val emojisSubset = listOf(
        "🦁", "🦁", "🐼", "🐼", "🦊", "🦊", "🐻", "🐻",
        "🦉", "🦉", "🐵", "🐵", "🐸", "🐸", "🐧", "🐧"
    )

    fun startNewMemoryGame() {
        val cardList = emojisSubset.shuffled().mapIndexed { index, emoji ->
            MemoryCard(id = index, emoji = emoji)
        }
        _memCards.value = cardList
        _memMoves.value = 0
        _memMatches.value = 0
        _memIsFinished.value = false
        _memTimeElapsed.value = 0

        firstSelectedCardIdx = null
        secondSelectedCardIdx = null
        isFlippingDisabled = false

        startMemoryTimer()
    }

    private fun startMemoryTimer() {
        if (memoryTimerRunning) return
        memoryTimerRunning = true
        viewModelScope.launch {
            while (memoryTimerRunning && !_memIsFinished.value && _currentScreen.value == ActiveScreen.MEMORY_MATCH) {
                delay(1000)
                _memTimeElapsed.value += 1
            }
            memoryTimerRunning = false
        }
    }

    fun revealCard(index: Int) {
        if (isFlippingDisabled || _memIsFinished.value) return
        val cards = _memCards.value.toMutableList()
        val card = cards[index]

        if (card.isRevealed || card.isMatched) return

        // Reveal the tapped card
        cards[index] = card.copy(isRevealed = true)
        _memCards.value = cards

        if (firstSelectedCardIdx == null) {
            firstSelectedCardIdx = index
        } else if (secondSelectedCardIdx == null) {
            secondSelectedCardIdx = index
            _memMoves.value += 1
            checkForMatch()
        }
    }

    private fun checkForMatch() {
        val index1 = firstSelectedCardIdx ?: return
        val index2 = secondSelectedCardIdx ?: return

        isFlippingDisabled = true

        viewModelScope.launch {
            delay(750) // Duration to memorize mismatched cards
            val cards = _memCards.value.toMutableList()
            val card1 = cards[index1]
            val card2 = cards[index2]

            if (card1.emoji == card2.emoji) {
                // Match confirmed!
                cards[index1] = card1.copy(isMatched = true, isRevealed = true)
                cards[index2] = card2.copy(isMatched = true, isRevealed = true)
                _memMatches.value += 1

                if (cards.all { it.isMatched }) {
                    _memIsFinished.value = true
                    saveMemoryScore()
                }
            } else {
                // Mismatch, hide them back
                cards[index1] = card1.copy(isRevealed = false)
                cards[index2] = card2.copy(isRevealed = false)
            }

            _memCards.value = cards
            firstSelectedCardIdx = null
            secondSelectedCardIdx = null
            isFlippingDisabled = false
        }
    }

    private fun saveMemoryScore() {
        viewModelScope.launch {
            repository.saveScore(
                GameScore(
                    gameType = "MEMORY_MATCH",
                    scoreName = "Moves",
                    scoreValue = _memMoves.value,
                    metadata = "Finished in ${_memTimeElapsed.value} seconds with ${_memMoves.value} moves."
                )
            )
        }
    }

    // ----------------------------------------------------
    // SLIDING PUZZLE GAME STATE
    // ----------------------------------------------------
    private val _puzzleBoard = MutableStateFlow<List<Int>>(emptyList()) // numbers 1-15, and 0 for blank
    val puzzleBoard: StateFlow<List<Int>> = _puzzleBoard.asStateFlow()

    private val _puzzleMoves = MutableStateFlow(0)
    val puzzleMoves: StateFlow<Int> = _puzzleMoves.asStateFlow()

    private val _puzzleTimeElapsed = MutableStateFlow(0)
    val puzzleTimeElapsed: StateFlow<Int> = _puzzleTimeElapsed.asStateFlow()

    private val _puzzleIsSolved = MutableStateFlow(false)
    val puzzleIsSolved: StateFlow<Boolean> = _puzzleIsSolved.asStateFlow()

    private var puzzleTimerRunning = false

    fun startNewPuzzleGame() {
        // To generate a solvable sliding puzzle: start with solved state and slide randomly
        val board = (1..15).toList() + listOf(0)
        val mutableBoard = board.toMutableList()

        // Perform random moves to shuffle the board
        var blankIndex = 15
        val shuffleMoves = 150
        for (i in 0 until shuffleMoves) {
            val adjacent = getAdjacentIndices(blankIndex)
            val randomTarget = adjacent.random()
            mutableBoard[blankIndex] = mutableBoard[randomTarget]
            mutableBoard[randomTarget] = 0
            blankIndex = randomTarget
        }

        _puzzleBoard.value = mutableBoard
        _puzzleMoves.value = 0
        _puzzleTimeElapsed.value = 0
        _puzzleIsSolved.value = false

        startPuzzleTimer()
    }

    private fun startPuzzleTimer() {
        if (puzzleTimerRunning) return
        puzzleTimerRunning = true
        viewModelScope.launch {
            while (puzzleTimerRunning && !_puzzleIsSolved.value && _currentScreen.value == ActiveScreen.SLIDING_PUZZLE) {
                delay(1000)
                _puzzleTimeElapsed.value += 1
            }
            puzzleTimerRunning = false
        }
    }

    private fun getAdjacentIndices(index: Int): List<Int> {
        val row = index / 4
        val col = index % 4
        val result = mutableListOf<Int>()

        if (row > 0) result.add(index - 4) // Up
        if (row < 3) result.add(index + 4) // Down
        if (col > 0) result.add(index - 1) // Left
        if (col < 3) result.add(index + 1) // Right

        return result
    }

    fun makePuzzleMove(index: Int) {
        val board = _puzzleBoard.value
        val blankIndex = board.indexOf(0)
        if (blankIndex == -1 || _puzzleIsSolved.value) return

        // Safe check if tile is adjacent
        val targetRow = index / 4
        val targetCol = index % 4
        val blankRow = blankIndex / 4
        val blankCol = blankIndex % 4

        val isAdjacent = (kotlin.math.abs(targetRow - blankRow) + kotlin.math.abs(targetCol - blankCol)) == 1

        if (isAdjacent) {
            val newBoard = board.toMutableList()
            newBoard[blankIndex] = board[index]
            newBoard[index] = 0
            _puzzleBoard.value = newBoard
            _puzzleMoves.value += 1

            // Check if solved
            if (isPuzzleBoardSolved(newBoard)) {
                _puzzleIsSolved.value = true
                savePuzzleScore()
            }
        }
    }

    private fun isPuzzleBoardSolved(board: List<Int>): Boolean {
        // A 15-puzzle is solved if it has values 1..15, and 0 in the last slot (position 15)
        for (i in 0..14) {
            if (board[i] != i + 1) return false
        }
        return board[15] == 0
    }

    private fun savePuzzleScore() {
        viewModelScope.launch {
            repository.saveScore(
                GameScore(
                    gameType = "SLIDING_PUZZLE",
                    scoreName = "Moves",
                    scoreValue = _puzzleMoves.value,
                    metadata = "Cleared 15-puzzle in ${_puzzleTimeElapsed.value}s with ${_puzzleMoves.value} moves."
                )
            )
        }
    }

    // Clear scores from database
    fun clearLeaderboard() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}

class GameViewModelFactory(private val repository: GameRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
