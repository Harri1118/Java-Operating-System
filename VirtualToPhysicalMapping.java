public class VirtualToPhysicalMapping {
    public int PhysicalPageNumber;
    public int DiskPageNumber;

    // Define a new class VirtualToPhysicalMapping with public members for physical page number and on-disk page number.

    // Initialize these members to -1 in the constructor.
    public VirtualToPhysicalMapping(){
        PhysicalPageNumber = -1;
        DiskPageNumber = -1;
    }
}
