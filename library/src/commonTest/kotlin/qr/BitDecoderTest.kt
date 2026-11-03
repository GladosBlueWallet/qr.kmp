package qr

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

    @Test
    fun unknown_mode_error_does_not_echo_decoded_payload() {
        // version 1: byte segment "SECRET", then reserved mode 0011.
        val payload = "SECRET"
        val bits = StringBuilder()
        fun put(value: Int, n: Int) = bits.append(value.toString(2).padStart(n, '0'))
        put(0x4, 4)
        put(payload.length, 8)
        payload.forEach { put(it.code, 8) }
        put(0x3, 4)
        while (bits.length % 8 != 0) bits.append('0')
        val data = ByteArray(bits.length / 8) { bits.substring(it * 8, it * 8 + 8).toInt(2).toByte() }

        val e = assertFailsWith<QRDecodingException> { BitDecoder.decodeCodewords(data, 1) }
        val message = e.message.orEmpty()
        assertFalse(message.contains(payload), message)
        assertTrue(message.contains("modeBits=0011"), message)
        assertTrue(message.contains("bit=${4 + 8 + payload.length * 8}"), message)
    }
}
