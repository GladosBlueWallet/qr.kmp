package qr

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BitDecoderTest {

    @Test
    fun numeric_digit_from_codewords() {
        // version 1 numeric: mode 0001, count 1, digit 1
        assertEquals("1", BitDecoder.decodeCodewords(byteArrayOf(0x10, 0x04, 0x40), 1))
    }

    @Test
    fun alphanumeric_character_from_codewords() {
        // version 1 alphanumeric: mode 0010, count 1, index 10 ('A')
        assertEquals("A", BitDecoder.decodeCodewords(byteArrayOf(0x20, 0x09, 0x40), 1))
    }

    @Test
    fun rejects_alphanumeric_value_outside_the_alphabet() {
        // 11-bit values above 44*45 are not a pair of alphanumeric characters.
        assertFailsWith<QRDecodingException> {
            BitDecoder.decodeCodewords(byteArrayOf(0x20, 0x0D, 0xA0.toByte()), 1)
        }
    }

    @Test
    fun rejects_eci_prefix_above_three_byte_form() {
        // 111xxxxx is not an ECI designator. Enough following bits are present
        // that a permissive parser would consume them and return an empty payload.
        assertFailsWith<QRDecodingException> {
            BitDecoder.decodeCodewords(byteArrayOf(0x7F, 0xF0.toByte(), 0, 0), 1)
        }
    }
}
