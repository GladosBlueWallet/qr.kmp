package qr

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class QRDecoderTest {

    @Test
    fun decode_throws_for_empty_image() {
        assertFailsWith<IllegalArgumentException> {
            QRDecoder.decode(0, 0, byteArrayOf())
        }
    }

    @Test
    fun decode_throws_for_too_small_image() {
        val width = 10
        val height = 10
        val data = ByteArray(width * height * 4) { 255.toByte() }
        assertFailsWith<ImageTooSmallException> {
            QRDecoder.decode(width, height, data)
        }
    }

    @Test
    fun image_constructor_validates_dimensions() {
        assertFailsWith<IllegalArgumentException> { Image(-1, 10, ByteArray(100)) }
        assertFailsWith<IllegalArgumentException> { Image(10, 0, ByteArray(100)) }
        assertFailsWith<IllegalArgumentException> { Image(10, 10, byteArrayOf()) }
    }

    @Test
    fun image_bytesPerPixel_detects_rgb_and_rgba() {
        assertEquals(3, Image(10, 10, ByteArray(10 * 10 * 3)).bytesPerPixel)
        assertEquals(4, Image(10, 10, ByteArray(10 * 10 * 4)).bytesPerPixel)
    }

    @Test
    fun image_bytesPerPixel_throws_for_invalid_format() {
        val invalidImage = Image(10, 10, ByteArray(10 * 10 * 2))
        assertFailsWith<IllegalArgumentException> { invalidImage.bytesPerPixel }
    }

    @Test
    fun decode_throws_finder_not_found_for_blank_image() {
        val size = 100
        val image = Image(size, size, ByteArray(size * size * 4) { 255.toByte() })
        assertFailsWith<FinderNotFoundException> { QRDecoder.decode(image) }
    }

    @Test
    fun point_operations() {
        val p1 = Point(3.0, 4.0)
        val p2 = Point(1.0, 2.0)
        assertEquals(Point(4.0, 6.0), p1 + p2)
        assertEquals(Point(2.0, 2.0), p1 - p2)
        assertEquals(Point(-3.0, -4.0), -p1)
        assertEquals(Point(4.0, 3.0), p1.mirror())
    }

    @Test
    fun point_distance() {
        val p1 = Point(0.0, 0.0)
        val p2 = Point(3.0, 4.0)
        assertEquals(5.0, Point.distance(p1, p2), 0.0001)
        assertEquals(25.0, Point.distance2(p1, p2), 0.0001)
    }

    @Test
    fun pattern_merge_is_weighted_average() {
        val merged = Pattern(0.0, 0.0, 2.0, 1).merge(Pattern(4.0, 4.0, 2.0, 1))
        assertEquals(2.0, merged.x, 0.0001)
        assertEquals(2.0, merged.y, 0.0001)
        assertEquals(2.0, merged.moduleSize, 0.0001)
        assertEquals(2, merged.count)
    }

    @Test
    fun error_correction_codes() {
        assertEquals(0b01, ErrorCorrection.LOW.code)
        assertEquals(0b00, ErrorCorrection.MEDIUM.code)
        assertEquals(0b11, ErrorCorrection.QUARTILE.code)
        assertEquals(0b10, ErrorCorrection.HIGH.code)
    }

    @Test
    fun error_correction_fromCode() {
        assertEquals(ErrorCorrection.LOW, ErrorCorrection.fromCode(0b01))
        assertEquals(ErrorCorrection.MEDIUM, ErrorCorrection.fromCode(0b00))
        assertEquals(ErrorCorrection.QUARTILE, ErrorCorrection.fromCode(0b11))
        assertEquals(ErrorCorrection.HIGH, ErrorCorrection.fromCode(0b10))
    }

    @Test
    fun encoding_type_mode_bits() {
        assertEquals("0001", EncodingType.NUMERIC.modeBits)
        assertEquals("0010", EncodingType.ALPHANUMERIC.modeBits)
        assertEquals("0100", EncodingType.BYTE.modeBits)
    }

    @Test
    fun encoding_type_fromModeBits() {
        assertEquals(EncodingType.NUMERIC, EncodingType.fromModeBits("0001"))
        assertEquals(EncodingType.ALPHANUMERIC, EncodingType.fromModeBits("0010"))
        assertEquals(EncodingType.BYTE, EncodingType.fromModeBits("0100"))
        assertNull(EncodingType.fromModeBits("1111"))
    }
}
