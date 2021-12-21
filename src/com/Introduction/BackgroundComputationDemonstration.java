package com.Introduction;

/**
 * This program serves to demonstrate background computation using threads. The program uses a single thread to
 * compute an image in the Mandelbrot set. The thread that does the actual computation runs at a lower priority
 * than the JavaFX application. This makes the program more responsive since events generated will be handled
 * quickly. This leaves all other processing time to the computation thread. After a row of pixels is calculated,
 * the computation thread uses Platform.runLater() to draw the row of pixels on the screen.
 */

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.geometry.Pos;
import javafx.application.Platform;
import javafx.scene.paint.Color;

public class BackgroundComputationDemonstration extends Application {

    private Stage mainWindow;
    private volatile boolean animationIsRunning;
    private ComputationThread workerThread;
    private Canvas canvas;
    private GraphicsContext drawingArea;
    private Button startButton;
    private Color[] colorPalette;

    private int canvasWidth = 700;
    private int canvasHeight = 600;

    public static void main( String[] args ) {
      launch( args );
    }

    public void start( Stage stage ) {
        setupMainWindow( stage );
        fillColorPalette();
        showMainWindow();
    }

    private void setupMainWindow( Stage stage ) {
        mainWindow = stage;
        mainWindow.setScene( setupScene() );
    }

    private Scene setupScene() {
        Scene scene = new Scene( setupBorderPane() );
        return scene;
    }

    private BorderPane setupBorderPane() {
        BorderPane canvasHolder = new BorderPane( setupCanvas() );
        canvasHolder.setBottom( setupButtonBar() );
        return canvasHolder;
    }

    private Canvas setupCanvas() {
        canvas = new Canvas( canvasWidth, canvasHeight );
        drawingArea = canvas.getGraphicsContext2D();
        fillDrawingArea();
        return canvas;
    }

    private HBox setupButtonBar() {
        HBox buttonBar = new HBox( setupStartButton() );
        buttonBar.setAlignment( Pos.CENTER );
        buttonBar.setStyle( "-fx-padding: 6px; -fx-border-color:black; -fx-border-width: 2px" );
        return buttonBar;
    }

    private Button setupStartButton() {
        startButton = new Button( "Start" );
        startButton.setOnAction( event -> startOrStopAnimation() );
        return startButton;
    }

    private void startOrStopAnimation() {
        if ( !animationIsRunning ) {
            startAnimation();
        }
        else {
            stopAnimation();
        }
    }

    private void startAnimation() {
        createWorkerThread();
        disableStartButton();
        fillDrawingArea();
        animationIsRunning = true;
        workerThread.start();
    }

    private void stopAnimation() {
        enableStartButton();
        animationIsRunning = false;
        workerThread = null;
    }

    private void enableStartButton() {
        startButton.setDisable( false );
    }

    private void disableStartButton() {
        startButton.setDisable( true );
    }

    private void createWorkerThread() {
        workerThread = new ComputationThread();
        setWorkerThreadPriority();
    }

    private void fillDrawingArea() {
        drawingArea.setFill( Color.LIGHTGRAY );
        drawingArea.fillRect( 0, 0, canvasWidth, canvasHeight );
    }

    private void setWorkerThreadPriority() {
        try {
            workerThread.setPriority( Thread.currentThread().getPriority() - 1 );
        }
        catch ( Exception e ) {
            System.out.println( "Cannot set priority of worker thread." );
        }
    }

    private void fillColorPalette() {
        colorPalette = new Color[ 256 ];
        for ( int i = 0; i < colorPalette.length; i++ )
            colorPalette[i] = Color.hsb( 360*( i / 256.0 ), 1, 1 );
    }

    private void showMainWindow() {
        mainWindow.setTitle( "Background Computation Demo" );
        mainWindow.show();
    }


    private void drawOneRowOfPixels( int rowNumber, Color[] colors ) {
        for ( int column = 0; column < canvasWidth; column++ ) {
            drawingArea.setFill( colors[ column ] );
            drawingArea.fillRect( column, rowNumber, 1, 1 );
        }
    }


    private class ComputationThread extends Thread {
        double xMin, xMax, yMin, yMax;
        int maximumIterations;

        ComputationThread() {
            xMin = -1.6744096740931858;
            xMax = -1.674409674093473;
            yMin = 4.716540768697223E-5;
            yMax = 4.716540790246652E-5;
            maximumIterations = 10000;
        }

        public void run() {
            try {
                Platform.runLater( () -> startButton.setDisable( false ) );
                Platform.runLater( () -> startButton.setText( "Abort" ) );
                calculateThePixelColors();
            }
            finally {
                restoreProgramState();
            }
        }

        private void calculateThePixelColors() {
            double dx = ( xMax - xMin ) / ( canvasWidth - 1 );
            double dy = ( yMax - yMin ) / ( canvasHeight - 1 );
            processRows( dx, dy );
        }

        private void processRows( double dx, double dy ) {
            for ( int row = 0; row < canvasHeight; row++ ) {
                processCurrentRow( dx, dy, row );
            }
        }

        private void processCurrentRow( double dx, double dy, int rowNumber ) {
            final Color[] rgb = new Color[ canvasWidth ];
            double y = yMax - dy * rowNumber;
            for ( int column = 0; column < canvasWidth; column++ ) {
                Color colorForCurrentColumn = processCurrentColumn( column, y, dx );
                rgb[ column ] = colorForCurrentColumn;

                if ( !animationIsRunning )
                    return;
            }
            drawCurrentRowOntoTheCanvas( rowNumber, rgb );
        }

        private Color processCurrentColumn( int column, double y, double dx ) {
            double x = xMin + dx * column;
            int count = 0;
            double xx = x, yy = y;
            while ( count < maximumIterations && ( xx*xx + yy*yy ) < 4 ) {
                count++;
                double newXX = xx*xx - yy*yy + x;
                yy = 2*xx*yy + y;
                xx = newXX;
            }
            return ( count == maximumIterations ) ? Color.BLACK : colorPalette[ count % colorPalette.length ];
        }
    }

    private void drawCurrentRowOntoTheCanvas( int rowNumber, Color[] colorsForCurrentRow ) {
        Platform.runLater( () -> drawOneRowOfPixels( rowNumber, colorsForCurrentRow ) );
    }

    private void restoreProgramState() {
        Platform.runLater( () -> startButton.setText( "Start" ) );
        Platform.runLater( () -> startButton.setDisable( false ) );
        animationIsRunning = false;
        workerThread = null;
    }
}
