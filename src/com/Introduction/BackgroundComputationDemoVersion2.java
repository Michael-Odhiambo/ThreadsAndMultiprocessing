package com.Introduction;

/**
 * This program serves as an introduction to multiple processing. The first version of the same program used only
 * one thread to compute an image in the Mandelbrot set. The aim of that program was to keep the computer as busy
 * as possible working on the computation. That aim is accomplished on a computer with a single processor but on a
 * computer with multiple processors, only one of those processors is used for the computation. In this version, several
 * threads are created and assigned part of the image to compute. The result is that the image is computed much faster
 * than when only a single thread is used. The number of threads to be used is selected by the user.
 */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.geometry.Pos;

public class BackgroundComputationDemoVersion2 extends Application {

    private Stage mainWindow;
    private Canvas canvas;
    private GraphicsContext drawingArea;
    private Button startOrStopButton;
    private ComboBox<String> threadCountSelector;
    private ComputationThread[] workerThreads;
    private volatile boolean animationIsRunning;
    private Color[] colorPalette;
    private ComputationThread[] workers;
    private int numberOfThreadsRunning;

    private final int CANVAS_WIDTH = 700;
    private final int CANVAS_HEIGHT = 500;

    public static void main( String[] args ) {
        launch( args );
    }

    public void start( Stage stage ) {
        setupMainWindow( stage );
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
        BorderPane canvasAndButtonHolder = new BorderPane( setupCanvas() );
        canvasAndButtonHolder.setBottom( setupButtonBar() );
        return canvasAndButtonHolder;
    }

    private Canvas setupCanvas() {
        canvas = new Canvas( CANVAS_WIDTH, CANVAS_HEIGHT );
        drawingArea = canvas.getGraphicsContext2D();
        fillDrawingArea();
        setupColorPalette();
        return canvas;
    }

    private void setupColorPalette() {
        colorPalette = new Color[ 256 ];
        for ( int i = 0; i < colorPalette.length; i++ )
            colorPalette[i] = Color.hsb( 360*( i / 256.0 ), 1, 1 );
    }

    private void fillDrawingArea() {
        drawingArea.setFill( Color.LIGHTGRAY );
        drawingArea.fillRect( 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT );
    }

    private HBox setupButtonBar() {
        HBox buttonBar = new HBox( 15, setupStartOrStopButton(), setupThreadCountSelector() );
        buttonBar.setStyle( "-fx-padding: 6px; -fx-border-color:black; -fx-border-width: 2px" );
        buttonBar.setAlignment( Pos.CENTER );
        return buttonBar;
    }

    private Button setupStartOrStopButton() {
        startOrStopButton = new Button( "Start" );
        startOrStopButton.setOnAction( event -> startOrStopAnimation() );
        return startOrStopButton;
    }

    private void startOrStopAnimation() {
        if ( !animationIsRunning )
            startAnimation();
        else
            stopAnimation();
    }

    private void startAnimation() {
        animationIsRunning = true;
        fillDrawingArea();
        setStartOrStopButtonLabel();
        disableThreadCountSelector();
        setupRequiredNumberOfThreads();
        startTheWorkerThreads();
    }

    private void setStartOrStopButtonLabel() {
        startOrStopButton.setText( ( startOrStopButton.getText().equals( "Start" ) ) ? "Stop" : "Start" );
    }

    private void disableThreadCountSelector() {
        threadCountSelector.setDisable( true );
    }

    private void setupRequiredNumberOfThreads() {
        numberOfThreadsRunning = threadCountSelector.getSelectionModel().getSelectedIndex() + 1;
        workers = new ComputationThread[ numberOfThreadsRunning ];
        allocatePartOfTheImageToEachThread();
    }

    private void allocatePartOfTheImageToEachThread() {
        int numberOfRowsPerThread = CANVAS_HEIGHT / ( threadCountSelector.getSelectionModel().getSelectedIndex() + 1 );
        for ( int worker = 0; worker < workers.length; worker++ ) {
            int startRow = worker * numberOfRowsPerThread;
            int endRow = startRow + numberOfRowsPerThread;
            if ( worker == workers.length - 1 )
                endRow = CANVAS_HEIGHT;
            workers[ worker ] = new ComputationThread( startRow, endRow );
        }
    }

    private void startTheWorkerThreads() {
        setWorkerThreadsPriority();
        for ( int worker = 0; worker < workers.length; worker++ )
            workers[ worker ].start();
    }

    private void setWorkerThreadsPriority() {
        for ( int worker = 0; worker < workers.length; worker++ ) {
            try {
                workers[ worker ].setPriority( Thread.currentThread().getPriority() - 1 );
            }
            catch ( Exception e ) {
                System.out.println( "Could not set thread priority." );
            }
        }
    }

    private void stopAnimation() {
        animationIsRunning = false;
        startOrStopButton.setDisable( true ); // Will be re-enabled when all the threads finish.
    }

    private ComboBox<String> setupThreadCountSelector() {
        threadCountSelector = new ComboBox<>();
        threadCountSelector.setEditable( false );
        threadCountSelector.getItems().addAll( "Use 1 Thread", "Use 2 Threads", "Use 3 Threads",
                "Use 4 Threads", "Use 5 Threads", "Use 6 Threads", "Use 7 Threads", "Use 8 Threads" );
        threadCountSelector.getSelectionModel().select( 1 );
        return threadCountSelector;
    }

    private void showMainWindow() {
        mainWindow.setTitle( "Background Computation version 2" );
        mainWindow.show();
    }


    private class ComputationThread extends Thread {

        private int startRow, endRow;
        double xMin, xMax, yMin, yMax;
        int maximumIterations;

        ComputationThread( int startRow, int endRow ) {
            this.startRow = startRow;
            this.endRow = endRow;
            xMin = -1.6744096740931858;
            xMax = -1.674409674093473;
            yMin = 4.716540768697223E-5;
            yMax = 4.716540790246652E-5;
            maximumIterations = 10000;
        }

        public void run() {
            try {
                calculateThePixelColors();
            }
            finally {
                threadHasFinished();
            }
        }

        private void calculateThePixelColors() {
            double dx = ( xMax - xMin ) / ( CANVAS_WIDTH - 1 );
            double dy = ( yMax - yMin ) / ( CANVAS_HEIGHT - 1 );
            processRows( dx, dy );
        }

        private void processRows( double dx, double dy ) {
            for ( int row = startRow; row <= endRow; row++ ) {
                processCurrentRow( dx, dy, row );
            }
        }

        private void processCurrentRow( double dx, double dy, int rowNumber ) {
            final Color[] rgb = new Color[ CANVAS_WIDTH ];
            double y = yMax - dy * rowNumber;
            for ( int column = 0; column < CANVAS_WIDTH; column++ ) {
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

    private void drawOneRowOfPixels( int rowNumber, Color[] colors ) {
        for ( int column = 0; column < CANVAS_WIDTH; column++ ) {
            drawingArea.setFill( colors[ column ] );
            drawingArea.fillRect( column, rowNumber, 1, 1 );
        }
    }

    synchronized private void threadHasFinished() {
        numberOfThreadsRunning--;
        if ( numberOfThreadsRunning < 1 ) {
            Platform.runLater( () -> {
                setStartOrStopButtonLabel();
                threadCountSelector.setDisable( false );
                startOrStopButton.setDisable( false );
            } );
            animationIsRunning = false;
            workers = null;
        }
    }

}
