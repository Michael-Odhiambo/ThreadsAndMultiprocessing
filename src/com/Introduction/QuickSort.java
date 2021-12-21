package com.Introduction;

// Meant for testing.

public class QuickSort {

    private static int[] arrayToSort;

    public static void main( String[] args ) {
        fillArrayToSort();
        printArrayElements();
        sortTheArray();
        printArrayElements();
    }

    private static void fillArrayToSort() {
        arrayToSort = new int[ 100 ];
        for ( int i = 0; i < 100; i++ )
            arrayToSort[i] = ( int )( Math.random() * 100 ) + 1;
    }

    private static void printArrayElements() {
        for ( int i = 0; i < 100; i++ )
            if ( i == 99 )
                System.out.print( arrayToSort[i] );
            else
                System.out.print( arrayToSort[i] + "," );
        System.out.println();
    }

    private static void sortTheArray() {
        quickSort( 0, arrayToSort.length - 1 );
    }

    private static void quickSort( int startPosition, int endPosition ) {
        if ( endPosition > startPosition ) {
            int pivotElementPosition = quickSortStep( startPosition, endPosition );
            quickSort( startPosition, pivotElementPosition );
            quickSort( pivotElementPosition + 1, endPosition );
        }
    }

    private static int quickSortStep( int startPosition, int endPosition ) {
        int pivotElement = arrayToSort[ startPosition ];

        while ( endPosition > startPosition ) {
            while ( endPosition > startPosition && arrayToSort[ endPosition ] >= pivotElement )
                endPosition--;

            if ( endPosition == startPosition )
                break;

            arrayToSort[ startPosition ] = arrayToSort[ endPosition ];
            startPosition++;

            while ( endPosition > startPosition && arrayToSort[ startPosition ] <= pivotElement )
                startPosition++;

            if ( endPosition == startPosition )
                break;

            arrayToSort[ endPosition ] = arrayToSort[ startPosition ];
            endPosition--;
        }

        arrayToSort[ startPosition ] = pivotElement;

        return startPosition;
    }
}
