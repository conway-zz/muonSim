/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import hep.aida.IHistogram1D;
import hep.aida.IHistogramFactory;
import org.lcsim.util.Driver;
import org.lcsim.event.EventHeader;
/**
 *
 * @author agias
 */
public class myDriver extends Driver {
    
    String[] names = {"GROUP","EM","H"};
    boolean hasRun = false;
    Driver driver;
    
    @Override
    protected void process(EventHeader event){
       if(!hasRun){
           this.runOnce();
       }
       this.processChildren(event);
    }
    @Override
    protected void endOfData(){
       
    }
    public void runOnce(){
        driver = new processor(names);
        this.add(driver);
        hasRun = true;
    }


}
