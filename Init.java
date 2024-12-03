public class Init extends UserLandProcess{
    public void main(){
        System.out.println("Init program initiated.");
        for(int i = 0; i <= 20; i++){
            OS.CreateProcess(new Piggy(i));
        }
        System.out.println("Init program completed.");
        OS.exit();
    }
}