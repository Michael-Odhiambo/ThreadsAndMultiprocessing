package com.Introduction;

/**
 * In version 3 of this program, a new thread pool is created every time an image is computed. It would be nice
 * to create the thread pool once at the very beginning of the program and re-use the threads every time a new image
 * is to be computed. In this version, this is done by using a blocking queue. Once the thread pool is created, the
 * threads are blocked while waiting for tasks to be added to the queue. Once tasks have been added to the queue, the
 * threads wake up, take tasks from the queue and run them.
 */
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.geometry.Pos;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import java.util.concurrent.LinkedBlockingQueue;

public class BackgroundComputationVersion4 extends Application {

    private Stage mainWindow;
    private Color[] colorPalette;
    private ComputationThread [] threadPool;
    private Canvas canvas;
    private GraphicsContext drawingArea;
    private Button startOrStopButton;
    private boolean jobInProgress;
    private int numberOfTasks;


    private final int THREAD_COUNT = 4;
    private int CANVAS_WIDTH = 700;
    private int CANVAS_HEIGHT = 550;
    private LinkedBlockingQueue< Runnable > taskQueue = new LinkedBlockingQueue<>();

    public static void main( String[] args ) {
        launch( args );
    }

    public void start( Stage stage ) {
        setupMainWindow( stage );
        setupThreadPool();
        showMainWindow();
    }

    private void setupMainWindow( Stage stage ) {
        createMainWindow( stage );
        mainWindow.setScene( setupScene() );
    }

    private void createMainWindow( Stage stage ) {
        mainWindow = stage;
    }

    private Scene setupScene() {
        Scene scene = new Scene( setupBorderPane() );
        return scene;
    }

    private BorderPane setupBorderPane() {
        BorderPane root = new BorderPane( setupCanvas() );
        root.setBottom( setupButtonBar() );
        return root;
    }

    private Canvas setupCanvas() {
        canvas = new Canvas( CANVAS_WIDTH, CANVAS_HEIGHT );
        drawingArea = canvas.getGraphicsContext2D();
        fillDrawingArea();
        setupColorPalette();
        return canvas;
    }

    private void fillDrawingArea() {
        drawingArea.setFill( Color.hsb( 227.8125, 1, 1 ) );
        drawingArea.fillRect( 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT );
    }

    private void setupColorPalette() {
        createColorPalette();
        fillColorPalette();
    }

    private void createColorPalette() {
        colorPalette = new Color[ 256 ];
    }

    private void fillColorPalette() {
        for ( int i = 0; i < colorPalette.length; i++ )
            colorPalette[i] = Color.hsb( 360*( i / 256.0 ), 1, 1 );
    }

    private HBox setupButtonBar() {
        HBox buttonBar = new HBox( 15, setupStartOrStopButton() );
        buttonBar.setAlignment( Pos.CENTER );
        buttonBar.setStyle( "-fx-padding: 6px; -fx-border-width: 2px" );
        return buttonBar;
    }

    private Button setupStartOrStopButton() {
        startOrStopButton = new Button( "Start" );
        startOrStopButton.setOnAction( event -> startOrStopAnimation() );
        return startOrStopButton;
    }

    private void startOrStopAnimation() {
        if ( !jobInProgress )
            startAnimation();
        else
            stopAnimation();
    }

    private void startAnimation() {
        jobInProgress = true;
        fillDrawingArea();
        startOrStopButton.setText( "Stop" );
        fillTaskQueue();
    }

    private void fillTaskQueue() {
        numberOfTasks = CANVAS_HEIGHT;
        for ( int i = 0; i < numberOfTasks; i++ )
            taskQueue.add( new MandelbrotTask( i ) );
    }

    private void stopAnimation() {
        taskQueue.clear();
        jobInProgress = false;
        startOrStopButton.setDisable( false );  // Will be re-enabled by the last task to finish.
        startOrStopButton.setText( "Start" );
    }

    private void setupThreadPool() {
        createThreadPool();
        fillThreadPool();
    }

    private void createThreadPool() {
        threadPool = new ComputationThread[ THREAD_COUNT ];
    }

    private void fillThreadPool() {
        for ( int i = 0; i < threadPool.length; i++ )
            threadPool[ i ] = new ComputationThread();
    }

    private void showMainWindow() {
        mainWindow.setTitle( "Background Computation Version 4" );
        mainWindow.show();
    }


    private class ComputationThread extends Thread {

        ComputationThread() {
            setToDaemonThread();
            setThreadPriority();
            startThread();
        }

        private void setToDaemonThread() {
            try {
                this.setDaemon( true );
            }
            catch ( SecurityException e ) {
                System.out.println( "Cannot set thread priority." );
            }
        }

        private void setThreadPriority() {
            try {
                this.setPriority( Thread.currentThread().getPriority() - 1 );
            }
            catch ( SecurityException e ) {
                System.out.println( "Cannot set thread priority." );
            }
        }

        private void startThread() {
            this.start();
        }

        public void run() {
            while ( true ) {
                try {
                    Runnable task = taskQueue.take();
                    task.run();
                }
                catch ( InterruptedException e ) {}
            }
        }
    }


    private class MandelbrotTask implements Runnable {

        private int rowNumber;
        double xMin, xMax, yMin, yMax;
        int maximumIterations;

        public MandelbrotTask( int rowNumber ) {
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

                if ( !jobInProgress )
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
                startOrStopButton.setDisable( false );
                startOrStopButton.setText( "Start" );
            } );
            jobInProgress = false;
        }
    }
}
