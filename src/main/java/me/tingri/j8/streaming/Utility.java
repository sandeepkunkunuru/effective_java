package me.tingri.j8.streaming;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by sandeep on 4/23/16.
 */
public class Utility {

    static boolean parseArgs(Object o, String[] args) {
        CmdLineParser cmdParser = new CmdLineParser(o);

        try {
            // parse the options.
            cmdParser.parseArgument(args);
        } catch (CmdLineException e) {
            cmdParser.printUsage(System.err);
            System.err.println();
            return false;
        }

        return true;
    }

    static List<Transaction> genRandomArray(int numOfProducts, int maxSales) {
        Random rnd = new Random();

        List<Transaction> num = new ArrayList<>();

        Month[] months = Month.values();

        int numOfRecords = numOfProducts * months.length;

        for (int j = 0; j < numOfRecords; j++)
            num.add(new Transaction(rnd.nextInt(numOfProducts), months[rnd.nextInt(months.length)], rnd.nextInt(maxSales)));

        return num;
    }
}
