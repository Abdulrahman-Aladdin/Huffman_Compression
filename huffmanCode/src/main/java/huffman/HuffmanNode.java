package huffman;

public class HuffmanNode implements Comparable<HuffmanNode> {
    public final String data;
    public final int frequency;

    public final HuffmanNode left;
    public final HuffmanNode right;

    public HuffmanNode(String data, int frequency, HuffmanNode left, HuffmanNode right) {
        this.data = data;
        this.frequency = frequency;
        this.left = left;
        this.right = right;
    }

    @Override
    public int compareTo(HuffmanNode o) {
        return this.frequency - o.frequency;
    }
}
