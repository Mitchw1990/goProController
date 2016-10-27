/**
 * Created by Mitch on 7/15/2016.
 */
public class GoProCapture extends Thread {

    private String captureURL;
    private volatile boolean active;

    public GoProCapture(String captureURL){
        this.captureURL = captureURL;
        active = false;
    }

    public void run(){
            if (active) {
                captureURL += "0";
            }else{
                captureURL += "1";
            }



    }

    public void setActive(boolean active){
        this.active = active;
    }
}
