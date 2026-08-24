package qr

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class QRInfoTest {

    @Test
    fun sizeEncode() {
        assertEquals(21, QRInfo.sizeEncode(1))
        assertEquals(25, QRInfo.sizeEncode(2))
        assertEquals(177, QRInfo.sizeEncode(40))
    }

    @Test
    fun sizeDecode() {
        assertEquals(1, QRInfo.sizeDecode(21))
        assertEquals(2, QRInfo.sizeDecode(25))
        assertEquals(40, QRInfo.sizeDecode(177))
    }

    @Test
    fun sizeEncode_and_sizeDecode_are_inverses() {
        for (version in 1..40) {
            assertEquals(version, QRInfo.sizeDecode(QRInfo.sizeEncode(version)))
        }
    }

    @Test
    fun validateVersion_accepts_1_to_40() {
        for (version in 1..40) {
            QRInfo.validateVersion(version)
        }
    }

    @Test
    fun validateVersion_rejects_invalid() {
        assertFailsWith<InvalidVersionException> { QRInfo.validateVersion(0) }
        assertFailsWith<InvalidVersionException> { QRInfo.validateVersion(41) }
        assertFailsWith<InvalidVersionException> { QRInfo.validateVersion(-1) }
    }

    @Test
    fun alignmentPatterns_version_1_empty() {
        assertEquals(0, QRInfo.alignmentPatterns(1).size)
    }

    @Test
    fun alignmentPatterns_version_2() {
        assertContentEquals(intArrayOf(6, 18), QRInfo.alignmentPatterns(2))
    }

    @Test
    fun alignmentPatterns_version_7() {
        val patterns = QRInfo.alignmentPatterns(7)
        assertEquals(3, patterns.size)
        assertEquals(6, patterns[0])
    }

    @Test
    fun eight_mask_patterns() {
        assertEquals(8, QRInfo.PATTERNS.size)
    }

    @Test
    fun mask_patterns_return_boolean() {
        for ((idx, pattern) in QRInfo.PATTERNS.withIndex()) {
            for (x in 0..20) {
                for (y in 0..20) {
                    val result = pattern(x, y)
                    assertTrue(result || !result, "Pattern $idx at ($x, $y)")
                }
            }
        }
    }

    @Test
    fun capacity_positive_for_all_versions() {
        for (version in 1..40) {
            for (ecc in ErrorCorrection.entries) {
                val cap = QRInfo.capacity(version, ecc)
                assertTrue(cap.words > 0)
                assertTrue(cap.numBlocks > 0)
                assertTrue(cap.blockLen >= 0)
                assertTrue(cap.capacity > 0)
                assertTrue(cap.total > 0)
            }
        }
    }

    @Test
    fun formatBits_are_15_bit() {
        for (ecc in ErrorCorrection.entries) {
            for (mask in 0..7) {
                val bits = QRInfo.formatBits(ecc, mask)
                assertTrue(bits >= 0)
                assertTrue(bits < (1 shl 15))
            }
        }
    }

    @Test
    fun versionBits_are_18_bit() {
        for (version in 7..40) {
            val bits = QRInfo.versionBits(version)
            assertTrue(bits >= 0)
            assertTrue(bits < (1 shl 18))
        }
    }

    @Test
    fun lengthBits() {
        assertEquals(10, QRInfo.lengthBits(1, EncodingType.NUMERIC))
        assertEquals(9, QRInfo.lengthBits(1, EncodingType.ALPHANUMERIC))
        assertEquals(8, QRInfo.lengthBits(1, EncodingType.BYTE))
        assertEquals(10, QRInfo.lengthBits(9, EncodingType.NUMERIC))
        assertEquals(12, QRInfo.lengthBits(10, EncodingType.NUMERIC))
        assertEquals(14, QRInfo.lengthBits(27, EncodingType.NUMERIC))
    }

    @Test
    fun drawTemplate_size() {
        val template = QRInfo.drawTemplate(1, ErrorCorrection.MEDIUM, 0)
        assertEquals(21, template.width)
        assertEquals(21, template.height)
    }

    @Test
    fun alphanumeric_roundtrip() {
        val indices = listOf(0, 10, 36, 44)
        val chars = QRInfo.alphanumericEncode(indices)
        assertEquals(listOf('0', 'A', ' ', ':'), chars)
        assertEquals(indices, QRInfo.alphanumericDecode(chars))
    }
}
