package com.sketchsync.data.model

import java.util.UUID


data class PictionaryGame(
    val id: String = UUID.randomUUID().toString(),
    val roomId: String = "",
    val currentDrawerId: String = "",
    val currentDrawerName: String = "",
    val currentWord: String = "",
    val wordHint: String = "",  // 提示（显示部分字母）
    val timeRemaining: Int = 60,
    val totalTime: Int = 60,
    val round: Int = 1,
    val maxRounds: Int = 5,
    val scores: Map<String, Int> = emptyMap(),
    val guessedUsers: List<String> = emptyList(),
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val turnOrder: List<String> = emptyList(),
    val startedAt: Long = 0
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "roomId" to roomId,
        "currentDrawerId" to currentDrawerId,
        "currentDrawerName" to currentDrawerName,
        "currentWord" to currentWord,
        "wordHint" to wordHint,
        "timeRemaining" to timeRemaining,
        "totalTime" to totalTime,
        "round" to round,
        "maxRounds" to maxRounds,
        "scores" to scores,
        "guessedUsers" to guessedUsers,
        "isActive" to isActive,
        "isPaused" to isPaused,
        "turnOrder" to turnOrder,
        "startedAt" to startedAt
    )
    
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(map: Map<String, Any?>, gameId: String = ""): PictionaryGame {
            return PictionaryGame(
                id = map["id"] as? String ?: gameId,
                roomId = map["roomId"] as? String ?: "",
                currentDrawerId = map["currentDrawerId"] as? String ?: "",
                currentDrawerName = map["currentDrawerName"] as? String ?: "",
                currentWord = map["currentWord"] as? String ?: "",
                wordHint = map["wordHint"] as? String ?: "",
                timeRemaining = (map["timeRemaining"] as? Number)?.toInt() ?: 60,
                totalTime = (map["totalTime"] as? Number)?.toInt() ?: 60,
                round = (map["round"] as? Number)?.toInt() ?: 1,
                maxRounds = (map["maxRounds"] as? Number)?.toInt() ?: 5,
                scores = map["scores"] as? Map<String, Int> ?: emptyMap(),
                guessedUsers = map["guessedUsers"] as? List<String> ?: emptyList(),
                isActive = map["isActive"] as? Boolean ?: false,
                isPaused = map["isPaused"] as? Boolean ?: false,
                turnOrder = map["turnOrder"] as? List<String> ?: emptyList(),
                startedAt = (map["startedAt"] as? Number)?.toLong() ?: 0
            )
        }
    }
}


data class GuessMessage(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val userName: String = "",
    val message: String = "",
    val isCorrect: Boolean = false,
    val isSystemMessage: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 游戏题目库
 */
object WordBank {
    val categories = mapOf(
        "Animals" to listOf(
            "Cat", "Dog", "Elephant", "Giraffe", "Penguin", "Lion", "Tiger", "Panda", 
            "Rabbit", "Monkey", "Snake", "Crocodile", "Eagle", "Butterfly", "Spider", "Shark"
        ),
        "Food" to listOf(
            "Pizza", "Burger", "Sushi", "Ice Cream", "Cake", "Apple", "Banana", "Watermelon",
            "Noodles", "Dumplings", "Bun", "Fried Chicken", "French Fries", "Sandwich", "Chocolate", "Coffee"
        ),
        "Objects" to listOf(
            "Phone", "Computer", "Car", "Airplane", "Bicycle", "Book", "Glasses", "Umbrella",
            "Key", "Clock", "Television", "Sofa", "Bed", "Table", "Chair", "Light Bulb"
        ),
        "Actions" to listOf(
            "Running", "Dancing", "Singing", "Swimming", "Sleeping", "Eating", "Drinking", "Reading",
            "Calling", "Taking Photo", "Driving", "Cycling", "Playing Piano", "Soccer", "Basketball", "Skiing"
        ),
        "Places" to listOf(
            "School", "Hospital", "Park", "Beach", "Mountain", "Library", "Supermarket", "Restaurant",
            "Cinema", "Airport", "Train Station", "Bank", "Church", "Museum", "Zoo", "Amusement Park"
        ),
        "Jobs" to listOf(
            "Doctor", "Teacher", "Police", "Firefighter", "Chef", "Programmer", "Painter", "Singer",
            "Actor", "Lawyer", "Reporter", "Pilot", "Waiter", "Scientist", "Farmer", "Driver"
        )
    )
    
    fun getRandomWord(): Pair<String, String> {
        val category = categories.keys.random()
        val word = categories[category]?.random() ?: "Cat"
        return category to word
    }
    
    /**
     * 获取提示（显示部分字母）
     */
    fun getHint(word: String): String {
        if (word.length <= 2) return "_ ".repeat(word.length).trim()
        val revealCount = word.length / 3
        val indices = (word.indices).shuffled().take(revealCount)
        return word.mapIndexed { index, char ->
            if (index in indices) char else '_'
        }.joinToString(" ")
    }
}
