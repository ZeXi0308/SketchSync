package com.sketchsync.data.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for PictionaryGame, WordBank, and GuessMessage.
 * Covers: toMap/fromMap serialization, WordBank hint generation, random word selection.
 */
class PictionaryGameTest {

    // ==================== PictionaryGame toMap / fromMap ====================

    @Test
    fun `toMap produces correct keys`() {
        val game = createTestGame()
        val map = game.toMap()

        assertTrue(map.containsKey("id"))
        assertTrue(map.containsKey("roomId"))
        assertTrue(map.containsKey("currentDrawerId"))
        assertTrue(map.containsKey("currentWord"))
        assertTrue(map.containsKey("wordHint"))
        assertTrue(map.containsKey("timeRemaining"))
        assertTrue(map.containsKey("round"))
        assertTrue(map.containsKey("maxRounds"))
        assertTrue(map.containsKey("scores"))
        assertTrue(map.containsKey("isActive"))
    }

    @Test
    fun `fromMap reconstructs game correctly`() {
        val original = createTestGame()
        val map = original.toMap()
        val restored = PictionaryGame.fromMap(map)

        assertEquals(original.id, restored.id)
        assertEquals(original.roomId, restored.roomId)
        assertEquals(original.currentDrawerId, restored.currentDrawerId)
        assertEquals(original.currentWord, restored.currentWord)
        assertEquals(original.wordHint, restored.wordHint)
        assertEquals(original.timeRemaining, restored.timeRemaining)
        assertEquals(original.round, restored.round)
        assertEquals(original.maxRounds, restored.maxRounds)
        assertEquals(original.isActive, restored.isActive)
    }

    @Test
    fun `fromMap with empty map uses defaults`() {
        val game = PictionaryGame.fromMap(emptyMap())

        assertEquals("", game.roomId)
        assertEquals("", game.currentDrawerId)
        assertEquals("", game.currentWord)
        assertEquals(60, game.timeRemaining)
        assertEquals(60, game.totalTime)
        assertEquals(1, game.round)
        assertEquals(5, game.maxRounds)
        assertFalse(game.isActive)
        assertFalse(game.isPaused)
    }

    @Test
    fun `fromMap with gameId fallback`() {
        val game = PictionaryGame.fromMap(emptyMap(), gameId = "fallback-id")
        assertEquals("fallback-id", game.id)
    }

    // ==================== WordBank ====================

    @Test
    fun `getRandomWord returns valid category and word`() {
        val (category, word) = WordBank.getRandomWord()
        assertTrue("Category should exist", WordBank.categories.containsKey(category))
        assertTrue("Word should be in category", WordBank.categories[category]!!.contains(word))
    }

    @Test
    fun `getRandomWord returns different results over multiple calls`() {
        // Run 20 times and collect unique results; at least 2 should differ
        val results = (1..20).map { WordBank.getRandomWord() }.toSet()
        assertTrue("Should get at least 2 different results in 20 calls", results.size >= 2)
    }

    @Test
    fun `all categories have at least one word`() {
        WordBank.categories.forEach { (category, words) ->
            assertTrue("Category '$category' should not be empty", words.isNotEmpty())
        }
    }

    @Test
    fun `WordBank has expected categories`() {
        val categories = WordBank.categories.keys
        assertTrue(categories.contains("Animals"))
        assertTrue(categories.contains("Food"))
        assertTrue(categories.contains("Objects"))
        assertTrue(categories.contains("Actions"))
        assertTrue(categories.contains("Places"))
        assertTrue(categories.contains("Jobs"))
    }

    // ==================== Hint Generation ====================

    @Test
    fun `getHint returns correct length`() {
        val hint = WordBank.getHint("Cat")
        val chars = hint.replace(" ", "")
        assertEquals("Cat".length, chars.length)
    }

    @Test
    fun `getHint contains underscores`() {
        val hint = WordBank.getHint("Elephant")
        assertTrue("Hint should contain underscores", hint.contains("_"))
    }

    @Test
    fun `getHint reveals some letters`() {
        // For long words, at least one letter should be revealed
        val hint = WordBank.getHint("Elephant") // 8 chars, should reveal 8/3 = 2
        val revealed = hint.replace(" ", "").count { it != '_' }
        assertTrue("Should reveal at least 1 letter", revealed >= 1)
    }

    @Test
    fun `getHint for short word returns all underscores`() {
        val hint = WordBank.getHint("Hi") // length <= 2
        val chars = hint.replace(" ", "")
        assertTrue("Short words should be all underscores", chars.all { it == '_' })
    }

    @Test
    fun `getHint for single char returns underscore`() {
        val hint = WordBank.getHint("A")
        assertEquals("_", hint)
    }

    // ==================== GuessMessage ====================

    @Test
    fun `GuessMessage default values`() {
        val msg = GuessMessage()
        assertEquals("", msg.userId)
        assertEquals("", msg.message)
        assertFalse(msg.isCorrect)
        assertFalse(msg.isSystemMessage)
    }

    @Test
    fun `GuessMessage correct guess`() {
        val msg = GuessMessage(
            userId = "user-1",
            userName = "Alice",
            message = "cat",
            isCorrect = true
        )
        assertTrue(msg.isCorrect)
        assertEquals("cat", msg.message)
    }

    @Test
    fun `GuessMessage system message`() {
        val msg = GuessMessage(
            isSystemMessage = true,
            message = "New round started"
        )
        assertTrue(msg.isSystemMessage)
    }

    // ==================== Helper ====================

    private fun createTestGame() = PictionaryGame(
        id = "game-1",
        roomId = "room-1",
        currentDrawerId = "user-1",
        currentDrawerName = "Alice",
        currentWord = "Cat",
        wordHint = "C _ _",
        timeRemaining = 45,
        totalTime = 60,
        round = 2,
        maxRounds = 5,
        scores = mapOf("user-1" to 100, "user-2" to 50),
        guessedUsers = listOf("user-2"),
        isActive = true,
        isPaused = false,
        turnOrder = listOf("user-1", "user-2"),
        startedAt = 1700000000L
    )
}
