package com.sketchsync.data.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for DrawPath, PathPoint, and DrawTool.
 * Covers: toMap/fromMap serialization, tool enum handling, default values, and edge cases.
 *
 * Note: DrawPath uses android.graphics.Color which is unavailable in JVM unit tests.
 * We test with explicit color int values instead of Color.BLACK constant.
 */
class DrawPathTest {

    // ==================== toMap / fromMap Round-Trip ====================

    @Test
    fun `toMap produces correct keys`() {
        val path = createTestPath()
        val map = path.toMap()

        assertTrue(map.containsKey("id"))
        assertTrue(map.containsKey("points"))
        assertTrue(map.containsKey("color"))
        assertTrue(map.containsKey("strokeWidth"))
        assertTrue(map.containsKey("tool"))
        assertTrue(map.containsKey("userId"))
        assertTrue(map.containsKey("userName"))
        assertTrue(map.containsKey("timestamp"))
        assertTrue(map.containsKey("isEraser"))
        assertTrue(map.containsKey("text"))
        assertTrue(map.containsKey("fontSize"))
    }

    @Test
    fun `fromMap reconstructs path correctly`() {
        val map = mapOf<String, Any?>(
            "id" to "path-1",
            "points" to listOf(
                mapOf("x" to 10.0, "y" to 20.0),
                mapOf("x" to 30.0, "y" to 40.0)
            ),
            "color" to -16777216, // Color.BLACK
            "strokeWidth" to 8.0,
            "tool" to "BRUSH",
            "userId" to "user-1",
            "userName" to "Alice",
            "timestamp" to 1700000000L,
            "isEraser" to false,
            "text" to "",
            "fontSize" to 48.0
        )
        val path = DrawPath.fromMap(map)

        assertEquals("path-1", path.id)
        assertEquals(2, path.points.size)
        assertEquals(10f, path.points[0].x, 0.01f)
        assertEquals(20f, path.points[0].y, 0.01f)
        assertEquals(-16777216, path.color)
        assertEquals(8f, path.strokeWidth, 0.01f)
        assertEquals(DrawTool.BRUSH, path.tool)
        assertEquals("user-1", path.userId)
        assertEquals("Alice", path.userName)
        assertFalse(path.isEraser)
    }

    @Test
    fun `fromMap with empty map uses defaults`() {
        val path = DrawPath.fromMap(emptyMap())

        assertTrue(path.points.isEmpty())
        assertEquals(5f, path.strokeWidth, 0.01f)
        assertEquals(DrawTool.BRUSH, path.tool)
        assertEquals("", path.userId)
        assertEquals("", path.userName)
        assertFalse(path.isEraser)
        assertEquals("", path.text)
        assertEquals(48f, path.fontSize, 0.01f)
    }

    @Test
    fun `fromMap with pathId fallback`() {
        val path = DrawPath.fromMap(emptyMap(), pathId = "fallback-id")
        assertEquals("fallback-id", path.id)
        assertEquals("fallback-id", path.odatabaseId)
    }

    // ==================== Tool Enum ====================

    @Test
    fun `fromMap handles all valid tool types`() {
        DrawTool.values().forEach { tool ->
            val map = mapOf<String, Any?>("tool" to tool.name)
            val path = DrawPath.fromMap(map)
            assertEquals(tool, path.tool)
        }
    }

    @Test
    fun `fromMap handles invalid tool gracefully`() {
        val map = mapOf<String, Any?>("tool" to "INVALID_TOOL")
        val path = DrawPath.fromMap(map)
        assertEquals(DrawTool.BRUSH, path.tool) // Should fallback to BRUSH
    }

    @Test
    fun `DrawTool enum contains all expected values`() {
        val tools = DrawTool.values().map { it.name }
        assertTrue(tools.contains("BRUSH"))
        assertTrue(tools.contains("ERASER"))
        assertTrue(tools.contains("LINE"))
        assertTrue(tools.contains("RECTANGLE"))
        assertTrue(tools.contains("CIRCLE"))
        assertTrue(tools.contains("TEXT"))
        assertTrue(tools.contains("PAN"))
        assertEquals(7, tools.size)
    }

    // ==================== Text Tool ====================

    @Test
    fun `text tool path serializes correctly`() {
        val path = DrawPath(
            tool = DrawTool.TEXT,
            text = "Hello World",
            fontSize = 36f,
            points = listOf(PathPoint(100f, 200f))
        )
        val map = path.toMap()
        assertEquals("TEXT", map["tool"])
        assertEquals("Hello World", map["text"])
        assertEquals(36f, map["fontSize"])
    }

    @Test
    fun `fromMap parses text fields`() {
        val map = mapOf<String, Any?>(
            "tool" to "TEXT",
            "text" to "Testing",
            "fontSize" to 24.0
        )
        val path = DrawPath.fromMap(map)
        assertEquals(DrawTool.TEXT, path.tool)
        assertEquals("Testing", path.text)
        assertEquals(24f, path.fontSize, 0.01f)
    }

    // ==================== Eraser ====================

    @Test
    fun `eraser path serializes correctly`() {
        val path = DrawPath(tool = DrawTool.ERASER, isEraser = true)
        val map = path.toMap()
        assertEquals(true, map["isEraser"])
        assertEquals("ERASER", map["tool"])
    }

    // ==================== Points ====================

    @Test
    fun `empty points list serializes correctly`() {
        val path = DrawPath(points = emptyList())
        val map = path.toMap()
        @Suppress("UNCHECKED_CAST")
        val points = map["points"] as List<*>
        assertTrue(points.isEmpty())
    }

    @Test
    fun `points are serialized as x-y maps`() {
        val path = DrawPath(points = listOf(PathPoint(1.5f, 2.5f)))
        val map = path.toMap()
        @Suppress("UNCHECKED_CAST")
        val points = map["points"] as List<Map<String, Float>>
        assertEquals(1.5f, points[0]["x"]!!, 0.01f)
        assertEquals(2.5f, points[0]["y"]!!, 0.01f)
    }

    // ==================== PathPoint ====================

    @Test
    fun `PathPoint default values`() {
        val point = PathPoint()
        assertEquals(0f, point.x, 0.01f)
        assertEquals(0f, point.y, 0.01f)
    }

    @Test
    fun `PathPoint equality`() {
        val a = PathPoint(10f, 20f)
        val b = PathPoint(10f, 20f)
        assertEquals(a, b)
    }

    // ==================== Helper ====================

    private fun createTestPath() = DrawPath(
        id = "path-123",
        points = listOf(PathPoint(10f, 20f), PathPoint(30f, 40f)),
        color = -16777216,
        strokeWidth = 8f,
        tool = DrawTool.BRUSH,
        userId = "user-1",
        userName = "TestUser",
        timestamp = 1700000000L,
        isEraser = false,
        text = "",
        fontSize = 48f
    )
}
