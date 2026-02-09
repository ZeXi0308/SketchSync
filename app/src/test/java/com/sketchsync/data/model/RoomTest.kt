package com.sketchsync.data.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Room data model.
 * Covers: toMap/fromMap serialization, getUserRole logic, default values, and edge cases.
 */
class RoomTest {

    // ==================== toMap / fromMap Round-Trip ====================

    @Test
    fun `toMap produces correct keys`() {
        val room = createTestRoom()
        val map = room.toMap()

        assertTrue(map.containsKey("id"))
        assertTrue(map.containsKey("name"))
        assertTrue(map.containsKey("creatorId"))
        assertTrue(map.containsKey("isPrivate"))
        assertTrue(map.containsKey("password"))
        assertTrue(map.containsKey("participants"))
        assertTrue(map.containsKey("gameMode"))
        assertTrue(map.containsKey("memberRoles"))
    }

    @Test
    fun `fromMap reconstructs room correctly`() {
        val original = createTestRoom()
        val map = original.toMap()
        val restored = Room.fromMap(map)

        assertEquals(original.id, restored.id)
        assertEquals(original.name, restored.name)
        assertEquals(original.creatorId, restored.creatorId)
        assertEquals(original.isPrivate, restored.isPrivate)
        assertEquals(original.password, restored.password)
        assertEquals(original.participants, restored.participants)
        assertEquals(original.gameMode, restored.gameMode)
        assertEquals(original.memberRoles, restored.memberRoles)
    }

    @Test
    fun `fromMap with empty map uses defaults`() {
        val room = Room.fromMap(emptyMap())

        assertEquals("", room.name)
        assertEquals("", room.creatorId)
        assertFalse(room.isPrivate)
        assertNull(room.password)
        assertEquals(emptyList<String>(), room.participants)
        assertEquals(GameMode.FREE_DRAW, room.gameMode)
    }

    @Test
    fun `fromMap with roomId fallback`() {
        val room = Room.fromMap(emptyMap(), roomId = "fallback-id")
        assertEquals("fallback-id", room.id)
    }

    @Test
    fun `fromMap handles invalid gameMode gracefully`() {
        val map = mapOf<String, Any?>("gameMode" to "INVALID_MODE")
        val room = Room.fromMap(map)
        assertEquals(GameMode.FREE_DRAW, room.gameMode)
    }

    @Test
    fun `fromMap with private room and password`() {
        val map = mapOf<String, Any?>(
            "isPrivate" to true,
            "password" to "secret123"
        )
        val room = Room.fromMap(map)
        assertTrue(room.isPrivate)
        assertEquals("secret123", room.password)
    }

    // ==================== getUserRole ====================

    @Test
    fun `getUserRole returns OWNER for creatorId`() {
        val room = Room(creatorId = "creator-1")
        assertEquals(RoomRole.OWNER, room.getUserRole("creator-1"))
    }

    @Test
    fun `getUserRole returns EDITOR for explicit editor role`() {
        val room = Room(
            creatorId = "creator-1",
            memberRoles = mapOf("user-2" to "EDITOR")
        )
        assertEquals(RoomRole.EDITOR, room.getUserRole("user-2"))
    }

    @Test
    fun `getUserRole returns VIEWER for explicit viewer role`() {
        val room = Room(
            creatorId = "creator-1",
            memberRoles = mapOf("user-3" to "VIEWER")
        )
        assertEquals(RoomRole.VIEWER, room.getUserRole("user-3"))
    }

    @Test
    fun `getUserRole defaults to EDITOR for unknown user`() {
        val room = Room(creatorId = "creator-1")
        assertEquals(RoomRole.EDITOR, room.getUserRole("unknown-user"))
    }

    @Test
    fun `getUserRole defaults to EDITOR for invalid role string`() {
        val room = Room(
            creatorId = "creator-1",
            memberRoles = mapOf("user-4" to "INVALID_ROLE")
        )
        assertEquals(RoomRole.EDITOR, room.getUserRole("user-4"))
    }

    @Test
    fun `getUserRole owner takes priority over memberRoles`() {
        val room = Room(
            creatorId = "creator-1",
            memberRoles = mapOf("creator-1" to "VIEWER")
        )
        // Creator should always be OWNER regardless of memberRoles
        assertEquals(RoomRole.OWNER, room.getUserRole("creator-1"))
    }

    // ==================== Participants ====================

    @Test
    fun `room with multiple participants serializes correctly`() {
        val room = Room(
            participants = listOf("user-1", "user-2", "user-3"),
            participantNames = mapOf("user-1" to "Alice", "user-2" to "Bob", "user-3" to "Charlie")
        )
        val map = room.toMap()
        @Suppress("UNCHECKED_CAST")
        val participants = map["participants"] as List<String>
        assertEquals(3, participants.size)
    }

    // ==================== Default Values ====================

    @Test
    fun `default room has correct values`() {
        val room = Room()
        assertEquals(8, room.maxParticipants)
        assertFalse(room.isPrivate)
        assertNull(room.password)
        assertTrue(room.isActive)
        assertEquals(GameMode.FREE_DRAW, room.gameMode)
        assertTrue(room.participants.isEmpty())
    }

    // ==================== Helper ====================

    private fun createTestRoom() = Room(
        id = "room-123",
        name = "Test Room",
        creatorId = "creator-1",
        creatorName = "TestCreator",
        isPrivate = true,
        password = "pass",
        participants = listOf("creator-1", "user-2"),
        participantNames = mapOf("creator-1" to "TestCreator", "user-2" to "User2"),
        gameMode = GameMode.FREE_DRAW,
        memberRoles = mapOf("user-2" to "EDITOR")
    )
}
