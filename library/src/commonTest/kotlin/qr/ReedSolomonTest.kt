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
    fun decode_corrects_up_to_half_the_ecc_symbols() {
        val eccWords = 7 // version 1-L corrects 3 errors
        val rs = ReedSolomon(eccWords)
        val data = ByteArray(19) { (it * 17 + 3).toByte() }
        val ecc = rs.encode(data)
        val codeword = ByteArray(data.size + ecc.size)
        data.copyInto(codeword)
        ecc.copyInto(codeword, data.size)
        val positions = intArrayOf(0, 8, data.size + 1)
        for (pos in positions) {
            codeword[pos] = (codeword[pos].toInt() xor 0xFF).toByte()
        }
        assertContentEquals(data, rs.decode(codeword).copyOf(data.size))
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
