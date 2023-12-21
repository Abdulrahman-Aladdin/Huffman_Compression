package huffman;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Decompressor {

    private record Pair(String data, int len) {}

    Logger logger = Logger.getLogger(Decompressor.class.getName());

    private final String path;
    private int offset;

    private HuffmanNode root;
    HashMap<String, String> codes;

    public Decompressor(String path) {
        this.path = path;
        codes = new HashMap<>();
        offset = 0;
        root = null;
    }

    public void decompress() {
        logger.info("Decompressing...");
        long startTime = System.currentTimeMillis();
        readFile();
        long end = System.currentTimeMillis();
        logger.log(Level.INFO, "Decompression time -> {0} seconds", (end - startTime) / 1000.0);
    }

    private void readFile() {
        try (BufferedInputStream fis = new BufferedInputStream(new FileInputStream(path))) {
            offset = fis.read();
            this.root = readOptimizedMap(fis);
            readCompressedData(fis);
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }

    private void readCompressedData(BufferedInputStream fis) throws IOException {
        long totalLength = fis.available();
        FileOutputStream fos = new FileOutputStream(getOutputPath(path));
        String temp = "";

        while (totalLength > 0) {
            int bufferSize = 1024 * 1024;
            byte[] buffer = new byte[Math.min(bufferSize, (int) totalLength)];
            int n = fis.read(buffer);

            if (n == -1) {
                break;
            }

            StringBuilder sb = new StringBuilder(buffer.length * 8);
            for (byte b : buffer) {
                for (int i = 7; i >= 0; i--) {
                    sb.append((b >> i) & 1);
                }

                if (totalLength == 1) {
                    while (offset > 0) {
                        sb.deleteCharAt(sb.length() - 1);
                        offset--;
                    }
                }

                totalLength--;
            }

            String[] data = getOriginalDataOP(sb.toString().toCharArray(), temp, this.root);
            temp = data[1];
            fos.write(stringToByteArray(data[0]));
        }

        fos.close();
    }

    private byte[] stringToByteArray(String s) {
        byte[] bytes = new byte[s.length()];
        for (int i = 0; i < s.length(); i++) {
            bytes[i] = (byte) s.charAt(i);
        }
        return bytes;
    }

    private HuffmanNode readOptimizedMap(BufferedInputStream fis) throws IOException {
        DataInputStream dis = new DataInputStream(fis);
        int maxChunkSize = dis.readInt();
        int smallestChunkSize = dis.readInt();
        int codesSize = dis.readInt();
        int diffChunk = dis.readInt();

        Pair[] mapEntries = new Pair[codesSize];
        int idx = 0;

        HuffmanNode tempRoot = new HuffmanNode(null, 0, null, null);

        for (; idx < codesSize; idx++) {
            StringBuilder sb = new StringBuilder();
            int limit = maxChunkSize;
            if (idx == diffChunk) {
                limit = smallestChunkSize;
            }
            for (int j = 0; j < limit; j++) {
                sb.append(dis.readChar());
            }
            mapEntries[idx] = new Pair(sb.toString(), dis.readByte());
        }

        constructTree(tempRoot, mapEntries, new int[]{0}, 0);


        return tempRoot;
    }

    private void constructTree(HuffmanNode root, Pair[] codes, int[] idx, int depth) {
        if (idx[0] == codes.length) {
            return;
        }
        if (depth == codes[idx[0]].len) {
            root.data = codes[idx[0]++].data;
            return;
        }
        if (root.left == null) {
            root.left = new HuffmanNode(null, 0, null, null);
        }
        constructTree(root.left, codes, idx, depth + 1);
        if (root.right == null) {
            root.right = new HuffmanNode(null, 0, null, null);
        }
        constructTree(root.right, codes, idx, depth + 1);
    }

    private String[] getOriginalDataOP(char[] binaryString, String rem, HuffmanNode originalRoot) {
        StringBuilder sb = new StringBuilder();
        StringBuilder remaining = new StringBuilder();
        HuffmanNode tempRoot = originalRoot;
        for (char c : rem.toCharArray()) {
            if (c == '0') {
                tempRoot = tempRoot.left;
            } else {
                tempRoot = tempRoot.right;
            }
        }
        for (char c : binaryString) {
            if (c == '0') {
                tempRoot = tempRoot.left;
            } else {
                tempRoot = tempRoot.right;
            }
            remaining.append(c);
            if (tempRoot.data != null) {
                sb.append(tempRoot.data);
                tempRoot = originalRoot;
                remaining = new StringBuilder();
            }
        }
        return new String[] {sb.toString(), remaining.toString()};
    }

    private String getOutputPath(String path) {
        Path inputPath = Path.of(path);
        String fileName = inputPath.getFileName().toString();
        String[] parts = fileName.split("\\.");
        if (parts.length == 5) {
            return inputPath.resolveSibling("extracted." + parts[2] + "." + parts[3]).toString();
        } else {
            return inputPath.resolveSibling("extracted." + parts[2]).toString();
        }
    }
}
