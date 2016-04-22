package me.tingri.j8.streaming;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

/**
 * Created by sandeep on 4/22/16.
 */
public class Amdahl {
    private static Random rnd = new Random();

    public static void main(String[] s) {
        List<Transaction> num = genRandomArray(100000, 10000);
        List<Double> timings1 = new ArrayList<>();
        List<Double> timings2 = new ArrayList<>();
        List<Double> timings3 = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            List<Transaction> sorted1 = TimeIt.time(() -> streamAndSort(num), timings1);
            List<Transaction> sorted2 = TimeIt.time(() -> parallelStreamAndSort(num), timings2);
            List<Transaction> sorted3 = TimeIt.time(() -> fullParallelStreamSort(num), timings3);


            assert isSorted(sorted1) &&  isSorted(sorted2) && isSorted(sorted3);
        }

        amdahls(timings1, timings2, 0.75, 2);
        amdahls(timings1, timings3, 0.9, 2);
    }

    private static void amdahls(List<Double> timings1, List<Double> timings3, double percentage, int cores) {
        double avgSpeedup = 0;

        for (int i = 0; i < timings1.size(); i++) avgSpeedup += (timings1.get(i)) / timings3.get(i);

        avgSpeedup /= timings1.size();

        // Assuming 75% of the execution time may be the subject of a speedup, p will be 0.75; if the improvement
        // makes the affected part twice faster(based on number of cores), s will be say 4.
        // Amdahl's law states that the overall speedup of applying the improvement will be
        // 1/(1 - p + p/s) i.e (1d/(1 - 0.75 + 0.75/2))
        double amdahlsSpeedup = (1d/(1 - percentage + percentage/cores));

        //variance from theoretical result is bounded
        System.out.println(" Average Speedup = " + avgSpeedup + " Amdahls Speedup = " + amdahlsSpeedup);

        assert Math.abs(avgSpeedup - amdahlsSpeedup) < 0.5 ;
    }

    private static boolean isSorted(List<Transaction> sorted1) {
        int sales = Integer.MAX_VALUE;

        for (Transaction t : sorted1)
            if (sales < t.sales) return false;
            else sales = t.sales;

        return true;
    }

    private static List<Transaction> streamAndSort(List<Transaction> num) {
        List<Transaction> list = num.stream().
                filter(t -> t.month == Month.JAN).
                collect(toList());

        Collections.sort(list, comparing(Transaction::sales).reversed());

        return list ;
    }

    private static List<Transaction> parallelStreamAndSort(List<Transaction> num) {
        List<Transaction> list = num.parallelStream().
                filter(t -> t.month == Month.JAN).
                collect(toList());

        Collections.sort(list, comparing(Transaction::sales).reversed());

        return list ;
    }

    private static List<Transaction> fullParallelStreamSort(List<Transaction> num) {
        return num.parallelStream().
                filter(t -> t.month == Month.JAN).
                sorted(comparing(Transaction::sales).reversed()).
                collect(toList());
    }

    private static List<Transaction> genRandomArray(int size, int numOfProducts) {
        List<Transaction> num = new ArrayList<>();

        Month[] months = Month.values();

        for (int j = 0; j < size; j++)
            num.add(new Transaction(rnd.nextInt(numOfProducts), months[rnd.nextInt(months.length)], rnd.nextInt(size)));

        return num;
    }


    private enum Month {
        JAN, FEB, MAR, APR, MAY, JUN, JUL, AUG, SEP, OCT, NOV, DEC
    }

    private static class TimeIt {

        static <T> T time(Callable<T> task, List<Double> timings) {
            T call = null;
            try {
                long startTime = System.currentTimeMillis();
                call = task.call();
                timings.add((System.currentTimeMillis() - startTime) / 1000d);
            } catch (Exception e) {
                //...
            }
            return call;
        }
    }

    private static class Transaction {
        int productId;
        int sales;
        Month month;

        Transaction(int productId, Month month, int sales) {
            this.sales = sales;
            this.productId = productId;
            this.month = month;
        }

        Integer sales() {
            return sales;
        }

        public String toString() {
            return (this.productId + "---" + this.month.toString() + "----" + this.sales);
        }
    }
}
