public class Piggy extends UserLandProcess{
    byte byteInput = 'A';
    int progress;

    private final int SIZE = (100 * 1024);
    @Override
    public void main() {
        int pointer = OS.AllocateMemory(SIZE);
        //System.out.println(progress + " : "+ pointer);
            for (int i = 0; i < SIZE; i++) {
                Hardware.Write((pointer + i), (byte) byteInput);
            }
            for (int i = pointer; i < (pointer + SIZE); i++) {
                //Hardware.Read(i);
                System.out.println(i + ": "+ Hardware.Read(i));
            }
        OS.exit();
    }

    public Piggy(int i){
        byteInput += i;
        progress = i;
    }
}
