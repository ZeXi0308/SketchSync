package com.sketchsync.data.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for ChatMessage and MessageType.
 * Covers: toMap/fromMap serialization, message types, and edge cases.
 */
class ChatMessageTest {

    // ==================== toMap / fromMap Round-Trip ====================

    @Test
    fun `toMap produces correct keys`() {
        val msg = createTestMessage()
        val map = msg.toMap()

        assertTrue(map.containsKey("id"))
        assertTrue(map.containsKey("roomId"))
        assertTrue(map.containsKey("userId"))
        assertTrue(map.containsKey("userName"))
        assertTrue(map.containsKey("content"))
        assertTrue(map.containsKey("timestamp"))
        assertTrue(map.containsKey("type"))
        assertTrue(map.containsKey("isCorrectGuess"))
    }

    @Test
    fun `fromMap reconstructs message correctly`() {
        val original = createTestMessage()
        val map = original.toMap()
        val restored = ChatMessage.fromMap(map)

        assertEquals(original.id, restored.id)
        assertEquals(original.roomId, restored.roomId)
        assertEquals(original.userId, restored.userId)
        assertEquals(original.userName, restored.userName)
        assertEquals(original.content, restored.content)
        assertEquals(original.type, restored.type)
        assertEquals(original.isCorrectGuess, restored.isCorrectGuess)
    }

    @Test
    fun `fromMap with empty map uses defaults`() {
        val msg = ChatMessage.fromMap(emptyMap())

        assertEquals("", msg.roomId)
        assertEquals("", msg.userId)
        assertEquals("", msg.content)
        assertEquals(MessageType.TEXT, msg.type)
        assertFalse(msg.isCorrectGuess)
    }

    @Test
    fun `fromMap with messageId fallback`() {
        val msg = ChatMessage.fromMap(emptyMap(), messageId = "fallback-id")
        assertEquals("fallback-id", msg.id)
    }

    // ==================== Message Types ====================

    @Test
    fun `all message types serialize correctly`() {
        MessageType.values().forEach { type ->
            val msg = ChatMessage(type = type)
            val map = msg.toMap()
            val restored = ChatMessage.fromMap(map)
            assertEquals(type, restored.type)
        }
    }

    @Test
    fun `fromMap handles invalid type gracefully`() {
        val map = mapOf<String, Any?>("type" to "INVALID_TYPE")
        val msg = ChatMessage.fromMap(map)
        assertEquals(MessageType.TEXT, msg.type)
    }

    @Test
    fun `MessageType enum has expected values`() {
        val types = MessageType.values().map { it.name }
        assertTrue(types.contains("TEXT"))
        assertTrue(types.contains("SYSTEM"))
        assertTrue(types.contains("GUESS"))
        assertTrue(types.contains("CORRECT_GUESS"))
        assertTrue(types.contains("EMOJI"))
    }

    // ==================== Correct Guess ====================

    @Test
    fun `correct guess message`() {
        val msg = ChatMessage(
            type = MessageType.CORRECT_GUESS,
            isCorrectGuess = true,
            content = "cat"
        )
        val map = msg.toMap()
        assertEquals(true, map["isCorrectGuess"])
        assertEquals("CORRECT_GUESS", map["type"])
    }

    @Test
    fun `system message serialization`() {
        val msg = ChatMessage(
            type = MessageType.SYSTEM,
            content = "User joined the room"
        )
        val map = msg.toMap()
        val restored = ChatMessage.fromMap(map)
        assertEquals(MessageType.SYSTEM, restored.type)
        assertEquals("User joined the room", restored.content)
    }

    // ==================== Helper ====================

    private fun createTestMessage() = ChatMessage(
        id = "msg-123",
        roomId = "room-1",
        userId = "user-1",
        userName = "Alice",
        content = "Hello everyone!",
        timestamp = 1700000000L,
        type = MessageType.TEXT,
        isCorrectGuess = false
    )
}
