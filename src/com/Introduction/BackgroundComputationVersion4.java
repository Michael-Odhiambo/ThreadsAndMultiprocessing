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
import javafx.scene.control.ComboBox;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.geometry.Pos;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import java.util.concurrent.LinkedBlockingQueue;

public class BackgroundComputationVersion4 extends Application {

    private Stage mainWindow;
    private Color[] colorPalette;
    private LinkedBlockingQueue< Runnable > taskQueue;
    //private ComputationThread [] threadPool;
    private Canvas canvas;
    private GraphicsContext drawingArea;
    private Button startOrStopButton;


    private final int THREAD_COUNT = 4;
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
        return canvas;
    }

    private void fillDrawingArea() {
        drawingArea.setFill( Color.LIGHTGRAY );
        drawingArea.fillRect( 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT );
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

    }

    private void startAnimation() {

    }

    private void stopAnimation() {

    }

    private void showMainWindow() {
        mainWindow.setTitle( "Background Computation Version 4" );
        mainWindow.show();
    }

}
