package deque;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.util.Comparator;

public class MaxArrayDequeTest {

    private static class Dog implements Comparable<Dog> {
        private String name;
        private int size;

        public Dog(String nm, int sz) {
            name = nm;
            size = sz;
        }

        public int size() {
            return size;
        }
        public int compareTo(Dog t) {
            return size - t.size();
        }

        private static class NameComparator implements Comparator<Dog> {
            public int compare(Dog x, Dog y) {
                return x.name.compareTo(y.name);
            }
        }

        public static Comparator<Dog> getNameComparator() {
            return new NameComparator();
        }

        private static class SizeComparator implements Comparator<Dog> {
            public int compare(Dog x, Dog y) {
                return x.compareTo(y);
            }
        }

        public static Comparator<Dog> getSizeComparator() {
            return new SizeComparator();
        }
    }
    @Test
    public void ArrayDequeSizeTest() {
        MaxArrayDeque<Dog> maxDog = new MaxArrayDeque<Dog>(Dog.getNameComparator());
        Dog y = new Dog("yao", 22);
        Dog ch = new Dog("chen", 23);
        Dog ke = new Dog("keke", 5);
        maxDog.addLast(y);
        maxDog.addLast(ch);
        maxDog.addLast(ke);

        assertEquals(maxDog.max(), y);
        assertEquals(maxDog.max(Dog.getSizeComparator()), ch);
    }
}
