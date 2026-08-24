package qr

/**
 * ECI byte-to-string decode for the encodings we keep in common Kotlin:
 * UTF-8 (26), ISO-8859-1 (1 and 3), and UTF-16BE (25).
 */
internal object EciDecode {

    fun decode(bytes: ByteArray, eci: Int = 26): String = when (eci) {
        26 -> bytes.decodeToString()
        1, 3 -> decodeLatin1(bytes)
        25 -> decodeUtf16Be(bytes)
        else -> throw QRDecodingException("Unsupported ECI: $eci")
    }

    private fun decodeLatin1(bytes: ByteArray): String =
        CharArray(bytes.size) { i -> Char(bytes[i].toInt() and 0xFF) }.concatToString()

    private fun decodeUtf16Be(bytes: ByteArray): String {
        if (bytes.size % 2 != 0) {
            throw QRDecodingException("Failed to decode with ECI 25 (UTF-16BE): odd length")
        }
        return CharArray(bytes.size / 2) { i ->
            val hi = bytes[i * 2].toInt() and 0xFF
            val lo = bytes[i * 2 + 1].toInt() and 0xFF
            Char((hi shl 8) or lo)
        }.concatToString()
    }
}
