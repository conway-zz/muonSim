/**
 * Some useful functions for looking at cutoffs and stuff
 * @author agias
 */

import org.lcsim.event.SimCalorimeterHit;
import org.lcsim.event.EventHeader;
import org.lcsim.util.Driver;
import java.util.List;

public class timingAndEnergyFns extends Driver {
    
    //local threshhold variable, this value seems to work for now
    public double stdthresh = .98;
    
    public double totalEnergy(EventHeader event){
        double E_out = 0.0;
        List<List<SimCalorimeterHit>> myHitCol = event.get(SimCalorimeterHit.class);
        for(List<SimCalorimeterHit> myHits : myHitCol) {
            for(SimCalorimeterHit hit : myHits){
                E_out += hit.getRawEnergy();
            }
        }
        return E_out;
    }
     
     //returns first hit time.  Used in eventTimer
     public double timeFirstHit(EventHeader event){
        double tmin = 100000; //any value close to this is clearly invalid
        List<List<SimCalorimeterHit>> myHitCol = event.get(SimCalorimeterHit.class);
        for(List<SimCalorimeterHit> myHits : myHitCol) {
            for(SimCalorimeterHit hit : myHits){
                tmin = Math.min(hit.getTime(), tmin);
            }
        }
        return tmin;
    }
     
    //takes event and timing window, integrates E over dt.
    //window[lowerbound, upperbound]
    public double windowEnergy(EventHeader event, double[] window){
        double E_sum = 0;
        List<List<SimCalorimeterHit>> myHitCol = event.get(SimCalorimeterHit.class);
        for(List<SimCalorimeterHit> myHits : myHitCol) {
            for(SimCalorimeterHit hit : myHits){
                /*
                if(window[0] <= hit.getTime() && hit.getTime() <= window[1]) {
                    E_sum += hit.getRawEnergy();
                }
                */
                if(hit.getTime() < window[1]){
                    E_sum += hit.getRawEnergy();
                    System.out.println(E_sum);
                }
            }
        }
        return E_sum;
    }
    
    //simply determines when event started (raw time for now)
    //and when certain amount of total 
    //detectable energy has been detected. returns [start, end]
    //threshold is percentage of total energy to require before stop
    //ie, between 0 and 1
    //increment window by .01ns each loop.
    public double[] eventTimer(EventHeader event, double threshhold){
       
        if(!((threshhold>0.0)&&(threshhold<1.0))) {
           System.out.println("eventTimer needs threshhold between 0 and1");
        }                
                
        double rv[] = new double[2];
        rv[0] = timeFirstHit(event);
        rv[1] = rv[0];
        double total = totalEnergy(event);
        while((windowEnergy(event, rv)/total)<(threshhold)){
            rv[1] += 0.001;
        }
        return rv;
        
    }
    
    //returns the time of the last hit in the event.  Could be useful...
    public double timeLastHit(EventHeader event){
        double tmax = 0;
        List<List<SimCalorimeterHit>> myHitCol = event.get(SimCalorimeterHit.class);
        for(List<SimCalorimeterHit> myHits : myHitCol) {
            for(SimCalorimeterHit hit : myHits){
                tmax = Math.max(hit.getTime(), tmax);
            }
        }
        return tmax;
    }
    
    
    //##### Time Correction Functions #####//
    //averages corrected times
    //uses eventTimer to dynamically cut off end time based on threshold
    //manually figuring threshhold for now
    public double getTprime(EventHeader event) {
        System.out.println("what the..?");
        double sum = 0;
        double events = 0;
        double cutoff = eventTimer(event, stdthresh)[1];
        System.out.println("cutoff"+cutoff);
        List<List<SimCalorimeterHit>> myHitCol = event.get(SimCalorimeterHit.class);
        for(List<SimCalorimeterHit> myHits : myHitCol) {
            for(SimCalorimeterHit myhit : myHits){
                if(myhit.getTime()<cutoff){
                    events++;
                    sum += getCorrectTime(myhit);
                }
            }
        }       
        return (sum/events);
               
    }
    
    //gets correct time as described, c in mm/ns
    public double getCorrectTime(SimCalorimeterHit hit){
        return (hit.getTime()-getAbsRadius(hit.getPosition())/299.792458);
    }
    
    //simply calculates radius to [x,y,z] from [0,0,0]
    //ie., absolute radius
    public double getAbsRadius(double[] pos){
        
        return Math.sqrt(pos[0]*pos[0]+pos[1]*pos[1]+pos[2]*pos[2]); 
    }
    
    //want to plot distance over time
    
}
