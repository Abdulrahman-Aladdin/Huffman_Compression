package huffman;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HashTable implements FrequencyTable <String, Integer>, Iterable<Map.Entry<String, Integer>> {

    HashMap<String, Integer> freq;

    HashTable () {
        freq = new HashMap<>();
    }

    @Override
    public void increment(String key) {
        freq.put(key, freq.getOrDefault(key, 0) + 1);
    }

    @Override
    public Integer getFrequencyOf(String key) {
        return freq.get(key);
    }

    @Override
    public Iterator<Map.Entry<String, Integer>> iterator() {
        return freq.entrySet().iterator();
    }
}
