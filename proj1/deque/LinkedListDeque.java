package deque;

import com.sun.source.tree.BreakTree;


public class LinkedListDeque<T> {
    private static class Node<T> {
        T item = null;
        Node<T> pre = null;
        Node<T> nxt = null;
        public Node(T it, Node<T> p, Node<T> n) {
            item = it;
            pre = p;
            nxt = n;
        }
        public Node() { }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    public LinkedListDeque() {
        head = new Node<T>();
        tail = new Node<T>();
        head.nxt = tail;
        tail.pre = head;
        size = 0;
    }
    public void addLast(T it) {
        Node<T> nd = new Node<T>(it, tail.pre, tail);
        tail.pre.nxt = nd;
        tail.pre = nd;
        size += 1;
    }

    public void addFirst(T it) {
        Node<T> nd = new Node<T>(it, head, head.nxt);
        head.nxt.pre = nd;
        head.nxt = nd;
        size += 1;
    }
    public boolean isEmpty() {
        return size==0;
    }
    public void printDeque() {
        Node<T> p = head.nxt;
        while (p!=tail) {
            System.out.print(p.item + " ");
            p = p.nxt;
        }
        System.out.println();
    }
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        T ret = head.nxt.item;
        head.nxt = head.nxt.nxt;
        head.nxt.pre = head;
        size -= 1;
        return ret;
    }

    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        T ret = tail.pre.item;
        tail.pre = tail.pre.pre;
        tail.pre.nxt = tail;
        size -= 1;
        return ret;
    }

    public T get(int index) {
        Node<T> p = head.nxt;
        for (int i = 0;p != tail && i < index; i++) {
            p = p.nxt;
        }
        if (p==tail) {
            return null;
        }
        return p.item;
    }

    private T dfs(int index, Node<T> p) {
        if (index==0 && p!=tail) {
            return p.item;
        }
        else if (p==tail) {
            return null;
        }
        else {
            return dfs(index-1, p.nxt);
        }
    }
    public T getRecursive(int index) {
        return dfs(index, head.nxt);
    }

    public int size() {
        return size;
    }
    // TODO: implements method of iteration
}
