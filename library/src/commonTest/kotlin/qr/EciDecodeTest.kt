package qr

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EciDecodeTest {

    @Test
    fun utf8_eci_decodes_ascii() {
        assertEquals("bc1qtest", EciDecode.decode("bc1qtest".encodeToByteArray(), 26))
    }

    @Test
    fun default_eci_is_utf8() {
        assertEquals("hello", EciDecode.decode("hello".encodeToByteArray()))
    }

    @Test
    fun latin1_eci_1_and_3_map_bytes_to_unicode() {
        val cafe = byteArrayOf(0x63, 0x61, 0x66, 0xE9.toByte())
        assertEquals("café", EciDecode.decode(cafe, 1))
        assertEquals("café", EciDecode.decode(cafe, 3))
    }

    @Test
    fun utf16be_eci_decodes_code_units() {
        val bytes = byteArrayOf(0x00, 0x62, 0x00, 0x63, 0x00, 0x31)
        assertEquals("bc1", EciDecode.decode(bytes, 25))
    }

    @Test
    fun cjk_and_unknown_eci_are_rejected() {
        val payload = "hi".encodeToByteArray()
        for (eci in listOf(20, 28, 29, 30, 99)) {
            assertFailsWith<QRDecodingException> { EciDecode.decode(payload, eci) }
        }
    }
}
