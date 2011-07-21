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
public abstract class processor implements datasetProcessor {
    
    String[] names = {"GROUP","EM","H"};
    statsObj GROUP;
    //etc
    
    protected void process(EventHeader event){
        
    }
    
    //TODO: instantiate  statsObj for each name in names
    public void runOnce(){
        
    }
}
