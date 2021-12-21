package com.Introduction;

/**
 * This version of the program uses Synchronization to prevent race conditions.
 *
 * In this program, instead of all the threads counting the number of primes in a specified range of values,
 * The problem is divided among several threads. Each thread counts the primes in a specified range. After
 * a thread is done, the number of primes it has counted is added to a variable, total, that keeps track of the
 * total number of primes that have been counted. Total is a shared resource so special care is taken to prevent
 * race conditions.
 */
import java.util.Scanner;

public class CountPrimesVersion2 {

    private static int total;
    private static Scanner standardInput;
    private static int numberOfThreadsToUse;
    private static WorkerThread[] workers;
    private static long startTime;
    private static long endTime;
    private static int startRange = 2;
    private static int endRange = 1000000;

    public static void main( String[] args ) {
        initializeTotal();
        initializeScanner();
        numberOfThreadsToUse = getNumberOfThreadsToUseFromUser();
        startTime = getStartTime();
        createAndStartThreads();
        endTime = getEndTime();
        reportNumberOfPrimesCountedToTheUser();
        reportNumberOfSecondsTaken();
    }

    private static void initializeTotal() {
        total = 0;
    }

    private static void initializeScanner() {
        standardInput = new Scanner( System.in );
    }

    private static int getNumberOfThreadsToUseFromUser() {
        System.out.print( "Enter the number of threads to use ( 1 - 25 ): " );
        int numberOfThreadsToUse = standardInput.nextInt();
        while ( numberOfThreadsToUse < 0 || numberOfThreadsToUse > 25 ) {
            System.out.print( "Please enter a value between 1 and 25: " );
            numberOfThreadsToUse = standardInput.nextInt();
        }
        return numberOfThreadsToUse;
    }

    private static void createAndStartThreads() {
        initializeThreads();
        startThreads();
        waitForThreadsToFinish();
    }

    private static void initializeThreads() {
        workers = new WorkerThread[ numberOfThreadsToUse ];
    }

    private static void startThreads() {
        int intervalForEachThread = ( endRange - startRange ) / numberOfThreadsToUse;
        for ( int i = 0; i < numberOfThreadsToUse; i++ ) {
            int start = startRange + i * intervalForEachThread;
            int end = startRange + ( i + 1 ) * intervalForEachThread - 1;
            if ( i == numberOfThreadsToUse - 1 )
                end = endRange;
            workers[i] = new WorkerThread( start, end );
            workers[i].start();
        }

    }

    private static void waitForThreadsToFinish() {
        for ( int i = 0; i < workers.length; i++ ) {
            while ( workers[i].isAlive() ) {
                try {
                    workers[i].join();
                }
                catch ( InterruptedException e ) {}
            }
        }
    }

    private static long getStartTime() {
        return System.currentTimeMillis();
    }

    private static long getEndTime() {
        return System.currentTimeMillis();
    }

    private static void reportNumberOfSecondsTaken() {
        System.out.println( "Number of seconds taken: " + ( endTime - startTime ) / 1000.0 );
    }

    synchronized private static void addToTotal( int numberOfPrimesCounted ) {
        total += numberOfPrimesCounted;
    }

    private static void reportNumberOfPrimesCountedToTheUser() {
        System.out.println( "Total number of primes counted: " + total );
    }


    private static class WorkerThread extends Thread {

        int lowerLimit;
        int upperLimit;
        int totalPrimesCounted;

        WorkerThread( int lowerLimit, int upperLimit ) {
            this.lowerLimit = lowerLimit;
            this.upperLimit = upperLimit;
            totalPrimesCounted = 0;
        }

        public void run() {
            countPrimes();
        }

        void countPrimes() {
            System.out.println( "Counting threads in range " + lowerLimit + " to " + upperLimit );
            for ( int i = lowerLimit; i <= upperLimit; i++ )
                if ( givenNumberIsPrime( i ) ) {
                    System.out.println(i);
                    totalPrimesCounted++;
                }
            addToTotal( totalPrimesCounted );
        }

        /**
         * Use the basic very inefficient method.
         */
        boolean givenNumberIsPrime( int number ) {
            for ( int divisor = 2; divisor <= number / 2; divisor++ )
                if ( number % divisor == 0 )
                    return false;
            return true;
        }
    }
}
