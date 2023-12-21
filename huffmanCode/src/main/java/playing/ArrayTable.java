package playing;

import java.util.Arrays;
import java.util.Iterator;

public class ArrayTable implements FrequencyTable <Character, Integer>, Iterable<ArrayEntry> {

    Integer[] arr;

    ArrayTable () {
        arr = new Integer[256];
        Arrays.fill(arr, 0);
    }

    @Override
    public void increment(Character key) {
        arr[key]++;
    }

    @Override
    public Integer getFrequencyOf(Character key) {
        return arr[key];
    }

    @Override
    public Iterator<ArrayEntry> iterator() {
        return new ArrayIterator(this);
    }
}
