package playing;

import javax.swing.tree.TreeNode;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Decompressor {

    private class Pair {
        public String data;
        public int len;

        public Pair(String data, int len) {
            this.data = data;
            this.len = len;
        }
    }

    private final String path;
    private final int bufferSize = 1024 * 1024;
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
        System.out.println("Decompressing...");
        long startTime = System.currentTimeMillis();
        readFile();
        long end = System.currentTimeMillis();
        System.out.println("time: " + (end - startTime) / 1000.0);
    }

    private void readFile() {
        try (BufferedInputStream fis = new BufferedInputStream(new FileInputStream(path))) {
            offset = fis.read();
            this.root = readOptimizedMap(fis);
            readCompressedData(fis);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void readCompressedData(BufferedInputStream fis) throws IOException {
        long totalLength = fis.available();
        FileOutputStream fos = new FileOutputStream(getOutputPath(path));
        String temp = "";
        while (totalLength > 0) {
            byte[] buffer = new byte[Math.min(bufferSize, (int) totalLength)];
            int read = fis.read(buffer);
            StringBuilder sb = new StringBuilder();
            for (byte b : buffer) {
                sb.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
                if (totalLength == 1) {
                    sb.delete(sb.length() - offset, sb.length());
                }
                totalLength--;
            }
            // String[] data = getOriginalData(sb.toString(), binaryString);



            String[] data = getOriginalDataOP(sb.toString().toCharArray(), temp, this.root);
            temp = data[1];
            fos.write(StringToByteArray(data[0]));
        }
    }

    private byte[] StringToByteArray(String s) {
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

        Pair[] codes = new Pair[codesSize];
        int idx = 0;

        HuffmanNode root = new HuffmanNode(null, 0, null, null);

        for (; idx < codesSize; idx++) {
            StringBuilder sb = new StringBuilder();
            int limit = maxChunkSize;
            if (idx == diffChunk) {
                limit = smallestChunkSize;
            }
            for (int j = 0; j < limit; j++) {
                sb.append(dis.readChar());
            }
//            byte[] buffer = new byte[maxChunkSize];
//            fis.read(buffer);
            codes[idx] = new Pair(sb.toString(), dis.readInt());
        }
//        StringBuilder sb = new StringBuilder();
//        for (int j = 0; j < smallestChunkSize; j++) {
//            sb.append(dis.readChar());
//        }
//        byte[] buffer = new byte[smallestChunkSize];
//        fis.read(buffer);
        // codes[idx] = new Pair(sb.toString(), dis.readInt());


        constructTree(root, codes, new int[]{0}, 0);


        return root;
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

    private void printTree(HuffmanNode root) {
        if (root == null) {
            return;
        }
        if (root.data != null) {
            System.out.println(root.data);
        }
        printTree(root.left);
        printTree(root.right);
    }

    private String[] getOriginalDataOP(char[] binaryString, String rem, HuffmanNode originalRoot) {
        StringBuilder sb = new StringBuilder();
        StringBuilder remaining = new StringBuilder();
        HuffmanNode root = originalRoot;
        for (char c : rem.toCharArray()) {
            if (c == '0') {
                root = root.left;
            } else {
                root = root.right;
            }
        }
        for (char c : binaryString) {
            if (c == '0') {
                root = root.left;
            } else {
                root = root.right;
            }
            remaining.append(c);
            if (root.data != null) {
                sb.append(root.data);
                root = originalRoot;
                remaining = new StringBuilder();
            }
        }
        return new String[] {sb.toString(), remaining.toString()};
    }

    private String[] getOriginalData(String binaryString, String remainingString) {
        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder(remainingString);
        for (int i = 0; i < binaryString.length(); i++) {
            temp.append(binaryString.charAt(i));
            if (codes.containsKey(temp.toString())) {
                sb.append(codes.get(temp.toString()));
                temp = new StringBuilder();
            }
        }
        return new String[] { sb.toString(), temp.toString() };
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

    private void readMap(FileInputStream fis) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(fis);
        codes = (HashMap<String, String>) ois.readObject();
    }
}
