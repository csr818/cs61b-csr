package deque;

public class ArrayDeque<T> {
    private int N = 8;
    private int size = 0;
    private int nextFirst = N-1;
    private int nextLast = 0;
    private T[] items;

    public ArrayDeque() {
        items = (T[]) new Object[N];
    }

    private void resize(boolean inc) {
        int old_N = N;
        N = inc ? N*2 : N/2;
        T[] tmp = (T[]) new Object[N];
        int index = 0;
        int p = (nextFirst + 1) % old_N;
        if (inc) { // is full
            tmp[index] = items[p];
            items[p] = null;
            index += 1;
            p = (p + 1) % old_N;
        }
        while (p!=nextLast) {
            tmp[index] = items[p];
            items[p] = null;
            index += 1;
            p = (p + 1) % old_N;
        }
        nextLast = index;
        nextFirst = N - 1;
        items = tmp;

    }
    public void addLast(T it) {
        if (size==N) {
            resize(true);
        }
        items[nextLast] = it;
        nextLast = (nextLast + 1) % N;
        size += 1;
    }

    public void addFirst(T it) {
        if (size == N) {
            resize(true);
        }
        items[nextFirst] = it;
        nextFirst = (nextFirst -1 + N)%N;
        size += 1;
    }

    public boolean isEmpty() {
        return size==0;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        int p = (nextFirst + 1) % N;
        while (p != nextLast) {
            System.out.print(items[p] + " ");
            p = (p + 1) % N;
        }
        System.out.println();
    }

    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        nextLast = (nextLast - 1 + N) % N;
        T ret = items[nextLast];
        items[nextLast] = null;
        size -= 1;
        if (N>=16 && size < N/4) {
            resize(false);
        }
        return ret;
    }

    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        nextFirst = (nextFirst + 1) % N;
        T ret = items[nextFirst];
        items[nextFirst] = null;
        size -= 1;
        if (N >= 16 && size < N/4) {
            resize(false);
        }
        return ret;
    }

    public T get(int index) {
        if (index >= size()) {
            return null;
        }
        return items[(nextFirst + 1 + index) % N];
    }
}
