/*
 * Fills dataObj's
 */
import org.lcsim.event.EventHeader;
import org.lcsim.util.Driver;

public abstract class datasetPROCESS extends Driver {
    
    //fill stats object for whole detector
    public void processGROUP(EventHeader event, dataObj data){    
        //Count events processed
        data.EVENTS++;
        //Sum total MC Energy
        data.addMCE(event);
        //sum total detectable energy over all hits.
        data.addEn(event);
        //add hits to stats.hitsDPS
        data.hitsToDPS(event);
    }
    //fill stats object for detector cal
    public void processCAL(EventHeader event, dataObj data, String cal){
        //Count events processed
        data.EVENTS++;
        //Sum total MC Energy
        data.addMCE(event);
        //sum total detectable energy over all hits.
        data.addEn(event,cal);
        //add hits to stats.hitsDPS
        data.hitsToDPS(event,cal);
    }
    
}
