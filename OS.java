import java.util.ArrayList;

public class OS {
    // Kernel declared.
    private static Kernel kernel;
    // Process enums to pass into kernel.
    public enum CallType {CREATE_PROCESS, SWITCH_PROCESS, EXIT_PROGRAM, SLEEP, OPEN, CLOSE, READ, WRITE, SEEK, GET_PID, GET_PID_BY_NAME, SEND_MESSAGE, WAIT_FOR_MESSAGE,
        GET_MAPPING, ALLOCATE_MEMORY, FREE_MEMORY
    }
    public enum Priority{REAL_TIME, INTERACTIVE, BACKGROUND}
    // CurrentCall enum variable to be read by the kernel.
    public static CallType currentCall;
    // Parameters to be read by the kernel and used in the scheduler.
    public static ArrayList<Object> parameters = new ArrayList<Object>();
    // returnValue to store the pid of a created process.
    public static Object returnValue = null;

    public static int nextPage = 0;
    public static int CreateProcess(UserLandProcess up) {
        return CreateProcess(up, Priority.INTERACTIVE);
    }

    public static void KernelStart(CallType call){
        currentCall = call;
        kernel.start();
        if(kernel.getScheduler().currentProcess != null)
            kernel.getScheduler().currentProcess.stop();
    }
    // CreateProcess to be called by userland and passed onto kerneland
    public static int CreateProcess(UserLandProcess up, Priority p) {
        //System.out.println(up.getClass() + " process is being created in OS...");
        // Params cleared to get rid of old objects.
        parameters.clear();
        // New process & Priority is added to params to be ran.
        parameters.add(up);
        parameters.add(p);
        // CurrentCall set in order to run the kernel main.
        KernelStart(CallType.CREATE_PROCESS);
        //else
        //    System.out.println("Current Process is null");
        // While there's no return value, call sleep (to fix a startup bug)
        while(returnValue == null){
            try{
                Thread.sleep(10);
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        if(!(returnValue instanceof Integer))
            returnValue = -1;
        // pid is to be returned by the method, will be declared after returnValue isn't null.
        int pid = (int) returnValue;
        return pid;
    }
    // Startup method called first before the process is run.
    public static void Startup(UserLandProcess init) {
        System.out.println("OS is starting...");
        // kernel is instantiated & semaphore is released to start its thread.
        kernel = new Kernel();
        kernel.start();
        FakeFileSystem swapFile = new FakeFileSystem();
        swapFile.Open("swap");
        // Two new processes init and idle are created.
        CreateProcess(init, Priority.REAL_TIME);
        CreateProcess(new Idle());
    }
    // SwitchProcess is used in cases where the OS wants to end the userlandProcess, call the kernel, and
    // then the kernel stops itself to call a new UserLandProcess.
    public static void SwitchProcess(){
        System.out.println("SwitchProcess called in OS.");
        //kernel.getScheduler().currentProcess.stop();
        KernelStart(CallType.SWITCH_PROCESS);
    }
    // Exit is very similar to SwitchProcess, except that it exists out of a script when its completed.
    public static void exit(){
        System.out.println("Exit called in OS.");
        // Calltype set to EXIT_PROGRAM, kernel is started.
        KernelStart(CallType.EXIT_PROGRAM);
    }

    // Sleep method takes in the milliseconds, clears the params & adds the milliseconds to params.
    // It then sets currentCall to sleep and then starts the kernel.
    public static void Sleep(int milliseconds){
        System.out.println("Sleep called in OS.");
        parameters.clear();
        parameters.add(milliseconds);
        KernelStart(CallType.SLEEP);
    }

    // Open routes userlandProcess device open request to kernel.
    public static int Open(String filePath){
        System.out.println("Open called in OS");
        returnValue = null;
        parameters.clear();
        parameters.add(filePath);
        KernelStart(CallType.SLEEP);
        return (int) returnValue;
    }

    // Read routes userlandProcess read request to the kernel.
    public static byte[] Read(int id, int len){
        System.out.println("Read called in OS");
        returnValue = null;
        parameters.clear();
        parameters.add(id);
        parameters.add(len);
        KernelStart(CallType.READ);
        return (byte[]) returnValue;
    }

    // Close routes userlandProcess close to the kernel.
    public static void Close(int filePath){
        System.out.println("Close called in OS");
        parameters.clear();
        parameters.add(filePath);
        KernelStart(CallType.CLOSE);
    }

    // Write routes userlandProcess write request to the kernel
    public static int Write(int id, byte[] data){
        System.out.println("Write called in OS");
        returnValue = null;
        parameters.clear();
        parameters.add(id);
        parameters.add(data);
        KernelStart(CallType.WRITE);
        return (int) returnValue;
    }

    // Seek routes userlandProcess seek request to the kernel.
    public static void Seek(int id, int to){
        System.out.println("Seek called in OS");
        parameters.clear();
        parameters.add(id);
        parameters.add(to);
        KernelStart(CallType.SEEK);
    }

    public static int GetPid(){
        System.out.println("GetPID called in OS");
        KernelStart(CallType.GET_PID);
        return (int) returnValue;
    }

    public static int GetPidByName(String s){
        System.out.println("GetPidByName called in OS");
        parameters.clear();
        parameters.add(s);
        KernelStart(CallType.GET_PID_BY_NAME);
        return (int) returnValue;
    }

    /*
     * SendMessage() should use the copy constructor to make a copy of the original message. It should populate the sender’s pid.
     * */
    public static void SendMessage(KernelMessage km){
        System.out.println("SendMessage called in OS");
        parameters.clear();
        parameters.add(km);
        KernelStart(CallType.SEND_MESSAGE);
    }

    /*
     * To implement WaitForMessage, first check to see if the current process has a message; if so, take it off of the queue and return it.
     * If not, we are going to de-schedule ourselves (similar to what we did for Sleep() ) and add ourselves to a new data structure to hold
     * processes that are waiting. I used a HashMap of pidPCB; there are many other choices that one could use.
     * */
    public static KernelMessage WaitForMessage(){
        System.out.println("WaitForMessage called in OS");
        KernelStart(CallType.WAIT_FOR_MESSAGE);
        return (KernelMessage) returnValue;
    }
    // GetMapping used to update the TLB with the given virtualPageNumber.
    public static void GetMapping(int virtualPageNumber){
        parameters.clear();
        parameters.add(virtualPageNumber);
        KernelStart(CallType.GET_MAPPING);
    }

    // validateSize is a helper method to check if the inputs for AllocateMemory and FreeMemory are correct.
    private static boolean validateSize(int s){
        if((s % 1024) == 0)
            return true;
        return false;
    }
    // AllocateMemory checks to see if the input is valid, if not then return -1 and print a warning. Else run the method.
    public static int AllocateMemory(int size){
        if(!validateSize(size)){
            System.out.println("Invalid input for AllocateMemory! Please input a number divisible by 1024. Ex: OS.AllocateMemory(1024)");
            return (int) -1;
        }
        parameters.clear();
        parameters.add(size);
        KernelStart(CallType.ALLOCATE_MEMORY);
        return (int) returnValue;
    }

    // FreeMemory validates the inputs and then runs itself. If invalid then return false and print warning.
    public static boolean FreeMemory(int pointer, int size){
        if(!validateSize(pointer) || !validateSize(size)){
            System.out.println("Warning! Invalid call of FreeMemory! Please make sure all inputs are divisible by 1024! Ex: FreeMemory(0, 2048)");
            return false;
        }
        parameters.clear();
        parameters.add(pointer);
        parameters.add(size);
        KernelStart(CallType.FREE_MEMORY);
        return (boolean) returnValue;
    }


}