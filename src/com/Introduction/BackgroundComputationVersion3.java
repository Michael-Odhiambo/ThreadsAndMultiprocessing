package com.Introduction;

/**
 * In version 2 of this program, the task of computing the required image is divided equally among the threads. This
 * works OK but is not optimal. Some parts of the image might take longer to compute than others. This means that
 * while the tasks are assigned equally among the threads, some threads might finish earlier than others. In this
 * version, the task of computing the image is broken down into a series of subtasks. A thread pool is then used
 * where several threads are created to work on the subtasks. The threads run in a loop, fetching tasks from a queue
 * and executing them.
 *
 * THE COMPUTATION IS MUCH FASTER WHEN TRIED WITH MORE THAN ONE THREAD!!!
 */
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.geometry.Pos;
import javafx.application.Platform;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BackgroundComputationVersion3 extends Application {

    private Stage mainWindow;
    private Canvas canvas;
    private GraphicsContext drawingArea;
    private Button startOrStopButton;
    private ComboBox<String> threadCountSelector;
    private volatile boolean animationIsRunning;
    private ConcurrentLinkedQueue< Runnable > taskQueue;
    private ComputationThread [] workers;
    private int threadsRunning;
    private Color[] colorPalette;

    private int CANVAS_WIDTH = 700;
    private int CANVAS_HEIGHT = 550;

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
        BorderPane root = new BorderPane( setupCanvas() );
        root.setBottom( setupButtonBar() );
        return root;
    }

    private Canvas setupCanvas() {
        canvas = new Canvas( CANVAS_WIDTH, CANVAS_HEIGHT );
        drawingArea = canvas.getGraphicsContext2D();
        setupColorPalette();
        fillDrawingArea();
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

        buttonBar.setStyle( "-fx-padding: 6px; -fx-border-width: 2px" );
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
        startOrStopButton.setText( "Stop" );
        threadCountSelector.setDisable( true );
        fillDrawingArea();
        setupTaskQueue();
        setupWorkerThreads();
        startWorkerThreads();
    }

    private void setupTaskQueue() {
        taskQueue = new ConcurrentLinkedQueue<>();
        fillTaskQueue();
    }

    private void fillTaskQueue() {
        for ( int row = 0; row < CANVAS_HEIGHT; row++ )
            taskQueue.add( new MandelbrotTask( row ) );

    }

    private void setupWorkerThreads() {
        threadsRunning = threadCountSelector.getSelectionModel().getSelectedIndex() + 1;
        workers = new ComputationThread[ threadsRunning ];
        fillThreadPool();
        setWorkerThreadsPriority();
    }

    private void fillThreadPool() {
        for ( int worker = 0; worker < threadsRunning; worker++ )
            workers[ worker ] = new ComputationThread();
    }

    private void setWorkerThreadsPriority() {
        for ( int i = 0; i < workers.length; i++ )
            setWorkerThreadPriority( workers[i] );
    }

    private void setWorkerThreadPriority( ComputationThread worker ) {
        try {
            worker.setPriority( Thread.currentThread().getPriority() - 1 );
        }
        catch ( Exception e ) {
            System.out.println( "Cannot set thread priority." );
        }
    }

    private void startWorkerThreads() {
        for ( int worker = 0; worker < workers.length; worker++ )
            workers[ worker ].start();
    }

    private void stopAnimation() {
        animationIsRunning = false;
        startOrStopButton.setDisable( true );
        taskQueue.clear();
        workers = null;
    }

    private ComboBox<String> setupThreadCountSelector() {
        threadCountSelector = new ComboBox<>();
        threadCountSelector.getItems().addAll( "Use 1 Thread", "Use 2 Threads", "Use 3 Threads",
                "Use 4 Threads", "Use 5 Threads", "Use 6 Threads", "Use 7 Threads", "Use 8 Threads" );
        threadCountSelector.getSelectionModel().select( 0 );
        return threadCountSelector;
    }

    private void showMainWindow() {
        mainWindow.setTitle( "Background computation version 3.0" );
        mainWindow.show();
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
            calculateThePixelColors();

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

    private class ComputationThread extends Thread {

        public void run() {
            try {
                while ( animationIsRunning ) {
                    Runnable task = taskQueue.poll();
                    if ( task == null )
                        break;
                    task.run();
                }
            }
            finally {
                threadHasFinished();
            }
        }
    }

    synchronized private void threadHasFinished() {
        threadsRunning--;
        if ( threadsRunning == 0 ) { // all threads have finished
            Platform.runLater( () -> {
                // Make sure state is correct when threads end.
                startOrStopButton.setText( "Start" );
                startOrStopButton.setDisable( false );
                threadCountSelector.setDisable( false );
            } );
            animationIsRunning = false; // Make sure running is false after the thread ends.
            workers = null;
        }
    }
}
