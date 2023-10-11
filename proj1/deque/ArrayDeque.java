package deque;
import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private int N = 8;
    private int size = 0;
    private int nextFirst = N - 1;
    private int nextLast = 0;
    private T[] items;

    public ArrayDeque() {
        items = (T[]) new Object[N];
    }

    private void resize(boolean inc) {
        int oldN = N;
        N = inc ? N * 2 : N / 2;
        T[] tmp = (T[]) new Object[N];
        int index = 0;
        int p = (nextFirst + 1) % oldN;
        if (inc) { // is full
            tmp[index] = items[p];
            items[p] = null;
            index += 1;
            p = (p + 1) % oldN;
        }
        while (p != nextLast) {
            tmp[index] = items[p];
            items[p] = null;
            index += 1;
            p = (p + 1) % oldN;
        }
        nextLast = index;
        nextFirst = N - 1;
        items = tmp;

    }
    @Override
    public void addLast(T it) {
        if (size == N) {
            resize(true);
        }
        items[nextLast] = it;
        nextLast = (nextLast + 1) % N;
        size += 1;
    }
    @Override
    public void addFirst(T it) {
        if (size == N) {
            resize(true);
        }
        items[nextFirst] = it;
        nextFirst = (nextFirst - 1 + N) % N;
        size += 1;
    }
    @Override
    public int size() {
        return size;
    }
    @Override
    public void printDeque() {
        int p = (nextFirst + 1) % N;
        while (p != nextLast) {
            System.out.print(items[p] + " ");
            p = (p + 1) % N;
        }
        System.out.println();
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        nextLast = (nextLast - 1 + N) % N;
        T ret = items[nextLast];
        items[nextLast] = null;
        size -= 1;
        if (N >= 16 && size < N / 4) {
            resize(false);
        }
        return ret;
    }

    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        nextFirst = (nextFirst + 1) % N;
        T ret = items[nextFirst];
        items[nextFirst] = null;
        size -= 1;
        if (N >= 16 && size < N / 4) {
            resize(false);
        }
        return ret;
    }

    @Override
    public T get(int index) {
        if (index >= size()) {
            return null;
        }
        return items[(nextFirst + 1 + index) % N];
    }

    public boolean equals(Object o) {
        if (o instanceof Deque<?>) {
            Deque<T> tmp = (Deque<T>) o;
            if (tmp.size() != size()) {
                return false;
            }
            for (int i = 0; i < size(); i++) {
                if (!get(i).equals(tmp.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T> {
        private int curPos = 0;

        public boolean hasNext() {
            return curPos < size();
        }

        public T next() {
            T retItem = get(curPos);
            curPos += 1;
            return retItem;
        }
    }
}
