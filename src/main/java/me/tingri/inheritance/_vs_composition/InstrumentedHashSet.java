package me.tingri.inheritance._vs_composition;// Broken - Inappropriate use of inheritance!

import java.util.*;

public class InstrumentedHashSet<E> extends HashSet<E> {
    // The number of attempted element insertions
    private int addCount = 0;

    public InstrumentedHashSet() {
    }

    public InstrumentedHashSet(int initCap, float loadFactor) {
        super(initCap, loadFactor);
    }

    @Override public boolean add(E e) {
        addCount++;
        return super.add(e);
    }

    @Override public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return super.addAll(c);
    }

    public int getAddCount() {
        return addCount;
    }

    public static void main(String[] args) {
        InstrumentedHashSet<String> s =
            new InstrumentedHashSet<String>();
        s.addAll(Arrays.asList("Snap", "Crackle", "Pop"));
        //We would expect the getAddCount method to return three at this point, but it
        //returns six. What went wrong? Internally, HashSet ’s addAll method is implemented on top of its add method,
        // although HashSet , quite reasonably, does not document this implementation detail.
        System.out.println(s.getAddCount());
    }
}
