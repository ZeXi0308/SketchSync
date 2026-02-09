package com.sketchsync.data.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for User data model.
 * Covers: toMap/fromMap serialization, default values, null handling, and AuthState.
 */
class UserTest {

    // ==================== toMap / fromMap Round-Trip ====================

    @Test
    fun `toMap produces correct keys`() {
        val user = createTestUser()
        val map = user.toMap()

        assertTrue(map.containsKey("uid"))
        assertTrue(map.containsKey("email"))
        assertTrue(map.containsKey("displayName"))
        assertTrue(map.containsKey("avatarUrl"))
        assertTrue(map.containsKey("totalDrawings"))
        assertTrue(map.containsKey("gamesPlayed"))
        assertTrue(map.containsKey("gamesWon"))
    }

    @Test
    fun `fromMap reconstructs user correctly`() {
        val original = createTestUser()
        val map = original.toMap()
        val restored = User.fromMap(map)

        assertEquals(original.uid, restored.uid)
        assertEquals(original.email, restored.email)
        assertEquals(original.displayName, restored.displayName)
        assertEquals(original.avatarUrl, restored.avatarUrl)
        assertEquals(original.totalDrawings, restored.totalDrawings)
        assertEquals(original.gamesPlayed, restored.gamesPlayed)
        assertEquals(original.gamesWon, restored.gamesWon)
    }

    @Test
    fun `fromMap with empty map uses defaults`() {
        val user = User.fromMap(emptyMap())

        assertEquals("", user.uid)
        assertEquals("", user.email)
        assertEquals("", user.displayName)
        assertNull(user.avatarUrl)
        assertEquals(0, user.totalDrawings)
        assertEquals(0, user.gamesPlayed)
        assertEquals(0, user.gamesWon)
    }

    @Test
    fun `fromMap with uid fallback`() {
        val user = User.fromMap(emptyMap(), uid = "fallback-uid")
        assertEquals("fallback-uid", user.uid)
    }

    @Test
    fun `fromMap handles nullable avatarUrl`() {
        val map = mapOf<String, Any?>(
            "avatarUrl" to null
        )
        val user = User.fromMap(map)
        assertNull(user.avatarUrl)
    }

    @Test
    fun `fromMap handles numeric type coercion`() {
        // Firebase sometimes returns Long for Int fields
        val map = mapOf<String, Any?>(
            "totalDrawings" to 42L,
            "gamesPlayed" to 10L,
            "gamesWon" to 5L
        )
        val user = User.fromMap(map)
        assertEquals(42, user.totalDrawings)
        assertEquals(10, user.gamesPlayed)
        assertEquals(5, user.gamesWon)
    }

    // ==================== AuthState ====================

    @Test
    fun `AuthState Loading is singleton`() {
        val a = AuthState.Loading
        val b = AuthState.Loading
        assertSame(a, b)
    }

    @Test
    fun `AuthState Unauthenticated is singleton`() {
        val a = AuthState.Unauthenticated
        val b = AuthState.Unauthenticated
        assertSame(a, b)
    }

    @Test
    fun `AuthState Authenticated contains user`() {
        val user = createTestUser()
        val state = AuthState.Authenticated(user)
        assertEquals(user, state.user)
    }

    @Test
    fun `AuthState Error contains message`() {
        val state = AuthState.Error("Network error")
        assertEquals("Network error", state.message)
    }

    // ==================== Helper ====================

    private fun createTestUser() = User(
        uid = "user-123",
        email = "test@example.com",
        displayName = "Test User",
        avatarUrl = "https://example.com/avatar.jpg",
        totalDrawings = 15,
        gamesPlayed = 10,
        gamesWon = 3
    )
}
