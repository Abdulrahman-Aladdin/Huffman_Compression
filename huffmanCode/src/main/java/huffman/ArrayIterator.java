package huffman;

import java.util.Iterator;
import java.util.NoSuchElementException;

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
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return new ArrayEntry((char) idx, arr.arr[idx++]);
    }
}
