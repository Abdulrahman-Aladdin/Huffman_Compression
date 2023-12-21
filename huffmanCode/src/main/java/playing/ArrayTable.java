package playing;

import java.util.Arrays;
import java.util.Iterator;

public class ArrayTable implements FrequencyTable <String, Integer>, Iterable<ArrayEntry> {

    Integer[] arr;

    ArrayTable () {
        arr = new Integer[256];
        Arrays.fill(arr, 0);
    }

    @Override
    public void increment(String key) {
        arr[key.charAt(0)]++;
    }

    @Override
    public Integer getFrequencyOf(String key) {
        return arr[key.charAt(0)];
    }

    @Override
    public Iterator<ArrayEntry> iterator() {
        return new ArrayIterator(this);
    }
}
