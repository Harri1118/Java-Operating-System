public class Init extends UserLandProcess{
    public void main(){
        System.out.println("Init program initiated.");
        OS.CreateProcess(new MemoryTest());
        OS.CreateProcess(new MemoryTest2());
        System.out.println("Init program completed.");
        OS.exit();
    }
}