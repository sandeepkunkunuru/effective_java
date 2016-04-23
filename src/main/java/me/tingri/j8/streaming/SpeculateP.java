package me.tingri.j8.streaming;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.summingDouble;
import static java.util.stream.Collectors.toList;

class SpeculateP {
    private final int numOfTrials;
    private final int numOfProducts;
    private final int maxSales;

    SpeculateP(Amdahl amdahl) {
        this.numOfTrials = amdahl.numOfTrials();
        this.numOfProducts = amdahl.numOfProducts();
        this.maxSales = amdahl.maxSales();
    }

    double[] speculate(Month month) {
        List<Double> timings2 = new ArrayList<>();
        List<Double> timings3 = new ArrayList<>();
        List<Double> timings4 = new ArrayList<>();

        for (int i = 0; i < numOfTrials; i++) {
            List<Transaction> num = Utility.genRandomArray(numOfProducts, maxSales);

            //Just for kicks another level of Lambda expressions next one is simpler refer that to understand this.
            TimeIt.time(() -> ((Function) o -> num.stream().filter(t -> t.month == month).
                    collect(Collectors.toList())).apply(null), timings2);

            TimeIt.time(() -> new Function() {
                @Override
                public Object apply(Object o) {
                    List<Transaction> list = num.stream().filter(t -> t.month == month).
                            collect(Collectors.toList());

                    Collections.sort(list, Comparator.comparing(Transaction::sales).reversed());

                    return list;
                }
            }.apply(null), timings3);

            TimeIt.time(() -> new Function() {
                @Override
                public Object apply(Object o) {
                    return num.stream().
                            filter(t -> t.month == month).
                            sorted(comparing(Transaction::sales).reversed()).
                            collect(toList());
                }
            }.apply(null), timings4);
        }

        double filterAndListTime = timings2.stream().collect(summingDouble(n -> n))/numOfTrials;
        double filterListAndSortTime = timings3.stream().collect(summingDouble(n -> n))/numOfTrials;
        double fullStreamTime = timings4.stream().collect(summingDouble(n -> n))/numOfTrials;

        //first one is almost accurate, second one is bad approximation
        //return new double[]{filterAndListTime/filterListAndSortTime, filterAndListTime/fullStreamTime};
        return new double[]{filterAndListTime/filterListAndSortTime, fullStreamTime/filterListAndSortTime};
    }
}