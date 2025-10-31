package src.qrcodegen;

import java.util.ArrayList;
import java.util.List;

/**
 * QR Code segments helper.
 * Based on Project Nayuki (MIT License).
 */
public final class QrSegment {
    enum Mode {
        BYTE(0x4, 8);
        final int modeBits;
        final int charCountBits;
        Mode(int bits, int countBits) {
            modeBits = bits;
            charCountBits = countBits;
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
        List<QrSegment> result = new ArrayList<>();
        byte[] bytes = text.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        result.add(new QrSegment(Mode.BYTE, text.length(), makeBytes(bytes)));
        return result;
    }

    private static byte[] makeBytes(byte[] data) {
        return data.clone();
    }

    static int getTotalBits(List<QrSegment> segments, int version) {
        long result = 0;
        for (QrSegment seg : segments) {
            int ccbits = seg.mode.charCountBits;
            result += 4L + ccbits + seg.data.length * 8L;
            if (result > Integer.MAX_VALUE)
                return -1;
        }
        return (int)result;
    }

    static byte[] encodeSegments(List<QrSegment> segments, int version, QrCode.Ecc ecl) {
        int capacity = QrCode.TOTAL_CODEWORDS[version] - QrCode.ECC_CODEWORDS_PER_BLOCK[ecl.formatBits][version] * QrCode.NUM_ERROR_CORRECTION_BLOCKS[ecl.formatBits][version];
        BitBuffer bb = new BitBuffer();
        for (QrSegment seg : segments) {
            bb.appendBits(seg.mode.modeBits, 4);
            bb.appendBits(seg.numChars, seg.mode.charCountBits);
            for (byte b : seg.data)
                bb.appendBits(b & 0xFF, 8);
        }
        int totalBits = bb.bitLength;
        int dataCapacity = capacity * 8;
        if (totalBits > dataCapacity)
            throw new IllegalArgumentException("Data too long");
        bb.appendBits(0, Math.min(4, dataCapacity - totalBits));
        int rem = bb.bitLength % 8;
        if (rem != 0)
            bb.appendBits(0, 8 - rem);
        int padByte = 0xEC;
        while (bb.bitLength < dataCapacity) {
            bb.appendBits(padByte, 8);
            padByte ^= 0xEC ^ 0x11;
        }
        byte[] result = new byte[capacity];
        for (int i = 0; i < result.length; i++)
            result[i] = (byte)bb.getByte(i);
        return result;
    }

    private static final class BitBuffer {
        private final List<Integer> bits = new ArrayList<>();
        int bitLength = 0;
        void appendBits(int val, int len) {
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
