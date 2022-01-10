package com.Introduction;

/**
 * This program is a visual demonstration of how the towers of hanoi is solved.
 */
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

public class TowersOfHanoiVisualization extends Application {

    private Stage mainWindow;
    private Button startOverButton;
    private Button pauseOrResumeButton;
    private Button nextStepButton;
    private Canvas canvas;
    private GraphicsContext drawingArea;
    private int[][] towers;
    private int[] towerHeights;

    private int CANVAS_WIDTH = 610;
    private int CANVAS_HEIGHT = 300;
    private final Color BACKGROUND_COLOR = Color.WHITE;
    private final Color BASE_COLOR = Color.BROWN;
    private final Color DISK_COLOR = Color.hsb( 30, 1, 1 );
    private final Color MOVE_DISK_COLOR = Color.RED;
    private enum STATUS { RUNNING, PAUSE, NEXTSTEP, RESTART };
    private STATUS currentStatus;

    public void start( Stage stage ) {
        setupMainWindow( stage );
        showMainWindow();
        setupSolutionThread();
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
        return canvas;
    }

    private void drawTowers() {
        fillTheDrawingArea();
        drawTheThreeBases();
        drawTheDisks();
    }

    private void fillTheDrawingArea() {
        drawingArea.setFill( BACKGROUND_COLOR );
        drawingArea.fillRect( 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT );
    }

    private void drawTheThreeBases() {
        drawingArea.setFill( BASE_COLOR );
        drawingArea.fillRect( 40, 270, 160, 5 );
        drawingArea.fillRect( 230, 270, 160, 5 );
        drawingArea.fillRect( 420, 270, 160, 5 );
    }

    private void drawTheDisks() {
        drawingArea.setFill( DISK_COLOR );
        for (int t = 0; t < 3; t++) {
            for (int i = 0; i < towerHeights[t]; i++) {
                int disk = towers[t][i];
                drawingArea.fillRoundRect( 120+190*t - 5*disk - 5, 250-12*i, 10*disk+10, 10, 10,
                        10 );
            }
        }
    }

    private HBox setupButtonBar() {
        HBox buttonBar = new HBox( 15, setupStartOverButton(), setupPauseOrResumeButton(), setupNextStepButton() );
        buttonBar.setStyle( "-fx-padding: 6px; -fx-border-width: 2px; -fx-background-color: lightgray" );
        buttonBar.setAlignment( Pos.CENTER );
        return buttonBar;
    }

    private Button setupStartOverButton() {
        startOverButton = new Button( "StartOver" );
        startOverButton.setOnAction( event -> startOver() );
        return startOverButton;
    }

    synchronized private void startOver() {
        currentStatus = STATUS.RESTART;
        pauseOrResumeButton.setText( "Run" );
        nextStepButton.setDisable( false );
        notify();
    }

    private Button setupPauseOrResumeButton() {
        pauseOrResumeButton = new Button( "Run" );
        pauseOrResumeButton.setOnAction( event -> pauseOrResume() );
        return pauseOrResumeButton;
    }

    synchronized private void pauseOrResume() {
        if ( currentStatus == STATUS.RUNNING )
            pauseSolution();
        else
            resumeSolution();
        notify();
    }

    private void pauseSolution() {
        currentStatus = STATUS.PAUSE;
        nextStepButton.setDisable( false );
        pauseOrResumeButton.setText( "Resume" );
    }

    private void resumeSolution() {
        currentStatus = STATUS.RUNNING;
        pauseOrResumeButton.setText( "Pause" );
        nextStepButton.setDisable( true );
        startOverButton.setDisable( false );
    }

    private Button setupNextStepButton() {
        nextStepButton = new Button( "Next Step" );
        nextStepButton.setOnAction( event -> nextStep() );
        return nextStepButton;
    }

    synchronized private void nextStep() {
        currentStatus = STATUS.NEXTSTEP;
        notify();
    }

    private void showMainWindow() {
        mainWindow.setTitle( "Towers of Hanoi" );
        mainWindow.show();
    }

    private void setupSolutionThread() {
        new AnimationThread().start();  // Will immediately go to sleep since currentStatus will be PAUSE.
    }


    private class AnimationThread extends Thread {

        AnimationThread() {
            setToDaemon( this );
        }

        public void run() {
            while ( true ) {
                setupTheProblem();
                drawTheInitialFrame();
                setCurrentStatusToPause();
                checkStatus();  // Returns only after user clicks RUN or NEXTSTEP.
                solveThePuzzle();
            }
        }
    }

    private void setToDaemon( Thread thread ) {
        try {
            thread.setDaemon( true );
        }
        catch ( SecurityException e ) {
            System.out.println( "Cannot set daemon status of thread." );
        }
    }
    private void setupTheProblem() {
        setupTowers();
        setupTowerHeights();
    }

    private void setupTowers() {
        towers = new int[3][10];
        for ( int i = 0; i < 10; i++ )
            towers[0][i] = 10 - i;
    }

    private void setupTowerHeights() {
        towerHeights = new int[3];
        towerHeights[0] = 10;
    }

    private void drawTheInitialFrame() {
        Platform.runLater( () -> {
            drawInitialFrame();
            startOverButton.setDisable( true );
        } );
    }

    private void drawInitialFrame() {
        drawTowers();
    }

    private void setCurrentStatusToPause() {
        currentStatus = STATUS.PAUSE;
    }

    synchronized private void checkStatus() {
        while ( currentStatus == STATUS.PAUSE ) {
            try {
                wait();
            }
            catch ( InterruptedException e ) {}
        }
        if ( currentStatus == STATUS.RESTART )
            throw new IllegalStateException( "RESTART" );
        if ( currentStatus == STATUS.NEXTSTEP )
            currentStatus = STATUS.PAUSE;
    }

    private void solveThePuzzle() {
        try {
            solve( 10, 0, 1, 2 );
            setCurrentStatusToPause();
            Platform.runLater( () -> {
                startOverButton.setDisable( false );
                pauseOrResumeButton.setText( "Run" );
                nextStepButton.setDisable( true );
                pauseOrResumeButton.setDisable( true );
            } );
            checkStatus();
        }
        catch ( Exception e ) {}
    }

    private void solve( int numberOfDisks, int fromTower, int throughTower, int toTower ) {
        if ( numberOfDisks == 1 )
            moveDisk( fromTower, toTower );
        else {
            solve( numberOfDisks - 1, fromTower, toTower, throughTower );
            moveDisk( fromTower, toTower );
            solve( numberOfDisks - 1, throughTower, fromTower, toTower );
        }
    }

    private void moveDisk( int fromTower, int toTower ) {
        int diskToMove = towers[ fromTower ][ towerHeights[ fromTower ] - 1 ];
        int moveTower = fromTower;
        delay( 120 );
        towerHeights[ fromTower ]--;
        putDisk( MOVE_DISK_COLOR, diskToMove, moveTower, towerHeights[ fromTower ] );  // Highlight the disk.
        delay( 100 );
        putDisk( BACKGROUND_COLOR, diskToMove, moveTower, towerHeights[ fromTower ] ); // Erase the disk.
        delay( 100 );
        moveTower = toTower;
        putDisk( MOVE_DISK_COLOR, diskToMove, moveTower, towerHeights[ toTower ] );
        delay( 100 );
        putDisk( DISK_COLOR, diskToMove, moveTower, towerHeights[ toTower ] );
        towers[ toTower ][ towerHeights[ toTower ] ] = diskToMove;
        towerHeights[ toTower ]++;
        checkStatus();
    }

    synchronized private void delay( int milliseconds ) {
        try {
            wait( milliseconds );
        }
        catch ( InterruptedException e ) {}
    }

    private void putDisk( Color color, int disk, int tower, int height ) {
        Platform.runLater( () -> {
            drawingArea.setFill( color );
            if ( color == BACKGROUND_COLOR )
                drawingArea.fillRoundRect( 120+190*tower - 5*disk - 6, 250-12*height - 1, 10*disk+12,
                        12, 10, 10 );
            else
                drawingArea.fillRoundRect( 120+190*tower - 5*disk - 5, 250-12*height, 10*disk+10, 10,
                        10, 10 );
        } );
    }
}
