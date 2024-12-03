public class HelloWorld extends UserLandProcess{
    public void main(){
        // Hello world process.
        System.out.println("Begin hello process");
        while(true){
            System.out.println("Hello world");
            //System.out.println("Hello PID: "+OS.GetPid());
            try {
                Thread.sleep(50);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
            cooperate();
        }
    }
}
