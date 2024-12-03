import java.nio.ByteBuffer;

public class Ping extends UserLandProcess{
    @Override
    public void main(){

        while(true){
            KernelMessage sendMessage = new KernelMessage(OS.GetPid(), OS.GetPidByName("Pong"), 0, "2".getBytes());
            OS.SendMessage(sendMessage);
            KernelMessage k = OS.WaitForMessage();
            if(k == null){
                System.out.println("received is null.");
            }
            String dataString = new String(k.getData());
            int pongVal = Integer.parseInt(dataString);
            System.out.println("I am PING, pong = " + pongVal);
            try {
                Thread.sleep(50);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
            cooperate();

        }
    }
}
