package com.example.chess.model

enum class GameStatus {
    ONGOING,
    CHECKMATE,
    STALEMATE,
    DRAW_REPETITION,
    DRAW_50_MOVES,
    DRAW_INSUFFICIENT_MATERIAL,
    RESIGNED,
    TIME_EXPIRED
}

enum class GameMode {
    PVP,       // Pass and play
    PV_AI,     // Player vs Stockfish AI
    AI_VS_AI,  // Bot vs Bot
    ONLINE,    // Real-time Cloud multiplayer
    LAN        // Local Network (Wi-Fi) multiplayer
}

enum class PieceStyle(
    val id: String,
    val title: String,
    val subtitle: String
) {
    WIKIPEDIA("wikipedia", "Wikipedia Classic", "Authentic Colin Burnett tournament set"),
    ALPHA("alpha", "Neo Modern", "High-contrast geometric silhouette pieces"),
    CHESS7("chess7", "Tournament Staunton", "Refined classical tournament pieces"),
    CYBER("cyber", "Cyber Neon", "Futuristic high-tech glowing vector pieces"),
    ROYAL_GOLD("royal_gold", "Royal Gold & Obsidian", "Imperial gold & midnight onyx tournament styling"),
    WOODEN_LUXE("wooden_luxe", "Carved Woodcraft", "Warm artisan maple & walnut tones"),
    MINIMAL_BAUHAUS("bauhaus", "Bauhaus Minimal", "Iconic geometric Bauhaus modernist chess set")
}

enum class AiDifficulty(val label: String, val elo: Int, val searchDepth: Int) {
    BEGINNER("Beginner", 800, 2),
    CASUAL("Casual", 1200, 3),
    INTERMEDIATE("Intermediate", 1600, 4),
    ADVANCED("Advanced", 2000, 5),
    MASTER("Master (Stockfish)", 2500, 6)
}

enum class TimeControl(val label: String, val seconds: Long, val increment: Long) {
    UNLIMITED("Unlimited", 0, 0),
    BULLET_1("Bullet 1 min", 60, 0),
    BLITZ_3("Blitz 3 min", 180, 0),
    BLITZ_5("Blitz 5 min", 300, 0),
    RAPID_10("Rapid 10 min", 600, 0)
}
