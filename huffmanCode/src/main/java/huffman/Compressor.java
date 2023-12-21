package huffman;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Compressor {
    FrequencyTable<String, Integer> frequencies;
    HashMap<String, String> codes;

    int chunkSize;
    String path;

    int bufferSize;

    HuffmanNode root;

    Logger logger = Logger.getLogger(Compressor.class.getName());

    int maxChunkSize = -1;
    int smallestChunkSize = (int) 1e9;

    public Compressor() {
        codes = new HashMap<>();
        chunkSize = 1;
        root = null;
        bufferSize = 1024 * 1024;
    }

    public void compress (String path, int chunkSize) {
        logger.info("Compressing...");
        this.chunkSize = chunkSize;
        if (chunkSize == 1) {
            this.frequencies = new ArrayTable();
        } else {
            this.frequencies = new HashTable();
        }
        this.path = path;
        long startTime = System.currentTimeMillis();
        readFile(path);

        root = constructHuffmanTree();
        getCodes(root, "");

        int[] t = new int[] {0};

        traverse(root, t);

        String outputPath = getOutputPath(path);
        long totalLength = 0;

        for (Map.Entry<String, String> entry : codes.entrySet()) {
            totalLength += (long) frequencies.getFrequencyOf(entry.getKey()) * entry.getValue().length();
        }

        byte offset = (byte) ((8 - (int) totalLength % 8) % 8);

        writeCompressedFile(outputPath, offset, t[0]);
        long end = System.currentTimeMillis();
        logger.log(Level.INFO, "Compression time -> {0} seconds", (end - startTime) / 1000.0);

        long compressedSize = 0;
        long originalSize = 0;

        try {
            compressedSize = Files.size(Path.of(outputPath));
            originalSize = Files.size(Path.of(path));
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }

        logger.log(Level.INFO, "compression ratio -> {0}", compressedSize / ((originalSize == 0)? 1.0 : (double) originalSize));
    }

    private boolean traverse(HuffmanNode root, int[] t) {
        if (root == null) {
            return false;
        }
        if (root.data != null && root.data.length() == maxChunkSize) {
            t[0]++;
            return false;
        }
        if (root.data != null && root.data.length() == smallestChunkSize) {
            return true;
        }
        if (traverse(root.left, t)) {
            return true;
        }
        return traverse(root.right, t);
    }

    private void writeCompressedFile(String path, byte offset, int t) {
        try (BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(path), bufferSize)) {
            fos.write(offset);
            DataOutputStream dos = new DataOutputStream(fos);
            dos.writeInt(maxChunkSize);
            dos.writeInt(smallestChunkSize);
            dos.writeInt(codes.size());
            dos.writeInt(t);
            writeOptimizedMap(root, dos);
            writeCompressedData(fos, offset);
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }

    private String byteArrayToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append((char) (b & 0xFF));
        }
        return sb.toString();
    }

    private void writeCompressedData(BufferedOutputStream fos, int offset) throws IOException {
            BufferedInputStream fis = new BufferedInputStream(new FileInputStream(path), bufferSize);
            long remaining = Files.size(Path.of(path));
            String remainingString = "";
            String binaryString = "";

            while (remaining > 0) {
                byte[] buffer = new byte[Math.min(bufferSize, (int) remaining)];
                int n = fis.read(buffer);

                if (n == -1) {
                    break;
                }

                String[] data = compressChunk(buffer, binaryString);
                binaryString = data[1];
                String compressed = remainingString + data[0];
                long numberOfBytes = compressed.length() / 8;
                remainingString = compressed.substring((int) numberOfBytes * 8);

                if (numberOfBytes > 0) {
                    writeString(fos, compressed.substring(0, (int) numberOfBytes * 8), (int) numberOfBytes);
                }

                remaining -= buffer.length;
            }

            if (!binaryString.isEmpty()) {
                remainingString += codes.get(binaryString);
            }

            if (!remainingString.isEmpty()) {
                String compressed = remainingString + "0".repeat(offset);
                writeString(fos, compressed, (int) Math.ceil((double) compressed.length() / 8));
            }

            fis.close();
    }

    private void writeOptimizedMap(HuffmanNode root, DataOutputStream dos) throws IOException {
        if (root.data != null) {
            for (char c : root.data.toCharArray()) {
                dos.writeChar(c);
            }
            dos.writeByte(codes.get(root.data).length());
            return;
        }
        writeOptimizedMap(root.left, dos);
        writeOptimizedMap(root.right, dos);
    }

    private String[] compressChunk(byte[] buffer, String remainingString) {
        String s = remainingString + byteArrayToString(buffer);
        int remaining = s.length() % chunkSize;
        remainingString = s.substring(s.length() - remaining);
        s = s.substring(0, s.length() - remaining);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i += chunkSize) {
            String chunk = s.substring(i, Math.min(i + chunkSize, s.length()));
            sb.append(codes.get(chunk));
        }
        return new String[]{sb.toString(), remainingString};
    }

    private void writeString (BufferedOutputStream fos, String compressed, int numberOfBytes) throws IOException {
        byte[] bytes = new byte[numberOfBytes];
        for (int i = 0; i < compressed.length(); i += 8) {
            String byteString = compressed.substring(i, Math.min(i + 8, compressed.length()));
            bytes[i / 8] = (byte) Integer.parseInt(byteString, 2);
        }
        fos.write(bytes);
    }

    private String getOutputPath(String path) {
        Path inputPath = Path.of(path);
        String fileName = inputPath.getFileName().toString();
        return inputPath.resolveSibling("20010824." + chunkSize + "." + fileName + ".hc").toString();
    }

    private String process(byte[] bytes, String remainingString) {
        StringBuilder s = new StringBuilder(remainingString);
        s.append(byteArrayToString(bytes));
        int remaining = s.length() % chunkSize;
        remainingString = s.substring(s.length() - remaining, s.length());
        s.delete(s.length() - remaining, s.length());

        for (int i = 0; i < s.length(); i += chunkSize) {
            String chunk = s.substring(i, Math.min(i + chunkSize, s.length()));
            if (chunk.length() > maxChunkSize) {
                maxChunkSize = chunk.length();
            }
            if (chunk.length() < smallestChunkSize) {
                smallestChunkSize = chunk.length();
            }
            frequencies.increment(chunk);
        }
        return remainingString;
    }

    private HuffmanNode constructHuffmanTree() {
        PriorityQueue<HuffmanNode> priorityQueue = new PriorityQueue<>();

        if (chunkSize == 1) {
            for (ArrayEntry entry : (ArrayTable) frequencies) {
                priorityQueue.add(new HuffmanNode(entry.key.toString(), entry.value, null, null));
            }
        } else {
            for (Map.Entry<String, Integer> entry : (HashTable) frequencies) {
                priorityQueue.add(new HuffmanNode(entry.getKey(), entry.getValue(), null, null));
            }
        }

        while (priorityQueue.size() > 1) {
            HuffmanNode left = priorityQueue.poll();
            HuffmanNode right = priorityQueue.poll();
            if (right == null) {
                continue;
            }
            HuffmanNode parent = new HuffmanNode(null, left.frequency + right.frequency, left, right);

            priorityQueue.add(parent);
        }

        return priorityQueue.poll();
    }

    private void getCodes(HuffmanNode root, String code) {
        if (root.data != null) {
            if (code.isEmpty()) {
                code = "0";
            }
            codes.put(root.data, code);
            return;
        }
        getCodes(root.left, code + "0");
        getCodes(root.right, code + "1");
    }

    private void readFile(String path) {
        String remainingString = "";


        try (BufferedInputStream fis = new BufferedInputStream(new FileInputStream(path))) {
            long remaining = Files.size(Path.of(path));

            while (remaining > 0) {
                byte[] buffer = new byte[Math.min(bufferSize, (int) remaining)];
                int n = fis.read(buffer);

                if (n == -1) {
                    break;
                }
                remainingString = process(buffer, remainingString);
                remaining -= buffer.length;
            }

        } catch (Exception e) {
            logger.severe(e.getMessage());
        }

        if (!remainingString.isEmpty()) {
            smallestChunkSize = remainingString.length();
            frequencies.increment(remainingString);
        }
    }

}
