import java.util.ArrayList;
import java.util.LinkedList;

public class PCB{
    // nextPid determines the next id of the next pcb
    private static int nextPid = 0;
    // stores the pcb
    private int PID;
    // Storage of VFSIds coordinates
    private int[] VFSIds = new int[10];
    // up stores the userlandProcess
    private UserLandProcess up;
    // priority stores the pcb priority
    private OS.Priority priority;
    // demote used to demote the process.
    private int demote = 0;
    // sleepTime stores the sleep time to wake up
    private long sleepTime;
    // origTime used for debugging purposes
    private long origTime;
    private String name;
    private LinkedList<KernelMessage> messageList = new LinkedList<KernelMessage>();
    private boolean isWaitingForMessage = false;
    // The virtual page number will be the index into the array, the value in the array will be the physical page number.
    private VirtualToPhysicalMapping[] virtualToPhysicalMappings = new VirtualToPhysicalMapping[100];
    // PCB constructor initializes the pcb and sets up up and PID
    public PCB(UserLandProcess up){
        this.up = up;
        name = up.getClass().getSimpleName();
        PID = nextPid++;
        for(int i = 0; i < VFSIds.length; i++)
            VFSIds[i] = -1;
//        for(int i = 0; i < virtualPageNumbers.length; i++)
//            virtualPageNumbers[i] = -1;
    }

    // iSDone checks to see if up is finished
    public boolean isDone(){
        return up.isDone();
    }
    // start starts the pcb
    public void start(){
        up.start();
    }
    // stop stops the pcb, checks to see if stopped and is then stopped.
    public void stop() {
        up.stop();
    }
    // returns ulp
    public UserLandProcess getProcess(){
        return up;
    }
    // priority mutator
    public OS.Priority getPriority(){
        return priority;
    }

    public void setPriority(OS.Priority p){
        priority = p;
    }
    // pid mutator
    public int getPid(){
        return PID;
    }
    // demote adds one to demote
    public void demote(){
        demote++;
    }
    // demote mutator
    public int getDemote(){
        return demote;
    }
    // sets demote to 0
    public void resetDemote(){
        demote = 0;
    }
    // original time set
    public void setOrigTime(long origTime){
        this.origTime = origTime;
    }
    // original time mutator
    public long getOrigTime(){
        return origTime;
    }
    public void setSleepTime(long sleepTime){
        this.sleepTime = sleepTime;
    }
    // returns sleeptime
    public long getSleepTime(){
        return sleepTime;
    }
    public String getName(){return name;}

    public int[] getVFSIds() {
        return VFSIds;
    }
    public void setVFSIds(int[] VFSIds) {
        this.VFSIds = VFSIds;
    }

    public void appendToMessageList(KernelMessage km){
        messageList.add(km);
    }

    public boolean isNotOccupied(){
        return messageList.isEmpty();
    }
    public KernelMessage popMessageList(){
        return messageList.pop();
    }
    public boolean isWaitingForMessage() {
        return isWaitingForMessage;
    }

    public void setWaitingForMessage(boolean waitingForMessage) {
        isWaitingForMessage = waitingForMessage;
    }
    // addPhysicalAddress adds to the next available virtualPageNumber.
    public int addPhysicalAddress(int PhysicalAddress){
        int i = 0;
        while(virtualToPhysicalMappings[i] != null)
            i++;
        virtualToPhysicalMappings[i].PhysicalPageNumber = PhysicalAddress;
        return i;
    }

    // Getter/setter for virtualPageNumbers
    public VirtualToPhysicalMapping[] getVirtualToPhysicalMappings() {
        return virtualToPhysicalMappings;
    }

    public void setVirtualPageNumbers(VirtualToPhysicalMapping[] vpms) {
        this.virtualToPhysicalMappings = vpms;
    }
}


