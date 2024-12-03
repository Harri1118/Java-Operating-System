import static java.lang.Thread.sleep;

public class Idle extends UserLandProcess{
    public void main() {
        // Idle process created
        while(true){
            try{
            Thread.sleep(50);}
            catch(Exception e){
                e.printStackTrace();
            }
            cooperate();
            //System.out.println("Idle is being called");
        }
    }
}
