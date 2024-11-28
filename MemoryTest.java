public class MemoryTest extends UserLandProcess{

    @Override
    public void main() {
        // Write two basic addresses on the same page.
        int address = OS.AllocateMemory(2048);
        Hardware.Write(address, (byte) 'A');
        System.out.println(Hardware.Read(address));
        Hardware.Write(address + 1, (byte) 'B');
        System.out.println(Hardware.Read(address + 1));
        OS.FreeMemory(address, 2048);
        OS.exit();
    }
}
