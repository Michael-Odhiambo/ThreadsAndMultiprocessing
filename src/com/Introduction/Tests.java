package com.Introduction;

public class Tests {


    enum STATUS { POSITIVE, NEGATIVE }

    public static void main( String[] args ) {

        STATUS myStatus = STATUS.POSITIVE;

        System.out.println( myStatus == STATUS.POSITIVE );
    }
}
