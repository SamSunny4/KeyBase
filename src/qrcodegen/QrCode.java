package src.qrcodegen;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * QR Code generator library (MIT License).
 * Based on Project Nayuki (https://www.nayuki.io/).
 */
public final class QrCode {
    public enum Ecc {
        LOW(1), MEDIUM(0), QUARTILE(3), HIGH(2);
        public final int formatBits;
        Ecc(int fb) { formatBits = fb; }
    }

    public final int size;
    private final int errorCorrectionLevel;
    private byte[][] modules;
    private final boolean[][] isFunction;

    private QrCode(int ver, int ecc) {
        if (ver < 1 || ver > 40)
            throw new IllegalArgumentException("Version out of range");
        size = ver * 4 + 17;
        errorCorrectionLevel = ecc;
        modules = new byte[size][size];
        isFunction = new boolean[size][size];
    }

    public static QrCode encodeText(String text, Ecc ecl) {
        return encodeSegments(QrSegment.makeSegments(text), ecl);
    }

    public static QrCode encodeSegments(List<QrSegment> segs, Ecc ecl) {
        int version = 1;
        for (; version <= 40; version++) {
            int dataCapacityBits = getNumDataCodewords(version, ecl) * 8;
            int dataUsedBits = QrSegment.getTotalBits(segs, version);
            if (dataUsedBits != -1 && dataUsedBits <= dataCapacityBits)
                break;
        }
        if (version > 40)
            throw new IllegalArgumentException("Data too long");
        QrCode qr = new QrCode(version, ecl.formatBits);
        qr.drawFunctionPatterns();
        byte[] allCodewords = qr.addEccAndInterleave(QrSegment.encodeSegments(segs, version, ecl));
        qr.drawCodewords(allCodewords);
        qr.applyBestMask();
        return qr;
    }

    private void drawFunctionPatterns() {
        for (int i = 0; i < size; i++) {
            setFunctionModule(6, i, i % 2 == 0);
            setFunctionModule(i, 6, i % 2 == 0);
        }
        drawFinderPattern(3, 3);
        drawFinderPattern(size - 4, 3);
        drawFinderPattern(3, size - 4);
        int[] align = getAlignmentPatternPositions(size);
        for (int i = 0; i < align.length; i++) {
            for (int j = 0; j < align.length; j++) {
                if ((i == 0 && j == 0) || (i == 0 && j == align.length - 1) || (i == align.length - 1 && j == 0))
                    continue;
                drawAlignmentPattern(align[i], align[j]);
            }
        }
        setFunctionModule(size - 8, 8, true);
        for (int i = 0; i < 7; i++) {
            setFunctionModule(8, i, ((errorCorrectionLevel >>> i) & 1) != 0);
            setFunctionModule(i, 8, ((errorCorrectionLevel >>> i) & 1) != 0);
            setFunctionModule(8, size - 1 - i, ((errorCorrectionLevel >>> (i + 8)) & 1) != 0);
            setFunctionModule(size - 1 - i, 8, ((errorCorrectionLevel >>> (i + 8)) & 1) != 0);
        }
    }

    private void drawFinderPattern(int x, int y) {
        for (int dy = -4; dy <= 4; dy++) {
            for (int dx = -4; dx <= 4; dx++) {
                int dist = Math.max(Math.abs(dx), Math.abs(dy));
                int xx = x + dx;
                int yy = y + dy;
                if (0 <= xx && xx < size && 0 <= yy && yy < size) {
                    setFunctionModule(xx, yy, dist != 2 && dist != 4);
                }
            }
        }
    }

    private void drawAlignmentPattern(int x, int y) {
        for (int dy = -2; dy <= 2; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                int xx = x + dx;
                int yy = y + dy;
                if (0 <= xx && xx < size && 0 <= yy && yy < size) {
                    setFunctionModule(xx, yy, Math.max(Math.abs(dx), Math.abs(dy)) != 1);
                }
            }
        }
    }

    private void setFunctionModule(int x, int y, boolean isBlack) {
        modules[y][x] = (byte)(isBlack ? 1 : 0);
        isFunction[y][x] = true;
    }

    private void drawCodewords(byte[] data) {
        int i = 0;
        for (int right = size - 1; right >= 1; right -= 2) {
            if (right == 6)
                right--;
            for (int vert = 0; vert < size; vert++) {
                for (int j = 0; j < 2; j++) {
                    int x = right - j;
                    int y = ((right + 1) & 2) == 0 ? size - 1 - vert : vert;
                    if (!isFunction[y][x]) {
                        boolean bit = false;
                        if (i < data.length * 8) {
                            bit = ((data[i >>> 3] >>> (7 - (i & 7))) & 1) != 0;
                            i++;
                        }
                        modules[y][x] = (byte)(bit ? 1 : 0);
                    }
                }
            }
        }
    }

    private void applyBestMask() {
        int minPenalty = Integer.MAX_VALUE;
        byte[][] best = null;
        int bestMask = 0;
        for (int mask = 0; mask < 8; mask++) {
            applyMask(mask);
            int penalty = getPenaltyScore();
            if (penalty < minPenalty) {
                minPenalty = penalty;
                best = deepCopy(modules);
                bestMask = mask;
            }
            applyMask(mask);
        }
        modules = best;
        drawFormatBits(bestMask);
    }

    private void applyMask(int mask) {
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (!isFunction[y][x]) {
                    boolean invert = switch (mask) {
                        case 0 -> (x + y) % 2 == 0;
                        case 1 -> y % 2 == 0;
                        case 2 -> x % 3 == 0;
                        case 3 -> (x + y) % 3 == 0;
                        case 4 -> ((x / 3) + (y / 2)) % 2 == 0;
                        case 5 -> xyMask(x, y);
                        case 6 -> ((x * y) % 3 + (x + y) % 2) % 2 == 0;
                        case 7 -> ((x + y) % 2 + (x * y) % 3) % 2 == 0;
                        default -> false;
                    };
                    if (invert)
                        modules[y][x] ^= 1;
                }
            }
        }
    }

    private boolean xyMask(int x, int y) {
        return ((x * y) % 2 + (x * y) % 3) % 2 == 0;
    }

    private void drawFormatBits(int mask) {
        int data = (errorCorrectionLevel << 3) | mask;
        int rem = data;
        for (int i = 0; i < 10; i++)
            rem = (rem << 1) ^ ((rem >>> 9) * 0x537);
        data = (data << 10 | rem) ^ 0x5412;
        for (int i = 0; i < 15; i++) {
            boolean bit = ((data >>> i) & 1) != 0;
            if (i < 6) setFunctionModule(8, i, bit);
            else if (i < 8) setFunctionModule(8, i + 1, bit);
            else setFunctionModule(8, size - 15 + i, bit);
            if (i < 8) setFunctionModule(size - i - 1, 8, bit);
            else setFunctionModule(14 - i, 8, bit);
        }
    }

    private int getPenaltyScore() {
        int penalty = 0;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; ) {
                int x2 = x;
                while (x2 < size && modules[y][x] == modules[y][x2]) x2++;
                int runLen = x2 - x;
                if (runLen >= 5)
                    penalty += 3 + runLen - 5;
                x = x2;
            }
        }
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; ) {
                int y2 = y;
                while (y2 < size && modules[y][x] == modules[y2][x]) y2++;
                int runLen = y2 - y;
                if (runLen >= 5)
                    penalty += 3 + runLen - 5;
                y = y2;
            }
        }
        for (int y = 0; y < size - 1; y++) {
            for (int x = 0; x < size - 1; x++) {
                int color = modules[y][x];
                if (color == modules[y][x + 1] && color == modules[y + 1][x] && color == modules[y + 1][x + 1])
                    penalty += 3;
            }
        }
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size - 6; x++) {
                if (modules[y][x] == 1 && modules[y][x + 1] == 0 && modules[y][x + 2] == 1 && modules[y][x + 3] == 1
                    && modules[y][x + 4] == 1 && modules[y][x + 5] == 0 && modules[y][x + 6] == 1)
                    penalty += 40;
            }
        }
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size - 6; y++) {
                if (modules[y][x] == 1 && modules[y + 1][x] == 0 && modules[y + 2][x] == 1 && modules[y + 3][x] == 1
                    && modules[y + 4][x] == 1 && modules[y + 5][x] == 0 && modules[y + 6][x] == 1)
                    penalty += 40;
            }
        }
        int black = 0;
        for (int y = 0; y < size; y++)
            for (int x = 0; x < size; x++)
                if (modules[y][x] == 1)
                    black++;
        int total = size * size;
        int darkPercent = (black * 100 + total / 2) / total;
        int prevMultipleOf5 = (darkPercent / 5) * 5;
        int nextMultipleOf5 = ((darkPercent + 4) / 5) * 5;
        penalty += Math.min(Math.abs(prevMultipleOf5 - 50), Math.abs(nextMultipleOf5 - 50)) * 2;
        return penalty;
    }

    private byte[][] deepCopy(byte[][] array) {
        byte[][] result = new byte[array.length][];
        for (int i = 0; i < array.length; i++)
            result[i] = array[i].clone();
        return result;
    }

    private byte[] addEccAndInterleave(byte[] data) {
        int version = (size - 17) / 4;
        int eccCodewords = ECC_CODEWORDS_PER_BLOCK[errorCorrectionLevel][version];
        int numBlocks = NUM_ERROR_CORRECTION_BLOCKS[errorCorrectionLevel][version];
        int dataLen = data.length;
        int blockLen = (dataLen + numBlocks - 1) / numBlocks;
        byte[][] blocks = new byte[numBlocks][];
        byte[][] eccBlocks = new byte[numBlocks][];
        for (int i = 0, k = 0; i < numBlocks; i++) {
            int sizeInBlock = Math.min(blockLen, dataLen - k);
            blocks[i] = Arrays.copyOfRange(data, k, k + sizeInBlock);
            eccBlocks[i] = reedSolomon(blocks[i], eccCodewords);
            k += sizeInBlock;
        }
        ArrayList<Byte> result = new ArrayList<>();
        for (int i = 0; i < blockLen; i++) {
            for (byte[] block : blocks) {
                if (i < block.length)
                    result.add(block[i]);
            }
        }
        for (int i = 0; i < eccCodewords; i++) {
            for (byte[] ecc : eccBlocks) {
                result.add(ecc[i]);
            }
        }
        byte[] res = new byte[result.size()];
        for (int i = 0; i < res.length; i++)
            res[i] = result.get(i);
        return res;
    }

    private byte[] reedSolomon(byte[] data, int eccLen) {
        byte[] result = new byte[eccLen];
        for (byte b : data) {
            int factor = (b & 0xFF) ^ (result[0] & 0xFF);
            System.arraycopy(result, 1, result, 0, eccLen - 1);
            result[eccLen - 1] = 0;
            if (factor != 0) {
                for (int i = 0; i < eccLen; i++) {
                    result[i] ^= GF256_EXP[(GF256_LOG[factor] + GF256_LOG[GEN_POLY[eccLen][i]]) % 255];
                }
            }
        }
        return result;
    }

    public BufferedImage toImage(int scale, int border) {
        int outputSize = (size + border * 2) * scale;
        BufferedImage img = new BufferedImage(outputSize, outputSize, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < outputSize; y++) {
            for (int x = 0; x < outputSize; x++) {
                boolean val = false;
                int xx = (x / scale) - border;
                int yy = (y / scale) - border;
                if (0 <= xx && xx < size && 0 <= yy && yy < size) {
                    val = modules[yy][xx] == 1;
                }
                img.setRGB(x, y, val ? 0x000000 : 0xFFFFFF);
            }
        }
        return img;
    }

    private static int[] getAlignmentPatternPositions(int size) {
        if (size == 21) return new int[] {6, 14};
        int num = size / 7 + 2;
        int step = (size == 145) ? 26 : (size / (num - 1));
        int[] result = new int[num];
        result[0] = 6;
        result[num - 1] = size - 7;
        for (int i = 1; i < num - 1; i++)
            result[i] = size - 7 - (num - 1 - i) * step;
        return result;
    }

    static int getNumDataCodewords(int version, Ecc ecl) {
        return TOTAL_CODEWORDS[version] - ECC_CODEWORDS_PER_BLOCK[ecl.formatBits][version] * NUM_ERROR_CORRECTION_BLOCKS[ecl.formatBits][version];
    }

    static final int[][] ECC_CODEWORDS_PER_BLOCK = new int[][] {
        {0, 7, 10, 15, 20, 26, 18, 20, 24, 30, 18, 20, 24, 26, 30, 22, 24, 28, 30, 28, 28, 28, 30, 30, 26, 28, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30},
        {0, 10, 16, 26, 18, 24, 16, 18, 22, 22, 20, 24, 26, 30, 22, 24, 28, 28, 26, 26, 26, 26, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28},
        {0, 13, 22, 18, 26, 18, 24, 18, 22, 20, 24, 28, 26, 24, 20, 30, 24, 28, 28, 26, 28, 30, 24, 30, 30, 28, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30},
        {0, 17, 28, 22, 16, 22, 28, 26, 26, 24, 28, 30, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30}
    };

    static final int[][] NUM_ERROR_CORRECTION_BLOCKS = new int[][] {
        {0, 1, 1, 1, 1, 2, 4, 4, 4, 5, 5, 5, 8, 9, 9, 10, 10, 11, 13, 14, 16, 17, 17, 18, 20, 21, 23, 25, 26, 28, 29, 31, 33, 35, 37, 38, 40, 43, 45, 47, 49},
        {0, 1, 1, 1, 2, 2, 4, 4, 5, 5, 5, 8, 9, 9, 12, 12, 15, 16, 17, 18, 20, 21, 23, 25, 26, 27, 29, 31, 33, 35, 37, 38, 40, 43, 45, 47, 49, 51, 53, 55, 57},
        {0, 1, 1, 2, 2, 4, 4, 6, 6, 8, 8, 10, 12, 16, 16, 18, 20, 21, 23, 25, 25, 27, 29, 31, 33, 35, 37, 39, 41, 43, 45, 47, 49, 51, 53, 55, 57, 59, 61, 63, 65},
        {0, 1, 1, 2, 4, 4, 4, 5, 7, 8, 8, 11, 11, 16, 16, 18, 18, 20, 21, 23, 25, 25, 27, 29, 31, 33, 35, 37, 39, 41, 43, 45, 47, 49, 51, 53, 55, 57, 59, 61, 63}
    };

    static final int[] TOTAL_CODEWORDS = new int[] {
        0, 26, 44, 70, 100, 134, 172, 196, 242, 292, 346, 404, 466, 532, 581, 655, 733, 815, 901, 991, 1085,
        1156, 1258, 1364, 1474, 1588, 1706, 1828, 1921, 2051, 2185, 2323, 2465, 2611, 2761, 2876, 3034, 3196, 3362, 3532, 3706
    };

    private static final int[] GF256_EXP = new int[512];
    private static final int[] GF256_LOG = new int[256];
    private static final int[][] GEN_POLY = new int[31][31];

    static {
        int x = 1;
        for (int i = 0; i < 255; i++) {
            GF256_EXP[i] = x;
            GF256_LOG[x] = i;
            x <<= 1;
            if ((x & 0x100) != 0)
                x ^= 0x11D;
        }
        for (int i = 255; i < 512; i++)
            GF256_EXP[i] = GF256_EXP[i - 255];
        for (int degree = 1; degree < GEN_POLY.length; degree++) {
            int[] poly = new int[degree];
            poly[degree - 1] = 1;
            for (int i = 0; i < degree; i++) {
                int[] next = new int[degree];
                for (int j = 0; j < degree; j++) {
                    next[j] ^= GF256_EXP[(GF256_LOG[poly[j] == 0 ? 1 : poly[j]] + i) % 255];
                }
                poly = Arrays.copyOf(next, degree);
            }
            GEN_POLY[degree] = poly;
        }
    }
}
