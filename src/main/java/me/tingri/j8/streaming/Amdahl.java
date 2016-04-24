package me.tingri.j8.streaming;

import org.kohsuke.args4j.Option;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.summingDouble;
import static java.util.stream.Collectors.toList;

/**
 * Created by sandeep on 4/22/16.
 */
public class Amdahl {

    @Option(name = "-numOfProducts", usage = "Sets a number of products")
    private int numOfProducts;

    @Option(name = "-maxSales", usage = "Largest estimated sales")
    private int maxSales;

    @Option(name = "-numOfTrials", usage = "Number of trials")
    private int numOfTrials;

    @Option(name = "-numOfCores", usage = "Number of cores")
    private int numOfCores;

    public static void main(String[] args) {
        new Amdahl().run(args);
    }

    void run(String[] args) {
        if (!Utility.parseArgs(this, args)) return;

        Month month = Month.JAN;
        Random rnd = new Random();

        List<Double> timings1 = new ArrayList<>();
        List<Double> timings2 = new ArrayList<>();
        List<Double> timings3 = new ArrayList<>();
        List<Double> timings4 = new ArrayList<>();
        List<Double> filterAndListTimes = new ArrayList<>();

        for (int i = 0; i < numOfTrials; i++) {
            List<Transaction> num = Utility.genRandomArray(rnd, numOfProducts, maxSales);

            //Paradigm 1 : Anonymous Function class instance
            List<Transaction> sorted1 = streamAndSort(month, timings1, num);

            //Paradigm 2 : Normal methods passed for Function
            List<Transaction> sorted2 = TimeIt.time(() -> parallelStreamAndSort(num, month), timings2);
            List<Transaction> sorted3 = TimeIt.time(() -> fullStream(num, month), timings3);
            List<Transaction> sorted4 = TimeIt.time(() -> fullParallelStream(num, month), timings4);

            //Paradigm 3 : Lambda expressions for anonymous Function class
            filterAndList(month, filterAndListTimes, num);

            assert isSorted(sorted1) && isSorted(sorted2) && isSorted(sorted3) && isSorted(sorted4);
        }

        double[] p = speculate(timings1, timings3, filterAndListTimes);

        amdahls(timings1, timings2, p[0], numOfCores); // I have 2 cores
        amdahls(timings3, timings4, p[1], numOfCores);
    }

    private void amdahls(List<Double> timings1, List<Double> timings2, double percentage, int cores) {
        double avgSpeedup = 0;

        for (int i = 0; i < timings1.size(); i++) avgSpeedup += (timings1.get(i)) / timings2.get(i);

        avgSpeedup /= timings1.size();

        // Assuming 75% of the execution time may be the subject of a speedup, p will be 0.75; if the improvement
        // makes the affected part twice faster(based on number of cores), s will be say 4.
        // Amdahl's law states that the overall speedup of applying the improvement will be
        // 1/(1 - p + p/s) i.e (1d/(1 - 0.75 + 0.75/2))
        double amdahlsSpeedup = (1d / (1 - percentage + percentage / cores));

        //variance from theoretical result is bounded
        System.out.println(" Average Speedup = " + avgSpeedup + " Amdahls percentage = " + percentage + " and Speedup = " + amdahlsSpeedup);

        assert Math.abs(avgSpeedup - amdahlsSpeedup) < 1;
    }

    private double[] speculate(List<Double> timings1, List<Double> timings2, List<Double> timings3) {
        double filterListAndSortTime = timings1.stream().collect(summingDouble(n -> n)) / numOfTrials;
        double fullStreamTime = timings2.stream().collect(summingDouble(n -> n)) / numOfTrials;
        double filterAndListTime = timings3.stream().collect(summingDouble(n -> n)) / numOfTrials;

        //first one is almost accurate, second one is a bad approximation
        // new double[]{filterAndListTime/filterListAndSortTime, filterAndListTime/fullStreamTime};
        return new double[]{filterAndListTime / filterListAndSortTime, fullStreamTime / filterListAndSortTime};
    }

    /**
     * this is still trying too many things at the same time next you can use a normal function
     * like parallelStreamAndSort.
     *
     * @param month
     * @param timings2
     * @param num
     * @return
     */
    private List<Transaction> streamAndSort(final Month month, List<Double> timings2, final List<Transaction> num) {
        return TimeIt.time(() -> new Function() {
            @Override
            public List<Transaction> apply(Object o) {
                List<Transaction> list = num.stream().filter(t -> t.month == month).
                        collect(Collectors.toList());

                Collections.sort(list, Comparator.comparing(Transaction::sales).reversed());

                return list;
            }
        }.apply(null), timings2);
    }

    private List<Transaction> parallelStreamAndSort(List<Transaction> num, Month month) {
        List<Transaction> list = num.parallelStream().
                filter(t -> t.month == month).
                collect(toList());

        Collections.sort(list, comparing(Transaction::sales).reversed());

        return list;
    }

    private List<Transaction> fullStream(List<Transaction> num, Month month) {
        return num.stream().
                filter(t -> t.month == month).
                sorted(comparing(Transaction::sales).reversed()).
                collect(toList());
    }

    /**
     * Dont'  try this if stability is a requirement since parllel sorting is unstable.
     *
     * Still serves for the purposes of this program which is to demonstrate
     * streams, parallel execution and Amdahl's observation
     *
     * http://stackoverflow.com/questions/30406281/encounter-order-wrong-when-sorting-a-parallel-stream?rq=1
     * https://bugs.openjdk.java.net/browse/JDK-8076446
     *
     * @param num
     * @param month
     * @return
     */
    private List<Transaction> fullParallelStream(List<Transaction> num, Month month) {
        return num.parallelStream().
                filter(t -> t.month == month).
                sorted(comparing(Transaction::sales).reversed()).
                collect(toList());
    }

    /**
     * Just for kicks another level of Lambda expressions you can try anonymous Function classes like streamAndSort
     *
     * @param month
     * @param timings1
     * @param num
     */
    private void filterAndList(Month month, List<Double> timings1, List<Transaction> num) {
        TimeIt.time(() -> ((Function) o -> num.stream().filter(t -> t.month == month).
                collect(Collectors.toList())).apply(null), timings1);
    }

    private boolean isSorted(List<Transaction> sorted1) {
        int sales = Integer.MAX_VALUE;

        for (Transaction t : sorted1)
            if (sales < t.sales) return false;
            else sales = t.sales;

        return true;
    }
}
