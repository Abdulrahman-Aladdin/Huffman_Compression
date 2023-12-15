package huffman;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class HuffmanCompressor {
    public String compress(List<String> bytes) {
        HashMap<String, Integer> frequencies = getFrequencies(bytes);
        HuffmanNode root = buildHuffmanTree(frequencies);
        HashMap<String, String> codes = new HashMap<>();
        getCodes(root, "", codes);
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
}
