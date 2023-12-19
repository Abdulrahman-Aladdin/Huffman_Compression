package huffman;

import java.util.*;

public class HuffmanCompressor {
    private final IOHandler ioHandler;

    HuffmanCompressor (IOHandler ioHandler) {
        this.ioHandler = ioHandler;
    }

    public void compress() {
        List<String> bytes = ioHandler.readNormalFile();    // read file and convert to base64

        System.out.println("Bytes: ");
        for (String byteChunk : bytes) {
            System.out.println(byteChunk);
        }

        HashMap<String, Integer> frequencies = getFrequencies(bytes);   // get frequencies of each byte chunk

        System.out.println("Frequencies: ");
        for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue());
        }

        HuffmanNode root = buildHuffmanTree(frequencies);   // build huffman tree
        HashMap<String, String> codes = new HashMap<>();
        getCodes(root, "", codes);  // get codes for each byte chunk

        System.out.println("Codes: ");
        for (Map.Entry<String, String> entry : codes.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue());
        }

        String encoded = encode(bytes, codes);  // encode the file

        System.out.println("Encoded: " + encoded);

        int fragment = 8 - (encoded.length() % 8);  // get the number of bits to be padded
        byte[] treeBytes = HuffmanNode.toBytes(root);  // convert the tree to bytes
        byte[] fragmentBytes = new byte[1]; // convert the fragment to bytes
        fragmentBytes[0] = (byte) fragment;
        byte[] encodedBytes = toBytesArray(encoded);  // convert the encoded file to bytes
        byte[] temp = concat(fragmentBytes, treeBytes);

        System.out.println("Fragment: " + fragment);
        System.out.println(fragmentBytes[0]);

        fragmentBytes[0] = (byte) '\n';

        byte[] temp2 = concat(temp, fragmentBytes);

        byte[] compressed = concat(temp2, encodedBytes);  // concatenate the fragment, tree, and encoded file


        ioHandler.writeCompressed(compressed);  // write the compressed file
        System.out.println("Compressed: " + Base64.getEncoder().encodeToString(compressed));
    }

    private byte[] concat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
    private String encode(List<String> bytes, HashMap<String, String> codes) {
        StringBuilder encoded = new StringBuilder();
        for (String byteChunk : bytes) {
            encoded.append(codes.get(byteChunk));
        }
        return encoded.toString();
    }

    private HuffmanNode buildHuffmanTree(HashMap<String, Integer> frequencies) {
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

    private HashMap<String, Integer> getFrequencies(List<String> bytes) {
        HashMap<String, Integer> frequencies = new HashMap<>();
        for (String byteChunk : bytes) {
            if (frequencies.containsKey(byteChunk)) {
                frequencies.put(byteChunk, frequencies.get(byteChunk) + 1);
            } else {
                frequencies.put(byteChunk, 1);
            }
        }
        return frequencies;
    }

    private void getCodes(HuffmanNode root, String code, HashMap<String, String> codes) {
        if (root.data != null) {
            codes.put(root.data, code);
            return;
        }
        getCodes(root.left, code + "0", codes);
        getCodes(root.right, code + "1", codes);
    }

    private byte[] toBytesArray(String binaryString) {
        int len = binaryString.length();
        byte[] data = new byte[(int) Math.ceil(len / 8.0)];
        for (int i = 0; i < len; i += 8) {
            data[i / 8] = (byte) Integer.parseInt(binaryString.substring(i, Math.min(i + 8, len)), 2);
        }
        return data;
    }
}
