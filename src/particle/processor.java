/*
 * runs processor when called by JAS3.
 * Run runOnce() first
 * uncomment separate processXX(event) methods to use them.
 */
import org.lcsim.event.EventHeader;
/**
 *
 * @author agias
 */
public abstract class processor implements statsInterface{
    
    String[] names = {"GROUP","EM","H"};
    dataObj GROUP;
    dataObj EM;
    dataObj H;
    boolean hasRun = false;
    
    EventHeader event1 = null;
    EventHeader event2 = null;
    
    protected void process(EventHeader event){
        //previous call
        event1 = event2;
        //current call
        event2 = event;
        //call previous
        subProcess(event1);
    }
    
    public void subProcess(EventHeader event2){
        if(!hasRun){
            runOnce();
            hasRun = true;
        }
        processGROUP(event1, GROUP);
        processCAL(event1, EM, "EM");
        processCAL(event1, H, "H");
    }
    
    //TODO: instantiate  statsObj for each name in names
    public void runOnce(){
        GROUP = new dataObj("GROUP");
        EM = new dataObj("EM");
        H = new dataObj("H");
    }
}
