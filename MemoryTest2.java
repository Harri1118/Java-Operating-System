public class MemoryTest2 extends UserLandProcess{
    @Override
    public void main() {
        // First address written & allocated, first string written on the first page.
        int address = OS.AllocateMemory(1024);
        String s = "Hello world!";
        System.out.println("String: " + "\""+s+"\"");
        int length = s.length();
        for(int i = 0; i < length; i++)
            Hardware.Write(address + i, (byte) s.charAt(i));
        for(int i = 0; i < length; i++)
            System.out.print(Hardware.Read(i) + " ");
        // First address is rewritten to check usability
        String s2 = "!dlrow olleH";
        System.out.println("\nString: " + "\""+s2+"\"");
        for(int i = 0; i < length; i++)
            Hardware.Write(address + i, (byte) s2.charAt(i));
        for(int i = 0; i < length; i++)
            System.out.print(Hardware.Read(i) + " ");
        // Second address created and allocated 2048 bytes. String is written to page to check functionality
        int address2 = OS.AllocateMemory(2048);
        String s3 = "This is a test";
        System.out.println("\nString: " + "\""+s3+"\"");
        length = s3.length();
        for(int i = 0; i < length; i++)
            Hardware.Write(address2 + i, (byte) s3.charAt(i));
        for(int i = address2; i < address2 + length; i++)
            System.out.print(Hardware.Read(i) + " ");
        // FreeMemory used twice to free the pages which were allocated.
        OS.FreeMemory(address, 1024);
        OS.FreeMemory(address2, 2048);
        OS.exit();
    }
}
