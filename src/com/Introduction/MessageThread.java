package com.Introduction;

/**
 * This program demonstrates the use of threads. It contains a nested class that represents a thread
 * that prints a message to standard output.
 */

import java.util.Scanner;

public class MessageThread {

    private static String messageToOutput;
    private static Scanner standardInput;
    private static NamedThread outputThread;

    public static void main( String[] args ) {
        standardInput = new Scanner( System.in );
        System.out.print( "Enter message to output: " );
        messageToOutput = standardInput.nextLine();

        outputThread = new NamedThread( messageToOutput );
        outputThread.start();
        System.out.println( "Thread has been started" );
    }

    private static class NamedThread extends Thread {

        private String messageToOutput;

        NamedThread( String message ) {
            messageToOutput = message;
        }

        public void run() {
            System.out.println( "Message is: " + messageToOutput );
        }
    }
}
