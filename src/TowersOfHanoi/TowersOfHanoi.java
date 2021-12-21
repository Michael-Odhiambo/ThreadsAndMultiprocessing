package TowersOfHanoi;

import javafx.application.Application;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.application.Platform;


public class TowersOfHanoi extends Application {

    private Button startOrPauseButton;
    private Button nextStepButton;
    private Button startOverButton;
    private Canvas canvas;
    private GraphicsContext drawingArea;
    private Stage mainWindow;

    private enum STATUS { RUN, PAUSE, STEP, RESTART };
    private STATUS currentStatus;

    private int[][] tower;
    private int[] towerHeight;
    private int diskToMove;
    private int moveTower;

    private Color BACKGROUND_COLOR = Color.LIGHTGRAY;
    private Color DISK_COLOR = Color.hsb( 12, 1, 1 );
    private Color MOVEDISKCOLOR = Color.hsb( 120, 1, 1 );


    public static void main( String[] args ) {
        launch( args );
    }

    // --------------------------------------------------------------------------------------------

    public void start( Stage stage ) {
        setUpMainWindow( stage );
        setUpMainWindowComponents();
        showMainWindow();
        startAnimationThread();
    }

    private void setUpMainWindow( Stage stage ) {
        mainWindow = stage;
        mainWindow.setTitle( "Towers of Hanoi" );
        mainWindow.setResizable( false );
    }

    private void setUpMainWindowComponents() {
        setUpSceneComponents();
        setUpScene();
    }


    private void setUpSceneComponents() {
        setUpCanvas();
        setUpButtons();
        setUpLayout();
    }

    private void setUpCanvas() {
        canvas = new Canvas( 1000, 500 );
        drawingArea = canvas.getGraphicsContext2D();
        drawTowers();
    }

    private void setUpButtons() {
        createStartOrPauseButton();
        createNextStepButton();
        createStartOverButton();
    }

    private void drawTowers() {
        fillBackground();
        drawingArea.setFill( Color.SADDLEBROWN );
        for ( int i = 0; i < 3; i++ )  // Draw the bases.
            drawingArea.fillRect( 100 + i * ( 220 + 100 ), 450, 220, 20 );
        for ( int i = 0; i < 3; i++ )  // Draw the poles.
            drawingArea.fillRect( 205 + i * ( 20 + 300 ), 250, 20, 199 );
    }

    private void fillBackground() {
        drawingArea.setFill( BACKGROUND_COLOR );
        drawingArea.fillRect( 0, 0, canvas.getWidth(), canvas.getHeight() );
    }

    private void createStartOrPauseButton() {
        startOrPauseButton = new Button( "Start" );
        startOrPauseButton.setMaxWidth( 10000 );
        startOrPauseButton.setPrefHeight( 40 );
        startOrPauseButton.setOnAction( event -> startOrPauseAnimation() );
    }

    private void createNextStepButton() {
        nextStepButton = new Button( "Next Step" );
        nextStepButton.setMaxWidth( 10000 );
        nextStepButton.setPrefHeight( 40 );
        nextStepButton.setOnAction( event -> doTheNextStep() );
    }

    private void createStartOverButton() {
        startOverButton = new Button( "Start Over" );
        startOverButton.setMaxWidth( 10000 );
        startOverButton.setPrefHeight( 40 );
        startOverButton.setOnAction( event -> restartAnimation() );
    }

    private BorderPane setUpLayout() {
        BorderPane root = new BorderPane( canvas );
        root.setBottom( createButtonHolder() );
        return root;

    }

    private HBox createButtonHolder() {
        HBox buttonHolder = new HBox( startOrPauseButton, nextStepButton, startOverButton );
        buttonHolder.setStyle( "-fx-border-color: black; -fx-border-width: 2px 0 0 0" );
        buttonHolder.setPrefHeight( 40 );
        HBox.setHgrow( startOrPauseButton, Priority.ALWAYS );
        HBox.setHgrow( nextStepButton, Priority.ALWAYS );
        HBox.setHgrow( startOverButton, Priority.ALWAYS );
        return buttonHolder;
    }

    private void setUpScene() {
        Scene scene = new Scene( setUpLayout() );
        mainWindow.setScene( scene );
    }

    private void showMainWindow() {
        disableNextStepButton();
        disableStartOverButton();
        mainWindow.show();
    }

    private void disableNextStepButton() {
        nextStepButton.setDisable( true );
    }

    private void enableNextStepButton() {
        nextStepButton.setDisable( false );
    }

    private void disableStartOverButton() {
        startOverButton.setDisable( true );
    }

    private void enableStartOverButton() {
        startOverButton.setDisable( false );
    }

    private void disableStartOrPauseButton() {
        startOrPauseButton.setDisable( true );
    }

    private void enableStartOrPauseButton() {
        startOrPauseButton.setDisable( false );
    }

    private void startAnimationThread() {
        new AnimationThread().start();
    }

    // --------------------------------------------------------------------------------------------

    private class AnimationThread extends Thread {

        AnimationThread() {
            setDaemon( true );
        }

        public void run() {
            while ( true ) {
                Platform.runLater( () -> {
                    modifyButtonStateAccordingToStatus();
                } );
                setUpTheProblem();  // Sets up the initial state of the puzzle.
                currentStatus = STATUS.PAUSE;
                checkStatus();  // Returns only when the user has clicked "Run" or "Next step".

                Platform.runLater( () -> enableStartOverButton() );

                try {
                    solve( 10, 0, 1, 2 );
                    currentStatus = STATUS.PAUSE;
                    Platform.runLater( () -> {
                        modifyButtonStateAccordingToStatus();
                    } );
                    checkStatus();
                }
                catch ( IllegalStateException e ) {
                    // Exception was thrown because the user clicked "Start Over".
                }
            }
        }

        private void modifyButtonStateAccordingToStatus() {
            if ( currentStatus == STATUS.PAUSE ) {
                startOrPauseButton.setText( "Run" );
                enableStartOrPauseButton();
                enableNextStepButton();
                disableStartOverButton();
            }
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    /**
     * This method is called before starting the solution and after each move of the solution. If currentStatus is
     * PAUSE, it waits until the status changes. If currentStatus is RESTART, it throws an IllegalStateException that
     * will abort the solution. When this method returns, the value of currentStatus must be RUN or STEP.
     */
    synchronized private void checkStatus() {
        while ( currentStatus == STATUS.PAUSE ) {
            try {
                wait();
            }
            catch ( InterruptedException e ) {}
        }
        // At this point, currentStatus is either RUN, STEP or RESTART.
        if ( currentStatus == STATUS.RESTART )
            throw new IllegalStateException();
        // At this point, currentStatus is RUN or STEP, and the solution should proceed accordingly.
    }

    synchronized private void setUpTheProblem() {
        diskToMove = 0;
        tower = new int[3][10];
        for ( int i = 0; i < 10; i++ )
            tower[0][i] = 10 - i;
        towerHeight = new int[3];
        towerHeight[0] = 10;
        Platform.runLater( this::drawInitialFrame );
    }

    private void solve( int disks, int from, int to, int spare ) {
        if ( disks == 1 )
            moveOne( from, to );
        else {
            solve( disks - 1, from, spare, to );
            moveOne( from, to );
            solve( disks - 1, spare, to, from );
        }
    }

    synchronized private void moveOne( int fromTower, int toTower ) {
        diskToMove = tower[ fromTower ][towerHeight[ fromTower ] - 1 ];
        moveTower = fromTower;
        delay(120);
        towerHeight[ fromTower ]--;
        putDisk( MOVEDISKCOLOR, diskToMove, moveTower, towerHeight[ fromTower ] );
        delay(100);
        putDisk( BACKGROUND_COLOR, diskToMove, moveTower, towerHeight[ fromTower ] );
        delay(100);
        moveTower = toTower;
        putDisk( MOVEDISKCOLOR, diskToMove, moveTower, towerHeight[ toTower ] );
        delay(100);
        putDisk( DISK_COLOR, diskToMove, moveTower, towerHeight[ toTower ] );
        tower[ toTower ][towerHeight[ toTower ] ] = diskToMove;
        towerHeight[ toTower ]++;
        diskToMove = 0;
        if ( currentStatus == STATUS.STEP )
            currentStatus = STATUS.PAUSE;
        checkStatus();

    }

    synchronized private void delay( int milliseconds ) {
        try {
            wait( milliseconds );
        }
        catch ( InterruptedException e ) {};
    }

    /**
     * Draw a specified disk to the off-screen canvas. This is used only during the moveOne() method to draw the disk
     * that is being moved. This method is called from the animation thread. It uses Platform.runLater() to apply the
     * drawing to the canvas.
     * @param color the color of the disk. ( Use background color to erase. )
     * @param disk the number of the disk that is to be drawn, 1 to 10.
     * @param t the number of the pile on top of which the disk is drawn.
     * @param h the height of the tower.
     */
    private void putDisk( Color color, int disk, int t, int h ) {
        Platform.runLater( () -> {
            drawingArea.setFill( color );
            if ( color == BACKGROUND_COLOR ) {
                // When drawing in the background color, to erase a disk, a slightly
                // larger roundrect is drawn.  This is done to make sure that the
                // disk is completely erased, since the anti-aliasing that was done
                // when the disk was drawn can allow the disk color to bleed into pixels
                // that lie outside the actual disk.
                drawingArea.fillRoundRect( 195 + ( 75+140*t - 5*disk-6 ), 320 + ( 116-17*h - 1 ), 10*disk+40,
                        15, 10, 10);
            }
            else
                drawingArea.fillRoundRect(190 + ( 325*t - 5*disk-5 ), 320 + ( 116-17*h ), 10*disk+40, 15,
                        10, 10);
        } );
    }

    /**
     * Called to draw the starting state of the towers, will all the disks on the first base. This method is called
     * on the JavaFX application thread.
     */
    private void drawInitialFrame() {
        drawingArea.setFill( DISK_COLOR );
        for ( int t = 0; t < 3; t++ ) {
            for ( int i = 0; i < towerHeight[ t ]; i++ ) {
                int disk = tower[ t ][ i ];
                drawingArea.fillRoundRect( 190 + ( 325*t - 5*disk-5 ), 320 + ( 116-17*i ), 10*disk+40, 15, 10,
                        10 );
            }
        }
    }

    synchronized private void startOrPauseAnimation() {
        if ( currentStatus == STATUS.RUN ) {  // Animation is running. Pause it.
            currentStatus = STATUS.PAUSE;
            enableNextStepButton();
            startOrPauseButton.setText( "Run" );
        }
        else { // Animation is paused. Start it.
            currentStatus = STATUS.RUN;
            disableNextStepButton();
            startOrPauseButton.setText( "Pause" );
        }
        notify();  // Wake up the animation thread so it can see the new status value.
    }

    synchronized private void doTheNextStep() {
        currentStatus = STATUS.STEP;
        notify();
    }

    synchronized private void restartAnimation() {
        currentStatus = STATUS.RESTART;
        notify();
    }
}
