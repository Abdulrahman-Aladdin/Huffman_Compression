package huffman;

public class HuffmanNode implements Comparable<HuffmanNode> {
    public String data;
    public final int frequency;

    public HuffmanNode left;
    public HuffmanNode right;

    public HuffmanNode(String data, int frequency, HuffmanNode left, HuffmanNode right) {
        this.data = data;
        this.frequency = frequency;
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof HuffmanNode other)) return false;
        return this.data.equals(other.data) && this.frequency == other.frequency;
    }

    @Override
    public int compareTo(HuffmanNode o) {
        return this.frequency - o.frequency;
    }
}
