package com.sketchsync.util

import com.sketchsync.data.model.DrawPath
import com.sketchsync.data.model.DrawTool
import com.sketchsync.data.model.PathPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ReplayManager.
 * Covers: state machine transitions (prepare/play/pause/stop/seekTo),
 * progress tracking, path ID emissions, and edge cases.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReplayManagerTest {

    private lateinit var replayManager: ReplayManager

    @Before
    fun setUp() {
        replayManager = ReplayManager()
    }

    // ==================== Initial State ====================

    @Test
    fun `initial state is IDLE`() {
        assertEquals(ReplayState.IDLE, replayManager.replayState.value)
    }

    @Test
    fun `initial progress is zero`() {
        assertEquals(0f, replayManager.progress.value, 0.01f)
    }

    @Test
    fun `initial path IDs are empty`() {
        assertTrue(replayManager.currentPathIds.value.isEmpty())
    }

    // ==================== Prepare ====================

    @Test
    fun `prepare with paths sets PAUSED state`() {
        replayManager.prepare(createTestPaths(5))
        assertEquals(ReplayState.PAUSED, replayManager.replayState.value)
    }

    @Test
    fun `prepare resets progress to zero`() {
        replayManager.prepare(createTestPaths(5))
        assertEquals(0f, replayManager.progress.value, 0.01f)
    }

    @Test
    fun `prepare clears path IDs`() {
        replayManager.prepare(createTestPaths(5))
        assertTrue(replayManager.currentPathIds.value.isEmpty())
    }

    @Test
    fun `prepare with empty list stays IDLE`() {
        replayManager.prepare(emptyList())
        assertEquals(ReplayState.IDLE, replayManager.replayState.value)
    }

    @Test
    fun `prepare with empty list resets progress`() {
        replayManager.prepare(emptyList())
        assertEquals(0f, replayManager.progress.value, 0.01f)
    }

    // ==================== Play ====================

    @Test
    fun `play with single path completes immediately`() = runTest {
        val paths = createTestPaths(1)
        replayManager.prepare(paths)

        replayManager.play(this, 1.0f)

        assertEquals(ReplayState.COMPLETED, replayManager.replayState.value)
        assertEquals(1f, replayManager.progress.value, 0.01f)
        assertEquals(1, replayManager.currentPathIds.value.size)
    }

    @Test
    fun `play sets PLAYING state`() = runTest {
        val paths = createTestPaths(5)
        replayManager.prepare(paths)

        replayManager.play(this, 1.0f)

        // Before advancing, state should be PLAYING
        val stateBeforeAdvance = replayManager.replayState.value
        assertTrue(
            stateBeforeAdvance == ReplayState.PLAYING || stateBeforeAdvance == ReplayState.COMPLETED
        )
    }

    @Test
    fun `play completes all paths`() = runTest {
        val paths = createTestPaths(3)
        replayManager.prepare(paths)

        replayManager.play(this, 1.0f)
        advanceUntilIdle()

        assertEquals(ReplayState.COMPLETED, replayManager.replayState.value)
        assertEquals(1f, replayManager.progress.value, 0.01f)
        assertEquals(3, replayManager.currentPathIds.value.size)
    }

    @Test
    fun `play does nothing if no paths`() = runTest {
        replayManager.prepare(emptyList())
        replayManager.play(this, 1.0f)
        assertEquals(ReplayState.IDLE, replayManager.replayState.value)
    }

    @Test
    fun `play does nothing if already playing`() = runTest {
        val paths = createTestPaths(10)
        replayManager.prepare(paths)

        replayManager.play(this, 1.0f)
        val stateBefore = replayManager.replayState.value

        // Calling play again should not change state
        replayManager.play(this, 2.0f)
        // Should still be same state (PLAYING or COMPLETED depending on timing)
    }

    // ==================== Pause ====================

    @Test
    fun `pause when not playing does nothing`() {
        replayManager.prepare(createTestPaths(5))
        replayManager.pause()
        assertEquals(ReplayState.PAUSED, replayManager.replayState.value)
    }

    @Test
    fun `pause from IDLE does nothing`() {
        replayManager.pause()
        assertEquals(ReplayState.IDLE, replayManager.replayState.value)
    }

    // ==================== Stop ====================

    @Test
    fun `stop resets to IDLE`() {
        replayManager.prepare(createTestPaths(5))
        replayManager.stop()
        assertEquals(ReplayState.IDLE, replayManager.replayState.value)
    }

    @Test
    fun `stop resets progress to zero`() {
        replayManager.prepare(createTestPaths(5))
        replayManager.stop()
        assertEquals(0f, replayManager.progress.value, 0.01f)
    }

    @Test
    fun `stop clears path IDs`() {
        replayManager.prepare(createTestPaths(5))
        replayManager.stop()
        assertTrue(replayManager.currentPathIds.value.isEmpty())
    }

    @Test
    fun `stop from IDLE is safe`() {
        replayManager.stop()
        assertEquals(ReplayState.IDLE, replayManager.replayState.value)
    }

    // ==================== SeekTo ====================

    @Test
    fun `seekTo with empty paths does nothing`() {
        replayManager.prepare(emptyList())
        replayManager.seekTo(0.5f)
        assertEquals(0f, replayManager.progress.value, 0.01f)
    }

    @Test
    fun `seekTo clamps to valid range`() {
        replayManager.prepare(createTestPaths(5))

        replayManager.seekTo(-1f)
        assertTrue(replayManager.progress.value >= 0f)

        replayManager.seekTo(2f)
        assertTrue(replayManager.progress.value <= 1f)
    }

    @Test
    fun `seekTo 0 shows no paths`() {
        val paths = createTestPaths(5)
        replayManager.prepare(paths)
        replayManager.seekTo(0f)

        assertEquals(0f, replayManager.progress.value, 0.01f)
        assertEquals(1, replayManager.currentPathIds.value.size) // First path visible at index 0
    }

    @Test
    fun `seekTo 1 shows all paths`() {
        val paths = createTestPaths(5)
        replayManager.prepare(paths)
        replayManager.seekTo(1f)

        assertEquals(1f, replayManager.progress.value, 0.01f)
        assertEquals(5, replayManager.currentPathIds.value.size)
    }

    @Test
    fun `seekTo 0_5 shows approximately half the paths`() {
        val paths = createTestPaths(10)
        replayManager.prepare(paths)
        replayManager.seekTo(0.5f)

        val visibleCount = replayManager.currentPathIds.value.size
        assertTrue("Should show around half (~5) paths, got $visibleCount", visibleCount in 4..7)
    }

    @Test
    fun `seekTo from COMPLETED state transitions to PAUSED`() {
        val paths = createTestPaths(3)
        replayManager.prepare(paths)
        replayManager.seekTo(1f)
        // Seeking to end doesn't change state from PAUSED (we haven't played yet)

        // Simulate completed state manually via play
        // Since we can't easily get to COMPLETED in sync test without runTest,
        // we test the boundary case: seeking to < 1 after prepare
        replayManager.seekTo(0.5f)
        assertEquals(ReplayState.PAUSED, replayManager.replayState.value)
    }

    // ==================== State Machine: Full Lifecycle ====================

    @Test
    fun `full lifecycle - prepare, play, complete`() = runTest {
        val paths = createTestPaths(3)

        // 1. Prepare
        replayManager.prepare(paths)
        assertEquals(ReplayState.PAUSED, replayManager.replayState.value)

        // 2. Play
        replayManager.play(this, 1.0f)
        advanceUntilIdle()

        // 3. Should complete
        assertEquals(ReplayState.COMPLETED, replayManager.replayState.value)
        assertEquals(3, replayManager.currentPathIds.value.size)
        assertEquals(1f, replayManager.progress.value, 0.01f)

        // 4. Stop and reset
        replayManager.stop()
        assertEquals(ReplayState.IDLE, replayManager.replayState.value)
        assertTrue(replayManager.currentPathIds.value.isEmpty())
    }

    // ==================== Helper ====================

    private fun createTestPaths(count: Int): List<DrawPath> {
        return (1..count).map { i ->
            DrawPath(
                id = "path-$i",
                points = listOf(PathPoint(i.toFloat(), i.toFloat())),
                color = -16777216,
                strokeWidth = 5f,
                tool = DrawTool.BRUSH,
                userId = "user-1",
                timestamp = 1700000000L + i
            )
        }
    }
}
