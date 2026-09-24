package qr

/**
 * Decodes QR code data from a bitmap.
 */
object BitDecoder {

    private const val MAX_BITS_ERROR = 3

    private fun decodeWithEci(bytes: ByteArray, eci: Int = 26): String =
        EciDecode.decode(bytes, eci)

    private data class InfoBits(
        val version1: Int,
        val version2: Int,
        val format1: Int,
        val format2: Int
    )

    private data class ParsedInfo(
        val version: Int,
        val ecc: ErrorCorrection,
        val mask: Mask
    )

    /**
     * Read format and version information bits from the bitmap.
     */
    private fun readInfoBits(b: Bitmap): InfoBits {
        val size = b.height

        fun readBit(x: Int, y: Int, out: Int): Int =
            (out shl 1) or (if (b.get(x, y) == true) 1 else 0)

        // Version information (for version >= 7)
        var version1 = 0
        for (y in 5 downTo 0) {
            for (x in size - 9 downTo size - 11) {
                version1 = readBit(x, y, version1)
            }
        }

        var version2 = 0
        for (x in 5 downTo 0) {
            for (y in size - 9 downTo size - 11) {
                version2 = readBit(x, y, version2)
            }
        }

        // Format information
        var format1 = 0
        for (x in 0 until 6) format1 = readBit(x, 8, format1)
        format1 = readBit(7, 8, format1)
        format1 = readBit(8, 8, format1)
        format1 = readBit(8, 7, format1)
        for (y in 5 downTo 0) format1 = readBit(8, y, format1)

        var format2 = 0
        for (y in size - 1 downTo size - 7) format2 = readBit(8, y, format2)
        for (x in size - 8 until size) format2 = readBit(x, 8, format2)

        return InfoBits(version1, version2, format1, format2)
    }

    /**
     * Population count (number of 1 bits).
     */
    private fun popcnt(a: Int): Int {
        var n = a
        var cnt = 0
        while (n != 0) {
            if (n and 1 != 0) cnt++
            n = n shr 1
        }
        return cnt
    }

    /**
     * Parse version and format information from the bitmap.
     */
    private fun parseInfo(b: Bitmap): ParsedInfo {
        val size = b.height
        val (version1, version2, format1, format2) = readInfoBits(b)

        // Guess format
        var format: Pair<ErrorCorrection, Mask>? = null
        var bestFormatScore = Int.MAX_VALUE
        var bestFormat: Pair<ErrorCorrection, Mask>? = null

        for (ecc in ErrorCorrection.entries) {
            for (mask in 0 until 8) {
                val bits = QRInfo.formatBits(ecc, mask)
                val cur = Pair(ecc, mask)
                if (bits == format1 || bits == format2) {
                    format = cur
                    break
                }
                val score1 = popcnt(format1 xor bits)
                val score2 = popcnt(format2 xor bits)
                val minScore = minOf(score1, score2)
                if (minScore < bestFormatScore) {
                    bestFormatScore = minScore
                    bestFormat = cur
                }
            }
            if (format != null) break
        }

        if (format == null && bestFormatScore <= MAX_BITS_ERROR) {
            format = bestFormat
        }

        if (format == null) {
            throw InvalidFormatException("Invalid format pattern")
        }

        // Guess version based on bitmap size
        var version: Int? = QRInfo.sizeDecode(size)
        if (version!! < 7) {
            QRInfo.validateVersion(version)
        } else {
            version = null
            var bestVerScore = Int.MAX_VALUE
            var bestVer: Int? = null

            for (ver in 7..40) {
                val bits = QRInfo.versionBits(ver)
                if (bits == version1 || bits == version2) {
                    version = ver
                    break
                }
                val score1 = popcnt(version1 xor bits)
                val score2 = popcnt(version2 xor bits)
                val minScore = minOf(score1, score2)
                if (minScore < bestVerScore) {
                    bestVerScore = minScore
                    bestVer = ver
                }
            }

            if (version == null && bestVerScore <= MAX_BITS_ERROR) {
                version = bestVer
            }

            if (version == null) {
                throw InvalidVersionException("Invalid version pattern")
            }

            if (QRInfo.sizeEncode(version) != size) {
                throw InvalidVersionException("Invalid version size: expected ${QRInfo.sizeEncode(version)}, got $size")
            }
        }

        return ParsedInfo(version, format.first, format.second)
    }

    /**
     * Decode the bitmap and extract the encoded text.
     */
    fun decodeBitmap(b: Bitmap): String {
        val size = b.height

        if (size < 21 || (size and 0b11) != 1 || size != b.width) {
            throw QRDecodingException("decode: invalid size=$size")
        }

        val (version, ecc, mask) = parseInfo(b)
        val tpl = QRInfo.drawTemplate(version, ecc, mask)
        val capacity = QRInfo.capacity(version, ecc)

        val bytes = ByteArray(capacity.total)
        var pos = 0
        var buf = 0
        var bitPos = 0

        QRInfo.zigzag(tpl, mask) { x, y, m ->
            bitPos++
            buf = buf shl 1
            buf = buf or if ((b.get(x, y) == true) != m) 1 else 0
            if (bitPos == 8) {
                bytes[pos++] = buf.toByte()
                bitPos = 0
                buf = 0
            }
        }

        if (pos != capacity.total) {
            throw QRDecodingException("decode: pos=$pos, total=${capacity.total}")
        }

        return decodeCodewords(Interleave(version, ecc).decode(bytes), version)
    }

    /**
     * Read QR segments from error-corrected data codewords.
     * [version] selects the character-count width.
     */
    internal fun decodeCodewords(data: ByteArray, version: Int): String {
        var bitIndex = 0
        val bitLength = data.size * 8

        fun read(n: Int): Int {
            if (bitIndex + n > bitLength) throw QRDecodingException("Not enough bits")
            var value = 0
            repeat(n) {
                val byte = data[bitIndex ushr 3].toInt() and 0xFF
                val bit = (byte ushr (7 - (bitIndex and 7))) and 1
                value = (value shl 1) or bit
                bitIndex++
            }
            return value
        }

        val result = StringBuilder()
        var eci = 26 // QR encoders emit UTF-8 byte mode without an ECI header.

        while (bitLength - bitIndex >= 4) {
            when (val mode = read(4)) {
                0x0 -> break
                0x7 -> {
                    val first = read(8)
                    eci = when {
                        (first and 0x80) == 0 -> first
                        (first and 0xC0) == 0x80 -> ((first and 0x3F) shl 8) or read(8)
                        (first and 0xE0) == 0xC0 -> ((first and 0x1F) shl 16) or read(16)
                        else -> throw QRDecodingException("Invalid ECI assignment")
                    }
                }
                0x1 -> appendNumeric(result, read(QRInfo.lengthBits(version, EncodingType.NUMERIC)), ::read)
                0x2 -> appendAlphanumeric(result, read(QRInfo.lengthBits(version, EncodingType.ALPHANUMERIC)), ::read)
                0x4 -> {
                    val count = read(QRInfo.lengthBits(version, EncodingType.BYTE))
                    val bytes = ByteArray(count) { read(8).toByte() }
                    result.append(decodeWithEci(bytes, eci))
                }
                0x8 -> throw QRDecodingException("Unsupported mode=kanji")
                else -> throw QRDecodingException(
                    "Unknown modeBits=${mode.toString(2).padStart(4, '0')} result=\"$result\""
                )
            }
        }
        return result.toString()
    }

    private fun appendNumeric(result: StringBuilder, count: Int, read: (Int) -> Int) {
        var left = count
        while (left >= 3) {
            val v = read(10)
            if (v >= 1000) throw QRDecodingException("numeric(3) = $v")
            result.append(v.toString().padStart(3, '0'))
            left -= 3
        }
        if (left == 2) {
            val v = read(7)
            if (v >= 100) throw QRDecodingException("numeric(2) = $v")
            result.append(v.toString().padStart(2, '0'))
        } else if (left == 1) {
            val v = read(4)
            if (v >= 10) throw QRDecodingException("numeric(1) = $v")
            result.append(v.toString())
        }
    }

    private fun appendAlphanumeric(result: StringBuilder, count: Int, read: (Int) -> Int) {
        var left = count
        while (left >= 2) {
            val v = read(11)
            if (v >= 45 * 45) throw QRDecodingException("alphanumeric = $v")
            val chars = QRInfo.alphanumericEncode(listOf(v / 45, v % 45))
            result.append(chars[0])
            result.append(chars[1])
            left -= 2
        }
        if (left == 1) {
            val v = read(6)
            if (v >= 45) throw QRDecodingException("alphanumeric = $v")
            result.append(QRInfo.alphanumericEncode(listOf(v))[0])
        }
    }
}
