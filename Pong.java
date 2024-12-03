import java.nio.ByteBuffer;

public class Pong extends UserLandProcess{
    @Override
    public void main() {

        while(true){
        KernelMessage sendMessage = new KernelMessage(OS.GetPid(), OS.GetPidByName("Ping"), 0, "3".getBytes());
        OS.SendMessage(sendMessage);
        KernelMessage k = OS.WaitForMessage();
            if(k == null){
                System.out.println("received is null.");
            }
            String dataString = new String(k.getData());
            int pingVal = Integer.parseInt(dataString);
            System.out.println("I am PONG, ping = " + pingVal);
            try {
                Thread.sleep(50);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
            cooperate();
        }
    }
}
