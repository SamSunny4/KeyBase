package src.qrcodegen;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * QR Code segments helper.
 * Based on Project Nayuki (MIT License).
 */
public final class QrSegment {

    enum Mode {
        BYTE(0x4, new int[] {8, 16, 16});

        final int modeBits;
        private final int[] charCountBits;

        Mode(int bits, int[] countBits) {
            modeBits = bits;
            charCountBits = countBits;
        }

        int numCharCountBits(int version) {
            if (version >= 1 && version <= 9)
                return charCountBits[0];
            else if (version <= 26)
                return charCountBits[1];
            else
                return charCountBits[2];
        }
    }

    final Mode mode;
    final int numChars;
    final byte[] data;

    private QrSegment(Mode md, int chars, byte[] bytes) {
        mode = md;
        numChars = chars;
        data = bytes;
    }

    public static List<QrSegment> makeSegments(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        List<QrSegment> result = new ArrayList<>(1);
        result.add(new QrSegment(Mode.BYTE, bytes.length, bytes.clone()));
        return result;
    }

    static int getTotalBits(List<QrSegment> segments, int version) {
        long result = 0;
        for (QrSegment seg : segments) {
            int ccbits = seg.mode.numCharCountBits(version);
            long segmentBits = 4L + ccbits + (long)seg.data.length * 8L;
            result += segmentBits;
            if (result > Integer.MAX_VALUE)
                return -1;
        }
        return (int)result;
    }

    static byte[] encodeSegments(List<QrSegment> segments, int version, QrCode.Ecc ecl) {
        int dataCapacityBits = QrCode.getNumDataCodewords(version, ecl) * 8;
        BitBuffer bb = new BitBuffer();
        for (QrSegment seg : segments) {
            bb.appendBits(seg.mode.modeBits, 4);
            bb.appendBits(seg.numChars, seg.mode.numCharCountBits(version));
            for (byte b : seg.data) {
                bb.appendBits(b & 0xFF, 8);
            }
        }

        if (bb.bitLength > dataCapacityBits)
            throw new IllegalArgumentException("Data too long");

        int remaining = dataCapacityBits - bb.bitLength;
        if (remaining >= 4)
            bb.appendBits(0, 4);
        else
            bb.appendBits(0, remaining);

        int rem = bb.bitLength % 8;
        if (rem != 0)
            bb.appendBits(0, 8 - rem);

        int padByte = 0xEC;
        while (bb.bitLength < dataCapacityBits) {
            bb.appendBits(padByte, 8);
            padByte ^= 0xEC ^ 0x11;
        }

        int dataCodewords = dataCapacityBits / 8;
        byte[] result = new byte[dataCodewords];
        for (int i = 0; i < result.length; i++)
            result[i] = (byte)bb.getByte(i);
        return result;
    }

    private static final class BitBuffer {
        private final List<Integer> bits = new ArrayList<>();
        int bitLength = 0;

        void appendBits(int val, int len) {
            if (len < 0 || len > 31)
                throw new IllegalArgumentException("Length out of range");
            for (int i = len - 1; i >= 0; i--) {
                bits.add((val >>> i) & 1);
            }
            bitLength += len;
        }

        int getByte(int index) {
            int result = 0;
            for (int i = 0; i < 8; i++) {
                result = (result << 1) | bits.get(index * 8 + i);
            }
            return result;
        }
    }
}
