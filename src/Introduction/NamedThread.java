package Introduction;

public class NamedThread extends Thread {
    private String nameOfThisThread;

    public static void main( String[] args ) {
        NamedThread thread = new NamedThread( "Michael" );
        thread.start();
        System.out.println( "Thread has been started." );
    }

    public NamedThread( String nameOfThisThread ) {
        this.nameOfThisThread = nameOfThisThread;
    }

    // The run method prints a message to standard output.
    public void run() {
        System.out.println( "Greetings from thread '" + nameOfThisThread + "'" );
    }
}
