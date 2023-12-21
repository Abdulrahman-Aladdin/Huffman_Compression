package huffman;

public interface FrequencyTable <K, V> {
    void increment(K key);
    V getFrequencyOf(K key);
}
