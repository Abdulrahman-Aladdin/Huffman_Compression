package playing;

public interface FrequencyTable <K, V> {
    public void increment(K key);
    public V getFrequencyOf(K key);
}
