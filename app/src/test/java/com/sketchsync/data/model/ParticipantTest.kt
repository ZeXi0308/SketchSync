package com.sketchsync.data.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Participant data model.
 * Covers: toMap/fromMap serialization, default values, and cursor position handling.
 */
class ParticipantTest {

    // ==================== toMap / fromMap Round-Trip ====================

    @Test
    fun `toMap produces correct keys`() {
        val participant = createTestParticipant()
        val map = participant.toMap()

        assertTrue(map.containsKey("userId"))
        assertTrue(map.containsKey("displayName"))
        assertTrue(map.containsKey("avatarUrl"))
        assertTrue(map.containsKey("isActive"))
        assertTrue(map.containsKey("isMuted"))
        assertTrue(map.containsKey("cursorX"))
        assertTrue(map.containsKey("cursorY"))
    }

    @Test
    fun `fromMap reconstructs participant correctly`() {
        val original = createTestParticipant()
        val map = original.toMap()
        val restored = Participant.fromMap(map)

        assertEquals(original.odatabaseId, restored.odatabaseId)
        assertEquals(original.displayName, restored.displayName)
        assertEquals(original.avatarUrl, restored.avatarUrl)
        assertEquals(original.isActive, restored.isActive)
        assertEquals(original.isMuted, restored.isMuted)
        assertEquals(original.cursorX, restored.cursorX, 0.01f)
        assertEquals(original.cursorY, restored.cursorY, 0.01f)
    }

    @Test
    fun `fromMap with empty map uses defaults`() {
        val p = Participant.fromMap(emptyMap())

        assertEquals("", p.odatabaseId)
        assertEquals("", p.displayName)
        assertNull(p.avatarUrl)
        assertTrue(p.isActive)
        assertFalse(p.isMuted)
        assertEquals(0f, p.cursorX, 0.01f)
        assertEquals(0f, p.cursorY, 0.01f)
    }

    @Test
    fun `fromMap with userId fallback`() {
        val p = Participant.fromMap(emptyMap(), odatabaseId = "fallback-uid")
        assertEquals("fallback-uid", p.odatabaseId)
    }

    // ==================== Cursor Position ====================

    @Test
    fun `cursor position serialized correctly`() {
        val p = Participant(cursorX = 150.5f, cursorY = 300.75f)
        val map = p.toMap()
        assertEquals(150.5f, (map["cursorX"] as Number).toFloat(), 0.01f)
        assertEquals(300.75f, (map["cursorY"] as Number).toFloat(), 0.01f)
    }

    @Test
    fun `fromMap parses cursor from numeric types`() {
        val map = mapOf<String, Any?>(
            "cursorX" to 100.0,  // Double from Firebase
            "cursorY" to 200.0
        )
        val p = Participant.fromMap(map)
        assertEquals(100f, p.cursorX, 0.01f)
        assertEquals(200f, p.cursorY, 0.01f)
    }

    // ==================== Mute State ====================

    @Test
    fun `muted participant serialized correctly`() {
        val p = Participant(isMuted = true)
        val map = p.toMap()
        assertEquals(true, map["isMuted"])
    }

    // ==================== Avatar ====================

    @Test
    fun `nullable avatar serialized correctly`() {
        val p = Participant(avatarUrl = null)
        val map = p.toMap()
        assertNull(map["avatarUrl"])
    }

    @Test
    fun `avatar url serialized correctly`() {
        val p = Participant(avatarUrl = "https://example.com/avatar.jpg")
        val map = p.toMap()
        assertEquals("https://example.com/avatar.jpg", map["avatarUrl"])
    }

    // ==================== Helper ====================

    private fun createTestParticipant() = Participant(
        odatabaseId = "user-1",
        displayName = "Alice",
        avatarUrl = "https://example.com/alice.jpg",
        isActive = true,
        isMuted = false,
        cursorX = 100f,
        cursorY = 200f
    )
}
