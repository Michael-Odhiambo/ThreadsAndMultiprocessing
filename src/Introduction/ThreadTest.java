package Introduction;

import java.util.Scanner;

public class ThreadTest {

    private static Scanner standardInput;
    private static int numberOfThreads;

    public static void main(String[] args) {
        createScanner();
        numberOfThreads = getNumberOfThreads();
        System.out.println( "\nCreating " + numberOfThreads
                + " prime-counting threads..." );
        createThreadsAndCountPrimes();
    }

    private static void createScanner() {
        standardInput = new Scanner( System.in );
    }

    private static int getNumberOfThreads() {
        int numberOfThreads = 0;

        while (numberOfThreads < 1 || numberOfThreads > 25) {
            System.out.print( "How many threads do you want to use (1 to 25) ? " );
            numberOfThreads = standardInput.nextInt();
            if (numberOfThreads < 1 || numberOfThreads > 25)
                System.out.println( "Please enter a number between 1 and 25 !" );
        }
        return numberOfThreads;
    }

    private static void createThreadsAndCountPrimes() {
        CountPrimesThread[] worker = new CountPrimesThread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++)
            worker[i] = new CountPrimesThread( i );
        for (int i = 0; i < numberOfThreads; i++)
            worker[i].start();
        System.out.println( "Threads have been created and started." );
    }

    private static class CountPrimesThread extends Thread {
        private int threadId;

        CountPrimesThread( int threadId ) {
            this.threadId = threadId;
        }

        public void run() {
            long startTime = System.currentTimeMillis();
            int count = countPrimesInTheRange( 2, 5000000 ); // Counts the primes.
            long elapsedTime = System.currentTimeMillis() - startTime;
            System.out.println( "Thread " + threadId + " counted " +
                    count + " primes in " + ( elapsedTime/1000.0 ) + " seconds." );
        }

        private static int countPrimesInTheRange( int min, int max ) {
            int count = 0;
            for ( int i = min; i <= max; i++ )
                if ( givenNumberIsPrime( i ) )
                    count++;
            return count;
        }

        private static boolean givenNumberIsPrime( int x ) {
            assert x > 1;
            int top = ( int )Math.sqrt( x );
            for ( int i = 2; i <= top; i++ )
                if ( x % i == 0 )
                    return false;
            return true;
        }
    }
}
