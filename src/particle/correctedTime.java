/**
 * ### Deprecated; absorbed by timingAndEnergyFns ###
 * Corrects time of initial impact by calculating 
 * the absolute radius of each cell and dividing
 * by the speed of light, then averaging the results.
 * @author agias
 */

import org.lcsim.event.EventHeader;
import java.util.List;
import org.lcsim.event.SimCalorimeterHit;
import org.lcsim.util.Driver;

public class correctedTime extends Driver {
    
    //averages corrected times
    public double getTprime(EventHeader event) {
        double sum = 0;
        int events = 0;
        List<List<SimCalorimeterHit>> myHitCol = event.get(SimCalorimeterHit.class);
        for(List<SimCalorimeterHit> myHits : myHitCol) {
            for(SimCalorimeterHit myhit : myHits){
                events++;
                sum += getCorrectTime(myhit);
            }
        }       
        return sum/events;
               
    }
    
    //gets correct time as described, c in mm/ns
    public double getCorrectTime(SimCalorimeterHit hit){
        double[] pos = hit.getPosition();
        return getAbsRadius(pos)/299.792458;
    }
    
    //simply calculates radius to [x,y,z] from [0,0,0]
    //ie., absolute radius.
    public double getAbsRadius(double[] pos){
        return Math.sqrt(pos[0]*pos[0]+pos[1]*pos[1]+pos[2]*pos[2]);        
    }
    
}
