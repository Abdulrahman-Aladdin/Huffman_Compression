package playing;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class HuffmanNode implements Comparable<HuffmanNode>, Serializable {
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

    public static byte[] toBytes(HuffmanNode root) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(root);
            oos.flush();
            return bos.toByteArray();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof HuffmanNode)) return false;
        HuffmanNode other = (HuffmanNode) o;
        return this.data.equals(other.data) && this.frequency == other.frequency;
    }

    @Override
    public int compareTo(HuffmanNode o) {
        return this.frequency - o.frequency;
    }
}
