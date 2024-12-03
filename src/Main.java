import java.util.Timer;

public class Main {
    public static void main(String[] args) {
        // OS is started up with a new instance of an init script.
        OS.Startup(new Init());
    }
}