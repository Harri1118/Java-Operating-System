import java.util.HashMap;

public class Kernel extends Process implements Device{
    // Scheduler member created in kernel.
    private Scheduler scheduler = new Scheduler();
    private static boolean[] freePages = new boolean[1024];
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
                    //clearBlocks();
                    exitProcessForMemory();
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

    // AllocateMemory sets true to the next size pages from an index and returns the virtualAddress within the currentProcesses physical addresses.
    public int AllocateMemory(int size){
        int pagesToAlloc = size/1024;
        int i = 0;
        // First add each page to mappings
        VirtualToPhysicalMapping[] mappings = scheduler.currentProcess.getVirtualToPhysicalMappings();
        while(i < pagesToAlloc){
            mappings[i] = new VirtualToPhysicalMapping();
            i++;
        }
        scheduler.currentProcess.setVirtualToPhysicalMappings(mappings);
        int fin = 0;
        // Then find the next available page WITHOUT changing any of the booleans.
        while(freePages[fin] == true && fin < 1023)
            fin++;
        // Return the next available page
        return fin * 1024;
    }
    // Clear block clears a singular block in memory.
    public void clearBlock(int addr, int page){
        int i = 0;
        while(i < 1024){
            Hardware.nullifyMemoryDirectly(page, addr + i);
            i++;
        }
    }
    // FreeMemory frees a pointer of size s within memory.
    public boolean FreeMemory(int pointer, int size){
        // get pointer, size, end of which the pointer will free up to.
        int pStart = pointer / 1024;
        int pSize = size / 1024;
        int pEnd = pStart + pSize;
        // get mappings
        VirtualToPhysicalMapping[] mappings = scheduler.currentProcess.getVirtualToPhysicalMappings();
        // iterate up to pEnd, clearing each block and freeing up the pages.
        while(pStart < pEnd){
            if(mappings[pStart].PhysicalPageNumber != -1)
                clearBlock(mappings[pStart].PhysicalPageNumber, pointer);
            mappings[pStart] = null;
            if(!freePages[pStart])
                return false;
            freePages[pStart] = false;
            pStart ++;
        }
        scheduler.currentProcess.setVirtualToPhysicalMappings(mappings);
        return false;
    }
    // Checks to see if all the pages are taken.
    private boolean allPagesAreTaken(){
        for(int i = 0; i < freePages.length; i++){
            if(!freePages[i])
                return false;
        }
        return true;
    }
    // Returns the next available free page.
    private int findNextFreePage(){
        int i = 0;
        while(freePages[i])
            i++;
        if(i > freePages.length-1)
            return -1;
        return i;
    }
    // Send a page's physical memory to storage.
    private int sendPageToStorage(PCB process, int index){
        // Get mappings of of index.
        VirtualToPhysicalMapping[] mappings = process.getVirtualToPhysicalMappings();
        VirtualToPhysicalMapping mapping = mappings[index];
        // generate all the data from the page and record all the characters from memory while also nullifying evertything.
        byte[] page = new byte[1024];
        for(int i = 0; i < 1024; i++){
            byte b = Hardware.getMemoryDirectly(mapping.PhysicalPageNumber, index+i);
            Hardware.nullifyMemoryDirectly(mapping.PhysicalPageNumber, index+i);
            page[i] = b;
        }
        // Get the size of everything written to swapFile
        int addSize = OS.swapFile.Write(0, page);
        // Record nextPage as the return value
        int fin = OS.nextPage;
        // add size to finPage to record the next iteration.
        OS.nextPage += addSize;
        return fin;
    }

    // sendToPysical sends data from storage back to memory.
    private void sendToPhysical(int index){
        // get mappings from current process,
        VirtualToPhysicalMapping[] mappings = scheduler.currentProcess.getVirtualToPhysicalMappings();
        // get read from file onto byte of size 1024
        int readAddress = mappings[index].DiskPageNumber;
        // Retrieve page from swapFile, then set the memory directly to memory within hardware.
        byte[] page = OS.swapFile.Read(readAddress, 1024);
        for(int i = 0; i < 1024; i++)
            Hardware.setMemoryDirectly(Hardware.generatePhysicalAddress(mappings[index].PhysicalPageNumber, index+i), page[i]);
    }

    /*
    GetMapping is inputted the virtualPageNumber which then updates the current Processes virtual nums as well as the Hardware's TLB. This is used for validating the TLB
    in the case that hardware cannot find the proper mapping for a virtual/physical address.
    */
    public void GetMapping(int virtualPageNumber) throws Exception {
            // Get the virtualPageNumbers from the currentProcess
            VirtualToPhysicalMapping[] virtualMappings = scheduler.currentProcess.getVirtualToPhysicalMappings();
            //int nextMapping = nextAvailableMapping(virtualMappings);
            int listIndex = virtualPageNumber % 100;
            if(virtualMappings[listIndex] == null)
                throw new Exception("Segmentation fault");
            if(virtualMappings[listIndex].DiskPageNumber != -1){
                // get random process
                PCB victimProcess = scheduler.getRandomProcess();
                // if physical mappings on data, then write it to the random process.disk
                if(virtualMappings[listIndex].PhysicalPageNumber != -1)
                    sendPageToStorage(victimProcess, listIndex);
                // set currentProcess.disk to currentProcess.physical
                sendToPhysical(listIndex);
            }
            else if((virtualPageNumber > freePages.length-1 || allPagesAreTaken()) && virtualMappings[listIndex].PhysicalPageNumber == -1){
                // Get random process
                PCB victimProcess = scheduler.getRandomProcess();
                // get virtualmappings[listIndex]
                VirtualToPhysicalMapping[] victimMappings = victimProcess.getVirtualToPhysicalMappings();
                // Write to disk the whole page, and remove it from memory.
                int diskAddress = sendPageToStorage(victimProcess, listIndex);
                // Set disk number  of virtualMappings[listIndex].diskPage to nextPage, iterate nextPage
                victimMappings[listIndex].DiskPageNumber = diskAddress;
                // Set currentProcess physical address to virtualMappings[listIndex].physical address
                int evictedPhysicalPage = victimMappings[listIndex].PhysicalPageNumber;
                // Set victimMappings as -1 since its now on disk.
                victimMappings[listIndex].PhysicalPageNumber = -1;
                // Hijack the physicalPagenumber for current process.
                virtualMappings[listIndex].PhysicalPageNumber = evictedPhysicalPage;
                // set everything else, and then set the freePage to false.
                victimProcess.setVirtualToPhysicalMappings(victimMappings);
                scheduler.currentProcess.setVirtualToPhysicalMappings(virtualMappings);
                freePages[listIndex] = false;
            }
            // If still -1, then find the next free page and set it to true.
            if (virtualMappings[listIndex].PhysicalPageNumber == -1) {
                int nextFreePage = findNextFreePage();
                if(nextFreePage == -1)
                    throw new Exception("next free page is -1!");
                freePages[nextFreePage] = true;
                virtualMappings[listIndex].PhysicalPageNumber = nextFreePage;
            }

            scheduler.currentProcess.setVirtualToPhysicalMappings(virtualMappings);
            //else if(virtualPageNumbers[virtualPageNumber].PhysicalPageNumber == -1 && freePages[virtualPageNumber] == true){}
            // make random num 1-10
            int randNum = (int) (Math.random() * 10) + 1;
            // Get the TLB
            int[][] TLB = Hardware.getTLB();
            // if less than 5, set TLB[0][0] to virtualPageNumber and TLB[0][1] to PCB[VirtualPageNumber]
            if (randNum < 5) {
                TLB[0][0] = virtualPageNumber;
                TLB[0][1] = virtualMappings[listIndex].PhysicalPageNumber;
            }
            // If more than 5, set TLB[1][0] to virtualPageNumber, and TLB[1][1] to PCB[VirtualPageNumber]
            else if (randNum >= 5) {
                TLB[1][0] = virtualPageNumber;
                TLB[1][1] = virtualMappings[listIndex].PhysicalPageNumber;
            }
            // Set the TLB to Hardware
            Hardware.setTLB(TLB);
    }

    // nullifies all the processes entires in memory.
    public void exitProcessForMemory(){
        // get virtualMappings
        VirtualToPhysicalMapping[] mappings = scheduler.currentProcess.getVirtualToPhysicalMappings();
        // clear all the mappings from memory
        for(int i = 0; i < mappings.length; i++)
            if(mappings[i] != null)
                clearBlock(0, mappings[i].PhysicalPageNumber);
    }
}