package deque;
import java.util.Comparator;
public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> cmp = null;
    public MaxArrayDeque(Comparator<T> c) {
        //super();
        cmp = c;
    }

    //public MaxArrayDeque() { }
    public T max() {
        if (isEmpty()) {
            return null;
        }
        int index = 0;
        for (int i = 0; i < size(); i++) {
            if (cmp.compare(get(i), get(index)) > 0) {
                index = i;
            }
        }
        return get(index);
    }

    public T max(Comparator<T> c) {
        if (isEmpty()) {
            return null;
        }
        int index = 0;
        for (int i = 0; i < size(); i++) {
            if (c.compare(get(i), get(index)) > 0) {
                index = i;
            }
        }
        return get(index);
    }
}
