import java.util.HashMap;

public class Hardware {
    // Memory size used for various methods.
    private static final int MEMORY_SIZE = (int) Math.pow(1024,2);
    // Memory is used as a RAM-like device
    private static byte[] Memory = new byte[MEMORY_SIZE];
    // TLB is to store virtual -> physical addresses
    private static int[][] TLB = new int[2][2];
    // Hardware declared, TLB is populated
    public Hardware(){
        for(int i = 0; i < TLB.length; i++){
            for(int j = 0; j < TLB[i].length;  j++)
                TLB[i][j] = -1;
        }
    }
    // Read calculates the page offset, physical page, and the physical address and reads the character within memory. Will return -1 if address is outside Memory
    public static byte Read(int address){
        int PhysicalPage = getPhysicalPage(address);
        if(PhysicalPage != -1 || PhysicalPage < MEMORY_SIZE){
        int PhysicalAddress = generatePhysicalAddress(PhysicalPage, address);
        return Memory[PhysicalAddress];
        }
        else{
            System.out.println("Error reading from address " + address);
            return -1;
        }
    }
    // Write calculates the PageOffset, physical page, and Physical address which is then populated in Memory. Will fail to write if PhysicalPage is invalid.
    public static void Write(int address, byte value){
        int PhysicalPage = getPhysicalPage(address);
        if(PhysicalPage != -1 || PhysicalPage > MEMORY_SIZE) {
            int PhysicalAddress = generatePhysicalAddress(PhysicalPage, address);
            Memory[PhysicalAddress] = value;
        }
        else{
            System.out.println("Error writing to address " + address);
        }
    }

    // Helper method that checks the TLB and then calculates the physical page from the mapping. Will throw and exception if the physical page is -1
    private static int getPhysicalPage(int address){
        // Get virtualPage from input
        int VirtualPage = address/1024;
        // Initialize PhysicalPage
        int PhysicalPage = -1;
        // Check if the TLB has the address within its first row, if not then call GetMapping
        if(TLB[0][0] != address && TLB[1][0] != address)
            OS.GetMapping(VirtualPage);
        // If the virtualPage is within the TLB, then set the physicalPage to the index within the TLB's first row.
        for(int i = 0; i < TLB.length; i++){
            if(VirtualPage == TLB[i][0]) {
                PhysicalPage = TLB[i][1];
                break;
            }
        }
        // address = 1047552 causes error.
        // if PhysicalPage == -1, then throw an error
        if(PhysicalPage == -1){
            try {
                throw new Exception();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Return PhysicalPage otherwise
        return PhysicalPage;
    }

    // Getter/setter for TLB
    public static int[][] getTLB() {
        return TLB;
    }

    public static void setTLB(int[][] TLB) {
        Hardware.TLB = TLB;
    }

    // TLB Cleared.
    public void ClearTLB(){
        for(int i = 0; i < TLB.length; i++){
            for(int n = 0; n < TLB[0].length; n++)
                TLB[i][n] = -1;
        }
    }

    // Returns byte directly from memory.
    public static byte getMemoryDirectly(int pa, int addr){
        return Memory[generatePhysicalAddress(pa, addr)];
    }

    // Sets memory directly
    public static void setMemoryDirectly(int pa, byte i){
        Memory[pa] = i;
    }

    // nullifies memory directly.
    public static void nullifyMemoryDirectly(int pa, int addr){
        Memory[generatePhysicalAddress(pa, addr)] = 0;
    }

    // Automatically generates a physical address.
    public static int generatePhysicalAddress(int pa, int addr){
        int pageOffset = addr % 1024;
        int PhysicalAddress = (pa * 1024) + pageOffset;
        if(PhysicalAddress < 0){
            int h = 6;
        }
        return PhysicalAddress;
    }
}
