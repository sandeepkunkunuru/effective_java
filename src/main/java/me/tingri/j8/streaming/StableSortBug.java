package me.tingri.j8.streaming;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Copied from
 *
 * http://stackoverflow.com/questions/30406281/encounter-order-wrong-when-sorting-a-parallel-stream?rq=1
 * https://bugs.openjdk.java.net/browse/JDK-8076446
 *
 * Created by sandeep on 4/23/16.
 */
public class StableSortBug {
    static final int SIZE = 50_000;

    static class Record implements Comparable<Record> {
        final int sortVal;
        final int seqNum;

        Record(int i1, int i2) { sortVal = i1; seqNum = i2; }

        @Override
        public int compareTo(Record other) {
            return Integer.compare(this.sortVal, other.sortVal);
        }
    }

    static Record[] genArray() {
        Record[] array = new Record[SIZE];
        Arrays.setAll(array, i -> new Record(i / 10_000, i));
        return array;
    }

    static boolean verify(Record[] array) {
        return IntStream.range(1, array.length)
                .allMatch(i -> array[i-1].seqNum + 1 == array[i].seqNum);
    }

    public static void main(String[] args) {
        Record[] array = genArray();
        System.out.println(SIZE);
        System.out.println(verify(array));
        Arrays.sort(array);
        System.out.println(verify(array));
        Arrays.parallelSort(array);
        System.out.println(verify(array));
    }
}