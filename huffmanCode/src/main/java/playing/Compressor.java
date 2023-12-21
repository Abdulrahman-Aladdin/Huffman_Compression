package playing;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class Compressor {
    HashMap<String, Integer> frequencies;
    HashMap<String, String> codes;

    int chunkSize;
    String path;

    int bufferSize;

    HuffmanNode root;

    int maxChunkSize = -1;
    int smallestChunkSize = (int) 1e9;

    public Compressor() {
        frequencies = new HashMap<>();
        codes = new HashMap<>();
        chunkSize = 1;
        root = null;
        bufferSize = 1024;
    }

    public void compress (String path, int chunkSize) {
        System.out.println("Compressing...");
        this.chunkSize = chunkSize;
        this.path = path;
        long startTime = System.currentTimeMillis();
        readFile(path);
        long readingEndTime = System.currentTimeMillis();

        System.out.println("reading time -> " + (readingEndTime - startTime) / 1000.0);

        root = constructHuffmanTree();
        getCodes(root, "");

        System.out.println("constructing tree -> " + (System.currentTimeMillis() - readingEndTime) / 1000.0);

        int[] t = new int[] {0};

        traverse(root, t);

        String outputPath = getOutputPath(path);
        long totalLength = 0;

        for (Map.Entry<String, String> entry : codes.entrySet()) {
            totalLength += (long) frequencies.get(entry.getKey()) * entry.getValue().length();
        }

        byte offset = (byte) ((8 - (int) totalLength % 8) % 8);

        writeCompressedFile(outputPath, offset, t[0]);
        long end = System.currentTimeMillis();
        System.out.println("time: " + (end - startTime) / 1000.0);

        long compressedSize = 0;
        long originalSize = 0;

        try {
            compressedSize = Files.size(Path.of(outputPath));
            originalSize = Files.size(Path.of(path));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        System.out.println("compression ratio -> " + compressedSize / (double) originalSize);
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
            writeOptimizedMap(fos, root, dos);
            writeCompressedData(fos, offset);
        } catch (Exception e) {
            e.printStackTrace();
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

                long start = System.currentTimeMillis();
                String[] data = compressChunk(buffer, binaryString);
                binaryString = data[1];
                String compressed = remainingString + data[0];
                long numberOfBytes = compressed.length() / 8;
                remainingString = compressed.substring((int) numberOfBytes * 8);

                long st = System.currentTimeMillis();

                // System.out.println("compressing -> " + (st - start) / 1000.0);

                if (numberOfBytes > 0) {
                    writeString(fos, compressed.substring(0, (int) numberOfBytes * 8), (int) numberOfBytes);
                }
                long end = System.currentTimeMillis();
                // System.out.println("write chunk -> " + (end - st) / 1000.0);

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

    private void writeOptimizedMap(BufferedOutputStream fos, HuffmanNode root, DataOutputStream dos) throws IOException {
        if (root.data != null) {
            for (char c : root.data.toCharArray()) {
                dos.writeChar(c);
            }
            dos.writeInt(codes.get(root.data).length());
            return;
        }
        writeOptimizedMap(fos, root.left, dos);
        writeOptimizedMap(fos, root.right, dos);
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

    private void writeMap(BufferedOutputStream fos) throws IOException {
        HashMap<String, String> map = new HashMap<>();
        for (Map.Entry<String, String> entry : codes.entrySet()) {
            map.put(entry.getValue(), entry.getKey());
        }
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(map);
    }

    private String getOutputPath(String path) {
        Path inputPath = Path.of(path);
        String fileName = inputPath.getFileName().toString();
        return inputPath.resolveSibling("20010824." + chunkSize + "." + fileName + ".hc").toString();
    }

    private String process(byte[] bytes, String remainingString) {
        long st = System.currentTimeMillis();
        String s = remainingString + byteArrayToString(bytes);
        int remaining = s.length() % chunkSize;
        remainingString = s.substring(s.length() - remaining, s.length());
        s = s.substring(0, s.length() - remaining);

        for (int i = 0; i < s.length(); i += chunkSize) {
            String chunk = s.substring(i, Math.min(i + chunkSize, s.length()));
            if (chunk.length() > maxChunkSize) {
                maxChunkSize = chunk.length();
            }
            if (chunk.length() < smallestChunkSize) {
                smallestChunkSize = chunk.length();
            }
            frequencies.put(chunk, frequencies.getOrDefault(chunk, 0) + 1);
        }
        return remainingString;
    }

    private HuffmanNode constructHuffmanTree() {
        PriorityQueue<HuffmanNode> priorityQueue = new PriorityQueue<>();

        for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
            priorityQueue.add(new HuffmanNode(entry.getKey(), entry.getValue(), null, null));
        }

        while (priorityQueue.size() > 1) {
            HuffmanNode left = priorityQueue.poll();
            HuffmanNode right = priorityQueue.poll();

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
                long st = System.currentTimeMillis();
                byte[] buffer = new byte[Math.min(bufferSize, (int) remaining)];
                int n = fis.read(buffer);
                long end = System.currentTimeMillis();

                if (n == -1) {
                    break;
                }
                remainingString = process(buffer, remainingString);
                remaining -= buffer.length;
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        if (!remainingString.isEmpty()) {
            smallestChunkSize = remainingString.length();
            frequencies.put(remainingString, 1);
        }
    }

}
