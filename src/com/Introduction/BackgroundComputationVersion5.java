package com.Introduction;

/**
 * Since thread pools are common in parallel programming, it is not surprising that java has higher level techniques
 * for creating and managing thread pools. This version of the program uses an ExecutorService manage the threads
 * and execute the tasks necessary to draw the image.
 */
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.geometry.Pos;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class BackgroundComputationVersion5 extends Application {

    private Stage mainWindow;
    private Color[] colorPalette;
    private Canvas canvas;
    private GraphicsContext drawingArea;
    private Button startOrStopButton;
    private ExecutorService executor;
    private int numberOfTasks;


    private int CANVAS_WIDTH = 700;
    private int CANVAS_HEIGHT = 550;

    public static void main( String[] args ) {
        launch( args );
    }

    public void start( Stage stage ) {
        setupMainWindow( stage );
        showMainWindow();
    }

    private void setupMainWindow( Stage stage ) {
        createMainWindow( stage );
    }

    private void createMainWindow( Stage stage ) {
        mainWindow = stage;
        mainWindow.setScene( setupScene() );
    }

    private Scene setupScene() {
        return createScene();
    }

    private Scene createScene() {
        Scene scene = new Scene( setupBorderPane() );
        return scene;
    }

    private BorderPane setupBorderPane() {
        return createBorderPane();
    }

    private BorderPane createBorderPane() {
        BorderPane root = new BorderPane( setupCanvas() );
        root.setBottom( setupButtonBar() );
        return root;
    }

    private Canvas setupCanvas() {
        setupColorPalette();
        return createCanvas();
    }

    private void setupColorPalette() {
        colorPalette = new Color[ 256 ];
        for ( int i = 0; i < colorPalette.length; i++ )
            colorPalette[i] = Color.hsb( 360*( i / 256.0 ), 1, 1 );
    }

    private Canvas createCanvas() {
        canvas = new Canvas( CANVAS_WIDTH, CANVAS_HEIGHT );
        drawingArea = canvas.getGraphicsContext2D();
        fillDrawingArea();
        return canvas;
    }

    private void fillDrawingArea() {
        drawingArea.setFill( Color.LIGHTGRAY );
        drawingArea.fillRect( 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT );
    }

    private HBox setupButtonBar() {
        return createButtonBar();
    }

    private HBox createButtonBar() {
        HBox buttonBar = new HBox( 15, setupStartOrStopButton() );
        buttonBar.setAlignment( Pos.CENTER );
        buttonBar.setStyle( "-fx-padding: 6px; -fx-border-width: 2px" );
        return buttonBar;
    }

    private Button setupStartOrStopButton() {
        return createStartOrStopButton();
    }

    private Button createStartOrStopButton() {
        startOrStopButton = new Button( "Start" );
        startOrStopButton.setOnAction( event -> startOrStopAnimation() );
        return startOrStopButton;
    }

    private void startOrStopAnimation() {
        if ( executor == null )
            startAnimation();
        else
            stopAnimation();
    }

    private void startAnimation() {
        fillDrawingArea();
        setupExecutor();
        submitTasksToTheExecutor();
        executor.shutdown();  // The executor will shut down once all the tasks are finished.
        startOrStopButton.setText( "Stop" );
    }

    private void setupExecutor() {
        int numberOfThreads = Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool( numberOfThreads );
    }

    private void submitTasksToTheExecutor() {
        numberOfTasks = CANVAS_HEIGHT;
        for ( int i = 0; i < numberOfTasks; i++ )
            executor.execute( new MandelbrotTask( executor, i ) );
    }

    private void stopAnimation() {
        executor.shutdownNow();
        executor = null;
        startOrStopButton.setText( "Start" );
    }

    private void showMainWindow() {
        mainWindow.show();
    }

    private class MandelbrotTask implements Runnable {

        private int rowNumber;
        double xMin, xMax, yMin, yMax;
        int maximumIterations;
        ExecutorService myExecutor;

        public MandelbrotTask( ExecutorService executor, int rowNumber ) {
            myExecutor = executor;  // Which executor will execute this task.
            this.rowNumber = rowNumber;

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
                taskFinished();
            }
        }

        private void calculateThePixelColors() {
            double dx = ( xMax - xMin ) / ( CANVAS_WIDTH - 1 );
            double dy = ( yMax - yMin ) / ( CANVAS_HEIGHT - 1 );
            processRow( dx, dy );
        }

        private void processRow( double dx, double dy ) {
            final Color[] rgb = new Color[ CANVAS_WIDTH ];
            double y = yMax - dy * rowNumber;
            for ( int column = 0; column < CANVAS_WIDTH; column++ ) {
                Color colorForCurrentColumn = processCurrentColumn( column, y, dx );
                rgb[ column ] = colorForCurrentColumn;

                if ( myExecutor != executor )
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

        private void drawCurrentRowOntoTheCanvas( int rowNumber, Color[] colorsForCurrentRow ) {
            Platform.runLater( () -> drawOneRowOfPixels( rowNumber, colorsForCurrentRow ) );
        }

        private void drawOneRowOfPixels( int rowNumber, Color[] colors ) {
            for ( int column = 0; column < CANVAS_WIDTH; column++ ) {
                drawingArea.setFill( colors[ column ] );
                drawingArea.fillRect( column, rowNumber, 1, 1 );
            }
        }
    }

    synchronized private void taskFinished() {
        numberOfTasks--;
        if ( numberOfTasks < 1 ) {
            Platform.runLater( () -> {
                startOrStopButton.setText( "Start" );
            } );
            executor = null;
        }
    }
}
