package qr

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BitmapTest {

    @Test
    fun constructor_sets_dimensions() {
        val bmp = Bitmap(10, 20)
        assertEquals(10, bmp.width)
        assertEquals(20, bmp.height)
    }

    @Test
    fun single_size_constructor_is_square() {
        val bmp = Bitmap(15)
        assertEquals(15, bmp.width)
        assertEquals(15, bmp.height)
    }

    @Test
    fun set_and_get() {
        val bmp = Bitmap(5, 5)
        assertNull(bmp.get(2, 2))
        bmp.set(2, 2, true)
        assertEquals(true, bmp.get(2, 2))
        bmp.set(2, 2, false)
        assertEquals(false, bmp.get(2, 2))
    }

    @Test
    fun isInside_checks_boundaries() {
        val bmp = Bitmap(10, 10)
        assertTrue(bmp.isInside(Point(0.0, 0.0)))
        assertTrue(bmp.isInside(Point(9.0, 9.0)))
        assertFalse(bmp.isInside(Point(-1.0, 0.0)))
        assertFalse(bmp.isInside(Point(0.0, -1.0)))
        assertFalse(bmp.isInside(Point(10.0, 0.0)))
        assertFalse(bmp.isInside(Point(0.0, 10.0)))
    }

    @Test
    fun rect_fills_region() {
        val bmp = Bitmap(10, 10)
        bmp.rect(2, 2, 3, 3, true)
        for (y in 0 until 10) {
            for (x in 0 until 10) {
                val expected = x in 2..4 && y in 2..4
                assertEquals(expected, bmp.get(x, y) == true, "Failed at ($x, $y)")
            }
        }
    }

    @Test
    fun hLine_draws_horizontal_line() {
        val bmp = Bitmap(10, 10)
        bmp.hLine(2, 5, 4, true)
        for (x in 2..5) {
            assertEquals(true, bmp.get(x, 5))
        }
        assertNull(bmp.get(1, 5))
        assertNull(bmp.get(6, 5))
    }

    @Test
    fun vLine_draws_vertical_line() {
        val bmp = Bitmap(10, 10)
        bmp.vLine(5, 2, 4, true)
        for (y in 2..5) {
            assertEquals(true, bmp.get(5, y))
        }
        assertNull(bmp.get(5, 1))
        assertNull(bmp.get(5, 6))
    }

    @Test
    fun border_adds_padding() {
        val bmp = Bitmap(3, 3)
        bmp.rect(0, 0, 3, 3, true)
        val bordered = bmp.border(2, false)
        assertEquals(7, bordered.width)
        assertEquals(7, bordered.height)
        assertEquals(false, bordered.get(0, 0))
        assertEquals(false, bordered.get(6, 6))
        assertEquals(true, bordered.get(3, 3))
    }

    @Test
    fun clone_is_independent() {
        val bmp = Bitmap(5, 5)
        bmp.set(2, 2, true)
        val clone = bmp.clone()
        clone.set(2, 2, false)
        assertEquals(true, bmp.get(2, 2))
        assertEquals(false, clone.get(2, 2))
    }

    @Test
    fun toImage_writes_rgba() {
        val bmp = Bitmap(2, 2)
        bmp.set(0, 0, true)
        bmp.set(1, 1, true)
        val img = bmp.toImage(isRGB = false)
        assertEquals(2, img.width)
        assertEquals(2, img.height)
        assertEquals(16, img.data.size)
        assertEquals(0, img.data[0].toInt() and 0xFF)
        assertEquals(0, img.data[1].toInt() and 0xFF)
        assertEquals(0, img.data[2].toInt() and 0xFF)
        assertEquals(255, img.data[3].toInt() and 0xFF)
    }

    @Test
    fun toASCII_is_non_empty() {
        val bmp = Bitmap(4, 4)
        bmp.rect(0, 0, 4, 4, true)
        assertTrue(bmp.toASCII().isNotEmpty())
    }

    @Test
    fun fromString_parses_modules() {
        val str = """
            X X
             X
            X X
        """.trimIndent()
        val bmp = Bitmap.fromString(str)
        assertEquals(3, bmp.width)
        assertEquals(3, bmp.height)
        assertEquals(true, bmp.get(0, 0))
        assertEquals(false, bmp.get(1, 0))
        assertEquals(true, bmp.get(2, 0))
        assertEquals(false, bmp.get(0, 1))
        assertEquals(true, bmp.get(1, 1))
    }
}
