/*
 * Implements groupStats functions for dataset
 */
import org.lcsim.event.EventHeader;
public abstract class datasetPROCESS implements datasetProcessor{
    
    //fill stats object for whole detector
    public void processGROUP(EventHeader event, statsObj stats){    
        //Count events processed
        stats.EVENTS++;
        //Sum total MC Energy
        stats.addMCE(event);
        //sum total detectable energy over all hits.
        stats.addEn(event);
        //add hits to stats.hitsDPS
        stats.hitsToDPS(event);
    }
    //fill stats object for detector cal
    public void processCAL(EventHeader event, statsObj stats, String cal){
        //Count events processed
        stats.EVENTS++;
        //Sum total MC Energy
        stats.addMCE(event);
        //sum total detectable energy over all hits.
        stats.addEn(event,cal);
        //add hits to stats.hitsDPS
        stats.hitsToDPS(event,cal);
    }
    
}
