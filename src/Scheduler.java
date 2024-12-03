import java.time.Clock;
import java.util.*;

public class Scheduler {
    // PidMap adds process to hashMap to be used for finding PIDs quickly from userland.
    HashMap<Integer, PCB> PidMap = new HashMap<Integer, PCB>();
    // WaitMap adds processes to Hashmap to be used for adding and removing processes from queues.
    HashMap<Integer, PCB> WaitMap = new HashMap<Integer, PCB>();
    // sleepingProcesses stores all the processes which are in sleep mode.
    private LinkedList<PCB> sleepingProcesses;
    // realTimeProcesses, interactiveProcesses, and backGroundProcesses contains each process corresponding to their priority.
    private LinkedList<PCB> realTimeProcesses;
    private LinkedList<PCB> interactiveProcesses;
    private LinkedList<PCB> backGroundProcesses;
    // Timer used to regulate the processes being currently run.
    private Timer timer;
    // Clock used to store processes which will be used towake processes.
    private Clock clock = Clock.systemUTC();
    // currentProcess is the current process that the kernel will start.
    public PCB currentProcess;
    // constructor schedules the currentProcess to the stopped at a fixed rate. This is to
    // regulate the rate at which the scheduler regulates the tasks. allocated to it.
    public Scheduler(){
        //System.out.println("Scheduler is being created...");
        // processes declared as a new LinkedList.
        sleepingProcesses = new LinkedList<>();
        realTimeProcesses = new LinkedList<>();
        interactiveProcesses = new LinkedList<>();
        backGroundProcesses = new LinkedList<>();
        // timer is declared as well as currentProcess.
        timer = new Timer();
        currentProcess = null;
        // timertask is given to timer in order to requestStop on
        // currentProcess every time it isn't null.
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (currentProcess != null) {
                    currentProcess.getProcess().requestStop();
                }
            }
        }, 0, 250);
    }
    // CreateProcess takes a userland process, adds it to the list of corresponding processes of the same priority, then
    // calls SwitchProcess if the currentProcess is null. It will then return the PID of the
    // process being run. Adds the process to a PID map.
    public int CreateProcess(PCB p){
        //System.out.println(p.getProcess().getClass() + " process is being created in the scheduler...");
        if(p.getPriority() == OS.Priority.REAL_TIME)
            realTimeProcesses.add(p);
        else if(p.getPriority() == OS.Priority.INTERACTIVE)
            interactiveProcesses.add(p);
        else
            backGroundProcesses.add(p);
        if(currentProcess == null){
            SwitchProcess();
        }
        //System.out.println("CreateProcess (Scheduler) Complete! Process #" + p.getPid() + " of " + p.getProcess().getClass() + " added to scheduler.");
        PidMap.put(p.getPid(), p);
        return p.getPid();
    }
    // SwitchProcess checks to see if the currentProcess isn't null & isn't finished processing. If so then demote the current process,
    // demotes the currentProcess if it's been demoted at least 5 times, and then adds the process to its corresponding priority list.
    // Finally, it checks to see if the sleepingProcess is empty. If it's not empty then awake any sleeping processes. After that,
    // change the current process.
    public void SwitchProcess(){
        //System.out.println("SwitchProcess being called in scheduler.");
        //printAllLists();
        if(currentProcess != null){
            if(!currentProcess.isDone()){
                currentProcess.demote();
                checkDemote();
            }
            if(currentProcess.getPriority() == OS.Priority.REAL_TIME)
                realTimeProcesses.add(currentProcess);
            else if(currentProcess.getPriority() == OS.Priority.INTERACTIVE)
                interactiveProcesses.add(currentProcess);
            else
                backGroundProcesses.add(currentProcess);
        }
        if(!sleepingProcesses.isEmpty())
            awakeSleepingProcesses();
        ProcessChange();
        //System.out.println("SwitchProcess complete!");
        //printAllLists();
    }

    // ExitProcess calls ProcessChange, and doesn't queue the current Process. Removes process from the PID map.
    public void ExitProcess(){
        PidMap.remove(currentProcess.getPid());
        //System.out.println("ExitProcess (scheduler) called!");
        if(!realTimeProcesses.isEmpty() || !interactiveProcesses.isEmpty() || !backGroundProcesses.isEmpty())
            ProcessChange();
        //System.out.println("ExitProcess (scheduler) complete!");
    }

    // Sleep take miliseconds from kernel, gets current milliseconds and adds milliseconds to it and sets the currentProcess
    // to be in sleepingProcess until after the time allocated. Sleep then calls ProcessChange()
    public void Sleep(int milliseconds){
        //System.out.println("Sleep method (scheduler) called!");
        long currentMs = clock.millis();
        long addedMs = currentMs + milliseconds;
        sleepingProcesses.add(currentProcess);
        currentProcess.setSleepTime(addedMs);
        currentProcess.setOrigTime(currentMs);
        //System.out.println("Added " + milliseconds + " to " + currentMs + " in " + " Current process#" + currentProcess.getPid() + " class: " + currentProcess.getProcess().getClass() + "(" + addedMs + ")");
        ProcessChange();
        //System.out.println("Sleep (Scheduler) complete!");
    }

    // ProcessChange, depending on the quantity of the lists, makes a problabistic model which generates a number between 1 and 10 repeatidly until
    // currentProcess has been changed due to one of the lists popping out a process to run.
    public void ProcessChange(){
        //System.out.println("ProcessChange called!");
        //printAllLists();
        Random rand = new Random();
        currentProcess = null;

        if(!realTimeProcesses.isEmpty() || interactiveProcesses.isEmpty()) {
            while(true) {
                int num = rand.nextInt(10) + 1;
                if (num < 7 && !realTimeProcesses.isEmpty()){
                    currentProcess = realTimeProcesses.pop();
                    break;
                }
                else if (num > 7 && num < 10 && !interactiveProcesses.isEmpty()){
                    currentProcess = interactiveProcesses.pop();
                    break;
                }
                else if (!backGroundProcesses.isEmpty()){
                    currentProcess = backGroundProcesses.pop();
                    break;
                }
                num = rand.nextInt(10) + 1;
                //System.out.println(num);
            }
        }
        else{
            while(true){
                double num = rand.nextDouble(10) + 1;
                if(num < 7.5 && !interactiveProcesses.isEmpty()){
                    currentProcess = interactiveProcesses.pop();
                    break;
                }
                else if(num > 7.5 && !backGroundProcesses.isEmpty()){
                    currentProcess = backGroundProcesses.pop();
                    break;
                }
                num = rand.nextDouble(10) + 1;
                //System.out.println(num);
            }
        }
        if(currentProcess.isWaitingForMessage()){
            OS.returnValue = currentProcess.popMessageList();
            currentProcess.setWaitingForMessage(false);
        }
        //System.out.println(currentProcess.toString());
        //System.out.println(currentProcess.getPid() + ", " +currentProcess.getName());
        //System.out.println("ProcessChange complete!");
        //printAllLists();
    }
    // Check demote sees if the currentProcess is able to be demoted, will demote it and will put it down a priority in the queue.
    public void checkDemote(){
        //System.out.println("checkDemote called!");
        //System.out.println("Current Processs demote value: " + currentProcess.getDemote());
        if(currentProcess.getDemote() >= 5){
            OS.Priority p = currentProcess.getPriority();
            OS.Priority p2;
            if(currentProcess.getPriority() == OS.Priority.REAL_TIME){
                currentProcess.setPriority(OS.Priority.INTERACTIVE);
                p2 = OS.Priority.INTERACTIVE;
            }
            else if(currentProcess.getPriority() == OS.Priority.INTERACTIVE){
                currentProcess.setPriority(OS.Priority.BACKGROUND);
                p2 = OS.Priority.BACKGROUND;
            }
            else
                p2 = OS.Priority.BACKGROUND;
            //System.out.println("Process #" + currentProcess.getPid() + " demoted from " + p + " to " + p2 + ".");
            currentProcess.resetDemote();
        }
        //System.out.println("checkDemote complete!");
    }
    // awakeSleepingProcesses gets current milliseconds, parses through sleepingProcesses and checks to see if currentMillis is more than the sleeping process.
    // It will then add the process to its corresponding queue if the test passes and removed the process from the sleeping queue.
    public void awakeSleepingProcesses(){
        //System.out.println("awakeSleepingProcesses called!");
        //printAllLists();
        for(PCB p : sleepingProcesses){
            long currentMillis = clock.millis();
            if(currentMillis > p.getSleepTime()){
                if(p.getPriority() == OS.Priority.REAL_TIME)
                    realTimeProcesses.add(p);
                else if(p.getPriority() == OS.Priority.INTERACTIVE)
                    interactiveProcesses.add(p);
                else
                    backGroundProcesses.add(p);
                sleepingProcesses.remove(p);
                //System.out.println("Process #" + p.getPid() + ", class " + p.getProcess().getClass() + " of has been awoken after " + (currentMillis - p.getOrigTime()) +" milliseconds!");
            }
        }
        //System.out.println("awakeSleepingProcesses complete!");
        //printAllLists();
    }

    // returns the current process’ pid
    public int GetPid(){
        return currentProcess.getPid();
    }


    // returns the pid of a process with that name.
    public int GetPidByName(String s){
        for (int entry : PidMap.keySet()){
            PCB e = PidMap.get(entry);
            if(e.getName().equals(s))
                return e.getPid();
        }
        return -1;
    }

    /*
     * Adds km to the sender PID, then adds km to the receiver's PID. It then checks to see if the target's in the waitMap and adds it to its
     * corresponding priority queue. It then removes the process of the target from the waitlist.
     * */
    public void SendMessage(KernelMessage km){
        // populate sender's message queue
        PCB sender = PidMap.get(km.getSenderPid());
        sender.appendToMessageList(km);
        KernelMessage km2 = new KernelMessage(km);
        // populate target's kernelMessage
        PCB target = PidMap.get(km2.getTargetPid());
        target.appendToMessageList(km2);
        // if in target list, restore it to the queue?
        if(WaitMap.containsKey(target.getPid())){
            if(target.getPriority() == OS.Priority.REAL_TIME)
                realTimeProcesses.add(target);
            else if(target.getPriority() == OS.Priority.INTERACTIVE)
                interactiveProcesses.add(target);
            else
                backGroundProcesses.add(target);
            WaitMap.remove(target.getPid());
        }

    }

    // Waits for message and returns the popped process from the currentProcess. If not it will put the currentProcess in the WaitMap and return null.
    public KernelMessage WaitForMessage(){
        if(!currentProcess.isNotOccupied())
            return currentProcess.popMessageList();
        WaitMap.put(currentProcess.getPid(), currentProcess);
        currentProcess.setWaitingForMessage(true);
        ProcessChange();
        return null;
    }

    // Method to check the mininum about of bytes needed for currentProcess to run properly.
    private int checkRequiredInts(){
        VirtualToPhysicalMapping[] mappings = currentProcess.getVirtualToPhysicalMappings();
        for(int i = 0; i < mappings.length; i++){
            if(mappings[i] == null)
                return i;
        }
        return mappings.length;
    }

    // goes through list, finds a random process suitable for getRandomProcess to return.
    private PCB getSuitableProcess(LinkedList<PCB> list){
        // Iterate through list
        int i = 0;
        int MIN = checkRequiredInts()-1;
        LinkedList<PCB> finList = new LinkedList<PCB>();
        while(i < list.size()){
            // retrieve mappings, check while null if it can hold all of currentProcess' data.
            VirtualToPhysicalMapping[] mappings = list.get(i).getVirtualToPhysicalMappings();
            int n = 0;
            while((mappings[n]) != null && (n < mappings.length-1))
                n++;
            // if the minimum is less than n, add i to finList.
            if(MIN <= n)
                finList.add(list.get(i));
            i++;
        }
        // get the random pages with mappings, return a random pcb of this pool.
        if(finList.size() > 0){
            Random rand = new Random();
            int j =  rand.nextInt(finList.size());
            return finList.get(j);
        }
        else
            return null;
    }

    // getRandomProcess gets a random process from a  random list which has a suitable PCB for a swap.
    public PCB getRandomProcess(){
        Random rand = new Random();
        PCB finPCB = null;
        while(true){
            int randNum = rand.nextInt(2);
            if(randNum == 0 && !backGroundProcesses.isEmpty())
                finPCB = getSuitableProcess(backGroundProcesses);
            else if(randNum == 1 && !interactiveProcesses.isEmpty())
                finPCB = getSuitableProcess(interactiveProcesses);
            else if(randNum == 2 && !realTimeProcesses.isEmpty())
                finPCB = getSuitableProcess(backGroundProcesses);
            if(finPCB != null)
                return finPCB;
        }
    }
    // PrintAllLists made to debug the lists and currentProcess in the scheduler.
    public void printAllLists(){
        System.out.println("---------------------------------------------------------------");
        System.out.println("Processes data:");
        if(currentProcess != null)
            System.out.println("Current Process: #" + currentProcess.getPid() + ", class: " + currentProcess.getProcess().getClass()+", priority: " + currentProcess.getPriority());
        System.out.println("Real time Processes:");
        for(PCB p : realTimeProcesses)
            System.out.println("Process: #" + p.getPid() + ", class: " + p.getProcess().getClass()+", priority: " + p.getPriority());
        System.out.println("Interactive processes:");
        for(PCB p : interactiveProcesses)
            System.out.println("Process: #" + p.getPid() + ", class: " + p.getProcess().getClass()+", priority: " + p.getPriority());
        System.out.println("Background processes:");
        for(PCB p : backGroundProcesses)
            System.out.println("Process: #" + p.getPid() + ", class: " + p.getProcess().getClass()+", priority: " + p.getPriority());
        System.out.println("Sleeping processes:");
        for(PCB p : sleepingProcesses)
            System.out.println("Process: #" + p.getPid() + ", class: " + p.getProcess().getClass()+", priority: " + p.getPriority());
        System.out.println("---------------------------------------------------------------");
    }
}
