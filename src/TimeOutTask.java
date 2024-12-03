public class TimeOutTask extends UserLandProcess{
    public void main(){
        //System.out.println("Sleeping for 500 milliseconds");
        OS.Sleep(10000);
        int i = 0;
        while(i < 50){
        System.out.println("Incrementing with num: " + i);
            try {
                Thread.sleep(50);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
            i++;
        }
        System.out.println("Process completed");

        OS.exit();
    }
}
