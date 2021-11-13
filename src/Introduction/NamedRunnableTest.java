package Introduction;

public class NamedRunnableTest {

    public static void main( String[] args ) {
        Thread greetingsThread = new Thread( new NamedRunnable( "Michael" ) );
        greetingsThread.start();
        System.out.println( "Runnable has been started." );
    }
}
