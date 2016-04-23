package me.tingri.j8.streaming;

import org.kohsuke.args4j.Option;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Comparator.comparing;
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

        SpeculateP speculateP = new SpeculateP(this);

        List<Double> timings1 = new ArrayList<>();
        List<Double> timings2 = new ArrayList<>();
        List<Double> timings3 = new ArrayList<>();
        List<Double> timings4 = new ArrayList<>();

        for (int i = 0; i < numOfTrials; i++) {
            List<Transaction> num = Utility.genRandomArray(numOfProducts, maxSales);

            List<Transaction> sorted1 = TimeIt.time(() -> streamAndSort(num, Month.JAN), timings1);
            List<Transaction> sorted2 = TimeIt.time(() -> parallelStreamAndSort(num, Month.JAN), timings2);
            List<Transaction> sorted3 = TimeIt.time(() -> fullStreamSort(num, Month.JAN), timings3);
            List<Transaction> sorted4 = TimeIt.time(() -> fullParallelStreamSort(num, Month.JAN), timings4);

//            if(i == 0) {
//                //num.stream().forEach(System.out::println);
//                sorted3.stream().forEach(System.out::println);
//            }

            assert isSorted(sorted1) &&  isSorted(sorted2) && isSorted(sorted3) && isSorted(sorted4);
        }

        double p[] = speculateP.speculate(Month.JAN);

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
        double amdahlsSpeedup = (1d/(1 - percentage + percentage/cores));

        //variance from theoretical result is bounded
        System.out.println(" Average Speedup = " + avgSpeedup + " Amdahls percentage = " + percentage + " and Speedup = " + amdahlsSpeedup);

        assert Math.abs(avgSpeedup - amdahlsSpeedup) < 0.5 ;
    }

    private boolean isSorted(List<Transaction> sorted1) {
        int sales = Integer.MAX_VALUE;

        for (Transaction t : sorted1)
            if (sales < t.sales) return false;
            else sales = t.sales;

        return true;
    }

    private List<Transaction> streamAndSort(List<Transaction> num, Month month) {
        List<Transaction> list = num.stream().
                filter(t -> t.month == month).
                collect(toList());

        Collections.sort(list, comparing(Transaction::sales).reversed());

        return list ;
    }

    private List<Transaction> parallelStreamAndSort(List<Transaction> num, Month month) {
        List<Transaction> list = num.parallelStream().
                filter(t -> t.month == month).
                collect(toList());

        Collections.sort(list, comparing(Transaction::sales).reversed());

        return list ;
    }

    private List<Transaction> fullStreamSort(List<Transaction> num, Month month) {
        return num.stream().
                filter(t -> t.month == month).
                sorted(comparing(Transaction::sales).reversed()).
                collect(toList());
    }

    private List<Transaction> fullParallelStreamSort(List<Transaction> num, Month month) {
        return num.parallelStream().
                filter(t -> t.month == month).
                sorted(comparing(Transaction::sales).reversed()).
                collect(toList());
    }


    int numOfProducts() {
        return numOfProducts;
    }

    int maxSales() {
        return maxSales;
    }

    int numOfTrials() {
        return numOfTrials;
    }
}
