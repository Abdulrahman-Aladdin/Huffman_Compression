package huffman;

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
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int compareTo(HuffmanNode o) {
        return this.frequency - o.frequency;
    }
}
