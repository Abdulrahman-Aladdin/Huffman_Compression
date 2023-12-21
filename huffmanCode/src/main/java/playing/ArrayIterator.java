package playing;

import java.util.Iterator;

public class ArrayIterator implements Iterator<ArrayEntry> {

    int idx;
    ArrayTable arr;
    ArrayIterator (ArrayTable arr) {
        idx = 0;
        this.arr = arr;
    }

    @Override
    public boolean hasNext() {
        for (; idx < 256; idx++) {
            if (arr.arr[idx] > 0) return true;
        }
        return false;
    }

    @Override
    public ArrayEntry next() {
        while (arr.arr[idx] == 0) idx++;
        return new ArrayEntry((char) idx, arr.arr[idx++]);
    }
}
