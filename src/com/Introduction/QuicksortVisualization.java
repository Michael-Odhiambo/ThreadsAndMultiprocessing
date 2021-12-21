package com.Introduction;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.application.Platform;

public class QuicksortVisualization extends Application {

    private Stage mainWindow;
    private Button startButton;
    private Canvas canvas;
    private GraphicsContext drawingArea;
    private Color[] colorPalette;
    private int[] hueArray;
    private final int ARRAY_SIZE = 100;
    private volatile boolean runningStatus = false;
    private AnimationThread animationThread;

    public static void main( String[] args ) {
        launch( args );
    }

    public void start( Stage stage ) {
        fillHueArray();
        createColorPalette();
        setUpMainWindow( stage );
        showMainWindow();
    }

    private void fillHueArray() {
        hueArray = new int[ ARRAY_SIZE ];
        for ( int i = 0; i < ARRAY_SIZE; i++ )
            hueArray[i] = i;
    }

    private void createColorPalette() {
        colorPalette = new Color[ ARRAY_SIZE ];
        for ( int i = 0; i < ARRAY_SIZE; i++ )
            colorPalette[i] = Color.hsb( ( 310.0*i ) / ARRAY_SIZE, 1, 1 );
    }

    private void setUpMainWindow( Stage stage ) {
        mainWindow = stage;
        setUpScene();
    }

    private void setUpScene() {
        Scene scene = new Scene( createBorderPane() );
        mainWindow.setScene( scene );
    }

    private BorderPane createBorderPane() {
        BorderPane root = new BorderPane( createCanvas() );
        root.setBottom( createButtonHolder() );
        return root;
    }

    private Canvas createCanvas() {
        canvas = new Canvas( 500, 250 );
        drawingArea = canvas.getGraphicsContext2D();
        drawColorPaletteInSortedOrder();
        return canvas;
    }

    private void fillDrawingAreaWithWhite() {
        drawingArea.setFill( Color.WHITE );
        drawingArea.fillRect( 0, 0, canvas.getWidth(), canvas.getHeight() );
    }

    private HBox createButtonHolder() {
        HBox buttonHolder = new HBox( createStartButton() );
        buttonHolder.setStyle( "-fx-padding: 6px" );
        buttonHolder.setAlignment( Pos.CENTER );
        return buttonHolder;
    }

    private Button createStartButton() {
        startButton = new Button( "Start" );
        startButton.setOnAction( event -> startOrStopAnimation() );
        return startButton;
    }

    public void showMainWindow() {
        mainWindow.setTitle( "Quicksort Visualization" );
        mainWindow.show();
    }

    private void drawColorPaletteInSortedOrder() {
        fillDrawingAreaWithWhite();
        for ( int i = 0; i < ARRAY_SIZE; i++ ) {
            drawBar( i, colorPalette[i] );
        }
    }

    private void drawBar( int indexOfBar, Color barColor ) {
        drawingArea.setFill( barColor );
        drawingArea.fillRect( indexOfBar * 5, 0, 5, 250 );
    }

    private void startOrStopAnimation() {
        if ( !runningStatus ) {
            animationThread = new AnimationThread();
            startButton.setText( "Stop" );
            runningStatus = true;
            animationThread.start();
        }
        else {
            runningStatus = false;
            animationThread.interrupt(); // Wake up the animation thread in case it was sleeping, so that it can see
                                         // the new value of runningStatus.
        }
    }

    private void setHue( int index, int colorNumber ) {
        hueArray[ index ] = colorNumber;
        Platform.runLater( () -> {
            if ( colorNumber == -1 )
                drawBar( index, Color.BLACK );
            else
                drawBar( index, colorPalette[ colorNumber ] );
        } );
    }

    private void randomizeTheHueArray() {
        for ( int i = hueArray.length - 1; i > 0; i-- ) {
            int r = ( int )( Math.random() * i ) + 1;
            int temp = hueArray[r];
            hueArray[ r ] = hueArray[ i ];
            setHue( i , temp );
        }
    }

    private void quickSort( int startPosition, int endPosition ) throws Exception {
        if ( endPosition > startPosition ) {
            int pivotElementPosition = quickSortStep( startPosition, endPosition );
            quickSort( startPosition, pivotElementPosition );
            quickSort( pivotElementPosition + 1, endPosition );
        }
    }

    private int quickSortStep( int startPosition, int endPosition ) throws Exception {
        int pivotElement = hueArray[ startPosition ];
        setHue( startPosition, -1 );  // Mark position as empty.

        while ( endPosition > startPosition ) {
            while ( endPosition > startPosition && hueArray[ endPosition ] >= pivotElement )
                endPosition--;

            if ( endPosition == startPosition )
                break;

            setHue( startPosition, hueArray[ endPosition ] );  // Move hueArray[ endPosition ] into empty slot.
            setHue( endPosition, -1 );  // Mark hueArray[ endPosition ] as empty.
            startPosition++;
            delay( 100 );

            while ( endPosition > startPosition && hueArray[ startPosition ] <= pivotElement )
                startPosition++;

            if ( endPosition == startPosition )
                break;

            hueArray[ endPosition ] = hueArray[ startPosition ];
            setHue( endPosition, hueArray[ startPosition ] );  // Move hueArray[ startPosition ] into empty slot.
            setHue( startPosition, -1 );  // Mark hueArray[ startPosition ] as empty.
            endPosition--;
            delay( 100 );
        }
        setHue( startPosition, pivotElement );
        delay( 100 );
        return startPosition;
    }

    private void delay( int milliseconds ) throws Exception {
        if ( !runningStatus )
            throw new ThreadTerminationException();
        try {
            Thread.sleep( milliseconds );
        }
        catch ( InterruptedException e ) {}
        if ( !runningStatus )
            throw new ThreadTerminationException();  // Check running status in case it changed while the thread was
                                                     // asleep.
    }

    private class AnimationThread extends Thread {

        AnimationThread() {
            try {
                setDaemon( true );
            }
            catch ( SecurityException e ) {
                System.out.println( "Cannot modify daemon status of the animation thread." );
            }
        }

        public void run() {
            randomizeTheHueArray();
            try {
                delay( 1000 );
                quickSort( 0, hueArray.length - 1 );
            }
            catch ( Exception e ) {
                Platform.runLater( () -> drawColorPaletteInSortedOrder() );
            }
            finally {  // This is only necessary if the thread terminated normally.
                animationThread = null;
                runningStatus = false;
                Platform.runLater( () -> startButton.setText( "Start" ) );
            }
        }
    }

    private class ThreadTerminationException extends Exception {}
}
