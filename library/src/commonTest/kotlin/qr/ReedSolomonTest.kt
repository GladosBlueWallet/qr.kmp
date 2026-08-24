package qr

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ReedSolomonTest {

    @Test
    fun encode_adds_ecc_bytes() {
        val encoded = ReedSolomon(10).encode(byteArrayOf(1, 2, 3, 4, 5))
        assertEquals(10, encoded.size)
    }

    @Test
    fun decode_passes_through_data_without_errors() {
        val rs = ReedSolomon(4)
        val originalData = byteArrayOf(0x40, 0x11, 0x22, 0x33)
        val ecc = rs.encode(originalData)
        val codeword = ByteArray(originalData.size + ecc.size)
        originalData.copyInto(codeword)
        ecc.copyInto(codeword, originalData.size)
        assertContentEquals(codeword, rs.decode(codeword))
    }

    @Test
    fun decode_handles_valid_codeword() {
        val rs = ReedSolomon(4)
        val data = byteArrayOf(0x10, 0x20, 0x30)
        val ecc = rs.encode(data)
        val codeword = ByteArray(data.size + ecc.size)
        data.copyInto(codeword)
        ecc.copyInto(codeword, data.size)
        assertEquals(codeword.size, rs.decode(codeword).size)
    }
}
