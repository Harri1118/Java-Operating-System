public class KernelMessage {
    private int senderPid;
    private int targetPid;
    private int messageSignal;
    private byte[] data;

    public String toString(){
        return "Sender PID: " + senderPid + ", Target PID: " + targetPid + ", Data: " + data.toString();
    }
    public KernelMessage(int senderPid, int targetPid, int messageSignal, byte[] data){
        this.senderPid = senderPid;
        this.targetPid = targetPid;
        this.messageSignal = messageSignal;
        this.data = data;
    }
    public KernelMessage(KernelMessage km){
        senderPid = km.getSenderPid();
        targetPid = km.getTargetPid();
        messageSignal = km.getMessageSignal();
        data = km.getData().clone();
    }

    public void setData(byte[] data){
        this.data = data;
    }

    public int getTargetPid(){return targetPid;}
    public int getSenderPid(){return senderPid;}
    public int getMessageSignal(){return messageSignal;}
    public byte[] getData(){
        return data;
    }
}
