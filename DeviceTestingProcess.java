public class DeviceTestingProcess extends UserLandProcess{
    @Override
    public void main() {
        // Make open test command
        int id = OS.Open("file test");
        // Create array of size 10 to pass through file
        char character = 'A';
        char character2 = 'B';
        int size = 10;

        // Create a byte array of the specified size
        byte[] byteArray = new byte[size];

        // Fill the byte array with the ASCII value of the character
        for (int i = 0; i < size; i++) {
            if(i % 2 == 0)
                byteArray[i] = (byte) character;
            else
                byteArray[i] = (byte) character2;
        }
        // Write array onto first VFS pointer
        OS.Write(id, byteArray);
        byte[] test = OS.Read(id, 5);
        for(byte i : test)
            System.out.println(i);
        // Test if seek is functional.
        OS.Seek(id, 2);
        byte[] test2 = OS.Read(id, 4);
        for(byte i : test2)
            System.out.println(i);
        OS.Close(id);
        // RandomDevice tested
        int id2 = OS.Open("random 1000");
        byte[] test3 = OS.Read(id2, 10);
        for(byte i : test3)
            System.out.println(i);
        OS.Close(id2);
        OS.exit();
    }
}
