package com.Introduction;

/**
 * This program uses threads to generate the number of primes in a specified range. The task is divided into several
 * tasks which are then submitted to the ExecutorService for execution. The algorithm used to generate the primes is
 * the Sieve of Eratosthenes. Given an array of integers starting at 2; Find the first uncrossed integer, and cross out
 * all its multiples. Repeat until there are no more multiples in the array.
 */

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.ArrayList;

public class PrimeGenerator {

    private static ExecutorService executorService;
    private static ArrayList< Future< Integer > > results;

    private static int MINIMUM_VALUE = 10;
    private static int MAXIMUM_VALUE = 1000000;

    public static void main( String[] args ) {
        setupExecutorService();
        submitTasksToTheExecutorService();
        getResults();
        executorService.shutdown();

    }

    private static void setupExecutorService() {
        executorService = Executors.newFixedThreadPool( Runtime.getRuntime().availableProcessors() );
    }

    private static void submitTasksToTheExecutorService() {
        int numberOfSubtasks = ( MAXIMUM_VALUE - MINIMUM_VALUE ) / 1000;
        results = new ArrayList<>();
        for ( int i = 0; i < numberOfSubtasks; i++ ) {
            int startRange = MINIMUM_VALUE + i * numberOfSubtasks;
            int endRange = startRange + numberOfSubtasks;
            Future< Integer > result = executorService.submit( new CountPrimesTask( startRange, endRange ) );
            results.add( result );
        }
    }

    private static void getResults() {
        for ( Future< Integer > result : results ) {
            try {
                result.get();
            }
            catch ( Exception e ) {}
        }
    }

    private static class CountPrimesTask implements Callable< Integer > {

        private int minimum;
        private int maximum;

        CountPrimesTask( int minimum, int maximum ) {
            this.minimum = minimum;
            if ( this.minimum < 2 )
                this.minimum = 2;
            this.maximum = maximum;
        }

        public Integer call() {
            System.out.println( " ------------------------------------------------- " );
            int numberOfPrimes = getNumberOfPrimes( this.minimum, this.maximum );
            System.out.println( "   " + this.minimum + "  -  " + this.maximum + "    " + numberOfPrimes );
            return numberOfPrimes;
        }

    }

    private static int getNumberOfPrimes( int minimumValue, int maximumValue ) {
        int count = 0;
        for ( int i = minimumValue; i <= maximumValue; i++ )
            if ( isPrime( i ) )
                count++;
        return count;
    }

    private static boolean isPrime( int number ) {
        int upperLimit = ( int ) Math.sqrt( number );
        for ( int i = 2; i <= upperLimit; i++ )
            if ( number % i == 0 )
                return false;
        return true;
    }


}
