public class Kernel extends Process implements Device{
    // Scheduler member created in kernel.
    private Scheduler scheduler = new Scheduler();
    private boolean[] freePages = new boolean[1024];
    private Hardware hardware = new Hardware();
    private VFS vfs = new VFS();
    @Override
    public void main(){
        // Infinite while loop to call the switch cases.
        while(true) {
            // Kernel stops before the current process is changed and then started.
            stop();
            //System.out.println("Kernel initiated");
            // currentCall is switched depending on cases.
            switch (OS.currentCall) {
                // On CREATE_PROCESS, the OS.returnValue is changed when the scheduler creates a new process from the first OS parameter.
                case CREATE_PROCESS:
                    try {
                        PCB newProcess = new PCB((UserLandProcess) OS.parameters.get(0));
                        newProcess.setPriority( (OS.Priority) OS.parameters.get(1));
                        OS.returnValue = (int) scheduler.CreateProcess(newProcess);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                // On SWITCH_PROCESS, it calls SwitchProcess within the scheduler. It clears the TLB as well.
                case SWITCH_PROCESS:
                    hardware.ClearTLB();
                    scheduler.SwitchProcess();
                    break;
                // On EXIT_PROGRAM, it calls ExitProcess within the scheduler. vfs then closes & clears its coordinates. It also clears all the virtual pages
                case EXIT_PROGRAM:
                    if(checkForMemoryLeak())
                        System.out.println("Memory leak detected!");
                    clearBlocks();
                    scheduler.ExitProcess();
                    vfs.clear();
                    break;
                // Sleep calls scheduler.sleep with the first OS parameter argument.
                case SLEEP:
                    scheduler.Sleep((int) OS.parameters.get(0));
                    break;
                // Open returns the vfs coordinate for the currentProcess to input.
                case OPEN:
                    OS.returnValue = Open( (String) OS.parameters.get(0));
                    break;
                // close Closes the VFS process mapped to the current process.
                case CLOSE:
                    Close((int) OS.parameters.get(0));
                    break;
                // read returns to OS the return value of the VFS.
                case READ:
                    OS.returnValue = Read((int) OS.parameters.get(0), (int) OS.parameters.get(1));
                    break;
                // write returns the return value of write configured by the vfs from the current process
                case WRITE:
                    OS.returnValue = Write((int) OS.parameters.get(0), (byte[]) OS.parameters.get(1));
                    break;
                // seek calls the VFS coordinates to seek within their devices from the current process.
                case SEEK:
                    Seek((int) OS.parameters.get(0), (int) OS.parameters.get(1));
                    break;
                case GET_PID:
                    OS.returnValue = scheduler.GetPid();
                    break;
                case GET_PID_BY_NAME:
                    OS.returnValue = scheduler.GetPidByName((String) OS.parameters.get(0));
                    break;
                case SEND_MESSAGE:
                    scheduler.SendMessage((KernelMessage) OS.parameters.get(0));
                    break;
                case WAIT_FOR_MESSAGE:
                    OS.returnValue = scheduler.WaitForMessage();
                    break;
                case GET_MAPPING:
                    // try/catch getMapping on the case it has a segmentation fault error.
                    try{
                        GetMapping((int) OS.parameters.get(0));
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    break;
                case ALLOCATE_MEMORY:
                    OS.returnValue = AllocateMemory((int) OS.parameters.get(0));
                    break;
                case FREE_MEMORY:
                    OS.returnValue = FreeMemory((int) OS.parameters.get(0), (int) OS.parameters.get(1));
                    break;
            }

            scheduler.currentProcess.start();

        }
    }

    // Returns the scheduler for OS use.
    public Scheduler getScheduler(){
        return scheduler;
    }

    /*
     * Open iterates through the VFS Virtual Filememberslist, finds the next available opening for a
     * mapping and then returns the index of which the available slot was it back towards the current PCB's internal device mapping list.
     * Open is called on the device associated with the string, and the device's open return value is stored within the VirtualFileMember object.
     * */
    @Override
    public int Open(String s) {
        int[] VFSIds = scheduler.currentProcess.getVFSIds();
        int VFSIndex = 0;
        /*for(int i = 0; i < vfs.getMembers().length; i++){
            if(vfs.getMembers()[i] != null)
                System.out.println("vfs member id: " + vfs.getMembers()[i].getId() + " of device " + vfs.getMembers()[i].getDevice().getClass());
        }*/
        boolean isFound = false;
        for(int i = 0; i < VFSIds.length; i++){
            if(VFSIds[i] == -1){
                VFSIndex = i;
                isFound = true;
                break;
            }
        }
        if(isFound == false)
            return -1;
        int returnVal = vfs.Open(s);
        VFSIds[VFSIndex] = returnVal;
        scheduler.currentProcess.setVFSIds(VFSIds);
        return VFSIndex;
    }

    /*
     * Close gets the id, gores into the vfs system and closes/nullifies the vfs id within the vfs array. Close is also called within the
     * device that's inside the VirtualFileMember at its specific VirtualFileMember.id index.
     * */
    @Override
    public void Close(int id) {
        int idToClose = scheduler.currentProcess.getVFSIds()[id];
        scheduler.currentProcess.getVFSIds()[id] = -1;
        System.out.println(idToClose);
        vfs.Close(idToClose);
    }

    // Read finds the vfsId and returns a byte[] of size to the userLandProcess from the device.
    @Override
    public byte[] Read(int id, int size) {
        return vfs.Read(scheduler.currentProcess.getVFSIds()[id], size);
    }

    // Seek calls the Seek method within the device, mapped from the vfs.
    @Override
    public void Seek(int id, int to) {
        vfs.Seek(scheduler.currentProcess.getVFSIds()[id], to);
    }

    // Write adds data to the vfs coordinate, the locally stored device then returns the size of the data as confirmation
    // that the data has been written.
    @Override
    public int Write(int id, byte[] data) {
        return vfs.Write(scheduler.currentProcess.getVFSIds()[id], data);
    }

    // checkAvailableBlocks is a quick helper method which returns false if the next 'size' pages from 'index' are free to be allocated or not. If not then return false,
    // otherwise return true.
    public boolean checkAvailableBlocks(int index, int size){
        int endBlock = index + size;
        for(int i  = index; i < endBlock; i++){
            if(freePages[i])
                return false;
        }
        return true;
    }
    // AllocateMemory sets true to the next size pages from an index and returns the virtualAddress within the currentProcesses physical addresses.
    public int AllocateMemory(int size){
        // Fin = -1
        int fin = -1;
        // i is used to find the virtual Address
        int i = 0;
        // pagesToAlloc calculates the amount of pages needed to be added.
        int pagesToAlloc = (size/1024);
        // Check for available memory blocks, add to i if any of the next (size/1024) pages are set to 'true'
        while(!checkAvailableBlocks(i, pagesToAlloc))
            i++;
        fin = i;
        // Find the endAddress, and add the start address with the amount of pages desired to be allocated together
        int endAddress = i + pagesToAlloc;
        VirtualToPhysicalMapping[] mappings = scheduler.currentProcess.getVirtualToPhysicalMappings();
        // While i is less than endAddress, set each block between the two nums and add the physicalAddress of i.
        while(i < endAddress){
            freePages[i] = true;
            mappings[i] = new VirtualToPhysicalMapping();
            if(i == fin)
                mappings[i].PhysicalPageNumber = i;
            i++;
        }
        // Get current processes list
        // check if the next s pages from pStart are null.
        // If they're all null, set all of the instances to new.
        // Return fin as a multiple of 1024, since it represents the virtual address within memory.
        return fin * 1024;
    }
    // FreeMemory gets a pointer and frees the next s sizes from the pages list.
    public boolean FreeMemory(int pointer, int size){
        //System.out.println("Pointer: " + pointer);
        // pStart represents the page # of which the memory block is on.
        int pStart = pointer/1024;
        // pEnd is a representation of the end address of which needs to be cleared all the way from pStart
        int pEnd = pStart + (size/1024);
        //System.out.println("pEnd: " + pEnd);
        VirtualToPhysicalMapping[] VPNums = scheduler.currentProcess.getVirtualToPhysicalMappings();
        // from pStart to pEnd, free all the blocks inbetween
        while(pStart < pEnd){
            if(VPNums[pStart].PhysicalPageNumber != -1){
                // What do I do?
            }
            // if any are are false, throw exception
            if(!freePages[pStart])
                return false;
            // set the page at freePages to false
            freePages[pStart] = false;
            // set the index pStart and VPNums to -1
            VPNums[pStart] = null;

            pStart ++;
        }
        scheduler.currentProcess.setVirtualPageNumbers(VPNums);
        return true;
    }

    private boolean allPagesAreTaken(){
        return false;
    }
    /*
    GetMapping is inputted the virtualPageNumber which then updates the current Processes virtual nums as well as the Hardware's TLB. This is used for validating the TLB
    in the case that hardware cannot find the proper mapping for a virtual/physical address.
    */
    public void GetMapping(int virtualPageNumber) throws Exception {
        try{
            // Get the virtualPageNumbers from the currentProcess
            VirtualToPhysicalMapping[] virtualPageNumbers = scheduler.currentProcess.getVirtualToPhysicalMappings();
            // If the virtualPagenumber's address is -1, throw a seg fault
            if(virtualPageNumbers[virtualPageNumber] == null)
                throw new Exception("Segmentation fault");
            if(virtualPageNumbers[virtualPageNumber].PhysicalPageNumber == -1){
                if(virtualPageNumbers[virtualPageNumber].DiskPageNumber != -1){
                    // set the diskPageNumber to physcial number?

                }
                else{

                }
            }
            // make random num 1-10
            int randNum = (int) (Math.random() * 10) + 1;
            // Get the TLB
            int[][] TLB = Hardware.getTLB();
            // if less than 5, set TLB[0][0] to virtualPageNumber and TLB[0][1] to PCB[VirtualPageNumber]
            if (randNum < 5) {
                TLB[0][0] = virtualPageNumber;
                TLB[0][1] = virtualPageNumbers[virtualPageNumber].PhysicalPageNumber;
            }
            // If more than 5, set TLB[1][0] to virtualPageNumber, and TLB[1][1] to PCB[VirtualPageNumber]
            else if (randNum >= 5) {
                TLB[1][0] = virtualPageNumber;
                TLB[1][1] = virtualPageNumbers[virtualPageNumber].PhysicalPageNumber;
            }
            // Set the TLB to Hardware
            Hardware.setTLB(TLB);
        } catch(Exception e){
            // In a case of an error throw a seg fault, clear the blocks.
            System.out.println("Segmentation fault");
            clearBlocks();
            scheduler.SwitchProcess();
        }

    }

    // clearBlocks frees all of the pages within the kernel.
    public void clearBlocks(){
        for(int i = 0; i < freePages.length; i++)
            freePages[i] = false;
    }

    // checkForMemoryLeak makes sure that every boolean within pages is false.
    private boolean checkForMemoryLeak(){
        for(int i  = 0; i < freePages.length; i++){
            if(freePages[i])
                return true;
        }
        return false;
    }
}
