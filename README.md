# qr

Kotlin Multiplatform QR code decoder. Fork of
[`limpbrains/qr`](https://github.com/limpbrains/qr) (itself a port of
[paulmillr/qr](https://github.com/paulmillr/qr)).

Decode from raw pixel data (grayscale, RGB, or RGBA) on Android, iOS, JVM, and
linuxX64. Pass a camera Y plane directly; no color conversion needed.

## Differences from limpbrains/qr

- Multiplatform `commonMain` (not a JVM-only Gradle project)
- No `java.nio.charset` — ECI decode is UTF-8, ISO-8859-1, and UTF-16BE only
- Shift_JIS, Big5, GBK, EUC-KR, and other table-based encodings are rejected
- JDK 11 bytecode for the Android / JVM targets

## Gradle

```kotlin
implementation("io.bluewallet:qr:0.0.1")
```

Package: `qr`

## Usage

```kotlin
import qr.QRDecoder

val decoded = QRDecoder.decode(width, height, grayscaleData)
```

Unsupported ECI or a frame with no QR throws `QRDecodingException` (or a
subclass such as `FinderNotFoundException`).

## Tests

```bash
./gradlew :qr:jvmTest :qr:linuxX64Test
```

On macOS:

```bash
./gradlew :qr:iosSimulatorArm64Test
```

## License

Apache 2.0 OR MIT, same as upstream. Derived from ZXing (Apache 2.0).
