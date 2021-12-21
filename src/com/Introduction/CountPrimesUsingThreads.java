package com.Introduction;

/**
 * This program uses several threads specified by the user to count the number of integers less than 5000000
 * that are prime. The number of threads to be used are specified by the user but is between 1 - 25. The time taken
 * to do the calculations is also displayed.
 */

import java.util.Scanner;

public class CountPrimesUsingThreads {

    private static Scanner stdin;
    private static int numberOfThreadsToUse;

    public static void main( String[] args ) {
        stdin = new Scanner( System.in );
        numberOfThreadsToUse = getNumberOfThreadsToUseFromUser();
        createAndStartThreads();
    }

    private static int getNumberOfThreadsToUseFromUser() {
        System.out.print( "Please enter the number of threads to be used ( 1 - 25 ): " );
        int numberOfThreadsToUse = stdin.nextInt();
        while ( !validateNumberOfThreads( numberOfThreadsToUse ) ) {
            System.out.print( "Please enter a valid number of threads ( 1 - 25 ): " );
            numberOfThreadsToUse = stdin.nextInt();
        }
        return numberOfThreadsToUse;
    }

    private static boolean validateNumberOfThreads( int numberOfThreads ) {
        if ( numberOfThreads < 1 || numberOfThreads > 25 )
            return false;
        return true;
    }

    private static void createAndStartThreads() {
        for ( int i = 0; i < numberOfThreadsToUse; i++ ) {
            CountPrimesThread thread = new CountPrimesThread( i );
            thread.start();
        }
    }


    /**
     * This nested class represents a Thread that counts primes in the range 3000000 - 6000000.
     */
    private static class CountPrimesThread extends Thread {

        int threadId;
        int lowerLimit;
        int upperLimit;

        CountPrimesThread( int threadId ) {
            this.threadId = threadId + 1;
            lowerLimit = 10;
            upperLimit = 100;
        }

        public void run() {
            long startTime = System.currentTimeMillis();
            int numberOfPrimes = countPrimes();
            long endTime = System.currentTimeMillis();
            System.out.println( "Thread " + this.threadId + " counted " + numberOfPrimes + " primes in " +
                    ( endTime - startTime ) / 1000 + " seconds." );
        }

        int countPrimes() {
            int count = 0;
            for ( int i = lowerLimit; i <= upperLimit; i++ )
                if ( givenNumberIsPrime( i ) )
                    count++;
            return count;
        }

        boolean givenNumberIsPrime( int number ) {
            int upperLimit = ( int ) Math.sqrt( number );
            for ( int divisor = 2; divisor <= upperLimit; divisor++ )
                if ( number % divisor == 0 )
                    return false;
            return true;
        }
    }
}
