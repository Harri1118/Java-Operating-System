import java.util.concurrent.Semaphore;

public abstract class Process implements Runnable{
    // Thread is declated as a new thread in process after its instantiated. Process class
    // is declared as the thread's candidate.
    public Thread thread = new Thread(this);
    // semaphore is declared as a new semaphore in the process class. Used to regulate the classes'
    // thread from running.
    public Semaphore semaphore = new Semaphore(0);
    // isExpired determines when the process is finished.
    public boolean isExpired = false;
    // Constructor automatically starts the thread.
    public Process(){
        thread.start();
    }
    // requestStop soft stops the process from running.
    // Sets isExpired to true.
    public void requestStop(){
        isExpired = true;
    }
    // abstract class to be used for any inheror classes.
    public abstract void main();
    // isStopped checks to see if the semaphore has the proper permit to run.
    // If it doesn't then return true, if not return false.
    public boolean isStopped(){
        return semaphore.availablePermits() == 0;
    }
    // isDone checks if the thread is null or if it's still alive. If either are true
    // then return false. It will return true otherwise.
    public boolean isDone(){
        if(thread == null)
            return false;
        if(thread.isAlive())
            return false;
        return true;
    }
    // start releases the semaphore to resume the thread.
    public void start(){
        semaphore.release();
        //semaphore.hasQueuedThreads();
        // If count  != 0, throw an exception

    }
    // stop acquires the semaphore to inhibit the thread from running.
    public void stop() {
        try{
        semaphore.acquire();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    // run acquires the semaphore and then calls main.
    @Override
    public void run(){
        try {
            semaphore.acquire();
            main();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    // cooperate checks if isExpired is true, will set it to false if so. It then calls OS.SwitchProcess.
    public void cooperate(){
        if(isExpired){
            isExpired = false;
            OS.SwitchProcess();
        }
        //System.out.println("Cooperate completed!");
    }
}
