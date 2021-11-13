package Introduction;

public class NamedRunnable implements Runnable {

    private String nameOfThisRunnable;

    public NamedRunnable( String nameOfThisRunnable ) {
        this.nameOfThisRunnable = nameOfThisRunnable;
    }

    public void run() {
        System.out.println( "Greetings from Runnable: '" + this.nameOfThisRunnable + "'" );
    }
}
