package playing;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class Compressor {
    HashMap<String, Integer> frequencies;
    HashMap<String, String> codes;

    int chunkSize;
    String path;

    int bufferSize;

    public Compressor() {
        frequencies = new HashMap<>();
        codes = new HashMap<>();
        chunkSize = 1;
        bufferSize = 1024 * 1024 * 32;
    }

    public void compress (String path, int chunkSize) {
        System.out.println("Compressing...");
        this.chunkSize = chunkSize;
        this.path = path;
        long startTime = System.currentTimeMillis();
        readFile(path);

        HuffmanNode root = constructHuffmanTree();
        getCodes(root, "");

        String outputPath = getOutputPath(path);
        long totalLength = 0;

        for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
            totalLength += (long) entry.getValue() * codes.get(entry.getKey()).length();
        }

        byte offset = (byte) ((8 - (int) totalLength % 8) % 8);

        writeCompressedFile(outputPath, offset);
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

    private void writeCompressedFile(String path, byte offset) {
        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(offset);
            writeMap(fos);
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

    private void writeCompressedData(FileOutputStream fos, int offset) throws IOException {
            FileInputStream fis = new FileInputStream(path);
            long remaining = Files.size(Path.of(path));
            String remainingString = "";

            while (remaining > 0) {
                byte[] buffer = new byte[Math.min(bufferSize, (int) remaining)];
                int n = fis.read(buffer);

                if (n == -1) {
                    break;
                }

                String compressed = remainingString + compressChunk(buffer);
                long numberOfBytes = compressed.length() / 8;
                remainingString = compressed.substring((int) numberOfBytes * 8);

                if (numberOfBytes > 0) {
                    writeString(fos, compressed.substring(0, (int) numberOfBytes * 8), (int) numberOfBytes);
                }

                remaining -= buffer.length;
            }

            if (!remainingString.isEmpty()) {
                String compressed = remainingString + "0".repeat(offset);
                writeString(fos, compressed, (int) Math.ceil((double) compressed.length() / 8));
            }

            fis.close();
    }

    private String compressChunk(byte[] buffer) {
        // String s = Base64.getEncoder().encodeToString(buffer).replace("=", "");
        String s = byteArrayToString(buffer);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i += chunkSize) {
            String chunk = s.substring(i, Math.min(i + chunkSize, s.length()));
            sb.append(codes.get(chunk));
        }
        return sb.toString();
    }

    private void writeString (FileOutputStream fos, String compressed, int numberOfBytes) throws IOException {
        byte[] bytes = new byte[numberOfBytes];
        for (int i = 0; i < compressed.length(); i += 8) {
            String byteString = compressed.substring(i, Math.min(i + 8, compressed.length()));
            bytes[i / 8] = (byte) Integer.parseInt(byteString, 2);
        }
        fos.write(bytes);
    }

    private void writeMap(FileOutputStream fos) throws IOException {
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

    private void process(byte[] bytes) {
        // String s = Base64.getEncoder().encodeToString(bytes).replace("=", "");
        String s = byteArrayToString(bytes);
        for (int i = 0; i < s.length(); i += chunkSize) {
            String chunk = s.substring(i, Math.min(i + chunkSize, s.length()));
            frequencies.put(chunk, frequencies.getOrDefault(chunk, 0) + 1);
        }
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
            codes.put(root.data, code);
            return;
        }
        getCodes(root.left, code + "0");
        getCodes(root.right, code + "1");
    }

    private void readFile(String path) {

        try (FileInputStream fis = new FileInputStream(path)) {
            long remaining = Files.size(Path.of(path));

            while (remaining > 0) {
                byte[] buffer = new byte[Math.min(bufferSize, (int) remaining)];
                int n = fis.read(buffer);

                if (n == -1) {
                    break;
                }
                process(buffer);
                remaining -= buffer.length;
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }



}
