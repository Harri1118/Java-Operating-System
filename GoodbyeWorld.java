public class GoodbyeWorld extends UserLandProcess{
    public void main(){
        // Goodbye world process.
        System.out.println("Begin goodbye process");
        while(true){
            System.out.println("Goodbye world");
            //System.out.println("Goodbye PID: "+OS.GetPid());
            try {
                Thread.sleep(50);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
            cooperate();
        }
    }
}
