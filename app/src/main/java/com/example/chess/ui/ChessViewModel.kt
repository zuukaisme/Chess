package com.example.chess.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chess.engine.ChessBoard
import com.example.chess.engine.ChessEngine
import com.example.chess.model.AiDifficulty
import com.example.chess.model.ChessColor
import com.example.chess.model.GameMode
import com.example.chess.model.GameStatus
import com.example.chess.model.Move
import com.example.chess.model.PieceType
import com.example.chess.model.Square
import com.example.chess.model.TimeControl
import com.example.chess.multiplayer.MultiplayerService
import com.example.chess.puzzle.ChessPuzzle
import com.example.chess.puzzle.PuzzleRepository
import com.example.chess.sound.SoundManager
import com.example.chess.ui.theme.BoardTheme
import com.example.chess.model.PieceStyle
import com.example.chess.multiplayer.LanConnectionState
import com.example.chess.multiplayer.LanMultiplayerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ChessUiState(
    val board: ChessBoard = ChessBoard(),
    val gameMode: GameMode = GameMode.PV_AI,
    val aiDifficulty: AiDifficulty = AiDifficulty.INTERMEDIATE,
    val playerColor: ChessColor = ChessColor.WHITE,
    val boardTheme: BoardTheme = BoardTheme.EMERALD,
    val pieceStyle: PieceStyle = PieceStyle.WIKIPEDIA,
    val isFlipped: Boolean = false,
    val selectedSquare: Square? = null,
    val legalMoves: List<Move> = emptyList(),
    val moveHistory: List<Move> = emptyList(),
    val lastMove: Move? = null,
    val hintMove: Move? = null,
    val evalCentipawns: Int = 0,
    val gameStatus: GameStatus = GameStatus.ONGOING,
    val isGameOverDismissed: Boolean = false,
    val isAiThinking: Boolean = false,
    val showPromotionDialog: Boolean = false,
    val pendingPromotionFrom: Square? = null,
    val pendingPromotionTo: Square? = null,
    val timeControl: TimeControl = TimeControl.BLITZ_5,
    val whiteTimeSeconds: Long = 300,
    val blackTimeSeconds: Long = 300,
    val isTimerActive: Boolean = false,
    val soundEnabled: Boolean = true,
    val showLegalMoveHints: Boolean = true,
    val isAiVsAiPlaying: Boolean = false,
    val aiVsAiSpeedMs: Long = 1000L,
    val lanOpponentName: String? = null,
    // Puzzle state
    val currentPuzzle: ChessPuzzle? = null,
    val puzzleMoveIndex: Int = 0,
    val puzzleSolved: Boolean = false,
    val puzzleFailed: Boolean = false,
    val puzzleRating: Int = 1200
)

class ChessViewModel(application: Application) : AndroidViewModel(application) {

    private val engine = ChessEngine()
    private val soundManager = SoundManager(application)
    val multiplayerService = MultiplayerService(application)
    val lanMultiplayerService = LanMultiplayerService(application)
    val authManager = com.example.chess.auth.AuthManager(application)

    private val _uiState = MutableStateFlow(ChessUiState())
    val uiState: StateFlow<ChessUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var aiVsAiJob: Job? = null

    init {
        startTimerLoop()
        updateEvaluation()

        lanMultiplayerService.onMoveReceived = { move ->
            executeLanOpponentMove(move)
        }
        lanMultiplayerService.onResignReceived = { color ->
            resign(color)
        }
        lanMultiplayerService.onRematchRequested = {
            val state = _uiState.value
            if (state.gameMode == GameMode.LAN) {
                resetGame(
                    mode = GameMode.LAN,
                    color = state.playerColor.opposite(),
                    timeControl = state.timeControl
                )
            }
        }

        viewModelScope.launch {
            lanMultiplayerService.connectionState.collect { connState ->
                if (connState is LanConnectionState.CONNECTED) {
                    resetGame(
                        mode = GameMode.LAN,
                        color = connState.myColor,
                        timeControl = connState.timeControl
                    )
                    _uiState.update {
                        it.copy(
                            isFlipped = connState.myColor == ChessColor.BLACK,
                            lanOpponentName = connState.opponentName
                        )
                    }
                }
            }
        }
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val state = _uiState.value
                if (state.isTimerActive && state.gameStatus == GameStatus.ONGOING && state.timeControl != TimeControl.UNLIMITED) {
                    if (state.board.turn == ChessColor.WHITE) {
                        val newTime = (state.whiteTimeSeconds - 1).coerceAtLeast(0)
                        _uiState.update { it.copy(whiteTimeSeconds = newTime) }
                        if (newTime == 0L) {
                            handleTimeForfeit(ChessColor.WHITE)
                        }
                    } else {
                        val newTime = (state.blackTimeSeconds - 1).coerceAtLeast(0)
                        _uiState.update { it.copy(blackTimeSeconds = newTime) }
                        if (newTime == 0L) {
                            handleTimeForfeit(ChessColor.BLACK)
                        }
                    }
                }
            }
        }
    }

    private fun handleTimeForfeit(loserColor: ChessColor) {
        _uiState.update {
            it.copy(
                gameStatus = GameStatus.TIME_EXPIRED,
                isTimerActive = false,
                isGameOverDismissed = false
            )
        }
        soundManager.playGameEndSound(_uiState.value.soundEnabled)
    }

    fun onSquareClicked(square: Square) {
        val state = _uiState.value
        if (state.gameStatus != GameStatus.ONGOING || state.isAiThinking) return

        // In PvAI mode, prevent player from moving pieces of the AI
        if (state.gameMode == GameMode.PV_AI && state.board.turn != state.playerColor) {
            return
        }
        // In LAN mode, prevent player from moving opponent's pieces
        if (state.gameMode == GameMode.LAN && state.board.turn != state.playerColor) {
            return
        }
        // In AI vs AI mode, moves are controlled by bot
        if (state.gameMode == GameMode.AI_VS_AI) return

        val piece = state.board.getPiece(square)

        // If clicking a target square of the selected piece
        if (state.selectedSquare != null) {
            val matchingMove = state.legalMoves.find { it.to == square }
            if (matchingMove != null) {
                // Check if move requires pawn promotion selection
                if (matchingMove.piece.type == PieceType.PAWN &&
                    ((matchingMove.piece.color == ChessColor.WHITE && square.row == 0) ||
                            (matchingMove.piece.color == ChessColor.BLACK && square.row == 7))
                ) {
                    _uiState.update {
                        it.copy(
                            showPromotionDialog = true,
                            pendingPromotionFrom = state.selectedSquare,
                            pendingPromotionTo = square
                        )
                    }
                    return
                }

                executeMove(matchingMove)
                return
            }
        }

        // If clicking one's own piece, select it and show legal moves
        if (piece != null && piece.color == state.board.turn) {
            val allLegal = state.board.generateLegalMoves(state.board.turn)
            val pieceLegal = allLegal.filter { it.from == square }
            _uiState.update {
                it.copy(
                    selectedSquare = square,
                    legalMoves = pieceLegal,
                    hintMove = null
                )
            }
        } else {
            // Deselect
            _uiState.update {
                it.copy(
                    selectedSquare = null,
                    legalMoves = emptyList()
                )
            }
        }
    }

    fun executePromotion(pieceType: PieceType) {
        val state = _uiState.value
        val from = state.pendingPromotionFrom ?: return
        val to = state.pendingPromotionTo ?: return
        val piece = state.board.getPiece(from) ?: return

        val promoMove = Move(
            from = from,
            to = to,
            piece = piece,
            capturedPiece = state.board.getPiece(to),
            promotionType = pieceType
        )

        _uiState.update {
            it.copy(
                showPromotionDialog = false,
                pendingPromotionFrom = null,
                pendingPromotionTo = null
            )
        }

        executeMove(promoMove)
    }

    fun dismissPromotionDialog() {
        _uiState.update {
            it.copy(
                showPromotionDialog = false,
                pendingPromotionFrom = null,
                pendingPromotionTo = null,
                selectedSquare = null,
                legalMoves = emptyList()
            )
        }
    }

    private fun executeMove(move: Move) {
        val state = _uiState.value
        val board = state.board
        val success = board.makeMove(move)
        if (!success) return

        val newStatus = board.checkGameStatus()
        val isCapture = move.capturedPiece != null || move.isEnPassant
        val isCheck = board.isInCheck(board.turn)

        if (newStatus != GameStatus.ONGOING) {
            soundManager.playGameEndSound(state.soundEnabled)
        } else if (isCheck) {
            soundManager.playCheckSound(state.soundEnabled)
        } else if (isCapture) {
            soundManager.playCaptureSound(state.soundEnabled)
        } else {
            soundManager.playMoveSound(state.soundEnabled)
        }

        // If local move made in LAN mode, send to opponent over socket
        if (state.gameMode == GameMode.LAN && state.board.turn != state.playerColor) {
            lanMultiplayerService.sendMove(move)
        }

        val updatedHistory = state.moveHistory + move

        _uiState.update {
            it.copy(
                selectedSquare = null,
                legalMoves = emptyList(),
                lastMove = move,
                hintMove = null,
                moveHistory = updatedHistory,
                gameStatus = newStatus,
                isGameOverDismissed = false,
                isTimerActive = newStatus == GameStatus.ONGOING
            )
        }

        updateEvaluation()

        // Handle puzzle check if in puzzle mode
        if (state.currentPuzzle != null) {
            handlePuzzleStep(move)
            return
        }

        // If PvAI mode and game is ongoing, trigger engine move
        if (state.gameMode == GameMode.PV_AI && newStatus == GameStatus.ONGOING && board.turn != state.playerColor) {
            triggerAiMove()
        }
    }

    private fun executeLanOpponentMove(move: Move) {
        viewModelScope.launch(Dispatchers.Main) {
            val state = _uiState.value
            if (state.gameMode == GameMode.LAN && state.board.turn != state.playerColor) {
                val legalMoves = state.board.generateLegalMoves(state.board.turn)
                val matched = legalMoves.find { it.from == move.from && it.to == move.to }
                if (matched != null) {
                    val finalMove = if (move.promotionType != null) {
                        matched.copy(promotionType = move.promotionType)
                    } else matched
                    executeMove(finalMove)
                }
            }
        }
    }

    private fun updateEvaluation() {
        viewModelScope.launch(Dispatchers.Default) {
            val score = engine.evaluatePosition(_uiState.value.board)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(evalCentipawns = score) }
            }
        }
    }

    fun triggerAiMove() {
        val state = _uiState.value
        if (state.gameStatus != GameStatus.ONGOING) return

        _uiState.update { it.copy(isAiThinking = true) }

        viewModelScope.launch(Dispatchers.Default) {
            // Realistic short delay for natural human feel
            delay(400)
            val moveSequence = _uiState.value.moveHistory.joinToString(" ") { it.uci }
            val bestMove = engine.findBestMove(_uiState.value.board, state.aiDifficulty, moveSequence)

            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isAiThinking = false) }
                if (bestMove != null && _uiState.value.gameStatus == GameStatus.ONGOING) {
                    executeMove(bestMove)
                }
            }
        }
    }

    fun requestHint() {
        viewModelScope.launch(Dispatchers.Default) {
            val state = _uiState.value
            if (state.gameStatus != GameStatus.ONGOING) return@launch
            val moveSequence = state.moveHistory.joinToString(" ") { it.uci }
            val hint = engine.findBestMove(state.board, AiDifficulty.MASTER, moveSequence)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(hintMove = hint) }
            }
        }
    }

    fun undoMove() {
        val state = _uiState.value
        if (state.moveHistory.isEmpty() || state.isAiThinking) return

        // In PvAI, undo 2 moves (AI move and player move) if it's player's turn
        val undoCount = if (state.gameMode == GameMode.PV_AI && state.board.turn == state.playerColor && state.moveHistory.size >= 2) 2 else 1

        for (i in 0 until undoCount) {
            state.board.undoMove()
        }

        val newHistory = state.moveHistory.dropLast(undoCount)
        val last = newHistory.lastOrNull()

        _uiState.update {
            it.copy(
                selectedSquare = null,
                legalMoves = emptyList(),
                lastMove = last,
                hintMove = null,
                moveHistory = newHistory,
                gameStatus = it.board.checkGameStatus()
            )
        }
        updateEvaluation()
    }

    fun resetGame(
        mode: GameMode = _uiState.value.gameMode,
        difficulty: AiDifficulty = _uiState.value.aiDifficulty,
        color: ChessColor = _uiState.value.playerColor,
        timeControl: TimeControl = _uiState.value.timeControl
    ) {
        aiVsAiJob?.cancel()
        val newBoard = ChessBoard()
        _uiState.update {
            it.copy(
                board = newBoard,
                gameMode = mode,
                aiDifficulty = difficulty,
                playerColor = color,
                timeControl = timeControl,
                whiteTimeSeconds = if (timeControl == TimeControl.UNLIMITED) 0L else timeControl.seconds,
                blackTimeSeconds = if (timeControl == TimeControl.UNLIMITED) 0L else timeControl.seconds,
                isTimerActive = timeControl != TimeControl.UNLIMITED,
                selectedSquare = null,
                legalMoves = emptyList(),
                moveHistory = emptyList(),
                lastMove = null,
                hintMove = null,
                evalCentipawns = 0,
                gameStatus = GameStatus.ONGOING,
                isGameOverDismissed = false,
                isAiThinking = false,
                isAiVsAiPlaying = false,
                isFlipped = color == ChessColor.BLACK,
                currentPuzzle = null
            )
        }
        updateEvaluation()

        // If player chose Black in PvAI, AI makes first move as White
        if (mode == GameMode.PV_AI && color == ChessColor.BLACK) {
            triggerAiMove()
        }
    }

    fun resign(color: ChessColor) {
        val state = _uiState.value
        if (state.gameMode == GameMode.LAN && state.playerColor == color) {
            lanMultiplayerService.sendResign(color)
        }
        _uiState.update {
            it.copy(
                gameStatus = GameStatus.RESIGNED,
                isTimerActive = false,
                isAiVsAiPlaying = false,
                isGameOverDismissed = false
            )
        }
        soundManager.playGameEndSound(_uiState.value.soundEnabled)
    }

    fun setPieceStyle(style: PieceStyle) {
        _uiState.update { it.copy(pieceStyle = style) }
    }

    fun dismissGameOver() {
        _uiState.update { it.copy(isGameOverDismissed = true) }
    }

    fun reopenGameOver() {
        _uiState.update { it.copy(isGameOverDismissed = false) }
    }

    fun hostLanMatch(playerName: String, color: ChessColor, timeControl: TimeControl) {
        lanMultiplayerService.startHosting(playerName, color, timeControl)
    }

    fun connectToLanHost(hostIp: String, port: Int, playerName: String) {
        lanMultiplayerService.connectToHost(hostIp, port, playerName)
    }

    fun requestLanRematch() {
        lanMultiplayerService.sendRematch()
        val state = _uiState.value
        resetGame(
            mode = GameMode.LAN,
            color = state.playerColor.opposite(),
            timeControl = state.timeControl
        )
    }

    // AI vs AI Controls
    fun toggleAiVsAiPlay() {
        val currentlyPlaying = _uiState.value.isAiVsAiPlaying
        if (currentlyPlaying) {
            aiVsAiJob?.cancel()
            _uiState.update { it.copy(isAiVsAiPlaying = false) }
        } else {
            _uiState.update { it.copy(isAiVsAiPlaying = true) }
            startAiVsAiLoop()
        }
    }

    fun stepAiVsAi() {
        if (_uiState.value.gameStatus == GameStatus.ONGOING && !_uiState.value.isAiThinking) {
            triggerAiMove()
        }
    }

    private fun startAiVsAiLoop() {
        aiVsAiJob?.cancel()
        aiVsAiJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive && _uiState.value.isAiVsAiPlaying && _uiState.value.gameStatus == GameStatus.ONGOING) {
                delay(_uiState.value.aiVsAiSpeedMs)
                if (!_uiState.value.isAiVsAiPlaying) break
                val moveSequence = _uiState.value.moveHistory.joinToString(" ") { it.uci }
                val bestMove = engine.findBestMove(_uiState.value.board, _uiState.value.aiDifficulty, moveSequence)
                if (bestMove != null) {
                    withContext(Dispatchers.Main) {
                        executeMove(bestMove)
                    }
                } else {
                    break
                }
            }
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isAiVsAiPlaying = false) }
            }
        }
    }

    fun setAiVsAiSpeed(speedMs: Long) {
        _uiState.update { it.copy(aiVsAiSpeedMs = speedMs) }
    }

    // Settings
    fun setBoardTheme(theme: BoardTheme) {
        _uiState.update { it.copy(boardTheme = theme) }
    }

    fun flipBoard() {
        _uiState.update { it.copy(isFlipped = !it.isFlipped) }
    }

    fun toggleSound() {
        _uiState.update { it.copy(soundEnabled = !it.soundEnabled) }
    }

    fun toggleHints() {
        _uiState.update { it.copy(showLegalMoveHints = !it.showLegalMoveHints) }
    }

    fun setDifficulty(difficulty: AiDifficulty) {
        _uiState.update { it.copy(aiDifficulty = difficulty) }
    }

    // Puzzles
    fun loadPuzzle(puzzle: ChessPuzzle) {
        val newBoard = ChessBoard()
        newBoard.loadFen(puzzle.initialFen)
        _uiState.update {
            it.copy(
                board = newBoard,
                currentPuzzle = puzzle,
                puzzleMoveIndex = 0,
                puzzleSolved = false,
                puzzleFailed = false,
                selectedSquare = null,
                legalMoves = emptyList(),
                lastMove = null,
                hintMove = null,
                moveHistory = emptyList(),
                gameStatus = GameStatus.ONGOING,
                playerColor = puzzle.playerColor,
                isFlipped = puzzle.playerColor == ChessColor.BLACK
            )
        }
        updateEvaluation()
    }

    private fun handlePuzzleStep(playerMove: Move) {
        val puzzle = _uiState.value.currentPuzzle ?: return
        val expectedUci = puzzle.solutionUci.getOrNull(_uiState.value.puzzleMoveIndex)

        if (playerMove.uci == expectedUci) {
            val nextIndex = _uiState.value.puzzleMoveIndex + 1
            if (nextIndex >= puzzle.solutionUci.size) {
                // Puzzle Solved!
                _uiState.update {
                    it.copy(
                        puzzleSolved = true,
                        puzzleRating = it.puzzleRating + 15
                    )
                }
                soundManager.playGameEndSound(_uiState.value.soundEnabled)
            } else {
                // Computer plays response
                _uiState.update { it.copy(puzzleMoveIndex = nextIndex) }
                val computerUci = puzzle.solutionUci[nextIndex]
                viewModelScope.launch {
                    delay(500)
                    val computerMove = _uiState.value.board.generateLegalMoves().find { it.uci == computerUci }
                    if (computerMove != null) {
                        executeMove(computerMove)
                        _uiState.update { it.copy(puzzleMoveIndex = nextIndex + 1) }
                    }
                }
            }
        } else {
            // Wrong move
            _uiState.update {
                it.copy(
                    puzzleFailed = true,
                    puzzleRating = (it.puzzleRating - 10).coerceAtLeast(800)
                )
            }
        }
    }
}
