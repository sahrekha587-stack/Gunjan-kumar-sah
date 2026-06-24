package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.data.GameDatabase
import com.example.data.GameRepository
import com.example.data.GameScore
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Init database & repository
        val database = GameDatabase.getDatabase(applicationContext)
        val repository = GameRepository(database.gameScoreDao())
        val viewModel = ViewModelProvider(this, GameViewModelFactory(repository))[GameViewModel::class.java]

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    GameAppContent(
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun GameAppContent(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(250))
            },
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                ActiveScreen.DASHBOARD -> ArcadeDashboard(viewModel)
                ActiveScreen.TIC_TAC_TOE -> TicTacToeScreen(viewModel)
                ActiveScreen.MEMORY_MATCH -> MemoryMatchScreen(viewModel)
                ActiveScreen.SLIDING_PUZZLE -> SlidingPuzzleScreen(viewModel)
                ActiveScreen.LEADERBOARD -> LeaderboardScreen(viewModel)
            }
        }
    }
}

// ----------------------------------------------------------------------------
// DASHBOARD VIEW
// ----------------------------------------------------------------------------
@Composable
fun ArcadeDashboard(viewModel: GameViewModel) {
    val colorPrimary = MaterialTheme.colorScheme.primary
    val colorSecondary = MaterialTheme.colorScheme.secondary
    val colorTertiary = MaterialTheme.colorScheme.tertiary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Hero Image Poster Frame with the custom generated arcade image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_game_banner_1782220927932),
                contentDescription = "Game Arcade Banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Beautiful Gradient Overlay to merge into SpaceDark Obsidian Background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )

            // Title and header overlay text
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = "Controller Icon",
                        tint = colorSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "GAME ZONE",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Classic Retro Arcade • High Fidelity Board Games",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dashboard Content List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Choose Your Game",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Game Card 1: Tic Tac Toe
            GameSelectionCard(
                title = "Tic-Tac-Toe (Grid)",
                description = "Play versus unbeatable smart computer AI or challenge a nearby friend local.",
                icon = Icons.Default.Grid3x3,
                accentColor = colorSecondary,
                testTag = "btn_play_ttt",
                onClick = {
                    viewModel.resetTttGame()
                    viewModel.navigateTo(ActiveScreen.TIC_TAC_TOE)
                }
            )

            // Game Card 2: Memory Match
            GameSelectionCard(
                title = "Animal Memory Match",
                description = "Flip and match cute animals. Tests concentration, speed, and focus.",
                icon = Icons.Default.Style,
                accentColor = colorPrimary,
                testTag = "btn_play_memory",
                onClick = {
                    viewModel.startNewMemoryGame()
                    viewModel.navigateTo(ActiveScreen.MEMORY_MATCH)
                }
            )

            // Game Card 3: 15-Puzzle
            GameSelectionCard(
                title = "15-Puzzle Sliding Tiles",
                description = "Slide scrambled numbers into order. Mathematically solvable board generator.",
                icon = Icons.Default.Extension,
                accentColor = colorTertiary,
                testTag = "btn_play_puzzle",
                onClick = {
                    viewModel.startNewPuzzleGame()
                    viewModel.navigateTo(ActiveScreen.SLIDING_PUZZLE)
                }
            )

            // Leaderboard / Score achievements Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { viewModel.navigateTo(ActiveScreen.LEADERBOARD) }
                    .testTag("btn_leaderboard"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ),
                border = BorderStroke(1.dp, colorPrimary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colorPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Trophy icon",
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Leaderboard & Stats",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "View highscores, histories & completion speed.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Arrow right",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun GameSelectionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.12f))
                    .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PLAY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = accentColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// ----------------------------------------------------------------------------
// GAME SCREEN: TIC-TAC-TOE
// ----------------------------------------------------------------------------
@Composable
fun TicTacToeScreen(viewModel: GameViewModel) {
    val board by viewModel.tttBoard.collectAsState()
    val winner by viewModel.tttWinner.collectAsState()
    val vsAI by viewModel.tttIsVsAI.collectAsState()
    val currentPlayer by viewModel.tttCurrentPlayer.collectAsState()
    val difficulty by viewModel.tttDifficulty.collectAsState()
    val isThinking by viewModel.tttIsThinking.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Core header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(ActiveScreen.DASHBOARD) },
                modifier = Modifier.testTag("ttt_back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Tic Tac Toe",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = { viewModel.resetTttGame() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Restart Game",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mode chip buttons
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = vsAI,
                    onClick = { viewModel.setTttVsAI(true) },
                    label = { Text("vs Computer (AI)") },
                    leadingIcon = if (vsAI) {
                        { Icon(Icons.Default.SmartToy, "AI") }
                    } else null
                )
                FilterChip(
                    selected = !vsAI,
                    onClick = { viewModel.setTttVsAI(false) },
                    label = { Text("2-Player Local") },
                    leadingIcon = if (!vsAI) {
                        { Icon(Icons.Default.People, "Local") }
                    } else null
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // AI Difficulty Badges Selector
        if (vsAI) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI Skill: ",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                listOf("EASY", "NORMAL", "HARD").forEach { diff ->
                    val isSelected = difficulty == diff
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface
                            )
                            .clickable { viewModel.setTttDifficulty(diff) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = diff,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Status Indicator Message
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            contentAlignment = Alignment.Center
        ) {
            val statusText = when {
                winner == "Draw" -> "It's a Draw! 🤝"
                winner == "X" -> if (vsAI) "You Won! 🎉🏆" else "Player X Won! 🎉"
                winner == "O" -> if (vsAI) "Computer Won! 🤖" else "Player O Won! 🎉"
                isThinking -> "Computer is plotting... 🤔"
                else -> if (vsAI) "Your Turn (X)" else "Player $currentPlayer Turn"
            }
            Text(
                text = statusText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (winner != null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3x3 Gaming Grid
        Column(
            modifier = Modifier
                .width(310.dp)
                .height(310.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                .border(2.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (row in 0..2) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (col in 0..2) {
                        val cellIndex = row * 3 + col
                        val cellValue = board[cellIndex]

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (cellValue != "") MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                                    else MaterialTheme.colorScheme.background.copy(alpha = 0.3f)
                                )
                                .clickable {
                                    viewModel.makeTttMove(cellIndex)
                                }
                                .border(
                                    width = if (cellValue != "") 1.5.dp else 0.dp,
                                    color = if (cellValue == "X") MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    else if (cellValue == "O") MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                    else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .testTag("ttt_cell_$cellIndex"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (cellValue != "") {
                                Text(
                                    text = cellValue,
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (cellValue == "X") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Game action control button
        if (winner != null) {
            Button(
                onClick = { viewModel.resetTttGame() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("ttt_play_again"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Text("Play Again", fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
            }
        } else {
            OutlinedButton(
                onClick = { viewModel.resetTttGame() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
            ) {
                Text("Reset Match", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ----------------------------------------------------------------------------
// GAME SCREEN: MEMORY MATCH
// ----------------------------------------------------------------------------
@Composable
fun MemoryMatchScreen(viewModel: GameViewModel) {
    val cards by viewModel.memCards.collectAsState()
    val moves by viewModel.memMoves.collectAsState()
    val matches by viewModel.memMatches.collectAsState()
    val isFinished by viewModel.memIsFinished.collectAsState()
    val timeElapsed by viewModel.memTimeElapsed.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(ActiveScreen.DASHBOARD) },
                modifier = Modifier.testTag("memory_back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Memory Match",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = { viewModel.startNewMemoryGame() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Restart",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Statistics Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatsDisplayColumn(title = "Time", value = formatSeconds(timeElapsed), icon = Icons.Default.Timer, tint = MaterialTheme.colorScheme.secondary)
                StatsDisplayColumn(title = "Moves", value = moves.toString(), icon = Icons.Default.SwapCalls, tint = MaterialTheme.colorScheme.primary)
                StatsDisplayColumn(title = "Matched", value = "$matches / 8", icon = Icons.Default.CheckCircle, tint = MaterialTheme.colorScheme.tertiary)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Grid of customizable paired match cards
        if (cards.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(cards) { index, card ->
                    MemoryCardView(card = card, cardIndex = index, onClick = { viewModel.revealCard(index) })
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isFinished) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "VICTORY! 🎉🎈",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Completed in $moves moves under ${formatSeconds(timeElapsed)}!",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = { viewModel.startNewMemoryGame() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("memory_restart_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(if (isFinished) "Play Again" else "Restart Game", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun MemoryCardView(
    card: MemoryCard,
    cardIndex: Int,
    onClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (card.isRevealed || card.isMatched) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing),
        label = "CardFlipAnimation"
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (card.isRevealed || card.isMatched) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
            .border(
                BorderStroke(
                    width = 1.5.dp,
                    color = if (card.isMatched) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                enabled = !card.isRevealed && !card.isMatched,
                onClick = onClick
            )
            .testTag("memory_card_$cardIndex"),
        contentAlignment = Alignment.Center
    ) {
        if (rotation > 90f) {
            // Revealing backend emoji of cards list
            Text(
                text = card.emoji,
                fontSize = 28.sp,
                modifier = Modifier.graphicsLayer {
                    rotationY = 180f // Counteract flipped container mirroring
                }
            )
        } else {
            // Standard back pattern decoration
            Icon(
                imageVector = Icons.Default.QuestionMark,
                contentDescription = "?",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ----------------------------------------------------------------------------
// GAME SCREEN: 15-PUZZLE SLIDING GRID
// ----------------------------------------------------------------------------
@Composable
fun SlidingPuzzleScreen(viewModel: GameViewModel) {
    val board by viewModel.puzzleBoard.collectAsState()
    val moves by viewModel.puzzleMoves.collectAsState()
    val timeElapsed by viewModel.puzzleTimeElapsed.collectAsState()
    val isSolved by viewModel.puzzleIsSolved.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(ActiveScreen.DASHBOARD) },
                modifier = Modifier.testTag("puzzle_back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "15-Puzzle Sliding",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = { viewModel.startNewPuzzleGame() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Restart",
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // High contrast performance panels
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatsDisplayColumn(title = "Time Spent", value = formatSeconds(timeElapsed), icon = Icons.Default.Timer, tint = MaterialTheme.colorScheme.secondary)
                StatsDisplayColumn(title = "Moves Done", value = moves.toString(), icon = Icons.Default.Gamepad, tint = MaterialTheme.colorScheme.tertiary)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4x4 Grid representation is guaranteed solvable
        if (board.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)), RoundedCornerShape(16.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(board) { index, tileValue ->
                    PuzzleTileView(
                        tileNumber = tileValue,
                        tileIndex = index,
                        onClick = { viewModel.makePuzzleMove(index) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isSolved) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SOLVED! 🏆🎯",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Excellent job! Cleared in $moves moves under ${formatSeconds(timeElapsed)}.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = { viewModel.startNewPuzzleGame() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("puzzle_shuffle_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(if (isSolved) "Play Again" else "Shuffle Board", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun PuzzleTileView(
    tileNumber: Int,
    tileIndex: Int,
    onClick: () -> Unit
) {
    if (tileNumber == 0) {
        // Render blank tile empty
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                .border(
                    BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(14.dp)
                )
        )
    } else {
        // Interactive solid values block
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    BorderStroke(1.5.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(14.dp)
                )
                .clickable(
                    onClick = onClick
                )
                .testTag("puzzle_tile_$tileIndex"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tileNumber.toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun StatsDisplayColumn(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun formatSeconds(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", m, s)
}

// ----------------------------------------------------------------------------
// LEADERBOARD & STATS VIEW
// ----------------------------------------------------------------------------
@Composable
fun LeaderboardScreen(viewModel: GameViewModel) {
    val scoresList by viewModel.highScoresList.collectAsState(initial = emptyList())
    var selectedFilterTab by remember { mutableStateOf("ALL") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(ActiveScreen.DASHBOARD) },
                modifier = Modifier.testTag("leaderboard_back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Leaderboard History",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = { viewModel.clearLeaderboard() }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear scores",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        ScrollableTabRow(
            selectedTabIndex = when (selectedFilterTab) {
                "ALL" -> 0
                "TIC_TAC_TOE" -> 1
                "MEMORY_MATCH" -> 2
                else -> 3
            },
            containerColor = Color.Transparent,
            divider = {},
            edgePadding = 0.dp
        ) {
            listOf("ALL", "TIC_TAC_TOE", "MEMORY_MATCH", "SLIDING_PUZZLE").forEach { tab ->
                val isSelected = selectedFilterTab == tab
                Tab(
                    selected = isSelected,
                    onClick = { selectedFilterTab = tab },
                    text = {
                        Text(
                            text = when (tab) {
                                "ALL" -> "Show All"
                                "TIC_TAC_TOE" -> "Tic Tac Toe"
                                "MEMORY_MATCH" -> "Memory Match"
                                else -> "15-Puzzle"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val filteredList = scoresList.filter {
            selectedFilterTab == "ALL" || it.gameType == selectedFilterTab
        }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.HistoryToggleOff,
                        contentDescription = "Empty",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "History Clear",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Play a complete game round to record highscore progress!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 30.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList) { score ->
                    LeaderboardScoreItem(score)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.navigateTo(ActiveScreen.DASHBOARD) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Back to Dashboard", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun LeaderboardScoreItem(score: GameScore) {
    val formatter = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    val dateStr = formatter.format(Date(score.timestamp))

    val (gameTitle, gameIcon, accentColor) = when (score.gameType) {
        "TIC_TAC_TOE" -> Triple("Tic-Tac-Toe", Icons.Default.Grid3x3, MaterialTheme.colorScheme.secondary)
        "MEMORY_MATCH" -> Triple("Memory Match", Icons.Default.Style, MaterialTheme.colorScheme.primary)
        else -> Triple("15-Sliding Puzzle", Icons.Default.Extension, MaterialTheme.colorScheme.tertiary)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = gameIcon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = gameTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = score.scoreName + ": " + score.scoreValue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = accentColor
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = score.metadata,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = dateStr,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    if (score.difficulty != "NORMAL") {
                        Text(
                            text = score.difficulty,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(accentColor.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
