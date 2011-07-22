/*
 * runs processor when called by JAS3.
 * Run runOnce() first
 * uncomment separate processXX(event) methods to use them.
 */
import org.lcsim.event.EventHeader;
import org.lcsim.util.Driver;
/**
 *
 * @author agias
 */
public class processor extends aidaFunctions {
    
    String[] names;
    dataObj GROUP;
    dataObj EM;
    dataObj H;
    boolean hasRun = false;   
    
    //TODO: find a way to autoinstantiate, perhaps?
    public processor(String[] list){
        names = list;
        GROUP = new dataObj("GROUP");
        EM = new dataObj("EM");
        H = new dataObj("H");
    }
    
    protected void process(EventHeader event){
        processGROUP(event, GROUP);
        processCAL(event, EM, "EM");
        processCAL(event, EM, "EM");
    }
    
    //called when process(event) calls last
    protected void endOfData(){
        graphTpEn(GROUP);
        graphTpEn(EM);
        graphTpEn(H);
    }
    
    //TODO: instantiate  statsObj for each name in names
    public void runOnce(){
        GROUP = new dataObj("GROUP");
        EM = new dataObj("EM");
        H = new dataObj("H");
        hasRun = true;
    }
}
