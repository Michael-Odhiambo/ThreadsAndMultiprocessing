package Introduction;

public class QuickSort {

    private static int[] arrayToSort;

    public static void main( String[] args ) {
        fillArray();
        quickSort( 0, arrayToSort.length - 1 );

        for ( int i = 0; i < arrayToSort.length; i++ )
            System.out.print( arrayToSort[i] + "," );
    }

    private static void fillArray() {
        arrayToSort = new int[ 100 ];
        for ( int i = 0; i < arrayToSort.length; i++ ) {
            arrayToSort[i] = ( int ) ( Math.random() * 100 ) + 1;
        }
    }

    private static void quickSort( int low, int high ) {
        if ( high > low ) {
            int pivot = getThePivot( low, high );
            quickSort( low, pivot - 1 );
            quickSort( pivot + 1, high );
        }
    }

    private static int getThePivot( int low, int high ) {
        int pivot = arrayToSort[low];

        while ( high > low ) {
            while ( high > low && arrayToSort[high] > pivot )
                high--;

            if ( high == low )
                break;

            arrayToSort[ low ] = arrayToSort[ high ];

            while ( high > low && arrayToSort[low] < pivot )
                low++;

            if ( high == low )
                break;

            arrayToSort[ high ] = arrayToSort[ low ];
        }

        arrayToSort[ low ] = pivot;
        return low;
    }
}
