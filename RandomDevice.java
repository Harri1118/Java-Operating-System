import java.util.Random;

public class RandomDevice implements Device{
    // Stores the random objects within the device
    private Random[] devices = new Random[10];

    // Open parses a seed from the incoming string, if the string is not valid then it just calls currentTimeMillis as a seed for the next
    // available null position suitable for a random object.
    @Override
    public int Open(String s) {
        for(int i = 0; i < devices.length; i++){
            if(devices[i] == null){
                int seed = (s != null && !s.isEmpty()) ? Integer.parseInt(s) : (int) System.currentTimeMillis();
                if (s != null && !s.isEmpty())
                    seed = Integer.parseInt(s);
                else
                    seed = (int) System.currentTimeMillis();
                devices[i] = new Random(seed);
                return i;
            }
        }
        return 0;
    }

    // Close nulls id of given index.
    @Override
    public void Close(int id) {
        devices[id] = null;
    }

    // Read returns a byte[] of size utilizing r.nextBytes to fill the array with random values (all dependent off a seed).
    @Override
    public byte[] Read(int id, int size) {
        if(size < 1)
            return new byte[0];
        Random r = devices[id];
        byte[] bytes = new byte[size];
        r.nextBytes(bytes);
        return bytes;
    }

    // Seek goes to the random iteration and reads from the index of to within the random list.
    @Override
    public void Seek(int id, int to) {
        byte[] bytes = Read(id, to);
        System.out.println(bytes.toString());
    }

    // Do nothing.
    @Override
    public int Write(int id, byte[] data) {
        return 0;
    }
}
